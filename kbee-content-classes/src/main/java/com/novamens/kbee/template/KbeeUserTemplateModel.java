package com.novamens.kbee.template;

import freemarker.template.*;
import java.util.Set;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;

public class KbeeUserTemplateModel extends KbeeValueTemplateModel {

	
	public KbeeUserTemplateModel(DataSetMember value) {
		super(value);
	}
	
	public KbeeUserTemplateModel(DataSetMember value, Classifier classifier, TemplateNodeModel parent) {
		super(value, classifier, parent);
	}
	
	@Override
	protected Set<KbeeMethod> getCanonicals() {
		Set<KbeeMethod> canonicals = super.getCanonicals();
		canonicals.add(new KbeeMethod("email", "Email"));
		canonicals.add(new KbeeMethod("firstName", "firstName"));
		canonicals.add(new KbeeMethod("lastName", "lastName"));
		canonicals.add(new KbeeMethod("firstlastname", "FirstLastName"));
		return canonicals;
	}
	
	

}
