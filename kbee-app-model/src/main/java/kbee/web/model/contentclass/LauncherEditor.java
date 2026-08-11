package kbee.web.model.contentclass;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.workflow.AttributeRule;
import com.novamens.content.workflow.ClassificationRule;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.ScriptRule;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.workflow.Procedure;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.EditorEvent;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.model.procedure.AttributesRulesEditor;
import kbee.web.model.procedure.ScriptEditor;
import kbee.web.model.procedure.ScriptRuleEditor;
import kbee.web.model.procedure.ClassifiersRulesEditor;
import kbee.web.panel.AlertPanel;
import kbee.web.security.AclEditorPanel;

import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.model.ObjectModel;

@SuppressWarnings("serial")
public class LauncherEditor extends DomainObjectEditor<ProcessLauncher> {
	private static final long serialVersionUID = 1L;
						
	private IModel<Acl> aclmodel;
	private AclEditor acleditor;
	private IModel<LauncherGroup> launcher_model;

	//private boolean isprocedurelink;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LauncherEditor.class.getName());
	
	public class AclModel implements IModel<Acl> {
		public Acl getObject() {
			Acl acl;
 			acl = LauncherEditor.this.getModel().getObject().getAcl();
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
			return LauncherEditor.this.isEditionEnabled();
		}
		@Override
		public void setUpdatedPart(String updatedPart) {
			LauncherEditor.this.setUpdatedPart(updatedPart);
		}
	}
	
