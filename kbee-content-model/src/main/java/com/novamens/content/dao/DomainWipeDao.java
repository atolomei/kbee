package com.novamens.content.dao;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.dao.Dao;
import com.novamens.dom.Domain;



public interface DomainWipeDao extends Dao {

	
	
	
	// Logs
	public void deleteAllLogEvent(Domain domain) throws ContentMgmtException;
	public void deleteAllNotification(Domain domain) throws ContentMgmtException;

	// Social
	public void deleteAllVote(Domain domain) throws ContentMgmtException;
	public void deleteAllComment(Domain domain) throws ContentMgmtException;
	
	// Suscription
	public void deleteAllSuscription(Domain domain) throws ContentMgmtException;

	
	// Resources
	public void deleteAllResources(Domain domain) throws ContentMgmtException;
	
	// Contents
	public void deleteAllContents(Domain domain) throws ContentMgmtException;

	// Dataset Values
	// Labels are Cascade with User
	
	public void deleteAllDatasetMembers(Domain domain) throws ContentMgmtException;
	
	// Model
	public void deleteAllContentClasses(Domain domain) throws ContentMgmtException;
	public void deleteAllClassifiers(Domain domain) throws ContentMgmtException;
	public void deleteAllDatasets(Domain domain) throws ContentMgmtException;
	
	// Security
	// User -> Preferences, Labels, ENotiRule
	//
	public void deleteAllRules(Domain domain) throws ContentMgmtException;
	public void deleteAllGroups(Domain domain) throws ContentMgmtException;
	public void deleteAllUsers(Domain domain) throws ContentMgmtException;

	// Domain
	public void deleteDomain(Domain domain) throws ContentMgmtException;

	void deleteAllReport(Domain domain) throws ContentMgmtException;


	public String getSchema();
	public void setSchema(String schema);


	
}
