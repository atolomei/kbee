package kbee.web.dashboard;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.library.Library;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.notification.Notification;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.web.query.DataSetMembersQuery;
import kbee.web.query.LibraryQuery;
import kbee.web.query.MonitorQuery;
import kbee.web.query.MyDocumentsQuery;
import kbee.web.query.PendingTasksQuery;
import kbee.web.query.ProgressNotesQuery2;
import kbee.web.query.WorkspaceQuery;


/**
 * 
 * 
 */
public class KbeeUserDashboardService implements UserDashboardService, EventListener {

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeUserDashboardService.class.getName());
	
	static final int LIMIT = 25;
	
    private User user;
    private ContentDao contentDao;
    private PortalDao portalDao;

    List<Site> sites = null;
    		
    
    public List<Site> getSites() {
    	
    	if (sites!=null)
    		return sites;
    	
    	synchronized (this) {
    		sites = getMySites();
    		
    	}
    	return sites;
    	
    }
    
    public KbeeUserDashboardService() {
    }

    public KbeeUserDashboardService(User user) {
        this.user = user;
    }
    
	@Override
	public List<Site> getMySites() {
		return getPortalDao().getSites(getDomain(), ObjectState.ENABLED);
	}
	
	@Override
	public List<Domain> getDomains() {
		return getContentDao().getDomains(ObjectState.ENABLED);
	}
	
    @Override
    public ResultSet getMyTasks() {
   		WorkspaceQuery query = new WorkspaceQuery(getIndex(), getUser());
		ResultSet result = query.execute();
		return result;
    }
    
    @Override
    public ResultSet getMyTasks(EntityMember entity) {
		WorkspaceQuery query = new WorkspaceQuery(getIndex(), getUser());
		query.setAsParameter(entity);
		ResultSet result = query.execute();
		return result;
    }
    
    @Override
    public ResultSet getPendingTasks() {
    	PendingTasksQuery query = new PendingTasksQuery(getIndex());
		ResultSet result = query.execute();
		return result; 
    }
    
    @Override
    public ResultSet getPendingTasks(EntityMember entity) {
    	PendingTasksQuery query = new PendingTasksQuery(getIndex());
		query.setAsParameter(entity);
		ResultSet result = query.execute();
		return result;
    }
    
    @Override
    public ResultSet getMonitoredTasks() {
    	MonitorQuery query = new MonitorQuery(getIndex());
		ResultSet result = query.execute();
		return result; 
    }
    
    @Override
    public ResultSet getMonitoredTasks(EntityMember entity) {
    	MonitorQuery query = new MonitorQuery(getIndex());
		query.setAsParameter(entity);
		ResultSet result = query.execute();
		return result;
    }
    
    @Override
    public ResultSet getMyDocuments() {
   		MyDocumentsQuery query = new MyDocumentsQuery(getIndex());
		ResultSet result = query.execute();
		return result;
    }
    
	public Content getLastTask(EntityMember entity) {
    	MonitorQuery query = new MonitorQuery(getIndex());
		query.setAsParameter(entity);
		query.getFilterParameters().remove("state");
		query.getFilterParameters().remove("inworkspace");
		ResultSet result = query.execute();
		if (result.hasNext()) {
			Object object = result.next().getObject();
			return ((Content)object);
		}
		return null;
	}
 
    public User getUser() {
        return user;
    }

	@Override
	public List<ProcessLauncher> getProcessLaunchersTasks() {
		return null;
	}
	
	public List<Content> getSiteContents(Site site, int limit) {
		List<Content> list_c = new ArrayList<Content>();
    	try {
    		SearcherSiteQuery qe= new SearcherSiteQuery(site, getIndex());
    		ResultSet res=qe.execute();
	    	if (res==null) 
	    		return list_c;
	    	int total = 0;
	    	while (res.hasNext() && total++<limit) {
	    		SearchResult r=res.next();
	    		list_c.add( (Content) r.getObject());
	    	}
	    	list_c.sort(new Comparator<Content>() {
				@Override
				public int compare(Content o1, Content o2) {
					try {
						return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
					} 
					catch (Exception e) {
						logger.error(e);
						return 0;
					}
				}
	    	});
    	} 
    	catch (Exception e) {
    		logger.error(e);
    	}
    	return list_c;

		
	}

	@Override
	public List<DataSetMember> getDataSetMembers(DataSet dataSet, int limit) {
	
		
		List<DataSetMember> list = new ArrayList<DataSetMember>();
    	
    	try {

    		DataSetMembersQuery qe = new DataSetMembersQuery(getQueryIndex(), dataSet, false);

    		ResultSet res=qe.execute();
    	
	    	if (res==null) 
	    		return list;
	    	
	    	int total = 0;
    	
	    	while (res.hasNext() && total++<limit) {
	    		SearchResult r=res.next();
	    		list.add( (DataSetMember) r.getObject());
	    	}
	    	
	    	
	    	list.sort(new Comparator<DataSetMember>() {
				@Override
				public int compare(DataSetMember c1, DataSetMember c2) {
					try {
						if (c1.getDisplayName()==null)
							return 1;
						if (c2.getDisplayName()==null)
							return -1;
						return c1.getDisplayName().compareToIgnoreCase(c2.getDisplayName());
					} catch (Exception e) {
						return 0;
					}
				}
	    	});
    	
    	} catch (Exception e) {
    		logger.error(e);
    	}
    	return list;
	}

	@Override
	public ResultSet getLibraryContents(Library library, EntityMember entity) {
	   	LibraryQuery query = new LibraryQuery(getIndex(), library);
		query.setAsParameter(entity);
		ResultSet result = query.execute();
		return result; 
	}

	@Override
	public ResultSet getUserLibraryContents(Library library, KbeeUser sessionUser) {
	   	LibraryQuery query = new LibraryQuery(getIndex(), library);
	   	query.getParameters().put("lastmodifieduser", sessionUser.getId().toString());
		ResultSet result = query.execute();
		return result; 
	}
	
	
	@Override
	public ResultSet getLibraryContents(EntityMember entity) {
	   	LibraryQuery query = new LibraryQuery(getIndex());
		query.setAsParameter(entity);
		ResultSet result = query.execute();
		return result; 
	}
	
	@Override
	public ResultSet getLibraryContents(Library library) {
	   	LibraryQuery query = new LibraryQuery(getIndex(), library);
		ResultSet result = query.execute();
		return result; 
	}

	@Override
	public List<Notification> getMyNotifications(int limit) {
		return  getContentDao().getAlertNotifications(getUser(), limit);
	}

	@Override
	public long getTotalCountMyNotifications() {
		return  getContentDao().getTotalCountNotifications(getUser());
	}
	
	@Override
	public ResultSet getProgressNotes( int max) {
		ProgressNotesQuery2 query = new ProgressNotesQuery2(getIndex());
		ResultSet result = query.execute();
		return result; 
	}
	
    public void setPortalDao(PortalDao dao) {
        portalDao = dao;
    }
    
    public PortalDao getPortalDao() {
		return portalDao;
	}
    
    public ContentDao getContentDao() {
        return contentDao;
    }

    public void setContentDao(ContentDao dao) {
        contentDao = dao;
    }

	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
    protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
    
    protected Domain getDomain() {
			return ServiceLocator.getService(UserService.class).getDomain();
	}

	@Override
	public boolean listen(Event event) {
	    if (event instanceof EvictCacheServiceEvent)
            return true;
        return false;
	}

	@Override
	public void onEvent(Event event) {
		sites = null;
	}
}