	public class ProcedureModel implements IModel<Procedure> {
		private Procedure procedure;
		private IModel<Procedure> model;
		private String id;
		public ProcedureModel(Procedure procedure) {
			this.procedure = procedure;
			this.id = String.valueOf(procedure.getId());
			if (!library(procedure)) {
				model = new ObjectModel<Procedure>(procedure);
			}
		}
		public Procedure getObject() {
			if (this.procedure==null) {
				if (model!=null) {
					this.procedure = model.getObject();
				}
				else {
					for (Procedure procedure : getLibrary()) {
						if (String.valueOf(procedure.getId()).equals(id)) {
							this.procedure = procedure;
						}
					}
				}
			}	
			return this.procedure;
		}
		public void setObject(Procedure procedure) {
			this.procedure = procedure;
			this.id = String.valueOf(procedure.getId());
		}
		public void detach() {
			if (model!=null)
				model.detach();
			procedure=null;
		}
	}

	
	/***
	 * 
	 * @param id
	 * @param model
	 */
	public LauncherEditor(String id, IModel<ProcessLauncher> model) {
		super(id, model);
		//this.isprocedurelink=isprocedurelink;
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setEditionEnabled(false);
		
		setAclModel(new AclModel());
		setAclEditor(new AclEditor());
		
		final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		AlertPanel<Void> pa=new AlertPanel<Void>("launcher.info", AlertPanel.INFO, null, 
				getLabel("process-launcher"), 
				getLabel("launcher.info"));
		pa.setIcon(AlertPanel.HELP_INFO);
		form.add(pa);
		
		
		form.add(new TextField<String>("label", true));
		
		form.add(new TextAreaField<String>("description", 4, 40,false));
		
		form.add(new BooleanField("enabled") {
			@Override
			public boolean isHelpInfo() {
				return false;
			}
			@Override
			protected void onHelp(AjaxRequestTarget target) {
			}
		});
		
		form.add(new BooleanField("library") {
			@Override
			public boolean isHelpInfo() {
				return false;
			}
			@Override
			protected void onHelp(AjaxRequestTarget target) {
			}
		});
		
		form.add(new BooleanField("apiEnabled") {
			@Override
			public boolean isHelpInfo() {
				return false;
			}
			@Override
			protected void onHelp(AjaxRequestTarget target) {
			}
		});
		
		form.add(new BooleanField("mobile") {
			@Override
			public boolean isHelpInfo() {
				return false;
			}
			@Override
			protected void onHelp(AjaxRequestTarget target) {
			}
		});

		LauncherGroup lg = getModel().getObject().getLauncherGroup();
		
		if (lg!=null)
			setLauncherModel(new ObjectModel<LauncherGroup>(lg));
		else {
			List<LauncherGroup> list = getLauncherGroups();
			if (list!=null && list.size()>0)
				setLauncherModel(new ObjectModel<LauncherGroup>(list.get(0)));
		}

		form.add(new ChoiceField<LauncherGroup>("launcherGroup", getLauncherModel(), new PropertyModel<List<LauncherGroup>>(LauncherEditor.this, "launcherGroups"), true) {
			protected String getDisplayValue(LauncherGroup value) {
				return value.getName();
			}
			@Override
			public boolean isVisible() {
				return !getLauncherGroups().isEmpty();
			}
		});
		
		form.add(new ClassifiersRulesEditor<ProcessLauncher>("classifiersrules") {
			@Override
			public List<ClassificationRule> getRules() {
				return getRule().getRules(ClassificationRule.class);
			}
			@Override
			public void setRules(List<ClassificationRule> rules) {
				LauncherEditor.this.setRules(rules);
			}
		});
		
		form.add(new AttributesRulesEditor<ProcessLauncher>("attributesrules") {
			@Override
			public List<AttributeRule> getRules() {
				return getRule().getRules(AttributeRule.class);
			}
			@Override
			public void setRules(List<AttributeRule> rules) {
				LauncherEditor.this.setRules(rules);
			}
		});
		
		form.add(new ScriptRuleEditor<ProcessLauncher>("scriptrule") {
			@Override
			public ContentTemplate getTemplate() {
				return getLauncher().getContentTemplate();
			}
			@Override
			public ScriptRule getRule() {
				return LauncherEditor.this.getRule().getRule(ScriptRule.class);
			}
			@Override
			public void setRule(ScriptRule rule) {
				LauncherEditor.this.setRules(Collections.singletonList(rule));
			}
		});
		
		form.add(new ScriptEditor<ProcessLauncher>("router") {
			@Override
			public ContentTemplate getTemplate() {
				return getLauncher().getContentTemplate();
			}
		});
		
		form.add(new ScriptEditor<ProcessLauncher>("condition") {
			@Override
			public ContentTemplate getTemplate() {
				return getLauncher().getContentTemplate();
			}
		});
	
		form.add(new AclEditorPanel(getAclEditor(), getCreatePermission()) {
			protected IModel<String> getHelpText() {
				return new StringResourceModel("start", LauncherEditor.this, null);
			}
		});
		
		form.add(new BooleanField("useTemplate") {
			@Override
			public boolean isHelpInfo() {
				return false;
			}
			@Override
			protected void onHelp(AjaxRequestTarget target) {
			}
		});

		
		add(form);
		
		add(new EditButtonsV5<ProcessLauncher>(this) {
			@Override
			public boolean isEnabled() {
				return isRoot() || (role_admin && !isExpressVersion());
			}
			@Override
			public void onSubmitClick(AjaxRequestTarget target) {
				super.onSubmitClick(target);
				fire(new EditorEvent(target));
			}
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
		});
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
	
	public Domain getDomain() {
		return getModelObject().getDomain();
	}
	
	public void update(AjaxRequestTarget target) {
		try {
 			if (!getUpdatedParts().isEmpty()) {
			
 				ProcessLauncher launcher = (KbeeProcessLauncher)getModelObject();
				
 				if (getLauncherModel()!=null && 
 							(  getModel().getObject().getLauncherGroup()==null || 
 							(! getLauncherModel().getObject().getId().equals(getModel().getObject().getLauncherGroup().getId())))) {
 					((KbeeProcessLauncher) getModel().getObject()).setLauncherGroup( getLauncherModel().getObject());
 				}
 						
 				ContentTemplate template = launcher.getContentTemplate();
				
				if (launcher.getContentTemplate().getAcl()!=null  &&
						launcher.getAcl()!=null &&
						launcher.getContentTemplate().getAcl().getId().equals(launcher.getAcl().getId())) {
					KbeeAcl clone = clone(launcher.getAcl());
					((KbeeProcessLauncher)launcher).setAcl(clone);
				}
				
				if (((KbeeProcedure) launcher.getProcedure())!=null) {
					if (((KbeeProcedure) launcher.getProcedure()).getLastModifiedUser()==null)
						((KbeeProcedure) launcher.getProcedure()).setLastModifiedUser(getSessionUser());
					
					((KbeeProcedure) launcher.getProcedure()).setLastModifiedOffsetDateTime(OffsetDateTime.now());
					((KbeeProcedure) launcher.getProcedure()).setName(launcher.getLabel());
				}
				
				template.getService(DOMObjectService.class).update();
				super.reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	protected boolean library(Procedure procedure) {
		for (Procedure libraryprocedure : getLibrary()) {
			if (libraryprocedure.getId().equals(procedure.getId()))
				return true;
		}
		return false;
	}
	
	public List<Procedure> getProcedures() {
		List<Procedure> procedures = new ArrayList<Procedure>();
		if (getModelObject().getProcedure()!=null)
			procedures.add(getModelObject().getProcedure());
		procedures.addAll(getLibrary());
		return procedures;
	}
	
	public List<Procedure> getLibrary() {
		return getDomain().getService(WorkflowDomainService.class).getProceduresLibrary();
	}
	
	public List<LauncherGroup> getLauncherGroups() {
		List<LauncherGroup> list = 	getRepository(LauncherGroup.class).findAll(getDomain());
		list.sort(new Comparator<LauncherGroup>() {
			@Override
			public int compare(LauncherGroup a, LauncherGroup b) {
				try {
					return a.getName().compareToIgnoreCase(b.getName());
				} catch (Exception e) {
					return 0;
				}
			}
			
		});
		return list;
	}
	
	public ProcessLauncher getLauncher() {
		return getModelObject();
	}
	
	public MultipleRule getRule() {
		MultipleRule rule = (MultipleRule)((KbeeProcessLauncher)getLauncher()).getRule();
		if (rule == null) rule = new MultipleRule();
		return rule;
	}
	
	public <T extends WorkflowRule> void setRules(List<T> rules) {
		MultipleRule rule = getRule();
		rule.setRules(rules);
		((KbeeProcessLauncher)getLauncher()).setRule(rule);
	}
	
	public void onDetach() {
		super.onDetach();
		if (aclmodel!=null)
			aclmodel.detach();
	}
	
	public void setLauncherModel(IModel<LauncherGroup> v) {
		launcher_model=v;
	}
	
	public IModel<LauncherGroup> getLauncherModel() {
		return this.launcher_model;
	}
	
	private List<Permission> getCreatePermission() {
		List<Permission> permissions = new ArrayList<Permission>();
		permissions.add(KbeePermission.CREATE);
		return permissions;
	}
	
	private KbeeAcl clone(Acl acl) {
		KbeeAcl clone = new KbeeAcl();
		for (AclEntry entry : acl.getEntries()) {
			KbeeAclEntry entryclone = new KbeeAclEntry(clone, entry.getPrincipal(), entry.isNegative());
			for (Permission permission : ((KbeeAclEntry)entry).getPermissions()) {
				entryclone.addPermission(permission);
			}
			clone.addEntry(getSessionUser(), entryclone);
		}
		getSecurityDao().save(clone);
		return clone;
	}
} 