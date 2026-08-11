package kbee.web.model.eform;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.form.EForm;
import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemWithModelPanel;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.model.SerializableModel;

import kbee.web.console.SimpleMenuPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.model.object.AuditTrailObjectPanel;

@SuppressWarnings("serial")
public class EFormMainPanel extends ObjectEditor<EForm> implements PageMainTabs {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EFormMainPanel.class.getName());

	private IModel<ContentTemplate> templatemodel;
	private String initial_tab;
	private List<IModel<EForm>> m_forms;
	private String tab;
	private VerticalLayout<ITab> layout;
	
	public EFormMainPanel(IModel<ContentTemplate> templatemodel, IModel<EForm> model) {
		super("editor", model);
		setTemplate(templatemodel);
	
	}

	
	/** --------------------------------
	 * 
	 * 
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(getTabs());	
		int sel = layout.getSelectedTab();
		if (sel==-1)
			sel=0;
		String str = (layout.getTabs().get(sel)).getTitle().getObject();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
	}
	
	public IModel<ContentTemplate> getTemplateModel() {
		return templatemodel;
	}
	
	public void setTemplate(IModel<ContentTemplate> model) {
		this.templatemodel = model;
	}
	
	public void onCancel(AjaxRequestTarget target) {
	}
	
	@Override
	public void setEditionEnabled(boolean editionEnabled) {
		super.setEditionEnabled(editionEnabled);
		if (editionEnabled) {
			if (layout!=null)
				layout.setSelectedTab(0);		
		}
	}
	
	@Override
	public void setInitialTab(String a) {
			initial_tab=a;
			try {
				Integer in=Integer.valueOf(a)-1;
				if (in>=0 && in < layout.getTabs().size())
						layout.setSelectedTab(in.intValue());	
			} 
			catch (Exception e) {
				logger.error(e);
			}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}

	public String getTab() {
		return tab;
	}
	public void setTab(String tab) {
		this.tab=tab;
	}

	
	@Override
	public void onDetach() {
		super.onDetach();
		
		for(IModel<EForm> e:getIModelEforms())
			e.detach();
		if (templatemodel!=null)
			 templatemodel.detach();
	}
	
	public List<IModel<EForm>> getIModelEforms() {
		if (m_forms!=null) {
			return m_forms;
		}
		
		m_forms = new ArrayList<IModel<EForm>>();
		List<EForm> list = getTemplateModel().getObject().getForms();
		
		logger.debug(list.size());
		
        for (EForm e:list) {
        	if (e!=null) {
        		m_forms.add(getEFormModel(e));
        	}
        }
        return m_forms;
	}
	

	
	protected VerticalLayout<ITab> getTabs() {
		
		List<ITab> tabs = new ArrayList<ITab>();

		tabs.add(new AbstractTabKB( getLabel("eform"), "info")  {
			@Override
			public Panel getPanel(String panelId) {
				return new EFormEditor(panelId, getTemplateModel(), getModel());
			}
		});
		
		tabs.add(new AbstractTabKB(getLabel("viewer"), "template") {
			@Override
			public Panel getPanel(String panelId) {
				return new EFormViewerEditor(panelId, getTemplateModel(), getModel());
			}
		});
		
		tabs.add(new AbstractTabKB(getLabel("visibility"), "visibility") {
			@Override
			public Panel getPanel(String panelId) {
				return new EFormVisibilityEditor(panelId, getModel());
			}
		});
		
		tabs.add(new AbstractTabKB(getLabel("audit"), "audit") {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<EForm>(panelId, getModel());
			}
		});
		
		layout = new VerticalLayout<ITab>("tabs",this.getClass().getName(), tabs, VerticalLayout.VERTICAL) {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				try {
					int sel = getSelectedTab();
					((KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue(EFormMainPanel.class.getName(), "selectedtab", sel);
					String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
					((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
				} 
				catch (Exception e) {
					logger.error(e);
					fire (new ErrorEvent<>(target));
				}
			}
		};
		
		layout.setTitle(new StringResourceModel("sections", this, null));
		
        List <MenuItemFactory<EForm>>  menuitems =  new ArrayList<MenuItemFactory<EForm>>();
      
        for (IModel<EForm> ee: getIModelEforms()) {
        	
       		if (ee!=null && ee.getObject()!=null && ee.getObject().getName()!=null)
        		menuitems.add(
	              		 new MenuItemFactory<EForm>() {
	           				@Override
	           				public AbstractMenuItemPanelV5<EForm> getItem(String id) {
	           					
	           					return new MenuItemWithModelPanel<EForm>(id, ee ) {
	           						@Override
	           						public void onClick() {
	           							try {
	           								IModel<EForm> me  =  getMenuItemModel();
	           								setResponsePage( new EFormPage( me, getTemplateModel() ));
	           							} catch (Exception e) {
	           								logger.error(e);
	           								setResponsePage(new ApplicationErrorPage<>(e));
	           							}
	           						}
	           						@Override
	           						public String getLabel() {
	           							if (getModel().getObject().getDisplayName()!=null)
	           								return   getModel().getObject().getDisplayName() + " - " + (getModel().getObject().getFormAccessLevel()!=null?getModel().getObject().getFormAccessLevel().getDisplayName():"");
	           							return "null";
	           						}
	           					};
	           				}
	           			}
	              );
        	}
        
        
        menuitems.sort(new Comparator<MenuItemFactory<EForm>>() {
			@Override
			public int compare(MenuItemFactory<EForm> o1, MenuItemFactory<EForm> o2) {
				return o1.getItem("item").getLabel().compareToIgnoreCase(o2.getItem("item").getLabel()); 
			}
        });
        
        
       SimpleMenuPanel<ContentTemplate, EForm> panel = new SimpleMenuPanel<ContentTemplate, EForm>("header-bottom-panel", getTemplateModel(), menuitems);
       
       panel.setTitle(new StringResourceModel("eforms", EFormMainPanel.this, null));
       layout.setHeaderBottomPanel(panel);
       layout.setTitle(new StringResourceModel("sections", this, null));
       layout.setContentBottomPanel(new InvisiblePanel("content-bottom-panel"));
	   return layout;
	}
	
	
	protected void onClose(AjaxRequestTarget target) {
	}


	
	private IModel<EForm> getEFormModel(EForm form) {
		if (form instanceof Identifiable) {
			return new ObjectModel<EForm>(form);
		}
		if (form instanceof Serializable) {
			return new SerializableModel<EForm>(form);
		}
		return null;
	}
	
}