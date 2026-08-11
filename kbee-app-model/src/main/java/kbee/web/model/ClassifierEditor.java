package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.DOMObjectService;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

/**
 * 
 * [Server Code - bucketName - ObjectName] 
 *  
 *  RPMinio1
 *  RPMinio2
 *  RPMinio3
 *  RPMinio4
 *  RPMinio5
 *
 */
@SuppressWarnings("serial")
public class ClassifierEditor extends DomainObjectEditor<Classifier> {
	private static final long serialVersionUID = 1L;
	
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ClassifierEditor.class.getName());

	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

	class UniquenessValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String classifiername = validatable.getValue();
			
			for (Classifier  classifier : getClassifiers()) {
				if (!classifier.equals(ClassifierEditor.this.getModelObject())) {
					if (classifier.getName()!=null && classifier.getName().equals(classifiername)) {
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
			for (Classifier  classifier : getClassifiers()) {
				if (!classifier.equals(ClassifierEditor.this.getModelObject())) {
					if (classifier.getAlias()!=null && classifier.getAlias().equals(classifierkey)) {
						validatable.error(new ValidationError(this));
					}
				}
			}
		}
	}
	

	/**
	 * @param model
	 * @param isnew
	 */
	public ClassifierEditor(IModel<Classifier> model, final boolean isnew) {
		this("editor", model, isnew);
	}
	
	public ClassifierEditor(String id, IModel<Classifier> model, final boolean isnew) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		setIsNew(isnew);
		setEditionEnabled(isnew);

		add(new InfoDialog("help-modal"));
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		WebMarkupContainer sapi = new WebMarkupContainer("isapi");
		sapi.setVisible(model.getObject().isAPIClassifier());
		form.add(sapi);
		
		// Structure ---------------------------------------------------------------
		//
		form.add(new TextField<String>("name", true, new UniquenessValidator()) {
			@Override
			@SuppressWarnings("unchecked")
			public void onUpdate(AjaxRequestTarget target) {
				if (isNew() && super.getValue()!=null) {
					ClassifierEditor.this.getModelObject().setAlias(parseAlias(super.getValue()));
					((KbeeClassifier)ClassifierEditor.this.getModelObject())
						.setPredicate(parsePredicate(super.getValue()));
					((TextField<String>) ClassifierEditor.this.get("form:alias"))
						.setValue(ClassifierEditor.this.getModelObject().getAlias());
					((TextField<String>) ClassifierEditor.this.get("form:predicate"))
						.setValue(ClassifierEditor.this.getModelObject().getPredicate());
					target.add(ClassifierEditor.this);
				}
			}
		});
		
		form.add(new TextAreaField<String>("description"));
		
		form.add(new TextField<String>("alias", true, new UniquenessKeyValidator()) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				ClassifierEditor.this.getModelObject().setAlias(ClassifierEditor.this.parseAlias(getValue()));
				setValue(ClassifierEditor.this.parseAlias(getValue()));
				target.add(ClassifierEditor.this);
			}
		});
		
		form.add(new ChoiceField<DataSet>("dataSet", () -> getDataSets(), true));
		
		form.add(new ChoiceField<DataSet>("dataSet2", () -> getDataSets(), false) {
			public boolean isNullValid() {
				return true;
			}
		});
		
		form.add(new BooleanField("hasHome"));
		
		form.add(new ChoiceField<Multiplicity>("multiplicity", () -> getMultiplicities(), true) {
			@Override
			protected String getDisplayValue(Multiplicity value) {
				return value.getLabel(getSessionUser().getLocale());
			}
			
			@Override
			public boolean isHelpInfo() {
				return false;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return getText("multiplicity.modal.title").getObject(); }, getText("multiplicity.modal.text"));
			}
		});
		
		form.add(new BooleanField("isDefaultStructure"));
		
		form.add(new BooleanField("isRuleCondition"));
		
		form.add(new TextField<String>("predicate", true) {
			@Override
			public boolean isEnabled() {
				return isRoot();
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				// is_predicate_null = false;
			}
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return getText("multiplicity.modal.title").getObject(); }, getText("multiplicity.modal.text"));
			}
		});
		
		
		// Semantic --------------------------------------------------------------
		//
		form.add(new BooleanField("isContentType") {
			@Override
			public boolean isHelpInfo() {
				return false;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return getText("isContentType.helptext.title").getObject(); }, getText("isContentType.helptext"));
			}
		});
		

		
		// Semantic --------------------------------------------------------------
		//
		form.add(new BooleanField("isDistribution") {
			@Override
			public boolean isHelpInfo() {
				return false;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return getText("isDistribution.helptext.title").getObject(); }, getText("isDistribution.helptext"));
			}
		});
		
		form.add(new BooleanField("isWorkflowStatus") {
			@Override
			public boolean isHelpInfo() {
				return false;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return getText("isContentType.helptext.title").getObject(); }, getText("isContentType.helptext"));
			}
		});
		
		form.add(new BooleanField("isOrganization"));
		
		form.add(new BooleanField("isIdentityDocumentType"));
		
		form.add(new BooleanField("isSearchable"));
		
		form.add(new BooleanField("isHierarchical"));
		
		form.add(new BooleanField("isMyDocument"));
 
		form.add(new BooleanField("isPortal") {
			@Override
			public boolean isHelpInfo(){
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return getText("isportal.helptext.title").getObject(); }, getText("isportal.helptext"));
			}
		});
		
		add(form);
		
		add(new EditButtonsV5<Classifier>(this)  {
			@Override
			public boolean isEnabled() {
				if (isRoot())
					return true;
				if (ClassifierEditor.this.getModel().getObject().isOnlyRootEdit())
					return false;
				return role_admin || role_model;
			}
		});
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				int content_type = getModelObject().isContentType()          ?  0 : 1;
				int mandatory    = getModelObject().isMandatory()            ?  0 : 1;				
				int haspredicate = (getModelObject().getPredicate() != null) ?  0 : 1; 				
				int metadata     = getModelObject().isMetadataSubtitle() ?  0 : (getModelObject().isContentType() ? 0 : 1);				
				
				int one_or_more  =  ((getModelObject().getMultiplicity()==Multiplicity.M1N) || (getModelObject().getMultiplicity()==Multiplicity.M11)) ? 0 : 1;
				
				int before_after_system_facets = 0;
				
				if (content_type + mandatory > 0)
					before_after_system_facets = Classifier.SYSTEM_FACETS_THRESHOLD;
						
				int order  = content_type * 4 + mandatory * 3  +  before_after_system_facets + haspredicate * 1  + one_or_more * 2 + metadata * 100;
				
				getModelObject().setOrder(order);
				getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				reset();
			}
		}
		
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}

	public void onUpdate(AjaxRequestTarget target) {
	}
	
	public List<Multiplicity> getMultiplicities() {
		List<Multiplicity> multiplicities = new ArrayList<Multiplicity>();
		multiplicities.add(Multiplicity.M01);
		multiplicities.add(Multiplicity.M11);
		multiplicities.add(Multiplicity.M0N);
		multiplicities.add(Multiplicity.M1N);
		return multiplicities;
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
			onClose(target);
		}
		else
			onCancel(target);
	}
	
	public List<DataSet> getDataSets() {
		return getContentDao().getDataSets(getDomain());
	}
	
	public List<Classifier> getClassifiers() {
		return getContentDao().getClassifiers(getDomain());
	}
	
	protected void onCancel(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);							
	}

	protected void onClose(AjaxRequestTarget target) {
		
	}
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
}