package kbee.replica;

import java.util.HashMap;
import java.util.Map;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.event.AppDeleteEvent;
import com.novamens.event.AppUpdateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.kbee.content.event.AppCheckinEvent;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiObject;
import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiValue;
import kbee.api.model.ApiClassifier;
import kbee.api.model.ApiDataSet;
import kbee.api.model.IForm;
import kbee.api.model.ILauncher;
import kbee.api.model.IModelAttribute;
import kbee.api.model.IPerson;
import kbee.api.model.ApiResource;
import kbee.api.model.IResourceTag;
import kbee.api.model.IRole;
import kbee.api.model.ITemplate;
import kbee.api.model.ApiUser;
import kbee.util.logging.Logger;

public class KbeeReplicaService implements ReplicaService, EventListener {
	
	private static Logger logger = Logger.getLogger(ReplicaService.class.getName());

	private Map<Domain, Boolean> replicas = new HashMap<>();

	public boolean listen(Event event) {
		return (event.getObject() instanceof DomainObject && event instanceof AppDeleteEvent) ||
			event instanceof AppUpdateEvent ||
			event instanceof AppCheckinEvent ||
			event instanceof EvictCacheServiceEvent;
	}
	
	public void onEvent(Event event) {
		try {
			if (event instanceof EvictCacheServiceEvent) {
				replicas.clear();
			}
			else
			if (event.getObject() instanceof DomainObject && replicas(((DomainObject)event.getObject()).getDomain())) {
				handle(event);
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public boolean replicas(Domain domain) {
		Boolean replicas = this.replicas.get(domain);
		if (replicas==null) {
			domain = (KbeeDomain)Proxy.Unproxy(domain);
			domain = (KbeeDomain)getContentDao().reload(domain);
			replicas = !((KbeeDomain)domain).getReplicas().isEmpty();
			this.replicas.put(domain, replicas);
		}
		return replicas;
	}
	
	public Object replicate(Replica replica, ApiObject object) throws ReplicaException {
		ReplicaHandler<?> handler = getHandler(replica, object);
		handler.replicate();
		return handler.getLocal();
	}
	
	private ReplicaHandler<?> getHandler(Replica replica, ApiObject object) {
		if (object instanceof ApiDataSet)
			return new DataSetReplicaHandler(replica, (ApiDataSet)object);
		if (object instanceof ApiClassifier)
			return new ClassifierReplicaHandler(replica, (ApiClassifier)object);
		if (object instanceof IModelAttribute)
			return new AttributeReplicaHandler(replica, (IModelAttribute)object);
		if (object instanceof ApiValue)
			return new DataSetMemberReplicaHandler(replica, (ApiValue)object);
		if (object instanceof IResourceTag)
			return new ResourceTagReplicaHandler(replica, (IResourceTag)object);
		if (object instanceof IForm)
			return new EFormReplicaHandler(replica, (IForm)object);
		if (object instanceof ApiProcedure)
			return new ProcedureReplicaHandler(replica, (ApiProcedure)object);
		if (object instanceof ILauncher)
			return new ProcessLauncherReplicaHandler(replica, (ILauncher)object);
		if (object instanceof ApiResource) {
			if ("file".equals(((ApiResource)object).getRel())) {
				return new KBFileReplicaHandler(replica, (ApiResource)object);
			}
		}	
		if (object instanceof ApiFile)
			return new ContentReplicaHandler(replica, (ApiFile)object);
		if (object instanceof ApiUser)
			return new UserReplicaHandler(replica, (ApiUser)object);
		if (object instanceof IRole)
			return new RoleReplicaHandler(replica, (IRole)object);
		if (object instanceof IPerson)
			return new PersonReplicaHandler(replica, (IPerson)object);
		if (object instanceof ITemplate)
			return new TemplateReplicaHandler(replica, (ITemplate)object);
		if (object instanceof ApiResource)
			return new KBFileReplicaHandler(replica, (ApiResource)object);
		return null;
	}
	
	private void handle(Event event) {
		KbeeDomain domain = (KbeeDomain)((DomainObject)event.getObject()).getDomain();
		domain = (KbeeDomain)getContentDao().reload(domain);
		for (Replica replica : domain.getReplicas()) {
			replica.handle(event);
		}
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}