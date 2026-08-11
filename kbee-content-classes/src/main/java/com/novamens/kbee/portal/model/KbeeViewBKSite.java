package com.novamens.kbee.portal.model;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.ViewBKSite;

/**
 * 
 * 
 */
@Entity
@Table(name = "PO_VIEWBKSITE")
@PrimaryKeyJoinColumn(name = "view_id")
@DynamicInsert
public class KbeeViewBKSite extends KbeeViewBK implements ViewBKSite {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeViewBKSite.class.getName());

	@ManyToOne(fetch = FetchType.EAGER, targetEntity = KbeeSite.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "site_id", nullable = true) // si borran el Site la View no se borra, queda apuntando a null.
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
	private Site site;

	@Override
	public String getMetadataAsString() {

		StringBuilder str = new StringBuilder();

		//str.append(getContentTypeAsString() + ". ");

		if (this.getSite() != null) {
			if (this.getSite().isExternal())
				str.append("Externo. ");
		}
		str.append(getLastModifiedUser() != null ? getLastModifiedUser().getFirstLastName() + ". " : "");
		str.append(getLastModifiedOffsetDateTimeColloquial());
		return str.toString();
	}

	@Override
	public KbeeViewBKSite clone() {
		KbeeViewBKSite clone = new KbeeViewBKSite();
		onClone(clone);
		return clone;
	}

	public void onClone(KbeeViewBKSite clone) {
		super.onClone((KbeeViewBK) clone);
		clone.setReferencedSite(this.getReferencedSite());
	}

	public KbeeViewBKSite() {
	}

	public KbeeViewBKSite(Site site) {
		setReferencedSite(site);
	}

	public KbeeViewBKSite(String title, Site site) {
		setTitle(title);
		setReferencedSite(site);
	}

	@Override
	public Site getReferencedSite() {
		return this.site;
	}

	@Override
	public void setReferencedSite(Site site) {
		this.site = site;
	}

	@Override
	public String getViewType() {
		return KbeeViewBK.SITE_TYPE;
	}

	

	/**
	@Override
	public WebPage getResponsePage() {
		if (getReferencedSite() == null)
			return null;
		if (getReferencedSite().isExternal())
			return new RedirectPage(getReferencedSite().getURI());
		else
			return new KBPWebPage(new ObjectModel<DiagrammableSite>(getReferencedSite()), null);
	}
	*/

	@Override
	public String getTitle() {
		if (super.getTitle() != null)
			return super.getTitle();
		if (site == null)
			return "[site deleted]";
		return site.getTitle();
	}

	@Override
	public Object getObject() {
		return site;
	}

	/**
	@Override
	public String getDescrip() {
		if (super.getMetadata() != null)
			return super.getMetadata();
		return getSite() != null ? getSite().getMetadataAsString() : "";
	}
	**/

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append("\n" + getViewType());
		str.append("\nReferenced Site: " + (getReferencedSite() != null ? getReferencedSite().getTitle() : ""));
		return str.toString();
	}

	@Override
	public boolean isSearchable() {
		return false;
	}
}
