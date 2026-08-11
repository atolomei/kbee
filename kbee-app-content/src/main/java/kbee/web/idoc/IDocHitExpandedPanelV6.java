package kbee.web.idoc;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EForm;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.FileSnippet;
import com.novamens.content.web.content.markup.CustomAttributesPanel;
import com.novamens.content.web.workflow.markup.ResolutionPanel;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;

import kbee.web.content.eform.ContentFormViewer;
import kbee.web.content.panel.FileMetaInfoPanel;
import kbee.web.content.panel.SnippetsPanel;
import kbee.web.content.workflow.TaskHomePanel;
import kbee.web.dashboard.LabelPanel;
import kbee.web.workflow.ProcessHistoryPanel;
import kbee.web.workflow.util.WorkflowContextModel;

@SuppressWarnings("serial")
public class IDocHitExpandedPanelV6 extends ModelPanel<IDoc> implements IAjaxIndicatorAware {
	private static final long serialVersionUID = 1L;
																	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IDocHitExpandedPanelV6.class.getName());
	
	private boolean is_workflow = false;
	private String query;
	private List<FileSnippet> snippets = null;
		
	private AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();
	
	
	public interface PanelFactoryBase {
		Panel getPanel(String id);
	}
	
	
	public class PanelFactory {
		PanelFactoryBase factory;
		public PanelFactory(PanelFactoryBase factory) {
			this.factory = factory;
		}
		public Panel getPanel(String panelId) {
			try {
				return factory.getPanel(panelId);
			}
			catch (Exception e) {
				logger.error("Can not create Panel.");
				logger.error(e);
				return new InvisiblePanel(panelId);
			}
		}
	}
	
	
	/**
	 * 
	 * @param id
	 * @param model
	 */
	public IDocHitExpandedPanelV6(String id, IModel<IDoc> model) {
		this(id, model, ViewMode.ICON, Boolean.valueOf(false), null, null);
	}
	
	public IDocHitExpandedPanelV6(String id, IModel<IDoc> model, boolean is_workflow) {
		this(id, model, ViewMode.ICON, Boolean.valueOf(is_workflow), null, null);
	}
	
	public IDocHitExpandedPanelV6(IModel<IDoc> model) {
		this("editor", model, ViewMode.ICON, Boolean.valueOf(false), null, null);
	}
	
	public IDocHitExpandedPanelV6(IModel<IDoc> model, ViewMode view_mode, Boolean is_workflow) {
		this("editor", model, view_mode, is_workflow, null, null);
	}
	
	public IDocHitExpandedPanelV6(IModel<IDoc> model, ViewMode view_mode, Boolean is_workflow, String query, List<String> snippets) {
		this("editor", model, view_mode, is_workflow, query, snippets);
	}
	
	public IDocHitExpandedPanelV6(String id, IModel<IDoc> model, ViewMode view_mode, Boolean is_workflow, String query, List<String> snippets) {
		super(id, model);

		setOutputMarkupId(true);
		
		this.is_workflow =is_workflow.booleanValue();
		this.query = query;
		this.add(indicatorAppender);
		
	}
	
	protected List<FileSnippet> getSnippets() {
		if (snippets==null) {
			snippets = searchSnippets();
		}
		return this.snippets;
	}
	
	public String getAjaxIndicatorMarkupId() {		
		return indicatorAppender.getMarkupId();
	}
	
	@Override
	public void onInitialize( ) {
		super.onInitialize();
	
		List<ITab> tabs = new ArrayList<ITab>();
		
		if (getSnippets()!=null && !getSnippets().isEmpty()) {
			tabs.add(new AbstractTab(getLabel("snippets")) {
				@Override
				public Panel getPanel(String panelId) {
					return (new PanelFactory(id -> new SnippetsPanel<IDoc>(id, getModel(), query, getSnippets()))).getPanel(panelId);
				}
			});
		}
		
		try {
			List<EForm> list = getForms();
			for (EForm eform : list) {
				tabs.add(new AbstractTabKB(new Model<String>(eform.getDisplayName()), eform.getName()) {
					@Override
					public Panel getPanel(String panelId) {
						return getViewer(panelId, eform);
					}
				});
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
		
		
		
		
		
		
		// ----------------------------------------------------------
		if (this.is_workflow || inWorkflow(getModelObject())) {
			tabs.add(new AbstractTab(getLabel("workflow")) {
				@Override
				public Panel getPanel(String panelId) {
					try {
						TaskHomePanel<IDoc> pa=new TaskHomePanel<IDoc>(panelId, getWorkflowModel());
						pa.setFileInfo(false);
						pa.setProfiles(false);
						pa.setInformationModel(false);
						pa.setComment(true);
						return pa;
					}
					catch (Exception e) {
						logger.error(e);
						return new LabelPanel(panelId,  new Model<String>(e.getClass().getSimpleName()));
					}
					
				}
			});
			if (getPreviousTaskResolution()!=null) {
				if (getPreviousTaskResolution().getResolution()!=null) {
					tabs.add(new AbstractTab( new Model<String>(getPreviousTaskResolution().getResolutionTitle())) {
						@Override
						public Panel getPanel(String panelId) {
							try {
								return (new PanelFactory(id -> new ResolutionPanel(id, getWorkflowModel()))).getPanel(panelId);
							}
							catch (Exception e) {
								logger.error(e);
								return new LabelPanel(panelId,  new Model<String>(e.getClass().getSimpleName()));
							}
						}
					});
				}
			}
		}
		
		tabs.add(new AbstractTab(getLabel("file-summary")) {
			@Override
			public Panel getPanel(String panelId) {
				try {
					return (new FileMetaInfoPanel<IDoc>(panelId, getModel()));
				}
				catch (Exception e) {
					logger.error(e);
					return new LabelPanel(panelId,  new Model<String>(e.getClass().getSimpleName()));
				}
			}
		});
		
		if (getModel().getObject().getContentTemplate().isCustomAttributes()) {
			tabs.add(new AbstractTab(new Model<String>(getModel().getObject().getContentTemplate().getCustomattributes_label())) {
				@Override
				public Panel getPanel(String panelId) {
					try {
						return new CustomAttributesPanel<IDoc>(panelId, getModel());
					}
					catch (Exception e) {
						logger.error(e);
						return new LabelPanel(panelId,  new Model<String>(e.getClass().getSimpleName()));
					}
				}
			});
		}
		
		if (this.is_workflow) {		
			tabs.add(new AbstractTab(getLabel("history")) {
				@Override
				public Panel getPanel(String panelId) {
					try {
						return (new PanelFactory(id -> new ProcessHistoryPanel<IDoc>(panelId, getWorkflowModel(), getModel()))).getPanel(panelId);
					}
					catch (Exception e) {
						logger.error(e);
						return new LabelPanel(panelId,  new Model<String>(e.getClass().getSimpleName()));
					}
				}
			});
		}

		AjaxTabbedPanel<ITab> tabbedpanel = new AjaxTabbedPanel<ITab>("tabs",tabs) {
			private static final long serialVersionUID = 1L;
			protected String getNavCss() {
				return "nav nav-tabs";
			}
		};
		
		add(tabbedpanel);
	}

	private IModel<WorkflowContext> getWorkflowModel() {
		WorkflowService workflowService = getModelObject().getService(WorkflowService.class);
		if (workflowService!=null) {
			WorkflowContext workflowcontext = workflowService.getContext();
			IModel<WorkflowContext> workflowmodel  =  new WorkflowContextModel<IDoc>(workflowcontext);
			return workflowmodel;
		}
		else
			return null;
	}
	
	protected Activity getPreviousTaskResolution() {
		return ((KbeeContext)getWorkflowModel().getObject()).getPreviousTaskResolution();	
	}
	
	/**
	 * Forms to be displayed INLINE
	 * 
	 * @return
	 */
	private List<EForm> getForms() {
		List<EForm> forms = new ArrayList<EForm>();
		for (EForm form : getModelObject().getContentTemplate().getForms()) {
			
			if (form.isUseInline()) {
				// VER AT	
				if (!(form.getFormAccessLevel()==EFormAccessLevel.INTERNAL_INFO))
					forms.add(new KbeeTaskForm(form));
			}
		}
		return forms;
	}
	
	private Panel getViewer(String panelid, EForm form) {
		return new ContentFormViewer<IDoc>(panelid, getModel(), form);
	}
	
	private List<FileSnippet> searchSnippets() {
		try {
			return getModelObject().getService(ContentService.class).getSnippets(query);
		} 
		catch (Exception e) {
			throw (e);
		}
	}
	
	private boolean inWorkflow(Content content) {
		return content.getService(WorkflowService.class).getTask()!=null;
	}
}
