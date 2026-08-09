package kbee.web.dashboard;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


import com.novamens.content.subscription.ContentSubscription;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.ServiceLocator;


public class DashboardWidgetContentSubscriptionPanel extends DashboardListWidgetPanel<ContentSubscription> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardWidgetContentSubscriptionPanel.class.getName());
	
	private String zid;
	private Locale locale;
	private int size;
	
	
	public DashboardWidgetContentSubscriptionPanel(String id, String preferences_key) {
		super(id, preferences_key);

	}
	
	public DashboardWidgetContentSubscriptionPanel(String id) {
		super(id);
		
		KbeeUser us = (KbeeUser) getSessionUser();
		locale=us.getLocale();
		zid = ServiceLocator.getService(DateTimeService.class).getMapZoneIds().get(us.getTimeZone());

	}




	@Override
	public void onInitialize() {
		setHelp(true);
		setTitle( new Model<String>( "NOOOOOOOOOSubscription"));
		addList();
		super.onInitialize();
		
		
	}
	
	
	
	protected void addList() {
		List<IModel<ContentSubscription>> list = new ArrayList<IModel<ContentSubscription>>();
		try {
			
			//User w_user = getDomain().getService(DomainService.class).getWorkflowUser();
			//((KbeeUser) w_user).getService(UserListService.class).getSavedQueries( etSiteModel().getObject()).forEach(item -> list.add(new ObjectModel<SavedQuery>(item)));

			size=list.size();
		} catch (Exception e) {
			logger.error(e);
		}
		setItems(list);
	}
	public void onDetach() {
		super.onDetach();
		//if (model!=null)
		//	model.detach();
	}
	
	
	
	
}
