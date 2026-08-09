package kbee.web.security;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetType;
import com.novamens.kbee.wicket.model.ModelPanel;

@SuppressWarnings("serial")
public class ContentTitlePanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;

	public ContentTitlePanel(String id, IModel<T> model) {
		super(id, model);
		
		add(new Label("title", new Model<String>() {
			public String getObject() {
				return ContentTitlePanel.this.getModelObject().getTitle();
			}
		}));
		
		add(new Label("subtitle", new Model<String>() {
			public String getObject() {
				return ContentTitlePanel.this.getSummary();
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
			if (!values.isEmpty()) {
				if (index>0)
					summary.append(" · ");
				summary.append(values.get(0));
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
		for (Classification classification : getModelObject().getClassification()) {
			if (classification.getClassifier().equals(classifer)) {
				return classification;
			}
		}
		return null;
	}
	
	protected List<String> getValues(Attribute attribute) {
		List<String> values = getModelObject().getAttributeValues(attribute);
		return values;
	}

}
