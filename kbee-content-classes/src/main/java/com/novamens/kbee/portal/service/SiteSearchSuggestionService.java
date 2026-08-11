package com.novamens.kbee.portal.service;

import java.util.Map;

import com.novamens.content.library.Library;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Criteria;
import com.novamens.kbee.content.library.IqlCriteria;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.portal6.model.Site;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.service.ServiceLocator;

public class SiteSearchSuggestionService extends SearchSuggestionService {
	
	public SiteSearchSuggestionService() {
	}
	
	public SiteSearchSuggestionService(Site site) {
		super(site);
	}
	
	@Override
	public Domain getDomain() {
		return getSite().getDomain();
	}
	
	@Override
	protected String getStatement(String pattern, Map<String, Object> parameters) {
		String statement = super.getStatement(pattern, parameters);
		if (parameters!=null)
			for (String key : parameters.keySet()) {
				String value = (String)parameters.get(key);
				String term = "";
				if (value.startsWith("[")) {
					value = value.substring(1, value.length()-1);
					String values[] =value.split(",");
					term ="(";
					int i = 0;
					for (String option : values) {
						if (i>0) 
							term += " OR ";
						term += key+":"+option.trim();
						i++;
					}
					term += ")";
					statement += " AND " + term;
				}
				else {
					statement += " AND " + key +":" + value;
				}
			}
		return statement;
	}
	
	@Override
	protected String getContentFilterStatement() {
		IqlCriteria iql = (IqlCriteria)getSiteCriteria();
		String statement = iql.getSolrClause();
		return statement;
	}
	
	private Criteria getSiteCriteria() {
		String lib = (String) ((KbeeSite)getSite()).getCustomValuesJson().get("library");
		if (lib!=null) {
			Library library = getRepository(Library.class).findById(Long.valueOf(lib));
			if (library!=null)
				return library.getCriteria();
		}
 		String iql = (String) ((KbeeSite)getSite()).getCustomValuesJson().get("iql");
		if (iql==null || iql.length()==0 || iql.toLowerCase().trim().equals("null"))
			return null;
		IqlCriteria ic = new IqlCriteria(getDomain(), iql);
		return ic;
	}
	
	private <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
}	