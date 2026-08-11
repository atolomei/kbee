package com.novamens.kbee.content.event;

import javax.persistence.Entity;

import com.novamens.content.model.ObjectId;
import com.novamens.logging.AbstractObjectEvent;
import com.novamens.portal6.model.Site;
import com.novamens.security.audit.AuditSet;

/**
 * ----------------------------------------------------------------
 * 
 * Eventos de los Sitios
 * 
 * {@link SiteCreationEvent} Create Site {@link SiteDeleteEvent} Delete Site
 * {@link SiteUpdateEvent} Update Site Structure
 *
 * {@link PageCreationEvent} Create {@link PageDeleteEvent} Delete
 * {@link PageUpdateEvent} Update
 * 
 * {@link AreaCreationEvent} Create {@link AreaDeleteEvent} Delete
 * {@link AreaUpdateEvent} Update
 * 
 * {@link BlockCreationEvent} Create {@link BlockDeleteEvent} Delete
 * {@link BlockUpdateEvent} Update
 *
 * 
 * Publish ViewBK -> contenido en ContentId, Link externo UnPublish ViewBK
 *
 */
@Entity
public class SiteEvent extends AbstractObjectEvent {

	public SiteEvent() {
		setAuditSet(AuditSet.PORTAL);
	}

	public SiteEvent(Site site) {
		super();
		setAuditSet(AuditSet.PORTAL);
		setSite(site);
	}

	public void setSite(Object object) {
		if (object instanceof Site) {
			Site site = (Site) object;
			setObjectId((new ObjectId(site)).toString());
			setKbeeClass("Site");

			if (site.getDomain()!=null) {
				setDomain(site.getDomain());
				setDomainId((Long) (site.getDomain().getId()));
			}

			String title = site.getTitle();
			if ((title != null) && (title.length() > 255))
				title = title.substring(0, 252) + "...";
			setTitle(title);
		}
	}

	@Override
	public String getType() {
		return "Site";
	}

	@Override
	public String getAction() {
		return "Create";
	}

	@Override
	public String getObjectClass() {
		return "Portal Object";
	}

	@Override
	public String toString() {
		return getAction() + " | " + getTarget();
	}

}
