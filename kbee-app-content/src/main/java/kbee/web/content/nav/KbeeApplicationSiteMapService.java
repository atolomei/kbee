package kbee.web.content.nav;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.content.web.admin.api.APIRequestsReportPage;
import com.novamens.content.web.admin.api.APIStatsReportPage;
import com.novamens.content.web.admin.api.SystemInfoAPIDashboardPanel;
import com.novamens.content.web.admin.files.DMDirectoryCreationPanel;
import com.novamens.content.web.admin.files.DMFilesPanel;
import com.novamens.content.web.admin.files.DMTextFileEditorPanel;
import com.novamens.content.web.admin.files.DMUploadPanel;
import com.novamens.content.web.admin.markup.JvmDumpPanel;
import com.novamens.content.web.admin.markup.SystemInfoConfigPanel;
import com.novamens.content.web.admin.markup.SystemInfoDatabasePanel;
import com.novamens.content.web.admin.markup.SystemInfoKBFSPanel;
import com.novamens.content.web.admin.markup.SystemInfoLogsPanel;
import com.novamens.content.web.admin.markup.SystemInfoPage;
import com.novamens.content.web.admin.markup.SystemInfoPropertiesPanel;
import com.novamens.content.web.admin.markup.SystemInfoSearchPanel;
import com.novamens.content.web.admin.markup.SystemInfoServerPanel;
import com.novamens.content.web.admin.markup.SystemParametersPanel;
import com.novamens.content.web.admin.markup.VersionInfoPanel;
import com.novamens.content.web.admin.markup.datamanagement.DMSQLPanel;
import com.novamens.content.web.admin.markup.datamanagement.SystemDataManagementPage;
import com.novamens.content.web.admin.markup.datamanagement.SystemDataManagementPanel;
import com.novamens.content.web.admin.markup.datamanagement.SystemSchedulerMonitorPage;
import com.novamens.content.web.admin.markup.datamanagement.SystemSchedulerMonitorPanel;
import com.novamens.content.web.console.markup.DashboardPage;
import com.novamens.content.web.deployManagement.DeployManagementPanel;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.content.web.report.markup.ReportPage;
import com.novamens.content.web.report.markup.ReportSubscriptionPage;
import com.novamens.content.web.security.markup.GroupsPage;

import com.novamens.content.web.security.markup.RulesPage;
import com.novamens.content.web.workflow.markup.batch.TaskBatchCreatePage;
import com.novamens.dom.Domain;

import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.web.alert.BillboardsPage;
import kbee.web.command.panel.CommandsPage;
import kbee.web.content.console.ArchivePage;
import kbee.web.content.console.ContentBasePage;
import kbee.web.content.console.MonitorPage;
import kbee.web.content.console.PendingTasksPage;
import kbee.web.content.console.RecycleBinPage;
import kbee.web.dashboard.DashboardHomePage;
import kbee.web.datamanagement.ReindexPage;
import kbee.web.datamanagement.TagManagementPage;
import kbee.web.dataset.DashboardDataSetMembersHomePage;

import kbee.web.dataset.DataSetMembersPage;
import kbee.web.dataset.FixedContentPage;
import kbee.web.dataset.MemberBatchCreationPageV5;
import kbee.web.dataset.MemberPage;
import kbee.web.domain.DomainPage;
import kbee.web.domain.DomainsPage;
import kbee.web.domain.DomainsRecycleBinPage;
import kbee.web.emailtemplate.EmailTemplatesPage;
import kbee.web.enoti.ENotiRulesPage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.library.LibrariesPage;
import kbee.web.model.AttributeModelPage;
import kbee.web.model.ClassifierModelPage;
import kbee.web.model.DashboardInformationModelPage;
import kbee.web.model.DataSetPage;
import kbee.web.model.DataSetsPage;

