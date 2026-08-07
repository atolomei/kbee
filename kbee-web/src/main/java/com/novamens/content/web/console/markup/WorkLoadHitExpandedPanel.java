package com.novamens.content.web.console.markup;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.novamens.content.base.Resource;
import com.novamens.content.entity.Person;
import com.novamens.content.service.workflow.UserWorkLoadData;
import com.novamens.content.service.workflow.WorkflowLoadService;
import com.novamens.content.user.UserProfile;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.HitExpandedPanel;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.query.MonitorQuery;
import kbee.web.resource.ResourceThumbnailImage;
import kbee.web.service.ApplicationSiteMapService;


public class WorkLoadHitExpandedPanel extends Panel implements HitExpandedPanel {
			
	@SuppressWarnings("unused")
	static private Logger logger = LogManager.getLogger(WorkLoadHitExpandedPanel.class.getName());
	
	static PackageResourceReference MENU_ICON = new PackageResourceReference(AbstractKbeeWebPage.class, "menu-red.png");
	
	private static final long serialVersionUID = 1L;

	private IModel<Person> model;

	private List<Entry<String, Integer>> list = null;

	
	public WorkLoadHitExpandedPanel(String id, IModel<Person> model) {
		super(id);
		setModel(model);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		addGeneralInfo();
		addDueDates();
		addTaskTypes();
	}
	
	@Override
	public void onDetach() {
		getModel().detach();
		list=null;
		super.onDetach();
	}
	

