package com.novamens.content.web.security.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.dao.DataIntegrityViolationException;


import com.novamens.content.base.SecurityRule;
import com.novamens.content.security.IQLRule;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.security.AclEditorPanel;

import com.novamens.wicket.markup.html.form.StaticField;

@SuppressWarnings("serial")
public class RuleEditor extends DomainObjectEditor<IQLRule> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RuleEditor.class.getName());
	
	private IModel<Acl> aclmodel;
	private AclEditor acleditor;
				
	final boolean isroot 			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean role_admin 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security 	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	
	private boolean isNewUserBasic;
	
	private IModel<String> type = null;

	public class AclEditor extends ObjectEditor<Acl> {
		public AclEditor() {
			super("editor", getAclModel());
		}
		@Override
		public boolean isEditionEnabled() {
			return RuleEditor.this.isEditionEnabled();
		}
		@Override
		public void setUpdatedPart(String updatedPart) {
			RuleEditor.this.setUpdatedPart(updatedPart);
		}
	}
	
	class IqlValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			
			String statement=validatable.getValue();
			
			/**for IQL Sentences the Domain must be the session domain	*/
			if  (RuleEditor.this.getModelObject().getType()==SecurityRule.RULE_COLLOQUIAL_IQL) {
					String tmp=statement.replaceAll( "\\s+","").toLowerCase().trim();
					String arr[] = tmp.split("domain\\(");
					if (arr.length>1) {
							for (String a: arr) {
								if (a.length()>0) {
									String b[]=a.split("\\)");
									if (b.length>0) {
										try {
											Integer t = Integer.valueOf(b[0]);
											if (t.intValue()!=((Long)getDomain().getId()).intValue()) { 
												validatable.error(new ValidationError(this, "invalid-domain"));
												return;
											}
										} catch (Exception e) {
											validatable.error(new ValidationError(this, "invalid-domain"));
											return;
										}
									} else {
										validatable.error(new ValidationError(this, "invalid-domain"));
										return;
									}
								}
							}
					}
			}
			try {
				IqlService iqlservice = getDomain().getService(IqlService.class);
				ResultSet set = iqlservice.execute(statement);
				set.hasNext();
			} 
			catch (RuntimeException e) {
				logger.error(e);
				validatable.error(new ValidationError(this));
			}
		}
	}

	/**
	 * 
	 */
	public RuleEditor(IModel<IQLRule> model) {
		this("editor", model, false);
	}
	
	
	/**
	 * @param id
	 * @param model
	 * @param isnew
	 */
	public RuleEditor(String id, IModel<IQLRule> model, boolean isnew) {
		super(id, model);
		setOutputMarkupId(true);
		setIsNew(isnew);
		setEditionEnabled(isnew);
	}
	

	/**
	 * @param id
	 * @param model
	 * @param isnew
	 */
	public RuleEditor(String id, IModel<IQLRule> model, IModel<User> userBasic) {
		super(id, model);
		setOutputMarkupId(true);
		setIsNew(true);
		setEditionEnabled(true);
		this.isNewUserBasic=true;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
										
		this.type=new Model<String> (((getModel().getObject().getType()==SecurityRule.RULE_COLLOQUIAL_IQL)?"Colloquial":"Wizard"));
		
		setAclModel(new IModel<Acl>() {
			public Acl getObject() {
				return (Acl)RuleEditor.this.getModelObject().getAcl();
			}
			public void setObject(Acl acl) {
			}
			public void detach() {
			}
		});
		
		setAclEditor(new AclEditor());
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		WebMarkupContainer description = new WebMarkupContainer("rule-description") {
			public boolean isVisible() {
				return getModelObject().isDerived();
			}
		};
		
		description.add((new Label("text", getModelObject().getDescription())).setEscapeModelStrings(false));
		form.add(description);
		
		ChoiceField<String> te = new ChoiceField<String>("type", new PropertyModel<String>(this, "type"), new PropertyModel<List<String>>(this, "types")) {
			public void onUpdate(AjaxRequestTarget target) {
				RuleEditor.this.onUpdate(target);
			}
		};
				
		form.add(te);
		te.setEnabled(false);
		
		form.add(new TextField<String>("name") {
			@Override
			public boolean isEnabled() {
				return super.isEnabled() && !RuleEditor.this.getModelObject().isDerived();
			}
		});
		
		form.add(new StaticField<String>("id", new Model<String>( String.valueOf(getModel().getObject().getId()))));

		form.add(new RuleConditionWizardPanel<IQLRule>() {
			public boolean isVisible() {
				return RuleEditor.this.getModelObject().getType()==IQLRule.RULE_WIZARD_IQL;
			}
			@Override
			protected IModel<String> getHelpText() {
				return new StringResourceModel("conditionwizard.helptext", RuleEditor.this);
			}
		});
		
		form.add(new TextAreaField<String>("condition", new IqlValidator(), 4, 0) {
			@Override
			public boolean  isVisible() {
				return RuleEditor.this.getModelObject().getType()==IQLRule.RULE_COLLOQUIAL_IQL;
			}
		});
		
		/***
		 * 
		 */
		form.add(new AclEditorPanel(getAclEditor()) {
			@Override 
			public boolean usersEnabled() {
				return RuleEditor.this.usersEnabled();
			}
		});
		
		form.add(new TextAreaField<String>("notes") {
			@Override
			public boolean isEnabled() {
				return super.isEnabled();
			}
		});
		
		add(form);
		
		add(new EditButtonsV5<IQLRule>(this) {
			@Override
			public boolean isVisible() {
				try {
					if (!getDomain().equals(getModelObject().getDomain()))
						return false;
					
					/** Rules generated by a Role can only be edited by root */
					if (getModelObject().isDerived())
						return isroot;
					
					return role_security;
				} 
				catch (Exception e) {
					logger.error(e);
					return false;
				}
			}
			@Override
			public boolean isEnabled()  {
				if (isSupportSessionUser() && !isRoot())
					return false;
				if (getModelObject().isDerived() && !isRoot())
					return false;
				return true;
			}
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
		});		
	}
	
	public boolean isNewUserBasic() {
		return isNewUserBasic;
	}

	public void setNewUserBasic(boolean isNewUserBasic) {
		this.isNewUserBasic = isNewUserBasic;
	}
	

	public void onClose(AjaxRequestTarget target) {
		
	}
	
	@Override
	public void cancel(AjaxRequestTarget target) {
		
		if (isNew()) {
			try {
				ServiceLocator.getService(SecurityContentMgmtService.class).delete(getModel().getObject());
			}
			catch (DataIntegrityViolationException e) {
				logger.error(e);
			}
			catch (Exception e) {
				logger.error(e);
			}
			onClose(target);
		}
		
		onCancel(target);
	}


	public void edit(AjaxRequestTarget target) {
		super.edit(target);
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				IQLRule rule = getModelObject();
				if (this.type.getObject().equals("Colloquial")) 
					rule.setDisplayCondition(rule.getCondition());
				ServiceLocator.getService(SecurityContentMgmtService.class).update(rule, getUpdatedParts());
				super.reset();
				target.add(RuleEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}

	public List<String> getTypes() {
		List<String> list = new ArrayList<String>();
		list.add("Colloquial");
		list.add("Wizard");
		return list;
	}

	public IModel<Acl> getAclModel() {
		return aclmodel;
	}

	public ObjectEditor<Acl> getAclEditor() {
		return acleditor;
	}

	public boolean usersEnabled() {
		return false;
	}

	@Override
	public void onDetach() {
		if (aclmodel!=null)
			aclmodel.detach();
		super.onDetach();
	}

	protected void onCancel(AjaxRequestTarget target) {
	}

	protected void onAfterSubmit(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}

	protected void onUpdate(AjaxRequestTarget target) {
		
	}

	private void setAclModel(IModel<Acl> model) {
		this.aclmodel = model;
	}

	private void setAclEditor(AclEditor editor) {
		this.acleditor = editor;
	}
}
