package kbee.web.editor;


import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Multiplicity;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.DateField;
import com.novamens.wicket.markup.html.form.TextField;


@SuppressWarnings("serial")
public class AttributeEditor<T extends Classificable> extends ObjectEditorPanel<T>  {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AttributeEditor.class.getName());

	
	private static final long serialVersionUID = 1L;

	private boolean updated = false;
	
	private IModel<AttributeTemplate> templatemodel;
	
	private String value;
	private Date date_value;
	private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
	
	
	/**
	 * 
	 * 
	 * @param id
	 * @param templatemodel
	 */
	public AttributeEditor(String id, IModel<AttributeTemplate> templatemodel) {
		this (id, templatemodel, false);
	}
	
	public AttributeEditor(String id, IModel<AttributeTemplate> templatemodel, boolean isReadOnly) {
		super(id);
		
		setOutputMarkupId(true);
		setTemplate(templatemodel);
		updated = false;
		setReadOnly(isReadOnly);
	}
	
	@Override
	public void updateModel() {
		if (!updated) 
			return;
		
		if (    ((getValue()==null  ||  getValue().isEmpty())  && getAttributeValue()!=null) || 
				(getValue()!=null                              && getAttributeValue()==null) || 
				(getValue()!=null                              && !getValue().equals(getAttributeValue()))) {
						
			if (getValue()!=null && getValue().isEmpty()) {
				setAttributeValue(null);
				
			}
			else
				setAttributeValue(getValue());
			
			setUpdatedPart(getTemplate().getAttribute().getName().toLowerCase());			
			
		}
		
		this.updated = false;
	}
	
	
	public void setTemplate(IModel<AttributeTemplate> model) {
		this.templatemodel = model;
	}
	
	public AttributeTemplate getTemplate() {
		return this.templatemodel.getObject();
	}
	
	public Attribute getAttribute() {
		return getTemplate().getAttribute();
	}
	
	public void setValue(String value) {
		this.updated = true;
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
	
			 
	public void setDateValue(Date value) {
		this.updated = true;
		this.date_value = value;
		this.value=dateformat.format(this.date_value);
	}
	
	public Date getDateValue() {
		return this.date_value;
	}
	
	
	public String getAttributeValue() {
		try {
			String value = null;
			if (getTemplate().getAttribute()==null) 
				return null;
			List<String> values = getModelObject().getAttributeValues(getTemplate().getAttribute());
			
			if (!values.isEmpty()) 
				value = values.get(0);
			
			return value;
			
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	public void setAttributeValue(String value) {
		List<String> values = new ArrayList<String>();
		if (value!=null) 
			values.add(value);
		getModelObject().setAttributeValues(getTemplate().getAttribute(), values);
		
	}

	
	@Override
	public void cancel() {
		this.updated = false;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		if (get("field")==null) {
			
		 	setValue(getAttributeValue());
		 	
			boolean isrequired = !isReadOnly() && (getAttribute().getMultiplicity().equals(Multiplicity.M11) || getAttribute().getMultiplicity().equals(Multiplicity.M1N));
			
			if (getAttribute().isDate()) {

				try {
					if (getAttributeValue()!=null) {
						Date date = dateformat.parse(getAttributeValue());
						setDateValue(date);
					}
				} catch (ParseException e) {
					logger.error(e);
				}
				
				add(new DateField("field", new PropertyModel<Date>(this, "dateValue"), isrequired) {
					@Override
					public IModel<String> getLabel() {
						return new Model<String>(getAttribute().getName());
					}
				});
			}
			else {
				add(new TextField<String>("field", new PropertyModel<String>(this, "value"), isrequired, null) {
					@Override
					public IModel<String> getLabel() {
						return new Model<String>(getAttribute().getName());
					}
				});
			}
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.templatemodel.detach();
	}
}
