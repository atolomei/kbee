package kbee.web.security.role;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.EntitySet;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.security.KbeeAbstractRole;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;

import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.dataset.DataSetMembersPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;

@SuppressWarnings("serial")
public class RoleEditor extends ObjectEditor<Role> {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RoleEditor.class.getName());

	
	final boolean role_admin	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_root=ServiceLocator.getService(SecurityService.class).isRoot();

	public RoleEditor(IModel<Role> model) {
		this("editor", model, false);
	}
	
	public RoleEditor(String id, IModel<Role> model, boolean isnew) {
		super(id, model);
		setOutputMarkupId(true);
		setIsNew(isnew);
		setEditionEnabled(isnew);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		/**
		 *  
		 * GENERAL
		 * 	
		 ***/
		
		
		Link<Role> members = new Link<Role>("members", getModel()) {
			@Override
			public boolean isVisible() {
				return (getModel().getObject() instanceof EntityRole);
			}
			@Override
			public void onClick() {
				setResponsePage( new DataSetMembersPage(new ObjectModel<DataSet> (((EntityRole) getModel().getObject()).getClassifier().getDataSet())));
			}
		};
		
		form.add(members);
		form.add(new TextField<String>("name",  true));
		form.add(new TextAreaField<String>("description"));
		form.add(new TextField<String>("alias",  true));
		form.add(new BooleanField("apiEnabled"));
		
		
		BooleanField def=new BooleanField("isDefault");
		form.add(def);
		form.add(new StaticField<String>("roleclass", new Model<String>(getModel().getObject().getRoleType()))); 

		
		
		form.add(new ChoiceField<Classifier>("classifier", new PropertyModel<List<Classifier>>(this, "classifiers"), true) {
			public boolean isEnabled() {
				return false;
			}
			public boolean isVisible() {				
				return getModelObject() instanceof EntityRole;
			}
		});
		
		form.add(new BooleanField("administrator") {
			public boolean isVisible() {				
				return getModelObject() instanceof EntityRole;
			}
		});
		
		form.add(new TextAreaField<String>("principalNameTemplate") {
			public boolean isVisible() {				
				return is_root;
			}
		});
		
		form.add((new RoleConditionWizardPanel<Role>() {
			@Override
			protected IModel<String> getHelpText() {
				if (RoleEditor.this.getModel().getObject() instanceof EntityRole && ((EntityRole) RoleEditor.this.getModel().getObject()).getClassifier()!=null) {
					String s = ((EntityRole) RoleEditor.this.getModel().getObject()).getClassifier().getName();
					return new StringResourceModel("role-entity-help", RoleEditor.this).setParameters(s);
				}
				else
					return new StringResourceModel("role-general-help", RoleEditor.this, null);
			}
		}).setVisible(true));
				
		add(form);
		
		form.add(new EditButtonsV5<Role>(this) {
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
				return role_admin || is_root;
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

	public void edit(AjaxRequestTarget target) {
		super.edit(target);
	}
	

	@Override
	public void onDetach() {
		super.onDetach();
	}

	/**
	 * 
	 */
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeAbstractRole role = getRole();
				if (role.getAlias()==null)
					role.setAlias(role.getName().toLowerCase().trim().replace(" ", "-"));
				ServiceLocator.getService(SecurityContentMgmtService.class).update(role, getUpdatedParts());
				super.reset();
				target.add(RoleEditor.this.getPage());
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
	
	public List<Classifier> getClassifiers() {
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (classifier.getDataSet() instanceof EntitySet) {
				classifiers.add(classifier);
			}
		}
		return classifiers;
	}

	protected void onCancel(AjaxRequestTarget target) {
	}

	protected void onAfterSubmit(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}

	protected void onUpdate(AjaxRequestTarget target) {
	}
	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
}
