package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxLink;
import com.novamens.beans.BeansService;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.content.workflow.KbeeCollaboratorTrigger;
import com.novamens.kbee.content.workflow.KbeeForkJoinTask;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.UserTask;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.security.User;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.workflow.ForkJoinTask;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.RouterType;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowThread;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class TasksTablePanel extends ObjectEditorPanel<Procedure> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TaskMainPanel.class.getName());
	
	boolean isEditEnabled = false;
	
	public TasksTablePanel(IModel<Procedure> model) {
		super("tasks");
		
		setOutputMarkupId(true);
		add(new AjaxLink<Void>("help-info") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "Tasks"; }, 
					getTaskTableHelp()	);
			}
		});
				
		add(new ListView<IModel<Task>>("tasks", new  PropertyModel<List<IModel<Task>>>(this, "tasks")) {
			public void populateItem(final ListItem<IModel<Task>> item) {
				
				Task task = item.getModelObject().getObject();
				
				Link<Void> tasklink = new Link<Void>("task-link") {
					public void onClick() {
						setResponsePage(new TaskConfigurationPage(item.getModelObject()));
					}
				};
				
				// LABEL -----
				tasklink.add(new AttributeModifier("title", "open"));
				String initial = ((KbeeTask)task).isInitial() ? ("<span class=\"ago\"> ( " + getLabel("initial").getObject() + " ) </span>") : "";
				Label taskname = new Label("task-name", task.getName());
				item.add( (new Label("initial", initial)).setEscapeModelStrings(false));
				taskname.setEscapeModelStrings(false);
				tasklink.add(taskname);
				item.add(tasklink);

				// W_ROLE
				Label rolename= new Label("role", task.getRole()!=null ? task.getRole().getLabel() : "");
				item.add(rolename);

				// ENABLED ROLES 
				StringBuilder enabledroles = new StringBuilder();
				for (Role role : getEnabledRoles(task)) {
					if (enabledroles.length()>0) 
						enabledroles.append(", ");
					enabledroles.append( getRoleLink( role ) );
				};
				
				Label precondition = new Label("precondition", enabledroles.toString());
				precondition.setEscapeModelStrings(false);
				item.add(precondition);
				
				// TRIGGER				
				String ts ="";
				
				if (task.isInitial()) 
					ts="<span>" + TasksTablePanel.this.getLabelString("initial") +"</span> | ";
				
				ts += (task.getTrigger()!=null && task.getTrigger().getType()!=null ? task.getTrigger().getType().getLabel(  getSessionUser().getLocale() ) : "");
					
				Label triggertype = new Label("trigger", ts);
				triggertype.setEscapeModelStrings(false);
				item.add(triggertype);
				
				IModel<String> conditionsmodel =  getActions(task);
			 
				item.add( (new Label("conditions", conditionsmodel)).setEscapeModelStrings(false));
				
				// DELETE
				item.add( new AjaxLink<Void>("delete-link") {
					public void onClick(AjaxRequestTarget target) {
						ConfirmationDialog dialog = (ConfirmationDialog)TasksTablePanel.this.get("confirmation-dialog");
						Task task = item.getModelObject().getObject();
						dialog.open(target, getLabel("confirmation.DeleteTask", task.getDisplayName()), Dialog.Delete, new Dialog.Handler() {
							@Override
							public void onClick(AjaxRequestTarget target, Button button) {
								if (button.key().equals(Dialog.Delete.key())) {
									try { 
										deleteTask(item.getModelObject().getObject());
										target.add(TasksTablePanel.this);
									} 
									catch (Exception e) {
										logger.error(e);
									}
								}
							}
						});
					}
					@Override
					public boolean isVisible() {
						return isRoot() && getEditor().isEditionEnabled() && isDeleteable(item.getModelObject().getObject());
					}
				});
				
				item.getModel().detach();
			}
		});
		
		add(new AjaxLink<Void>("addtask-link") {
			public void onClick(AjaxRequestTarget target) {
				addTask();
				target.add(TasksTablePanel.this);
			}
			
			public boolean isVisible() {
				return isEditionEnabled() && getProcedure().getVersion()>1;
			}
			
			public boolean isEnabled() {
				return isEditionEnabled() && getProcedure().getVersion()>1;
			}
		});
		
		add(new AjaxLink<Void>("addforktask-link") {
			public void onClick(AjaxRequestTarget target) {
				addForkTask();
				target.add(TasksTablePanel.this);
			}
			
			public boolean isVisible() {
				return isEditionEnabled() && getProcedure().getVersion()>2 && enableThreads();
			}
			
			public boolean isEnabled() {
				return isEditionEnabled() && getProcedure().getVersion()>2;
			}
		});
		
		add(new ConfirmationDialog("confirmation-dialog"));
		add(new InfoDialog("help-modal"));
	}


	public void setEditEnabled( boolean b) {
		this.isEditEnabled=b;
	}
	
	public boolean isEditionEnabled() {
		return isEditEnabled;
	}

	public List<IModel<Task>> getTasks() {
		List<IModel<Task>> tasks = new ArrayList<>();
		for (Task task : getProcedure().getTasks()) {
			tasks.add(new TaskModel(getModel(), task));
		}
		return tasks;
	}
	
	public Procedure getProcedure() {
		return getModel().getObject();
	}
	
	public void onUpdate(AjaxRequestTarget target) {
		target.add(this);
	}
	
	public void updateModel() {
		getWorkflowDao().update(getProcedure());
	}
	
	protected void addTask() {
		WebTask newtask = new WebTask();
		
		int n = 0, i = 0; 
		List<Task> tasks = getProcedure().getTasks();
		
		for (Task task : tasks) {
			if (task.getId().startsWith("newtask")) {
				i++;
			}
			if (task.getName()!=null && task.getName().startsWith("New Task")) {
				n++;
			}
		}
		
		String newid = "newtask" + (i>0 ? String.valueOf(i) : "");
		String newname =  getLabel("new-task").getObject()  + (n>0 ? String.valueOf(n) : "");
		
		newtask.setId(newid);
		newtask.setAlias(newid);
		newtask.setInitial(false);
		newtask.setEnableProgressNotes(true);
		newtask.setEditableTitle(true);
		newtask.setEnableEditingAllResources(true);
		
		if (getProcedure().getPhases().size()>0)
			newtask.setPhase(getProcedure().getPhases().get(0));

		if (getProcedure().getRoles().size()>0)
			newtask.setRole(getProcedure().getRoles().get(0));
		
		newtask.setName(newname);
		newtask.setTrigger(new KbeeCollaboratorTrigger());
		
		tasks.add(newtask);
		
		getProcedure().setTasks(tasks);
		
		updateModel();
	}
	
	protected String getRoleLink(Role role) {
		return "<a  title=\"open Role\" class=\"btn-link\" target=\"_blank\" href=\""+ getServerUrl() + "/security/roles/" + role.getId().toString()+   "\">" + role.getDisplayName() + "</a>";
	}

	protected IModel<String> getTaskTableHelp() {
		return new StringResourceModel("task-table-help-window", TasksTablePanel.this, null);
	}

	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	@Override
	protected Domain getDomain() {
		return ((DomainObject)getProcedure()).getDomain();
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected boolean enableThreads() {
		return true;
	}
		
	protected IModel<String> getActions(Task task) {
		StringBuilder sconditions = new StringBuilder();
		if (task instanceof UserTask && ((UserTask)task).getEndConditions()!=null) {
			for	(EndCondition condition : ((UserTask)task).getEndConditions()) {
				String label = "<span> - " + condition.getLabel() +"</span>";
				if (condition instanceof ManualEndCondition) {
					RouterType router = ((ManualEndCondition)condition).getRouter();
					if (router!=null) {
						if (router.equals(RouterType.TASK)) {
							Task nextTask = ((ManualEndCondition)condition).getNextTask();
							if (nextTask!=null)
								label += "<span class=\"separator\"> > </span>" + TasksTablePanel.this.getLabelString("task") + " [<span class=\"highlight\">"+nextTask.getDisplayName()+" </span>]";
							
							if (((ManualEndCondition)condition).getTrigger()!=null) 
								label += " - <span class=\"agso\"> "+ ((ManualEndCondition)condition).getTrigger().getType().getLabel(getSessionUser().getLocale()) + "</span>"; 
						}
						else if (router.equals(RouterType.PUBLISH)) {
							label += "<span class=\"separator\"> > </span><span class=\"adgo\"> " + TasksTablePanel.this.getLabelString("library") + "</span>";
						}
						else if (router.equals(RouterType.RETURN_TO_CALLER)) {
							label += "<span class=\"separator\"> > </span> <span class=\"highlight\">" +  TasksTablePanel.this.getLabelString("return-caller") +  "</span>";
						}
						else if (router.equals(RouterType.SCRIPT)) {
							label += "<span class=\"separator\"> > </span> <span class=\"highlight\"> " +  TasksTablePanel.this.getLabelString("js-script") + "  </span>";
						}
						else if (router.equals(RouterType.THREAD_END)) {
							label += "<span class=\"separator\"> > </span> <span class=\"highlight\"> " +  TasksTablePanel.this.getLabelString("thread-end") + "  </span>";
						}
					}
				}
				sconditions.append(label+"<br />");
			}
		}
		if (task instanceof ForkJoinTask && !((ForkJoinTask)task).getThreads().isEmpty()) {
			String text = "<table><tr><td style=\"width:20%\"><span style=\"display:table-cell;\">Start</span><span style=\"padding-left: 5px;vertical-align: middle;display:table-cell;\" class=\"separator\">></span></td><td style=\"width:50%\"><table class=\"table-responsive table table-header-bck  table-hover\" style=\"width:100%;\">";
			
			for (WorkflowThread thread : ((ForkJoinTask)task).getThreads()) {
				text += "<tr><td style=\"padding:10px;text-align: center;\"><span style=\"display:block;\">" + thread.getName() + "</span><span style=\"display:block;\">";
				if (thread.getProcedure()!=null) {
					text += "Subproceso[<span class=\"highlight\">"+thread.getProcedure().getDisplayName()+"</span>]";
				}
				text += "</span></td></tr>";
			}
			
			text+="</table></td><td style=\"width:25%\"><span style=\"padding-right:5px;padding-left:5px;display:table-cell;vertical-align: middle;\" class=\"separator\">></span><span style=\"width:75%;display:table-cell;\">Router Script</span></td></tr></table>";
			sconditions.append(text);
		}	
		return  new Model<String>(sconditions.toString());
	}
	
	private void addForkTask() {
		KbeeForkJoinTask newtask = new KbeeForkJoinTask();
		
		int n = 0, i = 0; 
		List<Task> tasks = getProcedure().getTasks();
		
		for (Task task : tasks) {
			if (task.getId().startsWith("newtask")) {
				i++;
			}
			if (task.getName()!=null && task.getName().startsWith("New Task")) {
				n++;
			}
		}
		
		String newid = "newtask" + (i>0 ? String.valueOf(i) : "");
		String newname =  TasksTablePanel.this.getLabel("new-task").getObject()  + (n>0 ? String.valueOf(n) : "");
		
		newtask.setId(newid);
		newtask.setAlias(newid);
		newtask.setInitial(false);
		newtask.setEnableProgressNotes(true);
		newtask.setEditableTitle(true);
		
		if (getProcedure().getPhases().size()>0)
			newtask.setPhase(getProcedure().getPhases().get(0));

		if (getProcedure().getRoles().size()>0)
		newtask.setRole(getProcedure().getRoles().get(0));
		
		newtask.setName(newname);
		newtask.setTrigger(new KbeeCollaboratorTrigger());
		
		
		tasks.add(newtask);
		getProcedure().setTasks(tasks);
		
		getWorkflowDao().update(getProcedure());
	}
	
	private void deleteTask(Task tasktodelete) {
		List<Task> tasks = getProcedure().getTasks();
		for (Task task : tasks) {
			if (task.getId().equals(tasktodelete.getId())) {
				tasks.remove(task);
				break;
			}
		}
		getProcedure().setTasks(tasks);
		updateModel();
	}
	
	private List<Role> getEnabledRoles(Task task) {
		List<Role> roles = new ArrayList<>();
		if (task.getTrigger()==null) return roles;
		List<Permission> taskpermissions = task.getTrigger().getPermissions();
		if (task instanceof UserTask && ((UserTask)task).getEndConditions()!=null) {
			for	(EndCondition condition : ((UserTask)task).getEndConditions()) {
				if (((ManualEndCondition)condition).getTrigger()!=null) {
					taskpermissions.addAll(((ManualEndCondition)condition).getTrigger().getPermissions());
				}
			}
		}	
 		for (Role role : getSecurityDao().getRoles(getDomain())) {
			for (Permission permission : ((KbeeAbstractRole)role).getPermissions()) {
				if (taskpermissions.contains(permission)) {
					if (!roles.contains(role)) {
						roles.add(role);
					}
				}
			}
		}
 		return roles;
	}
	
	private ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private boolean isDeleteable(Task task) {
		return isRoot() && !getWorkflowDao().hasActivities(task);
	}
	
	private boolean isRoot() {
		return ServiceLocator.getService(SecurityService.class).isRoot();
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
}