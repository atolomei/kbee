package com.novamens.wicket.markup.html.form;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
//import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;


import com.novamens.wicket.markup.html.form.Form.Disposition;


/**
 * 
 * 
 * Returns a Date assuming UTC ZoneOffset
 *
 */
@SuppressWarnings("serial")
public class DateField extends TextField<Date> {
	private static final long serialVersionUID = 1L;

	private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(DateField.class.getName());

	final WebMarkupContainer date = new WebMarkupContainer("date");
	final org.apache.wicket.markup.html.form.TextField<?> input  = newTextField();

	private DateValidator date_validator = new DateValidator();	
	// America/Buenos_Aires
	
	protected static final ResourceReference LOC = new CssResourceReference(DateField.class, "moment-with-locales.js");
	protected static final ResourceReference DTCSS = new CssResourceReference(DateField.class,
			"bootstrap-datetimepicker.min.css");
	protected static final ResourceReference DTJS = new CssResourceReference(DateField.class,
			"bootstrap-datetimepicker.min.js");
	 
	public class DateValidator implements IValidator<Date> {
													
		public void validate(final IValidatable<Date> validatable) {

			Date date = validatable.getValue();
			
			logger.debug("DateValidator.validate  -> " + validatable.getValue());
			
			if (date==null)
				return;
			
			OffsetDateTime dateTime = OffsetDateTime.ofInstant(date.toInstant(), ZoneId.of("Z"));
			
		 
			OffsetDateTime last_date = OffsetDateTime.of(2100, 12, 31, 23, 59, 59, 0, ZoneOffset.from(dateTime));
			
		 
			
			if (dateTime.isAfter(last_date)) {				
				validatable.error(new ValidationError(this, "invalid-date-high"));
				
			}			
		}	
	}
	
	/** ---------------------------------------------------
	 *
	 * 
	 */
	public class DateControlFragment extends Fragment {




		public DateControlFragment(String id) {
			super(id, "control-fragment", DateField.this);

			final WebMarkupContainer date = new WebMarkupContainer("date");
			final org.apache.wicket.markup.html.form.TextField<?> input  = newTextField();
			input.setOutputMarkupId(true);
			date.setOutputMarkupId(true);
			input.add(new AjaxFormComponentUpdatingBehavior("change") {
				protected void onUpdate(AjaxRequestTarget target) {

					DateField.this.onUpdate(target);

					if (DateField.this.hasFeedback()) {
	 					DateField.this.validate();
						target.add(DateField.this);
					}
				}
				protected void onError(AjaxRequestTarget target, RuntimeException e) {
					target.add(DateField.this);
				}
			});


			WebMarkupContainer datepicker_btn = new WebMarkupContainer("datepicker_btn");
			WebMarkupContainer datepickerIcon = new WebMarkupContainer("datepicker");

			datepicker_btn.add(datepickerIcon);

			datepicker_btn.add(new AttributeModifier("onclick", new Model<String>() {
				public String getObject() {
					
					String script = "$('#"+date.getMarkupId()+"').datetimepicker({ "+
							"showTodayButton: true," +
                            "showClear: true,"+
							"format: 'L'," +
							"useCurrent: false," +
							"debug:false," +
							"widgetPositioning: {" +
							"            horizontal: 'right'," +
							"            vertical: 'auto'" +
							"}," +
							//"keepOpen: true," +
							"}).on('dp.hide', function(e){" +
							"    return $('#"+input.getMarkupId()+ "').trigger('change');" +
							"});"+
							"$('#"+date.getMarkupId()+"').datetimepicker('show');";
					
					/*
					 * String script =
					 * "$('#"+date.getMarkupId()+" .input-group.date').datepicker({ "+
					 * "todayBtn: 'linked',"+ "clearBtn: true,"+
					 * "format: '"+getDatePattern().toLowerCase()+"'," + "autoclose: true"+ "});";
					 */
					return script;
				}
			}));
			
			datepickerIcon.add(new AttributeModifier("style", new Model<String>() {
				public String getObject() {
					return getEditor()!=null && getEditor().isEditionEnabled() ? "cursor:pointer;" : "";
				}
			}));
			
			date.add(input);

			date.add(datepicker_btn);
			date.add(getFeedback());
		
//			input.add(new KeyboardBehavior() {
//				protected void onKey(AjaxRequestTarget target, String jsKeycode) {
//					DateField.this.onKey(target, jsKeycode);
//				}
//			});
			
			IModel<String> help = getHelpText();
			
			if (help!=null && help.getObject()!=null)
				date.add((new Label ("help", help)).setEscapeModelStrings(false));
			else
				date.add((new Label ("help", "")).setVisible(false));
			
			add(date);
		}
		
