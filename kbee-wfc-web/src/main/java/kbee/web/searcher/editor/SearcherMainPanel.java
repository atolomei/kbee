package kbee.web.searcher.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExternalDao;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.editor.DomainObjectMainPanel;
import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;

/**
 * SearcherGeneralInfoEditor
 * SearcherHomeEditor
 * SearcherContactEditor
 * SearcherAboutEditor
 *
 */
public class SearcherMainPanel extends DomainObjectMainPanel<Site> {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherMainPanel.class.getName());
	
	public SearcherMainPanel(IModel<Site> model) {
		this(model, false);
	}
	
	public SearcherMainPanel(IModel<Site> model, boolean is_new) {
		super("editor", model);
		
		add(new SearcherSiteEditorHeaderPanel(model));
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.general-info", this, null)) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public Panel getPanel(String panelId) {
				return new SearcherSiteEditor(panelId, getModel(), is_new) {
					private static final long serialVersionUID = 1L;
				};
			}
		});

		
		tabs.add(new AbstractTab(new StringResourceModel("editor.state", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
			 	return new ObjectStateEditor<Site>(panelId, getModel(),  false);
			}
		});

		
		if (getSiteDataSetMemberModel()!=null) {
			tabs.add(new AbstractTab(new StringResourceModel("editor.attributes", this, null)) {
				private static final long serialVersionUID = 1L;
				@Override
				public Panel getPanel(String panelId) {
					return new SearcherSiteAttributesEditor(panelId, getSiteDataSetMemberModel());
				}
			});
		}

		
		tabs.add(new AbstractTab(new StringResourceModel("editor.iql", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new SearcherSiteIqlEditor(panelId, getModel()) {
					private static final long serialVersionUID = 1L;
				};
			}
		});

		/**
		tabs.add(new AbstractTab(new StringResourceModel("editor.facets", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new SearcherSiteFacetsEditor(panelId, getModel()) {
					private static final long serialVersionUID = 1L;
				};
			}
		});
		*/

		
		tabs.add(new AbstractTab(new StringResourceModel("editor.home-info", this, null)) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public Panel getPanel(String panelId) {
				return new SearcherSiteHomeEditor(panelId, getModel()) {
					private static final long serialVersionUID = 1L;
				};
			}
		});

		
		tabs.add(new AbstractTab(new StringResourceModel("editor.about-info", this, null)) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public Panel getPanel(String panelId) {
				return new SearcherAboutEditor(panelId, getModel()) {
					private static final long serialVersionUID = 1L;
					/**
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						//((DataSetHeaderPanel<T>) DataSetMainPanel.this.get("dataset-panel")).onUpdate(target);
					}
					@Override
					protected void onClose(AjaxRequestTarget target) {
						SearcherMainPanel.this.onClose(target);							
					}
					*/
				};
			}
		});


		/**
		tabs.add(new AbstractTab(new StringResourceModel("editor.contact-info", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new SearcherSiteEditor(panelId, getModel(), is_new) {
					private static final long serialVersionUID = 1L;
				};
			}
		});
		*/


		tabs.add(new AbstractTab(new StringResourceModel("editor.audit", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<Site>(panelId, getModel());
			}
		});


		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs",this.getClass().getName(), tabs) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				try {
					String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
					((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
				} catch (RuntimeException e) {
					logger.error(e);
				}
			}
		};
		add(editor);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		@SuppressWarnings("unchecked")
		VerticalLayout<ITab> tabs = (VerticalLayout<ITab>) get("tabs");
		tabs.setTitle(new StringResourceModel("sections", this, null));
		
		int sel = tabs.getSelectedTab();
		if (sel==-1)
			sel=0;
		String str = (tabs.getTabs().get(sel)).getTitle().getObject();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
	}

	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	 
	protected void onClose(AjaxRequestTarget target) {
	}

	
	protected IModel<DataSetMember> getSiteDataSetMemberModel() {
		DataSet dataset = getExternalDao().getSiteDataSet(getDomain());
		DataSetMember  member = getExternalDao().findMemberByExternalId(getModel().getObject().getOId(), dataset);
		if (member!=null)
			return new ObjectModel<DataSetMember>(member);
		return null;
	}
	
	private ExternalDao getExternalDao() {
		return (ExternalDao) ServiceLocator.getService(BeansService.class).getBean("externalDao");
	}

	


	
}
