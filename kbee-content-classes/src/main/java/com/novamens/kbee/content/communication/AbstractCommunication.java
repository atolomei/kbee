
package com.novamens.kbee.content.communication;


import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.content.base.KbeeResourceContainer;
@Deprecated
@MappedSuperclass
public class AbstractCommunication extends KbeeResourceContainer  {
	
	
	@Column(name = "SUBTITLE")
	private String 	subtitle;
	
	@Column(name = "CONTENTDATE")
 	private OffsetDateTime 	date;
	
	public AbstractCommunication() {
		super();
	}
		
	public AbstractCommunication(ContentTemplate ct) {
		super(ct);
	}
	
	public String getSubtitle() {
		return subtitle;
	}
	
	public OffsetDateTime getDate() {
		return date;
	}
			
	public void setSubtitle(String subtitle) {
		this.subtitle=subtitle;
	}
	
	public void setDate(OffsetDateTime date) {
		this.date=date;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append(super.toString());
		
		if (getSubtitle()!=null)
			str.append("\nsubtitle: " + subtitle);
		
		if (getDate()!=null)
			str.append("\ncontent date: " + date.toString());
		
		return str.toString();
	}
}
