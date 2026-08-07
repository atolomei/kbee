package com.novamens.content.web.security.markup;

import java.time.OffsetDateTime;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.util.Assert;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeType;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.content.security.PredicatesIqlEvaluator;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.DateField;
import com.novamens.wicket.markup.html.form.TextField;

import kbee.web.event.wicket.EditorEvent;

@SuppressWarnings("serial")
public class AttributeConditionEditor<T> extends ObjectEditorPanel<T>  {
	private static final long serialVersionUID = 1L;
																										
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AttributeConditionEditor.class.getName());
	
	private boolean updated = false;
	private boolean isEditable = true;				// el campo puede ser editado desde donde es invocado 
	private boolean read_only;						// el campo es readonly (intrinseco al campo)
	
	private IModel<Attribute> model;
	private String value;
	
	private Date date;
	
	private List<String> values = new ArrayList<String>();
	
	class NumericValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			
			String value = validatable.getValue();
			
			if (!isValid(value)) {
				validatable.error(new ValidationError(this, "not-number"));
			}
		}
	}
	
	public class ValueEditorFragment extends Fragment {

		public ValueEditorFragment(String id) {
			super(id, "editor-fragment", AttributeConditionEditor.this);
			
			if (getAttribute().isDate()) {
				add(new DateField("value", new PropertyModel<Date>(AttributeConditionEditor.this, "date")) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						String stringvalue = ServiceLocator.getService(DateTimeService.class).getStr_ISO_OFFSET_DATE_TIME(getValue());
						addValue(stringvalue);
						setValue(null);
						setDate(null);
						fire(new EditorEvent(target));
						target.focusComponent(getInput());
						target.add(AttributeConditionEditor.this);
					}	
					@Override
					public boolean isVisible() {
						return isEditionEnabled() && !isReadOnly();
					}	
				});
			}
			else {
				if (getAttribute().getType().equals(com.novamens.content.model.AttributeType.BOOLEAN)) {
					add(new BooleanField("value", new PropertyModel<Boolean>(AttributeConditionEditor.this, "booleanValue")) {
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							addValue(getValue().toString());
							fire(new EditorEvent(target));
							target.add(AttributeConditionEditor.this);
						}
						@Override
						public boolean isVisible() {
							return isEditionEnabled() && !isReadOnly();
						}	
					}); 
				}
				else {
					TextField<String> field = new TextField<String>("value", new PropertyModel<String>(AttributeConditionEditor.this, "value")) {
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							if (!isValid(getValue())) {
								setError((new ValidationError()).addKey("requiredvalidator.message"));
							}
							else {
								getFeedbackMessages().clear();
								addValue(getValue());
								AttributeConditionEditor.this.onUpdate(target);
								fire(new EditorEvent(target));
							}
							target.add(AttributeConditionEditor.this);
						}
						@Override
						public boolean isVisible() {
							return isEditionEnabled() && !isReadOnly();
						}	
					};
					if (getAttribute().getType().equals(com.novamens.content.model.AttributeType.NUMBER)) {
						field.add(new NumericValidator());
					}
					add(field);
				}
			}
		}
	}
	
	
	/**
	 * @param id
	 * @param templatemodel
	 * @param base
	 */
	public AttributeConditionEditor(String id, IModel<Attribute> model) {
		super(id);
	
		setOutputMarkupId(true);
		setAttribute(model);
		setValues(getCondition());
		
		add(new Label("attribute-name", getAttribute().getName()));
		
		updated = false;
	}
	
	public String getAttributeCondition() {
		
		StringBuffer condition = new StringBuffer();
		
		List<String> values = getValues();
		
		if (values.isEmpty()) 
			return "";
		
		String predicate = "a"+String.valueOf(getAttribute().getId());
		
		condition.append("(");
		int m = 0;
		for (String value : values) {
			if (m>0)
				condition.append(" or ");
			condition.append(predicate);
			condition.append("(");
			condition.append(value);
			condition.append(")");
			m++;
		}
		condition.append(")");
		
		return condition.toString();
	}
	
	public String getDescription() {
		StringBuffer condition = new StringBuffer();
		List<String> values = getValues();
		if (values.isEmpty()) 
			return "";
		
		String predicate = getAttribute().getPredicate();

		condition.append("<span class= \"predicate\" >" + predicate+"</span>");
		
		int m = 0;
		for (String value : values) {
			if (m>0)
				if (m==values.size()-1)
					condition.append("<span class= \"logical-operator\" > or "+"</span> ");
				else
					condition.append("<span class= \"logical-operator\" > or "+"</span> ");
			
			if (m==0)
				condition.append("<span class= \"iql-group-start\"> ( </span> ");
			
			condition.append("<span class= \"iql-value\" >"+ value +"</span> ");
			
			if (m==values.size()-1)
				condition.append("<span class= \"iql-group-end\"> ) </span> ");
			m++;
		}
		
		return condition.toString();
	}

	@Override
	public void updateModel() {
		
		if (!updated) 
			return;
		
		updated = false;
	}
	
	public void setReadOnly(boolean b) {
		this.read_only=b;
	}

	public boolean isReadOnly() {
		return this.read_only;
	}
	
	public void setAttribute(IModel<Attribute> model) {
		this.model = model;
	}
	
	public Attribute getAttribute() {
		return model.getObject();
	}
	
	public String getCondition() {
		return null;
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
	
	public boolean isUpdated() {
		return updated;
	}

	@Override
	public void cancel() {
		updated = false;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.model.detach();
	}
	
	public void setIsEditable(boolean b) {
		this.isEditable=b;
	}

	public boolean isEditable() {
		return this.isEditable;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("attribute")==null) {
			addValuesView();
		}
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
	}
	
	protected void addValuesView() {
		
		
		add(new ListView<String>("attribute", new PropertyModel<List<String>>(AttributeConditionEditor.this, "values")) {
			public void populateItem(final ListItem<String> item) {
				item.add(new Label("value", getDisplayValue(item.getModelObject())));
				item.add (new AjaxLink<Void>("remove-link") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						removeValue(item.getModelObject());
						fire(new EditorEvent(target));
						onUpdate(target);
						target.add(AttributeConditionEditor.this);
					}
					@Override
					public boolean isVisible() {
						return isEditionEnabled() && !isReadOnly();
					};
				});
				if (isEditionEnabled())
					item.add(new AttributeModifier("class", "list-group-item editmode"));
			}
		});
	
		add(new ValueEditorFragment("editor") {
			public boolean isVisible() {
				return isEditionEnabled();
			}
		});
	}
	
	public boolean isEditionEnabled() {
		return getEditor().isEditionEnabled();
	}
	
	protected boolean addValue(String value) {
		if (getAttribute().getType().equals(AttributeType.NUMBER) && (value!=null && !"".equals(value.trim()) && !isNumber(value))) {
			return false;
		}
		if (getValues().contains(value))
			return false;
		if (value!=null && !"".equals(value.trim())) {
			values.add(value);
		}
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
			return (new StringResourceModel(value, this, null)).getObject();
		}
		else {
			return value;
		}
	}
	
	private void setValues(String condition) {
		
		Assert.isTrue(getAttribute().getPredicate()!=null, "predicate not found!");
		
		this.values = new ArrayList<String>();
		
		if (condition==null || "".equals(condition) || condition.contains("null")) 
			return;
		
		Expression iqlexpression = getAttribute().getDomain().getService(IqlService.class).getExpression(condition);
		
		PredicatesIqlEvaluator evaluator = new PredicatesIqlEvaluator(iqlexpression);
		Map<String, List<String>> predicates = evaluator.evaluate();
		List<String> values = predicates.get(getAttribute().getPredicate());
		
		if (values!=null)
			this.values = values;
	}
	
	private void removeValue(String value) {
		setValue(null);
		getValues().remove(value);
		updated = true;
	}
	
	private boolean isValid(String value) {
		if (getAttribute().getType().equals(AttributeType.NUMBER) && (value!=null && !"".equals(value.trim()) && !isNumber(value))) {
			return false;
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
}
