package com.novamens.kbee.portal.model;

import java.util.Map;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.Criteria;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.TextFilter;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.library.IqlCriteria;
import com.novamens.kbee.content.userlist.UserListResultSetWrapper;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.portal6.model.Site;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class SearcherSiteQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;
	
	private boolean writeables = false;
	private boolean includeFacts = true;
	private String orderSet;

	public SearcherSiteQuery(Site site, Index index) {
		this(site, index, null);
	}
	
	public SearcherSiteQuery(Site site, Index index, Map<String, Object> parameters) {
		super(index);
		
		Object textvalue = parameters!=null ? parameters.get("text") : null;
		
		if (textvalue!=null && textvalue instanceof String) {
			parameters.put("text", new TextFilter((String)textvalue));
		}	
		
		if (parameters!=null && parameters.get("text")!=null) {
			getParameters().put("sort", "relevance");
			getParameters().put("ascending", "false");
		} 
		else {
			getParameters().put("sort", "modified");
			getParameters().put("ascending", "false");
		}
		
		User user =  ServiceLocator.getService(SecurityService.class).getSessionUser();
		if (parameters!=null && "true".equals(parameters.get("writeables"))) {
			this.writeables = true;
			if (!isAdmin(user) && !isSupport(user)) {
				getFilterParameters().put("writer", "["+getPrincipals(user)+"]");
			}
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
			getParameters().put("reader", "["+getPrincipals(user)+"]");
		}
	}

	@Override
	public ResultSet execute() {
		return new UserListResultSetWrapper(super.execute());
	}
	
	public boolean writeables() {
		return writeables;
	}
	
	public Map<String, Object> getFilterParameters() {
		Map<String, Object> filterparameters = super.getFilterParameters();
		filterparameters.put("state", String.valueOf(ObjectState.ENABLED.getId()));
		filterparameters.put("head", "true");
		filterparameters.put("domain", String.valueOf(getDomain().getId()));
		String types = getParameters().get("userlist")!=null ? "[idoc, useritem]" : "[idoc]";
		filterparameters.put("type", types);
		return filterparameters;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
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
	public String[] fields() {
		String fields[] = { "id", "title", "score" };
		return fields;
	}
	
	@Override
	public String getQueryFields() {
		return "title^3.0 metainfo^2.0 portaltext^1.0";
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
	
	@Override
	public void setParameter(String name, Object value) {
		super.setParameter(name, value);
		if ("text".equals(name)) {
			super.setParameter("sort", "relevance");
			super.setParameter("ascending", "false");
			setTextQuery(true);
		} 
	}
	
	private String getPrincipals(User user) {
		StringBuilder principals = new StringBuilder(String.valueOf(user.getId()));
		for (Group group : user.getGroups()) {
			principals = getGroups(group, principals);
		}
		return principals.toString();
	}
	
	private StringBuilder getGroups(Group group, StringBuilder principals) {
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
	
	private boolean isAdmin(User user) {
		return ServiceLocator.getService(SecurityService.class).isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
	}
	
	protected boolean isSupport(User user) {
		return ServiceLocator.getService(SecurityService.class).isMember(user, KbeeGlobalRole.SUPPORT.getId()); 
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
