package kbee.web.dataset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.PropertiesFactory;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.wicket.froala.FroalaField;
import kbee.wicket.tinymce.TinyField;



public class MemberNotesEditorPanel extends DomainObjectEditor<DataSetMember>  {
				
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(MemberNotesEditorPanel.class.getName());
	
	private String text;

	final boolean role_support = 
		ServiceLocator.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean role_admin = 
		ServiceLocator.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = role_admin || 
		ServiceLocator.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_dataset_members_read = role_support || role_model || role_admin || 
		ServiceLocator.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
	final boolean role_dataset_members_write = 
		ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());

	private static String defaultEditor =
			PropertiesFactory
				.getInstance("kbee")
				.getProperties()
				.getProperty("kbee.text.defaulteditor", null);	
	
	public MemberNotesEditorPanel(String id, IModel<DataSetMember> model, boolean is_new) {
			this(id, model, is_new, false);
	}
	
	public MemberNotesEditorPanel(String id, IModel<DataSetMember> model, boolean is_new, boolean is_readonly) {
		super(id, model);
		
		setIsNew(is_new);
		setOutputMarkupId(true);
		setEditionEnabled(false);
		setReadOnly(is_readonly);
		
	}

	public void setText(String text) {
		this.text = text;
	}


	public String getText() {
		return text;
	}

	
	public void onUpdate(AjaxRequestTarget target) {}
	public void onCancel(AjaxRequestTarget target) {}
	
	
	@Override
	public void update(AjaxRequestTarget target) {

		if ((this.text==null && getModelObject().getNotes()!=null) ||
			(this.text!=null && getModelObject().getNotes()==null) || 
			(this.text!=null && getModelObject().getNotes()!=null && !this.text.equals(getModelObject().getNotes().asString()))) {

			try {
				getModelObject().setNotes(this.text);
				setUpdatedPart("Notes");
				getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
		
			}
			catch (Exception e) {
				logger.error(e);
				fire(new ErrorEvent<>(target, e));
			}
		}
	}


	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		onCancel(target);
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (get("form:container-text:xcontent-text")==null) {
			if (getModelObject().getNotes()!=null) 
				setText(getModelObject().getNotes().asString());
			addComponents();
		}
	}
	

	@SuppressWarnings("serial")
	protected void addComponents() {
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);
		
		WebMarkupContainer addcontainer = new WebMarkupContainer("add-container") {
			@Override
			public boolean isVisible() {
				if (isReadOnly())
					return false;
				if (MemberNotesEditorPanel.this.getModel().getObject().getState()==ObjectState.DELETED) {
					return false;
				}
				if (!role_dataset_members_write &&
					!isWriteable()) {
					return false;
				}
				return true; 
			}
		};
		
		AjaxLink<Void> addb = new AjaxLink<Void>("add") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				setEditionEnabled(true);
				target.focusComponent(((Field<?>)MemberNotesEditorPanel.this.get("form:container-editor:ztext-editor")).getInput());
				target.add(MemberNotesEditorPanel.this);
			}
			@Override
			public boolean isEnabled() {
				return !isEditionEnabled(); 
			}
		};
		
		form.add(addcontainer);
		
		addcontainer.add(addb);

		Label addoredit = new Label("edit", new Model<String>() {
			
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				String key = getText()==null||getText().length()<1?"add":"edit";
				return new StringResourceModel(key,MemberNotesEditorPanel.this, null).getString(); 
			}
		});
		
		addb.add(addoredit);
		
		WebMarkupContainer container_text = new WebMarkupContainer("container-text") {
			@Override
			public boolean isVisible() {
				return !isEditionEnabled();
			}
		};
		
		WebMarkupContainer container_editor = new WebMarkupContainer("container-editor")  {
			@Override
			public boolean isVisible() {
				if (isReadOnly())
					return false;
				return isEditionEnabled(); 
			}
		};
		
		container_editor.setOutputMarkupId(true);
		
		form.add(container_text);
		form.add(container_editor);
		
		container_text.add(new Label("xcontent-text", new Model<String>() { 
			public String getObject() { 
				if (getText()==null || getText().length()==0) { 
					if (MemberNotesEditorPanel.this.isEditionEnabled())
						return new StringResourceModel("edit", MemberNotesEditorPanel.this, null).getString();
					return "";
				}
				else			
					return getText(); 
			}}) {

				private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !isEditionEnabled();
			}
		});
		
		((Label)get("form:container-text:xcontent-text")).setEscapeModelStrings(false);
		
		if ("Tiny".equals(defaultEditor)) {
			container_editor.add(new TinyField("ztext-editor", new PropertyModel<String>(this, "text")) {
				@Override
				public boolean isVisible() {
					if (isReadOnly())
						return false;
					if (!role_dataset_members_write && 
						!isWriteable()) {
						return false;
					}	
					return isEditionEnabled();
				}
				public void onClose(AjaxRequestTarget target) {
					super.onClose(target);
					updateModel();
					target.add(container_editor);
				}
				@Override
				public boolean isEditionEnabled() {
					return MemberNotesEditorPanel.this.isEditionEnabled();
				}
				@Override
				public IModel<String> getLabel() {
					return new Model<String>("");
				}
				@Override
				public boolean includeClose() {
					return false;
				}
			});
		}
		else {
			container_editor.add(new FroalaField("ztext-editor", new PropertyModel<String>(this, "text")) {
				@Override
				public boolean isVisible() {
					if (isReadOnly())
						return false;
					if (!role_dataset_members_write)
						return false;
					return MemberNotesEditorPanel.this.isEditionEnabled();
				}
				@Override
				public boolean isEditionEnabled() {
					return MemberNotesEditorPanel.this.isEditionEnabled();
				}
				@Override
				public IModel<String> getLabel() {
					return new Model<String>("");
				}
			});
		}
		
		container_editor.add(new AjaxSubmitLink("save-link", getForm()) {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				setEditionEnabled(!isEditionEnabled());
				update(target);
				target.focusComponent(((Field<?>)MemberNotesEditorPanel.this.get("form:container-editor:ztext-editor")).getInput());
				target.add(MemberNotesEditorPanel.this);
			}
			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
					@Override 
					public CharSequence getBeforeHandler(Component component) { 
						return "if (typeof(tinyMCE) != \"undefined\") tinyMCE.triggerSave(true,true)";
					}
				};
				attributes.getAjaxCallListeners().add(myAjaxCallListener);
			}
		});
		
		container_editor.add(new AjaxLink<Void>("cancel-link") {
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				setEditionEnabled(false);
				((TextAreaField<String>)(MemberNotesEditorPanel.this.get("form:container-editor:ztext-editor"))).cancel();
				target.add(MemberNotesEditorPanel.this);
			}
			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
		});
	}
	
	protected boolean isWriteable() {
		return ServiceLocator.getService(UserService.class).isWriteable(getModelObject());
	}
	
	protected boolean hasWritePermissions() {
		return role_dataset_members_read;
	}
}
