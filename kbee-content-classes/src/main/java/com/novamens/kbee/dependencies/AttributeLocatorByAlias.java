package com.novamens.kbee.dependencies;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.DataSet;
import com.novamens.service.ServiceLocator;

import java.io.Serializable;
import java.util.List;

public class AttributeLocatorByAlias implements ObjectLocator, Serializable {
    String alias;
    private Long domainId;

    public AttributeLocatorByAlias(String alias, Long domainId) {
        this.alias = alias;
        this.domainId = domainId;
    }

    @Override
    public Object resolveObject() {
        List<Attribute> attributes = getContentDao().getAttributes(alias, domainId);
        if(!attributes.isEmpty()){
            return attributes.get(0);
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
    public ObjectLocator.TargetObjectType getTargetObjectType() {
        return TargetObjectType.ATTRIBUTE;
    }

    private ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

    @Override
    public String getDescription() {
        return "Attribute with alias '" + alias + "' in current domain.";
    }
}
