package com.novamens.content.notification;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum NotificationType implements PersistentEnum {
	
	SYSTEM 					(10, "system"	, "system"), // ??
	
	WORK_NOTE				(20, "work-note", "work-note"), // alert
	
	WORK_NOTE_BILLBOARD		(22, "work-note-billboard", "work-note-billboard"), // bullhorn
	
	WORKFLOW 				(30, "workflow"	, "workflow"), // coffee
	
	PROGRESS_NOTE 			(35, "progress-note", "progress-note"), // coffee
	
	CONTENT 				(40, "content"	, "content"), // library (self service rule, system rule, ?
	
	CONTENT_AUDIT			(45, "content auditable"	, "content"), // not delete on accept
	
	CONDITION 				(50, "condition", "content"); // clock - timed action rule
		
	private String label;
	private int id;
	private String css;
				
	private NotificationType(int code, String label, String css) {
		this.label = label;
		this.id = code; 
		this.css = css;
	}
	
	public String toString() {
		return ("id: " + getId() + ". label: "+ getLabel()) + ". css: "+getCss();
	}

	public String getDisplayName() {
		return getLabel();
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public String getCss()	{
		return css;
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(NotificationType.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() {
		return id;
	}

	public String getHTMLLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(NotificationType.this.getClass().getName(), locale);
		return  "<span class=\"" + getCss() + "\">" + res.getString(this.label) + "</span>";
	}
}