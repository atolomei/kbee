package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Multiplicity;
import com.novamens.kbee.content.model.KbeeAttributeTemplate;

import kbee.web.form.RelationEditor;

@Deprecated
@SuppressWarnings("serial")
public class AttributesEditor<T> extends RelationEditor<T, AttributeTemplate> {
	private static final long serialVersionUID = 1L;

	public AttributesEditor() {
		super("attributes");
	}

	public List<Attribute> getAttributes() {
		List<Attribute> attributes =  new ArrayList<Attribute>();
		List<Attribute> domainAttributes =  getContentDao().getAttributes(getDomain());
		for (Attribute attribute : domainAttributes) {
			boolean found = false;
			for (IModel<AttributeTemplate> model : getValues()) {
				if (model.getObject().getAttribute()!=null && 
						model.getObject().getAttribute().equals(attribute)) {
					found = true;
					break;
				}
			}
			if (!found && getEditor()!=null &&		
				attributes.add(attribute));
		}
		Collections.sort(attributes, new Comparator<Attribute>() {
			@Override
			public int compare(Attribute a, Attribute b) {
				try{
					return a.getName().compareToIgnoreCase(b.getName());
				} catch (Exception e) {
					//logger.error(e.getClass().getName(), e);
					return 0;
				}
			}
		}); 
		return attributes;
	}

 

	@Override
	protected String getText(AttributeTemplate value) {
		String type;
		if (value.getAttribute()!=null)
		type = "<span class=\"label\"> " + new StringResourceModel("property.type", AttributesEditor.this, null).getObject() + ":</span> <span class=\"highlight\"> " + value.getAttribute().getType().getLabel() + "</span>" ;
		else
		type = "";
		return type + ". " + super.getText(value);
	}
	
	 
	@Override
	protected List<Property<?>> getProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
		properties.add(new Property<Multiplicity>() {
			public String getName() {
				return "metadataSubtitle";
			}
			public boolean isBoolean() {
				return true;
			}
		});
		
		return properties;
	}

	 

	protected Property<?> getKey() {
		return new Property<Attribute>() {
			public String getName() {
				return "attribute";
			}
			public List<Attribute> getChoices() {
				return getAttributes();
			}
		};
	}

	@Override
	protected AttributeTemplate getNewValue() {
		KbeeAttributeTemplate template = new KbeeAttributeTemplate();
		template.setMetadataSubtitle(false);
		return template;
	}
}
