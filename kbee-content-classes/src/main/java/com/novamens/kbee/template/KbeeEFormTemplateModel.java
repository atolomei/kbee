package com.novamens.kbee.template;

import freemarker.template.*;
import java.util.Set;

import com.novamens.content.base.Resource;
import com.novamens.content.form.EForm;
import com.novamens.content.model.ContentTemplate;

import java.util.HashSet;
import java.util.List;

public class KbeeEFormTemplateModel extends KbeeObjectTemplateModel {
	
	private EForm eform;
	private String nodename = "form";
	
	public KbeeEFormTemplateModel(EForm eform) {
		setEform(eform);
	}
	
	public KbeeEFormTemplateModel(EForm eform, String nodename) {
		setEform(eform);
		this.nodename = nodename;
	}
	
	public EForm getEform() {
		return eform;
	}

	public void setEform(EForm eform) {
		this.eform = eform;
	}

	@Override
	public Object getObject() {
		return eform;
	}
	
	public TemplateModel get(String key) throws TemplateModelException {
		TemplateModel model = super.get(key);
 
		return model;
	}


	
	@Override
	public String getNodeName() throws TemplateModelException {
		return nodename;
	}
	
	@Override
	public String getNodeType() throws TemplateModelException {
		return "eform";
	}
	
	@Override
	public String getAsString() {
		return getEform().getName();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected TemplateModel wrap(KbeeMethod canonical, Object value) {
		TemplateModel model = null;
		if (value!=null && value instanceof ContentTemplate) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("Name", "Name"));
			model = new KbeeObjectWrapperTemplateModel(value, "ContentTemplate", canonicals, this);
		}
		else 
		if (value!=null && value instanceof List<?>) {
			List<Object> values = (List<Object>)value;
			if (!values.isEmpty() && values.get(0) instanceof Resource) {
				Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
				canonicals.add(new KbeeMethod("Name", "Name"));
				canonicals.add(new KbeeMethod("Title", "Title"));
				canonicals.add(new KbeeMethod("Size", "Size"));
				canonicals.add(new KbeeMethod("Url", "Url"));
				model = new KbeeListWrapperTemplateModel(values, "Resource", canonicals, this);
			}
		}
		else {
			model = super.wrap(canonical, value);
		}
		return model;
	}
	
	protected List<TemplateModel> getChilds() {
		List<TemplateModel> childs = super.getChilds();
//
//		cada field
//			si es lista EFormComponent
//			
//			si es fijo
//			
//			si es classifier
			
			
		return childs;
	}

	@Override
	protected Set<KbeeMethod> getCanonicals() {
		Set<KbeeMethod> canonicals = super.getCanonicals();
		canonicals.add(new KbeeMethod("name", "name"));
//		canonicals.add(new KbeeMethod("ContentTemplate", "ContentTemplate"));
//		canonicals.add(new KbeeMethod("domain", "Domain"));
//		canonicals.add(new KbeeMethod("resource", "Resources"));
//		canonicals.add(new KbeeMethod("OId", "OId"));
//		canonicals.add(new KbeeMethod("lastModifiedTime", "lastModifiedOffsetDateTime"));
//		canonicals.add(new KbeeMethod("publicurl") {
//			public Object evaluate(Object object) {
//				return getContent().getService(UrlService.class).getPublicUrl();
//			}
//		});
//		canonicals.add(new KbeeMethod("taskurl") {
//			public Object evaluate(Object object) {
//				return getContent().getService(UrlService.class).getTaskUrl();
//			}
//		});
//
//		canonicals.add(new KbeeMethod("subtitle") {
//			public Object evaluate(Object object) {
//				return getContent().getService(ContentService.class).getConsoleSubtitle();
//			}
//		});
		
		return canonicals;
	}
	
	public TemplateCollectionModel values() throws TemplateModelException {
		Set<TemplateModel> keys = new HashSet<TemplateModel>();
		return new SimpleCollection(keys, null);
	}
	
	@Override
	protected Set<String> keysSet() throws TemplateModelException {
		Set<String> keys = super.keysSet();
		return keys;
	}
}
