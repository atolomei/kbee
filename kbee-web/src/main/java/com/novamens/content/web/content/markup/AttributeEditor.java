package com.novamens.content.web.content.markup;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeSource;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.AttributeValidatable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeCodeExecutor;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.DateField;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.TextField;

import kbee.web.event.wicket.EditorEvent;

@SuppressWarnings("serial")
public class AttributeEditor<T extends Content> extends ModelEditor<T>  {
			
	private static final long serialVersionUID = 1L;
																										
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AttributeEditor.class.getName());
	
	private boolean updated = false;
	private boolean leavevalues = false;
	private boolean editionEnabled = false;			// edit habilitado		
	private boolean isEditable = true;				// el campo puede ser editado desde donde es invocado 
	private boolean read_only;						// el campo es readonly (intrinseco al campo)
	private String errorMessage;
	
	private IModel<AttributeTemplate> templatemodel;
	private String value;
	
	private Date date;
	
	private List<String> values = new ArrayList<String>();
	
	Boolean parentsEnabled = null;
	
	class NumericValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String value = validatable.getValue();
			if (!isValid(value)) {
				validatable.error(new ValidationError(this, "not-number"));
			}
		}
	}
	
	class ValidatableValue<V> implements AttributeValidatable<V> {
		private V value;
		String message;
		public ValidatableValue(V value) {
			this.value = value;
		}
		public V getValue() {
			return value;
		}
		public void setError(String message) {
			this.message = message;
		}
		public String getMessage() {
			return this.message;
		}
		public Locale getLocale() {
			return Locale.getDefault();
		}
	}
	
	class AttributeValidator<P> implements IValidator<P> {
		@Override
		public void validate(final IValidatable<P> validatable) {
			P value = validatable.getValue();
			if (getAttribute().getValidator()!=null) {
				ValidatableValue<P> validatablevalue = new ValidatableValue<P>(value);
				if (!getAttribute().getValidator().validate(validatablevalue)) {
					validatable.error(new ValidationError(validatablevalue.getMessage()));
				}
			}
		}
	}

	
	public class ValueEditorFragment extends Fragment {

		public ValueEditorFragment(String id) {
			super(id, "editor-fragment", AttributeEditor.this);
			
			if (getAttribute().isDate()) {
				add(new DateField("value", new PropertyModel<Date>(AttributeEditor.this, "date")) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						if (!hasErrorMessage()) {
							
							// Dates are converted to the TimeZone of the Domain 
							Date date = getValue();
							String tz = getDomain().getTimeZone();
							ZoneId domain_zoneid;
							if (tz==null)
								tz="Z";
							try { 
								domain_zoneid=ZoneId.of(getDomain().getTimeZone());
								
							} catch (Exception e) {
								logger.error(e);
								domain_zoneid=ZoneId.of("Z");
							}
							
							
							Calendar cal=Calendar.getInstance();
							cal.setTime(date);
							
							LocalDate ldate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));

							ZonedDateTime zdt = ldate.atStartOfDay(domain_zoneid);
							
							OffsetDateTime dateTime = OffsetDateTime.ofInstant(zdt.toInstant(), domain_zoneid);
							
							if (dateTime.getYear()<100) {
								dateTime = OffsetDateTime.of(dateTime.getYear()+2000,
										 dateTime.getMonthValue(),
										 dateTime.getDayOfMonth(),
										 dateTime.getHour(),
										 dateTime.getMinute(),
										 dateTime.getSecond(),
										 0,
										 ZoneOffset.from(dateTime));
										 setValue(Date.from(dateTime.plusDays(2).toInstant()));
							}

							
							String stringvalue = ServiceLocator.getService(DateTimeService.class).getStr_ISO_OFFSET_DATE_TIME(dateTime);
							addValue(stringvalue);
							
							/**
							 * We will save the OffsetDateTime of the Date selected in the GMT of the Domain
							 */
							
							// logger.debug("DateField.onUptdate()  getValue()            -> " + getValue().toString());
							// logger.debug("DateField.onUptdate() stringvalue            -> " + stringvalue );
							
							setValue(null);
							setDate(null);
							fireScanAll(new EditorEvent(target, getAttribute()));
						}
						target.add(AttributeEditor.this);
					}	
					@Override
					public boolean isVisible() {
						return isEditionEnabled() && !isReadOnly();
					}	
				});
				if (getAttribute().getValidator()!=null) {
					((DateField)get("value")).add(new AttributeValidator<Date>());
				}
			}
			else {
				if (getAttribute().getType().equals(com.novamens.content.model.AttributeType.BOOLEAN)) {
					add(new BooleanField("value", new PropertyModel<Boolean>(AttributeEditor.this, "booleanValue")) {
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							addValue(getValue().toString());
							fireScanAll(new EditorEvent(target, getAttribute()));
							target.add(AttributeEditor.this);
							AttributeEditor.this.onUpdate(target);
						}
						@Override
						public boolean isVisible() {
							return isEditionEnabled() && !isReadOnly();
						}	
					}); 
				}
				else {
					TextField<String> field = new TextField<String>("value", new PropertyModel<String>(AttributeEditor.this, "value")) {
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							if (!hasErrorMessage()) {
								addValue(getValue());
								fireScanAll(new EditorEvent(target, getAttribute()));
							}
							target.add(AttributeEditor.this);
							AttributeEditor.this.onUpdate(target);
						}
						@Override
						public boolean isVisible() {
							return isEditionEnabled() && !isReadOnly();
						}
						@Override
						protected void onKey(AjaxRequestTarget target, String jsKeycode) {
							AttributeEditor.this.onBlur(target);
						}
					};
					if (getAttribute().getType().equals(AttributeType.NUMBER) ||
							getAttribute().getType().equals(AttributeType.FLOAT)) {
						field.add(new NumericValidator());
					}
					if (getAttribute().getValidator()!=null) {
						field.add(new AttributeValidator<String>());
					}
					add(field);
				}
			}
			
			WebMarkupContainer leavevalues = new WebMarkupContainer("leavevalues-container") {
				@Override
				public boolean isVisible() {
					return isBatchClassification() && !isReadOnly();
				}
			};
			
			leavevalues.add(new AjaxCheckBox("check", new PropertyModel<Boolean>(AttributeEditor.this, "leaveValues")) {
				protected void onUpdate(AjaxRequestTarget target) {
					if (getLeaveValues()) removeAllValues();
					target.add(AttributeEditor.this.get("container"));
				}
			});
			
			
			WebMarkupContainer calculationinfo = new WebMarkupContainer("calculation-info") {
				public boolean isVisible() {
					return AttributeSource.Script.equals(getTemplate().getSource()) && getTemplate().getCalculationScript()!=null;
				}
			};
			calculationinfo.add(new Label("calculated-message", getLabel("calculated.message")));
			Label scriptCode = new Label("script-code", getTemplate().getCalculationScript());
			scriptCode.setVisible(false);
			IModel<String> scriptErrorModel = new Model<String>() {
				public String getObject() {
					return AttributeEditor.this.errorMessage;
				}
			};
			Label errorMessage = new Label("error-message", scriptErrorModel) {
				public boolean isVisible() {
					return AttributeEditor.this.errorMessage!=null;
				}
			};
			calculationinfo.setOutputMarkupId(true);
			calculationinfo.add(scriptCode);
			calculationinfo.add(errorMessage);
			calculationinfo.add(new AjaxLink<Void>("script-link") {
				public void onClick(AjaxRequestTarget target) {
					scriptCode.setVisible(!scriptCode.isVisible());
					target.add(calculationinfo);
				}
			});
			add(calculationinfo);
			
			WebMarkupContainer checklabel = new WebMarkupContainer("label");
			checklabel.add(new AttributeModifier("for", new Model<String>() {
				public String getObject() {
					return leavevalues.get("check").getMarkupId();
				}
			}));
			
			leavevalues.add(checklabel);
			
			add(leavevalues);
			
			IModel<String> errorModel = new Model<String>() {
				public String getObject() {
					return getError();
				}
			};
			add((new Label("error-message", errorModel) {
				public boolean isVisible() {
 					return getError()!=null;
				}
			}).setEscapeModelStrings(false) );
		}
	}
	
	
	/**
	 * @param id
	 * @param templatemodel
	 * @param base
	 */
	public AttributeEditor(String id, IModel<AttributeTemplate> templatemodel, int base) {
		super(id);
		
		int index = base * 10;
	
		setOutputMarkupId(true);
		setTemplate(templatemodel);
		
		WebMarkupContainer container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		container.add(new AttributeModifier("tabindex", String.valueOf(index)));
		
		container.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				String editioncss = (isEditionEnabled() ? " editing ":"") + (isEditable() ? " editable ": " readonly");
				String errorcss = getError()!=null ? " errors " : "";
				return "toleft col-lg-12 col-md-12 col-xs-12 members-editor "+ editioncss + errorcss;
			}
		}));
		
		add(container);		
		container.add(new Label("attribute-name", getAttribute().getName()));
		container.add(new WebMarkupContainer("mandatory") {
			public boolean isVisible() {
				return (getTemplate().getMultiplicity()==Multiplicity.M11 ||  getTemplate().getMultiplicity()==Multiplicity.M1N) && !isReadOnly();
			}
		});
		
		WebMarkupContainer mk = new WebMarkupContainer("icon") {
			@Override
			public boolean isVisible() {
				return false;
			}
		};
		
		mk.add(new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				if (isEditionEnabled()) {
					return (AttributeEditor.this.isReadOnly()?" readonly " : "");
				}
				else {
					return "far fa-edit"  + (AttributeEditor.this.isReadOnly()?" readonly " : "" );
				}
			}
		}));
		
		container.add(mk);
		
		get("container:attribute-name").add(new AjaxEventBehavior("click") {
			@Override
			protected void onEvent(AjaxRequestTarget target) {
				if (!getEditor().isEditionEnabled()) { 
					return;
				}
				setEditionEnabled(!isEditionEnabled() || isBatchClassification());
				if (isEditionEnabled())	
					onEdit(target);
				((Field<?>)AttributeEditor.this.get("container:editor:value")).onBeforeRender();
				target.focusComponent(((Field<?>)AttributeEditor.this.get("container:editor:value")).getInput());
				
				((Field<?>)AttributeEditor.this.get("container:editor:value")).add(new AttributeModifier("tabindex", String.valueOf(index+4)));
				
				target.add(AttributeEditor.this);
			}
		});
		
		updated = false;
	}
	
	public void setFocus(AjaxRequestTarget target) {
		if (!getEditor().isEditionEnabled()) { 
			return;
		}
		setEditionEnabled(true);
		if (isEditionEnabled())	
			onEdit(target);
		
		((Field<?>)get("container:editor:value")).onBeforeRender();
		
		target.focusComponent(((Field<?>)get("container:editor:value")).getInput());
		target.add(AttributeEditor.this);
	}

	@Override
	public void updateModel() {
		
		if (!updated || getLeaveValues()) 
			return;
		
		
		//if ( this.getTemplate().getAttribute().getType()==AttributeType.TEXT) {
		//	getEditor().getModelObject().getA
		//	
	//	}
		
		getEditor().getModelObject().setAttributeValues(getTemplate().getAttribute(), getValues());
		
		setUpdatedPart(getTemplate().getAttribute().getName().toLowerCase());
		
		updated = false;
	}
	
	public void update(T content) {
		if (!this.updated) 
			return;
		content.setAttributeValues(getTemplate().getAttribute(), getValues());
	}
	 
	public void setAsDefault() {
		boolean first = true;
		StringBuffer buffer = new StringBuffer();
		for (String value : getValues()) {
			if (!first) 
				buffer.append(";");
			buffer.append(value.replace(";", ""));
			first = false;
		}
		getSessionUser().getService(PreferencesService.class).setValue("default-"+ getEditor().getModelObject().getContentTemplate().getName(), getAttribute().getName(), buffer.toString());
	}
	
	public void setReadOnly(boolean b) {
		this.read_only=b;
	}

	public boolean isReadOnly() {
		return this.read_only;
	}
	
	public void clearDefault() {
		getSessionUser().getService(PreferencesService.class).setValue("default-"+ getEditor().getModelObject().getContentTemplate().getName(), getAttribute().getName(), "");
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
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public Boolean getBooleanValue() {
		return value!=null && value.equals(Boolean.TRUE.toString()) ? Boolean.TRUE  : Boolean.FALSE ;
	}
	
	public void  setBooleanValue(Boolean value) {
		this.value = value.toString(); 
	}
	
	public Long getIntegerValue() {
		if (value!=null) {
			try {
				Long integervalue = Long.valueOf(value);
				return integervalue;
			}
			catch (Exception e) {
				return null;
			}
		}
	 	return null;
	}
	
	public void  setIntegerValue(Integer value) {
		this.value = value.toString(); 
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public List<String> getValues() {
		return values;
	}
	
	public void setValues(AttributeTemplate template) {
		Attribute attribute = template.getAttribute();
		values = getModelObject().getAttributeValues(attribute);

		//if (logger.isDebugEnabled())
		//			values.forEach(item -> logger.debug(item));
		
		
		Multiplicity multiplicity = getTemplate().getMultiplicity();
		if (multiplicity==null) multiplicity = attribute.getMultiplicity();
		if (multiplicity.equals(Multiplicity.M11) || multiplicity.equals(Multiplicity.M01)) {
			if (!values.isEmpty()) {
				setValue(values.get(0));
			}
		}
	}
	
	public void setEditionEnabled(boolean value) {
		editionEnabled = value;
	}
	
	public boolean isUpdated() {
		return updated;
	}

	@Override
	public void cancel() {
		updated = false;
	}
	
	@Override
	public void onBeforeRender() {
		if (getValues().isEmpty() && !updated) {
			setValues(getTemplate());
		}
		super.onBeforeRender();
		if (get("container:elements-container")==null) {
			addValuesView();
		}
		
		if ((parentsEnabled() && getTemplate().getParent()!=null) || 
				AttributeSource.Script.equals(getTemplate().getSource())) {
			this.read_only = true;
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.templatemodel.detach();
	}
	
	public void setIsEditable(boolean b) {
		this.isEditable=b;
	}

	public boolean isEditable() {
		return this.isEditable;
	}
	
	public boolean isBatchClassification() {
		return false;
	}
	
	public void setLeaveValues(boolean value) {
		this.leavevalues = value;
	}
	
	public boolean getLeaveValues() {
		return leavevalues;
	}
	
	public List<DataSetMember> getClassification() {
		return null;
	}
	
	protected void onEdit(AjaxRequestTarget target) {
	}
	
	protected void addValuesView() {
		
		WebMarkupContainer elementscontainer = new WebMarkupContainer("elements-container");
		
		elementscontainer.add( new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				if (isEditable())
					return "elements-container editable";
				else
					return "elements-container readonly";
			}}) {
		}); 
		
		((WebMarkupContainer) get("container")).add(elementscontainer);
		
		elementscontainer.add(new ListView<String>("attribute", new PropertyModel<List<String>>(AttributeEditor.this, "values")) {
			public void populateItem(final ListItem<String> item) {
				item.add(new Label("value", getDisplayValue(item.getModelObject())));
				item.add (new AjaxLink<Void>("remove-link") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						removeValue(item.getModelObject());
						fireScanAll(new EditorEvent(target, getAttribute()));
						target.add(AttributeEditor.this);
					}
					@Override
					public boolean isVisible() {
						return isEditionEnabled() && !isReadOnly();
					};
				});
				item.add(new WebMarkupContainer("separator") {
					public boolean isVisible() {
						return !isEditionEnabled() && 
								getValues().size()>1 && item.getIndex()<getValues().size()-1;
					}
				});
			}
		});
		
		elementscontainer.add(new WebMarkupContainer("nullmember") {
			public boolean isVisible() {
				return isBatchClassification() && getValues().isEmpty() && !getLeaveValues(); 
			}
		});
		

		
		elementscontainer.add(new WebMarkupContainer("leavevalues-message") {
			public boolean isVisible() {
				return getLeaveValues() && !isReadOnly();
			}
		});
	
		((WebMarkupContainer) get("container")).add(new ValueEditorFragment("editor") {
			public boolean isVisible() {
				//return false;
				return isEditionEnabled();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public void onUpdate(AjaxRequestTarget target, ModelElement element) {
		
		if (parentsEnabled()) {
			
			if (AttributeSource.Script.equals(getTemplate().getSource()) &&
				getTemplate().getCalculationScript()!=null) {
				Content content = getModelObject();
				getEditor().update((T)content);
				try {
					errorMessage = null;
					Object evaluation = (new KbeeCodeExecutor()).execute(getTemplate().getCalculationScript(), content);
					if (evaluation!=null) {
						if (!getValues().contains(evaluation.toString())) {
							this.updated = true;
							addValue(evaluation.toString());
							onUpdate(target);
						}
					}
				}
				catch (Exception e) {
					errorMessage = e.getMessage();
					logger.error(e);
				}
				target.add(this);
				return;
			}
			
			
			if (!(element instanceof Classifier) ||
				getTemplate().getParent()==null || 
				!getTemplate().getParent().equals((Classifier)element))
				return;
			
			this.updated = true;
			
			this.values.clear();
			
			for (DataSetMember member : getClassification((Classifier)element)) {
				List<String> values = member.getAttributeValues(getAttribute());
				for (String value : values) {
					addValue(value);
				}
			}
		}
		
		target.add(this);
		
		onUpdate(target);
	}
	
	public boolean isEditionEnabled() {
		return editionEnabled;
	}
	
	@Override
	public void onAfterRender() {
		super.onAfterRender();
		getFeedbackMessages().clear();
	}
	
	protected void onBlur(AjaxRequestTarget target) {
		
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
	}
	
	protected boolean addValue(String value) {
		
		if (!isValid(value)) {
			return false;
		}
		
		if (getValues().contains(value))
			return false;
		
		Multiplicity multiplicity = getTemplate().getMultiplicity()!=null ? getTemplate().getMultiplicity() : getAttribute().getMultiplicity();
		
		if (multiplicity.equals(Multiplicity.M0N) || multiplicity.equals(Multiplicity.M1N) || getValues().isEmpty()) {
			if (value!=null && !"".equals(value.trim())) {
				values.add(value);
			}
		}	
		else {
			if (value==null || "".equals(value.trim())) {
				values.remove(0);
			}	
			else {
				values.set(0, value);
			}
		}	
		
		updated = true;
		
		setLeaveValues(false);

		return true;
	}
	
	protected String getDisplayValue(String value) {
		
		if (getAttribute().isDate()) {
			try {
				OffsetDateTime odate=ServiceLocator.getService(DateTimeService.class).parseStrDate(value);
				value=ServiceLocator.getService(DateTimeService.class).getDateDisplayString(odate, getSessionUser().getLocale());
				return value;
			}
			catch (Exception e) {
				logger.error(e);
				return "err";
			}
		}
		else 
		if (getAttribute().getType().equals(com.novamens.content.model.AttributeType.BOOLEAN)) {
			try {
				return (new StringResourceModel(value, this, null)).getObject();
			}
			catch (Exception e) {
				return (new StringResourceModel(Boolean.FALSE.toString(), this, null)).getObject();
			}
		}
		else {
			return value;
		}
	}
	
	protected void removeValue(String value) {
		setValue(null);
		getValues().remove(value);
		updated = true;
	}
	
	protected void removeAllValues() {
		setValue(null);
		getValues().clear();
		updated = true;
	}
	
	protected List<DataSetMember> getClassification(DataSet dataset) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (DataSetMember member : getClassification()) {
			if (member.getDataSet().equals(dataset)) {
				members.add(member);
			}
		}
		return members;
	}
	
	protected List<DataSetMember> getClassification(Classifier classifier) {
		return new ArrayList<DataSetMember>();
	}
	
	private boolean isValid(String value) {
		if (getAttribute().getType().equals(AttributeType.NUMBER) && (value!=null && !"".equals(value.trim()) && !isNumber(value))) {
			return false;
		}
		if (getAttribute().getType().equals(AttributeType.FLOAT) && value!=null && !"".equals(value.trim())) {
			try {
				Float.valueOf(value);
			}
			catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isNumber(String value) {
		for (char c : value.toCharArray()) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}
	
	private KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	private boolean parentsEnabled() {

		return true;
		//if (parentsEnabled == null) {
		//	String value = ServiceLocator.getService(SystemParameterService.class).getParameter("com.novamens.content.contentclass.parentsenabled", "false");
		//	parentsEnabled = "true".equals(value);
		//}
		//return parentsEnabled;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
