  package kbee.web.text.eforms;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.logging.log4j.LogManager;
import org.apache.wicket.ajax.AjaxPreventSubmitBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;

import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.document.IDoc;
import com.novamens.content.form.EForm;
import com.novamens.content.service.ContentService;

import com.novamens.content.web.content.markup.CustomAttributesPanel;
import com.novamens.content.web.workflow.markup.ResolutionPanel;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.workflow.KbeeWorkflowEvent;

import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import com.novamens.workflow.WorkflowContext;

import kbee.web.content.eform.ContentFormEditor;
import kbee.web.content.workflow.EOpenFormEvent;
import kbee.web.content.workflow.TaskHomePanel;
import kbee.web.content.workflow.TaskKnowledgeBasePanelV6;
import kbee.web.content.workflow.TaskPanel;
import kbee.web.content.workflow.TaskRelatedFilesPanelv6;
import kbee.web.content.workflow.TaskResolutionPanel;
import kbee.web.content.workflow.TitlePanel;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.NavigablePage;
import kbee.web.nav.Navigator;
import kbee.web.nav.NavigatorPanelV6;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.ProcessHistoryPanel;
import kbee.web.workflow.TaskCommentPanel;
import kbee.web.workflow.task.ActionEvent;
import kbee.web.workflow.task.EFormEvent;
import kbee.web.workflow.task.ValidationEvent;

/**
 * eForms version
 */
@SuppressWarnings("serial")
@Deprecated
public class TextTaskPanelV6 extends TaskPanel<OrganizationalText> implements PageMainTabs  {
	private static final long serialVersionUID = 1L;
	
