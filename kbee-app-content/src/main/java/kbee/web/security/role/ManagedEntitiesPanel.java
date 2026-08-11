package kbee.web.security.role;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.kbee.content.security.KbeeAbstractRole;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.logging.Logger;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class ManagedEntitiesPanel extends ObjectEditor<Role> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = Logger.getLogger(ManagedEntitiesPanel.class.getName());

	final boolean role_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());

	
	public ManagedEntitiesPanel(IModel<Role> model) {
		this("editor", model, false);
	}
	
	public ManagedEntitiesPanel(String id, IModel<Role> model, boolean isnew) {
		super(id, model);
		
		setOutputMarkupId(true);
		setIsNew(isnew);
		setEditionEnabled(isnew);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		final String rolename=getModel().getObject().getName(); // Role
		final String ca; // Classifier
		final String da; // DataSet
		
		if (getModel().getObject() instanceof com.novamens.content.security.EntityRole) {
				ca=((EntityRole) getModel().getObject()).getClassifier().getDisplayName();
				da=((EntityRole) getModel().getObject()).getClassifier().getDataSet().getDisplayName();
		}
		else {
			ca="null";
			da="null";
		}
		
		
		Label ma=new Label("alert", new StringResourceModel("alert-text", ManagedEntitiesPanel.this, null).setParameters(new Object [] {rolename, ca, da}  )) ;
		ma.setEscapeModelStrings(false);
		ma.setVisible(true);
		
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		form.add(ma);
		form.add(new ManagedEntitiesEditor());
				
		add(form);
		
		add(new EditButtonsV5<Role>(this) {
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled()  {
				return true;
			}
		});		
	}

	public void onClose(AjaxRequestTarget target) {
	}

	
	@Override
	public void cancel(AjaxRequestTarget target) {
		if (isNew()) {
			try {
				ServiceLocator.getService(SecurityContentMgmtService.class).delete(getModelObject());
			}
			catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.error(e);
				}
				else {
					logger.error(e);
				}	
			}
			onClose(target);
		}
		
		onCancel(target);
	}

	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeAbstractRole role = getRole();
				if (role.getAlias()==null)
					role.setAlias(role.getName().toLowerCase().trim().replace(" ", "-"));
				ServiceLocator.getService(SecurityContentMgmtService.class).update(role, getUpdatedParts());
				super.reset();
				target.add(ManagedEntitiesPanel.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	
	public KbeeAbstractRole getRole() {
		return (KbeeAbstractRole) getModelObject();
	}

	protected void onCancel(AjaxRequestTarget target) {
	}

	protected void onAfterSubmit(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}

	protected void onUpdate(AjaxRequestTarget target) {
		
	}
}
