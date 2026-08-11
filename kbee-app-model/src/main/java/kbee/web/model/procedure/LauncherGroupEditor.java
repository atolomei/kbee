package kbee.web.model.procedure;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.service.DomService;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class LauncherGroupEditor extends DomainObjectEditor<LauncherGroup> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LauncherGroupEditor.class.getName());

	class UniquenessValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String tagname = validatable.getValue();
			for (LauncherGroup group : getGroups()) {
				if (!group.equals(LauncherGroupEditor.this.getModelObject())) {
					if (group.getName()!=null && group.getName().equals(tagname)) {
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
			for (LauncherGroup group : getGroups()) {
				if (!group.equals(LauncherGroupEditor.this.getModelObject())) {
					if (group.getAlias()!=null && group.getAlias().equals(alias)) {
						validatable.error(new ValidationError(this));
					}
				}
			}
		}
	}
	
	public LauncherGroupEditor(IModel<LauncherGroup> model, final boolean isnew) {
		this("editor", model, isnew);
	}
	
	public LauncherGroupEditor(String id, IModel<LauncherGroup> model, final boolean isnew) {
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
					((KbeeLauncherGroup)LauncherGroupEditor.this.getModelObject()).setAlias(parseAlias(super.getValue()));
					//LauncherGroupEditor.this.getModelObject().setPredicate(parsePredicate(super.getValue()));
					((TextField<String>) LauncherGroupEditor.this.get("form:alias")).setValue(LauncherGroupEditor.this.getModelObject().getAlias());
					target.add(LauncherGroupEditor.this);
				}
			}
		});
		
		form.add(new TextField<String>("alias", true, new UniquenessKeyValidator()) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				((KbeeLauncherGroup)LauncherGroupEditor.this.getModelObject()).setAlias(LauncherGroupEditor.this.parseAlias(getValue()));
				setValue(LauncherGroupEditor.this.parseAlias(getValue()));
				target.add(LauncherGroupEditor.this);
			}
		});
		
		//form.add(new BooleanField("multiple"));
		
		add(form);
		
		add(new EditButtonsV5<LauncherGroup>(this)  {
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
				((KbeeLauncherGroup)getModelObject()).getService(DomService.class).update(getUpdatedParts());
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
				((KbeeLauncherGroup)getModelObject()).getService(DomService.class).update(getUpdatedParts());
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
	
	public List<LauncherGroup> getGroups() {
		return getRepository(LauncherGroup.class).findAll();
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