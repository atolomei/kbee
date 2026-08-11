package com.novamens.kbee.content.iql;

import java.util.List;
import java.util.stream.Collectors;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentTemplate;
import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.iql.SolrAbstractPredicate;

public class ContentClassPredicate extends SolrAbstractPredicate implements CalculatedPredicate {
	
	public ContentClassPredicate() {
		setName("contentclass");
		setPath("template");
		setValueType("ClassCode");
	}
	
		
	

	
	@Override
	public String getHelpValueTypeDescription() {
		return "Content Class Oid | Name | Alias";
	}

	
	public boolean isInformationModel() {
		return false;
	}

	public boolean isCanonical() {
		return true;
	}

	
	/**
	 * 
	 */
	public String getCode(String argument) {
		StringBuilder code = new StringBuilder();
		
		if (isDigits(argument)) {
			code.append(getPath() + ":" + argument);
		}
		else {
			List<ContentTemplate> templates = findTemplatesByCode(argument);
			if (templates.isEmpty()) {
				templates = findTemplatesByAlias(argument);
			}
			if (templates.isEmpty()) {
				templates = findTemplatesByName(argument);
			}
			if (templates.size()>1) 
				code.append("(");
			int i=0;
			for (ContentTemplate template : templates) {
				if (i>0) 
					code.append(" OR ");
				code.append(getPath() + ":" + template.getId());
				i++;
			}
			if (i>1) 
				code.append(")");
			if (templates.isEmpty()) {
				code.append(getPath() +":x");
			}
		}
		
		return code.toString();
	}
	
	public boolean evaluate(Object object, Object argument) {
		if (!(object instanceof Content)) return false;
		
		boolean evaluation = false;
	
		if (isDigits((String)argument))
			evaluation = argument.toString().toLowerCase().equals(String.valueOf(((Content)object).getContentTemplate().getId()));
		else
			evaluation = argument.toString().toLowerCase().equals(((Content)object).getContentTemplate().getContentClassCode().toLowerCase());
		return evaluation;
	}
	
	private List<ContentTemplate> findTemplatesByCode(String code) {
		List <ContentTemplate> templates = getContentDao().getTemplates().stream().
			filter((template) -> template.getContentClassCode()!=null && code.toLowerCase().equals(template.getContentClassCode().toLowerCase())).
			collect(Collectors.toList());
		return templates;
	}
	
	private List<ContentTemplate> findTemplatesByAlias(String alias) {
		List <ContentTemplate> templates = getContentDao().getTemplates().stream().
			filter((template) -> template.getAlias()!=null && alias.toLowerCase().equals(template.getAlias().toLowerCase())).
			collect(Collectors.toList());
		return templates;
	}
	
	private List<ContentTemplate> findTemplatesByName(String name) {
		List <ContentTemplate> templates = getContentDao().getTemplates().stream().
			filter((template) -> name.toLowerCase().equals(template.getName().toLowerCase())).
			collect(Collectors.toList());
		return templates;
	}
	
	private boolean isDigits(String argument) {
		for (int c=0; c<argument.length(); c++) {
			if (!Character.isDigit(argument.charAt(c))) {
				return false;
			}
		}
		return true;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
