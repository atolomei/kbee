package com.novamens.event;



import java.time.OffsetDateTime;


import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;



/**
 * Logs 
 * 
 *  ObjectEvent
 *  -----------
 *  
 *     {@link ContentEvent}
 *     		{@link AssignationEvent}  (Re Assign)
 *     		{@link CheckinEvent}}	
 *     		{@link CheckoutEvent}
 *     		{@link CreationEvent}
 *     		{@link DropCheckoutEvent}
 *     		{@link NotificationEvent}
 *     		{@link SendByEmailEvent}
 *     		{@link AppUpdateEvent}
 *     		{@link WorkflowEvent}
 *     
 *     {@link DataSetMemberEvent} (CRUD. DataSetMember, UserLabel)
 *     
 *     {@link ModelEvent}
 *     		{@link ModelUpdateEvent} (CRUD. DataSetMember, UserLabel, DataSet, Classifier,ContentTemplate, Domain)
 *
 *     {@link SecurityEvent}   [CreateUser, CreateGroup, CreateRule, DeleteUser, AddGroups to Person]
 *         	{@link LoginEvent}
 *          {@link LogOutEvent}
 *          {@link RuleUpdateEvent} (ENoti Rules, para SecurityRule se usa SecurityEvent) 
 *          {@link UserUpdateEvent} (UpdateUser enable/disable, change password, update Person, UserProfile) 
 *           
 *     {@link ResourceEvent}
 *      	{@link CreateResourceEvent}
 *      	{@link RemoveResourceEvent}
 *
 *     ---------------------------------------
 *     
 *     {@link SiteEvent}
 *     		{@link SiteCreationEvent} Create Site  
 *  		{@link SiteDeleteEvent} Delete Site
 *  		{@link SiteUpdateEvent} Update Site Structure

 *           
 *     ---------------------------------------
 *           
 *     {@link EmptyRecycleBinEvent}

 *  
 *  SystemEvent  (no se usa)
 *  -----------
 *  
 *  SendEmailEvent
 *  --------------  
 *  
 *  
 *  {@link SiteStatEvent} 
 *  
 *  	SiteObject in (Page Render)
 *  	SiteObject out (click on ouit going link)
 *  
 *  
 *  FALTA 
 *  -----
 *  . ADD ACL USER
 *  . public void save(Acl acl)throws ContentMgmtException 
 *    
 *  
 *  
 */
public interface LogEvent extends Identifiable {

	public Long getId();

	public AuditSet getAuditSet();
	
	public boolean isSilentMode();
	
	/** Who triggers the event
	 *  ---------------------- 
	 * Sends de Content
	 * Downloads the export
	 * Completes a Task
	 * */
	public User getEventUser();
	
	/** Timestamp */
	public OffsetDateTime getTime();
	
	public String getTarget();
	
	/** See Event Taxonomy  */
	@Deprecated
	public String getEventType();

	/** Wil Replace getEventType. We keep 2 for compatibility  */
	public String getType();

	/** This is also subtype */
	public String getObjectClass();
	
	/** Action */
	public String getAction();
	
	/** Description */
	public String getTitle();
	public String getDescription();
	public String getParameters();

	public Long getAuditResourceKBFileId();
	
	public boolean isNotifiable();
}