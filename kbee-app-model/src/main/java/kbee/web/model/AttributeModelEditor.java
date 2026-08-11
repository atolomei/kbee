 package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.dao.DataIntegrityViolationException;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.AttributeValidator;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.DOMObjectService;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.NumberField;

import kbee.util.logging.Logger;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

/**
 * 
 * DomainIObjectMainPanel
 * 
 */
@SuppressWarnings("serial")
public class AttributeModelEditor extends DomainObjectEditor<Attribute> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(AttributeModelEditor.class.getName());
	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

	private boolean is_alias_null = false;
	private boolean is_predicate_null = false;
	private AttributeType type;
	
	class UniquenessValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String Attributename = validatable.getValue();
			
			for (Attribute  Attribute : getAttributes()) {
				if (!Attribute.equals(AttributeModelEditor.this.getModelObject())) {
					if (Attribute.getName()!=null && Attribute.getName().equals(Attributename)) {
						validatable.error(new ValidationError(this));
					}
				}
			}
		}
	}
	
	class UniquenessKeyValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String classifierkey = validatable.getValue();
			for (Attribute  classifier : getAttributes()) {
				if (!classifier.equals(AttributeModelEditor.this.getModelObject())) {
					if (classifier.getAlias()!=null && classifier.getAlias().equals(classifierkey)) {
						validatable.error(new ValidationError(this));
					}
				}
			}
		}
	}
	
	public interface ValidatorEditor {
		public AttributeValidator getValidator();
		public boolean isEditorEnabled();
	}
	

	/**
	 * @param id
	 * @param model
	 * @param isnew
	 */
	public AttributeModelEditor() {
		super("editor");
	}
	
	public AttributeModelEditor(IModel<Attribute> model, final boolean isnew) {
		this("editor", model, isnew);
	}

	public AttributeModelEditor(String id, IModel<Attribute> model, final boolean isnew) {
		super(id, model);
								
		setOutputMarkupId(true);
		
		setIsNew(isnew);
		setEditionEnabled(isnew);
		
		setType(getModelObject().getType());
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		WebMarkupContainer sapi = new WebMarkupContainer("isapi");
		sapi.setVisible(model.getObject().isAPIClassifier());
		form.add(sapi);
		
		this.is_alias_null	= isnew || (getModelObject().getAlias()==null || getModelObject().getAlias().length()==0);
		this.is_predicate_null 	= isnew || (getModelObject().getPredicate()==null || getModelObject().getPredicate().length()==0);
		
		form.add(new TextField<String>("name", true, new UniquenessValidator()) {
			@Override
			@SuppressWarnings("unchecked")
			public void onUpdate(AjaxRequestTarget target) {
				if (is_alias_null && getValue()!=null) {
					((KbeeAttribute)getAttribute()).setAlias(parseAlias(super.getValue()));
					((TextField<String>)AttributeModelEditor.this.get("form:alias")).setValue(getAttribute().getAlias());
				}
				if (is_predicate_null && super.getValue()!=null) {
					((KbeeAttribute)getAttribute()).setPredicate(parsePredicate(super.getValue()));
					((TextField<String>) AttributeModelEditor.this.get("form:predicate")).setValue(getAttribute().getPredicate());
				}
				target.add(AttributeModelEditor.this);
			}
		});
		form.add(new TextField<String>("alias", true, new UniquenessKeyValidator()) {
			@Override
			public boolean isEnabled() {
				return isNew() || isRoot(); 
			}
			
			
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				is_alias_null = !"".equals(getValue());
				AttributeModelEditor.this.getModelObject().setAlias(AttributeModelEditor.this.parseAlias(getValue()));
				setValue(AttributeModelEditor.this.parseAlias(getValue()));
				target.add(AttributeModelEditor.this);
			}
			
		});
		
		form.add(new ChoiceField<AttributeType>("type", new PropertyModel<List<AttributeType>>(this, "types"), true) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setType(getValue());
				target.add(AttributeModelEditor.this);
			}
		});
		
		form.add(new ChoiceField<Multiplicity>("multiplicity", new PropertyModel<List<Multiplicity>>(this, "multiplicities"), true));

		form.add(new TextField<String>("predicate") {
			@Override
			public boolean isEnabled() {
				return isNew() || isRoot(); 
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				is_predicate_null = !"".equals(getValue());;
			}
		});
		
		form.add(new BooleanField("isFilterable"));
		form.add(new BooleanField("isRuleCondition"));
		form.add(new BooleanField("isIdentityDocument"));
		form.add(new BooleanField("isSearchable"));
		form.add(new BooleanField("isDefaultStructure"));
		form.add(new NumberField<Integer>("boostFactor"));
		
		add(form);
		
		add(new EditButtonsV5<Attribute>(this)  {
			@Override
			public boolean isEnabled() {
				if (isRoot())
					return true;
				if (getModel().getObject().isOnlyRootEdit())
					return false;
				return (role_admin || role_model);
			}
		});
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				if (getModelObject().getType()==AttributeType.TEXT) {
					
					((KbeeAttribute) getModelObject()).setOrdered(false);
					((KbeeAttribute) getModelObject()).setDefaultGridColumn(false);
					((KbeeAttribute) getModelObject()).setFilterable(false);
					
					
				}
				
				int mandatory    = getModelObject().isRequired() ?  0 : 1;				
				int zero_or_one  =  ((getModelObject().getMultiplicity()==Multiplicity.M01) || (getModelObject().getMultiplicity()==Multiplicity.M11)) ? 0 : 1;
				int order  = mandatory * 3  +   zero_or_one * 1;// + metadata * 1000;
				((KbeeAttribute)getModelObject()).setOrder(order);
				//((KbeeAttribute)getModelObject()).setValidator(getValidator());
				getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public void setType(AttributeType type) {
		this.type = type;
	}
	
	public AttributeType getType() {
		return type;
	}
	
	public List<Multiplicity> getMultiplicities() {
		List<Multiplicity> multiplicities = new ArrayList<Multiplicity>();
		multiplicities.add(Multiplicity.M01);
		multiplicities.add(Multiplicity.M11);
		multiplicities.add(Multiplicity.M0N);
		multiplicities.add(Multiplicity.M1N);
		return multiplicities;
	}
	
	public List<AttributeType> getTypes() {
		List<AttributeType> types = new ArrayList<AttributeType>();
		types.add(AttributeType.DATE);
		types.add(AttributeType.NUMBER);
		types.add(AttributeType.FLOAT);
		types.add(AttributeType.STRING);
		types.add(AttributeType.TEXT);
		types.add(AttributeType.BOOLEAN);
		types.add(AttributeType.HTML);
		types.add(AttributeType.VALIDITY_FROM);
		types.add(AttributeType.VALIDITY_TO);
		return types;
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	@Override
	public void cancel(AjaxRequestTarget target) {
		if (isNew()) {
			try {
				getModelObject().getService(DOMObjectService.class).delete();
			}
			catch (DataIntegrityViolationException e) {
				logger.error(e);
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		onCancel(target);
	}
	
	public List<Attribute> getAttributes() {
		return getContentDao().getAttributes(getDomain());
	}
	
	protected void onCancel(AjaxRequestTarget target) {
		onClose(target);
	}
	
	protected void onClose(AjaxRequestTarget target) {
	}
	
	private Attribute getAttribute() {
		return getModelObject();
	}
}
