package com.novamens.kbee.content.activity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;


import com.novamens.content.activity.Activity;
import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.content.base.KbeeResourceContainer;

/**
 * 
 * 
 * 
 *
 */
@Deprecated
@Entity
@PrimaryKeyJoinColumn(name="content_id")
@Table(name = "Activity")
public class KbeeActivity extends KbeeResourceContainer implements Activity, Serializable {

	private static final long serialVersionUID = 8436721484740131934L;

	public static final String CLASS_CODE = "at";

	@Column(name = "SUBTITLE")
	private String 	subtitle;
	
	@Column(name = "contentdate")
 	private Date 	contentdate;
	
	@Column(name = "todate")
 	private Date 	todate;

	@Column(name = "fromhour")
 	private Date	fromhour;

	@Column(name = "tohour")
 	private Date	tohour;

	@Column(name = "location")
 	private String	location;
	
	@Column(name = "summary")
 	private String	summary;

	@Column(name = "ktext")
 	private String	text;
	
	public KbeeActivity() {
		super();
	}
		
	public KbeeActivity(ContentTemplate ct) {
		super(ct);
	}
	
	public String getSubtitle() {
		return subtitle;
	}
	
	public void setSubtitle(String subtitle) {
		this.subtitle=subtitle;
	}
	
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append(super.toString());
		
		if (getSubtitle()!=null)
			str.append("\nsubtitle: " + subtitle);
		
		return str.toString();
	}
	

	@Override
	public Date getDateFrom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDateTo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getHourFrom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getHourTo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSumary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDateFrom(Date from) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDateTo(Date to) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setHourFrom(Date fromh) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setHourTo(Date tohour) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setLocation(String location) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setSumary(String summary) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setText(String text) {
		// TODO Auto-generated method stub
	}
}
