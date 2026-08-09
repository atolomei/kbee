package kbee.web.eform;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EValidatable;

public class KbeeEValidatable implements EValidatable {
	EFormField<?> field;
	EFormData data;
	public KbeeEValidatable(EFormData data, EFormField<?> field) {
		this.data = data;
		this.field = field;
	}
	public Object getValue() {
		return getData().getData(getField());
	}
	public EFormField<?> getField() {
		return field;
	}
	public EFormData getData() {
		return data;
	}
	public void error(String message) {
		onError(message);
	}
	public void error(String message, String... parameter) {
		onError(message, parameter);
	}
	public void onError(String message) {
	}
	public void onError(String message, String... partameter) {
	}
}