package kbee.web.console;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.resource.IResourceStream;

import com.novamens.indexer.query.Query;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.RefreshClickEvent;
import com.novamens.kbee.wicket.markup.html.event.ClickH1Event;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.form.Form;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.KbeeMarkupProvider;

@SuppressWarnings("serial")
public abstract class ConsolePage<T> extends AbstractApplicationPage<T> implements IMarkupResourceStreamProvider,  IMarkupCacheKeyProvider {
	private static final long serialVersionUID = 1L;
						
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ConsolePage.class.getName());
	
	protected static final ResourceReference BL = new CssResourceReference(Form.class, "build.css");
	protected static final ResourceReference BS = new CssResourceReference(Form.class, "bootstrap-select.css");
	protected static final ResourceReference BSJS = new JavaScriptResourceReference(Form.class, "bootstrap-select.js");
	
	private Query query;
	
//	public class RefreshBehavior extends AbstractDefaultAjaxBehavior implements IAjaxIndicatorAware {
//		private final ResourceReference indicator = INDICATOR;
//		@Override
//		protected void respond(AjaxRequestTarget target) {
//			((AbstractConsole<?>)ConsolePage.this.get("console")).refresh(target);
//			StringBuilder script = new StringBuilder();
//			script.append("	if (navigator.userAgent.indexOf('Trident') > 0) {\n");
//			script.append("		var sheet = window.document.styleSheets[0]; sheet.deleteRule(top.rule);\n");
//			script.append("	}\n");
//			script.append("	else {\n");
//			script.append("		var sheet = window.document.styleSheets[0]; sheet.removeRule(top.rule);");
//			script.append("	}\n");
//			target.appendJavaScript(script.toString());
//		}
//		@Override
//		public void renderHead(final Component component, final IHeaderResponse response) {
//			super.renderHead(component, response);
//			StringBuilder script = new StringBuilder();
//			script.append("function refresh() {\n");
//			script.append("var sheet = window.document.styleSheets[0];");
//			script.append("top.rule = sheet.cssRules.length;");
//			script.append("sheet.insertRule('a { pointer-events: none; }', top.rule); alert(top.rule)");
//			script.append(getCallbackScript());
//			script.append("alert(1);}\n");
//			response.render(JavaScriptHeaderItem.forScript(script.toString(), "refresh"));
//		}
//		@Override
//		protected void onComponentRendered(){
//			Response r = getComponent().getResponse();
//			r.write("<span style=\"display:none;\" class=\"");
//			r.write(getSpanClass());
//			r.write("\" ");
//			r.write("id=\"");
//			r.write(getMarkupId());
//			r.write("\">");
//			r.write("<img style=\"position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);\" src=\"");
//			r.write(getIndicatorUrl());
//			r.write("\" alt=\"\"/></span>");
//		}
//		protected CharSequence getIndicatorUrl() {
//			IRequestHandler handler = new ResourceReferenceRequestHandler(indicator);
//			return RequestCycle.get().urlFor(handler);
//		}
//		
//		protected String getSpanClass()	{
//			return "wicket-ajax-indicator";
//		}
//		public String getMarkupId()	{
//			return getComponent().getMarkupId() + "--ajax-indicator";
//		}
//		@Override
//		public String getAjaxIndicatorMarkupId() {
//			return getMarkupId();
//		}
//	}
	
	
	public class RefreshBehavior extends AbstractDefaultAjaxBehavior implements IAjaxIndicatorAware {
		private static final long serialVersionUID = 1L;
		private final ResourceReference indicator = INDICATOR;
		@Override
		protected void respond(AjaxRequestTarget target) {
			ConsolePage.this.fireScanAll(new RefreshClickEvent(target));
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
	
	public ConsolePage() {
		this(null);
	}

	public ConsolePage(Query query) {
		add(new RefreshBehavior());
		setQuery(query);
		setLogVisit(true);
		setName(getClass().getSimpleName().toLowerCase().replace("page", ""));
	}
	
	@Override
	public void addListeners() {
		super.addListeners();

		add(new WicketEventListener<OnSearchEvent>() {
			@Override
			public void onEvent(OnSearchEvent event) {
				ConsolePage.this.handle(event);
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
		
		add(new WicketEventListener<ClickH1Event<T>>() {
			@Override
			public void onEvent(ClickH1Event<T> event) {
				setResponsePage(getConsolePage());
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof ClickH1Event;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public Query getQuery() {
		return ((Console<T>)get("console")).getQuery();
	}
	
	public void setQuery(Query query) {
		this.query = query;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		try {
			if (get("console")==null) {
				Console<T> console = null;
				if (hasPermissions()) {
					console = newConsole(query);
					
					if (console!=null) {
						
						logger.debug("ConsolePage -> " + console.getName());
						
						setPageTitle(console.getDisplayName());
						setTopNavigation(newNavigationPanel());
						
						//if (isFreeVersion() && !isAdminUser()) 
						//	setMenu(new InvisiblePanel("menu"));
						//else {
							setMenu(getMainLaternalMenu());  
						//}
						
						if (isOpenHeader())
							console.add(new AttributeModifier("class", "open-page-header"));
						add(console);
					}
				}
				if (console == null) {
					setTopNavigation(getMainTopbar()); 	
					setMenu(getMainLaternalMenu()); 	
					
					addOrReplace(new ErrorPanel("console", 
						getLabel("not-authorized-title"), 
						getLabel("not-authorized-text", hasPermissionsReason())));
				}
			}
		} 
		catch (WicketRuntimeException e1) {
			logger.error(e1);
			java.lang.StackTraceElement[] arr= e1.getStackTrace();
			StringBuilder str = new StringBuilder();
			str.append(Thread.currentThread().getStackTrace()[1].getMethodName() + " | <br />");
			for (int n=0; n<arr.length;n++) 
				str.append(arr[n].toString() + "<br />");
			addOrReplace( new ErrorPanel("console", new Model<String>(e1.getClass().getName()),	new Model<String>(e1.getMessage() +" | <br />" + str.toString())));
			setTopNavigation(new InvisiblePanel("navigation"));
		} 
		catch (Exception e) {
			logger.error(e);
			java.lang.StackTraceElement[] arr= e.getStackTrace();
			StringBuilder str = new StringBuilder();
			str.append(Thread.currentThread().getStackTrace()[1].getMethodName() + " | <br />");
			for (int n=0; n<arr.length;n++) 
				str.append(arr[n].toString() + "<br />");
			addOrReplace( new ErrorPanel("console", new Model<String>(e.getClass().getName()), 
					new Model<String>(this.getClass().getName() + " | " + e.getMessage() +" | <br />" + str.toString())));
			setTopNavigation(new InvisiblePanel("navigation"));
		}
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(BL));
		response.render(CssHeaderItem.forReference(BS));
		response.render(JavaScriptHeaderItem.forReference(BSJS));
	}
	
	@Override
	public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
		return KbeeMarkupProvider.Get().getMarkupResourceStream(container, containerClass);
	}
	
	@Override
	public String getCacheKey(MarkupContainer container, Class<?> containerClass) {
		return KbeeMarkupProvider.Get().getCacheKey(container, containerClass);
	}
	
	protected void handle(OnSearchEvent event) {
		getQuery().getParameters().put("text", event.getText());
		getQuery().getParameters().put("sort", "relevance");
		setResponsePage(getConsolePage(getQuery(), 0));
	}
	
	protected Component newNavigationPanel() {
		return getMainTopbar();
	}
	
	protected boolean isAdminUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()) || ServiceLocator.getService(SecurityService.class).isRoot();
	}
	
	/** No common header 
	 *  Like Stand Alone Rule page
	 * */
	protected boolean isOpenHeader() {return false;}
	
	/**
	 * My Notes Panel
	 * @return
	 */
	protected boolean isNotesPanel() {return false;}
	
	/**
	 * Alerts Panel
	 * @return
	 */
	protected boolean AlertsPanel() {return false;}
	
	/**
	 * Saved Queries Panel
	 * @return
	 */
	protected boolean isSavedQueriesPanel() {return false;}

	protected String getConsoleName() {
		try {
		return getPageTitle().getObject();
		} catch (Exception e) {
			logger.error(e);
			return this.getClass().getSimpleName();
		}
	}
	
	@Override
	public boolean isClearAllSearch() {
		return true;
	}
	
	protected abstract Page getConsolePage(Query query, long index);
	
	protected Page getConsolePage() {
		try {
			return  (Page) Class.forName(this.getClass().getName()).newInstance();
		} 
		catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			logger.error(e);
			return new ApplicationErrorPage<Void>(e);
		}
	}
	
	public abstract boolean hasPermissions();
	
	public abstract Console<T> newConsole(Query query);
	
	protected String getUrl(IModel<T> model) {
		return null; 
	}
		
	protected String getUserPreference(String key) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user != null)
			return user.getService(PreferencesService.class).getValue(getName(), key);
		return null;
	}

	protected void setUserPreference(String key, String value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user != null)
			user.getService(PreferencesService.class).setValue(getName(), key, value);
	}
	
	// site id and site title are the sections
	//
	protected String getPageType() {return "con";}  // con | det  
	protected String getContentTitle() {return null;} // content title or user title, ...

	protected String getStatsPageTitle() {return getName();} // for console page, it is the name of the console 
	protected Long getStatsPageId() {return Long.valueOf(0);} // for console page, it is the name of the console
	
	protected String getObjectId() {return null;} // for user, domain, ...
	protected String getContentId() {return null;} // for content
}
