package kbee.content.support;

import java.util.Map;

import com.novamens.content.resource.KBFile;
import com.novamens.dom.DomainObject;

import com.novamens.security.Auditable;
import com.novamens.security.Identifiable;
import com.novamens.security.User;


/**
 *  DELIVERY_STATUS_PENDING    =   0
 *  DELIVERY_STATUS_SENT	   =  200
 *  
 *  DELIVERY_STATUS_ERROR      =  400
 *  DELIVERY_STATUS_DOMAIN_NOT_ENABLED = 900
 * 
 * 
 */

public interface SupportTicket  extends Identifiable, Auditable, DomainObject, com.novamens.dom.Object {
	
	 final int  DELIVERY_STATUS_PENDING    =   0;
	 final int  DELIVERY_STATUS_SENT	   =  200;
	 final int  DELIVERY_STATUS_ERROR      =  400;
	 final int  DELIVERY_STATUS_DRAFT	   =  600;
	 
	 final int  DELIVERY_STATUS_DOMAIN_NOT_ENABLED = 900;
	 

	 
	public void setUser(User user);
	public User getUser();
	
	public String getSubject();
	public void setSubject(String subject);
	
	public String getText();
	public void setText(String text);
	
	public int getPriority();
	public void setPriority(int priority);
	
		
	public KBFile getKBFile();
	public void setKBFile(KBFile kfile);
	
	public int getDeliveryStatus();
	public void setDeliveryStatus(int deliverystatus);

	public String getDeliveryStatusMsg();
	public void setDeliveryStatusMsg(String deliverystatusmsg);
	
	
	public String getCreationOffsetDateTimeColloquial();
	public String getCreationOffsetDateTimeColloquialAgo();
	public String getCreationOffsetDateTimeColloquial(boolean ago);
	
	public String getLastModifiedOffsetDateTimeColloquialAgo();
	public String getLastModifiedOffsetDateTimeColloquial(boolean ago);

	public Map<String, String> getContext();
	public void setContext(Map<String, String> map);
	public int getErrorCount();
	public void setErrorCount(int error_count);
	
	
	
	
}
