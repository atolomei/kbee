package com.novamens.kbee.dependencies;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;

import com.novamens.service.ServiceLocator;

import java.io.Serializable;
import java.util.List;

public class DataSetLocatorByAlias implements ObjectLocator, Serializable {

	private static final long serialVersionUID = 1L;
	
	String alias;
    private Long domainId;

    public DataSetLocatorByAlias(String alias, Long domainId) {
        this.alias = alias;
        this.domainId = domainId;
    }

    @Override
    public Object resolveObject() {
        List<DataSet> dataSets = getContentDao().getDataSets(alias, domainId);
        if(!dataSets.isEmpty()){
            return dataSets.get(0);
        }
        return null;
    }

    @Override
    public String resolveId() {
        DataSet ds = (DataSet) resolveObject();
        if(ds != null){
            return ds.getId().toString();
        }
        return null;
    }

    @Override
    public TargetObjectType getTargetObjectType() {
        return TargetObjectType.DATASET;
    }

    private ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

    @Override
    public String getDescription() {
        return "Dataset with alias '" + alias + "' in current domain.";
    }
}
