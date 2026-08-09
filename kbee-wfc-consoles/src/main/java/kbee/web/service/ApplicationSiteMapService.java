package kbee.web.service;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.security.User;
import com.novamens.service.SystemService;

public interface ApplicationSiteMapService extends SystemService {
	
	public static String WorkflowDashbaardPage = "workflow-dashboard-page";
	public static String HomePage = "dashboard-page";
	public static String WorkspacePage = "task-mytasks-page";
	public static String MonitorPage = "task-monitor-page";
	public static String ContentBasePage = "library-contentbase-page";
						
	public static String UserListPage = "users-list-page";
	public static String DataSetMemberPage = "settings-dataset-member-page";
	
	public static String MyDocumentsPage = "workspace-mydocuments";

	
	public static String SecurityUsersPage = "security-users-page";

	public WebPage getPage(String pageKey);
	public WebPage getPage(String pageKey, PageParameters parameters);
	public WebPage getPage(String pageKey, Object... parameters);

	public Component getMainTopBar();
	public Component getMainLateralMenu(String appKey);
	
	public Component getSiteLateralMenu(String appKey);
	
	public Panel getPanel(String panelKey);
	
	public Object getBean(String key, Object...parameter);
	
	

	public boolean isAccessEnabled(String webResource, User user);
	public Panel getFactoryPanel(String id, String key);
	public Panel getFactoryPanel(String id, String key, PageParameters parameters);

	public Panel getPanel(String id, String key, PageParameters parameters);
	public Panel getBeanPanel(String id, String key, Object... parameters);

}