	public List<Entry<String, Integer>> getTaskTypes() {
		if (list!=null)
			return list;
		list = new ArrayList<Entry<String, Integer>>();
		WorkflowLoadService service = (WorkflowLoadService)  WorkLoadHitExpandedPanel.this.getModel().getObject().getDomain().getService(WorkflowLoadService.class);
		User user = WorkLoadHitExpandedPanel.this.getModel().getObject().getProfile(UserProfile.class).getUser();
		Map<String, Integer> mp = service.getTaskTypesWorkLoad(user);
		for (Entry<String, Integer> entry: mp.entrySet()) 
			list.add(entry);
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> a, Entry<String, Integer> b) {
				try {
					return a.getKey().compareToIgnoreCase(b.getKey());
				} catch (Exception e) {
					return 0;
				}
			}
		}); 
		return list;
	}

	
	protected void setModel(IModel<Person> model) {
		this.model=model;
	}

	
	protected IModel<Person> getModel() {
		return model;
	}

	protected Index getQueryIndex() {
		return getModel().getObject().getProfile(UserProfile.class).getDomain().getService(JavaIndexerService.class).getIndex();
	}


	private void addGeneralInfo() {
		
		Person person = getModel().getObject();
		
		if (person.getPhoto()!=null) {
			Image image = new ResourceThumbnailImage("photo", new ObjectModel<Resource>((Resource) person.getPhoto()), ThumbnailSize.MINI);
			add(image);
		}
		else {
			add(ServiceLocator.getService(BrandingWebService.class).getUserAvatarPhoto("photo", person));
		}
		
		Link<Void> link = new Link<Void>("name-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				MonitorQuery query = new MonitorQuery( getQueryIndex());
				Serializable id = WorkLoadHitExpandedPanel.this.getModel().getObject().getProfile(UserProfile.class).getUser().getId();
				query.getParameters().put("workspace", id.toString());
				setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.MonitorPage, query));
			}
		};
		
		add(link);
		link.add(new Label("name", person.getFirstLastName()));
		add(new Label("username", (person.getBusinessTitle()!=null) ? (person.getBusinessTitle()):(person.getProfile(UserProfile.class).getUser().getUserName())));
		add(new Label("email", person.getEmail()));
	}


	private void addTaskTypes() {
		
		WebMarkupContainer wlist = new WebMarkupContainer("task-types") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return true;
			}
		};
			
		add(wlist);
		
		ListModel<Entry<String, Integer>> lm = new ListModel<Entry<String, Integer>>(new Model<Panel>(WorkLoadHitExpandedPanel.this), "taskTypes");
		wlist.add(new ListView<Entry<String, Integer>>("task-types-list",  lm) {
			private static final long serialVersionUID = 1L;
				@Override
				protected void populateItem(ListItem<Entry<String, Integer>> item) {
					item.add(new Label("label",  item.getModelObject().getKey()));
					item.add(new Label("value",  item.getModelObject().getValue()));
				}
			});
	}

	
	
	private void addDueDates() {
		
		DateTimeService d_service = ServiceLocator.getService(DateTimeService.class);
		
		Locale locale = getSessionUser().getLocale();
		
		String zid = d_service.getMapZoneIds().get(getSessionUser().getTimeZone());

		if (zid==null) 
				zid=ZoneId.systemDefault().getId();
		
		String today_str =  new StringResourceModel("today", this, null).getString(); // "Today"; //d_service.format( OffsetDateTime.now(), zid, locale, DateTimeService.Dow_Month_Day);
		String today1_str= d_service.format( OffsetDateTime.now().plusDays(1), zid, locale, DateTimeService.Dow_Month_Day_year);
		String today2_str= d_service.format( OffsetDateTime.now().plusDays(2), zid, locale, DateTimeService.Dow_Month_Day_year);
		String today3_str= d_service.format( OffsetDateTime.now().plusDays(3), zid, locale, DateTimeService.Dow_Month_Day_year);
		String today4or_more_str= d_service.format( OffsetDateTime.now().plusDays(4), zid, locale, DateTimeService.Dow_Month_Day_year) + " or later";
		String duenone =  new StringResourceModel("due-none", this, null).getString(); // "Due None";
		String pastdue_str = new StringResourceModel("past-due", this, null).getString(); // "Past None";
		
		Domain domain = getModel().getObject().getProfile(UserProfile.class).getDomain();
		
		WorkflowLoadService service = (WorkflowLoadService) domain.getService(WorkflowLoadService.class);
		User user = getModel().getObject().getProfile(UserProfile.class).getUser();
		UserWorkLoadData data = service.getUserWorkLoad(user);
		
		
		WebMarkupContainer mk = new WebMarkupContainer("due-dates");
		add(mk);

		Label total_label = new Label("total-label", "Total Tasks");
		Label total_value = new Label("total-value", String.valueOf(data.total));
		mk.add(total_label);
		mk.add(total_value);

		
		Label today_label = new Label("today-label", today_str);
		Label today_value = new Label("today-value", String.valueOf(data.today_due_date));
		mk.add(today_label);
		mk.add(today_value);
		
		Label pastdue_label = new Label("pastdue-label", pastdue_str);
		Label pastdue_value = new Label("pastdue-value", String.valueOf(data.past_due_date));
		mk.add(pastdue_label);
		mk.add(pastdue_value);
		
		
		Label today1_label = new Label("today1-label", today1_str);
		Label today1_value = new Label("today1-value", String.valueOf(data.due_plus_one));
		mk.add(today1_label);
		mk.add(today1_value);
		
		
		Label today2_label = new Label("today2-label", today2_str);
		Label today2_value = new Label("today2-value", String.valueOf(data.due_plus_two));
		mk.add(today2_label);
		mk.add(today2_value);

		Label today3_label = new Label("today3-label", today3_str);
		Label today3_value = new Label("today3-value", String.valueOf(data.due_plus_three));
		mk.add(today3_label);
		mk.add(today3_value);
		
		int n_or_more= data.due_plus_four + data.due_plus_five + data.due_plus_six + data.due_plus_n;
		
		Label today4or_more_label = new Label("today4ormore-label", today4or_more_str);
		Label today4or_more_value = new Label("today4ormore-value",  String.valueOf(n_or_more));
		mk.add( today4or_more_label);
		mk.add( today4or_more_value);
		
		Label duenone_label = new Label("duenone-label", duenone);
		Label duenone_value = new Label("duenone-value", String.valueOf(data.due_none));
		mk.add(duenone_label);
		mk.add(duenone_value);
	}

	private KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	 

}
