package kbee.aerolineas.web.searcher;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Page;
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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.UrlService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Versionable;
import com.novamens.kbee.content.script.KbeeClassificableScriptWrapper;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.ProxyUtil;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.command.panel.CommandAttributePanelV5;
import kbee.web.nav.ContentNavigationBarV6;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.searcher.page.SearcherDetailDocumentPage;

@SuppressWarnings("serial")
public class AAVersionHistoryPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private Boolean is_enabled;
	private List<T> history;
	private boolean isConsole = false;
	private IModel<Site> site_model;
	
	private List<Panel> panels;

	/**
	 * 
	 * @param id
	 * @param model
	 * 
	 */
	public AAVersionHistoryPanel(String id, IModel<T> model, IModel<Site> siteModel, boolean  isConsole) {
		super(id, model);
		this.isConsole=isConsole;
		this.site_model=siteModel;
	}

	public class TitleFragment extends Fragment {
		public TitleFragment(String id, final IModel<T> model) {
			super(id, "title-fragment", AAVersionHistoryPanel.this);
			
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
			Label la=new Label("subtitle", getSubtitle(model));
			la.setEscapeModelStrings(false);
			add(la);;
		};
	};	

	public class UserFragment extends Fragment {
		public UserFragment(String id, final IModel<T> model) {
			super(id, "user-fragment", AAVersionHistoryPanel.this);
			
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

	public boolean isConsole() {
		return this.isConsole;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		DataTable<T, String> table = new DataTable<T, String>("history", getColumns(), new HistoryProvider(), 40);
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (HistoryProvider)table.getDataProvider()));
		WebMarkupContainer container = new WebMarkupContainer("history-container");
		WebMarkupContainer tablecontainer = new WebMarkupContainer("table-container");
		tablecontainer.add(table);
		container.add(tablecontainer);
		container.add(new com.novamens.wicket.markup.html.repeater.util.NavigationToolbar("navigation", table));
		add(container);
		
		List<Panel> _list = getPanels();
		
		add(new ListView<Panel>("result",  _list) {
			protected void populateItem(ListItem<Panel> item){
				item.setOutputMarkupId(true);
				item.add(item.getModelObject());
				item.setVisible(item.getModelObject().isVisible());
			}
		});
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
		
		columns.add(new PropertyColumn<T, String>(getLabel("column.title"), "title") {
			@Override
			public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
				item.add(new TitleFragment(componentId, rowModel));
			}
			@Override
			public String getCssClass()	{
				return "col-lg-8 col-md-8 col-xs-8";
			}
		});
		
		columns.add(new PropertyColumn<T, String>(getLabel("column.user"), "user") { 
			@Override
			public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
				item.add(new UserFragment(componentId, rowModel));
			}
			
			@Override
			public String getCssClass()	{
				return "col-lg-4 col-md-4 col-xs-4";
			}
		});
		
		
		return columns;
	}	
	

	@Override
	public void onDetach() {
		super.onDetach();
		this.history=null;
		if (this.site_model!=null) {
			this.site_model.detach();
		}	
	}
	
	public List<Panel> getPanels() {

		if (this.panels!=null)
			return this.panels;
		
		this.panels = new ArrayList<Panel>();
	 
		IModel<String> kcss = new Model<String>("col-lg-3 col-md-7 col-xs-7 keyc");
		IModel<String> vcss = new Model<String>("col-lg-9 col-md-5 col-xs-5 valuec");
		
		this.panels.add(new CommandAttributePanelV5("command_item", 
			getLabel("version"), 	
			getValue("numero_revision"), 
			kcss, 
			vcss));
	
		this.panels.add(new CommandAttributePanelV5("command_item", 
			getLabel("date"), 	
			getDate("fecha"), 
			kcss, 
			vcss));
	
		this.panels.add(new CommandAttributePanelV5("command_item", 
			getLabel("published-by"),
			new Model<String>(getModelObject().getLastModifiedUser().getFirstLastName() ), 
			kcss, 
			vcss));
	
		this.panels.add(new CommandAttributePanelV5("command_item", 
			getLabel("published-on"),
			format(getModelObject().getCheckinOffsetDateTime()), 
			kcss, 
			vcss));

		this.panels.add(new CommandAttributePanelV5("command_item", 
			getLabel("content-type"), 
			getModelObject().getContentTypeClassificationAsString(), 
			kcss, 
			vcss));

	
		return this.panels;
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
	
	protected boolean isOpenEnabled() {
		if (this.is_enabled==null) {
			this.is_enabled=Boolean.valueOf(isSupportUser() || isReadable(getModel()));
		}
		return this.is_enabled.booleanValue();
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected boolean isReadable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(model.getObject());
	}
	
	protected String getSubtitle(IModel<T> model) {
		return model.getObject().getService(ContentService.class).getConsoleSubtitleDefaultIfNull();
	}
	
	protected String format(OffsetDateTime time) {
		return ServiceLocator.getService(DateTimeService.class).getDateDisplayString(time);
	}
	
	protected String getValue(String classifier) {
		KbeeClassificableScriptWrapper wrapper = new KbeeClassificableScriptWrapper(getModelObject());
		return wrapper.getLabel(classifier);
	}
	
	protected String getDate(String classifier) {
		KbeeClassificableScriptWrapper wrapper = new KbeeClassificableScriptWrapper(getModelObject());
		OffsetDateTime time = wrapper.getDateTime(classifier);
		return time!=null ? format(time) : "";
	}
}