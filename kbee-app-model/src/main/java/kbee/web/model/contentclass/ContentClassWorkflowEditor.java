package kbee.web.model.contentclass;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.ContentTemplate;

import com.novamens.content.workflow.WorkflowDomainService;

import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;

import kbee.web.model.procedure.ProcedurePage;

@SuppressWarnings("serial")
public class ContentClassWorkflowEditor extends ModelPanel<ContentTemplate> {
	private static final long serialVersionUID = 1L;
	
//	static private Logger logger = Logger.getLogger(ContentClassWorkflowEditor.class.getName());
	
	/**
	 * @param id
	 * @param model
	 */
	public ContentClassWorkflowEditor(String id, IModel<ContentTemplate> model) {
		super(id, model);
	
		if (getModel().getObject().isOnlyRootEdit() && !isRoot()) {
			add(new InvisiblePanel("new.button"));
		}
		else {
			add(new NewBusinessProcessButton("new.button") {
				@Override
				protected void onCreate(Procedure prototype, AjaxRequestTarget target) {
					getNewProcedure(prototype);
					//ProcessLauncher launcher = getNewLauncher(procedure);
					//@SuppressWarnings("unchecked")
					//AjaxTabbedPanel<ITab> tabs = (AjaxTabbedPanel<ITab>) ContentClassWorkflowEditor.this.get("tabs");
					//tabs.getTabs().add(new LauncherTab(new LauncherModel(launcher)));
					//tabs.setSelectedTab(tabs.getTabs().size()-1);
					target.add(ContentClassWorkflowEditor.this);
				}
			});
		}
		addTable();
		add(new ConfirmationDialog("confirmation.dialog"));
	}
	
	public List<Procedure> getProcedures() {
		return getModelObject().getProcedures();
	}
	
	private boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	private Procedure getNewProcedure(Procedure prototype) {
		Procedure procedure = getDomain().getService(WorkflowDomainService.class).createProcedure(getModelObject(), prototype);
		return procedure;
	}

	
//	private ProcessLauncher getNewLauncher(Procedure procedure) {
//	
//		try {
//			
//			ContentTemplate template = getModelObject();
//			KbeeProcessLauncher launcher = new KbeeProcessLauncher();
//			
//			
//			launcher.setLabel(template.getName() + " - " + procedure.getDisplayName() + " " + String.valueOf(getModelObject().getProcessLaunchers().size()+1));
//			
//			launcher.setCreationOffsetDateTime(OffsetDateTime.now());
//			launcher.setLastModifiedOffsetDateTime(OffsetDateTime.now());
//			launcher.setLastModifiedUser(getSessionUser());
//			
//			launcher.setState(ObjectState.ENABLED);
//			launcher.setDomain(template.getDomain());
//			launcher.setContentTemplate(template);
//			
//			List<LauncherGroup> list= this.getLauncherGroups();
//			if(list!=null && list.size()>0) {
//				launcher.setLauncherGroup(list.get(0));
//			}
//			
//			launcher.setEnabledContext(true);
//			launcher.setEnabled(true);
//
//			KbeeAcl acl = new KbeeAcl(); 
//			acl.setLastModifiedUser(getSessionUser());
//			acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
//
//			List<Group> fgroups = null;
//			fgroups = getSecurityDao().getCanonicalGroups(getDomain());
//			Group fusers = null;
//			for (Group group: fgroups) {
//				if (group.getName().equals(KbeeGlobalRole.USER.getId())) {
//					fusers = group;
//					break;
//				}
//			}
//			if (fusers!=null) {
//				AclEntry docuentry = new KbeeAclEntry(acl, fusers, false);
//				List<Permission> docupermissions= new ArrayList<Permission>();
//				docupermissions.add(KbeePermission.CREATE);
//				docuentry.setPermissions(docupermissions);
//				acl.addEntry(getSessionUser(), docuentry);
//				launcher.setAcl(acl);
//			} else {
//				logger.error("can not find group USERS");
//				launcher.setAcl(new KbeeAcl());
//			}
//			
//			template.getProcessLaunchers().add(launcher);
//			template.getService(DOMObjectService.class).update();
//			
//			launcher.setProcedure(procedure);
//			template.getService(DOMObjectService.class).update();
//			
//			return launcher;
//		}
//		catch (ContentMgmtException e) {
//			logger.error(e);
//			return null;
//		}
//	}
	
	private void addTable() {
		add(new ListView<Procedure>("procedures", () -> getProcedures()) {
			public void populateItem(final ListItem<Procedure> item) {
				Procedure procedure =  item.getModelObject();
				IModel<Procedure> model = new ObjectModel<Procedure>(procedure);
				Link<Void> link =   new Link<Void>("procedure.link") {
					public void onClick() {
						setResponsePage(new ProcedurePage(model));	
					}
				};	
				link.add(new Label("procedure.name", procedure.getDisplayName()));
				item.add(link);
				item.add(new Label("procedure.description", procedure.getDescription()));
			}
		});	
	}
	
	
//	private List<LauncherGroup> getLauncherGroups() {
//		return getRepository(LauncherGroup.class).findAll();
//	}
//	
//	private ContentSecurityDao getSecurityDao() {
//		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
//	}
}
