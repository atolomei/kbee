package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.kbee.wicket.editor.Editor;

import com.novamens.logging.ModelUpdateEvent;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.security.AclEditorPanel;

@Deprecated
@SuppressWarnings("serial")
public class TemplateWorkflowEditor extends DomainObjectEditor<ContentTemplate> {
	private static final long serialVersionUID = 1L;

	static private Logger logger = LogManager.getLogger("TxLogger");
	
	private IModel<Acl> aclmodel;
	private AclEditor acleditor;
	private Long aclid;
	
	private static long AclId = 0;
	private static HashMap<Long, Acl> AclMap = new HashMap<Long, Acl>();

	public class AclModel implements IModel<Acl> {
		public Acl getObject() {
			Acl acl;
			if (aclid==null) {
				acl = TemplateWorkflowEditor.this.getModel().getObject().getAcl();
				if (acl == null) {
					aclid = AclId++;
					acl = new KbeeAcl();
				}
				else {
					aclid = (Long)acl.getId();
				}
				AclMap.put(aclid, acl);
			}
			else {
				acl = AclMap.get(aclid);
			}
			return acl;
		}
		public void setObject(Acl acl) {
		}
		public void detach() {
		}
	}
	
	public class AclEditor extends ObjectEditor<Acl> {
		public AclEditor() {
			super("editor",getAclModel());
		}
		@Override
		public boolean isEditionEnabled() {
			return TemplateWorkflowEditor.this.isEditionEnabled();
		}
		@Override
		public void setUpdatedPart(String updatedPart) {
			TemplateWorkflowEditor.this.setUpdatedPart(updatedPart);
		}
	}
	
	public TemplateWorkflowEditor(String id, IModel<ContentTemplate> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		setAclModel(new AclModel());
		setAclEditor(new AclEditor());
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		form.add(new LaunchersEditor(getModel()));
		form.add(new AclEditorPanel(getAclEditor(), getCreatePermission()));
		
		add(form);
		
		final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		final boolean role_info = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
		final boolean free_version = model.getObject().getDomain().getDomainType()==DomainType.EXPRESS;
		
		
		/**
		 * Workflow can only be edited in the Premium Version.
		 */
		add(new EditButtonsV5<ContentTemplate>(this) {
			@Override
			public boolean isEnabled() {
		
				if (free_version)
					return false;
				
				return role_admin || role_info;
			}
		});
	}

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				((KbeeContentTemplate) getModelObject()).setAcl(getAclModel().getObject());
				for (ProcessLauncher launcher: getModelObject().getProcessLaunchers()) {
					if (((KbeeProcedure) launcher.getProcedure())!=null) {
						if (((KbeeProcedure) launcher.getProcedure()).getLastModifiedUser()==null)
							((KbeeProcedure) launcher.getProcedure()).setLastModifiedUser(getSessionUser());
					}
					((KbeeProcessLauncher)launcher).setAcl(getAclModel().getObject());
				}
				
				getModelObject().getService(DOMObjectService.class).update();
				
				logger.info(new ModelUpdateEvent(getModelObject(), getUpdatedParts()));
				AclMap.remove(aclid);
				aclid = null;
				super.reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Void>(target, e));
		}
	}
	
	public Editor<Acl> getAclEditor() {
		return acleditor;
	}
	
	public void setAclEditor(AclEditor editor) {
		this.acleditor = editor;
	}
	
	public IModel<Acl> getAclModel() {
		return aclmodel;
	}
	
	public void setAclModel(IModel<Acl> model) {
		this.aclmodel = model;
	}
	
	private List<Permission> getCreatePermission() {
		List<Permission> permissions = new ArrayList<Permission>();
		permissions.add(KbeePermission.CREATE);
		return permissions;
	}
	

}
