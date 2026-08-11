package com.novamens.kbee.content.webapi.handler;


import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.library.Library;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.webapi.query.MonitorQuery;
import com.novamens.kbee.content.webapi.query.PendingTasksQuery;
import com.novamens.kbee.content.webapi.query.WorkspaceQuery;
import com.novamens.kbee.content.webapi.type.ILibraryAdapter;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ILibrary;
import kbee.api.model.ApiResource;
import kbee.api.model.ApiUser;
import kbee.api.model.IUserDashboard;

public class UserDashboardHandler extends  UserUpdateHandler {
	
	static final public String PROPERTY_UNREAD = "unread";
	ResultSet workspace;
	
	public IUserDashboard get(User user) {
		IUserDashboard dashboard = new IUserDashboard();
	
 		dashboard.setWorkspace(getWorkspaceCount(user));
		dashboard.setWorkspaceUnread(getWorkspaceUnreadCount(user));
		dashboard.setPendings(getPendingsCount(user));
		dashboard.setMonitor(getMonitorCount(user));
		
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		dashboard.setSignatureLevel(userProfile.getSignatureSecurityLevel());
		dashboard.setUser(getUser(userProfile));

		dashboard.setMonitorEnabled(isMonitorEnabled());
		dashboard.setPendingsEnabled(isPendingsEnabled());
		dashboard.setValuesEnabled(isValuesEnabled());
		
		DomRepository<Library> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(Library.class);
		List<ILibrary> libraries = new ArrayList<>();
		for (Library library : repository.findAll()) {
			if (library.isReadable() && library.isEnabled()) {
				libraries.add((new ILibraryAdapter()).adapt(library));
			}
		}
		dashboard.setLibraries(libraries);
		 
		return dashboard;
	}
	
	private ApiUser getUser(UserProfile profile) {
		KbeeUser user = (KbeeUser)profile.getUser();
		ApiUser iuser = new ApiUser();
		iuser.setDisplayName(user.getDisplayName());
		iuser.setId(String.valueOf(user.getId()));
		iuser.setDomain(user.getDomain().getDisplayName());
		iuser.setPhoto(null);
		Person person = profile.getPerson();
		if (person.getPhoto()!=null) {
			ApiResource photo = new ApiResource();
			photo.setId(String.valueOf(person.getPhoto().getId()));
			photo.setHRef(UriHelper.getUri(person.getPhoto()));
			photo.setName(person.getPhoto().getName());
			photo.setRel("file");
			iuser.setPhoto(photo);
		}
		return iuser;
	}
	
	private int getWorkspaceCount(User user) {
		WorkspaceQuery query = new WorkspaceQuery(getQueryIndex(), user, false);
		workspace = query.execute();
		int count = workspace.size();
		return count;
	}
	
	private int getWorkspaceUnreadCount(User user) {
		int count = 0;
		boolean unread = true;
		while (workspace.hasNext()) {
			Content content = (Content)workspace.next().getObject();
			unread = unread(content);
			if (unread) count++;
		}
		workspace.close();
		return count;
	}
	
	private int getPendingsCount(User user) {
		PendingTasksQuery query = new PendingTasksQuery(getQueryIndex(), user, false);
		ResultSet pendings = query.execute();
		int count = pendings.size();
		pendings.close();
		return count;
	}
	
	private int getMonitorCount(User user) {
		MonitorQuery query = new MonitorQuery(getQueryIndex(), user, false);
		ResultSet monitor = query.execute();
		int count = monitor.size();
		monitor.close();
		return count;
	}
	
	private boolean unread(Content content) {
		String nr = (String) content.getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
		boolean isUnread= nr!=null && nr.equals("yes");
		return isUnread;
	}
	
	private boolean isMonitorEnabled() {
		boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot(); 
		boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		boolean is_monitor = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MONITOR_AUDIT.getId());
		boolean is_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
		return is_domain_admin || is_root || is_monitor || is_support; 
	}
	
	private boolean isPendingsEnabled() {
		boolean is_root	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
		boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
		boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		boolean is_pending	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());
		return is_domain_admin || is_root || is_pending || is_support; 
	}
	
	private boolean isValuesEnabled() {
		boolean is_root	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
		boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		boolean is_values	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
		return is_domain_admin || is_root || is_values; 
	}
	
 	private Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
}