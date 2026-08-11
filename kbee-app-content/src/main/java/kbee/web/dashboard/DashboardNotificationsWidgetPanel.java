package kbee.web.dashboard;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import com.novamens.content.notification.Notification;
import com.novamens.content.notification.NotificationType;
import com.novamens.content.service.DomService;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.notification.KbeeNotification;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.query.NotificationsQuery;

@SuppressWarnings({ "serial", "deprecation" })
public class DashboardNotificationsWidgetPanel extends DashboardWidgetBasePanel implements PortalViewRender {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardProgressNotesWidgetPanel.class.getName());
	
	static final int PAGE_SIZE = 25;

	static final int MAX = 280;
	
	private List<IModel<Notification>> notifications;
	private WebMarkupContainer help;
	private int total, page=0;
	private WebMarkupContainer main_container;
	private WebMarkupContainer list_container;
	
	private NotificationType filteredType = null;
	private boolean unreads = false;
	
	
	static final String BTN = "btn-mini";
	
	public class FiltersPanel extends Fragment {
		
		AjaxLink<Void> b_all;
		AjaxLink<Void> b_workflow;
		AjaxLink<Void> b_publications;
		AjaxLink<Void> b_unread;
		
		
		public FiltersPanel() {
			
			super("filters", "filters-fragment", DashboardNotificationsWidgetPanel.this);
			setOutputMarkupId(true);
			
			b_all=new AjaxLink<Void>("all") {
				public void onClick(AjaxRequestTarget target) {
					filteredType = null;
					setUnreads(false);
					setPageNumber(0);
					refresh(target);
				}
			};
			
			add(b_all);
			
			b_all.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return !isUnreads() && getFilteredType()==null ? ("selected " +BTN) : BTN;
				}
			}));
			
		
			
			b_workflow = new AjaxLink<Void>("workflow") {
				public void onClick(AjaxRequestTarget target) {
					filteredType = NotificationType.PROGRESS_NOTE.equals(getFilteredType())  ? null : NotificationType.PROGRESS_NOTE;
					setPageNumber(0);
					refresh(target);
				}
			};
			add(b_workflow);
			
			b_workflow.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return NotificationType.PROGRESS_NOTE.equals(getFilteredType())  ? ("selected " +BTN) : BTN;
				}
			}));
			
			
			
			b_publications=new AjaxLink<Void>("publications") {
				public void onClick(AjaxRequestTarget target) {
					filteredType = NotificationType.CONTENT.equals(getFilteredType())  ? null : NotificationType.CONTENT;
					setPageNumber(0);
					refresh(target);
				}
			};
			add(b_publications);
			
			b_publications.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return NotificationType.CONTENT.equals(getFilteredType()) ? ("selected " +BTN) : BTN;
				}
			}));
			
			b_unread=new AjaxLink<Void>("unreads") {
				public void onClick(AjaxRequestTarget target) {
					setUnreads(!isUnreads());
					setPageNumber(0);
					refresh(target);
				}
			};
			add(b_unread);
			
			b_unread.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return isUnreads() ? ("selected " +BTN) : BTN;
				}
			}));
		}
	}

	
	public class NavigationPanel extends Fragment {
		
		public NavigationPanel() {
			super("navigation", "navigation-fragment", DashboardNotificationsWidgetPanel.this);
			
			add(new AjaxLink<Void>("previous-page-link") {
				public void onClick(AjaxRequestTarget target) {
					setPageNumber(getPageNumber()-1);
					refresh(target);
				}
				public boolean isEnabled() {
					return getPageNumber()>0;
				}
			});
			
			add(new AjaxLink<Void>("next-page-link") {
				public void onClick(AjaxRequestTarget target) {
					setPageNumber(getPageNumber()+1);
					refresh(target);
				}
				public boolean isEnabled() {
					return (getPageNumber()+1)*PAGE_SIZE < getTotal();
				}
			});
			
			add(new Label("total", ()->getTotal()));
		}
		@Override
		public void onInitialize() {
			super.onInitialize();
		}	
	}	
	
	public DashboardNotificationsWidgetPanel(String id) {
		this(id, "mynotifications");
	}
	
	public DashboardNotificationsWidgetPanel(String id, String preferences_key) {
		super(id, preferences_key);						
		super.setHelp(true);
		setTitle(getLabel("notifications"));
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		
		main_container = new WebMarkupContainer ("main-container");
		main_container.setOutputMarkupId(true);
		addOrReplace(main_container);

		main_container.add(new FiltersPanel());
		
		addNotificationsView();
		
		main_container.add(new NavigationPanel());
		
		main_container.setVisible(!isCollapsed());
		main_container.add(new InvisiblePanel("help"));
	}
	
	
	public NotificationType getFilteredType() {
		return filteredType;
	}

	public void setFilteredType(NotificationType filteredType) {
		this.filteredType = filteredType;
	}
	

	public boolean isUnreads() {
		return unreads;
	}

	public void setUnreads(boolean unreads) {
		this.unreads = unreads;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
	
	public int getPageNumber() {
		return page;
	}

	public void setPageNumber(int page) {
		this.page = page;
	}

	public void onDetach() {
		super.onDetach();
		if (notifications!=null)
			notifications.forEach(item -> item.detach());
	}
	
	@Override
	protected void refresh(AjaxRequestTarget target) {
		super.refresh(target);
		notifications = null;
		target.add(this);
	}

	protected boolean isCollapsable() {
		return true;
	}
	
	@Override
	protected void onClickCollapse(AjaxRequestTarget target) {
		main_container.setVisible(!main_container.isVisible());
		refresh(target);
	}

	protected void onHelp(AjaxRequestTarget target) {
		toogleHelp(target);
	}
	
	protected List<IModel<Notification>> getLastNotes() {
		
		if ( notifications!=null)
			return notifications;
		
		int index = 0;
		notifications = new ArrayList<IModel<Notification>>();
		
		NotificationsQuery query = new NotificationsQuery(getSessionUser());
		query.setType(getFilteredType());
		query.setUnreads(isUnreads());
		ResultSet notificationsset = query.execute();
		
		notificationsset.absolute(page*PAGE_SIZE+1);
		
		if (notificationsset!=null) {
			while (notificationsset.hasNext() && index++<PAGE_SIZE) {
				notifications.add(new ObjectModel<Notification>((Notification)notificationsset.next().getObject()));
			}
			total = notificationsset.size();
		}
		
		return notifications;
	}
	
	@Override
	protected void onTitleClick() {
		
	}

	
	protected void addNotificationsView() {
		
		list_container = new WebMarkupContainer ("list-container");
		list_container.setOutputMarkupId(true);
		main_container.addOrReplace(list_container);
		
		list_container.add(new ListView<IModel<Notification>>("notification", ()->getLastNotes()) {
			public void populateItem(ListItem<IModel<Notification>> item) {
				try {
					Notification notification = item.getModelObject().getObject();
					
					item.add(new WebMarkupContainer("unread") {
						public boolean isVisible() {
							return item.getModelObject().getObject().getDateRead()==null;
						}
					});
					
					WebMarkupContainer menu = new WebMarkupContainer("menu-container");
					//menu.setOutputMarkupId(true);
					menu.add(getMenu(item.getModelObject()));
					item.add(menu);
					
					// WebMarkupContainer icon = new WebMarkupContainer("glyphicon");
					// icon.add(new AttributeModifier("class", notification.getIcon()));
					// item.add(icon);

					item.add(new Label("type", notification.getTypeStr()));
					
					Link<?> link = new Link<Void>("link") {
						public void onClick() {
							markAsRead(item.getModelObject().getObject());
							String url = item.getModelObject().getObject().getUrl();
							if (url!=null) {
								setResponsePage(new RedirectPage(url));
							}
						}
					};
					
					//link.setPopupSettings(new PopupSettings(  PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
					//	PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
					//	PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR));
					//
					
					link.add(new Label("title", notification.getTitle()));
					Label textlabel = new Label("text",   getSnippet( notification.getText() ));
					textlabel.setEscapeModelStrings(false);

					item.add(textlabel);
					item.add(link);
					item.add(new Label("user", notification.getSender().getFirstLastName()));
					Label datelabel = new Label("date", ServiceLocator.getService(DateTimeService.class).timeElapsed(notification.getCreationOffsetDateTime()));
					datelabel.setEscapeModelStrings(false);
					item.add(datelabel);
				} 
				catch (Exception e) {
					item.setVisible(false);
					logger.error(e);
				}
			}
		});
	}
	
	protected Panel getMenu(IModel<Notification> model) {
		
		ContextMenuPanel<Notification> menu = new ContextMenuPanel<Notification>(model);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Notification>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					markAsRead(getModelObject());
					refresh(target);
				}	
				@Override
				public boolean isEnabled() {
					return true;
				}
				@Override
				public String getLabel() {	
					return getLabelString("menu.markasread");
				}
		});
	
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Notification>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					delete(getModelObject());
					refresh(target);
				}	
				@Override
				public boolean isEnabled() {
					return true;
				}
				@Override
				public String getLabel() {	
					return getLabelString("menu.delete");
				}
		});
		
		return menu;
	}
	
	protected void markAsRead(Notification notification) {
		((KbeeNotification)notification).setDateRead(OffsetDateTime.now());
		((KbeeNotification)notification).getService(DomService.class).update();
	}
	
	protected void delete(Notification notification) {
		((KbeeNotification)notification).getService(DomService.class).delete();
	}
	
	protected IModel<String> getSnippet(String text) {
		if (text==null || text.isEmpty())
			return new Model<String>();
		String s = null;
		if (text.length()>MAX)
			s = text.substring(0, MAX)+"...";
		else
			s=text;
		Safelist list = Safelist.basic();
		list.removeTags("p");
		String cleaned = Jsoup.clean(s, list);
		String t1 = cleaned;
		return  new Model<String>(t1);
	}
	
	
	protected void toogleHelp(AjaxRequestTarget target) {
		if (help==null) {
			help=getHelpPanel();
			help.setVisible(false);
			main_container.addOrReplace(help);
		}
		if (help!=null && !(help instanceof InvisiblePanel)) {
			help.setVisible(!help.isVisible());
			list_container.setVisible(!list_container.isVisible());
			target.add(this);
		}
	}

	public boolean isMenu() {
		return false;
	}
	
}