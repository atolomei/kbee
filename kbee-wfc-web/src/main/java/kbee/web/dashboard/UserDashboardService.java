package kbee.web.dashboard;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.library.Library;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.notification.Notification;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal6.model.Site;
import com.novamens.service.BusinessObjectService;

public interface UserDashboardService extends BusinessObjectService {

	public ResultSet getMyTasks();
	public ResultSet getMyTasks(EntityMember entity);
	public ResultSet getPendingTasks();
	public ResultSet getPendingTasks(EntityMember entity);
	
	public ResultSet getMyDocuments();
	
	public ResultSet getMonitoredTasks();
	public ResultSet getMonitoredTasks(EntityMember entity);
	
	public Content getLastTask(EntityMember entity);
	
	public ResultSet getLibraryContents(Library library);
	public ResultSet getLibraryContents(Library library, EntityMember entity);
	public ResultSet getLibraryContents(EntityMember entity);
	
	public List<Notification> getMyNotifications(int limit);
	
	public List<Site> getMySites();
	public List<Site> getSites();
	
	public List<Domain> getDomains();
	
	public long getTotalCountMyNotifications();
	
	public List<ProcessLauncher> getProcessLaunchersTasks();
	
	public List<Content> getSiteContents(Site site, int max);
	public List<DataSetMember> getDataSetMembers(DataSet dataSet, int i);
	
	public ResultSet getProgressNotes(int max);
	public ResultSet getUserLibraryContents(Library library, KbeeUser sessionUser);
}