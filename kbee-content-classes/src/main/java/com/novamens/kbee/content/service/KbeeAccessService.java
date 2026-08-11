package com.novamens.kbee.content.service;

import java.util.*;

import com.novamens.indexer.query.QuerySortOrder;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.SecuredSet;
import com.novamens.content.service.DataAccessService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.query.TextQuery;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.indexer.service.IndexerDocument;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.kbee.content.model.KbeeClassification;
import com.novamens.solr.indexer.iql.SolrIqlQuery;
import com.novamens.wicket.markup.html.form.WebSuggestion;
import com.novamens.wicket.model.ObjectModel;

import kbee.query.QueryHelpher;

@SuppressWarnings("serial")
public abstract class KbeeAccessService implements DataAccessService {
	
	private List<DataSet> datasets;
	private ClassifierTemplate template;

	public KbeeAccessService(DataSet dataset) {
		this.datasets = new ArrayList<DataSet>();
		this.datasets.add(dataset);
	}
	
	public KbeeAccessService(ClassifierTemplate template) {
		this.datasets = new ArrayList<DataSet>();
		this.datasets.add(template.getClassifier().getDataSet());
		if (template.getClassifier().getDataSet2()!=null) {
			this.datasets.add(template.getClassifier().getDataSet2());
		}
		setTemplate(template);
	}
	
	public List<DataSet> getDataSets() {
		return datasets;
	}
	
	public ClassifierTemplate getRelationTemplate() {
		return template;
	}
	
	public void setTemplate(ClassifierTemplate template) {
		this.template = template;
	}
	
	public List<DataSetMember> getAll() {
		return getAll(null);
	}
	
	public List<DataSetMember> getAll(Classificable object) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();

		DataSetMember parentValue = getParentValue(object);
		boolean reverse = getRelationTemplate()!=null && getRelationTemplate().isReverse();
		List<DataSetMember> parentmembers = getMembers(parentValue);
		
		if (!parentmembers.isEmpty()) {
			for (DataSetMember member : parentmembers) {
				if (isReadable(member)) {
					members.add(member);
				}
			}
		}
		else {
			String statement = getStatement(null, object, null);
			
			
			TextQuery query = new TextQuery(statement) {
				public String[] fields() {
					String[] fields = {"id", "title", "lastmodifiedtime"};
					return fields;
				}
			};
			query.setPageSize(150);
			query.setFaceted(false);
			query.setSortField("title_sort");
			
			QueryResponse response = (QueryResponse)getIndex().execute(query);
			SolrDocumentList results = response.getResults();
			for (int i=0; i<results.size(); i++) {
				SolrDocument document = results.get(i);
				DataSetMember member = getMember(document);
				if (member!=null) {
					if (parentValue==null || reverse  || parentmembers.contains(member)) {
						members.add(member);
					}
				}
			}
		}