	static final boolean PUBLIC_AREA = true;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TextTaskPanelV6.class.getName());

	private Panel taskPanel = null;
	
	private TaskResolutionPanel<IDoc> taskActionsPanel = null; 
	
	private String initial_tab;
	private VerticalLayout<ITab> layout;
	
	private Form<?> form;
	private PageContentHeaderPanel<OrganizationalText> header;
	
	/**
	 * 
	 * toolbar
	 * 
	 * resolution
	 * eform_1
	 * eform_2
	 * ...
	 * process history
	 * task
	 * 
	 */
	public TextTaskPanelV6(IModel<WorkflowContext> workflowmodel) {
		super(workflowmodel);
		setOutputMarkupId(true);

	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		setIsNew(getWorkflowModel().getObject().getProcess().getActivities().size()<=1);
		
		// Header ----------------- 
		header=new PageContentHeaderPanel<OrganizationalText>("header", getModel());
		header.setTitle(getModel().getObject().getDisplayName());
		header.setTitle(new TitlePanel<IDoc>("titlepanel"));
		header.setSubLine(new Model<String>(getModelObject().getService(ContentService.class).getConsoleSubtitle()));
		
		MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
		bc.addElement(new BCElement( new Model<String>(getWorkflowModel().getObject().getProcedure().getDisplayName())));
		bc.addElement(new BCElement( new Model<String>( getWorkflowModel().getObject().getTask().getDisplayName())));
		header.setBreadcrumbPanel(bc);
	

		
		// Billboard (due today etc.) -----------------
		//
		form = new Form<Void>("form", Disposition.VERTICAL) {
			public void onSubmit() {
			}
		};
		add(form);
		
		form.add(header);
		
		// Billboard (due today etc.) -----------------
		//
		form.add(getBillboard());
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		/** --------------------------------
		 * Task Resolution
		 */
		if (getTaskResolution()!=null) {

			tabs.add(new AbstractTabKB(getLabel("editor.resolution"), "resolution") {
				@Override
				public Panel getPanel(String panelId) {
					return new ResolutionPanel(panelId, getWorkflowModel());
				}
			});
		}
		
		/** --------------------------------
		 eForms
		 */	 
		for (EForm eform : getForms()) {
			try {
				addEditor(new ContentFormEditor<OrganizationalText>(getContent().getFormData(eform), this));
				tabs.add(new AbstractTabKB(new Model<String>(eform.getDisplayName()), eform.getName()) {
					@Override
					public Panel getPanel(String panelId) {
						return (Panel)getEditor(eform);
					}
				});
			}
			catch (Throwable e) {
				logger.error(e);
				IModel<String> title = eform!=null? new Model<String>(eform.getDisplayName()): new Model<String>("null");
				 tabs.add(new AbstractTabKB(title, title.getObject()) {
					@Override
					public Panel getPanel(String panelId) {
						String message = e.getCause()!=null ? e.getCause().getMessage() : e.getMessage();
						return new ErrorPanel(panelId, new Model<String>("Form Error"), new Model<String>(message));
					}
				});
			}
		}

		
		/** --------------------------------
		Task actions
		 */	 
		tabs.add(new AbstractTabKB(getLabel("editor.actions"), "actions") {
			@Override
			public Panel getPanel(String panelId) {
				try {
					if (taskActionsPanel == null) {
						taskActionsPanel =  new TaskResolutionPanel<IDoc>(panelId, getWorkflowModel());
					}
					return taskActionsPanel;
				} 
				catch (Exception e) {
					logger.error(e);
					return new ErrorPanel(panelId, e);
				}
 			}
		});
		
		/** --------------------------------
		Task Home
		 */	 
		tabs.add(new AbstractTabKB(getLabel("file-info"), "info") {
			@Override
			public Panel getPanel(String panelId) {
				try {
					if (taskPanel == null) {
						taskPanel =  new TaskHomePanel<IDoc>(panelId, getWorkflowModel());
					}
					return taskPanel;
				} 
				catch (Exception e) {
					logger.error(e);
					return new ErrorPanel(panelId, e);
				}
			}
		});
		
		if(getContent().getDomain().getDomainType()!=DomainType.EXPRESS && getTask()!=null) {
			tabs.add(new AbstractTab(new StringResourceModel("tab.related", this, null)) {
				@Override
				public Panel getPanel(String panelId) {
					return new TaskRelatedFilesPanelv6<IDoc>(panelId, getWorkflowModel());
				}
			});
		}
		
		if(getContent().getDomain().getDomainType()!=DomainType.EXPRESS && getTask()!=null && getTask().getKnowledgeCriteria()!=null) {
			tabs.add(new AbstractTabKB(getLabel("tab.kbase"), "kbase") {
				@Override
				public Panel getPanel(String panelId) {
					return new TaskKnowledgeBasePanelV6<IDoc>(panelId, getWorkflowModel());
				}
			});
		}
		

		/** --------------------------------
			Custom Attributes
		 */
		if (getModel().getObject().getContentTemplate().isCustomAttributes()) {
			tabs.add(new AbstractTabKB(new Model<String>(getModel().getObject().getContentTemplate().getCustomattributes_label()), "customtags" ) {
				@Override
				public Panel getPanel(String panelId) {
					return new CustomAttributesPanel<OrganizationalText>(panelId, getModel());
				}
			});
		}
		
		/** --------------------------------
			ProcessHistory
		*/ 
		tabs.add(new AbstractTabKB(getLabel("editor.history"), "history") {
			@Override
			public Panel getPanel(String panelId) {
				return new ProcessHistoryPanel<OrganizationalText>(panelId, getWorkflowModel(),  TextTaskPanelV6.this.getModel());
			}
		});
		
		layout  = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs, VerticalLayout.VERTICAL);
		
		layout.setContentTopPanel( getProcessChartPanel("content-top-panel"));
		
		layout.setTitle(getLabel("menu"));
		layout.setSections(VerticalLayout.COLS_9X3);
		layout.setHeaderBottomPanel(new TaskCommentPanel("header-bottom-panel", getWorkflowModel()));
		
		form.add(layout);
		form.add(new AjaxPreventSubmitBehavior());
	}

	@Override
	public void update(boolean auto) {
		try {
 			if (!getUpdatedFields().isEmpty()) {
				long start=System.currentTimeMillis();
				getModelObject().getService(ContentService.class).updateFields(getUpdatedFields());
				logger.debug("ContentService.class).update() -> "+ String.valueOf(System.currentTimeMillis()-start)+" ms");
				super.reset();
			}
		}
		catch (Exception e) {
			LogManager.getLogger(TextTaskPanelV6.class.getName()).error(e.getClass().getName());
			throw new KbeeRuntimeException(e);
		}
	}
	
	@Override
	public void update(AjaxRequestTarget target, boolean auto) {
		// lock
		if (validateUser()) {
			update(false);
		}
		else {
			setEditionEnabled(false);
			target.add(this);
		}	
	}

	public VerticalLayout<ITab> getLayout() {
		return layout;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (getPage() instanceof NavigablePage<?>) {
			@SuppressWarnings("unchecked")
			Navigator<IDoc> navigator = ((NavigablePage<IDoc>)getPage()).getNavigator();
			NavigatorPanelV6<IDoc> np = new NavigatorPanelV6<IDoc>("navigation", navigator);
			header.setSearchNavigatorPanel(np);
		}
	}

	@Override
	protected void addListeners() {
		super.addListeners();
		add(new WicketEventListener<KbeeWorkflowEvent>() {
			public void onEvent(KbeeWorkflowEvent event) {
				update(false);
				getWorkflowService().handle(event, getWorkflowModel().getObject());
				clearValidation();
				// ((KbeeWebWorkflowEvent)event).getTarget().appendJavaScript((new CloseBehavior()).getScript());
			}
		});
		add(new WicketEventListener<ValidationEvent>() {
			public void onEvent(ValidationEvent event) {
				for (ITab tab : getLayout().getTabs(ContentFormEditor.class)) {
					Panel panel = getLayout().setSelectedTab(tab);
					if (panel instanceof ContentFormEditor) {
						((ContentFormEditor<?>)panel).setFocus(event.getRequestTarget(), event.getField());
						break;
					}
				}
				event.getRequestTarget().add(TextTaskPanelV6.this);
			}
		});
		add(new WicketEventListener<EOpenFormEvent>() {
			public void onEvent(EOpenFormEvent event) {
				for (ITab tab : getLayout().getTabs(ContentFormEditor.class)) {
					Panel panel = getLayout().setSelectedTab(tab);
					if (((ContentFormEditor<?>)panel).getData().getForm().getName().equals(event.getName())) {
						break;
					}
				}
				event.getRequestTarget().add(TextTaskPanelV6.this);
			}
		});
		add(new WicketEventListener<ActionEvent>() {
			public void onEvent(ActionEvent event) {
				update(false);
				TextTaskPanelV6.this.handle(event);
			}
		});
		add(new WicketEventListener<EFormEvent>() {
			public void onEvent(EFormEvent event) {
				TextTaskPanelV6.this.handle(event);
			}
		});
	}
	
	@Override
	public void setInitialTab(String a) {
		initial_tab=a;
		try {
			layout.setSelectedTab(a);
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}
}