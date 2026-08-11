package com.novamens.kbee.content.service;

import java.io.Serializable;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.model.ContentId;
import com.novamens.content.model.ModelObject;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectID;
import com.novamens.service.SystemService;

public interface DataAccessService extends SystemService {

    public Content findContentById(ObjectID id);
	public Content findContentById(ContentId id);
	
	public Content			findContentById(Class<? extends Content> clazz, Serializable id);
	public Resource 		findResourceById(Class<? extends Resource> clazz, Serializable id);
	public ModelObject 		findModelObjectById(Class<? extends ModelObject> clazz, Serializable id);
	
	public Content			findContentByName(Class<? extends Content> clazz, String name, Serializable domainid);
	public Resource 		findResourceByName(Class<? extends Resource> clazz,  String name, Serializable domainid);
	public ModelObject 		findModelObjectByName(Class<? extends ModelObject> clazz,  String name, Serializable domainid);
	public ModelObject 		findModelObjectByName(Class<? extends ModelObject> clazz,  ModelObject type, String name);

	public Domain 			findDomainByName(String name);
	

	// SOLO PARA DEBUG. ELIMINAR. REVISAR
	//public Domain getDomain();
	//public List<DataSet> 		getDataSet(Serializable domainid); 
	//public List<Classifier> 	getClassifier(Serializable domainid);
	//public List<? extends Content> getContent(Class<? extends Content> clazz, Serializable domainid);
	
}
