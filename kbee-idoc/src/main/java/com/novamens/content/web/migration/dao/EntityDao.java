package com.novamens.content.web.migration.dao;

import java.io.IOException;
import java.util.List;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.web.migration.model.EntityMatching;
import com.novamens.dao.Dao;

public interface EntityDao  extends Dao {
	
	public void save(EntityMatching entity) throws IOException;
	
	public EntityMatching findEntityById(String id, String url) throws ContentMgmtException;
	public List<EntityMatching> findEntitiesByClass(String className) ;
}
