package com.novamens.kbee.dependencies;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.service.ServiceLocator;

import java.io.Serializable;
import java.util.List;

public class DataSetClassifierLocatorByAlias implements ObjectLocator, Serializable {

    private ObjectLocator dsLocator;

    private String classifierAlias;

    public DataSetClassifierLocatorByAlias(ObjectLocator dsLocator, String classifierAlias) {

        this.dsLocator = dsLocator;
        this.classifierAlias = classifierAlias;
    }

    @Override
    public Object resolveObject() {
        DataSet ds = (DataSet) dsLocator.resolveObject();
        Classifier classifier = null;
        if(ds !=null) {
            classifier = ds.getClassifiers().stream().filter(clf -> classifierAlias.equals(clf.getAlias())).findFirst().orElse(null);
        }
        return classifier;
    }

    @Override
    public String resolveId() {
        Classifier classifier = (Classifier) resolveObject();
        String id = null;
        if(classifier != null)
            id = classifier.getId().toString();

        return id;
    }

    @Override
    public ObjectLocator.TargetObjectType getTargetObjectType() {
        return ObjectLocator.TargetObjectType.CLASSIFIER;
    }

    private ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

    @Override
    public String getDescription() {
        return "Classifier with alias '" + classifierAlias + "' on " + dsLocator.getDescription();
    }
}
