package com.novamens.content.web.security.markup;



import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.model.ObjectId;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorPanel;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;


/**
 * 
 *
 */
@SuppressWarnings("serial")
public class GroupPage extends ApplicationPage<Group> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GroupPage.class.getName());

	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
 	final boolean is_support 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

 	
	public GroupPage(PageParameters parameters) {
		Group g = getGroup(parameters);
		
		if (g!=null && hasPermissions()) {
				setModel(new ObjectModel<Group>(g));
				addComponents(getModel(), false, false, false); 
		}
		else 
			add(new ErrorPanel("editor", "Group not found", ""));
	}

	public GroupPage(IModel<Group> model) {
		super(model);
		if (hasPermissions()) 
			addComponents(model, false, false, false);
		else
			add(new ErrorPanel("editor", "Authorization required", ""));
		
		
		
	}

	@Override
	public boolean hasPermissions() {
		
		//if (getModel()==null || getModel().getObject()==null)
		//	return false;
		// Session User's Domain must be the same as  ObjectModel´s Domain
		//if (!getDomain().equals(getModel().getObject(). .getDomain()))
		//		return false;
			
		return is_domain_admin || is_root || is_support;
		
	}

	
	 
 	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SECURITY;
	}

 	 


	private void addComponents(IModel<Group> model,  boolean edition, boolean gotolastselection, boolean isNew) {
		
		setLogVisit(true);
		
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());

		getPageParameters().set("id", model.getObject().getId().toString());
		setPageTitle( new Model<String>("Group. " + model.getObject().getName()));
		GroupMainPanel editor = new GroupMainPanel(model, gotolastselection, isNew) {
			@Override
			protected void onClose(AjaxRequestTarget target) {
				setResponsePage(new com.novamens.content.web.security.markup.GroupsPage());
			}
		};
		editor.setEditionEnabled(edition);
		add(editor);
	}
	
			
	private Group getGroup(PageParameters parameters) {
		if (parameters.get("id")!=null && !"".equals(parameters.get("id").toString())) {
			try {
				String id = parameters.get("id").toString();
				return (Group) getContentSecurityDao().findGroupById(Long.valueOf(id));
			} catch (Exception e) {
					logger.error(e);
					return null;
			}
		}	
		return null;
	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}

	
	protected String getPageType()     {return "det";} 													 // con | det  
	protected String getContentTitle() {return getModel().getObject().getDisplayName();} 				// content title or user title, ...
	protected String getStatsPageTitle() {return getModel().getObject().getDisplayName();} 			// for console page, it is the name of the console 
	protected Long getStatsPageId() {return new Long(0);} 								                // for console page, it is the name of the console
	protected String getObjectId()  {return new ObjectId(getModel().getObject()).toString();}    		// for user, domain, ...
	protected String getContentId() {return null;}	  													// for content
	
}
