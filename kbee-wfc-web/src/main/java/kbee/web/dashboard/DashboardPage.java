package kbee.web.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.notification.NotificationService;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.kbee.wicket.markup.html.console.browser.RefreshClickEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.alert.BillboardPanel;
import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.page.ConsoleSectionHomePage;
import kbee.web.page.PageContentHeaderPanel;


public abstract class DashboardPage<T> extends ConsoleSectionHomePage<T> {
						
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(DashboardPage.class.getName());
	
	
	final protected boolean is_root = ServiceLocator
			.getService(SecurityService.class)
			.isRoot();
	final protected boolean role_admin = ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final protected boolean role_support = ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.SUPPORT.getId());
	final protected boolean role_federated_values = ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.FEDERATED_VALUES.getId());
	final protected boolean role_dataset_members = role_admin || 
			ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
	final protected boolean role_dataset_members_write = role_admin || 
			ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	
	protected static final ResourceReference BL = new CssResourceReference(Form.class, "build.css");
	
	protected WebMarkupContainer modal_container;
	protected WebMarkupContainer dash;
	protected WebMarkupContainer alert;
	
	public interface WidgetFactory extends Serializable {
		public MarkupContainer getWidget(String id);
		public IModel<String> getLabel();
	}
	
	
	private List<IModel<DataSet>> entitiessets;
	
	

	
	


	
	public class RefreshBehavior extends AbstractDefaultAjaxBehavior implements IAjaxIndicatorAware {
		private static final long serialVersionUID = 1L;
		private final ResourceReference indicator = INDICATOR;
		@Override
		protected void respond(AjaxRequestTarget target) {
			DashboardPage.this.fireScanAll(new RefreshClickEvent(target));
			StringBuilder script = new StringBuilder();
			script.append("	try {\n");
			script.append("		if (top.inrefresh) {\n");
			script.append("			if (navigator.userAgent.indexOf('Trident') > 0) {\n");
			script.append("				var sheet = window.document.styleSheets[1]; sheet.deleteRule(top.rule);\n");
			script.append("			}\n");
			script.append("			else {\n");
			script.append("				var sheet = window.document.styleSheets[1]; sheet.removeRule(top.rule);");
			script.append("			};\n");
			script.append("		};\n");
			script.append("	}\n"); 
			script.append("	catch (err) {\n");
			script.append("		console.log(err);\n");
			script.append("	}\n");
			script.append("	finally {\n");
			script.append("		top.inrefresh=false;\n");
			script.append("	};\n");
			target.appendJavaScript(script.toString());
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function refresh() {\n");
			script.append("		if (top.inrefresh === 'undefined'|| !top.inrefresh) {\n");
			script.append("			top.inrefresh=true;\n");
			script.append("			var sheet = window.document.styleSheets[1];\n");
			script.append("			top.rule = sheet.cssRules.length;");
			script.append("			sheet.insertRule('a { pointer-events: none; }', top.rule);");
			script.append(getCallbackScript());
			script.append("		}\n");
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "refresh"));
		}
		@Override
		protected void onComponentRendered(){
			Response r = getComponent().getResponse();
			r.write("<span style=\"display:none;\" class=\"");
			r.write(getSpanClass());
			r.write("\" ");
			r.write("id=\"");
			r.write(getMarkupId());
			r.write("\">");
			r.write("<img style=\"position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);\" src=\"");
			r.write(getIndicatorUrl());
			r.write("\" alt=\"\"/></span>");
		}
		protected CharSequence getIndicatorUrl() {
			IRequestHandler handler = new ResourceReferenceRequestHandler(indicator);
			return RequestCycle.get().urlFor(handler);
		}
		
		protected String getSpanClass()	{
			return "wicket-ajax-indicator";
		}
		public String getMarkupId()	{
			return getComponent().getMarkupId() + "--ajax-indicator";
		}
		@Override
		public String getAjaxIndicatorMarkupId() {
			return getMarkupId();
		}
	}
	
	public IModel<String> getTitle() {
		return getLabel("home");
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(BL));
	}
	
	public List<IModel<DataSet>> getDataSets() {
		if (entitiessets==null) {
			entitiessets = new ArrayList<IModel<DataSet>>();
			List<DataSet> list = getDomain().getService(DomainService.class).getEntitySets();
			for (DataSet ds : list) 
				if (hasRole(ds))
					entitiessets.add(new ObjectModel<DataSet>(ds));
		}
		return entitiessets;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (!hasPermissions()) {
			addOrReplace(new ErrorNotAuthorizedPanel<>("feedback"));
			addOrReplace(new InvisiblePanel("dashboard"));	
			add(new InvisiblePanel("modal-container"));
			add(new InvisiblePanel("billboard"));
			return;
		}
		
		try {
			if (this.hasBillboardPanel())		
				setBillboardPanel(new BillboardPanel());
			else								
				add(new InvisiblePanel("billboard"));
		} 
		catch (Exception e) {
			logger.error(e);
			add(new ErrorPanel("billboard", e));
		}

		try {
			setTopNavigation(getMainTopbar());
			setMenu(getMainLaternalMenu());
		} 
		catch (Exception e) {
			setTopNavigation(new InvisiblePanel("navigation"));
			logger.error(e);
		}
		
		PageContentHeaderPanel<Person> panel=new PageContentHeaderPanel<Person>(null);
		panel.setSectionHome(isSectionHome());
		
		setPageTitle(getTitle());
		panel.setTitle(getTitle());
		panel.setBreadcrumbPanel(getBreadcrumbPanel());

		setSuggester(false); 
		setSearchPanel(false);
		setAdvancedSearch(false); 


		modal_container = new WebMarkupContainer("modal-container"); 
		modal_container.setOutputMarkupId(true);
		add(modal_container);
		
		dash = new WebMarkupContainer("dashboard"); 
		dash.setOutputMarkupId(true);
		add(dash);
		
		setPageContentHeader(panel);
		add(new InvisiblePanel("feedback"));
		addWidgets();
		
		if (this.alert==null)		
			setAlertPanel(new InvisiblePanel("alert"));

		
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (entitiessets!=null)
			entitiessets.forEach(item-> item.detach());
	}
	
	


	
	protected  WebMarkupContainer getDashboardMarkupContainer() {
		return dash;
	}
	
	
	protected  WebMarkupContainer getModalContainerMarkupContainer() {
		return modal_container;
	}
	 
	
	
	protected MarkupContainer getWidget(WidgetFactory factory) {
		try {
			return factory.getWidget("panel");
		}
		catch(Exception e) {
			return new DashboardWidgetSimpleWrapperPanel<Person>("panel", 
				getPersonModel(), 
				new ErrorPanel("payload", e), 
				factory.getLabel(),
				getPageKey());
		}	
	}
	
	abstract protected void addWidgets();
	

	/**
	 * id  = "alert"
	 * 
	 * @param panel
	 */
	protected void setAlertPanel(WebMarkupContainer panel) {
		if (panel==null)
			throw new IllegalArgumentException("alert can not be null");
		if (!panel.getId().contentEquals("alert"))
			throw new IllegalArgumentException("alert Panel must have id = alert");
		dash.addOrReplace(panel);
	}

	
	@Override
	protected boolean hasPermissions() {
		
		if (is_root || role_admin)
			return true;
		
		if (role_support)
			return true;
		
		if (role_dataset_members)
			return true;
		
		if (role_federated_values)
			return true;
		
		return false;
		
	}
	/**
	 <p>El usuario tiene algun rol en entidades del dataset</p>
	 * @param ds
	 * @return
	 */
	protected boolean hasRole(DataSet ds) {
		if (ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId())) {
			return true;
		};
		
		UserProfile usersessionprofile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		for (UserRole userrole : usersessionprofile.getRoles()) {
			Role role = userrole.getRole();
			if (role.isEntity()) {
				EntityRole entityrole = (EntityRole)getContentDao().unproxy(role); 
				if (entityrole.getClassifier().getDataSet().equals(ds) && entityrole.getClassifier().hasHome()) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isEntitiesAdmin() {
		if (ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId())) {
			return true;
		};
		
		UserProfile usersessionprofile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		for (UserRole userrole : usersessionprofile.getRoles()) {
			Role role = userrole.getRole();
			if (role.isEntity()) {
				EntityRole entityrole = (EntityRole)getContentDao().unproxy(role); 
				if (entityrole.isAdministrator()) {
					return true;
				}
			}
		}
		return false;
	}

	
	/**
	 * 
	 * id panel
	 * 
	 * @param panel
	 */
	protected void addWidget(WebMarkupContainer panel) {
		if (panel==null)
			throw new IllegalArgumentException("widget can not be null");
		dash.add(panel);
	}
	
	protected boolean hasBillboardPanel() {
		return ServiceLocator.getService(NotificationService.class).getTotalBillboardNotifications(getSessionUser())>0;
	}
	
	protected void setBillboardPanel(Panel panel) {
		if (panel==null)
			throw new IllegalArgumentException("billboard-panel can not be null");
		if (!panel.getId().contentEquals("billboard"))
			throw new IllegalArgumentException("billboard Panel must have id = billboard");
		addOrReplace(panel);
	}
	
	protected Panel getBreadcrumbPanel() {
		return new InvisiblePanel("breadcrumb");
	}
	
	protected boolean hasError() {
		return false;
	}
	
	protected String getPageKey() {
		return null;
	}
	
	protected boolean isSectionHome() {
		return true;
	}
	
	private IModel<Person> getPersonModel() {
		return new ObjectModel<Person>(getPerson());
	}
}
