package com.novamens.kbee.content.webapi.type;

import java.time.OffsetDateTime;

import com.novamens.content.base.Content;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.ContentService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;

import kbee.api.model.IActivityProxy;
import kbee.api.model.INote;

public class IActivityProxyAdapter implements Adapter<Content, IActivityProxy> {
	
	static final public String PROPERTY_UNREAD = "unread";
	
	public IActivityProxyAdapter() {
	}
	
	public IActivityProxy adapt(Content content) {
		IActivityProxy proxy = new IActivityProxy();
		KbeeWorkflowActivity activity = (KbeeWorkflowActivity)content.getService(WorkflowService.class).getActivity();
		if (activity==null) return null;
		
		proxy.setId(String.valueOf(activity.getId()));
		proxy.setRel("activity");
		proxy.setName(getTitle(activity.getContent()));
		proxy.setSubline(getSubline(content));
		proxy.setTask(activity.getTask().getDisplayName());
		proxy.setUser(new ApiUserProxy(activity.getUser()));
		proxy.setUnread(unread(activity.getContent()));
 		
		KbeeContext context = (KbeeContext)content.getService(WorkflowService.class).getContext();
		OffsetDateTime time = activity.getStartTime();
		String timestring = ServiceLocator.getService(DateTimeService.class).timeElapsed(time);
		proxy.setTime(timestring);
		proxy.setTime(time);
		Activity previous = context.getPreviousActivity();
		if (previous!=null && previous.getNote()!=null) {
			INote note = new INote();
			note.setText(previous.getNote());
			note.setTime(previous.getEndTime());
			note.setAuthor(new ApiUserProxy(previous.getUser()));
			proxy.setNote(note);
		}
		
		return proxy;	
	}
	
	
	private boolean unread(Content content) {
		String nr = (String) content.getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
		boolean isUnread= nr!=null && nr.equals("yes");
		return isUnread;
	}
	
	private String getTitle(Content content) {
		String title = content.getTitle();
		if (title==null) return "";
		title = title.replace("\r", "");
		title = title.replace("\n", "");
		return title;
	}
	
	private String getSubline(Content content) {
		String nr = (String) content.getService(ContentService.class).getConsoleSubtitle();
		return nr;
	}
}