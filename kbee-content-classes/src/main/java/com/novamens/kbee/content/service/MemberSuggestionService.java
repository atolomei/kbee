package com.novamens.kbee.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novamens.indexer.query.QuerySortOrder;
import org.apache.logging.log4j.LogManager;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

 import com.novamens.content.base.Content;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.EntitySet;
import com.novamens.content.text.template.VariableResolver;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.query.TextQuery;
import com.novamens.indexer.service.IndexerDocument;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.indexer.service.SuggestionService;
import com.novamens.kbee.content.text.template.KbeeContentTextTemplate;
import com.novamens.kbee.content.util.ContentVariableResolver;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.iql.SolrIqlQuery;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.wicket.markup.html.form.WebSuggestion;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;

@Deprecated
public class MemberSuggestionService implements SuggestionService {
	
	private DataSet dataset;
	private Classifier classifier;
	private ClassifierTemplate template;
	private boolean hierarchical = false;
	
	private static Logger logger = new Logger(LogManager.getLogger(MemberSuggestionService.class.getName()));

	public MemberSuggestionService() {
	}
	
	public MemberSuggestionService(Classifier classifier) {
		this.classifier = classifier;
		this.dataset = classifier.getDataSet();
		this.hierarchical = this.dataset.isHierachical();
	}
	
	public MemberSuggestionService(ClassifierTemplate template) {
		this.classifier = template.getClassifier();
		this.template = template;
		this.dataset = classifier.getDataSet();
		this.hierarchical = this.dataset.isHierachical();
	}
	
	public MemberSuggestionService(DataSet dataset) {
		this.dataset = dataset;
		this.hierarchical = this.dataset.isHierachical();
	}

	public List<Suggestion> getSuggestions(String pattern) {
		return getSuggestions(pattern, null);
	}
	
