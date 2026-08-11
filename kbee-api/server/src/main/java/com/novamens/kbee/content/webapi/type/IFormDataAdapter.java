package com.novamens.kbee.content.webapi.type;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EListField;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.UrlService;
import com.novamens.content.text.TextPart;
import com.novamens.kbee.content.form.EFormAbstractComponent;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.text.TextTemplate;
import com.novamens.thumbnail.ThumbnailSize;

import javassist.expr.Instanceof;
import kbee.api.model.IFieldData;
import kbee.api.model.IFieldValue;
import kbee.api.model.ITextPart;


public class IFormDataAdapter implements Adapter<EFormData, List<IFieldData>> {

	public IFormDataAdapter() {
	}

	public List<IFieldData> adapt(EFormData formdata) {
		List<IFieldData> data = new ArrayList<>();
		for (EFormField<?> field : formdata.getForm().getFields()) {
			if (isVisible(field, formdata)) {
				IFieldData fielddata = new IFieldData();
				fielddata.setName(field.getName());
				fielddata.setLabel(field.getLabel());
				fielddata.setType(((EFormAbstractComponent)field).getTypeLabel());
				fielddata.setValues(getValues(field, formdata.getData(field)));
				data.add(fielddata);
			}
		}
		return data;
	}
	
	private List<IFieldValue> getValues(EFormField<?> field, Object object) {
		List<IFieldValue> values = new ArrayList<>();
		if (object==null) return values;
		if (object instanceof List<?>) {
			for (Object value : (List<?>)object) {
				IFieldValue fieldValue = getValue(field, value);
				if (fieldValue!=null)
				values.add(fieldValue);
			}
		}
		else {
			IFieldValue fieldValue = getValue(field, object);
			if (fieldValue!=null)
			values.add(fieldValue);
		}
		return values;
	}
	
	private IFieldValue getValue(EFormField<?> field, Object object) {
		if (object instanceof DataSetMember) {
			IFieldValue value = new IFieldValue();
			value.setId(String.valueOf(((DataSetMember)object).getId()));
			value.setUri(String.valueOf(((DataSetMember)object).getId()));
			value.setDisplayName(((DataSetMember)object).getDisplayName());
			value.setSubline(getSubline(field, ((DataSetMember)object)));
			value.setType("member");
			return value;
		}
		if (object instanceof Resource) {
			IFieldValue value = new IFieldValue();
			value.setId(String.valueOf(((Resource)object).getId()));
			value.setDisplayName(((Resource)object).getName());
			String uri = ((Resource)object).getService(UrlService.class).getThumbnailPublicUrl(ThumbnailSize.MEDIUM);
			value.setThumbnailUri(uri);
			uri = ((Resource)object).getService(UrlService.class).getPublicUrl();
			value.setUri(uri);
			value.setContentType(getContentType((Resource)object));
			String type = null, parent = null;
			if (object instanceof ResourceNode) {
				ResourceNode node = (ResourceNode)object;
				Resource resource = node.getResource();
				type = resource instanceof ResourceFolder ? "resource folder" : "resource";
				if (node.getFolder()!=null) parent = String.valueOf(node.getFolder().getId());
			}
			else {
				type = object instanceof ResourceFolder ? "resource folder" : "resource";
			}
			value.setType(type);
			value.setParent(parent);
			value.setSize(((Resource)object).getSize());
			return value;
		}
		if (object instanceof OffsetDateTime) {
			IFieldValue value = new IFieldValue();
			value.setDisplayName(((Serializable)object).toString());
			value.setDate(((OffsetDateTime)object));
			value.setType("date");
			return value;
		}
		if (isHTML(object)) {
			IFieldValue value = new IFieldValue();
			value.setDisplayName(((Serializable)object).toString());
			value.setType("value");
			KbeeText text = new KbeeText((String)object);
			List<ITextPart> parts = new ArrayList<>();
			for (TextPart part : text.getParts()) {
				ITextPart ipart = new ITextPart();
				ipart.setName(part.getName());
				ipart.setTitle(part.getTitle());
				ipart.setLevel(part.getLevel());
				parts.add(ipart);
			}
			value.setParts(parts);
			return value;
		}	
		if (object instanceof OffsetDateTime) {
			IFieldValue value = new IFieldValue();
			value.setDisplayName(((Serializable)object).toString());
			value.setType("value");
			value.setDate((OffsetDateTime)object);
			return value;
		}
		if (object instanceof Serializable) {
			IFieldValue value = new IFieldValue();
			value.setDisplayName(((Serializable)object).toString());
			value.setType("value");
			return value;
		}
		return null;
	}
	
	private boolean isVisible(EFormField<?> field, EFormData data) {
		return field.isVisible(data);
	}
	
	private String getSubline(EFormField<?> field, DataSetMember value) {
		if (field instanceof EListField && ((EListField<?>)field).getInfoTemplate()!=null) {
			TextTemplate template = new KbeeTextTemplate(((EListField<?>)field).getInfoTemplate());
			String label = template.process(value);
			return label;
		}
		else {
			ExtractionRule rule = value.getDataSet().getSublineRule();
			if (rule!=null) {
				String label = (String)rule.extract(value);
				return label;
			}
		}
		return null;
	}
	
	private boolean isHTML(Object object) {
		if (!(object instanceof String)) return false;
		String string = (String)object;
		return string.contains("</p>") || string.contains("</h1>") || string.contains("</h2>");
	}
	
	private String getContentType(Resource resource) {
		String fileName = resource.getName().toLowerCase();
		if (fileName!=null && fileName.endsWith("pdf")) {
			return "application/pdf";
		}
		if (resource instanceof KBFile) {
			return ((KBFile)resource).getContentType();
		}
		return null;
	}
}