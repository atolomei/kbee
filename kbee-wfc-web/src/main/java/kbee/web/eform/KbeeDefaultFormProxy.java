package kbee.web.eform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EDisposition;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;

public class KbeeDefaultFormProxy  implements DefaultForm, Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String cssClass;
	
	public KbeeDefaultFormProxy(String name) {
		this.name = name;
	}
	
	public List<EFormComponent> getComponents() {
		return null;
	}
	
	public void setComponents(List<EFormComponent> components) {
	}
	
	public List<EFormField<?>> getFields() {
		return null;
	}
	
	public EFormField<?> getField(String name) {
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return false;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getCssClass() {
		return cssClass;
	}
	
	public String getDisplayName() {
		return name;
	}
	
	@Override
	public String getViewer() {
		return null;
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
	public boolean isVisible(EFormData data) {
		return true;
	}

	@Override
	public List<String> getBehaviors() {
		return new ArrayList<String>();
	}

	@Override
	public boolean hasToolbar() {
		return false;
	}
}
