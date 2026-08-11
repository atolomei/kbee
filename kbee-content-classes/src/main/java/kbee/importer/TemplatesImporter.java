package kbee.importer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;


import com.novamens.content.base.ContentClass;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EComponentType;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EModelType;
import com.novamens.content.form.EText;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.form.EFormAbstractComponent;
import com.novamens.kbee.content.form.EFormAbstractField;
import com.novamens.kbee.content.form.KbeeEBooleanAttributeModel;
import com.novamens.kbee.content.form.KbeeEBooleanField;
import com.novamens.kbee.content.form.KbeeEBooleanModel;
import com.novamens.kbee.content.form.KbeeEClassifierFieldModel;
import com.novamens.kbee.content.form.KbeeEDateAttributeModel;
import com.novamens.kbee.content.form.KbeeEDateField;
import com.novamens.kbee.content.form.KbeeEDateModel;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.form.KbeeEFormRow;
import com.novamens.kbee.content.form.KbeeEFormSection;
import com.novamens.kbee.content.form.KbeeEHtmlField;
import com.novamens.kbee.content.form.KbeeEMemberAutoCompleteField;
import com.novamens.kbee.content.form.KbeeEMemberComboField;
import com.novamens.kbee.content.form.KbeeEMembersListField;
import com.novamens.kbee.content.form.KbeeENumberAttributeModel;
import com.novamens.kbee.content.form.KbeeENumberField;
import com.novamens.kbee.content.form.KbeeEResource;
import com.novamens.kbee.content.form.KbeeEResourceFieldModel;
import com.novamens.kbee.content.form.KbeeEResourceSystem;
import com.novamens.kbee.content.form.KbeeEResourceSystemFieldModel;
import com.novamens.kbee.content.form.KbeeEResources;
import com.novamens.kbee.content.form.KbeeEStringAttributeModel;
import com.novamens.kbee.content.form.KbeeEStringField;
import com.novamens.kbee.content.form.KbeeEStringModel;
import com.novamens.kbee.content.form.KbeeEText;
import com.novamens.kbee.content.form.KbeeETextField;
import com.novamens.kbee.content.form.KbeeETitle;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeAttributeTemplate;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeClassifierTemplate;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.model.KbeeExtractionMacro;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.kbee.content.workflow.DynamicStates;
import com.novamens.kbee.content.workflow.KbeeAttributeRule;
import com.novamens.kbee.content.workflow.KbeeClassificationRule;
import com.novamens.kbee.content.workflow.KbeeCollaboratorTrigger;
import com.novamens.kbee.content.workflow.KbeeContentProcedure;
import com.novamens.kbee.content.workflow.KbeeLastUserAutomaticTrigger;
import com.novamens.kbee.content.workflow.KbeeLastUserManualTrigger;
import com.novamens.kbee.content.workflow.KbeeManualTrigger;
import com.novamens.kbee.content.workflow.KbeeProcedurePhase;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.kbee.content.workflow.KbeeRoundRobin;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.content.workflow.KbeeUserAutomaticTrigger;
import com.novamens.kbee.content.workflow.KbeeUserTrigger;
import com.novamens.kbee.content.workflow.KbeeWRole;
import com.novamens.kbee.content.workflow.KbeeWRoleTrigger;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.FormLayout;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.RouterType;
import com.novamens.workflow.Task;
import com.novamens.workflow.Trigger;
import com.novamens.workflow.TriggerType;

import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.IAcl;
import kbee.api.model.IAclEntry;
import kbee.api.model.IAction;
import kbee.api.model.ApiClassifier;
import kbee.api.model.IComponent;
import kbee.api.model.IForm;
import kbee.api.model.IGroup;
import kbee.api.model.IKeyValue;
import kbee.api.model.ILauncher;
import kbee.api.model.ILauncherGroup;
import kbee.api.model.IModelAttribute;
import kbee.api.model.IModelElement;
import kbee.api.model.IResourceTag;
import kbee.api.model.IRule;
import kbee.api.model.ITask;
import kbee.api.model.ITaskForm;
import kbee.api.model.ITemplate;
import kbee.api.model.ITrigger;
import kbee.api.service.ApiService;

public class TemplatesImporter extends ClassificablesImporter {
	
