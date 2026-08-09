package kbee.web.searcher.panel;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.form.EForm;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.FileSnippet;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.wicket.markup.html.console.grid.DateFormatModel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;

import kbee.web.content.editor.ContentEditor;
import kbee.web.content.eform.ContentFormViewer;
import kbee.web.content.panel.SnippetsPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.util.PanelBeanResolver;
import kbee.web.workflow.TaskInfoPanel;
import kbee.web.workflow.util.WorkflowContextModel;

@SuppressWarnings("serial")
public class SearcherContentExpandedViewPanel<T extends Content> extends ContentEditor<T> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherContentExpandedViewPanel.class.getName());

	private IModel<T> model;
	private String textquery;
	
	private IModel<Site> siteModel;
	
	private List<FileSnippet> snippets = null;
	
	public SearcherContentExpandedViewPanel(IModel<T> model, String textquery, IModel<Site> siteModel) {
		super("expanded-panel", model);
		this.model = model;
		this.textquery = textquery;
		this.siteModel=siteModel;
		
	}

	public 	IModel<Site> getSiteModel() {
		return this.siteModel;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (model != null)
			model.detach();

		if (siteModel!=null)
			siteModel.detach();
	}

	public IModel<T> getModel() {
		return model;
	}
	
	@Override
	public void setIsNew(boolean value) {
	}
	
	@Override
	public boolean isNew() {
		return false;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer workflow = new WebMarkupContainer("workflow") {
			public boolean isVisible() {
				return false;
				//return isWriteable() && getModelObject().isLocked();
			}
		};
		
		Label status = new Label("status", new Model<String> () {
			public String getObject() {
				WorkflowContext wc = getModelObject()
					.getService(ContentService.class)
					.getWorkflow();
				String label = "";
				if (wc.getProcess()!=null) {
					Activity activity = wc.getCurrentActivity();
					User user = activity.getUser();
					String task = activity.getTask().getDisplayName();
					OffsetDateTime startTime = (activity.getStartTime());
					DateFormatModel datemodel = new DateFormatModel(startTime, false, null, ""); 
					label = getLabelString("workflow_status", 
						user.getDisplayName(),
						task,
						datemodel.getObject());
				}	
				return label;
			}
		});
		status.setEscapeModelStrings(false);
		workflow.add(status);
		
		add(workflow);
	}	
		
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("tabs")!=null)
			return;
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		if (getSnippets()!=null && !getSnippets().isEmpty()) {
			tabs.add(new AbstractTab(getLabel("snippets")) {
				@Override
				public Panel getPanel(String panelId) {
					try {
						return new SnippetsPanel<T>(panelId, getModel(), textquery, getSnippets());
					} 
					catch (Exception e) {
						logger.error(e);
						return (Panel) add(new InvisiblePanel(panelId));
					}
				}
			});
		}
		
		boolean has_eforms = false;
		
		/** --------------------------------
		 eForms
		 */	 
		for (EForm eform : getForms()) {
			try {
				tabs.add(new AbstractTabKB(new Model<String>(eform.getDisplayName()), eform.getName()) {
					@Override
					public Panel getPanel(String panelId) {
						return new ContentFormViewer<T>(panelId, getModel(), eform);
					}
				});
				
				has_eforms = true;
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

		
		
		
		tabs.add(new AbstractTabKB( getLabel("summary"), "summary") {
			@Override
			public Panel getPanel(String panelId) {
				return new PanelBeanResolver(
						"searcher-summary-panel", 
						panelId,
						getModel(), 
						getSiteModel(), 
						false).getPanel();
			}
		});
		
		
		if (!has_eforms) {
			if (getModel().getObject() instanceof ResourceContainer) {
				IModel<String> labelmodel = new Model<String>() {
					public String getObject() {
						String label = getModel().getObject().getContentTemplate().getResourcesLabel();
						int numberofresources = ((ResourceContainer)getModelObject()).getPortalEnabledResources().size();
						label += " (" + String.valueOf(numberofresources) + ")";
						return label;
					}
				};
				tabs.add(new AbstractTab(labelmodel) {
					@Override
					public Panel getPanel(String panelId) {
						try {
							
							SearcherDetailResourcesPanel<T> panel = new SearcherDetailResourcesPanel<T>(panelId, 
									getModel(), getSiteModel(), true, false);
							return panel;
						} 
						catch (Exception e) {
							logger.error(e);
							return (Panel) add(new InvisiblePanel(panelId));
						}
					}
				});
			}
			tabs.add(new AbstractTab(getLabel("attributes")) {
				@Override
				public Panel getPanel(String panelId) {
					try {
						SearcherDetailAttributesPanel<T> panel = new SearcherDetailAttributesPanel<T>(panelId, getModel(), getSiteModel(), false);
						panel.setHasTitle(false);
						return panel;
					} 
					catch (Exception e) {
						logger.error(e);
						return (Panel) add(new InvisiblePanel(panelId));
					}
				}
			});
		}
		

		
		if (isWriteable() && getModelObject().isLocked()) {
			WorkflowContext wc = getModelObject()
					.getService(ContentService.class)
					.getWorkflow();
			IModel<WorkflowContext> model = new WorkflowContextModel<T>(wc);
			tabs.add(new AbstractTab(getLabel("status")) {
				@Override
				public Panel getPanel(String panelId) {
					try {
						TaskInfoPanel<T> pa = new TaskInfoPanel<T>(panelId, model);
						return pa;
					}
					catch (Exception e) {
						logger.error(e);
						return (Panel) add(new InvisiblePanel(panelId));
					}
				}
			});	
		}

		/*
 		boolean hasrel =  ((getModel().getObject().getRelations()!=null) && (getModel().getObject().getRelations().size()>0)) || ((getModel().getObject().getReverseRelations()!=null) && (getModel().getObject().getReverseRelations().size()>0));
		if (hasrel) {
			tabs.add(new AbstractTab(getLabel("relationships")) {
				@Override
				public Panel getPanel(String panelId) {
					try {
						SearcherDetailRelationshipPanel<T> panel = new SearcherDetailRelationshipPanel<T>(panelId, getModel(), getSiteModel()); 
						return panel;
					} 
					catch (Exception e) {
						logger.error(e);
						return (Panel) add(new InvisiblePanel(panelId));
					}
				}
			});
		}
		*/
		
		AjaxTabbedPanel<ITab> tabbedpanel = new AjaxTabbedPanel<ITab>("tabs", tabs) {
			protected String getNavCss() {
				return "nav nav-tabs";
			}
		};
		
		add(tabbedpanel);
	}
	
	public List<FileSnippet> getSnippets() {
		if (snippets==null) {
			snippets = searchSnippets();
		}
		return this.snippets;
	}
	
	private List<FileSnippet> searchSnippets() {
		try {
			return getModel().getObject().getService(ContentService.class).getSnippets(textquery, true);
		} 
		catch (Exception e) {
			logger.error(e);
			return new ArrayList<FileSnippet>();
		}
	}
	
	protected List<EForm> getForms() {
		
		List<EForm> forms = new ArrayList<EForm>();
		
		if (getModelObject().getContentTemplate().getForms()==null)
			return forms;
		
		for (EForm form : getModelObject().getContentTemplate().getForms()) {
			
			if (	form.isUseInline()) {
				forms.add(new KbeeTaskForm(form));
			}
		}
		return forms;
	}
	
	protected boolean isWriteable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(getModelObject());
	}	
}
