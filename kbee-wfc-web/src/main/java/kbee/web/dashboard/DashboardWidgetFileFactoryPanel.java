package kbee.web.dashboard;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.entity.Person;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemWithModelPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Process;

import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;

@SuppressWarnings("serial")
public class DashboardWidgetFileFactoryPanel extends DashboardWidgetBasePanel {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardWidgetFileFactoryPanel.class.getName());
	
	private List<IModel<ProcessLauncher>> launchers = null;
	private List<LauncherList> launcher_list;

	private WebMarkupContainer help;
	private WebMarkupContainer main_container;
	
	private ProxyLG lgNull = new ProxyLG();
	private IModel<Person> model;
	
	
	public class LauncherList implements IDetachable {

		public IModel<LauncherGroup> lg;
		public List<IModel<ProcessLauncher>> launchers;
		
		public LauncherList(IModel<LauncherGroup> lg, List<IModel<ProcessLauncher>> launchers) {
				this.lg=lg;
				this.launchers=launchers;
		}
		
		public void detach() {
		   
			if (lg!=null)
				lg.detach();
			
			if (launchers!=null)
			   launchers.forEach(item -> item.detach());
		}
		
	} 
	
	public class ProxyLG implements LauncherGroup {

		@Override
		public void setId(Serializable id) {}

		@Override
		public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {	return null;	}

		@Override
		public void setState(ObjectState enabled) {}

		@Override
		public ObjectState getState() {			return null;	}

		@Override
		public Serializable getId() {	return null;	}

		@Override
		public void setDefaultAudit() {	}

		@Override
		public void setLastModifiedUser(User user) {		}

		@Override
		public User getLastModifiedUser() {		return null;		}

		@Override
		public void setCreationOffsetDateTime(OffsetDateTime date) {	}

		@Override
		public OffsetDateTime getCreationOffsetDateTime() {		return null;		}

		@Override
		public void setLastModifiedOffsetDateTime(OffsetDateTime date) {	}

		@Override
		public OffsetDateTime getLastModifiedOffsetDateTime() {		return null;		}

		@Override
		public String getLastModifiedOffsetDateTimeColloquial(String css) {		return null;		}

		@Override
		public String getCreationOffsetDateTimeColloquial() {		return null;	}

		@Override
		public Domain getDomain() {			return null;		}

		@Override
		public void setDomain(Domain domain) {		}
		
		@Override
		public String getAlias() {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String getDisplayName() {
			return "General";
		}

		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		public boolean isVisible() {
			return true;
		}
		
		public boolean equals(LauncherGroup lg) {
			return (lg instanceof ProxyLG);
		}
	}

	public DashboardWidgetFileFactoryPanel(String id, String preferences_key) {
		super(id, preferences_key);
	}
	
	@Override
	protected void onClickCollapse(AjaxRequestTarget target) {
		main_container.setVisible(!main_container.isVisible());
		refresh(target);
	}
	
	@Override
	public IModel<String> getTitle() {
		return getLabel("new-file");
	}

	public void toogleHelp(AjaxRequestTarget target) {
		if (help==null) {
			help=getHelpPanel();
			help.setVisible(false);
			main_container.addOrReplace(help);
		}
		if (help!=null && !(help instanceof InvisiblePanel)) {
			help.setVisible(!help.isVisible());
			main_container.get( "menuitem").setVisible(!main_container.get( "menuitem").isVisible());
			target.add(this.main_container);
		}
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addLaunchers();	 
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (launchers!=null)
			launchers.forEach(item -> item.detach());
		this.lgNull=null;
		if (launcher_list!=null)
			launcher_list.forEach(item -> item.detach());
		if (model!=null)
			model.detach();
	}
	
	protected boolean isEnabled(ProcessLauncher launcher) {
		return launcher.isEnabled() && 
			launcher.executeable() && 
			launcher.getContentTemplate()!=null &&
			launcher.getContentTemplate().getState()==ObjectState.ENABLED;
	}
		
	protected void onStart(Process process) {
	}
	
	protected void onHelp(AjaxRequestTarget target) {
		toogleHelp(target);
	}
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		InlineHelpWebService se=ServiceLocator.getService(InlineHelpWebService.class);
		 WebMarkupContainer  pa = se.getPanel("help", getLocale(), InlineHelpWebService.HOME_FACTORY);
		 if (pa!=null)
			 return pa;
		 return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_FACTORY));
	}
	
	protected Process startProcess(String launcherlabel) {
		for(ProcessLauncher launcher : getDomain().getService(WorkflowDomainService.class).getLaunchers()) {
			if (launcherlabel.equals(launcher.getLabel())) 
				return getDomain().getService(WorkflowDomainService.class).startProcess(launcher);
		}
		return null;
	}
	
	
	@Override
	protected void onTitleClick() {
	}
	
	
	private void addLaunchers() {
		
		setHelp(true);
		
		main_container = new WebMarkupContainer ("file-factory");
		add(main_container);
		
		main_container.add(new InvisiblePanel("help"));
		
		ContextMenuPanel<ProcessLauncher> menu = new ContextMenuPanel<ProcessLauncher>("menuitem", null);
		
		int index=0;
		
		for (LauncherList ll: getLauncherLists()) {
			final String lg_label = ll.lg.getObject().getDisplayName();
			if (lg_label!=null) {
				if (index++>0) {
					menu.addItem(id ->
						new SeparatorMenuItemPanelV5<ProcessLauncher>(id) {
							@Override
							public String getCssClass() {
								return "divider";
							}
							@Override
							public boolean isVisible() {
								return  true;
							}
						}
					);
				}
				menu.addItem(id ->
					new HeaderMenuItemPanelV5<ProcessLauncher>(id) {
						@Override
						public String getLabel() {
							return lg_label;
						}
					}
				);
			}
			
			for (IModel<ProcessLauncher> pl: ll.launchers) {
				menu.addItem(new MenuItemFactory<ProcessLauncher>() {
					@Override
					public AbstractMenuItemPanelV5<ProcessLauncher> getItem(String id) {
						return new MenuItemWithModelPanel<ProcessLauncher>(id,  pl ) {
							@Override
							public void onClick() {
								Process process = startProcess( getModel().getObject().getLabel());
								if (process!=null) {
									onStart(process);
								}
							}
							@Override
							protected boolean isContextualHelp() {
								return true;
							}
							protected Panel getContextualDetailPanel() {
								String l="<span>" + (getModel().getObject().getDescription() != null ? getModel().getObject().getDescription()  : "n/a")+"</span>";
								 return new kbee.web.dashboard.LabelPanel("contextual-help-detail", new Label("label", l));
							}
							@Override
							public String getLabel() {
								return   getModel().getObject().getLabel();
							}
							
							// -------------------------
							// @Override
							// public PopupSettings getPopupSettings() {
							//	 return new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
							//	 	PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
							//	 	PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR);
							// }
							// -------------------------
							
						};
					}
				});
			}
		}
		
		main_container.setOutputMarkupId(true);
		main_container.add(menu);
	}
	
	private List<LauncherList> getLauncherLists() {
		if (this.launcher_list!=null) 
			return this.launcher_list;
		
		this.launcher_list = new ArrayList<LauncherList>();

		Map<LauncherGroup, List<ProcessLauncher>> map = new HashMap<LauncherGroup, List<ProcessLauncher>>();
		
		for(ProcessLauncher launcher: getDomain().getService(WorkflowDomainService.class).getLaunchers()) {
			if (isEnabled(launcher)) {
				LauncherGroup lg = launcher.getLauncherGroup();
				if (lg==null) {
					logger.debug(launcher.getDisplayName() + " " + " lg is null");
					if (!map.containsKey(lgNull)) {
						map.put(lgNull, new ArrayList<ProcessLauncher>());
					}
					map.get(lgNull).add(launcher);
				}
				else { 
					if (!map.containsKey(lg)) {
						map.put(lg, new ArrayList<ProcessLauncher>());
					}
					map.get(lg).add(launcher);
				}
			}
		}
		
		for (Entry<LauncherGroup, List<ProcessLauncher>> entry: map.entrySet()) {
			List<IModel<ProcessLauncher>> l =new ArrayList<IModel<ProcessLauncher>>();
			for (ProcessLauncher p:entry.getValue()) {
				l.add( new ObjectModel<ProcessLauncher>(p));
			}
			this.launcher_list.add(new LauncherList(new ObjectModel<LauncherGroup>(entry.getKey()), l));
		}
		
		this.launcher_list.sort(new Comparator<LauncherList>() {
			@Override
			public int compare(LauncherList o1, LauncherList o2) {
				try {
					if (o1.lg.getObject().getDisplayName()==null)
							return -1;
					if (o2.lg.getObject().getDisplayName()==null)
						return 1;
					return o1.lg.getObject().getDisplayName().compareToIgnoreCase(o2.lg.getObject().getDisplayName());
				} 
				catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		
		return this.launcher_list;
	}
}
