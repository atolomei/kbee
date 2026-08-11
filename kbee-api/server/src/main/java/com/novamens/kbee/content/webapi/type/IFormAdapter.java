package com.novamens.kbee.content.webapi.type;

import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EAttributeModel;
import com.novamens.content.form.EClassifierModel;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EResourceModel;
import com.novamens.content.form.EResourceSystemModel;
import com.novamens.content.form.EText;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.form.EFormAbstractComponent;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.form.KbeeEStringModel;

import kbee.api.model.ApiProxy;
import kbee.api.model.IComponent;
import kbee.api.model.IForm;

public class IFormAdapter implements Adapter<EForm, IForm> {
	
	public IFormAdapter() {
	}
	
	public IForm adapt(EForm form) {
		
		KbeeEForm kbform = (KbeeEForm)form;
		
		IForm iform = new IForm();
		
		iform.setName(kbform.getName());
		iform.setDisplayName(kbform.getDisplayName());
		iform.setDomain(kbform.getDomain().getName());
		iform.setState(kbform.getState().name());
		iform.setFileContainer(kbform.isFileContainer());
		iform.setViewer(kbform.getViewer());
		iform.setDisplayLevel(kbform.getFormAccessLevel().name());
		iform.setId(String.valueOf(kbform.getId()));
		
		for (EFormComponent component : kbform.getComponents()) {
			iform.addComponent(getComponent(component)) ;
		}
		
		return iform;	
	}
	
	private IComponent getComponent(EFormComponent component) {
		IComponent icomponent = new IComponent();
		
		icomponent.setName(component.getName());
		icomponent.setLabel(component.getLabel());
		icomponent.setSublabel(component.getSublabel());
		icomponent.setType(((EFormAbstractComponent)component).getTypeLabel());
		icomponent.setCss(component.getCssClass());
		icomponent.setVisible(component.getVisibleCondition());
		icomponent.setEnabled(component.getEnabledCondition());
		
		if (component instanceof EFormField<?>) {
			EFieldModel<?> model =  ((EFormField<?>)component).getModel();
      
			if (model instanceof EClassifierModel) {
				Classifier classifier =  ((EClassifierModel<?>)model).getClassifier();
				icomponent.setClassifier(new ApiProxy(String.valueOf(classifier.getId()), classifier.getAlias(), UriHelper.getUri(classifier), "classifier"));
				icomponent.setModel("Classifier");
			}
			else
			if (model instanceof EAttributeModel) {
				Attribute attribute =  ((EAttributeModel<?>)model).getAttribute();
				icomponent.setAttribute(new ApiProxy(String.valueOf(attribute.getId()), attribute.getAlias(), UriHelper.getUri(attribute), "attribute"));
				icomponent.setModel("Attribute");
			}
			else
			if (model instanceof EResourceSystemModel) {
				ResourceTag tag =  ((EResourceModel<?>)model).getTag();
				icomponent.setResourceTag(new ApiProxy(String.valueOf(((KbeeResourceTag)tag).getId()), tag.getAlias(), UriHelper.getUri(tag), "resourcetag"));
				icomponent.setModel(EResourceSystemModel.GetTypeLabel());
			}
			else
			if (model instanceof EResourceModel) {
				ResourceTag tag =  ((EResourceModel<?>)model).getTag();
				icomponent.setResourceTag(new ApiProxy(String.valueOf(((KbeeResourceTag)tag).getId()), tag.getAlias(), UriHelper.getUri(tag), "resourcetag"));
				icomponent.setModel(EResourceModel.GetTypeLabel());
			}
			else 
			if (model instanceof KbeeEStringModel) {
				icomponent.setModel("Form Attribute");
			}
			else {
				model = null;
			}
			icomponent.setCalculation(((EFormField<?>)component).getCalculation());
		}
		
		if (component instanceof EFormContainer) {
			for (EFormComponent child : ((EFormContainer)component).getComponents()) {
				icomponent.addChild(getComponent(child)) ;
			}
		}
		
		if (component instanceof EText) {
			icomponent.setText(((EText)component).getText());
		}
		
		return icomponent;
	}
}