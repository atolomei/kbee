package kbee.web.content.workflow;

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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.HelpService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.content.web.workflow.markup.TaskPanel;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.console.browser.RefreshClickEvent;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailEvent;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.util.PropertiesFactory;
import kbee.web.content.workflow.TaskToolBarPanel;
import kbee.web.event.wicket.CancelWorkflowEvent;
import kbee.web.event.wicket.PreviewClickEvent2;
import kbee.web.nav.NavigablePage;
import kbee.web.nav.NavigationPanel;
import kbee.web.nav.Navigator;
import kbee.web.nav.NavigatorPanel;

/**
 * 
 * Bootstrap based. Navigation Bar
 * 
 * @param <T>
 * 
 */
@SuppressWarnings("serial")
public class TaskToolBarPanel<T extends Content> extends NavigationPanel<T> implements NavigablePage<Content> {
	
	private static final long serialVersionUID = 1L;
	
	//static String onNavigateBehavior = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.onNavigate", "navigate");
	
	static String SP = PropertiesFactory.getInstance("kbee").getProperties().getProperty("license.portal", "yes");
	
	static AtomicBoolean IS_HELP_VISIBLE;
	
	private String isportal = null;
	
	private IModel<WorkflowContext> model;
	private Editor<T> editor;

	public class HelpFragment extends Fragment {
		public HelpFragment(String id) {
			super(id, "help-fragment", TaskToolBarPanel.this);
			
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
			add(link);
		}
	}
	
	
	/** --------------------------------------------------------
	 * 
	 * 
	 * @param model
	 */
	public TaskToolBarPanel(IModel<WorkflowContext> model) {
		this("navigation", model);
	}
	
	public TaskToolBarPanel(String id, IModel<WorkflowContext> model, Searcher searcher, long index) {
		super(id);
		setOutputMarkupId(true);
		setWorkflowModel(model);
		add(newPreviewLink());
		add(newTakeActionPanel());
		add(newEndConditionsPanel());
		add(newDeletePanel());
		add(newHelpPanel());
	}
	
	public TaskToolBarPanel(String id, IModel<WorkflowContext> model) {
		super(id);
		setOutputMarkupId(true);
		setWorkflowModel(model);
		add(newPreviewLink());
		add((new Panel("navigator"){}).setVisible(false));
		add(newTakeActionPanel());
		add(newEndConditionsPanel());
		add(newDeletePanel());
		add(newHelpPanel());
	}
	
	public void navigate() {
	};
	
	public void onNavigate(Content object) {
	}

	public boolean isFromContentBase() {
		return false;
	};
	
	@SuppressWarnings("unchecked")
	public void setWorkflowModel(IModel<WorkflowContext> model) {
		if(get("endconditions")!=null) {
			((TaskActionsBar<T>)get("endconditions")).setWorkflowModel(model);
			addOrReplace(newSubmitLink());
		}	
		this.model = model;
	}

	public IModel<WorkflowContext> getWorkflowModel() {
		return model;
	}
	
	public Navigator<Content> getNavigator() {
		return null;
	}
	
	public void setNavigator(Navigator<Content> navigator) {
	}
	
	public Task getTask() {
		return getWorkflowModel()!=null ? getWorkflowModel().getObject().getTask() : null; 
	}

	public Activity getRunningActivity() {
		List<Activity> activities = getWorkflowModel().getObject().getProcess().getActivities();
		Activity activity = !activities.isEmpty() && activities.get(0).isRunning() ? activities.get(0) : null;
		return activity;
	}

	public long getIndex() {
		if (get("navigator")!=null && get("navigator") instanceof NavigatorPanel) {
			return ((NavigatorPanel<?>)get("navigator")).getIndex();
		}
		return -1;
	}
	
	public static boolean isHelpEnabled() {
		if (IS_HELP_VISIBLE!=null)
			return 	IS_HELP_VISIBLE.get();
			IS_HELP_VISIBLE = new AtomicBoolean(((ContentDao) (ServiceLocator.getService(BeansService.class).getBean("contentDao"))).findSystemParameterValueByKey("help.enabled", "no").toLowerCase().trim().equals("yes"));
		return 	IS_HELP_VISIBLE.get();
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		StringBuilder script = new StringBuilder();
		script.append("function closewindow() {\n");
		script.append("	if (window.opener && window.opener.refresh) { window.opener.refresh(); };\n");
		script.append("	var agent = navigator.userAgent;\n");
		script.append("	if (agent.indexOf('Edge') > 0 || agent.indexOf('Trident') > 0) {\n");
		script.append("		window.open('', '_self', '');\n");
		script.append("	}\n");
		script.append("	window.close();\n");
		script.append("}\n");
		response.render(JavaScriptHeaderItem.forScript(script.toString(), "closewindow"));
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addOrReplace(newSubmitLink());
		addOrReplace(newCloseLink());
		addOrReplace(newRefreshLink());
	}
	
	@Override
	public void onDetach() {
		if (this.model!=null)
			this.model.detach();
		super.onDetach();
	}
	
	protected void onReturn(AjaxRequestTarget target)  {
	}
	
