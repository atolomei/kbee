package kbee.web.portal6;

import java.util.HashMap;
import java.util.Map;

import com.novamens.portal6.model.PortalService;

import kbee.web.dashboard.DashboardMyTasksWidgetPanel;
import kbee.web.dashboard.DashboardPendingTasksWidgetPanel;
import kbee.web.searcher.PortalSearchForm;


import com.novamens.portal6.model.PortalLiteralsService;

public class KbeePortalLiteralsService implements PortalLiteralsService {
	
	// Model
	// View
	// DataProvider

	
	
	
	
		

	
	
	private class PL_Item {
		public String model;
		public String view;
		public String data_provider;
		public PL_Item(String model, String view, String data_provider) {
			this.model=model;
			this.view=view;
			this.data_provider=data_provider;
		}
	}
	
	
	Map<String, PL_Item> map = new HashMap<String, PL_Item>();

	
	
	
	public KbeePortalLiteralsService() {
		map.put(BLOCK_SEARCH,   new PL_Item(BLOCK_SEARCH, PortalSearchForm.class.getName(), null));
		map.put(BLOCK_MY_TASKS, new PL_Item(BLOCK_MY_TASKS, DashboardMyTasksWidgetPanel.class.getName(), null));
		map.put(BLOCK_MY_TASKS, new PL_Item(BLOCK_PENDING_TASKS, DashboardPendingTasksWidgetPanel.class.getName(), null));
	}

	
	public String getViewer( String model) {
		if (map.containsKey(model)) {
			return map.get(model).view;
		}
		return null;
	}

	public String getDataProvider( String model) {
		if (map.containsKey(model)) {
			return map.get(model).data_provider;
		}
		return null;
	}
}




/**
ServiceLocator.getService(PortalMVCService.class).registerViewer("block-search", 					PortalSearchForm.class.getName());

// Widgets
ServiceLocator.getService(PortalMVCService.class).registerViewer("block-widget-mytasks", 				DashboardMyTasksWidgetPanel.class.getName());
ServiceLocator.getService(PortalMVCService.class).registerViewer("block-widget-pending-tasks", 			DashboardPendingTasksWidgetPanel.class.getName());
ServiceLocator.getService(PortalMVCService.class).registerViewer("block-widget-monitor", 		 		DashboardMonitorTasksWidgetPanel.class.getName());
ServiceLocator.getService(PortalMVCService.class).registerViewer("block-widget-library", 		 		DashboardLibraryWidgetPanel.class.getName());
ServiceLocator.getService(PortalMVCService.class).registerViewer("block-widget-portal-library",  		PortalBlockContentsPanel.class.getName());

// text
ServiceLocator.getService(PortalMVCService.class).registerViewer("block-portal-text", PortalSimpleTextPanel.class.getName());


// list of views: IQLs, News. 
ServiceLocator.getService(PortalMVCService.class).registerViewer("block-widget-listview",  	PortalBlockListViewPanel.class.getName()); 
ServiceLocator.getService(PortalMVCService.class).registerViewer("block-widget-content-list",  PortalBlockListContentPanel.class.getName());

// ------------------
//
ServiceLocator.getService(PortalMVCService.class).registerViewer("block-billboard", "kbee.web.alert.BillboardPanel");
ServiceLocator.getService(PortalMVCService.class).registerViewer("area-billboard", kbee.web.alert.BillboardPanel.class.getName());
ServiceLocator.getService(PortalMVCService.class).registerViewer("area-dummy", com.novamens.wicket.util.DummyBlockPanel.class.getName());
ServiceLocator.getService(PortalMVCService.class).registerViewer("block-dummy", DummyBlockPanel.class.getName());



// DataProvider ---
ServiceLocator.getService(PortalMVCService.class).registerDataProvider("block-portal-text", PortalBlockTextDataProvider.class.getName());
*/


