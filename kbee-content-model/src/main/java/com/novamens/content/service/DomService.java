package com.novamens.content.service;

import java.util.List;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.event.LogEvent;
import com.novamens.service.ObjectService;

/**
 *  Servicio para la gestión de Alta, Baja, Modificación de un Objeto DOM
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
 * 
 * Domain
 *
 * Los Objetos de tipo Content usan un servicio diferente
 * 
 */
public interface DomService extends ObjectService {
	public void update(LogEvent logevent) 		throws ContentMgmtException;
	public void update(String part) 			throws ContentMgmtException;
	public void update(List<String> parts) 		throws ContentMgmtException;
	public void update() 						throws ContentMgmtException;
	public void delete() 						throws ContentMgmtException, ConstraintException;
	public void markAsDeleted() 				throws ContentMgmtException;
	public void restore() 						throws ContentMgmtException;
}