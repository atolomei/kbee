package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.DataSet;

import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.EditorEvent;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.panel.AlertPanel;

@SuppressWarnings("serial")
public class DataSetEditor<T extends DataSet> extends DomainObjectEditor<T> {
		
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DataSetEditor.class.getName());
	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	
	private boolean createClassifier = true;
	
	class UniquenessNameValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String datasetname = validatable.getValue();
			for (DataSet dataset : getDataSets()) {
				if (!dataset.equals(DataSetEditor.this.getModelObject())) {
					if (dataset.getName()!=null && dataset.getName().equals(datasetname)) {
						validatable.error(new ValidationError(this));
					}
				}
			}
		}
	}
	
	class UniquenessAliasValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String datasetalias = validatable.getValue();
			if (datasetalias!=null) {
				for (DataSet dataset : getDataSets()) {
					if (!dataset.equals(DataSetEditor.this.getModelObject())) {
						if (dataset.getAlias()!=null && dataset.getAlias().trim().equals(datasetalias.trim())) {
							validatable.error(new ValidationError(this));
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	public DataSetEditor(IModel<T> model) {
		this("editor", model, false);
	}

	/**
	 * 
	 */
	public DataSetEditor(String id, IModel<T> model, boolean is_new) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		setEditionEnabled(is_new);
		setIsNew(is_new);
		
		
		if (model.getObject().isCanonical()) {
			add(new AlertPanel<T>("iscanonical", 
				AlertPanel.INFO, 
				model, null, 
				getLabel("canonical")) );
		}
		else {
			add(new InvisiblePanel("iscanonical"));
			
		}
		
		
		createClassifier = is_new;
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("name", true, new UniquenessNameValidator()) {
			@SuppressWarnings("unchecked")
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				if (isNew() && getValue()!=null && getValue().trim().length()>0) {
					((KbeeDataSet)DataSetEditor.this.getModelObject())
						.setAlias(DataSetEditor.this.parseAlias(getValue()));
					((TextField<String>) DataSetEditor.this.get("form:alias"))
						.setValue(DataSetEditor.this.getModelObject().getAlias());
					target.add(DataSetEditor.this);
				}
			}
		});
		
		form.add(new TextField<String>("alias", true, new UniquenessAliasValidator()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					((KbeeDataSet)DataSetEditor.this.getModelObject())
						.setAlias(DataSetEditor.this.parseAlias(getValue()));
					setValue(DataSetEditor.this.parseAlias(getValue()));
					target.add(DataSetEditor.this);
				}
			}
		);
		
		form.add(new BooleanField("readonly") {
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled() {
				return true;
			}
		});
		
		form.add(new BooleanField("uniqueValues") {
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled() {
				return true;
			}
		});
		
		form.add(new TextAreaField<String>("description"));

		
		WebMarkupContainer cc=new WebMarkupContainer("create-classifier-container") {
			public boolean isVisible() {
				return isNew();
			} 
		};
		form.add(cc);
		
				
		cc.add(new BooleanField("createClassifier", new PropertyModel<Boolean>(this, "createClassifier")) {
			public boolean isEnabled() {
				return isNew();
			}
		});
		
		form.add(new StaticField<String>("id", new Model<String>(model.getObject().getId().toString())));
		form.add(new StaticField<String>("type", new Model<String>(model.getObject().getDataSetType().getLabel())));
		
		form.add(new BooleanField("aggregation") {
			public boolean isEnabled() {
				return false;
			}
			public boolean isVisible() {
				return DataSetEditor.this.getModelObject().isAggregation();
			}
		});
		
		form.add(new ChoiceField<AccessStrategy>("accessStrategy", () -> getAccessStrategies()) {
			@Override
			public String getDisplayValue(AccessStrategy value) {
				return value.getLabel(getSessionUser().getLocale());
			}
		});
		
		form.add(new BooleanField("hierachical") {
			@Override
			public boolean isVisible() {
				return isRoot();
			}
		});
		
		form.add(new BooleanField("suggester"));
		
		add(form);
		add(new EditButtonsV5<T>(this) {
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled() {
				
				if (isRoot())
					return true;
				
				if (DataSetEditor.this.getModel().getObject().isOnlyRootEdit())
					return false;
				
				return role_admin || role_model;
			}
			
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
			
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
		});
	}
	
	public List<AccessStrategy> getAccessStrategies () {
		List<AccessStrategy> strategies = new ArrayList<AccessStrategy>();
		strategies.add(AccessStrategy.All);
		strategies.add(AccessStrategy.Roles);
		return strategies;
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

	protected void onClose(AjaxRequestTarget target) {
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				logger.debug(getModelObject().getACL());
				getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				if (isCreateClassifier())
					ServiceLocator.getService(ObjectFactoryService.class).createClassifier(getModelObject());
				fire(new EditorEvent(target));
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public boolean isCreateClassifier() {
		return this.createClassifier;
	}
	
	public boolean getCreateClassifier() {
		return this.createClassifier;
	}
				
	public void setCreateClassifier(boolean b) {
		this.createClassifier = b;
	}

	public void onUpdate(AjaxRequestTarget target) {
	}
	
	protected void onCancel(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);							
	}
	
	public List<DataSet> getDataSets() {
		return getContentDao().getDataSets(getDomain());
	}
}
