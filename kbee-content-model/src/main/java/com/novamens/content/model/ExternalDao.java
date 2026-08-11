package com.novamens.content.model;

import java.io.Serializable;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.dao.Dao;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
 

/** ----------------------------------------------------
 *
 */
public interface ExternalDao extends Dao {
	
	
	public void resetCache();
	
	public void update(Serializable id, String value, String url, ObjectState state, DataSet dataset);
	public void delete(Serializable id, String value, DataSet dataset);
	
	public DataSetMember findMemberByExternalId(Serializable id);
	public DataSetMember findMemberByExternalId(Serializable id, DataSet dataset);
	
	public DataSet getSiteDataSet(Domain domain);
	public DataSet getSiteRepositoryDataSet(Domain domain);
	
	
	public Classifier getPortalHomeClassifier(Domain domain);
	

	// Site Classifier
	//
	public Classifier getSiteClassifier(Domain domain);
	public Classifier getSiteRepositoryClassifier(Domain domain);
	
	public ExternalMember create(Serializable external_id, String value, String url, ObjectState state, DataSet dataset);
	
	// Site DataSet
	// Site Repository
	// Site Classifier
	
	public DataSet createSiteDataSetIfNotExists(Domain domain) throws ContentMgmtException;
	public DataSet createSiteRespositoryDataSetIfNotExists(Domain domain) throws ContentMgmtException;

	public Classifier createSiteClassifierIfNotExists(Domain domain, DataSet dataset) throws ContentMgmtException;
	public Classifier createSiteRepositoryClassifierIfNotExists(Domain domain, DataSet dataset) throws ContentMgmtException;
	public Classifier createPortalHomeClassifierIfNotExists(Domain domain)  throws ContentMgmtException;
	
	
	
	
	
}
