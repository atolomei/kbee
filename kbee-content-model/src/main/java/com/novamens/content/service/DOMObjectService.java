package com.novamens.content.service;


import java.util.List;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Object;
import com.novamens.event.LogEvent;
import com.novamens.service.BusinessObjectService;



/**
 *  <p>DOM Object CRUD
 * 
 *  delete, update,  etc
 *
 * AbstractObject
 * 
 * Classifier
 * DataSetMember
 * ContentTemplate
 * 
 * Person
 * Domain
 * </p>
 * 
 */
public interface DOMObjectService extends BusinessObjectService {

	final static public String _DELETED_ =" [ DELETED ]";
	
	public Object getObject();
	
	public void update(LogEvent logevent) 		throws ContentMgmtException;
	public void update(String part) 			throws ContentMgmtException;
	public void update(List<String> parts) 		throws ContentMgmtException;
	public void update() 						throws ContentMgmtException;
	public void delete() 						throws ContentMgmtException, ConstraintException;
	public void markAsDeleted() 				throws ContentMgmtException;
	public void restore() 						throws ContentMgmtException;

	void asyncDelete() 							throws ContentMgmtException;
	

}
