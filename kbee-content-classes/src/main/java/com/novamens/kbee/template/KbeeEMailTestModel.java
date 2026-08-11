package com.novamens.kbee.template;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class KbeeEMailTestModel extends KbeeEMailTemplateModel {
	
	
	public KbeeEMailTestModel() {
		super();
	}
	
	@Override
	public Object getObject() {
		return this;
	}
	
	public TemplateModel get(String key) throws TemplateModelException {
		TemplateModel model = null;
		if (getModels().containsKey(key)) {
			model = wrap(new KbeeMethod(key, null), getModels().get(key));
		}
		else
		if (getMacros().containsKey(key)) {
			model = new KbeeCanonicalTemplateModel(key, getMacros().get(key));
		}
		else {
			model = super.get(key);
		}
		return model;
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}