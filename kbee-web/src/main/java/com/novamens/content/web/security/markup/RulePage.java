package com.novamens.content.web.security.markup;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.entity.Person;
import com.novamens.content.model.ObjectId;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.IQLRule;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorPanel;

import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;



@SuppressWarnings("serial")
public class RulePage extends ApplicationPage<IQLRule> {
	private static final long serialVersionUID = -1L;

	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_support 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	
	public RulePage(PageParameters parameters) {
		IQLRule rule = getRule(parameters);
		if (rule != null) {

			setTopNavigation(getMainTopbar());
			setMenu(getMainLaternalMenu());

			setModel(new ObjectModel<IQLRule>(rule));
			addComponents(getModel(), false); 
		}
		else 
			add(new ErrorPanel("editor", "rule not found", ""));
	}
	
	/**
	 * @param model
	 */
	public RulePage(IModel<IQLRule> model, boolean isNew) {
		this(model, new GlobalNavigationBar<ENotiRule>("navigation"), isNew);
	}
	
	/**
	 * @param model
	 * @param navigation
	 * @param editon
	 */
	public RulePage(IModel<IQLRule> model, Panel navigation, boolean isnew) {
		super(model, navigation);
		setPageTitle(new Model<String>(model.getObject().getName()));
		
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());

		addComponents(model, isnew);
	}
	
    
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SECURITY;
	}


	protected String getPageType()     {return "det";} 													 // con | det  
	protected String getContentTitle() {return getModel().getObject().getDisplayName();} 				// content title or user title, ...
	protected String getStatsPageTitle() {return getModel().getObject().getDisplayName();} 			// for console page, it is the name of the console 
	protected Long getStatsPageId() {return new Long(0);} 								                // for console page, it is the name of the console
	protected String getObjectId()  {return new ObjectId(getModel().getObject()).toString();}    		// for user, domain, ...
	protected String getContentId() {return null;}	  													// for content

	
	private void addComponents(IModel<IQLRule> model, boolean isnew) {
		
		
		RuleMainPanel editor = new RuleMainPanel(getModel(), isnew) {
			@Override
			protected void onClose(AjaxRequestTarget target) {
				setResponsePage(new com.novamens.content.web.security.markup.RulesPage());
			}
		};
		
		editor.setEditionEnabled(isnew);
		add(editor);
		
		getPageParameters().set("id", model.getObject().getId());
	}
	
	private IQLRule getRule(PageParameters parameters) {
		if (parameters.get("id")!=null && !"".equals(parameters.get("id").toString())) {
			String ruleid = parameters.get("id").toString();
			IQLRule rule = (IQLRule)getContentSecurityDao().findRuleById(Long.valueOf(ruleid));
			return rule;
		}	
		return null;
	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
