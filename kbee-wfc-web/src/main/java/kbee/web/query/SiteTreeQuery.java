package kbee.web.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.library.Library;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.PersonMember;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.Criteria;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.TextFilter;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.library.IqlCriteria;
import com.novamens.kbee.content.multidimensional.ClassifierFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.kbee.content.multidimensional.RelationFacet;
import com.novamens.kbee.content.userlist.UserListResultSetWrapper;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeNode;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeProvider;
import com.novamens.portal6.model.Site;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.dataset.DataSetNode;

public class SiteTreeQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;
	
	private boolean includeFacts = true;
	private boolean writeables = false;
	private String orderSet;
	private String membersolrclause = null;
	
	String nodeId = null;
	Serializable dataSetId = null;
	Serializable domainId = null;
	String classifierName = null;
	
	IModel<TreeProvider<TreeNode<DataSetMember>>> treeSupplier;
	
	public SiteTreeQuery(Site site, DataSet dataSet, Index index) {
		this(site, dataSet, index, null, null);
	}
	
	public SiteTreeQuery(Site site, 
			DataSet dataSet, 
			Index index, 
			IModel<TreeProvider<TreeNode<DataSetMember>>> treeSupplier,
			Map<String, Object> parameters) {
		
		super(index);
		
		Object textvalue = parameters!=null ? parameters.get("text") : null;
		
		if (textvalue!=null && textvalue instanceof String) {
			parameters.put("text", new TextFilter((String)textvalue));
		}
		
		getParameters().put("head", "true");
		getParameters().put("type", "idoc");
		getParameters().put("state", "1");
		
		User user =  ServiceLocator.getService(SecurityService.class).getSessionUser();
		
		if (parameters!=null && "true".equals(parameters.get("writeables"))) {
			writeables = true;
			if (!isAdmin(user) && !isSupport(user)) {
				getFilterParameters().put("writer", "["+getPrincipals(user)+"]");
			}
		}
		
		this.treeSupplier = treeSupplier;
		
		if (parameters!=null && parameters.get("text")!=null) {
			getParameters().put("sort", "relevance");
			getParameters().put("ascending", "false");
		} 
		else {
			getParameters().put("sort", "modified");
			getParameters().put("ascending", "false");
		}
		
		Criteria sitecriteria = getCriteria(site);
		if (sitecriteria!=null) {
			for (String parametername : sitecriteria.getParameters().keySet()) {
				getFilterParameters().put(parametername, sitecriteria.getParameters().get(parametername));
			}
		}
		
		if (parameters!=null) {
			for (String parametername : parameters.keySet()) {
				if (!"writeables".equals(parametername)) {
					getParameters().put(parametername, parameters.get(parametername));
				}
			}
		}
		
		if (!isAdmin(user) && !isSupport(user)) {
			getFilterParameters().put("reader", "["+getPrincipals(user)+"]");
		}
		
		dataSetId = dataSet.getId();
		domainId = dataSet.getDomain().getId();
		Classifier classifier = getClassifier(dataSet);
		classifierName = classifier!=null ? classifier.getUniqueName() : "x";
	}
	
	@Override
	public void setParameter(String name, Object value) {
		if (value==null) {
			nodeId=null;
			getParameters().remove("members");
			getParameters().remove(name);
		}
		else {
			if ("node".equals(name))
				setNode(((DataSetNode)value).getObject());
			else
				super.setParameter(name, value);
		}
	}
	
	@Override
	public String[] fields() {
		String fields[] = { "id", "title", "score" };
		return fields;
	}
	
	public String getSortField() {
		return "type asc, " + super.getSortField();
	}

	@Override
	public ResultSet execute() {
		String sortfield = (String)getParameters().get("sort");
		
		if (sortfield==null || !sortfield.startsWith("type")) {
			if (sortfield==null) sortfield="";
			sortfield = "type asc" + (sortfield==null ? "" : ", " + sortfield);
			getParameters().put("sort", sortfield);
		}
		
		getParameters().put("sort", "type asc, title_sort asc");
		ResultSet resultSet = new UserListResultSetWrapper(super.execute());
	
		if (writeables && resultSet.size()==0) {
			List<IModel<DataSetMember>> resultList = new ArrayList<>();
			Iterator<? extends TreeNode<DataSetMember>> nodes;
			if (nodeId==null) {
				nodes = treeSupplier.getObject().getRoots();
			}
			else {
				TreeNode<DataSetMember> node = getNode(nodeId);
				if (node!=null) {
					nodes = treeSupplier.getObject().getChildren(node);
				}
				else {
					return resultSet;
				}
			}
			while (nodes.hasNext()) {
				resultList.add(new ObjectModel<DataSetMember>(nodes.next().getObject()));
			}
			resultSet = new ListModelResultSet<DataSetMember>(resultList);
		}
		
		return resultSet;
	}
	
	public Map<String, Object> getFilterParameters() {
		Map<String, Object> filterparameters = super.getFilterParameters();
		//Map<String, Object> filterparameters = new HashMap<>();
		filterparameters.put("domain", String.valueOf(getDomain().getId()));
		String types = getParameters().get("userlist")!=null ? "[idoc, useritem]" : "[idoc, datasetmember]";
		filterparameters.put("type", types);
		filterparameters.remove("state");
		filterparameters.remove("head");
		String solrclause = (String)filterparameters.get("solrclause");
		if (solrclause!=null && membersolrclause==null) {
			solrclause = "((" + solrclause +") OR type:datasetmember)";
			membersolrclause = solrclause;
			filterparameters.put("solrclause", solrclause);
		}
		return filterparameters;
	}
	
	public void setNode(DataSetMember member) {
		nodeId = String.valueOf(member.getId());
		setAsParameter(member, null, false);
		getParameters().put("type", "idoc");
	}
	
	
	public Criteria getCriteria(Site site) {
		String lib = (String) ((KbeeSite) site).getCustomValuesJson().get("library");
		if (lib!=null) {
			Library library = getRepository(Library.class).findById(Long.valueOf(lib));
			if (library!=null)
				return library.getCriteria();
		}
		
		
		String iql = (String) ((KbeeSite) site).getCustomValuesJson().get("iql");
		if (iql==null || iql.length()==0 || iql.toLowerCase().trim().equals("null"))
			return null;
		IqlCriteria ic = new IqlCriteria(getDomain(), iql);
		return ic;
	}
	
	public void setIncludeFacets(boolean b) {
		this.includeFacts=b;
	}
	
	@Override
	public boolean includeScore() {
		return true;
	}
	
	@Override
	public boolean includeFacets() {
		return includeFacts;
	}
	
	@Override
	public IqlService getIqlService() {
		return getDomain().getService(IqlService.class);
	}
	
	public void setOrderSet(String s) {
		this.orderSet=s;
	}
	
	public String getOrderSet() {
		return this.orderSet;
	}
	
	public boolean writeables() {
		return writeables;
	}
	
	@Override
	public String getStatement() {
		String statement = super.getStatement();
		if (nodeId!=null) {
			statement = "("+statement+") OR (type:datasetmember AND parent:"+nodeId;
			statement +=")";
		}
		else {
			if (!"".equals(statement)) {
				statement  = "(("+statement+") AND ";
			}
			else {
				statement  = "(";
			}
			statement += "NOT ";
			statement += classifierName + "name:* AND domain:"+ domainId + ")";
			statement += " OR (type:datasetmember AND NOT parent:* AND dataset:"+dataSetId;
			
			statement +=")";
		}
		return statement;
	}
	
	protected Classifier getClassifier(DataSet dataSet) {
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) { 
			if (classifier!=null && classifier.getDataSet().equals(dataSet) && classifier.isHierarchical()) {
				return classifier;
			}	
		}
		return null;
	}
	
	protected String getPrincipals(User user) {
		StringBuilder principals = new StringBuilder(String.valueOf(user.getId()));
		for (Group group : user.getGroups()) {
			principals = getGroups(group, principals);
		}
		return principals.toString();
	}
	
	protected StringBuilder getGroups(Group group, StringBuilder principals) {
		String id = ((KbeeGroup)group).getId().toString();
		if (principals.indexOf(" "+id)>0) 
			return principals;
		if (principals.length()>0) 
			principals.append(", ");
		principals.append(((KbeeGroup)group).getId());
		for (Group parent : ((KbeeGroup)group).getGroups()) { 
			principals = getGroups(parent, principals);
		}	
		return principals;
	}
	
	protected boolean isAdmin(User user) {
		return ServiceLocator.getService(SecurityService.class).isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
	}
	
	protected boolean isSupport(User user) {
		return ServiceLocator.getService(SecurityService.class).isMember(user, KbeeGlobalRole.SUPPORT.getId()); 
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected void setAsParameter(DataSetMember member, String facetname, boolean descendants) {
		List<String> members = new ArrayList<String>(); 
		for (Facet facet : getFacets()) {
			if (facet instanceof ClassifierFacet) {
				ClassifierFacet  classifierfacet = (ClassifierFacet)facet; 
				if (((ClassifierFacet)facet).getDisplayName().equals(member.getDataSet().getName()) ||
						(facetname!=null && facetname.equals(facet.getDisplayName()))) {
					members.add(((ClassifierFacet)facet).getMember(member).getPath());
				}
				else {
					if (facetname==null && classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						members.add(classifierfacet.getMember(member).getPath());
					}
				}
			}
			else
			if (facet instanceof ClassifierHierarchicalFacet) {
				ClassifierHierarchicalFacet  classifierfacet = (ClassifierHierarchicalFacet)facet; 
				if ((facetname==null && classifierfacet.getDisplayName().equals(member.getDataSet().getName())) || 
					(facetname!=null && facetname.equals(facet.getDisplayName()))) {
					String path = member.getDataSet().isHierachical() && descendants
						? classifierfacet.getMember(member).getPath()+"*"
						: classifierfacet.getMember(member).getPath();		
					members.add(path);
				}
				else {
					if (facetname==null && classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						members.add(classifierfacet.getMember(member).getPath());
					}
				}
			}
			else
			if (facet instanceof RelationFacet) {
				if (((RelationFacet)facet).getClassName().equals("user") && member.getDataSet().getDataSetType().equals(DataSetType.USER)) {
					Person person = ((PersonMember)member).getPerson();
					User user = person.getProfile(UserProfile.class).getUser();
					members.add(facet.getName() + "/" + user.getId());
					break;
				}
			}
		}
		getParameters().put("members", members);
	}
	
	protected TreeNode<DataSetMember> getNode(String nodeId) {
		return getNode(treeSupplier.getObject().getRoots(), nodeId);
		
	}
	
	protected TreeNode<DataSetMember> getNode(Iterator<? extends TreeNode<DataSetMember>> nodes, String nodeId) {
		List<TreeNode<DataSetMember>> nodeslist = new ArrayList<>();
		while (nodes.hasNext()) {
			TreeNode<DataSetMember> node = nodes.next();
			if (nodeId.equals(String.valueOf(node.getObject().getId()))) {
				return node;
			}
			else {
				nodeslist.add(node);
			}
		}
		for (TreeNode<DataSetMember> node : nodeslist) {
			Iterator<? extends TreeNode<DataSetMember>> childs = treeSupplier.getObject().getChildren(node);
			TreeNode<DataSetMember> child = getNode(childs, nodeId);
			if (child!=null) {
				return child;
			}
		}
		return null;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
}