	public List<Suggestion> getSuggestions(List<DataSetMember> classification) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		for (DataSetMember member : classification) {
			if (getDataSet().isAFunctionOf(member.getDataSet())) {
				for (Classification memberclassification : ((Classificable)member).getClassification()) {
					if (memberclassification.getDataSetMember().getDataSet().equals(getDataSet())) {
						WebSuggestion suggestion = new WebSuggestion(new ObjectModel<DataSetMember>(memberclassification.getDataSetMember()), memberclassification.getDataSetMember().getStrValue(), 0, false);
						suggestions.add(suggestion);
					}
				}
				return suggestions;
			}
		}
		for (DataSetMember member : getUserRelations()) {
			WebSuggestion suggestion = new WebSuggestion(new ObjectModel<DataSetMember>(member), member.getStrValue(), 0, false);
			suggestions.add(suggestion);
		}
		return suggestions;
	}
	
	public List<Suggestion> getSuggestions(String pattern, Map<String, Object> parameters) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		try {
			
			if (parameters!=null && parameters.get("template")!=null && parameters.get("template") instanceof ClassifierTemplate) {
				template = (ClassifierTemplate)parameters.get("template");
			}

			List<String> relationsmembers = getRelationsMembers(parameters);
			
			String statement = getStatement(pattern, parameters, relationsmembers);
			
			TextQuery query = new TextQuery(statement);
			query.setPageSize(5000);
			query.setFaceted(false);
			query.setSortField("".equals(pattern) ? "title_sort" : null);

			if(parameters!=null && parameters.containsKey("querySortOrder")){
				QuerySortOrder querySortOrder = (QuerySortOrder) parameters.get("querySortOrder");
				query.setSortField(querySortOrder.getSolrSortFields());
				query.setAscending(querySortOrder.isSolrSortAscending());
			}
			if(parameters!=null && parameters.containsKey("maxResults")){
				query.setPageSize((Integer)parameters.get("maxResults"));
			}

			
			QueryResponse response = (QueryResponse)getIndex().execute(query);
			
			SolrDocumentList results = response.getResults();
			for (int i=0; i<results.size(); i++) {
				SolrDocument document = results.get(i);
				String indexid = document.getFieldValue("id").toString();
				int c = indexid.indexOf("#");
				String id = indexid.substring(c+1);
				if (relationsmembers.isEmpty() || relationsmembers.contains(id)) {
					DataSetMember member = getMember(document);
					if (member!=null) {
						WebSuggestion suggestion = new WebSuggestion(new ObjectModel<DataSetMember>(member), getDisplayName(member), 0, false);
						suggestions.add(suggestion);
					}
				}
			}
		}
		catch (IndexerException e) {
			logger.error(e);
		}
		
		return suggestions;
 	}
	
	public JavaIndex getIndex() {
		return (JavaIndex)getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	public DataSet getDataSet() {
		return dataset;
	}
	
	public ClassifierTemplate getTemplate() {
		return template;
	}
	
	@SuppressWarnings("unchecked")
	private String getStatement(String pattern, Map<String, Object> parameters, List<String> relationsmembers) {
		StringBuffer statement = new StringBuffer(100);
		
		if ("".equals(pattern) || pattern==null) {
			statement.append("dataset:" +String.valueOf(getDataSet().getId()));
		}
		else {
			if (isKeyword(pattern)) {
				statement.append("(\""+pattern+"\")" + " AND dataset:" +String.valueOf(dataset.getId()));
			}
			else {
				statement.append("("+ pattern + " OR " + pattern+"*)" + " AND dataset:" +String.valueOf(dataset.getId()));
			}
		}
		statement.append(" AND domain:" +String.valueOf(getDomain().getId()));
		statement.append(" AND state:1 AND type:datasetmember");
		if (parameters!=null && parameters.get("root")!=null) {
			statement.append(" AND parent:" + String.valueOf(((DataSetMember)parameters.get("root")).getId()) +"*");
		}
		if (template!=null && template.getParent()!=null) {
			//if (template.getClassifier().getDataSet().isAggregation()) {
				List<Classification> classifications = null;
				if (parameters!=null && parameters.get("classification2")!=null) {
					classifications = (List<Classification>)parameters.get("classification2");
				}
				if (classifications!=null) {
					for (Classification classification : classifications) {
						if (classification.getClassifier().equals(template.getParent())) {
							DataSetMember parent = classification.getDataSetMember();
							if (!parent.getClassification(template.getClassifier()).isEmpty() ||
									template.getClassifier().getDataSet().isAggregation()) {
								statement.append(" AND "+classification.getClassifier().getUniqueName()+"member:" + String.valueOf(classification.getDataSetMember().getId()));
							}
						}
					}
				}
			//}
		}
		
		if (!relationsmembers.isEmpty()) {
			statement.append(" AND (");
			String orstatement = "";
			for (String memberid : relationsmembers) {
				if (!"".equals(orstatement)) orstatement += " OR ";
				orstatement += "id:kbee*"+memberid;
			}
			statement.append(orstatement + ")");
		}
		
		if (parameters!=null && parameters.get("members")!=null) {
			if (!((List<String>)parameters.get("members")).isEmpty()) {
				String clause = getMembersClause((List<String>)parameters.get("members"));
				if (clause.length()>0) statement.append(" AND ");
				statement.append(clause);
			}
		}
		
		if (getTemplate()!=null && getTemplate().getValuesCriteria()!=null && !"".equals(getTemplate().getValuesCriteria())) {
			try {
				KbeeContentTextTemplate template = new KbeeContentTextTemplate(getTemplate().getValuesCriteria());
				VariableResolver resolver = new ContentVariableResolver<Content>(null, null) {
					protected Object getClassification(String classifiername) {
						List<Classification> classifications = null;
						if (parameters!=null && parameters.get("classification2")!=null) {
							classifications = (List<Classification>)parameters.get("classification2");
						}
						for (Classification classification : classifications) {
							if (classification!=null && classification.getClassifier().getName().toLowerCase().equals(classifiername.toLowerCase())) {
								if (classification.getDataSetType().equals(DataSetType.DATE))
									return classification.getDateValue();
								else	
									return classification.getDataSetMember();
							}
						}
						return null;
					}
					@Override
					public String getDefaultValue() {
						return "null";
					}
				};
				String criteria = template.getText(resolver);
				criteria = criteria.replace("ó", "o");
				IqlService iqlservice = getDomain().getService(IqlService.class);
				IqlQuery query = iqlservice.getNewQuery(criteria);
				String solrcriteria = ((SolrIqlQuery)query).getSolrStatement();
				if (solrcriteria!=null && !"".equals(solrcriteria)) {
					statement.append(" AND ("+solrcriteria +")");
				}
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		
		
		return statement.toString();
	}
	
	private String getDisplayName(DataSetMember member) {
		if (hierarchical && member.getParents()!=null && !member.getParents().isEmpty()) {
			String displayName = member.getDisplayName();
			DataSetMember parent = member.getParents().get(0);
			while (parent!=null) {
				displayName = parent.getDisplayName() + "/" + displayName;
				parent = parent.getParents()!=null && !parent.getParents().isEmpty() ? parent.getParents().get(0) : null; 
			}
			return displayName;
		}
		else {
			return member.getDisplayName();
		}
	}
	
	// Una clasificacion  y las relaciones del usuario pueden determinar funcionalmente al 
	// conjuntos de sugerencias a retornar.
	@SuppressWarnings("unchecked")
	private List<String> getRelationsMembers(Map<String, Object> parameters) {
		List<String> relationsmembers = new ArrayList<String>();
		List<DataSetMember> classifiedmembers = null;
		List<Classification> classifications = null;
		ClassifierTemplate template = null;
		
		if (parameters!=null && parameters.get("classification")!=null) {
			classifiedmembers = (List<DataSetMember>)parameters.get("classification");
		}
		if (parameters!=null && parameters.get("classification2")!=null) {
			classifications = (List<Classification>)parameters.get("classification2");
		}
		if (parameters!=null && parameters.get("template")!=null) {
			template = (ClassifierTemplate)parameters.get("template");
		}

		if (classifiedmembers!=null) {
			for (DataSetMember member : classifiedmembers) {
				if (getDataSet().isAFunctionOf(member.getDataSet())) {
					for (Classification memberclassification : ((Classificable)member).getClassification()) {
						if (memberclassification.getDataSetMember().getDataSet().equals(getDataSet())) {
							relationsmembers.add(memberclassification.getDataSetMember().getId().toString());
						}
					}
				}
			}
		}
		
		if (classifications!=null && template!=null) {
			for (Classification classification : classifications) {
				if (classification.getClassifier().equals(template.getParent())) {
					for (Classification memberclassification : ((Classificable)classification.getDataSetMember()).getClassification()) {
						if (memberclassification.getDataSetMember().getDataSet().equals(getDataSet())) {
							relationsmembers.add(memberclassification.getDataSetMember().getId().toString());
						}
					}
				}
			}
		}
		
		if (relationsmembers.isEmpty()) {
			for (String member : getUserRelationsIds()) {
				//if (member.getDataSet().equals(getDataSet())) {
					relationsmembers.add(member);
				//}
			}
		}
		
		return relationsmembers;
	}
	
	private DataSetMember getMember(SolrDocument solrdocument) {
		IndexerDocument document = new IndexerDocument();
		Object documentId = solrdocument.getFieldValue("id");
		document.setId(documentId.toString());
		for (String field : solrdocument.getFieldNames()) {
			document.addField(field, solrdocument.getFieldValue(field).toString());
		}
		Object object = getIndex().getObjectBuilder().build(document);
		return (DataSetMember)object;
	}
	
	// Si un usuario no es domain admin y no tiene acceso a todos los datasets solo se pueden ofrecer 
	// aquellas entidades sobre las cuales cumple con algún rol
	private List<DataSetMember> getUserRelations() {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		
		if (getTemplate()!=null && (getTemplate().getAccessibility()==null || getTemplate().getAccessibility().equals(AccessStrategy.All)))
			return members;
		
		if (getTemplate()==null)
			return members;
		
		if (!(getTemplate().getClassifier().getDataSet() instanceof EntitySet))
			return members;
		
		for (UserRole role : ServiceLocator.getService(UserService.class).getSessionUserProfile().getRoles()) {
			if (role.getRole().isEntity()) {
				EntityMember entity = role.getEntity();
				if ((classifier!=null && entity.getDataSet().equals(classifier.getDataSet())) ||
						dataset!=null && entity.getDataSet().equals(dataset)) {
					members.add(entity);
				}
			}
		}
		
		return members;
	}
	
	private List<String> getUserRelationsIds() {
		List<String> members = new ArrayList<String>();
		
		if (getTemplate()!=null && (getTemplate().getAccessibility()==null || getTemplate().getAccessibility().equals(AccessStrategy.All)))
			return members;
		
		if (getTemplate()==null)
			return members;
		
		if (!(getTemplate().getClassifier().getDataSet() instanceof EntitySet))
			return members;
		
		if (ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId())) {
			return members;
		};
		
		for (UserRole role : ServiceLocator.getService(UserService.class).getSessionUserProfile().getRoles()) {
			if (role.getRole().isEntity()) {
				EntityMember entity = role.getEntity();
				if ((classifier!=null && entity.getDataSet().equals(classifier.getDataSet())) ||
						dataset!=null && entity.getDataSet().equals(dataset)) {
					members.add(entity.getId().toString());
				}
			}
		}
		
		if (members.isEmpty()) {
			members.add("x");
		}
		
		return members;
	}
	
	@SuppressWarnings("unused")
	private List<DataSetMember> getMemberRelations(List<DataSetMember> explored, DataSetMember member) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (Classification classification : ((Classificable)member).getClassification()) {
			DataSetMember classified = classification.getDataSetMember();
			if (classifier!=null && classification.getClassifier().equals(classifier)) {
				
			}
			else 
			if (classifier==null && classification.getClassifier().getDataSet().getId().equals(dataset.getId())) {
				members.add(classified);
			}
			else {
				if (!explored.contains(classified)) {
					explored.add(classified);
					members.addAll(getMemberRelations(explored, member));
				}
			}
		}
		return members;
	}
	
	private String getMembersClause(List<String> members) {
		StringBuilder statement = new StringBuilder();
		for (String member : members) {
			if (member.contains("|")) {
				String ormembers[] = member.split("\\|");
				if (statement.length()>0) 
						statement.append(" AND ");
				statement.append("(");
				for (int m=0; m<ormembers.length; m++) {
					String ormember = ormembers[m];
					if (ormember.length()>0) {
						int i = ormember.indexOf("/");
						String facetname = ormember.substring(0,i);
						String memberid = ormember.substring(i+1);
						SolrFacet facet = (SolrFacet)getIndex().getCube().getFacet(facetname);
						if (m>0) 
							statement.append(" OR ");
						statement.append(facet.getName() +":" + memberid);
					}
				}
				statement.append(")");
			}
			else {
				if (member.length()>0) {
					int i = member.indexOf("/");
					String facetname = member.substring(0,i);
					String memberid = member.substring(i+1);
					SolrFacet facet = (SolrFacet)getIndex().getCube().getFacet(facetname);
					if (facet!=null) {
						if (memberid.contains(" ") && !memberid.startsWith("[")) 
							memberid="\""+memberid+"\"";
						if (statement.length()>0) 
							statement.append(" AND ");
						statement.append(facet.getName() +":" + memberid);
					}
				}
			}
		}
		return statement.toString();
	}
	
	private boolean isKeyword(String pattern) {
		if (pattern==null) return false;
		String lower = pattern.toLowerCase().trim();
		return lower.equals("no"); 
	}
}
