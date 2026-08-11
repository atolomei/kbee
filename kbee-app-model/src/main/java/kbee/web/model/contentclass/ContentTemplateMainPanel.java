package kbee.web.model.contentclass;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.model.object.AuditTrailObjectPanel;


/**
 * 
 * 
 */
@SuppressWarnings("serial")
public class ContentTemplateMainPanel extends DomainObjectEditor<ContentTemplate> implements PageMainTabs   {

	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentTemplateMainPanel.class.getName());
	
	private boolean isNew;
	private String initial_tab;
	
	
	public ContentTemplateMainPanel(IModel<ContentTemplate> model, boolean isNew) {
		super("editor", model);
		this.isNew = isNew;
		setModel(model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addEditor();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setInitialTab(String a) {
		initial_tab=a;
		try {
			((VerticalLayout<ITab>) get("tabs")).setSelectedTab(a);
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}
	
	protected void onClose(AjaxRequestTarget target) {
	}
	
	protected void addEditor() {
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTabKB(getLabel("editor.info"), "info" ) {
			@Override
			public Panel getPanel(String panelId) {
				return new ContentTemplateInfoEditor(panelId, getModel(), isNew) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						addEditor();
						target.add(ContentTemplateMainPanel.this);
					}
					protected void onClose(AjaxRequestTarget target) {
						ContentTemplateMainPanel.this.onClose(target);
					}
				};
			}
		});

		tabs.add(new AbstractTabKB(getLabel("editor.state"), "status") {
			@Override
			public Panel getPanel(String panelId) {
				return new ContentClassStateEditor(panelId, getModel());
			}
		});

		

		
		
		tabs.add(new AbstractTabKB(getLabel("editor.structure"), "structure") {
			@Override
			public Panel getPanel(String panelId) {
					return new ContentClassStructureEditor(panelId, getModel());
			}
		});

			tabs.add(new AbstractTabKB(getLabel("eforms"), "eforms") {
				@Override
				public Panel getPanel(String panelId) {
					return new ContentTemplateEFormsEditor(panelId, new ObjectModel<ContentTemplate>(getModel().getObject()));
				}
			});

			
			tabs.add(new AbstractTabKB(getLabel("editor.workflow"), "workflow") {
				@Override
				public Panel getPanel(String panelId) {
					return new ContentClassWorkflowEditor(panelId, getModel());
				}
			});

			
			tabs.add(new AbstractTabKB(getLabel("editor.display"), "display") {
				@Override
				public Panel getPanel(String panelId) {
					return new ContentClassDisplayEditor(panelId, getModel());
				}
			});

		

		
		tabs.add(new AbstractTabKB(getLabel("editor.relations"), "relations") {
			@Override
			public Panel getPanel(String panelId) {
				return new RelationsEditor(panelId, getModel());
			}
		});
		
		if (getModelObject().includesRelationshipsByCriteria()) {
			tabs.add(new AbstractTabKB(new StringResourceModel("editor.relationshipsbycriteria", this, null), "criteriarelations") {
				@Override
				public Panel getPanel(String panelId) {
					return new RelationsByCriteriaEditor(panelId, getModel());
				}
			});
		}
		
		tabs.add(new AbstractTabKB(getLabel("editor.resourceTags"), "resourceTags") {
			@Override
			public Panel getPanel(String panelId) {
				return new ContentTemplateResourceTagsEditor(panelId, new ObjectModel<ContentTemplate>(getModel().getObject()));
			}
		});
		
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.audit", this, null), "audit") {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<ContentTemplate>(panelId, new ObjectModel<ContentTemplate>(getModel().getObject()));
			}
		});
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs, VerticalLayout.VERTICAL) {

			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				try {

					int sel = getSelectedTab();
					((KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue(ContentTemplateMainPanel.class.getName(), "selectedtab", sel);
					String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
					((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
				} 
				catch (RuntimeException e) {
					logger.error(e);
					fire (new ErrorEvent<>(target, e));
				}
			}
		};
		
		editor.setTitle(new StringResourceModel("sections", this, null));
		addOrReplace(editor);
	}


}
