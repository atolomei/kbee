package com.novamens.kbee.content.service;

import org.springframework.util.Assert;

import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.service.DataAccessService;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;

public class DataAccessServiceFactory extends AbstractServiceFactory<DataAccessService> {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DataAccessServiceFactory.class.getName());

	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(DataAccessService.class);
	}

	@SuppressWarnings("unchecked")
	public DataAccessService getService(Object object) {
		Assert.isTrue(object instanceof Classifier || object instanceof DataSet || object instanceof ClassifierTemplate, "invalid class");
		if (object instanceof Classifier) {
			Classifier classifier = (Classifier) object;
			return getService(classifier.getDataSet());
		}
		if (object instanceof DataSet) {
			return resolveDataSetService((DataSet) object);
		}
		if (object instanceof ClassifierTemplate) {
			return resolveTemplateService((ClassifierTemplate) object);
		}
		throw new IllegalArgumentException("Unsupported type: " + object.getClass());
	}

	private DataAccessService resolveDataSetService(DataSet dataSet) {
		AccessStrategy strategy = dataSet.getAccessStrategy();

		if (strategy == null) {
			logger.warn("DataSet " + dataSet.getName() + " has no AccessStrategy defined. Defaulting to AllAccessService.");
			return new KbeeAllAccessService(dataSet);
		}

		switch (strategy.getName()) {

		case "Roles":
			return new KbeeRolesAccessService(dataSet);
		case "Managed":
			return new KbeeManagedAccessService(dataSet);
		default:
			return new KbeeAllAccessService(dataSet);
		}

	}

	private DataAccessService resolveTemplateService(ClassifierTemplate template) {
		AccessStrategy strategy = template.getAccessibility();
		switch (strategy.getName()) {
		case "Roles":
			return new KbeeRolesAccessService(template);
		case "Managed":
			return new KbeeManagedAccessService(template);
		case "Iql":
			return new KbeeIqlAccessService(template);
		case "Writeables":
			return new KbeePermissionAccessService(template, KbeePermission.WRITE);
		case "ChildsEnabled":
			return new KbeePermissionAccessService(template, KbeePermission.CHILDS);
		default:
			return new KbeeAllAccessService(template);
		}
	}
}