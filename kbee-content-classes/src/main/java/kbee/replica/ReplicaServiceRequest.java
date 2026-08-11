package kbee.replica;

import java.io.Serializable;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ObjectId;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.kbee.domain.KbeeReplica;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.service.ServiceLocator;

public class ReplicaServiceRequest extends AbstractServiceRequest {
	private static final long serialVersionUID = 1L;
	
	private ObjectId objectId;
	private Serializable replicaId;
	
	public ReplicaServiceRequest(Replica replica, Object object) {
		objectId = new ObjectId(object);
		replicaId = replica.getId();
	}
	
	@Override
	public void execute() {
		((MasterReplica)getReplica()).replicate(getObject());
	}
	
	public Object getObject() {
		Object object = getContentDao().findObjectById(objectId);
		return object;
	}
	
	public Replica getReplica() {
		Replica replica = getSessionFactory().getCurrentSession().load(KbeeReplica.class, replicaId);
		replica = (MasterReplica)Proxy.Unproxy(replica);
		return replica;
	}
	
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}	