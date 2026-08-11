package kbee.replica;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.common.reflect.TypeToken;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.domain.KbeeReplica;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.Auditable;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.api.model.ApiObject;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiClassifier;
import kbee.api.model.ApiDataSet;
import kbee.api.model.IGroup;
import kbee.api.model.IModelAttribute;
import kbee.api.service.ApiService;

@SuppressWarnings("serial")
public class AbstractReplicaHandler<R extends ApiObject, L extends Identifiable> implements ReplicaHandler<R> {
	
	private Replica replica;
	private R object;
	private final TypeToken<L> typeToken = new TypeToken<L>(getClass()) { };
	private final Type type = typeToken.getType();
	private boolean forceUpdate = false;
	
	protected Logger logger = LogManager.getLogger("Migration");
	
	protected static String CLASSIFIER_REL = "classifier";
	protected static String ATTRIBUTE_REL = "attribute";
	
	public AbstractReplicaHandler(Replica replica, R object) {
		setReplica(replica);
		setObject(object);
	}
	
	public boolean replicate() throws ReplicaException {
		try {
			boolean replicated = false;
			L local = getLocal();
			if (replicable(local)) {
				if (local==null) {
					local = createLocal();
					setLocal(getObject(), local);
				}
				replicateIn(local);
				replicated = true;
			}
			return replicated;
		}
		catch (IOException e) {
			throw new ReplicaException(e);
		}
	}

	public R getObject() {
		return object;
	}

	public Replica getReplica() {
		return replica;
	}

	public void setReplica(Replica replica) {
		this.replica = replica;
	}

	public void setObject(R object) {
		this.object = object;
	}
	
	public boolean isForceUpdate() {
		return forceUpdate;
	}

	public void setForceUpdate(boolean forceUpdate) {
		this.forceUpdate = forceUpdate;
	}

	protected L createLocal() throws ReplicaException {
	    try {
	        return typeToken.constructor(typeToken.getRawType().getConstructor())
	            .invoke(null);
	    } 
	    catch (Exception e) {
	    	throw new KbeeRuntimeException(e);
	    }
	}
	
	@SuppressWarnings("unchecked")
	public L getLocal() {
		return (L)getLocal((Class<?>) type, getObject());
	}
	
	protected <T> T getLocal(Class<T> localclass, ApiObject remote) {
		return ((ReplicaStandBy)getReplica()).getLocal(localclass, remote);
	}
	
	protected void setLocal(ApiObject remote, Identifiable local) throws IOException {
		((ReplicaStandBy)getReplica()).setLocal(remote, local);
	}
	
	protected boolean replicable(L local) {
		OffsetDateTime localTime = local!=null ? ((Auditable)local).getLastModifiedOffsetDateTime() : null;
		OffsetDateTime remoteTime = getObject().getLastModifiedDate();
		return local==null || localTime==null || remoteTime==null || remoteTime.isAfter(localTime) || isForceUpdate();
	}
	
	protected void replicateIn(L local) throws ReplicaException {
	}
	
	protected void replicate(ApiObject object) throws ReplicaException {
		ServiceLocator.getService(ReplicaService.class).replicate(getReplica(), object);
	}
	
	protected <T> T replicated(Class<T> localclass, ApiObject remote) throws ReplicaException {
		ServiceLocator.getService(ReplicaService.class).replicate(getReplica(), remote);
		T local = getLocal(localclass, remote);
		return local;
	}
	
	protected DataSet getDataSet(ApiProxy proxy) throws ReplicaException {
		ApiDataSet remote = getReplicaApi().getDataSet(proxy.getId());
		DataSet local = "USER".equals(remote.getType()) ? getUserSet() : getLocal(KbeeDataSet.class, remote);
		if (local==null) {
			local = replicated(KbeeDataSet.class, remote);
		} 
		return local;
	}
	
	protected Classifier getClassifier(ApiProxy proxy) throws ReplicaException {
		ApiClassifier remote = getReplicaApi().getClassifier(proxy.getId());
		Classifier local = getLocal(KbeeClassifier.class, remote);
		if (local==null) {
			local = replicated(KbeeClassifier.class, remote);
		} 
		return local;
	}
	
	protected Attribute getAttribute(ApiProxy proxy) throws ReplicaException {
		IModelAttribute remote = getReplicaApi().getAttribute(proxy.getId());
		Attribute local = getLocal(KbeeAttribute.class, remote);
		if (local==null) {
			local = replicated(KbeeAttribute.class, remote);
		} 
		return local;
	}
	
	protected Group getLocalGroup(IGroup igroup) {
		if (igroup.isCanonical()) {
			for (Group group : getSecurityDao().getGroups(getSessionDomain())) {
				if (igroup.getName().equals(group.getName())) {
					return group;
				}
			}
		}
		else {
			return getLocal(KbeeGroup.class, igroup);
		}
		return null;
	}
	
	protected ApiService getReplicaApi() {
		return ((KbeeReplica)getReplica()).getApi();
	}
	
	protected void update(Object object) {
		getCurrentSession().save(object);
	}
	
	protected DataSet getUserSet() {
		return getContentDao().getUserSet();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected Domain getSessionDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private Session getCurrentSession() {
		return getSessionFactory().getCurrentSession();	
	}
	
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
}