package kbee.web.portal6.directory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.ObjectState;

import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.portal6.panel.PortalPanel;

import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal.favorites.SiteFavorites;
import com.novamens.portal.service.PortalUrlService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteType;


public class DirectoryListPanel extends PortalPanel<Person> {

	private static final long serialVersionUID = 1L;
																						
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DirectoryListPanel.class.getName());

	List<IModel<Site>> sites = null;
	List<IModel<Site>> favorites = null;

	private String filterStr;

	int FAVORITES = 0;
	int SITES = 1;
	int UTILITIES = 2;
	int ALL = 3;

	private int tag = FAVORITES;

	int status = 1;
	boolean dirty = true;

	
	@Override
	public void onDetach() {
		super.onDetach();

		if (sites != null)
			for (IModel<Site> m : sites)
				m.detach();

		if (favorites != null)
			for (IModel<Site> m : favorites)
				m.detach();
		
		if (st!=null)
			for (IModel<Site> m : st)
				m.detach();
	}


	
	public DirectoryListPanel(String id, IModel<Person> model) {
		super(id);
		setModel(model);

		String selected=getUserPreference("site-favorites", "favorites");
		
		if (selected.equals("favorites"))	 	setSelectedTag(FAVORITES);
		else if (selected.equals("sites"))	 	setSelectedTag(SITES);
		else if (selected.equals("utilities")) 	setSelectedTag(UTILITIES);
		else if (selected.equals("all")) 		setSelectedTag(ALL);
		
		addMarkup();
		
		setOutputMarkupId(true);
	}

	@Override
	public void addListeners() {
		super.addListeners();

		add(new WicketEventListener<TagSelectionEvent>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(TagSelectionEvent event) {
				if (event.getTag().equals("favorites"))		 setSelectedTag(FAVORITES);
				else if (event.getTag().equals("sites"))	 setSelectedTag(SITES);
				else if (event.getTag().equals("utilities")) setSelectedTag(UTILITIES);
				else if (event.getTag().equals("all")) 		 setSelectedTag(ALL);
				if (status==0) 
					addMarkup();
				event.getRequestTarget().add(DirectoryListPanel.this);
			}
		});
	}
	

	
	private void addMarkup() {

		status=1;
		
		final WebMarkupContainer results_container = new WebMarkupContainer("results");
		results_container.setOutputMarkupId(true);
		addOrReplace(results_container);

		WebMarkupContainer filter_contaniner = new WebMarkupContainer("filter-container");
		addOrReplace(filter_contaniner);

		final TextField<String> filter = new TextField<String>("filter-input");
		filter.add(new AjaxFormComponentUpdatingBehavior("keyup") {
			private static final long serialVersionUID = 1L;

			public void onUpdate(AjaxRequestTarget target) {
				setFilterStr(filter.getInput());
				target.add(results_container);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				ThrottlingSettings settings = new ThrottlingSettings("fid", Duration.ofMillis(500));
				attributes.setThrottlingSettings(settings);
			}
		});
		filter.setModel(new Model<String>() {
			private static final long serialVersionUID = 1L;

			public String getObject() {
				return getFilterStr();
			}

			public void setObject(String value) {
				setFilterStr(value);
			}
		});

		filter_contaniner.add(filter);

		ListModel<IModel<Site>> lm = new ListModel<IModel<Site>>(new Model<Panel>(this), "filteredSites");

		ListView<IModel<Site>> results = new ListView<IModel<Site>>("result", lm) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<IModel<Site>> item) {
				IModel<Site> site_model = item.getModel().getObject();
				WebMarkupContainer link = new WebMarkupContainer("link");
				Label ti = new Label("title", site_model.getObject().getTitle());
				link.add(ti);
				link.add(new AttributeModifier("href", 				getServerUrl() + "/"+ ServiceLocator.getService(PortalUrlService.class).getRelativeSiteUrl(site_model.getObject())));
				link.add(new AttributeModifier("target", "_blank"));
				item.add(link);
				if (item.getIndex() == 0)
					item.add(new AttributeModifier("class", "result first"));
				else
					item.add(new AttributeModifier("class", "result"));
			}
		};

		results_container.add(results);
	}

	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	
		if (status==0) {
			WebMarkupContainer res= new WebMarkupContainer("results");
			res.setVisible(false);
			add(res);
			
			WebMarkupContainer fc= new WebMarkupContainer("filter-container");
			fc.setVisible(false);
			add(fc);
		}
		else
			addMarkup();
			
	}
	

	public String getFilterStr() {
		return filterStr;
	}
	
	public void setFilterStr(String filterStr) {
		this.filterStr = filterStr;
		dirty =true;
	}

	public void setSelectedTag(int tag) {
		this.tag = tag;
		dirty=true;
	}

	public int getSelectedTag() {
		return this.tag;
	}

	List<IModel<Site>> st;
	
	public List<IModel<Site>> getFilteredSites() {

		if(st!=null && !dirty)
			return st;

		st = new ArrayList<IModel<Site>>();
		String prefix = getFilterStr() != null ? getFilterStr().trim().toLowerCase() : null;

		List<IModel<Site>> ca;

		if (getSelectedTag() == FAVORITES)
			ca = getFavorites();
		else
			ca = getSites();

		for (IModel<Site> member : ca) {
		 

			 
			if (getSelectedTag() == ALL || getSelectedTag() == FAVORITES) {
				if (prefix == null || member.getObject().getTitle().toLowerCase().contains(prefix)) 
					st.add(member);
			}
			
			//} else if (member.getObject().getSiteType() == SiteType.APPLICATION && getSelectedTag() == UTILITIES) {

			//	if (prefix == null || member.getObject().getTitle().toLowerCase().contains(prefix))
			//		st.add(member);

			//} else if (getSelectedTag() == SITES && member.getObject().getSiteType() != SiteType.APPLICATION) {

				//if (prefix == null || member.getObject().getTitle().toLowerCase().contains(prefix))
				//	st.add(member);
			//}
			 
		}

		dirty = false;
		
		return st;
	}

	protected PortalDao getPortalDao() {
		return (PortalDao) ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}

	/**
	 * 
	 * @return
	 */
	private List<IModel<Site>> getFavorites() {

		if (this.favorites != null)
			return this.favorites;

		this.favorites = new ArrayList<IModel<Site>>();
		try {
			
			SiteFavorites sf = getPortalDao().getSiteFavorites(getUser());
				
			if (sf==null || sf.getFavorites() == null)
				return this.favorites;
			
			for (Site s : sf.getFavorites()) {
				this.favorites.add(new ObjectModel<Site>(s));
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return this.favorites;
	}

	private User getUser() {
		return getModel().getObject().getProfile(UserProfile.class).getUser();
	}

	private List<IModel<Site>> getSites() {

		if (this.sites != null)
			return this.sites;
		
		this.sites = new ArrayList<IModel<Site>>();
		for (Site s : getPortalDao().getSites(getDomain(), ObjectState.ENABLED))
			this.sites.add(new ObjectModel<Site>(s));
		return this.sites;
	}
}