	protected Component newCloseLink()  {
		AbstractLink link;
		if (getTask()!=null && getRunningActivity()!=null && getRunningActivity().getUser().equals(getUser())) { 
			link = new AjaxSubmitLink("close-link") {
				@Override
				public void onSubmit(AjaxRequestTarget target) {
	 				Editor<T> editor = getEditor();
	 				editor.update(target);
					target.appendJavaScript("closewindow();");
	 			}
				@Override
				public Form<?> getForm() {
					return getEditor().getForm();
				}
				@Override
				public boolean isVisible() {
					return true;
				}
			};	
		}
		else {
			link = new AjaxLink<Void>("close-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					target.appendJavaScript("closewindow();");
	 			}
				@Override
				public boolean isVisible() {
					return true;
				}
			};	
		}
		return link;
	}

	protected Component newHelpPanel()  {
		return  isHelpEnabled() ? new HelpFragment("help")  : new InvisiblePanel("help");
	}
	
	protected Component newKnowledgePanel()  {
		WebMarkupContainer knowledgecontanier = new WebMarkupContainer("knowledge-container") {
			public boolean isVisible() {
				WebTask task = (WebTask)getWorkflowModel().getObject().getTask();
				return task.getKnowledgeCriteria()!=null;
			}
		};
 		
 		knowledgecontanier.add(new AjaxLink<Void>("knowledge-link") {
 			public void onClick(AjaxRequestTarget target) {
 				Editor<T> editor = getEditor();
 				if (((TaskPanel<T>)editor).isRightPanelVisible()) { 					
 					((TaskPanel<T>)editor).setRightPanelVisible(false);
 					((KbeeUser) getUser()).getService(PreferencesService.class).setValue(TaskPanel.class.getSimpleName(), "one-panel", "yes");
 					target.add((TaskPanel<T>)editor);
 				}
 				else {
 					((KbeeUser) getUser()).getService(PreferencesService.class).setValue(TaskPanel.class.getSimpleName(), "one-panel", "no");
 					((TaskPanel<T>)editor).showKnowledgePanel(target);
 				}
 				target.add(TaskToolBarPanel.this);
 			}
 		});
 		
 		return knowledgecontanier;
 	}
	
	protected Component newAuditPanel() {
		return new AjaxLink<Void>("audit-link") {
			public void onClick(AjaxRequestTarget target) {
				fire(new AuditTrailEvent(target));
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
				return  getTask()!=null && ((WebTask)getTask()).isCancelEnabled() && !isSupportUser() || isAdminUser();
			}
		};
	}
	
	protected Component newEndConditionsPanel()  {
		return new TaskActionsBar<T>("endconditions", getWorkflowModel()) {
			@Override
			public boolean isVisible() {
				return getTask()!=null && getRunningActivity()!=null && getRunningActivity().getUser().equals(getUser()) ;
			}
			@Override
			protected Editor<T> getEditor() {
				return TaskToolBarPanel.this.getEditor(); 
			}
		};
	}
	
	protected Component newTakeActionPanel()  {
		WebMarkupContainer takepanel = new WebMarkupContainer("takepanel") {
			public boolean isVisible() {
				return getWorkflowModel()!=null ? getWorkflowModel().getObject().isPending() : false;
			}
		};
		takepanel.add(new TakeActionBar<T>("takebar", getWorkflowModel()) {
			@Override
			protected Editor<T> getEditor() {
				return TaskToolBarPanel.this.getEditor(); 
			}
		});
		return takepanel;
	}

	/**  
	 * ContentConsole: {@code getPortalPreviewPage(IModel<T> model)}
	 * TaskPanel: :    {@code getPortalPreviewPage(IModel<T> model)}
	 */
	protected Component newPreviewLink()  {
		return new Link<Void>("preview-link") {
			@Override
			public void onClick() {
				fire(new PreviewClickEvent2<T>(TaskToolBarPanel.this.getEditor().getModel()));
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

	protected boolean isPortalEnabled() {
		if (isportal==null) {
			isportal = ServiceLocator.getService(UserService.class).getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.PORTAL);
			if (isportal==null)
				isportal="no";
		}
		return isportal.equals("yes") || isportal.equals("true");
	}
	
	protected Component newRefreshLink()  {
		return new AjaxLink<Void>("refresh-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll(new RefreshClickEvent(target));
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
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<span class=\"far fa-sync\"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 220);";
						
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "if (typeof(tinyMCE) != \"undefined\") tinyMCE.triggerSave(true,true);";
						s += "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin fa-fw\" style=\"font-size:16px; color:white;\"></i>'";
						return s;
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
	
	protected Component newSubmitLink()  {
		return new AjaxSubmitLink("submit-link") {
			@Override
			public void onSubmit(AjaxRequestTarget target) {
 				Editor<T> editor = getEditor();
 				editor.update(target);
 			}
			@Override
			public void onError(AjaxRequestTarget target) {
 				//Editor<T> editor = getEditor();
			}
			@Override
			public boolean isVisible() {
				return getTask()!=null && getRunningActivity()!=null && getRunningActivity().getUser().equals(getUser()) ;
			}
			@Override
			public Form<?> getForm() {
				return getEditor().getForm();
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
						String s = null;
						String id = component.getMarkupId();
						s = "if (typeof(tinyMCE) != \"undefined\") tinyMCE.triggerSave(true,true);";
						s += "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin fa-fw\" style=\"font-size:16px; color:white;\"></i>'";
						return s;
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

	protected Editor<T> getEditor() {
		if (this.editor==null) {
			this.editor = getEditor(getPage().iterator());
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

	protected User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	private boolean isAdminUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
}