import kbee.web.model.contentclass.ContentTemplatePage;
import kbee.web.model.procedure.ProcedurePage;
import kbee.web.multidimensional.FacetsPage;
import kbee.web.notes.UserNotesPage;
import kbee.web.notification.UserNotificationsPage;
import kbee.web.notes.BillboardPage;
import kbee.web.objectstorage.ObjectStoragePage;
import kbee.web.portal6.SitesPage;
import kbee.web.report.ReportsHomePage;
import kbee.web.rule.ActionRulesPage;
import kbee.web.searcher.page.SearcherUserListPage;
import kbee.web.security.role.RolePage;
import kbee.web.security.role.RolesPage;
import kbee.web.security.user.MyAccountPage;
import kbee.web.security.user.UserMainPanel;
import kbee.web.security.user.UserPage;
import kbee.web.security.user.UserStandAlonePage;
import kbee.web.security.user.UsersPage;
import kbee.web.service.ApplicationSiteMapService;
import kbee.web.source.SourcesPage;
	
public class KbeeApplicationSiteMapService implements ApplicationSiteMapService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeApplicationSiteMapService.class.getName());
													
	private Map<String, Class<?>> map = new  HashMap<String, Class<?>>();

	private Map<String, List<KbeeGlobalRole>> sec;
	
	public KbeeApplicationSiteMapService() {
		init();
	}

	@Override
	public WebPage getPage(String pageKey) {
		return getPage(pageKey, (PageParameters)null);
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public WebPage getPage(String pageKey, PageParameters parameters) {
		if (this.map.containsKey(pageKey)) {
			Class<?> pageclass = this.map.get(pageKey);
			try {
				if (parameters!=null) {
					Constructor<?> constructor = pageclass.getConstructor(new Class[]{ PageParameters.class });
					WebPage page = (WebPage) constructor.newInstance(parameters);
					return page;
				} 
				else {
					Constructor<?> constructor = pageclass.getConstructor();
					WebPage page = (WebPage) constructor.newInstance();
					return page;
				}
			} 
			catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.error(e);
				throw new KbeeRuntimeException(e);
			}
		}
		return new ApplicationErrorPage(new Model<String>("pageKey -> " + pageKey), new Model<String>("PageKey not found"));
	}
	
	public WebPage getPage(String pageKey, Object... parameters) {
	
		if (this.map.containsKey(pageKey)) {
			Class<?> pageclass = this.map.get(pageKey);
			try {
				if (parameters!=null) {
					Constructor<?> constructor = getConstructor(pageclass, parameters);
					if (constructor!=null) {
						WebPage page = (WebPage) constructor.newInstance(parameters);
						return page;
					}
				} 
				else {
					Constructor<?> constructor = pageclass.getConstructor();
					WebPage page = (WebPage) constructor.newInstance();
					return page;
				}
			} 
			catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.error(e);
				throw new KbeeRuntimeException(e);
			}
		}
		return new ApplicationErrorPage<Void>(new Model<String>("pageKey -> " + pageKey), new Model<String>("PageKey not found"));
	}

	@Override
	public Component getMainTopBar() {
		Object c= getBean("app-top-bar");
		if (c!=null)
			return (Component) c;
		return  new GlobalNavigationBar<Void>("navigation");
	}
	
	@Override
	public Component getMainLateralMenu(String appKey) {
		return (Component)getBean("app-lateral-menu", "menu", appKey);
	}
	
	@Override
	public Component getSiteLateralMenu(String appKey) {
		return (Component)getBean("app-lateral-menu", "menu", appKey);
	}
	
	@Override
	public Panel getFactoryPanel(String id, String key) {
		return getFactoryPanel(id, key, null);
	}
	
	@Override
	public Panel getBeanPanel(String id, String key, Object... parameters) {
		return (Panel) getBean(key, parameters);
	}
	
	

	@Override
	public Panel getFactoryPanel(String id, String key,  PageParameters parameters) {
		
		switch (key) {
		
			case  "object-storage": 				return new SystemInfoKBFSPanel(id);
			case  "database": 						return new SystemInfoDatabasePanel(id);
			case  "search": 		 				return new SystemInfoSearchPanel(id); 
			case  "hardware": 		 				return new SystemInfoServerPanel(id);
			case  "api-dashboard": 					return new SystemInfoAPIDashboardPanel(id);
			case  "properties": 					return new SystemInfoPropertiesPanel(id); 
			case  "logs":  							return new SystemInfoLogsPanel(id);
			case  "config": 						return new SystemInfoConfigPanel(id);
			case  "version": 						return new VersionInfoPanel(id); 
			case  "jvm-threads": 					return new JvmDumpPanel(id);
			case   "backups":						return new DeployManagementPanel(id);
			case  "dashboard": 						return new SystemInfoAPIDashboardPanel(id);
			case  "system.parameters":              return new SystemParametersPanel(id);
			
			//
			case  "commands": 						return new SystemDataManagementPanel(id);
			case  "sql-gateway": 					return new DMSQLPanel(id);
			case  "file-explorer": 					return new DMFilesPanel(id);
			case  "deploy": 						return new DeployManagementPanel(id);
			case  "scheduler": 						return new SystemSchedulerMonitorPanel(id);
			
			//
			case  "dm-text-file-editor-panel": 		return new DMTextFileEditorPanel(id, parameters);
			case  "dm-upload-panel": 				return new DMUploadPanel(id, parameters);
			
			case  "directory-creation": 			return new DMDirectoryCreationPanel(id, parameters);
			
			case  "user-main-panel": 				return new UserMainPanel(id, parameters);
			
		}
		return new ErrorPanel(id, new Model<String>("Key -> " + key), new Model<String>("Key not found"));
	}

	
	
	@Override
	public Panel getPanel(String id, String key, PageParameters parameters) {
		switch (key) {
			case  "user-main-panel": return new UserMainPanel(id, parameters);
		}
		return new ErrorPanel(id, new Model<String>("Key -> " + key), new Model<String>("Key not found"));
	}
	
	
	
	// @Override
	public Panel getPanel(String panelKey) {
		return (Panel)getBean(panelKey);
 	}
	
	@Override
	public boolean isAccessEnabled(String webResource, User user) {
		if (ServiceLocator.getService(SecurityService.class).isRoot(user))
			return true;
		return 	isPremiumAccessEnabled(webResource, user);
	}
	
	public Map<String, Class<?>> getPageKeys() {
		return map;
	}
	
	public Object getBean(String key, Object...parameter) {
		return ServiceLocator.getService(BeansService.class).getBean(key, parameter);
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}	
	
	private void init() {
		
		// My Workspace
		
		map.put(ApplicationSiteMapService.MyDocumentsPage , kbee.web.content.console.MyDocumentsPage.class);

		// Tasks
		//
		map.put("task-pending-page", PendingTasksPage.class);
		map.put("task-mytasks-page", kbee.web.content.console.WorkspacePage.class);
					
	 
		map.put(ApplicationSiteMapService.HomePage , DashboardHomePage.class);
		map.put(ApplicationSiteMapService.WorkflowDashbaardPage , DashboardPage.class);
		
		
		map.put("task-monitor-page", MonitorPage.class);
		map.put("task-bulk-upload-page", TaskBatchCreatePage.class);
		
		
		// resources
		map.put("resources-mybox-page", kbee.web.content.console.MyResourcesPage.class);
		map.put("resources-publicbox-page", kbee.web.content.console.PublicResourcesPage.class);
		
		
		// Library
		//
		map.put("library-archive-page", ArchivePage.class);
		map.put("library-contentbase-page", ContentBasePage.class);
		
		map.put("recycle-bin-page", RecycleBinPage.class);
		map.put("library-fixed-page", FixedContentPage.class);

		// Settings
		//
		map.put("settings-libraries-page", LibrariesPage.class);
		map.put("settings-facets-page", FacetsPage.class);
		map.put("settings-sources-page", SourcesPage.class);
		map.put("settings-generalsettings-page", DomainPage.class);
		map.put("settings-emailtemplates-page", EmailTemplatesPage.class);
		
		map.put("settings-dataset-members-home-page", DashboardDataSetMembersHomePage.class);
		map.put("settings-dataset-members-page", DataSetMembersPage.class);
		map.put("settings-dataset-members-bulk-page", MemberBatchCreationPageV5.class);
		map.put("settings-dataset-member-page", MemberPage.class);						
		
		
		// portal
		//
		map.put("sites-page", SitesPage.class);
		map.put("portal-sites-page", SitesPage.class);
		
		
		// Security
		//
		map.put(ApplicationSiteMapService.SecurityUsersPage, UsersPage.class);
		map.put("security-roles-page", RolesPage.class);
		map.put("security-rules-page", RulesPage.class);
		map.put("security-groups-page", GroupsPage.class);
		
		map.put("security-user-page", UserPage.class);
		map.put("security-role-page", RolePage.class);
		map.put("security-user-standalone-page", UserStandAlonePage.class);
		
		// User
		//
		map.put("user-myaccount-page", MyAccountPage.class);
		map.put("user-notifications-page", UserNotificationsPage.class);
		map.put("user-notes-page", UserNotesPage.class);
		
		
		// Model
		//
		map.put("model-procedure-page", ProcedurePage.class);
		map.put("model-datasets-page", DataSetsPage.class);

		map.put("model-dataset-page", DataSetPage.class);
		map.put("model-attribute-page",  AttributeModelPage.class);
		map.put("model-classifier-page",  ClassifierModelPage.class);
		map.put("model-contenttemplate-page",  ContentTemplatePage.class);
		
		map.put("model-home-page",DashboardInformationModelPage.class);
		
		// Data Management
		//
		map.put("data-management-tagtool-page", TagManagementPage.class);
		map.put("data-management-reindex-page", ReindexPage.class);
		
		
		// Factory
		//
		map.put("factory-system-info-page", SystemInfoPage.class);
		map.put("factory-commands-page", CommandsPage.class);
		map.put("factory-scheduler-monitor-page",  SystemSchedulerMonitorPage.class);
		map.put("factory-domains-page", DomainsPage.class);
		
		map.put("factory-domain-recycle-bin-page", DomainsRecycleBinPage.class);
		map.put("factory-system-info-kbfs-page", SystemInfoKBFSPanel.class);
		map.put("factory-database-page", SystemInfoKBFSPanel.class);
		
		map.put("factory-system-data-management-page", SystemDataManagementPage.class);
		map.put("factory-objectstorage-page", ObjectStoragePage.class);
		
		map.put("factory-api-requests-report-page", APIRequestsReportPage.class);
		map.put("factory-api-stats-report-page", APIStatsReportPage.class);
		
		// Reports
		//
		map.put("report-subscription-page",ReportSubscriptionPage.class);
		map.put("report-page",ReportPage.class);
		map.put("report-home-page",ReportsHomePage.class);
		
		 
		
		// Alerts
		//
		map.put("alert-management-billboards-page", BillboardsPage.class);
		map.put("alert-management-enoti-rule-page", ENotiRulesPage.class);
		map.put("alert-management-action-rules-page", ActionRulesPage.class);
		        		
		
		map.put("alert-billboard-page", BillboardPage.class);
		
		map.put(ApplicationSiteMapService.UserListPage, SearcherUserListPage.class );
		
		
		initWebResourcesAccess();
		
	}
	
	

	private Constructor<?> getConstructor(Class<?> aclass, Object... parameters) {
		Constructor<?> constructors[] = aclass.getConstructors();
		for (int i = 0; i<constructors.length; i++) {
			Constructor<?> constructor = constructors[i];
			boolean match = false;
			if (constructor.getParameterTypes().length == parameters.length) {
				int p = 0;
				match = true;
				for (Class<?> type : constructor.getParameterTypes()) {
					if (!type.isAssignableFrom(parameters[p].getClass())) {
						match = false;
						break;
					}
				}
				if (match) {
					return constructor;
				}
			}
		}
		return null;
	}
	
	
	
	/**
	 * Access
	 * 
	 */
	
	
	protected Map<String, List<KbeeGlobalRole>> getResourcesAccessMap() {
		return sec;
	}
	
	private synchronized void initWebResourcesAccess() {
		sec = new HashMap<String, List<KbeeGlobalRole>>();
	}
	
	protected boolean isBasicAccessEnabled(String webResource, User user) {
		if (ServiceLocator.getService(SecurityService.class).isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId()))
			return true;
		return true;
	}
							
	protected boolean isPremiumAccessEnabled(String webResource, User user) {
		if (ServiceLocator.getService(SecurityService.class).isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId()))
			return true;
		return true;
	}
}
