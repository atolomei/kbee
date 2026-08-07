package com.novamens.content.web.idoc.markup;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;

import com.novamens.content.base.Content;
import com.novamens.content.base.Relation;
import com.novamens.content.document.IDoc;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.relationshipsbycriteria.RelationshipsByCriteriaService;
import com.novamens.content.web.content.markup.AbstractPanel;
import com.novamens.content.web.content.markup.ContentInfoPanel;
import com.novamens.content.web.content.markup.ContentPanel;
import com.novamens.content.web.content.markup.CustomAttributesPanel;
import com.novamens.content.web.content.markup.PrivatePanel;
import com.novamens.content.web.content.markup.RelationPanel;
import com.novamens.content.web.content.markup.ReverseRelationByCriteriaPanel;
import com.novamens.content.web.content.markup.TitlePanel;
import com.novamens.content.web.resource.markup.ResourcesPanel;
import com.novamens.content.web.workflow.markup.TaskPanel;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;

import kbee.web.event.wicket.FullScreenEvent;
import kbee.web.event.wicket.PreviewClickEvent2;
import kbee.web.workflow.ProcessHistoryPanel;

@Deprecated
@SuppressWarnings("serial")
public class IDocPanel extends ContentPanel<IDoc> {  
	private static final long serialVersionUID = 1L;

	boolean is_full_width = false;
	static final boolean PUBLIC_AREA = true;
	
