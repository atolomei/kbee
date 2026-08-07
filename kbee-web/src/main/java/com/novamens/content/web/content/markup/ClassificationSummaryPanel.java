package com.novamens.content.web.content.markup;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetType;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;

import kbee.web.content.editor.ContentEditor;
import kbee.web.event.wicket.EditorEvent;
import kbee.web.report.Row;

@SuppressWarnings("serial")
public class ClassificationSummaryPanel<T extends Content> extends ObjectEditorPanel<T>{
	private static final long serialVersionUID = 1L;

	
	public ClassificationSummaryPanel() {
		this("summary");
	}
		
		
	public ClassificationSummaryPanel(String id) {
		super(id);
		
		setOutputMarkupId(true);
		
		add(new WicketEventListener<EditorEvent>() {
			public void onEvent(EditorEvent event) {
				event.getRequestTarget().add(ClassificationSummaryPanel.this);
			}
		});
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("label")==null) {
			addLabel();
		}
	}
	
	protected void addLabel() {
		add(new Label("label", new Model<String>() {
			public String getObject() {
				return getSummary(); 
			}
		}));
	}
	
	protected String getSummary() {
		int index = 0;
		StringBuffer summary = new StringBuffer();
		for (Classifier classifier : getCanonicalClassifiers()) {
			Classification classification = getClassification(classifier);
			if (classification!=null && classification.getDataSetMember()!=null) {
				if (index>0)
					summary.append(" · ");
				if (classifier.getDataSetType().equals(DataSetType.DATE)) {
					if (classification.getDataSetMember().getDateValue()!=null) {
						// TODO Fix multi language
						//
						DateTimeFormatter dt = DateTimeFormatter.ofPattern("MM/dd/yy"); 
						String label =dt.format(classification.getDataSetMember().getDateValue());
						summary.append(label);
					}
				}
				else {
					summary.append(classification.getDataSetMember().getDisplayName());
				}
				index++;
			}
		}
		for (AttributeTemplate template : getCanonicalAttributes()){
			List<String> values = getValues(template.getAttribute());
			if (values!=null && !values.isEmpty()) {
				if (index>0)
					summary.append(" · ");
				summary.append(format(template, values.get(0)));
				index++;
			}	
		}
		return summary.toString();
	}
	
	protected List<Classifier> getCanonicalClassifiers() {
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (ClassifierTemplate template : getModelObject().getContentTemplate().getClassifiers()) {
			if (template.isMetadataSubtitle()) {
				classifiers.add(template.getClassifier());
			}
		}
		Collections.sort(classifiers, new Comparator<Classifier>() {
			@Override
			public int compare(Classifier a, Classifier b) {
				return a.getDisplayName().compareTo(b.getDisplayName());

			}
		}); 
		return classifiers;
	}
	
	protected List<AttributeTemplate> getCanonicalAttributes() {
		List<AttributeTemplate> attributes = new ArrayList<AttributeTemplate>();
		for (AttributeTemplate template : getModelObject().getContentTemplate().getAttributes()) {
			if (template.isMetadataSubtitle()) {
				attributes.add(template);
			}
		}
		return attributes;
	}
	
	protected Classification getClassification(Classifier classifer) {
		for (Classification classification : ((ContentEditor<T>)getEditor()).getClassification()) {
			if (classification.getClassifier().equals(classifer)) {
				return classification;
			}
		}
		return null;
	}
	
	protected List<String> getValues(Attribute attribute) {
		List<String> values = ((ContentEditor<T>)getEditor()).getAttributeValue(attribute);
		return values;
	}
	
	protected String format(AttributeTemplate template, String value) {
		if (template.getAttribute().getType().equals(AttributeType.DATE)) {
 			OffsetDateTime odate = ServiceLocator.getService(DateTimeService.class).parseStrDate(value);
			value = ServiceLocator.getService(DateTimeService.class).getDateDisplayString(odate, getSessionUser().getLocale());
			return value;
		}
		else {
			return value;
		}
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