		return members;
	}

	
	public long getTotalMembers() {

		String statement = getStatement(null, null, null);
		
		TextQuery query = new TextQuery(statement) {
				public String[] fields() {
					String[] fields = {"id"};
					return fields;
				}
		};
		query.setPageSize(1);
		query.setFaceted(false);
		query.setSortField("title_sort");
		
		QueryResponse response = (QueryResponse)getIndex().execute(query);
		
		long total = response.getResults().getNumFound();
	
		return total;
	}
	
	public List<Suggestion> getSuggestions(String pattern) {
		return getSuggestions(pattern, null);
	}

	public List<Suggestion> getSuggestions(String pattern, Classificable object) {
		try {
			return getSuggestions(pattern, object, new HashMap<>());
		}
		catch (Exception e) {
			return new ArrayList<Suggestion>();
		}
	}
	
	public List<Suggestion> getSuggestions(String pattern, Classificable object, Map<String, Object> parameters) {
		
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		int maxResults = 120;

		//pattern = pattern.replace("-", " ");
		
		String statement = getStatement(pattern, object, parameters);
		
		TextQuery query = new TextQuery(statement);

		query.setFaceted(false);
		query.setSortField("".equals(pattern) ? "title_sort" : "score, title_sort");

		if(parameters!=null && parameters.containsKey("querySortOrder")){
			QuerySortOrder querySortOrder = (QuerySortOrder) parameters.get("querySortOrder");
			if (!"".equals(pattern)) {
				query.setSortField(querySortOrder.getSolrSortFields());
				query.setAscending(querySortOrder.isSolrSortAscending());
			}
		}

		if(parameters!=null && parameters.containsKey("maxResults")){
			maxResults=(Integer)parameters.get("maxResults");
		}
		query.setPageSize(maxResults);

		QueryResponse response = (QueryResponse)getIndex().execute(query);
		
		DataSetMember parent = getParentValue(object);
		boolean reverse = getRelationTemplate()!=null && getRelationTemplate().isReverse();
		List<DataSetMember> parentmembers = getMembers(parent);
		
		if (!parentmembers.isEmpty()) {
			for (DataSetMember member : parentmembers) {
				if (isReadable(member) && isSuggestion(pattern, member)) {
					WebSuggestion suggestion = new WebSuggestion(new ObjectModel<DataSetMember>(member), getDisplayName(member), 0, false);
					suggestions.add(suggestion);
				}
			}
			Collections.sort(suggestions, new Comparator<Suggestion>() {
				@Override
				public int compare(Suggestion a, Suggestion b) {
					try {
						String texta = a.getText();
						String textb = b.getText();
						if (texta==null) return 1;
						if (textb==null) return -1;
						return texta.toLowerCase().compareTo(textb.toLowerCase());
					} 
					catch (Exception e) {
						return 0;
					}
				}
			});
		}
		else {
			SolrDocumentList results = response.getResults();
			for (int i=0; i<results.size() && suggestions.size()<maxResults; i++) {
				SolrDocument document = results.get(i);
				DataSetMember member = getMember(document);
				if (member!=null) {
					if (parent==null || reverse  || parentmembers.contains(member)) {
						Float score = (Float)document.get("score");
						WebSuggestion suggestion = new WebSuggestion(new ObjectModel<DataSetMember>(member), getDisplayName(member), score, false);
						suggestions.add(suggestion);
					}
				}
			}
		}

		return suggestions;
	}
	
	public Domain getDomain() {
		return getDataSets().get(0).getDomain();
	}
	
	public JavaIndex getIndex() {
		return (JavaIndex)((IndexProxy)getDomain().getService(JavaIndexerService.class).getIndex()).getIndex();
	}
	
	protected DataSetMember getParentValue(Classificable object) {
		Classification classification = getParentRelation(object);
		if (classification!=null) 
			return classification.getDataSetMember();
		return null;
	}
	
	protected Classification getParentRelation(Classificable object) {
		if (object!=null && getRelationTemplate()!=null && getRelationTemplate().getParent()!=null) {
			for (Classification classification : object.getClassification()) {
				if (classification.getClassifier().equals(getRelationTemplate().getParent())) {
					return classification;
				}
			}
		}
		if (object instanceof DataSetMember && 
			getRelationTemplate().getParent()!=null &&
			((DataSetMember)object).getDataSet().equals(((Classifier)getRelationTemplate().getParent()).getDataSet())) {
			KbeeClassification classification = new KbeeClassification();
			classification.setClassifier((Classifier)getRelationTemplate().getParent());
			classification.setDataSetMember((DataSetMember)object);
			return classification;
		}
		return null;
	}
	
	protected List<DataSetMember> getMembers(DataSetMember parentValue) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		if (parentValue!=null) {
			for (Classification classification : parentValue.getClassification()) {
				if (classification!=null && classification.getClassifier().equals(getRelationTemplate().getClassifier())) {
					members.add(classification.getDataSetMember());
				}
			}
		}
		return members;
	}
	
	protected String getStatement(String pattern, Classificable object, Map<String, Object> parameters) {
		boolean securedSet = false;
		StringBuffer statement = new StringBuffer(100);
		String datasetpredicate ="(";
		for (DataSet dataset : getDataSets()) {
			datasetpredicate += "(".equals(datasetpredicate) ? "" : " OR ";
			datasetpredicate += "dataset:" +String.valueOf(dataset.getId());
			if (dataset instanceof SecuredSet) securedSet = true;
		}
		datasetpredicate +=")";
		if ("".equals(pattern) || pattern==null) {
			//statement.append("dataset:" +String.valueOf(dataset.getId()));
			statement.append(datasetpredicate);
		}
		else {
			if (pattern.startsWith("\"")) {
				statement.append("(title:"+ pattern + ")" + " AND " +datasetpredicate);
			}
			else {
				if (pattern.contains("(")) {
					statement.append("(title:\""+ pattern + "\")" + " AND " +datasetpredicate);
				}
				else {
					statement.append("(title:"+ pattern + "^8 OR title:" + pattern+"*^2 OR title_sort:"+ pattern+")" + " AND " +datasetpredicate);
				}
			}
		}
		statement.append(" AND domain:" +String.valueOf(getDomain().getId()));
		statement.append(" AND state:1 AND type:datasetmember");
		
		// Reversa es la relacion definida sobre todos los elementos del dataset que hacen referencia al padre 
		if (getRelationTemplate()!=null && getRelationTemplate().isReverse()) {
			// El padre se busca en el contexto definido por el objeto (clasificable)
			Classification classification = getParentRelation(object);
			if (classification!=null) {
				statement.append(" AND "+classification.getClassifier().getUniqueName()+"member:" + String.valueOf(classification.getDataSetMember().getId()));
			}
			else {
				// Si el padre no existe en el contexto el resultado debe ser vacio
				ModelElement parent = getRelationTemplate().getParent();
				if (parent!=null && parent instanceof Classifier) {
					statement.append(" AND "+((Classifier)parent).getUniqueName()+"member:undefined");
				}
			}
			
		}
		
		if (parameters!=null && parameters.get("qf")!=null) {
			String filterstatement = getIqlStatement((String)parameters.get("qf"));
			if (filterstatement!=null && !"".equals(filterstatement)) {
				statement.append(" AND ("+filterstatement+")");
			}
		}
		
		if (securedSet) {
			String readersStatement = getReadersStatement();
			if (!"".equals(readersStatement)) {
				statement.append(" AND ("+readersStatement+")");
			}
		}
		
		return statement.toString();
	}
	
	protected String getDisplayName(DataSetMember member) {
		if (member.getParents()!=null && !member.getParents().isEmpty()) {
			String displayName = member.getDisplayName();
			DataSetMember parent = member.getParents().get(0);
			while (parent!=null) {
				displayName = parent.getDisplayName() + "/" + displayName;
				parent = parent.getParents()!=null && !parent.getParents().isEmpty()
					? parent.getParents().get(0) 
					: null;
			}
			return displayName;
		}
		else {
			return member.getDisplayName();
		}
	}
	
	protected boolean isSuggestion(String pattern, DataSetMember member) {
		if (pattern==null) 
			return true;
		String value = member.getDisplayName();
		if (value==null) 
			return false;
		if (value.toLowerCase().contains(pattern.toLowerCase()))
			return true;
		return false;
	}
	
	protected DataSetMember getMember(SolrDocument solrdocument) {
		IndexerDocument document = new IndexerDocument();
		Object documentId = solrdocument.getFieldValue("id");
		document.setId(documentId.toString());
		for (String field : solrdocument.getFieldNames()) {
			document.addField(field, solrdocument.getFieldValue(field).toString());
		}
		Object object = getIndex().getObjectBuilder().build(document);
		return (DataSetMember)object;
	}
	
	protected String getIqlStatement(String iql) {
		IqlService iqlservice = getDomain().getService(IqlService.class);
		IqlQuery query = iqlservice.getNewQuery(iql);
		String solrstatement = ((SolrIqlQuery)query).getSolrStatement();
		return solrstatement;
	}
	
	protected String getReadersStatement() {
		return QueryHelpher.buildSecurityTerm("read");
//
//		String statement = "";
//		
//		SecurityService service = ServiceLocator.getService(SecurityService.class);
//		
//		User user = service.getSessionUser();
//		
//		boolean admin = service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
//		boolean support = service.isMember(KbeeGlobalRole.SUPPORT.getId());
//		
//		if (!service.isRoot() && !admin && !support) {
//			List<String> readers = new ArrayList<>();
//			readers.add(String.valueOf(user.getId()));
//			for (Group group : user.getGroups()) {
//				readers = getReaders(group, readers);
//			}
//			for (String principal : readers) {
//				if (!"".equals(statement))
//					statement += " OR ";
//				statement += "reader:"+principal;
//			}
//		}	
//		
//		return statement;
	}

//	protected List<String> getReaders(Group group, List<String> readers) {
//	
//		String id = ((KbeeGroup)group).getId().toString();
//	
//		if (readers.contains(id)) 
//			return readers;
//		
//		readers.add(id);
//	
//		for (Group parent : ((KbeeGroup)group).getGroups()) {
//			readers = getReaders(parent, readers);
//		}
//		
//		return readers;
//	}
}