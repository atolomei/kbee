package com.novamens.solr.indexer.iql;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;

import com.novamens.dom.Domain;
import com.novamens.dom.Object;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceNotFoundException;

public class SolrProxy implements Object {
	private String id;
	
	public SolrProxy(String id) {
		setId(id);
	}
	
	public Serializable getId() {
		return id;
	};
	
	public void setId(Serializable id) {
		this.id = (String)id;
	};

	public String getName() {
		if (id!=null)
			return id.toString();
		return null;
	};
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return null;
	};

	public User getLastModifiedUser() {
		return null;
	}
	
	public Date getLastModifiedDate() {
		return null;
	}
											
	public void setLastModifiedDate(Date date) {
	}
	
	public void setLastModifiedUser(User user) {
	}
	
	public void setState(ObjectState enabled) {
	}
	
	public ObjectState getState() {
		return null;
	}
	
	public Domain getDomain() {
		return null;
	}
	
	public void setDomain(Domain domain) {
		
	}

	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return null;
	}

	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
	}

	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		return null;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		return null;
	}

	@Override
	public String getCreationOffsetDateTimeColloquial() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return getName();
	}

	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) {
	}

	@Override
	public void setDefaultAudit() {
	}
	
	public AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}

	
	public String getClassCode() {
		return SolrProxy.class.getSimpleName();
	}
}