	/** 
	 * @param model
	 * @param select_preference
	 */
	public IDocPanel(IModel<IDoc> model, boolean select_preference) {
		super(model);
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		setReadOnly(true);

		if (select_preference) {
			String onepanel = getSessionUser().getService(PreferencesService.class).getValue(TaskPanel.class.getSimpleName(), "one-panel", "no");
			if (onepanel.equals("no"))
					setRightPanelVisible(true);
			else
				setRightPanelVisible(false);
			setFullWidth(true);
		}
		else
			setFullWidth(true);
		

		WebMarkupContainer leftpanel = new WebMarkupContainer("left-panel");
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		leftpanel.add( new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				return isFullWidth() ? " container-fluid one-panel" : " container one-panel";
			}											
		}));
		
		leftpanel.add( new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				return !isRightPanelVisible() ? ("one-panel toleft toright " + (isFullWidth() ? " container-fluid" : " container")) : "left-panel toleft toright col-md-8 col-xs-12 col-lg-9 ui-resizable two-panels";
			}											
		}));

		leftpanel.add(new TitlePanel<IDoc>());
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		if (getModel().getObject().getContentTemplate().isTreeFile()) {
			tabs.add(new AbstractTab( new Model<String>(getModel().getObject().getContentTemplate().getTreeFileLabel())) {
				@Override
				public Panel getPanel(String panelId) {
					return new ResourcesPanel<IDoc>(panelId) {
						protected void onContainerFluid(AjaxRequestTarget target) {
							setFullWidth(!isFullWidth());
							getSessionUser().getService(PreferencesService.class).setValue(IDocPanel.class.getSimpleName(), "container-fluid", isFullWidth()?"yes":"no");
							target.add(IDocPanel.this);
						}
					};
				}
			});
		}
		
		
		if (getModel().getObject().getContentTemplate().isResources()) {
			tabs.add(new AbstractTab( new Model<String>(getModel().getObject().getContentTemplate().getResourcesLabel() + " (" + String.valueOf(getModel().getObject().getResources(PUBLIC_AREA).size())+")" )) {
				@Override
				public Panel getPanel(String panelId) {
					return new ResourcesPanel<IDoc>(panelId) {
						protected void onContainerFluid(AjaxRequestTarget target) {
							setFullWidth(!isFullWidth());
							getSessionUser().getService(PreferencesService.class).setValue(IDocPanel.class.getSimpleName(), "container-fluid", isFullWidth()?"yes":"no");
							target.add(IDocPanel.this);
						}
					};
				}
			});
		}
		 
		
		if (getContentTemplate().isAbstract()) {
			tabs.add(new AbstractTab(new Model<String>(getModel().getObject().getContentTemplate().getAbstract_label())) {
				@Override
				public Panel getPanel(String panelId) {
					return new AbstractPanel<IDoc>(panelId, false);
				}
			});
		}
		
		if (!getContentTemplate().getRelations().isEmpty()) {
			for (RelationTemplate template : getContentTemplate().getRelations()) {
				if (template.getState()==ObjectState.ENABLED) {
					IModel<RelationTemplate> templatemodel = new com.novamens.wicket.model.ObjectModel<RelationTemplate>(template, true);
					tabs.add(new AbstractTab(new Model<String>() {
						public String getObject() {
							RelationTemplate template = templatemodel.getObject();
							List<Relation> relations = getModelObject().getRelations(template);
							String label = template.getTargetLabel();
							label += " (" + relations.size() + ")";
							templatemodel.detach();
							return label;
						}
					}) {
						@Override
						public Panel getPanel(String panelId) {
							RelationPanel<IDoc> relationpanel = new RelationPanel<IDoc>(panelId, false);
							relationpanel.setReadOnly(true);
							relationpanel.setTemplateModel(templatemodel);
							return relationpanel;
						}
					});
				}
			}
		}
		
		if (!getContentTemplate().getReverseRelations().isEmpty()) {
			for (RelationTemplate template : getContentTemplate().getReverseRelations()) {
				if (template.getState()==ObjectState.ENABLED) {
					IModel<RelationTemplate> templatemodel = new ObjectModel<RelationTemplate>(template, true);
					tabs.add(new AbstractTab(new Model<String>() {
						public String getObject() {
							RelationTemplate template = templatemodel.getObject();
							List<Relation> relations = getModelObject().getReverseRelations(template);
							String label = template.getReverseLabel();
							label += " (" + relations.size() + ")";
							templatemodel.detach();
							return label;
						}
					}) {
						@Override
						public Panel getPanel(String panelId) {
							RelationPanel<IDoc> relationpanel = new RelationPanel<IDoc>(panelId, true);
							relationpanel.setReadOnly(true);
							relationpanel.setTemplateModel(templatemodel);
							return relationpanel;
						}
					});
				}
			}
		}

		if (getContentTemplate().acceptsRelationshipsByCriteria()) {
			Map<RelationshipByCriteriaTemplate,List<Content>> related = getModel().getObject().getService(RelationshipsByCriteriaService.class).getRelatedTemplates();
			for (RelationshipByCriteriaTemplate template : related.keySet()) {
				IModel<RelationshipByCriteriaTemplate> templatemodel = new ObjectModel<RelationshipByCriteriaTemplate>(template, true);
				tabs.add(new AbstractTab(new Model<String>(template.getReverseLabel())) {
					@Override
					public Panel getPanel(String panelId) {
						ReverseRelationByCriteriaPanel<IDoc> relationpanel = new ReverseRelationByCriteriaPanel<IDoc>(panelId);
						relationpanel.setModel(getModel());
						relationpanel.setTemplateModel(templatemodel);
						return relationpanel;
					}
				});
			}
		}
		
		/**
		 * Tabs can have icon
		 * tabs.add(new AbstractTabWithIcon( new Model<String>(getModel().getObject().getContentTemplate().getPrivate_notes_label()), "warning fa fa-shield", "Secured Access") {
		 * 
		 */
		if (isPrivateEnabled()) {
			tabs.add(new AbstractTab( new Model<String>(getContentTemplate().getPrivate_notes_label()  + " (" + String.valueOf(getModel().getObject().getResources(false).size()) + ")")) {
				@Override
				public Panel getPanel(String panelId) {
					return new PrivatePanel<IDoc>(panelId, IDocPanel.this.getModel());
				}
			});
		}

		
		if (getContentTemplate().isCustomAttributes()) {
			tabs.add(new AbstractTab(new Model<String>(getContentTemplate().getCustomattributes_label())) {
				@Override
				public Panel getPanel(String panelId) {
					return new CustomAttributesPanel<IDoc>(panelId, getModel());
				}
			});
		}
		
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.process.history", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new ProcessHistoryPanel<IDoc>(panelId, getModel(), getActivities());
			}
		});
		
		AjaxTabbedPanel<ITab> tabbedpanel = new AjaxTabbedPanel<ITab>("tabs", tabs) {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
				getSessionUser().getService(PreferencesService.class).setValue(IDocPanel.class.getSimpleName(), "selected_tab", str);
				((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
			}
		};

		
		if (select_preference) {
			String selected_tab = getSessionUser().getService(PreferencesService.class).getValue(IDocPanel.class.getSimpleName(), "selected_tab", null);
			if (selected_tab!=null) { 
				int n = 0;
				for (ITab tab: tabs) {
					if (selected_tab.equals(tab.getTitle().getObject())) {
						tabbedpanel.setSelectedTab(n);		
						break;
					}
					n++;
				}
			}
		}
		
		leftpanel.add(tabbedpanel);
		
		form.add(leftpanel);
		add(form);
		
		WebMarkupContainer rightpanel = new WebMarkupContainer("right-panel") {
			@Override
			public boolean isVisible() {
				return isRightPanelVisible();
			}
		};
		
		rightpanel.setOutputMarkupId(true);
		
		rightpanel.add(new ContentInfoPanel<IDoc>(model, true) {
			public void onClose(AjaxRequestTarget target) {
				IDocPanel.this.setRightPanelVisible(false);
				target.add(IDocPanel.this);;
			}
		});
		
		form.add(rightpanel);
		
		add(form);
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		@SuppressWarnings("unchecked")
		AjaxTabbedPanel<ITab> tabs = (AjaxTabbedPanel<ITab>) get("form:left-panel:tabs");
		int sel = tabs.getSelectedTab();
		
		if (sel==-1)
			sel=0;
					
		String str = (tabs.getTabs().get(sel)).getTitle().getObject();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);

	}


	@Override
	public void showInfoPanel(AjaxRequestTarget target) {
		WebMarkupContainer rightpanel = (WebMarkupContainer)get("form:right-panel");
		setRightPanelVisible(true);
		rightpanel.addOrReplace(new ContentInfoPanel<IDoc>(getModel()) {
			public void onClose(AjaxRequestTarget target) {
				setRightPanelVisible(false);
				target.add(IDocPanel.this);;
			}
		});
		target.add(IDocPanel.this);
	}
	
	public ContentTemplate getContentTemplate() {
		return getModel().getObject().getContentTemplate();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (get("left-panel")!=null)
			get("left-panel").detach();
	}
	 
	@Override
	public boolean isFullWidth() {
		return this.is_full_width;
	}

	 
	protected void setFullWidth(boolean b) {
		this.is_full_width=b;
	}

	/**
	 * 
	 * @return
	 */
	protected List<IModel<Activity>> getActivities() {
	
		com.novamens.workflow.Process process= getModel().getObject().getService(WorkflowService.class).getLastProcess();
		
		List<IModel<Activity>> list_m = new ArrayList<IModel<Activity>>();
		
		if (process==null)
			return list_m;
		
		List<Activity> list= process.getActivities();
		
		if (list==null)
			return list_m;
		
		for (Activity a: list) {
			list_m.add(new ObjectModel<Activity>(a));
		}
		
		return list_m;
	}	
	
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<FullScreenEvent>() {
			public void onEvent(FullScreenEvent event) {
				setFullWidth(!isFullWidth());
				getSessionUser().getService(PreferencesService.class).setValue(IDocPanel.class.getSimpleName(), "container-fluid", isFullWidth() ? "yes" : "no");
				event.getRequestTarget().add(IDocPanel.this);
			}
		});
		
		
		add(new WicketEventListener<PreviewClickEvent2<IDoc>>() {
			@Override
			public void onEvent(PreviewClickEvent2<IDoc> event) {
				WebPage page = getPortalPreviewPage(event.getModel());
				if (page!=null)
					setResponsePage(page);
			}
		});
	}
}
