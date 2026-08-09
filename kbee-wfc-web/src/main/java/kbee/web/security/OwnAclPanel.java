package kbee.web.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.security.acl.Acl;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.workflow.Procedure;

import kbee.util.logging.Logger;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class OwnAclPanel<T extends Content> extends ObjectEditor<T> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(OwnAclPanel.class.getName());
	
	IModel<Acl> aclmodel;
	
	public class AclModel implements IModel<Acl> {
		private Acl acl;
		
		public AclModel(IModel<T> model) {
			acl = (Acl)model.getObject().getAcl();
		}
		public void setObject(Acl acl) {
		}
		public Acl getObject() {
			if (acl==null) {
				acl = (Acl)getModelObject().getAcl();
				if (acl==null) {
					acl = new KbeeAcl();
				}
			}
			return acl;
		}
		public void detach() {
			acl = null;
		}
	}
	
	public class AclEditor extends ObjectEditor<Acl> {
		public AclEditor(IModel<Acl> model) {
			super("editor", model);
		}
		@Override
		public boolean isEditionEnabled() {
			return OwnAclPanel.this.isEditionEnabled();
		}
	}

	public OwnAclPanel(String id, IModel<T> model) {
		super(id, model);
		
		setEditionEnabled(false);
		
		setAclModel(new AclModel(model));
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		form.add(new AclEditorPanel("acl", new AclEditor(getAclModel())) {
			@Override
			protected List<Procedure> getProcedures() {
				return new ArrayList<>();
			}
		});
		
		add(form);
		
		add(new EditButtonsV5<T>(this)  {
			@Override
			public boolean isEnabled() {
				return isWriteable(OwnAclPanel.this.getModel());
			}
		});
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				if (getModelObject().getAcl()==null) {
					((KbeeContent)getModelObject()).setAcl(getAclModel().getObject());
				}
				getModelObject().getService(ContentService.class).updateAcl(getUpdatedParts());
				super.reset();
				target.add(OwnAclPanel.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public IModel<Acl> getAclModel() {
		return aclmodel;
	}
	
	protected void setAclModel(IModel<Acl> model) {
		this.aclmodel = model;
	}
	
	protected boolean isWriteable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(model.getObject());
	}
}