		public Date getValue() {
			 
			logger.debug("DateField getValue() -> " + DateField.this.getValue().toString());
			
			return DateField.this.getValue();
		}
		
		public void setValue(Date value) {
			DateField.this.setValue(value);
		}
	}
	
	
	/** 
	 * 
	 */
	
	public DateField(String id) {
		this(id, null, false, Width.W12);
	}
	
	public DateField(String id, Width width) {
		this(id, null, false, width);
	}
	
	public DateField(String id, boolean required) {
		this(id, null, required, Width.W12);
	}
	
	public DateField(String id, IModel<Date> model) {
		this(id, model, false, Width.W12);
	}
	
	public DateField(String id, IModel<Date> model, boolean required) {
		this(id, model, required, Width.W12);
	}
	
	public DateField(String id, IModel<Date> model, boolean required, Width width) {
		super(id, model, required, width, null);
	}
	
	@Override
	public Component getInput() {
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			return get("horizontal-layout:control:date:input");
		}
		else {
			return get("control:date:input");
		}
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(DTCSS));
		response.render(JavaScriptHeaderItem.forReference(LOC));
		response.render(JavaScriptHeaderItem.forReference(DTJS));

	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	
		if (super.getValidator()==null)
			add(getValidator());
	}
	
	
	public void setValidator(DateValidator va) {
		date_validator = va;
	}
	
	public DateValidator getValidator() {
		return date_validator;
	}


	
	/**
	 * Date in UTC TZ
	 * @return
	 */
	 
	
	protected Object getInputValue() {
		
		String value = ((FormComponent<?>)getInput()).getInput();
		if ("".equals(value))
			return null;
		else {
			try {
				// --
				// Dia Mes Ano 
				// --
				 SimpleDateFormat dateformat = new SimpleDateFormat(getDatePattern());
				 
				 // dateformat.setTimeZone(TimeZone.getTimeZone("Z"));
				 dateformat.setTimeZone(TimeZone.getDefault());
				 Date date = dateformat.parse(value);
				 return date;
			}
			catch (Exception e) {
				return null;
			}
		}
	}

	@Override
	protected Fragment newControlFragment() {
		return new DateControlFragment("control");
	}
	
	@Override
	protected org.apache.wicket.markup.html.form.TextField<?> newTextField() {
		
		org.apache.wicket.extensions.markup.html.form.DateTextField input = new org.apache.wicket.extensions.markup.html.form.DateTextField("input", new PropertyModel<Date>(this, "value"), getDefaultDatePattern()) {
			@Override
			public void validate() {
				super.validate();
				DateField.this.validate();
			}
			
			@Override
			public boolean isEnabled() {
				return getEditor()!=null ? getEditor().isEditionEnabled() : true;
			}
		};

		return input;
	}
	
	protected String getDatePattern() {
		return getDefaultDatePattern();
	}
	
	protected String getDefaultDatePattern() {
		String pattern = "es".equals(WebSession.get().getLocale().getLanguage()) ? "dd/MM/yyyy" : "MM/dd/yyyy";
		return pattern;
	}
}
 