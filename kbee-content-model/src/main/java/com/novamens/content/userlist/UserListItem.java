package com.novamens.content.userlist;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetMember;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.dom.Object;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;

public interface UserListItem extends DomainObject, Object, Indexable, Classificable {

	static final int CONTENT=1;
	static final int DATASETMEMBER=2;
	static final int USER=3;
	
	static final int PUBLISHED=0; 			// library
	static final int NEWEST=1; 				// monitor
	static final int SAVED_VERSION=2; 		// for recycle
								
	static final int ARCHIVED=3; 			// Archive


	public List<Classification> getClassification();
	
	/**
	 * title should contain the user/content/member in lowercase 
	 * this is used to sort in the database
	 */
	public String getTitle();
	
	/**
	 * One of these 3 is used for each Item
	 * @return
	 */
	public Content getContent();
	public DataSetMember getDataSetMember();
	public User getItemUser();
	public java.lang.Object getObject();

	/**
	 * CONTENT
	 * DATASETMEMEBER
	 * USER
	 */
	public int getUserListItemType();

	
	/** PUBLISHED version (Library)
	 *  EXACT VERSION saved (for Recycle bin)
	 *  NEWEST version (Monitor, mytasks)
	 */
	
	public int getVersionMatch();
	public String getObjectId();
	
	public void add(com.novamens.dom.Object obj);
	public boolean holds(com.novamens.dom.Object obj);
	
	public UserList getUserlist();
	
	
	public User getOwner();
	public String getConsole();
	
	public default AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}

	
	public Site getSite();
	
}
