package com.novamens.wicket.markup.html.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.validation.ValidationError;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class NumberField<T extends Number & Comparable<T>> extends TextField<T> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TextField.class.getName());

	public NumberField(String id) {
		this(id, null, false, Width.W03);
	}

	public NumberField(String id, Width width) {
		this(id, null, false, width);
	}

	public NumberField(String id, boolean required) {
		this(id, null, required, Width.W03);
	}

	public NumberField(String id, IModel<T> model) {
		this(id, model, false, Width.W03);
	}

	public NumberField(String id, IModel<T> model, boolean required, Width width) {
		super(id, model, required, width, null);
	}

	@Override
	public void updateModel() {

		Object input = null;

		try {

			input = getInputValue();

			if (getModel() == null)
				return;
			if (input != null) {
				if (getModel().getObject() != null && !getModel().getObject().equals(getNumber(input))
						|| getModel().getObject() == null && input != null && isNumber(input)) {
					if (getEditor() != null) {
						getEditor().setUpdatedPart(getPart());
					}
					getModel().setObject((T) getNumber(input));
				}
			} else {
				if (getModel().getObject() != null)
					getModel().setObject(null);
			}
		} catch (Exception e) {
			logger.error(e, input != null ? input.toString() : "");
			getModel().detach();
		}
	}

	public void validate() {
		super.validate();
		if (getInput().hasErrorMessage()) {
			error(new ValidationError("This field must be a number"));
			setFeedback();
			return;
		}
	}

	protected boolean isNumber(Object input) {
		if (input == null || "".equals(input))
			return false;
		return isDigits(input.toString());
	}

	protected T getNumber(Object input) {

		if (getModel().getObject() instanceof Long) {
			if (!isNumber(input))
				return (T)(new Long(0));
			return (T)Long.valueOf(input.toString());
		}

		else if (getModel().getObject() instanceof Integer) {
			if (!isNumber(input))
				return (T)(new Integer(0));
			return (T)Integer.valueOf(input.toString());
		}

		else if (getModel().getObject() instanceof Double) {
			if (!isNumber(input))
				return (T)(new Double(0));
			return (T)Double.valueOf(input.toString());
		}

		else if (getModel().getObject() instanceof Float) {
			if (!isNumber(input))
				return (T)(new Float(0));
			return (T)Float.valueOf(input.toString());
		}

		else {
			logger.error("not Long | Integer | Float | Double ");
		}

		return null;
	}

	protected org.apache.wicket.markup.html.form.TextField<?> newTextField() {

		org.apache.wicket.markup.html.form.TextField<T> input = new NumberTextField<T>("input",
				new PropertyModel<T>(this, "value")) {
			@Override
			public void validate() {
				super.validate();
				NumberField.this.validate();
			}

			@Override
			public boolean isEnabled() {
				return getEditor() != null ? getEditor().isEditionEnabled() : true;
			}

			@Override
			@SuppressWarnings("unchecked")
			public void convertInput() {
				try {
					String value = getInput();

					if (getModel().getObject() instanceof Long) {
						
						Long number = Long.valueOf(value);
						setConvertedInput((T) number);
						
					} else if (getModel().getObject() instanceof Integer) {
						Integer number = Integer.valueOf(value);
						setConvertedInput((T) number);
						
					} else if (getModel().getObject() instanceof Float) {
						Float number = Float.valueOf(value);
						setConvertedInput((T) number);
						
					} else if (getModel().getObject() instanceof Double) {
						Double number = Double.valueOf(value);
						setConvertedInput((T) number);
						
					} else {
						logger.error("not Long | Integer");
					}
				} catch (NumberFormatException e) {
					error("NumberFormatException");
				}
			}

			protected void onComponentTag(final ComponentTag tag) {
				IValueMap attributes = tag.getAttributes();
				attributes.put("type", "number");
				super.onComponentTag(tag);
			}
		};
		input.add(new AttributeModifier("type", "number"));
		return input;
	}

	private boolean isDigits(String argument) {
		for (int c = 0; c < argument.length(); c++) {
			if (!Character.isDigit(argument.charAt(c))) {
				return false;
			}
		}
		return true;
	}
}
