package kbee.web.eform;

import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EValidatable;
import com.novamens.content.form.EValidation;

public class EValidator<T> implements IValidator<T> {
	private static final long serialVersionUID = 1L;
	
	EFormField<T> field;
	IModel<EFormData> datamodel;
	EValidation validation;
	
	public class KbeeEValidatable implements EValidatable {
		IValidatable<T> validatable;
		public KbeeEValidatable(IValidatable<T> validatable) {
			this.validatable = validatable;
		}
		@Override
		public Object getValue() {
			return validatable.getValue();
		}
		@Override
		public EFormField<T> getField() {
			return field;
		}
		@Override
		public EFormData getData() {
			return datamodel.getObject();
		}
		@Override
		public void error(String message) {
			validatable.error(new ValidationError(message));
		}
		@Override
		public void error(String message, String... parameter) {
			validatable.error(new ValidationError(message));
		}
	}
	
	public EValidator(EValidation validation, EFormField<T> field, IModel<EFormData> datamodel) {
		this.field = field;
		this.datamodel = datamodel;
		this.validation = validation;
	}
	
	public EFormField<?> getField() {
		return field;
	}
	
	public EFormData getData() {
		return datamodel.getObject();
	}
	
	@Override
	public void validate(final IValidatable<T> validatable) {
		validation.validate(new KbeeEValidatable(validatable));
	}
} 