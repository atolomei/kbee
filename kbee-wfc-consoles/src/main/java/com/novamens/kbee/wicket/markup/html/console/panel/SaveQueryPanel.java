package com.novamens.kbee.wicket.markup.html.console.panel;



import java.util.Map;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.query.SavedQuery;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.console.Console;

@SuppressWarnings("serial")
public class SaveQueryPanel extends ObjectEditor<SavedQuery> {
	
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SaveQueryPanel.class.getName());
	
	private String title;
	private String console;
	private String browser;
	private Map<String, Object> parameters;
	private Form<?> form;
	private IModel<Site> site_model;
	private boolean isdashboard = false;
	
	protected final boolean root		   = ServiceLocator.getService(SecurityService.class).isRoot();
	protected final boolean role_admin     = root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	public SaveQueryPanel(String id, String console, IModel<Site> sitemodel, boolean is_dashboard) {
		this(id, console, null, sitemodel, is_dashboard);
	}
	
	public SaveQueryPanel(String id, String console, String browser, IModel<Site> sitemodel, boolean is_dashboard) {
		super(id);
		setOutputMarkupId(true);
		this.console=console;
		this.isdashboard=is_dashboard;
		this.browser = browser;
		site_model=sitemodel;
	}	
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (site_model!=null)
			site_model.detach();
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (form==null)
			addForm();
	}	
	
	public void setTitle(String value) {
		this.title = value;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (isDashboardQuery()) {
				ServiceLocator.getService(ObjectFactoryService.class).createSavedQueryDashboard(getSessionUser(), getTitle(), getConsole(), getSite(),  getParameters());
			}
			else {
				ServiceLocator.getService(ObjectFactoryService.class).createSavedQuery(getSessionUser(), getTitle(), getConsole(), getBrowser(), getSite(),  getParameters());
			}
		} 
		catch (Exception e) {
			logger.error (e);
		}
	}
	
	protected boolean isDashboardQuery() {
		return this.isdashboard;
	}
	
	@Override
	public Form<?> getForm() {
		return form; 
	}
	
	protected void addForm() {
		
		form = new Form<Void>("queryform", Disposition.VERTICAL);
		
		form.add(new TextField<String>("title", new PropertyModel<String>(this, "title")) {
			protected boolean autofocus() {
				return true;
			}
		});

		Label in=new Label("dashboard-panel",  new StringResourceModel("portal-dashboard", this, null));
		in.setVisible(isDashboard());
		form.add(in);
		addOrReplace(form);
	}
	

	
	protected String getConsole() {
		if (console==null) {
			MarkupContainer parent = getParent();
			while (parent!=null) {
				if (parent instanceof Console<?>) {
					console = ((Console<?>)parent).getName();
					break;
				}
				else {
					parent = parent.getParent();
				}
			}
		}
		return console;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	public Site getSite() {
		if (site_model!=null) {
			return site_model.getObject();
		}
		return null;
	}


	public boolean isDashboard() {
		return isdashboard;
	}


	public void setDashboard(boolean isdashboard) {
		this.isdashboard = isdashboard;
	}	
	
}