	private int total = 0;
	private int updated = 0;

	public TemplatesImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain);
	}
	
	public void execute() throws ContentMgmtException  {
		int i = 0;
		try {
			for (ITemplate remote : getRemoteTemplates()) {
				ContentTemplate local = getLocal(KbeeContentTemplate.class, remote);
				if (local==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime()) || forceUpdate()) {
					if (local == null) {
						local = createTemplate();
						setLocal(remote, local);
					}
					syncTemplate(remote, local);
					update(local);
					updated++;
					setProgress(i);
					logger.info("Template "+local.getDisplayName());
				}
				else {
					logger.info("Template "+local.getDisplayName() + " not modified");
				}
			}
			getContentDao().flush();
		}
		catch (Throwable e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new ContentMgmtException(e);
		}
	}

	@Override
	public int getTotal() {
		if (total == 0) {
			List<ITemplate> templates = getServer().getTemplates();
			total = (int)templates.size();
		}
		return total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" templates processed. ";
		result += String.valueOf(updated)+" templates updated</p>";
		return result;
	}
	
	protected Task createTask() {
		return null;
	}
	
	private List<ITemplate> getRemoteTemplates() {
		return getServer().getTemplates();
	}
	
	private ContentTemplate createTemplate() throws ContentCreationException {
		return (ContentTemplate)ServiceLocator.getService(ObjectFactoryService.class).createTemplate(false);
	}
	
	private EForm createForm(ContentTemplate template) throws ContentCreationException {
		return (EForm)ServiceLocator.getService(ObjectFactoryService.class).createEForm(template);
	}
	
	private ProcessLauncher createLauncher(ContentTemplate template) {
		return (ProcessLauncher)ServiceLocator.getService(ObjectFactoryService.class).createLauncher(template);
	}
	
	private KbeeContentProcedure createProcedure(ContentTemplate template) {
		KbeeContentProcedure procedure = new KbeeContentProcedure();
		procedure.setDomain(getSessionDomain());
		procedure.setState(ObjectState.ENABLED);
		procedure.setStates(new DynamicStates());
		procedure.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		procedure.setLastModifiedUser(getUser());
		procedure.setContentTemplate(template);
		update(procedure);
		return procedure;
	}
	
	private void syncTemplate(ITemplate remote, ContentTemplate local) throws IOException {
		local.setName(remote.getDisplayName());
		local.setState(ObjectState.valueOf(remote.getState()));
		local.setContentClass(getContentClass(remote.getBaseClass()));
		local.setTitleEditable(remote.isTitleEditable());
		((KbeeContentTemplate)local).setOnlyRootEdit(remote.isOnlyRoot());
		if (remote.getTitleRule()!=null) {
			((KbeeContentTemplate)local).setTitleRule(new KbeeExtractionMacro(remote.getTitleRule()));
		}
		((KbeeContentTemplate)local).setConsoleSubtitleRule(remote.getConsoleSubline());
		((KbeeContentTemplate)local).setPortalsSubtitleRule(remote.getPortalSubline());
		local.setTitleEditable(remote.isTitleEditable());
		List<ModelElementTemplate> elements = new ArrayList<ModelElementTemplate>();
		if (remote.getStructure()!=null)
		for (IModelElement element : remote.getStructure()) {
			if ("classifier".equals(element.getAttribute().getRel())) {
				KbeeClassifierTemplate template = new KbeeClassifierTemplate();
				Classifier classifier =  getClassifier(element.getAttribute());
				if (classifier!=null) {
					template.setClassifier(classifier);
					template.setMultiplicity(Multiplicity.valueOf((element.getMutiplicity())));
					elements.add(template);
				}	
			}
			else {
				KbeeAttributeTemplate template = new KbeeAttributeTemplate();
				template.setAttribute(getAttribute(element.getAttribute()));
				template.setMultiplicity(Multiplicity.valueOf((element.getMutiplicity())));
				elements.add(template);
			}
		}
		local.setStructure(elements);
		getContentDao().flush();
		update(local);
		
		if (remote.getResourceTags()!=null) {
			List<ResourceTag> tags = new ArrayList<ResourceTag>();
			for (ApiProxy proxy : remote.getResourceTags()) {
				IResourceTag itag = getServer().getResourceTag(proxy.getId());
				if (itag!=null) {
					ResourceTag tag = getLocal(KbeeResourceTag.class, itag);
					if (tag!=null) {
						tags.add(tag);
					}
				}
			}
			((KbeeContentTemplate)local).setResourceTags(tags);
		}
		
		if (remote.getForms()!=null) {
			List<EForm> forms = new ArrayList<EForm>();
			for (ApiProxy proxy : remote.getForms()) {
				forms.add(importForm(proxy, local));
			}
			((KbeeContentTemplate)local).setForms(forms);
		}
		
//		if (remote.getLaunchers()!=null) {
//			List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
//			for (ILauncher ilauncher : remote.getLaunchers()) {
//				ProcessLauncher localLauncher = getLocal(KbeeProcessLauncher.class, ilauncher);
//				if (localLauncher == null) {
//					localLauncher = createLauncher(local);
//					getContentDao().flush();
//					setLocal(ilauncher, localLauncher);
//				}
//				syncLauncher(ilauncher, (KbeeProcessLauncher)localLauncher);
//				update(localLauncher);
//				launchers.add(localLauncher);
//			}
//		}
		
		if (remote.getProcedures()!=null) {
			for (ApiProxy procedureproxy : remote.getProcedures()) {
				ApiProcedure iprocedure = getServer().getProcedure(procedureproxy.getId());
				KbeeContentProcedure procedure = getLocal(KbeeContentProcedure.class, iprocedure);
				if (procedure == null) {
					procedure = createProcedure(local);
					setLocal(iprocedure, procedure);
				}
				syncProcedure(iprocedure, procedure);
				update(procedure);
				((KbeeContentTemplate)local).addProcedure(procedure);
			}
		}	
	}
	
	private EForm importForm(ApiProxy proxy, ContentTemplate template) throws IOException {
		IForm remote = getServer().getForm(proxy.getId());
		if (remote!=null) {
			EForm local = getLocal(KbeeEForm.class, remote);
			if (local == null) {
				local = createForm(template);
				setLocal(remote, (KbeeEForm)local);
			}
			syncForm(remote, (KbeeEForm)local);
			return local;
		}
		return null;
		
	}
	
	private void syncLauncher(ILauncher remote, KbeeProcessLauncher local) throws IOException {
		local.setLabel(remote.getDisplayName());
		local.setEnabled(remote.isNewDocumentEnabled());
		local.setDescription(remote.getDescription());
		local.setLibrary(remote.isLibraryEnabled());
		Acl acl = local.getAcl();
		syncAcl(remote.getAcl(), acl);
		if (remote.getGroup()!=null) {
			local.setLauncherGroup(getLauncherGroup(remote.getGroup()));
		}
	}
	
	private void syncAcl(IAcl remote, Acl local) throws IOException {
		try {
			List<AclEntry> entries = ((KbeeAcl)local).getEntries(); 
			while (!entries.isEmpty()) {
				for (AclEntry entry : entries) {
					local.removeEntry(getUser(), (KbeeAclEntry)entry);
					break;
				}
				entries = ((KbeeAcl)local).getEntries(); 
			}
			if (remote.getEntries()!=null)
			for (IAclEntry entry : remote.getEntries()) {
				KbeeAclEntry localentry = new KbeeAclEntry();
				IGroup igroup = getServer().getGroup(entry.getPrincipal().getId());
				Group group = getLocalGroup(igroup);
				if (group!=null) {
					List<Permission> permissions = new ArrayList<Permission>();
					for (String permissionvalue : entry.getPermissions()) {
						Permission permission = KbeePermission.valueOf(permissionvalue);
						permissions.add(permission);
					}
					localentry.setPrincipal(group);
					localentry.setPermissions(permissions);
					local.addEntry(getUser(), localentry);
				}
			}
		}
		catch (Exception e) {
		}
	}

	
	private void syncForm(IForm remote, KbeeEForm local) {
		local.setName(remote.getName());
		local.setDisplayName(remote.getDisplayName());
		local.setFormAccessLevel(EFormAccessLevel.valueOf(remote.getDisplayLevel()));
		local.setComponents(getComponents(remote.getComponents()));
		local.setFileContainer(remote.isFileContainer());
		local.setViewer(remote.getViewer());
	}
	
	private List<EFormComponent> getComponents(List<IComponent> icomponents) {
		List<EFormComponent> components = new ArrayList<EFormComponent>();
		for (IComponent icomponent : icomponents) {
			EFormAbstractComponent component = (EFormAbstractComponent)getComponent(icomponent);
			if (component!=null) {
				component.setName(icomponent.getName());
				component.setLabel(icomponent.getLabel());
				component.setCssClass(icomponent.getCss());
				component.setVisibleCondition(icomponent.getVisible());
				component.setEnabledCondition(icomponent.getEnabled());
				if (component instanceof EFormField) {
					((EFormAbstractField<?>)component).setSublabel(icomponent.getSublabel());
					((EFormAbstractField<?>)component).setModel(getModel(icomponent));
					((EFormAbstractField<?>)component).setCalculation(icomponent.getCalculation());
				}
				if (component instanceof EText) {
					((KbeeEText)component).setText(icomponent.getText());
				}
				if (component instanceof EFormContainer && icomponent.getChilds()!=null) {
					((EFormContainer)component).setComponents(getComponents(icomponent.getChilds()));
				}
				components.add(component);
			}
		}
		return components;
	}
	
	private EFormComponent getComponent(IComponent icomponent) {
		if (EComponentType.ROW.getLabel().equals(icomponent.getType())) {
			return new KbeeEFormRow();
		}
		if (EComponentType.SECTION.getLabel().equals(icomponent.getType())) {
			return new KbeeEFormSection();
		}
		if (EComponentType.COMBO.getLabel().equals(icomponent.getType())) {
			return new KbeeEMemberComboField();
		}
		if ("AutoComplete".equals(icomponent.getType())) {
			return new KbeeEMemberAutoCompleteField();
		}
		if (EComponentType.STRING.getLabel().equals(icomponent.getType())) {
			return new KbeeEStringField();
		}
		if (EComponentType.DATE.getLabel().equals(icomponent.getType())) {
			return new KbeeEDateField();
		}
		if (EComponentType.RESOURCE_SYSTEM.getLabel().equals(icomponent.getType())) {
			return new KbeeEResourceSystem();
		}
		if (EComponentType.RESOURCE.getLabel().equals(icomponent.getType())) {
			return new KbeeEResource();
		}
		if (EComponentType.RESOURCES.getLabel().equals(icomponent.getType())) {
			return new KbeeEResources();
		}
		if (EComponentType.HTML.getLabel().equals(icomponent.getType())) {
			return new KbeeEHtmlField();
		}
		if (EComponentType.TEXT.getLabel().equals(icomponent.getType())) {
			return new KbeeETextField();
		}
		if (EComponentType.LIST.getLabel().equals(icomponent.getType())) {
			return new KbeeEMembersListField();
		}
		if (EComponentType.STATIC_TEXT.getLabel().equals(icomponent.getType())) {
			return new KbeeEText();
		}
		if (EComponentType.TITLE.getLabel().equals(icomponent.getType())) {
			return new KbeeETitle();
		}
		if (EComponentType.NUMBER.getLabel().equals(icomponent.getType())) {
			return new KbeeENumberField();
		}
		if (EComponentType.BOOLEAN.getLabel().equals(icomponent.getType())) {
			return new KbeeEBooleanField();
		}
		return null;
	}
	
	private EFieldModel<?> getModel(IComponent icomponent) {
		if (EModelType.CLASSIFIER.getLabel().equals(icomponent.getModel())) {
			KbeeEClassifierFieldModel model = new KbeeEClassifierFieldModel();
			model.setClassifier(getClassifier(icomponent.getClassifier()));
			model.setAccessStrategy(AccessStrategy.All);
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && EComponentType.STRING.getLabel().equals(icomponent.getType())) {
			KbeeEStringAttributeModel model = new KbeeEStringAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && "Date".equals(icomponent.getType())) {
			KbeeEDateAttributeModel model = new KbeeEDateAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && "Boolean".equals(icomponent.getType())) {
			KbeeEBooleanAttributeModel model = new KbeeEBooleanAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && "Html".equals(icomponent.getType())) {
			KbeeEStringAttributeModel model = new KbeeEStringAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && "Text".equals(icomponent.getType())) {
			KbeeEStringAttributeModel model = new KbeeEStringAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && "Number".equals(icomponent.getType())) {
			KbeeENumberAttributeModel model = new KbeeENumberAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Resource".equals(icomponent.getModel())) {
			KbeeEResourceFieldModel model = new KbeeEResourceFieldModel();
			model.setTag(getResourceTag(icomponent.getResourceTag()));
			return model;
		}
		if (EModelType.RESOURCE_SYSTEM.getLabel().equals(icomponent.getModel())) {
			KbeeEResourceSystemFieldModel model = new KbeeEResourceSystemFieldModel();
			model.setTag(getResourceTag(icomponent.getResourceTag()));
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && "String".equals(icomponent.getType())) {
			KbeeEStringModel model = new KbeeEStringModel();
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && "Date".equals(icomponent.getType())) {
			KbeeEDateModel model = new KbeeEDateModel();
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && EComponentType.BOOLEAN.getLabel().equals(icomponent.getType())) {
			KbeeEBooleanModel model = new KbeeEBooleanModel();
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && "Html".equals(icomponent.getType())) {
			KbeeEStringModel model = new KbeeEStringModel();
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && "Text".equals(icomponent.getType())) {
			KbeeEStringModel model = new KbeeEStringModel();
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && "Number".equals(icomponent.getType())) {
			KbeeEStringModel model = new KbeeEStringModel();
			return model;
		}
		return null;
	}
	
	private void syncProcedure(ApiProcedure remote, KbeeContentProcedure local) throws IOException {
		local.setName(remote.getName());
		local.setAlias(remote.getAlias());
		local.setVersion(2);
		local.setDisplayName(remote.getDisplayName());
		local.setLastModifiedUser(getUser());
		
		WorkflowRule initialrule = getRule(remote);
		if (initialrule!=null) local.setInitialRule(initialrule);

		List<RoleInProcess> roles = new ArrayList<RoleInProcess>();
		if (remote.getRoles()!=null)
		for (IKeyValue irole : remote.getRoles()) {
			KbeeWRole role = new KbeeWRole();
			role.setName(irole.getKey());
			role.setLabel(irole.getValue());
			roles.add(role);
		}
		local.setRoles(roles);
		
		if (remote.getPhases()!=null) {
			List<ProcedurePhase> phases = new ArrayList<ProcedurePhase>();
			for (IKeyValue iphase : remote.getPhases()) {
				KbeeProcedurePhase phase = new KbeeProcedurePhase();
				phase.setName(iphase.getKey());
				phase.setLabel(iphase.getValue());
				phases.add(phase);
			}
			local.setPhases(phases);
		}
		
		List<Task> tasks = new ArrayList<Task>();
		for (ITask itask :remote.getTasks()) {
			tasks.add(importTask(itask, local));
		}
		local.setTasks(tasks);
		
		update(local);
		getContentDao().flush();
		
		if (remote.getLaunchers()!=null) {
			List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
			for (ILauncher ilauncher : remote.getLaunchers()) {
				ProcessLauncher localLauncher = getLocal(KbeeProcessLauncher.class, ilauncher);
				if (localLauncher == null) {
					localLauncher = createLauncher(((ContentProcedure)local).getContentTemplate());
					getContentDao().flush();
					setLocal(ilauncher, localLauncher);
				}
				syncLauncher(ilauncher, (KbeeProcessLauncher)localLauncher);
				local.addLauncher(localLauncher);
				update(localLauncher);
				launchers.add(localLauncher);
			}
		}
	}
	
	private KbeeTask importTask(ITask itask, Procedure procedure) {
		KbeeTask task = (KbeeTask)createTask();
		task.setProcedure(procedure);
		task.setId(itask.getId());
		task.setName(itask.getDisplayName());
		task.setInitial(itask.isInitial());
		task.setRoleName(itask.getRole());
		task.setPhaseName(itask.getPhase());
		task.setEditableTitle(itask.isEditableTitle());
		
		task.setTrigger(getTrigger(itask.getTrigger(), procedure));
		
		List<EndCondition> actions = new ArrayList<EndCondition>();
		for (IAction iaction : itask.getActions()) {
			ManualEndCondition action = new ManualEndCondition(iaction.getLabel(), iaction.getEvent());
			action.setCollaboration(iaction.isCollaboration());
			action.setEnabled(iaction.isEnabled());
			action.setDescription(iaction.getDescription());
			action.setEnablePriority(iaction.isPriority());
			if (iaction.getRouter()!=null)
			action.setRouter(RouterType.valueOf(iaction.getRouter()));
			action.setNextTaskId(iaction.getNextTask());
			action.setRouterScript(iaction.getRouterScript());
			action.setRule(getRule(iaction));
			action.setDefault(iaction.isDefa());
			action.setAutoRunAfter(iaction.getAutoRunAfter());
			if (iaction.getTrigger()!=null) {
				action.setTrigger(getTrigger(iaction.getTrigger(), procedure));
			}
			if (iaction.getCollaborationGroups()!=null) {
				List<Group> collaborators = new ArrayList<Group>();
				for (ApiProxy groupproxy : iaction.getCollaborationGroups()) {
					IGroup igroup = getServer().getGroup(groupproxy.getId());
					Group group = getLocalGroup(igroup);
					if (group!=null)
					collaborators.add(group);
				}
				action.setCollaborationGroups(collaborators);
			}
			actions.add(action);
		}
		task.setEndConditions(actions);
		List<EForm> forms = new ArrayList<EForm>();
		if (itask.getForms()!=null)
		for (ITaskForm formproxy : itask.getForms()) {
			IForm iform = getServer().getForm(formproxy.getId());
			if (iform!=null) {
				EForm form = getLocal(KbeeEForm.class, iform);
				if (form!=null) {
					KbeeTaskForm taskform = new KbeeTaskForm(form);
					taskform.setSignatureRequired(formproxy.isSignatureRequired());
					taskform.setReadOnly(formproxy.isReadonly());
					taskform.setFormLayout(FormLayout.valueOf(formproxy.getLayout()));
					forms.add(taskform);
				}
			}
		}
		task.setForms(forms);
		return task;
	}
	
	private Trigger getTrigger(ITrigger itrigger, Procedure procedure) {
		
		Trigger trigger = getTrigger(itrigger.getType());
		
		if (itrigger.getManualPermission()!=null) {
			((KbeeUserTrigger)trigger).setManualPermission(getPermission(itrigger.getManualPermission(), procedure));
		}
		
		if (trigger instanceof KbeeUserAutomaticTrigger) {
			KbeeRoundRobin roundRobin = new KbeeRoundRobin();
			roundRobin.setPermission(getPermission(itrigger.getPermission(), procedure));
			roundRobin.setBackupPermission(getPermission(itrigger.getBackupPermission(), procedure));
			((KbeeUserAutomaticTrigger)trigger).setUserAssignationStrategy(roundRobin);
		}
		return trigger;
	}

	
	private Trigger getTrigger(String type) {
		Trigger trigger = null;
		if (type == null) {
			trigger = new KbeeManualTrigger();
		}
		else
		if (type.equals(TriggerType.AUTOMATIC.name())) {
			trigger = new KbeeUserAutomaticTrigger();
		}
		else
		if (type.equals(TriggerType.USERAUTOMATIC.name()))  {
			trigger = new KbeeUserAutomaticTrigger();
		}
		else
		if (type.equals(TriggerType.OLDUSERAUTOMATIC.name()) || 
			type.equals(TriggerType.USERAUTOMATIC_LASTUSER.name())) {
			trigger = new KbeeLastUserAutomaticTrigger();
		}
		else
		if (type.equals(TriggerType.MANUAL.name()))  {
			trigger = new KbeeManualTrigger();
		}
		else
		if (type.equals(TriggerType.MANUAL_LASTUSER.name()))  {
			trigger = new KbeeLastUserManualTrigger();
		}
		else
		if (type.equals(TriggerType.ROLE.name()))  {
			trigger = new KbeeWRoleTrigger();
		}
		else
		if (type.equals(TriggerType.COLLABORATOR.name()))  {
			trigger = new KbeeCollaboratorTrigger();
		}
		else {
			trigger = new KbeeManualTrigger();
		}
		return trigger;
	}
	
	private Permission getPermission(IKeyValue ipermission, Procedure procedure) {
		if (ipermission==null) return null;
		String action = ipermission.getKey();
		String label = ipermission.getValue();
		KbeePermission permission = new KbeePermission(action, label);
		permission.setAction(action);
		return permission;
	}
	
	private WorkflowRule getRule(ApiProcedure iprocedure) {
		List<WorkflowRule> rules = new ArrayList<WorkflowRule>();
		if (iprocedure.getRules()!=null) {
			for (IRule irule : iprocedure.getRules()) {
				WorkflowRule rule = getRule(irule);
				if (rule!=null) {
					rules.add(rule);
				}
			}
		}
		if (!rules.isEmpty()) {
			MultipleRule rule = new MultipleRule(rules);
			return rule;
		}
		return null;
	}
	
	private WorkflowRule getRule(IAction action) {
		List<WorkflowRule> rules = new ArrayList<WorkflowRule>();
		if (action.getRules()!=null) {
			for (IRule irule : action.getRules()) {
				WorkflowRule rule = getRule(irule);
				if (rule!=null) {
					rules.add(rule);
				}
			}
		}
		if (!rules.isEmpty()) {
			MultipleRule rule = new MultipleRule(rules);
			return rule;
		}
		return null;
	}
	
	private WorkflowRule getRule(IRule irule) {
		WorkflowRule rule = null;
		if ("classification".equals(irule.getType())) {
			Classifier classifier = null;
			DataSetMember value = null;
			ApiClassifier iclassifier = null;
			ApiValue ivalue = null;
			if (irule.getClassifier()!=null) {
				iclassifier = getServer().getClassifier(irule.getClassifier().getId());
			}
			if (iclassifier!=null) {
				classifier = getLocal(KbeeClassifier.class, iclassifier);
			}
			if (classifier!=null && irule.getValue()!=null) {
				ivalue = new ApiValue();
				ivalue.setId(irule.getValue().getId());
				ivalue.setDomain(iclassifier.getDomain());
				value = getLocal(KbeeDataSetMember.class, ivalue);
			}
			if (value!=null && classifier!=null) {
				rule = new KbeeClassificationRule(classifier, value);
			}
		}
		else
		if ("attribute".equals(irule.getType())) {
			Attribute attribute = null;
			IModelAttribute iattribute = null;
			if (irule.getAttribute()!=null) {
				iattribute = getServer().getAttribute(irule.getAttribute().getId());
			}
			if (iattribute!=null) {
				attribute = getLocal(KbeeAttribute.class, iattribute);
			}
			if (attribute!=null && irule.getStringValue()!=null) {
				rule = new KbeeAttributeRule(attribute, irule.getStringValue());
			}
		}
		return rule;
	}
	
	private Group getLocalGroup(IGroup igroup) {
		if (igroup.isCanonical()) {
			for (Group group : getSecurityDao().getGroups(getDomain())) {
				if (igroup.getName().equals(group.getName())) {
					return group;
				}
			}
		}
		else {
			return getLocal(KbeeGroup.class, igroup);
		}
		return null;
	}
	
	private Classifier getClassifier(ApiProxy proxy) {
		ApiClassifier remote = getServer().getClassifier(proxy.getId());
		Classifier local = getLocal(KbeeClassifier.class, remote);
		return local;
	}
	
	private Attribute getAttribute(ApiProxy proxy) {
		IModelAttribute remote = getServer().getAttribute(proxy.getId());
		Attribute local = getLocal(KbeeAttribute.class, remote);
		return local;
	}
	
	private ResourceTag getResourceTag(ApiProxy proxy) {
		IResourceTag remote = getServer().getResourceTag(proxy.getId());
		ResourceTag local = getLocal(KbeeResourceTag.class, remote);
		return local;
	}
	
	private LauncherGroup getLauncherGroup(ApiProxy proxy) {
		ILauncherGroup remote = getServer().getLauncherGroup(proxy.getId());
		LauncherGroup local = getLocal(KbeeLauncherGroup.class, remote);
		return local;
	}
	
	private ContentClass getContentClass(String name) {
		return getContentDao().findContentClassByName(name);
	}
}