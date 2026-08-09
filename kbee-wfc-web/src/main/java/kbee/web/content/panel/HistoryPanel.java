package kbee.web.content.panel;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Page;
//import org.apache.wicket.datetime.DateConverter;
//import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.jsoup.select.Evaluator.IsEmpty;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.UrlService;
import com.novamens.content.text.TextChange;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Versionable;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.ProxyUtil;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.nav.ContentNavigationBarV6;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.searcher.page.SearcherDetailDocumentPage;

@SuppressWarnings("serial")
public class HistoryPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private Boolean is_enabled;
	private List<T> history;
	private boolean isConsole = false;
	private IModel<Site> site_model;

	public class TitleFragment extends Fragment {
		public TitleFragment(String id, final IModel<T> model) {
			super(id, "title-fragment", HistoryPanel.this);
			
			Link<?> link = new Link<Void>("title-link") {
				public void onClick() {
					setResponsePage(new RedirectPage( model.getObject().getService(UrlService.class).getUrl()));
				}
				public boolean isEnabled() {
					return isOpenEnabled();
				}
			};
			
			add(link);
			Label la=new Label("version", String.valueOf(model.getObject().getVersion()));
			la.setEscapeModelStrings(false);
			link.add(la);
		};
	};	
	
	public class DateFragment extends Fragment {
		public DateFragment(String id, final IModel<T> model) {
			super(id, "date-fragment", HistoryPanel.this);
			
			Link<?> link = new Link<Void>("title-link") {
				public void onClick() {
					setResponsePage(new RedirectPage( model.getObject().getService(UrlService.class).getUrl()));
				}
				public boolean isEnabled() {
					return isOpenEnabled();
				}
			};
			add(link);
			link.add(new Label("title", model.getObject().getTitle()));
			
			WebMarkupContainer vigent=new WebMarkupContainer("vigent");
			boolean is_vigent= model.getObject().isHeadVersion() &&
					model.getObject().getService(ContentService.class).isValidVersion();
			
			vigent.setVisible(is_vigent);
			add(vigent);
			Label la=new Label("subtitle", getSubtitle(model));
			la.setEscapeModelStrings(false);
			add(la);
		};
	};	

	public class UserFragment extends Fragment {
		public UserFragment(String id, final IModel<T> model) {
			super(id, "user-fragment", HistoryPanel.this);
			
			IModel<String> date = new Model<String>() {
				public String getObject() {
					try {
					return ServiceLocator.getService(DateTimeService.class).timeElapsed(model.getObject().getCheckinOffsetDateTime());
					} catch (Exception e) {
						return e.getClass().getName();
					}
				}
			};
			
			Label dat = new Label("date", date);
			dat.setEscapeModelStrings(false);
			add(dat);
			
			IModel<String> username = new Model<String>( model.getObject().getLastModifiedUser().getFirstLastName());
			add(new Label("username", username));
		};
	};
	
	public class TextChangesFragment extends Fragment {
		private List<TextChange> changes = null;
		public TextChangesFragment(String id, final IModel<T> model) {
			super(id, "text-change-fragment", HistoryPanel.this);

			WebMarkupContainer adds = new WebMarkupContainer("adds") {
				public boolean isVisible() {
					return !getAdds().isEmpty();
				}
			};
			
			adds.add(new ListView<TextChange>("adds", getAdds()) {
				public void populateItem(ListItem<TextChange> item) {
					item.add(new Label("label", item.getModelObject().getPart().getTitle()));
				}
			});
			
			add(adds);
			
			WebMarkupContainer updates = new WebMarkupContainer("updates") {
				public boolean isVisible() {
					return !getUpdates().isEmpty();
				}
			};
			
			updates.add(new ListView<TextChange>("updates", getUpdates()) {
				public void populateItem(ListItem<TextChange> item) {
					item.add(new Label("label", item.getModelObject().getPart().getTitle()));
					WebMarkupContainer paragraphs = new WebMarkupContainer("paragraphs-updates") {
						public boolean isVisible() {
							return !item.getModelObject().getNotes().isEmpty();
						}
					};
					paragraphs.add(new ListView<String>("paragraph", item.getModelObject().getNotes()) {
						public void populateItem(ListItem<String> item) {
							String text = item.getModelObject();
							if (text.length()>100) text = text.substring(0,100)+"...";
							item.add(new Label("text", text));
						}
					});	
					item.add(paragraphs);
				}
			});
			
			add(updates);
			
			WebMarkupContainer deletes = new WebMarkupContainer("deletes") {
				public boolean isVisible() {
					return !getDeletes().isEmpty();
				}
			};
			
			deletes.add(new ListView<TextChange>("deletes", getDeletes()) {
				public void populateItem(ListItem<TextChange> item) {
					item.add(new Label("label", item.getModelObject().getPart().getTitle()));
				}
			});
			
			add(deletes);

		}
		@Override
		public boolean isVisible() {
			return getModelObject().getVersion()>1 &&
			getModelObject().getService(ContentService.class).getText()!=null &&
			!getChanges().isEmpty();
		}
		public List<TextChange> getChanges() {
			if (changes==null) {
				changes = getModelObject().getService(ContentService.class).getTextChanges();
				if (changes==null) changes = new ArrayList<>();
			}
			return changes;
		}
		public List<TextChange> getAdds() {
			List<TextChange> adds = new ArrayList<>();
			for (TextChange change : getChanges()) {
				if (change.getType() == TextChange.ADD) 
					adds.add(change);
			}
			return adds;
		}
		public List<TextChange> getUpdates() {
			List<TextChange> updates = new ArrayList<>();
			for (TextChange change : getChanges()) {
				if (change.getType() == TextChange.UPDATE) 
					updates.add(change);
			}
			return updates;
		}
		public List<TextChange> getDeletes() {
			List<TextChange> deletes = new ArrayList<>();
			for (TextChange change : getChanges()) {
				if (change.getType() == TextChange.DELETE) 
					deletes.add(change);
			}
			return deletes;
		}
	}	


	
	public class HistoryProvider extends SortableDataProvider<T, String> {
		public Iterator<T> iterator(long first, long count) {
			ArrayList<T> iteration = new ArrayList<T>();
			Iterator<T> iterator = getHistory().listIterator((int)first);
			int i = 0;
			while (i++<count) {
				iteration.add(iterator.next());
			}
			return iteration.iterator();
		}	
		public IModel<T> model(T object) {
			return new ObjectModel<T>(object);
		}
		public long size() {
			return getHistory().size();
		}
	}


	/**
	 * 
	 * @param id
	 * @param model
	 * 
	 */
	public HistoryPanel(String id, IModel<T> model, IModel<Site> siteModel, boolean  isConsole) {
		super(id, model);
		this.isConsole=isConsole;
		this.site_model=siteModel;
	}

	public boolean isConsole() {
		return this.isConsole;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new TextChangesFragment("text-changes", getModel()));
		
		DataTable<T, String> table = new DataTable<T, String>("history", getColumns(), new HistoryProvider(), 40);
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (HistoryProvider)table.getDataProvider()));
		WebMarkupContainer container = new WebMarkupContainer("history-container");
		WebMarkupContainer tablecontainer = new WebMarkupContainer("table-container");
		tablecontainer.add(table);
		container.add(tablecontainer);
		container.add(new com.novamens.wicket.markup.html.repeater.util.NavigationToolbar("navigation", table));
		add(container);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<T> getHistory() {
		
		if (history!=null)
			return history;
		
		this.history = new ArrayList<T>();
				 
		Versionable<T> versionable = (Versionable<T>)getModel().getObject();
		T version = versionable.getPreviousVersion();
		while (version!=null) {
			this.history.add(version);
			versionable = (Versionable<T>)version;
			version = versionable.getPreviousVersion();
		}
		return this.history;
	}
	
	public List<IColumn<T, String>> getColumns() {
		List<IColumn<T, String>> columns = new ArrayList<IColumn<T, String>>();
		
				
		columns.add(new PropertyColumn<T, String>(getLabelModel("column.date"), "lastModifiedDate") {
			@Override
			public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
				item.add(new DateFragment(componentId, rowModel));
			}
			@Override
			public String getCssClass()	{
				return "col-lg-7 col-md-7 col-xs-7";
			}
		});

		
		columns.add(new PropertyColumn<T, String>(getLabelModel("column.user"), "user") { 
			@Override
			public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
				item.add(new UserFragment(componentId, rowModel));
			}
			
			@Override
			public String getCssClass()	{
				return "col-lg-3 col-md-3 col-xs-3";
			}
		});
		
		columns.add(new PropertyColumn<T, String>(getLabelModel("column.version"), "version") { 
			@Override
			public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
				item.add(new TitleFragment(componentId, rowModel));
			}
			
			@Override
			public String getCssClass()	{
				return "col-lg-2 col-md-2 col-xs-2 text-center";
			}
		});

		
		return columns;
	}	
	

	@Override
	public void onDetach() {
		super.onDetach();
		this.history=null;
		if (this.site_model!=null)
			this.site_model.detach();
	}

	/**
	 * 
	 * Spring: 
	 * 
	 * {@link AclPage}
	 * {@link TextPage}
	 * 
	 */
	protected Page getPage(IModel<T> model) {
		Page page;
		@SuppressWarnings("unchecked")
		T content = (T)getContentDao().reload(model.getObject());
		model = new ObjectModel<T>(content);

		if (isConsole()) {
			page = (Page)ServiceLocator.getService(BeansService.class).getBean(getContentClass(model.getObject()) + "-page", model);
			((AbstractApplicationPage<?>)page).setTopNavigation(new ContentNavigationBarV6<T>(model));
			
		}
		else {
			page = new SearcherDetailDocumentPage<T>(model, getSiteModel());
		}
		
		model.detach();
		
		return page;
	}

	protected IModel<Site> getSiteModel() {
		return this.site_model;
	}
	
	protected Page getContentPage(IModel<Content> model, int index, final boolean openandedit) {
		return null;
	}

	protected String getContentClass(T content) {
		return ProxyUtil.getClassName(content).toLowerCase();
	}
	
	protected IModel<String> getLabelModel(String resourceKey) {
		return new StringResourceModel(resourceKey, this, null);
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected boolean isOpenEnabled() {
		if (this.is_enabled==null) {
			this.is_enabled=Boolean.valueOf(isSupportUser() || isReadable(getModel()));
		}
		return this.is_enabled.booleanValue();
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected boolean isWriteable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(model.getObject());
	}
	
	protected boolean isReadable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(model.getObject());
	}
	
	protected String getSubtitle(IModel<T> model) {
		return model.getObject().getService(ContentService.class).getConsoleSubtitleDefaultIfNull();
	}
}