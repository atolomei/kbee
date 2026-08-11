package kbee.web.rule;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.rule.ActionRule;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.nav.ActionRulesBC;
import kbee.web.nav.AlertManagementDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

/**
 * 
 * Detail
 *
 */
@SuppressWarnings("serial")
public class ActionRulePage extends ApplicationPage<ActionRule> {
	private static final long serialVersionUID = -1L;

	private boolean isNew = false;
	
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	public ActionRulePage(PageParameters parameters) {
		ActionRule cabinet = getActionRule(parameters);

		if (cabinet != null)
			setModel(new ObjectModel<ActionRule>(cabinet));
			
	}
	
	
	public ActionRulePage(IModel<ActionRule> model, boolean isNew) {
		this(model, new GlobalNavigationBar<ENotiRule>("navigation"), isNew);
	}
	
	/**
	 * @param model
	 * @param navigation
	 * @param editon
	 */
	public ActionRulePage(IModel<ActionRule> model, Panel navigation, boolean isnew) {
		super(model, navigation);
		this.isNew=isnew;
		
	}
	
	protected boolean hasPermissions() {
		return  is_domain_admin ||  is_root || is_support;
	}
	
	
	public void onInitialize() {
		super.onInitialize();

		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());

		if (hasPermissions()) {
			
			PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<>();
							
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
			
			bc.addElement(new  AlertManagementDropDownBC());
			bc = bc.addElement(new  ActionRulesBC());
			
			
			//bc = bc.addElement(new  DataManagementDropdownBC());
			
			
			bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName()!=null? getModel().getObject().getDisplayName(): getModel().getObject().getId().toString())));
			panel.setBreadcrumbPanel(bc);
			
			setPageTitle( new Model<String>(getModel().getObject().getDisplayName()));
			panel.setTitle(getModel().getObject().getDisplayName());
			setSearchPanel(false);
			setAdvancedSearch(false);
			setSuggester(false);
			setPageContentHeader(panel);
			addComponents( getModel(), isNew);
			}	
			else {
				add(new ErrorPanel("editor", "ActionRule not found", ""));
			}
	}
	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DATA_MANAGEMENT;
	}
	
	private void addComponents(IModel<ActionRule> model, boolean isnew) {
		ActionRuleMainPanel editor = new ActionRuleMainPanel(getModel(), isnew) {
			@Override
			protected void onClose(AjaxRequestTarget target) {
				setResponsePage(new ActionRulesPage());
			}
		};
		editor.setEditionEnabled(isnew);
		add(editor);
		getPageParameters().set("id", model.getObject().getId());
	}
	
	private ActionRule getActionRule(PageParameters parameters) {
		if (parameters.get("id")!=null && !"".equals(parameters.get("id").toString())) {
			String ruleid = parameters.get("id").toString();
			ActionRule rule = getRepository(ActionRule.class).findById(Long.valueOf(ruleid));
			return rule;
		}	
		return null;
	}
	
//	protected String getPageType()     {return "det";} 													 // con | det  
//	protected String getContentTitle() {return getModel().getObject().getDisplayName();} 				// content title or user title, ...
//	protected String getStatsPageTitle() {return getModel().getObject().getDisplayName();} 			// for console page, it is the name of the console 
//	protected Long getStatsPageId() {return (long)0; } 								                // for console page, it is the name of the console
//	protected String getObjectId()  {return new ObjectId(getModel().getObject()).toString();}    		// for user, domain, ...
//	protected String getContentId() {return null;}	  													// for content
}
