package kbee.web.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.LinkCellItem;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.notification.UserNotificationsPage;
import kbee.web.notification.UserNotificationsPanel;
 

public class DashboardWidgetNotificationsPanel extends DashboardWidgetBasePanel {
	
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardWidgetFileFactoryPanel.class.getName());
	
	private WebMarkupContainer help;
	private WebMarkupContainer main_container;

	/** 
	 * @param id
	 */
	public DashboardWidgetNotificationsPanel(String id, String preferences_key) {
		super(id, preferences_key);
		setModel(new ObjectModel<Person>(getPerson()));
		
		setTitle(new StringResourceModel("mynotifications",  this, null));
	}
	
	IModel<Person> model;
	
	
	@Override
	protected void onClickCollapse(AjaxRequestTarget target) {
		main_container.setVisible(!main_container.isVisible());
		refresh(target);
	}
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		InlineHelpWebService se=ServiceLocator.getService(InlineHelpWebService.class);
		 WebMarkupContainer  pa = se.getPanel("help", getLocale(), InlineHelpWebService.HOME_NOTIFICATIONS);
		 if (pa!=null)
			 return pa;
		 return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_NOTIFICATIONS));
	}

	
	public IModel<Person> getModel() {
		return model;
	}

	public void setModel(IModel<Person> model) {
		this.model = model;
	}
	
	@Override
	public void onInitialize() {
			super.onInitialize();

			
			setHelp(true);
			
			main_container = new WebMarkupContainer ("notifications-container");
			
			main_container.setOutputMarkupId(true);
			add(main_container);
			
			if (help==null)
				help=new InvisiblePanel("help");
			main_container.add(help);


			KbeeUser us = (KbeeUser) getSessionUser();
			long total=us.getService(UserDashboardService.class).getTotalCountMyNotifications();
			Panel view;
			
			UserNotificationsPanel pa= new UserNotificationsPanel("notifications");
			
			if (pa.getListSize()==0) {
				main_container.add(new  DashboardSimpleInfoPanel("notifications", new StringResourceModel("no-items", this,null), "fad fa-bell"));
				 view=new InvisiblePanel("item");
			}
			else {
				main_container.add(pa);
				view=new LabelPanel("item", new Label("label", "Newest <b>" + pa.getListSize()  +"</b> of <b>" + String.valueOf(total)+"</b>")); 
			}
			
			 LinkCellItem<Person> notes_l=new LinkCellItem<Person>("item", new ObjectModel<Person>(getPerson()), new StringResourceModel("mynotifications", this, null)) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick() {
						setResponsePage(new UserNotificationsPage());
				}
			};
			List<Panel> l_p =new ArrayList<Panel>();
			l_p.add(notes_l);
			List<Panel> l_r =new ArrayList<Panel>();
			l_r.add(view);
			DashboardSimpleBottomPanel db =new DashboardSimpleBottomPanel("base-bottom", l_p, l_r);
			setBottomPanel(db);
			
			
			
			
			
			
			
			
	}
	
	@Override
	protected void onTitleClick() {
	}
	

	protected void onHelp(AjaxRequestTarget target) {
		
		
		if (help==null || help instanceof InvisiblePanel) {
			help=new DummyBlockPanel("help");
			help.setVisible(false);
			main_container.addOrReplace(help);
		}
		toogleHelp(target);
	}
	
	public void toogleHelp(AjaxRequestTarget target) {

		if (help!=null && !(help instanceof InvisiblePanel)) {
			help.setVisible(!help.isVisible());
			main_container.get( "notifications").setVisible(!main_container.get( "notifications").isVisible());
			target.add(this.main_container);
		}
	}
	

	@Override
	public void onDetach() {
		super.onDetach();
	}
	

}
