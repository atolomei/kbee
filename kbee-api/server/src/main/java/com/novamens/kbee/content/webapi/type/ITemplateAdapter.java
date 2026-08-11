package com.novamens.kbee.content.webapi.type;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EIdentifiableForm;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.model.KbeeExtractionMacro;
import com.novamens.workflow.Procedure;

import kbee.api.model.ApiProxy;
import kbee.api.model.IModelElement;
import kbee.api.model.ITemplate;

public class ITemplateAdapter implements Adapter<ContentTemplate, ITemplate> {
	
	public ITemplateAdapter() {
	}
	
	public ITemplate adapt(ContentTemplate template) {
		
		ITemplate itemplate = new ITemplate();
		itemplate.setDisplayName(template.getDisplayName());
		itemplate.setDomain(template.getDomain().getName());
		itemplate.setState(template.getState().name());
		itemplate.setId(String.valueOf(template.getId()));
		if (template.getTitleRule() instanceof KbeeExtractionMacro) {
			itemplate.setTitleRule(((KbeeExtractionMacro)template.getTitleRule()).getMacro());
		}
		itemplate.setPortalSubline(template.getPortalsSubtitleRule());
		itemplate.setConsoleSubline(template.getConsoleSubtitleRule());
		if (template.getTitleRule() instanceof KbeeExtractionMacro) {
			itemplate.setTitleRule(((KbeeExtractionMacro)template.getTitleRule()).getMacro());
		}
		itemplate.setTitleEditable(template.isTitleEditable());
		itemplate.setOnlyRoot(template.isOnlyRootEdit());
		itemplate.setBaseClass(template.getContentClass().getDisplayName());
		itemplate.setLastModifiedDate(template.getLastModifiedOffsetDateTime());
		
		List<ModelElementTemplate> elements = new ArrayList<ModelElementTemplate>();
		elements.addAll(template.getClassifiers());
		elements.addAll(template.getAttributes());
		for (ModelElementTemplate elementtemplate : elements) {
			if (elementtemplate!=null) {
				IModelElement element = new IModelElement();
				element.setAttribute(getProxy(elementtemplate.getElement()));
				element.setParent(elementtemplate.getParent()!=null ? getProxy(elementtemplate.getParent()) : null);
				element.setMutiplicity(elementtemplate.getMultiplicity()!=null ? elementtemplate.getMultiplicity().name() : null);
				itemplate.addStructure(element);
			}
		}
		
		for (Procedure procedure : template.getProcedures()) {
			itemplate.addProcedure(new ApiProxy(String.valueOf(procedure.getId()), procedure.getName(), UriHelper.getUri(procedure), "procedure"));
			//ilauncher.setProcedure(new IProxy(String.valueOf(launcher.getProcedure().getId()), launcher.getProcedure().getName(), UriHelper.getUri(launcher.getProcedure()), "procedure"));
		}
		
		for (EForm eform : template.getForms()) {
			if (eform!=null) {
				itemplate.addForm(new ApiProxy(String.valueOf(((EIdentifiableForm)eform).getId()), eform.getName(), UriHelper.getUri(eform), "eform"));
			}
		}
		
		for (ResourceTag tag : template.getResourceTags()) {
			itemplate.addResourceTag(new ApiProxy(String.valueOf(((KbeeResourceTag)tag).getId()), tag.getName(), UriHelper.getUri(tag), "resourcetag"));
		}
		
		return itemplate;	
	}
	
	public ApiProxy getProxy(ModelElement element) {
		ApiProxy proxy = new ApiProxy(UriHelper.getUri(element));
		proxy.setId(String.valueOf(element.getId()));
		proxy.setRel(element instanceof Classifier ? "classifier" : "attribute");
		proxy.setName(element.getAlias());
		return proxy;
	}
}   