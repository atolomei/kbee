package com.novamens.content.web.nav.markup;


import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.HelpService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.content.web.nav.markup.GlobalNavigationBar.HelpFragment;
import com.novamens.content.web.workflow.markup.TaskPanel;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailEvent;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.web.event.wicket.CancelWorkflowEvent;
import kbee.web.event.wicket.FullScreenEvent;
import kbee.web.event.wicket.PreviewClickEvent2;
import kbee.web.nav.DonePage;
import kbee.web.nav.NavigationPanel;
import kbee.web.nav.NavigatorPanel;
import kbee.web.nav.RefreshParentBehavior;

/**
 * Bootstrap based. Navigation Bar
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class MonitorNavigationBar<T extends Content> extends NavigationPanel<T>  {
	private static final long serialVersionUID = 1L;
	
	private String isportal = null;
	
	private IModel<WorkflowContext> model;
	private Editor<T> editor;

	private static AtomicBoolean IS_HELP_VISIBLE= null;

	public static boolean isHelpEnabled() {
		if (IS_HELP_VISIBLE!=null)
			return 	IS_HELP_VISIBLE.get();
			IS_HELP_VISIBLE = new AtomicBoolean(((ContentDao) (ServiceLocator.getService(BeansService.class).getBean("contentDao"))).findSystemParameterValueByKey("help.enabled", "no").toLowerCase().trim().equals("yes"));
		return 	IS_HELP_VISIBLE.get();
	}

	
	public class HelpFragment extends Fragment {
		public HelpFragment(String id) {
			super(id, "help-fragment", MonitorNavigationBar.this);
			
			Link<Void> link = new Link<Void>("help-link") {
				@Override
				public void onClick() {
					Page page = getPage();
					if (page instanceof AbstractKbeeWebPage) {

						String page_key = ((AbstractKbeeWebPage) page).getPageHelpKey();
						String section_key = ((AbstractKbeeWebPage) page).getPageInternalSectionHelpKey();
						String url = ServiceLocator.getService(HelpService.class).getHelpUrl(page_key+ (section_key!=null? ("-" + section_key):""));
						setResponsePage(new RedirectPage(url));
					}
				}
			};
			link.add(new AttributeModifier("target", "_blank"));
			link.setVisible(ServiceLocator.getService(BrandingService.class).isHelpVisible());
			add(link);
		}
	}

	

	
	public MonitorNavigationBar(IModel<WorkflowContext> model) {
		this("navigation", model);
	}
	
	
	public MonitorNavigationBar(String id, IModel<WorkflowContext> model, Searcher searcher, long index) {
		super(id);
		setWorkflowModel(model);
		add((searcher!=null) ? new InvisiblePanel("navigator") : newNavigator(searcher, index));
	}
	
	
	public void onInitialize() {
		super.onInitialize();
		add(newCloseLink());
		add(newFullWidthLink());
		add(newEndConditionsPanel());
		add(newToolsPanel());
		add(newPreviewLink());
		add(newInfoPanel());
		setOutputMarkupId(true);
		add(newDeletePanel());
		add(newHelpPanel());
	}
	
	
	public MonitorNavigationBar(String id, IModel<WorkflowContext> model) {
		super(id);
		setWorkflowModel(model);
		add(new InvisiblePanel("navigator"));
	}


	public void navigate() {
		Page donePage = new DonePage();
		donePage.add(new RefreshParentBehavior());
		setResponsePage(donePage);
	};
	
 	

	@Deprecated
	public void navigate(AjaxRequestTarget target) {
		navigate();
	}

 	
	public void onStartWorkflow() {
		
	};

 	

	public void onNavigate(T object) {
		
	};

 	

	public void onReturn() {
		
	};

 	

	public void setEditor(Editor<?> editor) {
		
	};

 	

	public boolean isFromContentBase() {
		return false;
	};

 	

	public void setWorkflowModel(IModel<WorkflowContext> model) {
		this.editor = null;
		this.model = model;
	}

 	

	public IModel<WorkflowContext> getWorkflowModel() {
		return model;
	}

 	

	public long getIndex() {
		if (get("navigator")!=null && get("navigator") instanceof NavigatorPanel) {
			return ((NavigatorPanel<?>)get("navigator")).getIndex();
		}
		return -1;
	}

 	

	public void onBeforeRender() {
		super.onBeforeRender();
		addOrReplace(newSubmitLink());
	}

 	

	public void onDetach() {
		if (this.model!=null)
			this.model.detach();
		super.onDetach();
	}
	
 	

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		StringBuilder script = new StringBuilder();
		script.append("function closewindow() {\n");
		script.append("	if (window.opener.refresh) { window.opener.refresh(); };\n");
		script.append("	var agent = navigator.userAgent;\n");
		script.append("	if (agent.indexOf('Edge') > 0 || agent.indexOf('Trident') > 0) {\n");
		script.append("		window.open('', '_self', '');\n");
		script.append("	}\n");
		script.append("	window.close();\n");
		script.append("}\n");
		response.render(JavaScriptHeaderItem.forScript(script.toString(), "closewindow"));
	}
	
	protected void onReturn(AjaxRequestTarget target)  {
	
	}

	


	protected Component newHelpPanel()  {
		return  isHelpEnabled() ? new HelpFragment("help")  : new InvisiblePanel("help");
	}


	protected Component newCloseLink()  {
		Link<?> link = new Link<Void>("close-link")	{
			public void onClick() {
			}
		};
		link.add(new AttributeModifier("onclick", "closewindow();"));
		return link;
	}



	protected Component newNavigator(Searcher searcher, long index)  {
		return new NavigatorPanel<T>("navigator", searcher, (int)index) {
			public void onNavigate(T object) {
				MonitorNavigationBar.this.onNavigate(object);
			}
		};
	}


 	protected Component newAuditPanel() {
 		return new AjaxLink<Void>("audit-link") {
 			public void onClick(AjaxRequestTarget target) {
 				fire(new AuditTrailEvent(target));
 			}
 		};
 	}


 	/** --------------------------------------------------------------------------
	 * 
	 * ContentConsole: {@code getPortalPreviewPage(IModel<T> model)}
	 * TaskPanel: :    {@code getPortalPreviewPage(IModel<T> model)}
	 * 
	 *  
	 */
	protected Component newPreviewLink()  {
		return new Link<Void>("preview-link") {
			@Override
			public void onClick() {
				fire(new PreviewClickEvent2<T>(MonitorNavigationBar.this.getEditor().getModel()));
			}
	
			@Override
			public boolean isVisible() {
				return isPortalEnabled();
			}
				
			@Override
			public boolean isEnabled() {
				return true;
			}
 		};
	}
	
 	/** --------------------------------------------------------------------------
	 */
 	
 	protected Component newEndConditionsPanel()  {
		return new EndConditionsPanel<T>(getWorkflowModel()) {
			@Override
			protected Editor<T> getEditor() {
				return MonitorNavigationBar.this.getEditor();
			}
			@Override
			public boolean isVisible() {
				return getTask()!=null && getRunningActivity()!=null && getRunningActivity().getUser().equals(getUser());
			}
		};
 	}
 	
 	protected Component newDeletePanel() {
 		
 		return new AjaxLink<Void>("delete-link") {
 			public void onClick(AjaxRequestTarget target) {
 				fire(new CancelWorkflowEvent(target));
 			}
 			
 			@Override
			public boolean isVisible() {
 				return false;
			}
 		};
 	}

 	
	protected Component newInfoPanel()  {
		return new AjaxLink<Void>("info-link") {
			public void onClick(AjaxRequestTarget target) {
				
				Editor<T> editor = getEditor();
				
				
 				if (((TaskPanel<T>)editor).isRightPanelVisible()) {
 					((TaskPanel<T>)editor).setRightPanelVisible(false);
 					target.add((TaskPanel<T>)editor);
 				}
 				else
 					((TaskPanel<T>)editor).showInfoPanel(target);
 				    
 				target.add(MonitorNavigationBar.this);
			}
		};
	}

	
	protected Component newToolsPanel()  {
		return new NavBarMonitorMenu("monitor-tools") {
			@Override
			public boolean isVisible() {
				return false;
			}
		};
	}

	
 	protected Component newFullWidthLink()  {
 		return new AjaxLink<Object>("fullwidth-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire(new FullScreenEvent(target));
			}
			@Override
			public boolean isVisible() {
				return false;
			}
			
 		};
	}
 	
	
	
	protected Editor<T> getEditor() {
		if (this.editor==null) {
			this.editor = getEditor(getPage().iterator());
			Assert.isInstanceOf(TaskPanel.class, editor);
		}
		return this.editor;
	}
	
	@SuppressWarnings("unchecked")
	protected Editor<T> getEditor(Iterator<Component> components) {
		while (components.hasNext()) {
			Component component = components.next();
			if (component instanceof Editor<?>) {
				return (Editor<T>)component;
			}
			else {
				if (component instanceof WebMarkupContainer) {
					Editor<T> editor = getEditor(((WebMarkupContainer)component).iterator());
					if (editor!=null) {
						return editor;
					}
				}
			}
		}
		return null;
	}

		

	public Task getTask() {
		return getWorkflowModel()!=null ? getWorkflowModel().getObject().getTask() : null; 
	}



	public Activity getRunningActivity() {
		List<Activity> activities = getWorkflowModel().getObject().getProcess().getActivities();
		Activity activity = !activities.isEmpty() && activities.get(0).isRunning() ? activities.get(0) : null;
		return activity;
	}
	

	protected User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	

	protected Component newSubmitLink()  {
	
		return new AjaxSubmitLink("submit-link", getEditor().getForm()) {
			@Override
			public void onSubmit(AjaxRequestTarget target) {
 				Editor<T> editor = getEditor();
 				editor.update(target);
 			}
			
			@Override
			public boolean isVisible() {
				return getTask()!=null && getRunningActivity()!=null && getRunningActivity().getUser().equals(getUser());
			}
			
			
			@Override
			public boolean isEnabled() {
				return getTask()!=null && getRunningActivity()!=null && getRunningActivity().getUser().equals(getUser());
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<span class=\"far fa-save\"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 220);";
						
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String id = component.getMarkupId();
						return "document.getElementById('"+id+"').innerHTML = '<i class=\""+ com.novamens.wicket.markup.html.form.Form.SPINNING +" fa-fw \" style=\"font-size:16px; color:white;\"></i>'";
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
 		};
 	}
	
	
	
	@SuppressWarnings("unchecked")
	protected T getContent() {
		return (T)((KbeeContext)getWorkflowModel().getObject()).getContent();
	}
	

 	protected boolean isPortalEnabled() {
		if (isportal==null) {
			isportal = ServiceLocator.getService(UserService.class).getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.PORTAL);
			if (isportal==null)
				isportal="no";
		}
		return isportal.equals("yes") || isportal.equals("true");
	}
 	

	protected boolean isMonitorable(T content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(content);
	}

}

