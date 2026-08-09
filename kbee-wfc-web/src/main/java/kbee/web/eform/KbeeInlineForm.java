package kbee.web.eform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EDisposition;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.kbee.content.form.EFormAbstractField;
import com.novamens.kbee.content.form.KbeeEContentTitleModel;
import com.novamens.kbee.content.form.KbeeEStringField;

public class KbeeInlineForm  implements EForm, Serializable {
	private static final long serialVersionUID = 1L;
	private List<EFormComponent> components;
	private String name;
	private String cssClass;
	
	public KbeeInlineForm(Content content) {
		this.components = getComponents(content);
		this.name = content.getContentTemplate().getName();
	}
	
	public List<EFormComponent> getComponents() {
		return components;
	}
	
	public void setComponents(List<EFormComponent> components) {
		this.components = components;
	}
	
	public List<EFormField<?>> getFields() {
		return getFields(getComponents());
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCssClass() {
		return cssClass;
	}
	
	public String getDisplayName() {
		return name;
	}
	
	@Override
	public EDisposition getDisposition() {
		return EDisposition.VERTICAL;
	}
	
	@Override
	public EFormAccessLevel getFormAccessLevel() {
		return EFormAccessLevel.GENERAL;
	}
	
	@Override
	public boolean isUseInline() {
		return false;
	}

	@Override
	public boolean isFileContainer() {
		return false;
	}
	
	@Override
	public String getViewer() {
		return null;
	}
	
	@Override
	public boolean isVisible(EFormData data) {
		return true;
	}
	
	@Override
	public List<String> getBehaviors() {
		return new ArrayList<String>();
	}
	
	@Override
	public EFormField<?> getField(String name) {
		for (EFormField<?> field : getFields(getComponents())) {
			if (name.equals(field.getName())) {
				return field;
			}
		}
		return null;
	}
	
	protected List<EFormComponent> getComponents(Content content) {
		List<EFormComponent> components = new ArrayList<EFormComponent>();
		
		EFormAbstractField<?> field = new KbeeEStringField();
		field.setName("title");
		field.setLabel("Title");
		KbeeEContentTitleModel model = new KbeeEContentTitleModel();
		field.setModel(model);
		components.add(field);
		
		return components;
	}
	
	private List<EFormField<?>> getFields(List<EFormComponent> components) {
		List<EFormField<?>> fields = new ArrayList<EFormField<?>>();
		for (EFormComponent component : components) {
			if (component instanceof EFormField) {
				fields.add((EFormField<?>)component);
			}
			if (component instanceof EFormContainer) {
				fields.addAll(getFields(((EFormContainer)component).getComponents()));
			}
		}
		return fields;
	}

	@Override
	public boolean hasToolbar() {
		// TODO Auto-generated method stub
		return false;
	}
}