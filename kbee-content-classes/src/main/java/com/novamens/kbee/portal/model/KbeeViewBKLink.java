package com.novamens.kbee.portal.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import com.novamens.dom.Indexable;
import com.novamens.portal6.model.ViewBKLink;

/**
 * update contentclass set javaclass ='com.novamens.kbee.portal.model.KbeeViewBKLink' where javaclass='com.novamens.kbee.portal.model.publish.KbeeViewBKLink'
 */
@Entity
@Table(name = "PO_VIEWBKLINK")
@PrimaryKeyJoinColumn(name = "view_id")
@DynamicInsert
public class KbeeViewBKLink extends KbeeViewBK implements ViewBKLink, Indexable {

	@Column(name = "link")
	private String link;

	public KbeeViewBKLink() {
	}

	public KbeeViewBKLink(String title, String link) {
		this.link = link;
		setName(title);
		setTitle(title);
	}

	public KbeeViewBKLink(String title, String link, String subtitle) {
			this(title, link, subtitle, null);
	}
	
	public KbeeViewBKLink(String title, String link, String subtitle, String description) {
		this.link = link;
		setSubtitle(subtitle);
		setDescription(description);
		setTitle(title);
		setName(title);
	}

	@Override
	public KbeeViewBKLink clone() {
		KbeeViewBKLink clone = new KbeeViewBKLink();
		onClone(clone);
		return clone;
	}

	public void onClone(KbeeViewBKLink clone) {
		super.onClone((KbeeViewBK) clone);
		clone.setLink(this.getLink());
		
	}
 
	public String getSubtitle() {
		
		if (super.getSubtitle()!=null)
			return super.getSubtitle();
		
		return this.getLink();
		
		
		
	}
	@Override
	public String getLink() {
		return link;
	}

	@Override
	public Object getObject() {
		return link;
	}

	@Override
	public String getTitle() {
		if (super.getTitle() == null)
			return getLink();
		else
			return super.getTitle();
	}

	@Override
	public void setLink(String link) {
		this.link = link;
		if (getName() == null)
			setName(link);
	}

	@Override
	public String getViewType() {
		return KbeeViewBK.LINK_TYPE;
	}

	@Override
	public String getMetadataAsString() {
		StringBuilder str = new StringBuilder();
		 
			str.append("Link. ");

		str.append(getLastModifiedUser() != null ? getLastModifiedUser().getFirstLastName() + ". " : "");
		str.append(getLastModifiedOffsetDateTimeColloquial());
		return str.toString();
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append("\n" + getViewType());
		str.append("\nLink: " + getLink() != null ? getLink() : "");
		return str.toString();
	}

	 
	@Override
	public boolean isSearchable() {
		// si es acceso rapido. no
		// si es link a kbee no
		// si es link externo si.
		if (getLink() != null) {
			String link = getLink();
			boolean not_idex = link.startsWith("localhost") || link.startsWith("http://localhost")
					|| link.startsWith("https://localhost")
					|| link.startsWith(PortalUriHelper.getInstance().getPortalURL(getDomain().getName()));
			return !not_idex;
		}
		return false;
	}

 

}
