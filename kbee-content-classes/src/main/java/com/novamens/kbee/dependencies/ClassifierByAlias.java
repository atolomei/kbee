package com.novamens.kbee.dependencies;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.service.ServiceLocator;
import org.danekja.java.misc.serializable.SerializableCallable;

import java.io.Serializable;
import java.util.List;

public class ClassifierByAlias implements ObjectLocator, Serializable {
    private String classifierAlias;
    private Serializable domainId;

    public ClassifierByAlias(String classifierAlias, Serializable domainId) {
        this.classifierAlias = classifierAlias;
        this.domainId = domainId;
    }

    @Override
    public Object resolveObject() {
        final List<Classifier> classifiers = getContentDao().getClassifiers(this.classifierAlias, this.domainId);
        if(!classifiers.isEmpty()){
            return classifiers.get(0);
        }
        return null;
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
        return "Classifier with alias '" + classifierAlias;
    }
}