package com.novamens.kbee.portal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ObjectId;
import com.novamens.content.multidimensional.FacetService;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DataAccessService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.indexer.service.SuggestionService;
import com.novamens.kbee.content.multidimensional.ClassifierFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.kbee.content.multidimensional.RelationFacet;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.service.SolrIndex;

import kbee.query.QueryHelpher;
import kbee.util.logging.Logger;

public class SearchSuggestionService implements SuggestionService {
	
	private Domain domain;
	private Site site;
	
	public static String DOCUMENT_ID_FIELD = "id";

	static private Logger logger = Logger.getLogger(SearchSuggestionService.class.getName());
	
	public SearchSuggestionService() {
	}
	
	public SearchSuggestionService(Domain domain) {
		this.domain = domain;
	}
	
	public SearchSuggestionService(Site site) {
		this.site = site;
	}

	public List<Suggestion> getSuggestions(String pattern) {
		return getSuggestions(pattern, null);
	}

	public List<Suggestion> getSuggestions(String pattern, Map<String, Object> parameters) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		try {
			String solrstatement;
			String sortfield;
			
			if ("".equals(pattern) || pattern==null) {
				sortfield = "title";
			}
			else {
				sortfield = null;
			}
			
			solrstatement = getStatement(pattern, parameters);
			
			String fields[] = { "id", "type", "title", "score" };
			QueryResponse response = ((SolrIndex)getIndex()).select(solrstatement, 
					null, 
					sortfield, 
					true, 
					0, 50, 
					false, 
					false, 
					0, 
					fields, 
					false, 
					true);
			SolrDocumentList results = response.getResults();
			
			// filtro por cota de relevancia y total de resultados
			int s = 0;
			for (int i=0; i<results.size() && s<20; i++) {
				SolrDocument solrdocument = results.get(i);
				float score = Float.valueOf(solrdocument.getFieldValue("score").toString());
				
				if (score>0.1) {
					String type = solrdocument.getFieldValue("type").toString();
					ObjectId objectid = new ObjectId(solrdocument.getFieldValue(DOCUMENT_ID_FIELD));
					if ("datasetmember".equals((type))) {
						ObjectId memberid = objectid;
						DataSetMember member = getContentDao()
								.findMemberById(Long.valueOf(memberid.getId()));
						if (member!=null && isVisible(member.getDataSet()) && isReadable(member)) {
							s++;
							for (String facet : getFacets(member)) {
								String label = getLabel(member);
								label += " - "+facet;
								//IModel<DataSetMember> model = new ObjectModel<DataSetMember>(member);
								suggestions.add(createSuggestion(member, label, facet, score));
							}
						}
					}
					else {
						s++;
						Content content;
						try {
							content = (Content)getContentDao()
									.findObjectById(objectid);
							if (content!=null) {
								if (site!=null && site.isDisplayValidVersion()) {
									Content version = content
										.getService(ContentService.class)
										.getValidVersion();
									if (version!=null && !content.equals(version)) {
										content = version;
									}
								}
								String label = content.getTitle();
								if (label==null) {
									label="";
								}	
								label = label.replace(" - ", " -- ");
								suggestions.add(createSuggestion(content, label, null, score));
							}
						} 
						catch (ContentMgmtException e) {
							logger.error(e);
						}
					}
				}
			}
		}
		catch (IndexerException e) {
			logger.error(e);
		}
		
