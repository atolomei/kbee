package kbee.replica;

import java.io.IOException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.event.AppDeleteEvent;
import com.novamens.event.Event;
import com.novamens.kbee.domain.KbeeReplica;
import com.novamens.kbee.idoc.webapi.client.KbeeApiService;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiObject;
import kbee.api.service.ApiService;
import kbee.util.logging.Logger;

@Entity
@DiscriminatorValue(value="2")
public class KbeeReplicaStandBy extends KbeeReplica implements ReplicaStandBy {

    static private Logger logger = Logger.getLogger(KbeeReplica.class.getName());
	
	private transient LocalMatcher localMatcher;
	
	public KbeeReplicaStandBy() {
		setType(ReplicaType.STANDBY);
	}
	
	public Object getLocal(ApiObject object) {
		return getMatcher().getLocal(object);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getLocal(Class<T> localclass, ApiObject remote) {
		Long localid = getMatcher().getLocal(remote);
		if (localid!=null) {
			if (remote instanceof ApiFile)
				return (T)getContentDao().findLastVersion(localid);
			else 
				return getCurrentSession().get(localclass, localid);
		}
		return null;
	}
	
	public void setLocal(ApiObject remote, Identifiable local) throws IOException {
		getMatcher().setLocal(remote, local);
	}
	
	public LocalMatcher getMatcher() {
		if (localMatcher==null) {
			localMatcher = newMatcher();
		}
		return localMatcher;
	}
	
	public void handle(Event event) {
		try {
			if (event instanceof AppDeleteEvent) {
				if (event.getObject() instanceof Identifiable) {
					getMatcher().removeLocal((Identifiable)event.getObject());
				}
			}
		}
		catch (IOException e) {
			logger.error(e);
		}
	}
	
	@Override
	public ApiService getApi() {
		ApiService api = new KbeeApiService(getServer(), getUser(), getPassword());
		return api;
	}
	
	protected  LocalMatcher newMatcher() {
		return new KbeeReplicaLocalDao(this);
	}
	
	private Session getCurrentSession() {
		return getSessionFactory().getCurrentSession();	
	}
	
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
}