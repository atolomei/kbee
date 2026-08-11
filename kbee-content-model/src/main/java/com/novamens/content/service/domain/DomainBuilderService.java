package com.novamens.content.service.domain;

import java.util.Map;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Domain;
import com.novamens.service.FactoryService;
import com.novamens.service.SystemService;

public interface DomainBuilderService extends SystemService, FactoryService {

	public Domain createDomain(String name, Map<String, Object> parameters) throws ContentMgmtException, ContentCreationException;
	public Domain createEmptyDomain(String name, Map<String, Object> parameters) throws ContentMgmtException, ContentCreationException;

	public void setUpModelBasic(Domain domain) throws ContentMgmtException, ContentCreationException;
	public void setUpRolesBasic(Domain domain) throws ContentMgmtException, ContentCreationException;
	public void setUpUsersBasic(Domain domain, Map<String, Object> map) throws ContentMgmtException, ContentCreationException;

	public void setUpModelPremium(Domain domain,  String imodeltype) throws ContentMgmtException, ContentCreationException;
	public void setUpRolesPremium(Domain domain,  String imodeltype) throws ContentMgmtException, ContentCreationException;
	public void setUpUsersPremium(Domain domain, Map<String, Object> map, String imodeltype) throws ContentMgmtException, ContentCreationException;
	
	public void setUpExpress(Domain domain) throws ContentMgmtException, ContentCreationException;
}
