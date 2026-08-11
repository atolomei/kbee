package kbee.web.model;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.base.ResourceTag;
import com.novamens.content.model.DataSet;
import com.novamens.content.service.DomService;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class ResourceTagEditor extends DomainObjectEditor<ResourceTag> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ResourceTagEditor.class.getName());

	class UniquenessValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String tagname = validatable.getValue();
			for (ResourceTag tag : getTags()) {
				if (!tag.equals(ResourceTagEditor.this.getModelObject())) {
					if (tag.getName()!=null && tag.getName().equals(tagname)) {
						validatable.error(new ValidationError(this));
					}
				}
			}
		}
	}
	
	class UniquenessKeyValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String alias = validatable.getValue();
			for (ResourceTag tag : getTags()) {
				if (!tag.equals(ResourceTagEditor.this.getModelObject())) {
					if (tag.getAlias()!=null && tag.getAlias().equals(alias)) {
						validatable.error(new ValidationError(this));
					}
				}
			}
		}
	}
	
	public ResourceTagEditor(IModel<ResourceTag> model, final boolean isnew) {
		this("editor", model, isnew);
	}
	
	public ResourceTagEditor(String id, IModel<ResourceTag> model, final boolean isnew) {
		super(id, model);
		
		final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		
		setOutputMarkupId(true);
		
		setIsNew(isnew);
		setEditionEnabled(isnew);

		add(new InfoDialog("help-modal"));
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("name", true, new UniquenessValidator()) {
			@Override
			@SuppressWarnings("unchecked")
			public void onUpdate(AjaxRequestTarget target) {
				if (isNew() && super.getValue()!=null) {
					((KbeeResourceTag)ResourceTagEditor.this.getModelObject()).setAlias(parseAlias(super.getValue()));
					//ResourceTagEditor.this.getModelObject().setPredicate(parsePredicate(super.getValue()));
					((TextField<String>) ResourceTagEditor.this.get("form:alias")).setValue(ResourceTagEditor.this.getModelObject().getAlias());
					target.add(ResourceTagEditor.this);
				}
			}
		});
		
		form.add(new TextField<String>("alias", true, new UniquenessKeyValidator()) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				((KbeeResourceTag)ResourceTagEditor.this.getModelObject()).setAlias(ResourceTagEditor.this.parseAlias(getValue()));
				setValue(ResourceTagEditor.this.parseAlias(getValue()));
				target.add(ResourceTagEditor.this);
			}
		});
		
		form.add(new BooleanField("multiple"));
		
		form.add(new BooleanField("default"));
		
		add(form);
		
		add(new EditButtonsV5<ResourceTag>(this)  {
			@Override
			public boolean isEnabled() {
				if (isRoot())
					return true;
				return role_admin;
			}
		});
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				((KbeeResourceTag)getModelObject()).getService(DomService.class).update(getUpdatedParts());
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

	@Override
	public void cancel(AjaxRequestTarget target) {
		if (isNew()) {
			try {
				((KbeeResourceTag)getModelObject()).getService(DomService.class).update(getUpdatedParts());
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
	
	public List<ResourceTag> getTags() {
		return getRepository(ResourceTag.class).findAll();
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