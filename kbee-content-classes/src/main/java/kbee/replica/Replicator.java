package kbee.replica;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.kbee.domain.KbeeReplica;
import com.novamens.service.ServiceLocator;

import kbee.api.service.ApiService;

public abstract class Replicator<T> {
	
	private Replica replica;
	private int progress = 0, replicated = 0;
	
	protected Logger logger = LogManager.getLogger("Migration");
	
	private String typeName = null;
	private String errorMessage = null;
	
	public Replicator(Replica replica) {
		setReplica(replica);
	}
	
	public void replicate() throws ContentMgmtException  {
		int i=0;
		try {
			Iterator<T> iterator = getIterator();
			while (iterator.hasNext()) {
				T object = iterator.next();
				if (typeName == null && object!=null) {
					typeName = object.getClass().getSimpleName().toLowerCase();
				}
				if (getHandler(object).replicate() ) {
					this.replicated++;
				}	
				setProgress(i++);
			}
			getContentDao().flush();
		}
		catch (Throwable e) {
			logger.error(e);
			e.printStackTrace();
			errorMessage = e.getMessage();
			if (errorMessage==null) 
				errorMessage = e.getClass().getSimpleName();
			throw new ContentMgmtException(e);
		}
	}
	
	public Replica getReplica() {
		return replica;
	}

	public void setReplica(Replica replica) {
		this.replica = replica;
	}

	public long getTotal() {
		return 0;
	}

	public int getProgress() {
		return progress;
	}
	
	public int getReplicated() {
		return replicated;
	}
	
	protected void setProgress(int progress) {
		this.progress = progress;
	}
	
	protected abstract Iterator<T> getIterator();
	
	protected abstract ReplicaHandler<T> getHandler(T object);
	
	public String getResult() {
		String result;
		if (errorMessage == null) {
			result = "<p>"+String.valueOf(getTotal())+ " "+ (typeName!=null ? typeName : "objects") + " processed. ";
			result += String.valueOf(getReplicated())+" local updated</p>";
		}
		else {
			result = errorMessage;
		}
		return result;
	}
	
	protected ApiService getReplicaApi() {
		return ((KbeeReplica)getReplica()).getApi();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