		return suggestions;
	}
	

	
	protected String getStatement(String pattern, Map<String, Object> parameters) {
		String solrstatement;
		
		String type = parameters!=null ? (String)parameters.get("type") : null;
		
		if ("".equals(pattern) || pattern==null) {
			solrstatement = "(type:idoc OR type:text) AND head:true AND state:1 AND ";
		}
		else
		if (pattern.startsWith("\"") && pattern.startsWith("\"")) {
			solrstatement = "title:"+escape(pattern)+" AND ";
			if (type==null || type.contains("idoc")) {
				solrstatement += "(";
				solrstatement += "((type:idoc OR type:text) AND head:true AND state:1";
				String securityStatement = QueryHelpher.buildSecurityTerm(KbeePermission.READ);
				if (!"".equals(securityStatement)) solrstatement += " AND " + securityStatement;
				solrstatement += ") ";
			}		
			if (type==null || type.contains("datasetmember")) {
				solrstatement += "OR type:datasetmember^4";
			}	
			solrstatement += ") AND ";
		}
		else {
			solrstatement = "";
			if (pattern!=null && !"".equals(pattern.trim())) {
				solrstatement  = "(";
				solrstatement += "title:("+escape(pattern.trim())+") OR title:("+escape(pattern.trim())+"*)";
				for (Attribute attribute : getBoostedAttributes()) {
					solrstatement += " OR ";
					String un = attribute.getUniqueName()+"name_sort";
					solrstatement += un+":("+pattern.trim()+")^4"; 
					solrstatement += " OR ";
					solrstatement += un+":("+pattern.trim()+"*)^4";
				}
				solrstatement += ")";
				solrstatement += " AND ";
			}
			if (type==null || type.contains("idoc")) {
				solrstatement += "(";
				solrstatement += "((type:idoc OR type:text) AND head:true AND state:1";
				String contentFilterStatement = getContentFilterStatement();
				if (contentFilterStatement!=null && !"".equals(contentFilterStatement)) { 
					solrstatement += " AND (" + contentFilterStatement + ")";
				}	
				solrstatement += ") ";
			}		
			if (type==null || type.contains("datasetmember")) {
				solrstatement += "OR type:datasetmember^4";
			}	
			solrstatement += ") AND ";
		}
		
		solrstatement += " domain:" +String.valueOf(getDomain().getId());
		
		return solrstatement;
	}
	
	protected String getStatement(String pattern) {
		return null;
	}
	
	protected String getContentFilterStatement() {
		return null;
	}
	
	protected Classifier getClassifier(DataSet dataset) {
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (classifier.getDataSet()!=null && classifier.getDataSet().equals(dataset))
				return classifier;
		}
		return null;
	}
	
	protected boolean isVisible(DataSet dataset) {
		return dataset.isSuggester();
	}
	
	protected boolean isReadable(DataSetMember member) {
		return member.getDataSet().getService(DataAccessService.class).isReadable(member);
	}
	
	protected boolean isVisible(Facet facet) {
		return !"workspace".equals(facet.getDisplayName().toLowerCase());
	}
	
	protected List<String> getFacets(DataSetMember member) {
		List<String> facets = new ArrayList<String>(); 
		for (Facet facet : getFacets()) {
			if (isVisible(facet) && facet.isSuggester()) {
				if (facet instanceof FacetWrapper) 
					facet = ((FacetWrapper)facet).getFacet(); 
				if (facet instanceof ClassifierFacet) {
					ClassifierFacet  classifierfacet = (ClassifierFacet)facet; 
					if (((ClassifierFacet)facet).getDisplayName().equals(member.getDataSet().getName())) {
						facets.add(facet.getDisplayName());
					}
					else {
						if (classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
							facets.add(facet.getDisplayName());
						}
					}
				}
				else
				if (facet instanceof ClassifierHierarchicalFacet) {
					ClassifierHierarchicalFacet  classifierfacet = (ClassifierHierarchicalFacet)facet; 
					if (classifierfacet.getDisplayName().equals(member.getDataSet().getName())) {
						facets.add(facet.getDisplayName());
					}
					else {
						if (classifierfacet.getClassifier()!=null && (classifierfacet.getClassifier().getDataSet().equals(member.getDataSet()) ||
							(classifierfacet.getClassifier().getDataSet2()!=null && classifierfacet.getClassifier().getDataSet2().equals(member.getDataSet())))) {
							facets.add(facet.getDisplayName());
						}
					}
				}
				else
				if (facet instanceof RelationFacet) {
					if (((RelationFacet)facet).getClassName().equals("user") && member.getDataSet().getDataSetType().equals(DataSetType.USER)) {
						facets.add(facet.getDisplayName());
					}
				}
			}
		}
		return facets;
	}
	
	protected String getLabel(DataSet dataset) {
		return dataset.getName();
	}
	
	protected String escape(String text) {
		text = text.replace("\\", "\\\"");
		return text;
	}
	
	protected Suggestion createSuggestion(Object object, String label, String facet, float score) {
		return new KbeeSuggestion(object, label, facet, score, false);		
	}
	
	protected String getLabel(DataSetMember member) {
		String label = member.getDisplayName();
		label = label.replace("{", " ");
		label = label.replace("}", " ");
		label = label.replace("-", " ");
		return label;
	}
	
	protected List<Attribute> getBoostedAttributes() {
		List<Attribute> boosted = new ArrayList<>();
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if (attribute.getBoostFactor()>1) {
				boosted.add(attribute);
			}
		}
		return boosted;
	}
	
	public List<Facet> getFacets() {
		List<Facet> facets = new ArrayList<Facet>();
		
		facets.addAll(getDomain().getService(FacetService.class).getFacets(getIndex()));

		return facets;
	}
	
	public JavaIndex getIndex() {
		return (JavaIndex)((IndexProxy)getDomain().getService(JavaIndexerService.class).getIndex()).getIndex();
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

