package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.HREFMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;

import kbee.util.logging.Logger;
import kbee.web.console.SimpleMenuPanel2;
import kbee.web.event.wicket.EditorEvent;
import kbee.web.model.contentclass.LauncherEditor;
import kbee.web.model.object.AuditTrailObjectPanel;

@SuppressWarnings("serial")
public class ProcedureMainPanel extends ObjectEditor<Procedure> implements PageMainTabs {
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = Logger.getLogger(ProcedureMainPanel.class.getName());

	private String initial_tab;
	
	public ProcedureMainPanel(IModel<Procedure> model) {
		super("editor", model);
		add(getTabs());
	}

	
	@Override
	public void setEditionEnabled(boolean editionEnabled) {
		super.setEditionEnabled(editionEnabled);
		if (editionEnabled) {
			@SuppressWarnings("unchecked")
			VerticalLayout<ITab> editor = (VerticalLayout<ITab>)get("tabs");
			if (editor!=null)
				editor.setSelectedTab(0);		
		}
	}
	
	public void onCancel(AjaxRequestTarget target) {
	}

	
	public ContentProcedure getProcedure() {
		return (ContentProcedure)getModelObject();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setInitialTab(String a) {
		initial_tab=a;
		try {
			Integer in=Integer.valueOf(a)-1;
			if (in>=0 && in < ((VerticalLayout<ITab>) get("tabs")).getTabs().size())
			((VerticalLayout<ITab>) get("tabs")).setSelectedTab(in.intValue());	
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
	
	protected VerticalLayout<ITab> getTabs() {
		List<ITab> tabs = new ArrayList<ITab>();

		tabs.add(new AbstractTab(getLabel("editor.procedure")) {
			@Override
			public Panel getPanel(String panelId) {
				return new ProcedureEditor(panelId, getModel());
			}
		});
		
		tabs.add(new AbstractTab(getLabel("editor.launchers")) {
			@Override
			public Panel getPanel(String panelId) {
				return new ProcedureLaunchersPanel(panelId, getModel()) {
					@Override
					public void onSelect(AjaxRequestTarget target, IModel<ProcessLauncher> model) {
						final int starting=3;
						ProcessLauncher launcher = model.getObject();
						@SuppressWarnings("unchecked")
						VerticalLayout<ITab> tabs = (VerticalLayout<ITab>)ProcedureMainPanel.this.get("tabs");
						for (int index=starting; index<tabs.getTabs().size(); index++) {
							List<IModel<ProcessLauncher>> launchers = getLaunchers();
							if (launcher.getId().equals(launchers.get(index-starting).getObject().getId())) {
								tabs.setSelectedTab(index-1);
								target.add(tabs);
								break;
							}
						}
					}
					public void onUpdate(AjaxRequestTarget target) {
						ProcedureMainPanel.this.addOrReplace(getTabs());
						target.add(ProcedureMainPanel.this);
					}
				};
			}
		});
		
		for (ProcessLauncher launcher : getLaunchers()) {
			IModel<ProcessLauncher> launchermodel = new ObjectModel<ProcessLauncher>(launcher, true);
			IModel<String> labelmodel = new Model<String>() {
				public String getObject() {
					return  getLabelString("editor.launcher", launchermodel.getObject().getLabel(), getContext(launchermodel.getObject()));
				}
			};
			tabs.add(new AbstractTab(labelmodel) {
				@Override
				public Panel getPanel(String panelId) {
					return new LauncherEditor(panelId, launchermodel);
				}
			});
		}
		
		tabs.add(new AbstractTab(getLabel("editor.audit")) {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<Procedure>(panelId, getModel());
			}
		});
		
		VerticalLayout<ITab> tabbedpanel = new VerticalLayout<ITab>("tabs",this.getClass().getName(), tabs, VerticalLayout.VERTICAL) {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				try {
					int selectedTab = getSelectedTab();
					((KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue(ProcedureMainPanel.class.getName(), "selectedtab", selectedTab);
					String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
					((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
				} 
				catch (Exception e) {
					logger.error(e);
				}
			}
		};
		
		tabbedpanel.setSections(VerticalLayout.COLS_9X3);
		
		tabbedpanel.setTitle(getLabel("sections"));

		List <MenuItemFactory<Procedure>> procedureTemplateMenuItems =  new ArrayList<>();
		for (Procedure procedure : getContentTemplate().getProcedures()) {
			String url = "/model/procedure/"+procedure.getId();
			IModel<String> label = new Model<String>(procedure.getDisplayName());
			procedureTemplateMenuItems.add(id ->
				new HREFMenuItemPanelV5<Procedure>(id, url, label)
			);
		}
		
		procedureTemplateMenuItems.sort(new Comparator<MenuItemFactory<Procedure>>() {
			@Override
			public int compare(MenuItemFactory<Procedure> o1, MenuItemFactory<Procedure> o2) {
				return o1.getItem("item").getLabel().compareToIgnoreCase(o2.getItem("item").getLabel()); 
			}
		});
		
		SimpleMenuPanel2<Procedure> menupanel = new SimpleMenuPanel2<Procedure>("header-bottom-panel", procedureTemplateMenuItems);
		menupanel.setTitle(getLabel("procedures"));
		tabbedpanel.setHeaderBottomPanel(menupanel);
		tabbedpanel.setTitle(getLabel("sections"));
		tabbedpanel.setContentBottomPanel(new InvisiblePanel("content-bottom-panel"));
		
		
		add(new WicketEventListener<EditorEvent>() {
			@Override
			@SuppressWarnings("unchecked")
			public void onEvent(EditorEvent event) {
				VerticalLayout<ITab> tabs = (VerticalLayout<ITab>)get("tabs");
				event.getRequestTarget().add(tabs.getHeaderTabsContainer());
			}
		});
		
		return tabbedpanel;
	}

	private String getContext(ProcessLauncher launcher) {
		String context = "";
		if (launcher.isEnabled())
			context += getLabelString("mytask.context");
		if (launcher.isLibrary()) {
			if (!"".equals(context))
				context += ", ";
			context += getLabelString("library.context");
		}
		if (launcher.isMobile()) {
			if (!"".equals(context))
				context += ", ";
			context += getLabelString("mobile.context");
		}	
		return context;
	}
	
	private List<ProcessLauncher> getLaunchers() {
		return getProcedure().getProcessLaunchers();
	}
	
	private ContentTemplate getContentTemplate() {
		return getProcedure().getContentTemplate();
	}
	
}