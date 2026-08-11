package kbee.replica;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.beans.BeansService;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelObject;
import com.novamens.event.AppUpdateEvent;
import com.novamens.event.Event;
import com.novamens.kbee.content.event.AppCheckinEvent;
import com.novamens.kbee.domain.KbeeReplica;
import com.novamens.kbee.idoc.webapi.client.KbeeApiService;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.scheduler.ServiceRequest;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiObject;
import kbee.api.service.ApiSerializer;
import kbee.api.service.ApiService;
import kbee.util.logging.Logger;

@Entity
@DiscriminatorValue(value="1")
public class KbeeMasterReplica extends KbeeReplica implements MasterReplica {

	
    static private Logger logger = Logger.getLogger(KbeeReplica.class.getName());

	@Column(name = "standbyreplica_id")
	private String standByReplicaId;
		
	public KbeeMasterReplica() {
		setType(ReplicaType.MASTER);
	}
	
	public String getStandByReplicaId() {
		return standByReplicaId;
	}
	
	public void handle(Event event) {
		try {
			Object object = event.getObject();
			if (event instanceof AppUpdateEvent) {
				if (object instanceof ModelElement ||
					object instanceof ModelObject ||
					object instanceof User) {
					schedule(new ReplicaServiceRequest(this, object));
				}
			}
			else
			if (event instanceof AppCheckinEvent) {
				schedule(new ReplicaServiceRequest(this, object));
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public ApiService getApi() {
		ApiService api = new KbeeApiService(getServer(), getUser(), getPassword());
		return api;
	}

	public void setStandByReplicaId(String standByReplicaId) {
		this.standByReplicaId = standByReplicaId;
	}

	public void replicate(Object object) {
		getApi().replicate(serialize(object), getStandByReplicaId());
	}
	
	private ApiObject serialize(Object object) {
		return ((ApiSerializer)ServiceLocator.getService(BeansService.class).getBean("ApiSerializer")).serialize(object);
	}
	
	private void schedule(ServiceRequest request) throws SchedulerException {
		ServiceLocator.getService(SchedulerService.class).enqueue(request);
	}
}