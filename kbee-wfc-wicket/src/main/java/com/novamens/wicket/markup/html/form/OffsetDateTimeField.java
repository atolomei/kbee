package com.novamens.wicket.markup.html.form;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
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
 
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.kbee.wicket.markup.html.behaviour.KeyboardBehavior;
import com.novamens.wicket.markup.html.form.Form.Disposition;

/**
 * 
 * 
 * <p>
 * This field selects a dd / mm / yyyy, with a Time Zone the 24hr window
 * would be uncler. Therefore the Time Zone must be provided. Time Zone in java
 * are ZoneId (a GMT offset plus a region/city particulars).
 * </p>
 * 
 */
@Deprecated
@SuppressWarnings("serial")
public class OffsetDateTimeField extends TextField<OffsetDateTime> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(OffsetDateTimeField.class.getName());

	
	protected static final ResourceReference LOC = new CssResourceReference(DateField.class, "moment-with-locales.js");
	protected static final ResourceReference DTCSS = new CssResourceReference(DateField.class,"bootstrap-datetimepicker.min.css");
	
	/*
	 * protected static final ResourceReference GLCSS = new
	 * CssResourceReference(DateField.class, "bootstrap-glyphicons.css");
	 */
	protected static final ResourceReference DTJS = new CssResourceReference(DateField.class,"bootstrap-datetimepicker.min.js");
	
	//protected static final ResourceReference DTJQJS = new CssResourceReference(DateField.class, "jquery-2.1.1.min.js");
	

	
	public class DateValidator implements IValidator<OffsetDateTime> {
		
		public void validate(final IValidatable<OffsetDateTime> validatable) {

			Object dateobject = validatable.getValue();
			
			if (dateobject==null) 
				return;
			
			OffsetDateTime date = ((OffsetDateTime)dateobject);
			
			ZoneId zid = getZoneId();
			
			if (zid==null)
				zid = ZoneId.systemDefault();
			
			OffsetDateTime dateTime = OffsetDateTime.ofInstant(date.toInstant(), zid);
			OffsetDateTime last_date = OffsetDateTime.of(2200, 12, 31, 23, 59, 59, 0, ZoneOffset.from(dateTime));
			
			if (dateTime.getYear()<70) {
				OffsetDateTime correctedDateTime = OffsetDateTime.of(dateTime.getYear()+2000,
					dateTime.getMonthValue(),
					dateTime.getDayOfMonth(),
					dateTime.getHour(),
					dateTime.getMinute(),
					dateTime.getSecond(),
					0,
					ZoneOffset.from(dateTime));
					setValue(OffsetDateTime.from(correctedDateTime.plusDays(2).toInstant()));
			}
			else if (dateTime.getYear()<100) {
				OffsetDateTime correctedDateTime = OffsetDateTime.of(dateTime.getYear()+1900,
					dateTime.getMonthValue(),
					dateTime.getDayOfMonth(),
					dateTime.getHour(),
					dateTime.getMinute(),
					dateTime.getSecond(),
					0,
					ZoneOffset.from(dateTime));
				setValue(OffsetDateTime.from(correctedDateTime.plusDays(2).toInstant()));
			}
			else if (dateTime.isAfter(last_date)) {				
				validatable.error(new ValidationError(this, "invalid-date-high"));
			}			
		}	
	}

	

	

	/**
	 * ---------------------------------------------------
	 *
	 * 
	 */
	public class DateControlFragment extends Fragment {

		public DateControlFragment(String id) {
			super(id, "control-fragment", OffsetDateTimeField.this);

			final WebMarkupContainer date = new WebMarkupContainer("date");
			
			date.setOutputMarkupId(true);

			final org.apache.wicket.markup.html.form.TextField<?> input = newTextField();

			input.setOutputMarkupId(true);

			input.add(new AjaxFormComponentUpdatingBehavior("change") {
				protected void onUpdate(AjaxRequestTarget target) {
 					OffsetDateTimeField.this.onUpdate(target);
					if (OffsetDateTimeField.this.hasFeedback()) {
						OffsetDateTimeField.this.validate();
						target.add(OffsetDateTimeField.this);
					}
				}

				protected void onError(AjaxRequestTarget target, RuntimeException e) {
					target.add(OffsetDateTimeField.this);
				}
			});

			WebMarkupContainer datetimepicker = new WebMarkupContainer("datetimepicker") {
				@Override
				public boolean isVisible() {
					return isEnabledInHierarchy();
				}
			};
			datetimepicker.add(new AttributeModifier("onclick", new Model<String>() {
				public String getObject() {
					
					  String script= "$('#" + date.getMarkupId()
					  +" .input-group.date').datetimepicker({ " +"showTodayButton: true, format:'L'," +
							  "showClear: true"  + "}).on('dp.hide', function(e){ return $('#"+input.getMarkupId()+"').trigger('change');});";

					return script;
				}
			}));

			datetimepicker.add(new AttributeModifier("style", new Model<String>() {
				public String getObject() {
					return getEditor() != null && getEditor().isEditionEnabled() ? "cursor:pointer;" : "";
				}
			}));
			date.add(input);
			date.add(datetimepicker);
			date.add(getFeedback());
			date.add(getInfo());

			IModel<String> help = getHelpText();

			if (help != null && help.getObject() != null)
				date.add((new Label("help", help)).setEscapeModelStrings(false));
			else
				date.add((new Label("help", "")).setVisible(false));

			add(date);
		}

		public OffsetDateTime getValue() {

			logger.info(OffsetDateTimeField.this.getValue().toString());

			return OffsetDateTimeField.this.getValue();
		}

		public void setValue(OffsetDateTime value) {
			OffsetDateTimeField.this.setValue(value);
		}
	}

	/**
	 * ---------------------------------------------------
	 *
	 * 
	 */

	private ZoneId zid;
	
	public OffsetDateTimeField(String id, ZoneId zid) {
		this(id, zid, null, false, Width.W12);
	}

	public OffsetDateTimeField(String id, ZoneId zid, Width width) {
		this(id, zid, null, false, width);
	}

	public OffsetDateTimeField(String id, ZoneId zid, boolean required) {
		this(id, zid, null, required, Width.W12);
	}

	public OffsetDateTimeField(String id, ZoneId zid, IModel<OffsetDateTime> model) {
		this(id, zid, model, false, Width.W12);
	}

	public OffsetDateTimeField(String id, ZoneId zid, IModel<OffsetDateTime> model, boolean required) {
		this(id, zid, model, required, Width.W12);
	}

	public OffsetDateTimeField(String id, ZoneId zid, IModel<OffsetDateTime> model, boolean required, Width width) {
		super(id, model, required, width, null);
		
		setOutputMarkupId(true);
		setZoneId(zid);
	}

	
	private Locale locale;

	
	public void setZoneId(ZoneId z) {
		this.zid = z;
	}

	public ZoneId getZoneId() {
		return this.zid;
	}
	
	@Override
	public Component getInput() {
		if (getDisposition() == null || getDisposition() == Disposition.HORIZONTAL) {
			return get("horizontal-layout:control:date:input");
		} else {
			return get("control:date:input");
		}
	}

	public Date getDateValue() {

		if (getValue() == null)
			return null;

		OffsetDateTime value = getValue();

		long epochMilli = value.toInstant().toEpochMilli();

		Date date = new Date(epochMilli);
		return date;
	}

	public void setDateValue(Date date) {

		if (date == null) {
			setValue(null);
			return;
		}

		ZoneId zid = getZoneId();

		if (zid == null)
			zid = ZoneId.systemDefault();

		
		
		OffsetDateTime zdate=date.toInstant().atOffset(ZoneOffset.UTC);
		
		
		ZonedDateTime zdt=zdate.atZoneSimilarLocal(zid);
		OffsetDateTime value = zdt.toOffsetDateTime();
		
		setValue(value);

	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(LOC));
		//response.render(JavaScriptHeaderItem.forReference(DTJQJS));
		response.render(CssHeaderItem.forReference(DTCSS));
		//response.render(CssHeaderItem.forReference(GLCSS));
		//response.render(CssHeaderItem.forReference(DTBMINCSS));
		response.render(JavaScriptHeaderItem.forReference(DTJS));
		//response.render(JavaScriptHeaderItem.forReference(DTBMINJS));
		
		

	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		if (getValidator() == null) {
			add(new DateValidator());
		}
	}

	protected Object getInputValue() {
		
		String value = ((FormComponent<?>)getInput()).getInput();
		
		
		if (value==null || "".equals(value))
			return null;
		
		try {
				// --
				// Dia Mes Ano -> en el OffsetDateTime del usuario a las 00:00:00
				// --
				SimpleDateFormat dateformat = new SimpleDateFormat(getDatePattern());
				Date date = dateformat.parse(value);
				
				LocalDate ldate = date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
				
				ZoneId zid = getZoneId();
				
				if (zid==null)
					zid=ZoneId.systemDefault();

				ZonedDateTime zdt = ldate.atStartOfDay(zid);
				
				OffsetDateTime dvalue = zdt.toOffsetDateTime();

				return dvalue;
				
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
		
	}

	@Override
	protected Fragment newControlFragment() {
		return new DateControlFragment("control");
	}

	@Override
	protected org.apache.wicket.markup.html.form.TextField<?> newTextField() {

		DateTextField input = new DateTextField("input", new PropertyModel<Date>(this, "dateValue"),
				getDefaultDatePattern()) {
			@Override
			public void validate() {
				super.validate();
				OffsetDateTimeField.this.validate();
			}

			@Override
			public boolean isEnabled() {
				return getEditor() != null ? getEditor().isEditionEnabled() : true;
			}
		};
		
		input.add(new KeyboardBehavior() {
			protected void onKey(AjaxRequestTarget target, String jsKeycode) {
				OffsetDateTimeField.this.onKey(target, jsKeycode);
			}
		});

		return input;
	}

	protected String getDatePattern() {
		return getDefaultDatePattern();
	}
	
	protected String getDefaultDatePattern() {
		//String pattern = "es".equals(WebSession.get().getLocale().getLanguage()) ? "dd/MM/yyyy" : "MM/dd/yyyy";
		if (getLocale()!=null) {
			if (getLocale().getLanguage().startsWith("es")) {
				return "dd/MM/yyyy";
			}
			else
				return "MM/dd/yyyy";
		}
		return "MM/dd/yyyy";
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
}
