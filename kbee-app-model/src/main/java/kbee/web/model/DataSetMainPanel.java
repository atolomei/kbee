package kbee.web.model;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.DataSet;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.editor.DomainObjectMainPanel;
import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;

/** 
 * DataSet Editor in the Information Model section
 * <b>NOTE</b>  that this is different from {@link DataSetMember Editor}
 * 
 * 
 * <#if tipodocumento??>${tipodocumento}<#else>Documento</#if> ${oid} ${fechareferencia?string["dd/MM/yy"]} <#if fideicomiso??>${fideicomiso}</#if>
 * 
 * 
 * 
 */

@SuppressWarnings("serial")								
public class DataSetMainPanel<T extends DataSet> extends DomainObjectMainPanel<T> implements PageMainTabs {
			
	private static final long serialVersionUID = 1L;
														
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DataSetMainPanel.class.getName());
	
	public DataSetMainPanel(IModel<T> model) {
		this(model, false);
	}
	
	public DataSetMainPanel(IModel<T> model, boolean is_new) {
		super("editor", model);
				
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.info", this, null), "info") {
			@SuppressWarnings("unchecked")
			@Override
			public Panel getPanel(String panelId) {
				return new DataSetEditor<T>(panelId, getModel(), is_new) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						((DataSetHeaderPanel<T>) DataSetMainPanel.this.get("dataset-panel")).onUpdate(target);
					}
					@Override
					protected void onClose(AjaxRequestTarget target) {
						DataSetMainPanel.this.onClose(target);							
					}
				};
			}
		});
		
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.state", this, null), "status") {
			@Override
			public Panel getPanel(String panelId) {
				boolean real_only= false;
				return new ObjectStateEditor<T>(panelId, getModel(),  real_only);
			}
		});

		tabs.add(new AbstractTabKB(new StringResourceModel("editor.structure", this, null), "structure") {
			@Override
			public Panel getPanel(String panelId) {
				return new DataSetStructureEditor<T>(panelId, getModel());
			}
		});


		
		tabs.add(new AbstractTabKB( new StringResourceModel("editor.display", this, null), "display") {
			@Override
			public Panel getPanel(String panelId) {
				return new DataSetDisplayEditor<T>(panelId, getModel());
			}
		});
		
		if (!getModel().getObject().isAggregation()) {
			tabs.add(new AbstractTabKB(new StringResourceModel("editor.aggregations", this, null), "aggregations") {
				@Override
				public Panel getPanel(String panelId) {
					return new DataSetAggregationsPanel<T>(panelId, getModel());
				}
			});
		}
		
		
		

		tabs.add(new AbstractTabKB(new StringResourceModel("editor.classifiers", this, null), "classifiers") {
			@Override
			public Panel getPanel(String panelId) {
				return new DataSetUsedByPanel<T>(panelId, getModel());
			}
		});
		

		
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.audit", this, null), "audit") {
			@Override
			public Panel getPanel(String panelId) {
					IModel<T> model = new ObjectModel<T>(getModel().getObject());
					return new AuditTrailObjectPanel<T>(panelId, model);
				
			}
		});
		
		
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs, VerticalLayout.VERTICAL) {
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
		
		editor.setTitle(new StringResourceModel("sections", this, null));
		
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
		String str =  (tabs.getTabs().get(sel)).getTitle().getObject();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
	}

	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	 
	protected void onClose(AjaxRequestTarget target) {
	}
	
	private String initial_tab;
	
	@Override
	@SuppressWarnings("unchecked")
	public void setInitialTab(String a) {
			
		initial_tab=a;
		try {
				((VerticalLayout<ITab>) get("tabs")).setSelectedTab(a);
			
		} catch (Exception e) {
			logger.error(e);
		}
		
		/**
		initial_tab=a;
			
		
			
			try {
				Integer in=Integer.valueOf(a)-1;
				if (in>=0 && in < ((VerticalLayout<ITab>) get("tabs")).getTabs().size())
				((VerticalLayout<ITab>) get("tabs")).setSelectedTab(in.intValue());	
			} catch (Exception e) {
				logger.error(e);
			}
			*/
		
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}
	
	

	

}
