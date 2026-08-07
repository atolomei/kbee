package com.novamens.content.web.security.markup;

import java.util.List;
 

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.logging.SecurityUpdateEvent;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class GroupEditor extends ObjectEditor<Group> {
	private static final long serialVersionUID = 1L;
	
	static Logger logger = LogManager.getLogger(GroupEditor.class.getName());

	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());

	
	class UniquenessValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			
			String groupname = validatable.getValue();
			
			List<Group> groups = ServiceLocator.getService(com.novamens.service.SecurityService.class).findGroupByName(groupname, String.valueOf(getDomain().getId()));
			
			for (Group group : groups) {
				if (!group.equals(GroupEditor.this.getModelObject())) {
					if (group.getName().equals(groupname)) {
						validatable.error(new ValidationError(this));
					}
				}
			}
		}
	}


	
	/** -----------------------------------------------------------------------------------------
	 */
	public GroupEditor(IModel<Group> model) {
		this("editor", model);
	}

	
	public GroupEditor(String id, IModel<Group> model) {
		this(id, model, false);
	}
	
	
	
	public GroupEditor(String id, IModel<Group> model, boolean isNew) {
		super(id, model);
			
		setIsNew(isNew);
		setOutputMarkupId(true);
			
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		WebMarkupContainer description = new WebMarkupContainer("group-description-alert") {
			public boolean isVisible() {
				return getModelObject().isDerived();
			}
		};
		description.add((new Label("text", getModelObject().getDescription())).setEscapeModelStrings(false));
		form.add(description);

		form.add(new TextField<String>("name", true, new UniquenessValidator()) {
			public void onUpdate(AjaxRequestTarget target) {
				GroupEditor.this.onUpdate(target);
			}
			protected boolean autofocus() {
				return true;
			}
			@Override
			public boolean isEnabled() {
				//if (getModelObject().isCanonical()) {					
				return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
				//}
				//else
				//	return true;
			}
			@Override
			protected IModel<String> getHelpText() {
				if (getModelObject().isCanonical())
					return new StringResourceModel("onlyroot", this, null);
				else
					return null;
			}
		});
			
		form.add(new TextField<String>("description") {
			@Override
			public boolean isEnabled() {
				return !getModelObject().isDerived();
			}
		});
			
		IModel<String> canonicalmodel = new Model<String>() { 
			public String getObject() {
				if (getModelObject().isCanonical())
					return new StringResourceModel("group.type.system", GroupEditor.this, null).getString();
				else				
					return new StringResourceModel("group.type.editable", GroupEditor.this, null).getString();
			}
		};
			
		form.add(new StaticField<String> ("id",  new Model<String>(String.valueOf(model.getObject().getId()))));
		form.add(new StaticField<String> ("type", canonicalmodel));
			
		form.add(new GroupsEditor() {
			@Override
			public boolean isVisible() {
				if (getDomain().getDomainType()==DomainType.EXPRESS)
					return false;
				if (getModelObject().isCanonical()) 					
					return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
				else
					return true;
			}
		});
			
		add(form);
			
		add(new EditButtonsV5<Group>(this)  {
			@Override
			public boolean isVisible()  {
				return isRoot();
			}
			@Override
			public boolean isEnabled()  {
				return isRoot();
			}
		});
 
	}
	
	/** -----------------------------------------------------------------------------------------
	 */
	@Override
	public void cancel(AjaxRequestTarget target) {

		if (isNew()) {
			try {
				ServiceLocator.getService(SecurityContentMgmtService.class).delete(getModel().getObject());
			}
			catch (DataIntegrityViolationException e) {
				logger.error(e.getStackTrace());
			}
			catch (Exception e) {
				logger.error(e);
				fire(new ErrorEvent(target, e));
			}
		}
		onCancel(target);
	}

	/** -----------------------------------------------------------------------------------------
	 */
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				Group group = getModelObject();
				ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
				logger.info(new SecurityUpdateEvent(getModelObject(), getUpdatedParts()));
				reset();
				target.add(GroupEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));

		}
	}

	/** -----------------------------------------------------------------------------------------
	 */
	protected void onCancel(AjaxRequestTarget target) {
	}

	/** -----------------------------------------------------------------------------------------
	 */
	protected void onUpdate(AjaxRequestTarget target) {
	}
	
	/** -----------------------------------------------------------------------------------------
	 */
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
	
	/** --------------------------------------------------------------------------
	 */
	protected boolean isSupportUser() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}

	/** --------------------------------------------------------------------------
	 * Session User
	 */
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	/**
	 * Session USer
	 * @return
	 */
	protected User getSessionUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();	
		
	}

}
