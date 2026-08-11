package com.novamens.kbee.content.service;

import java.io.Serializable;
import java.util.List;


import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentId;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ModelObject;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectID;
import com.novamens.service.ServiceLocator;

public class DataAccessServiceImpl implements DataAccessService {
				
//	private static com.novamens.logging.Logger logger = com.novamens.logging.Logger.getLogger(DataAccessServiceImpl.class.getName());
	
	private ContentDao dao;
	
	public DataAccessServiceImpl() {
	}

	@Override
	public Content findContentById(ObjectID id) {
		return getDao().findContentById(id.getClassName(), id.getId());
	}

	@Override
	public Content findContentById(ContentId id) {
		return getDao().findContentById(id);
	}

	@Override
	public Content findContentById(Class<? extends Content> clazz, Serializable id) {
		return getDao().findContentById(clazz,id);
	}

	@Override
	public Resource findResourceById(Class<? extends Resource> clazz, Serializable id) {
		return getDao().findResourceById(clazz,id);
	}

	@Override
	public ModelObject findModelObjectById(Class<? extends ModelObject> clazz, Serializable id) {
		return getDao().findModelObjectById(clazz, id); 
	}
 

	@Override
	public Content findContentByName(Class<? extends Content> clazz, String name, Serializable domainid) {
		return findContentByName(clazz, name, domainid);
	}

	@Override
	public Resource findResourceByName(Class<? extends Resource> clazz, String name, Serializable domainid) {
		return getDao().findResourceByName(clazz, name, domainid);
	}											

	@Override
	public ModelObject findModelObjectByName(Class<? extends ModelObject> clazz, String name,Serializable domainid) {
		return getDao().findModelObjectByName(clazz,name,domainid);
	}

	@Override
	public ModelObject findModelObjectByName(Class<? extends ModelObject> clazz, ModelObject type, String name) {
		return getDao().findModelObjectByName(clazz, type,name);
	}

	@Override
	public Domain findDomainByName(String name) {
		return getDao().findDomainByName(name); 
	}

	
	public Domain getDomain() {
		return getDao().getDomain(); 
	}

	
	public List<DataSet> getDataSet(Serializable domainid) {
		return getDao().getDataSets(domainid);
	}

	
	public List<Classifier> getClassifier(Serializable domainid) {
		return getDao().getClassifiers(domainid);
	}

	
	public List<? extends Content> getContent(Class<? extends Content> clazz, Serializable domainid) {
		return getDao().getContent(clazz, domainid);
	}
	
	public ContentDao getDao() {
		if (dao==null) {
			BeansService beans = ServiceLocator.getService(BeansService.class);
			dao = (ContentDao) beans.getBean("contentDao");
		}
		return dao;
	}
}