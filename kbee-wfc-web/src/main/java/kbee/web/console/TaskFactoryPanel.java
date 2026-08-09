package kbee.web.console;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Modal.Button;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Process;

import kbee.web.content.template.BrowserModal;
import kbee.web.content.template.BrowserModal.CreateButton;
import kbee.web.content.template.BrowserPanel;
import kbee.web.object.AuditTrailModal;

@SuppressWarnings("serial")
public class TaskFactoryPanel extends KBPanel {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskFactoryPanel.class.getName());
	
	private List<ProcessLauncher> launchers = null;
	private List<LauncherList> launcher_list;

	private ProxyLG lgNull = new ProxyLG();
	
	public class LauncherList {
		public LauncherGroup lg;
		public List<ProcessLauncher> launchers;
		public LauncherList(LauncherGroup lg, List<ProcessLauncher> launchers) {
			this.lg=lg;
			this.launchers=launchers;
		}
	}
	
	public TaskFactoryPanel() {
		this("new-task");
	}
	
	public TaskFactoryPanel(String id) {
		super(id);
		
		setOutputMarkupId(true);
		
		ProcessLauncher la=null;
		for(ProcessLauncher launcher : getLaunchers()) {			
			if (launcher.isEnabled() &&  launcher.executeable() && launcher.getContentTemplate().getState()==ObjectState.ENABLED) {
				la=launcher;
				break;
			}
		}
		final String nlabel = (la!=null?la.getLabel():"err");
		Link<Void> news = new Link<Void>("new-single-button") {
			@Override
			public void onClick() {
				if (nlabel!=null && !nlabel.equals("err")) {
					Process process = startProcess(getLaunchers().get(0));
					if (process!=null)
						onStart(process);
				}
			}
		};
		news.add(new AttributeModifier("target", "_blank"));
		news.setVisible(getLaunchers().size()==1 && la!=null);
		add(news);
		Label newlabel= new Label("new", new Model<String>() {
			public String getObject() {
				StringResourceModel model = new StringResourceModel("new", TaskFactoryPanel.this);
				model.setParameters(nlabel);
				return model.getObject();
			}
		});
		news.add(newlabel);
		WebMarkupContainer  newm = new WebMarkupContainer ("new-multiple-button");
		newm.setVisible(getLaunchers().size()>1);
		newm.add(new AttributeModifier("class", "btn-md btn btn-primary dropdown-toggle"));
		newm.add(new AttributeModifier("data-toggle", "dropdown"));
		add(newm);
			
		addLaunchers();
		
		add(new BrowserModal<Content>("browser-modal"));
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
			return null;
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

	 
			
	
	private List<LauncherList> getLauncherLists() {
		
		if (this.launcher_list!=null) 
			return this.launcher_list;
		
		this.launcher_list = new ArrayList<LauncherList>();
		Map<LauncherGroup, List<ProcessLauncher>> map = new HashMap<LauncherGroup, List<ProcessLauncher>>();
		
		for(ProcessLauncher launcher: getDomain().getService(WorkflowDomainService.class).getLaunchers()) {
			if (launcher.isEnabled() && launcher.executeable() && launcher.getContentTemplate()!=null && launcher.getContentTemplate().getState()==ObjectState.ENABLED) {
			
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
						map.put(lg, new ArrayList<>());
					}
					map.get(lg).add(launcher);
				}
			}
		}
		
		for (Entry<LauncherGroup, List<ProcessLauncher>> entry: map.entrySet()) {
			this.launcher_list.add(new LauncherList(entry.getKey(), entry.getValue()));
		}
		
		this.launcher_list.sort(new Comparator<LauncherList>() {

			@Override
			public int compare(LauncherList o1, LauncherList o2) {
				
				try {
					if (o1.lg.getDisplayName()==null)
							return -1;
					if (o2.lg.getDisplayName()==null)
						return 1;
					return o1.lg.getDisplayName().compareToIgnoreCase(o2.lg.getDisplayName());
					} catch (Exception e) {
						logger.error(e);
					return 0;
				}
				
			}
			
		});
		
		return this.launcher_list;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.launchers = null;
		this.launcher_list = null;
		this.lgNull=null;
	}
	
	protected void onStart(Process process) {
	}
	
	protected List<ProcessLauncher> getLaunchers() {
		if (this.launchers!=null) 
			return this.launchers;
		this.launchers = new ArrayList<ProcessLauncher>();
		for(ProcessLauncher launcher: getDomain().getService(WorkflowDomainService.class).getLaunchers()) {
			if (launcher.isEnabled() && launcher.executeable() && launcher.getContentTemplate()!=null && launcher.getContentTemplate().getState()==ObjectState.ENABLED) {
				this.launchers.add(launcher);
			}
		}
		return this.launchers;
	}
	
	protected List<ProcessLauncher> getLaunchersLists() {
		if (this.launchers!=null) 
			return this.launchers;
		this.launchers = new ArrayList<ProcessLauncher>();
		for(ProcessLauncher launcher: getDomain().getService(WorkflowDomainService.class).getLaunchers()) {
			if (launcher.isEnabled() && launcher.executeable() && launcher.getContentTemplate().getState()==ObjectState.ENABLED) {
				this.launchers.add(launcher);
			}
		}
		return this.launchers;
	}
	
	private Process startProcess(ProcessLauncher launcher) {
		return getDomain().getService(WorkflowDomainService.class).startProcess(launcher);
	}
	
	private Process startProcess(ProcessLauncher launcher, Content template) {
		return getDomain().getService(WorkflowDomainService.class).startProcess(launcher, template);
	}

	
	private void addLaunchers() {
		
		long start=System.currentTimeMillis();
		
		ContextMenuPanel<ProcessLauncher> menu = new ContextMenuPanel<ProcessLauncher>(null);
		
		int index=0;
		for (LauncherList ll: getLauncherLists()) {
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
			
			final String lg_label = ll.lg.getDisplayName();
			
			if (lg_label!=null) {
				menu.addItem(id ->
					new HeaderMenuItemPanelV5<ProcessLauncher>(id) {
						@Override
						public String getLabel() {
							return lg_label;
						}
								
					}
				);
			}
			
			for (ProcessLauncher launcher : ll.launchers) {
				final String la_label = launcher.getLabel();
				IModel<ProcessLauncher> launchermodel = new ObjectModel<ProcessLauncher>(launcher);
				if (launchermodel.getObject().useTemplate()) {
					menu.addItem(id ->
						new AjaxMenuItemPanelV5<ProcessLauncher>(id) {
							@Override
							public void onClick(AjaxRequestTarget target) {
								Modal modal = getBrowserModal();
								((BrowserModal<Content>)modal).open(target, new Modal.Handler() {
									@Override
									public void onClick(AjaxRequestTarget target, Button button) {
										IModel<Content> model = ((BrowserPanel<Content>)getBrowserModal().getBody()).getContentModel();
										if (model!=null && button!=null && button instanceof CreateButton) {
											Process process = startProcess(launchermodel.getObject(), model.getObject());
											if (process!=null) {
												onStart(process);
											}
										}
										else {
											setResponsePage(new RedirectPage("/mytasks"));
											//target.add(TaskFactoryPanel.this);
										}
									}
								});
							}
							@Override
							public String getLabel() {
								return la_label;
							}
						}
					);
				}
				else {
					menu.addItem(id ->
						new MenuItemPanelV5<ProcessLauncher>(id) {
							@Override
							public void onClick() {
								Process process = startProcess(launchermodel.getObject());
								if (process!=null) {
									onStart(process);
								}
							}
							@Override
							public String getLabel() {
								return la_label;
							}
							@Override
							public PopupSettings getPopupSettings() {
								return new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
									PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
									PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR);
							}
						}
					);
				}
			}
		}
		
		add(menu);
		long end=System.currentTimeMillis();
		logger.debug("TaskFactory -> " + String.valueOf(end-start)+ " ms");
	}
	
	protected Modal getBrowserModal() {
		return (Modal) get("browser-modal");
	}
}
