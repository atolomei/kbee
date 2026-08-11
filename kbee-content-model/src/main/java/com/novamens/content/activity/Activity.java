package com.novamens.content.activity;

import java.util.Date;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.resource.KBFile;

@Deprecated
public interface Activity extends Content {

	public static final String ACTIVITY		 		= "acti";
	public static final String BASEACTIVITY			= "base";
	public static final String CAMPAIGN 			= "camp";
	public static final String CONFERENCE 			= "conf";
	public static final String SEMINAR 				= "semin";
	
	public void addFile(KBFile file);
	public void removeFile(KBFile file);
	public List<KBFile> getFiles();
	public void setFiles(List<KBFile> files);
	
	public String getSubtitle();
	public Date getDateFrom();
	public Date getDateTo();
	public Date getHourFrom();
	public Date getHourTo();
	public String getLocation();
	public String getSumary();
	public String getText();
			
	public void setSubtitle(String sub);
	public void setDateFrom(Date from);
	public void setDateTo(Date to);
	public void setHourFrom(Date fromh);
	public void setHourTo(Date tohour);
	public void setLocation(String location);
	public void setSumary(String summary);
	public void setText(String text);
	
}
