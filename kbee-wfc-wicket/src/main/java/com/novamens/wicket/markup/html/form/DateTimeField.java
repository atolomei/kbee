package com.novamens.wicket.markup.html.form;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.DateConverter;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.logging.Logger;

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
@SuppressWarnings("serial")
public class DateTimeField extends TextField<OffsetDateTime> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(DateTimeField.class.getName());
	
	protected static final ResourceReference LOC = new CssResourceReference(DateField.class, "moment-with-locales.js");
	protected static final ResourceReference DTCSS = new CssResourceReference(DateField.class, "bootstrap-datetimepicker.min.css");
	protected static final ResourceReference DTJS = new CssResourceReference(DateField.class, "bootstrap-datetimepicker.min.js");

	private String format = "L";
	
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

	ZoneId zid;

	public void setZoneId(ZoneId z) {
		this.zid = z;
	}

	public ZoneId getZoneId() {
		return this.zid;
	}

	

	/**
	 * ---------------------------------------------------
	 *
	 * 
	 */
	public class DateControlFragment extends Fragment {

		public DateControlFragment(String id) {
			super(id, "control-fragment", DateTimeField.this);

			final WebMarkupContainer date = new WebMarkupContainer("date");
			
			date.setOutputMarkupId(true);

			final org.apache.wicket.markup.html.form.TextField<?> input = newTextField();

			input.setOutputMarkupId(true);

			input.add(new AjaxFormComponentUpdatingBehavior("change") {
				protected void onUpdate(AjaxRequestTarget target) {
 					DateTimeField.this.onUpdate(target);
					if (DateTimeField.this.hasFeedback()) {
						DateTimeField.this.validate();
						target.add(DateTimeField.this);
					}
				}

				protected void onError(AjaxRequestTarget target, RuntimeException e) {
					target.add(DateTimeField.this);
				}
			});
			
			
			input.add(new AjaxFormComponentUpdatingBehavior("update") {
				protected void onUpdate(AjaxRequestTarget target) {
 					DateTimeField.this.onUpdate(target);
					if (DateTimeField.this.hasFeedback()) {
						DateTimeField.this.validate();
						target.add(DateTimeField.this);
					}
				}

				protected void onError(AjaxRequestTarget target, RuntimeException e) {
					target.add(DateTimeField.this);
				}
			});

			WebMarkupContainer datetimepicker = new WebMarkupContainer("datetimepicker") {
				@Override
				public boolean isVisible() {
					return isEnabledInHierarchy() && isInputEnabled();
				}
			};
			datetimepicker.add(new AttributeModifier("onclick", new Model<String>() {
				public String getObject() {
					String locale = WebSession.get().getLocale().getLanguage();
					String script;
					if ("L".equals(getFormat())) {
						script ="$('#" + date.getMarkupId() +
						" .input-group.date').datetimepicker({ " +"showTodayButton: true, locale:'"+locale+"', format:'L'," +
						"showClear: true"  + "}).on('dp.hide', function(e){ return $('#"+input.getMarkupId()+"').trigger('change');});";
					}
					else {
						if ("LT".equals(getFormat())) {
							script= "$('#" + date.getMarkupId() +
							" .input-group.date').datetimepicker({ " +"showTodayButton: true, locale:'"+locale+"',"  +
							"showClear: true"  + "}).on('dp.change', function(e){ return $('#"+input.getMarkupId()+"').trigger('change');});";
						}
						else {
							script= "$('#" + date.getMarkupId() +
							" .input-group.date').datetimepicker({ " +"showTodayButton: true, locale:'"+locale+"', format:'"+getFormat()+"',"  +
							"showClear: true"  + "}).on('dp.change', function(e){ return $('#"+input.getMarkupId()+"').trigger('change');});";
						}
					}
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

			logger.info(DateTimeField.this.getValue().toString());

			return DateTimeField.this.getValue();
		}

		public void setValue(OffsetDateTime value) {
			DateTimeField.this.setValue(value);
		}
	}

	/**
	 * ---------------------------------------------------
	 *
	 * 
	 */

	public DateTimeField(String id, ZoneId zid) {
		this(id, zid, null, false, Width.W12);
	}

	public DateTimeField(String id, ZoneId zid, Width width) {
		this(id, zid, null, false, width);
	}

	public DateTimeField(String id, ZoneId zid, boolean required) {
		this(id, zid, null, required, Width.W12);
	}

	public DateTimeField(String id, ZoneId zid, IModel<OffsetDateTime> model) {
		this(id, zid, model, false, Width.W12);
	}

	public DateTimeField(String id, ZoneId zid, IModel<OffsetDateTime> model, boolean required) {
		this(id, zid, model, required, Width.W12);
	}

	public DateTimeField(String id, ZoneId zid, IModel<OffsetDateTime> model, boolean required, Width width) {
		super(id, model, required, width, null);
		
		setOutputMarkupId(true);
		setZoneId(zid);
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
	    
	    logger.debug("date");
	    logger.debug(getValue().toString());
	    logger.debug(getZoneId());

	    return Date.from(getValue().toInstant()); // 🔥 clave
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

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(LOC));
		response.render(CssHeaderItem.forReference(DTCSS));
		response.render(JavaScriptHeaderItem.forReference(DTJS));
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
			if ("L".equals(getFormat())) {
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
			else {
				LocalDateTime ldate = LocalDateTime.parse(value,  DateTimeFormatter.ofPattern(getDatePattern(), WebSession.get().getLocale()));
				ZoneId zid = getZoneId();
				
				
				if (zid==null)
					zid=ZoneId.systemDefault();

				ZonedDateTime zdt = ldate.atZone(zid);
				
				OffsetDateTime dvalue = zdt.toOffsetDateTime();
				
				return dvalue;
			}	
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

		DateTextField input = new DateTextField("input", new PropertyModel<Date>(this, "dateValue"), getDefaultDatePattern()) {
			@Override
			public void validate() {
				super.validate();
				DateTimeField.this.validate();
			}
			@Override
			public boolean isEnabled() {
				return isInputEnabled();
				
			}
		    @Override
		    @SuppressWarnings("unchecked")
		    public <C> IConverter<C> getConverter(Class<C> type) {
		        if (Date.class.isAssignableFrom(type)) {
		            return (IConverter<C>) new DateConverter() {
		                private TimeZone tz = TimeZone.getTimeZone(getZoneId());
		                @Override
		                public DateFormat getDateFormat(Locale locale) {
		                    SimpleDateFormat df = new SimpleDateFormat(getDefaultDatePattern(), locale);
		                    df.setLenient(false);
		                    df.setTimeZone(tz); 
		                    return df;
		                }
		                @Override
		                public String convertToString(Date value, Locale locale) {
		                    if (value == null) return null;
		                    return getDateFormat(locale).format(value);
		                }

		                @Override
		                public Date convertToObject(String value, Locale locale) {
		                    if (value == null || value.trim().isEmpty())
		                        return null;
		                    try {
		                        return getDateFormat(locale).parse(value);
		                    } 
		                    catch (ParseException e) {
		                        throw new ConversionException("Invalid date", e);
		                    }
		                }
		            };
		        }
		        return super.getConverter(type);
		    }
		};
		
		return input;
	}

	protected String getDatePattern() {
		return getDefaultDatePattern();
	}
	
	protected String getDefaultDatePattern() {
		String pattern;
		if ("L".equals(getFormat())) {
			pattern = "es".equals(WebSession.get().getLocale().getLanguage()) ? "dd/MM/yyyy" : "MM/dd/yyyy";
		}
		else {
			if ("LT".equals(getFormat())) {
				pattern = "es".equals(WebSession.get().getLocale().getLanguage()) ? "dd/MM/yyyy h:m a" : "MM/dd/yyyy h:m a";
			}
			else {
				pattern = format;
			}
		}
		return pattern;
	}
}
