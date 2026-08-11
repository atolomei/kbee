package com.novamens.kbee.content.webapi.type;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.CustomAttribute;
import com.novamens.content.base.Resource;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.resource.KBFileProxy;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.base.KbeeResourceContainer;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.ApiAttributeProxy;
import kbee.api.model.IAttributeValues;
import kbee.api.model.ApiResource;

public class IDocAdapter2 implements Adapter<KbeeContent, ApiFile> {
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public IDocAdapter2() {
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public ApiFile adapt(KbeeContent kbeeidoc) {
		ApiFile idoc = new ApiFile();
		idoc.setId(String.valueOf(kbeeidoc.getId()));
		idoc.setVersion(kbeeidoc.getVersion());
		idoc.setExternalId(kbeeidoc.getExternalId());
		idoc.setTitle(kbeeidoc.getTitle());
		idoc.setDomain(kbeeidoc.getDomain().getName());
		idoc.setApplication(getApplication(kbeeidoc.getDomain()));
		idoc.setClassName(kbeeidoc.getContentTemplate().getName());
		idoc.setContentClass(new ApiProxy(kbeeidoc.getContentTemplate().getDisplayName(), UriHelper.getUri(kbeeidoc.getContentTemplate())));

		List<IAttributeValues> values = new ArrayList<IAttributeValues>();
		
		for (Classification classification : kbeeidoc.getClassification()) {
			Classifier classifier = classification.getClassifier();
			
			ApiAttributeProxy attribute = new ApiAttributeProxy();
			attribute.setHRef(UriHelper.getUri(classifier));		
			attribute.setRel("classifier");
			attribute.setName(classifier.getName());
			
			DataSetMember member = classification.getDataSetMember();
			
			ApiValue value = new ApiValue();
			value.setId(String.valueOf(member.getId()));
			value.setValue(member.getDisplayName());
			
			values.add(new IAttributeValues(attribute, value));
		}
		
		
		for (AttributeTemplate template : kbeeidoc.getContentTemplate().getAttributes()) {
			for (String value : kbeeidoc.getAttributeValues(template.getAttribute())) {
				ApiAttributeProxy iattribute = new ApiAttributeProxy();
				iattribute.setHRef(UriHelper.getUri(template.getAttribute()));		
				iattribute.setRel("attribute");
				iattribute.setName(template.getAttribute().getName());
				
				ApiValue ivalue = new ApiValue();
				
				if (template.getAttribute().isDate()) {
					int t = value.indexOf("T");
					if (t>0) {
						value = value.substring(0, t);
					}				
				}
				
				ivalue.setValue(value);
				
				values.add(new IAttributeValues(iattribute, ivalue));
			}
		}
		
		idoc.setAttributes(values);
		
		for (CustomAttribute attribute : kbeeidoc.getUserDefinedAttributes()) {
			idoc.setCustomAttribute(attribute.getName(), attribute.getValue());
		}
		
		List<ApiResource> files = new ArrayList<ApiResource>();
		
		if (kbeeidoc instanceof KbeeResourceContainer) {
			for (Resource resource : ((KbeeResourceContainer)kbeeidoc).getResources()) {
				ApiResource file = new ApiResource();
				if (idoc.getExternalId()!=null)
					file.setHRef(UriHelper.getUri(idoc)+"/"+resource.getName());		
				else
					file.setHRef(UriHelper.getUri(kbeeidoc, resource));		
				file.setRel("file");
				file.setName(resource.getName());
				files.add(file);
				
				if (resource instanceof KBFileProxy) {
					file.setControlAttribute("url", ((KBFileProxy)resource).getUrl());
				}
			}
			idoc.setResources(files);
		}
		
		return idoc;	
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected String getApplication(Domain domain) {
		return domain.getService(DomainSettingsService.class).get("name", "application");
	}
}
