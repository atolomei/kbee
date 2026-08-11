package kbee.web.content.workflow;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.WorkflowContext;

import kbee.web.content.panel.FileMetaInfoPanel;
import kbee.web.content.panel.TaskInformationModelAccessPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.workflow.ProcessChartPanel;
import kbee.web.workflow.TaskCommentPanel;
import kbee.web.workflow.TaskInfoPanel;

public class TaskHomePanel<T extends Content> extends ModelPanel<WorkflowContext>  {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskHomePanel.class.getName());
	
	protected final boolean root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	
	boolean is_file_info = true;
	boolean is_profiles = true;
	boolean is_comment = false;
	boolean is_information_model = root|| role_admin;
	
	
	public TaskHomePanel(IModel<WorkflowContext> workflowmodel) {
		this("task-info", workflowmodel);
	}
	
	public TaskHomePanel(String id, IModel<WorkflowContext> workflowmodel) {
		this(id, workflowmodel, true);
	}
	
	public TaskHomePanel(String id, IModel<WorkflowContext> workflowmodel, boolean is_actions_visible) {
		super(id, workflowmodel);
	}
	
	
	public void setFileInfo(boolean is_file_info) {
		this.is_file_info=is_file_info;
	}
					
	public void setProfiles(boolean is_Profiles) {
		this.is_profiles=is_Profiles;
	}
					
	public void setComment(boolean is_Comment) {
		this.is_comment=is_Comment;
	}
				
	public void setInformationModel(boolean b) {
		this.is_information_model=b;
	}

	
	
	public void onInitialize() {
		super.onInitialize();
		
		ProcessChartPanel p=new ProcessChartPanel("process-chart", getModel());
		p.setHide(false);
		add(p);
		
		add(new TaskInfoPanel<T>("task-info", getModel()));
		
		if (is_comment) {
			add(new TaskCommentPanel("task-comment", getModel()));
		}
		else
			add(new InvisiblePanel("task-comment"));
		
		if (is_profiles) {
			if (getModelObject().getTask()!=null && getModelObject().getRoles()!=null && !getModelObject().getRoles().isEmpty()) {
				add(new RolesPanel("profiles", getModel()));
			}
			else
				add(new InvisiblePanel("profiles"));
			}
		else
			add(new InvisiblePanel("profiles"));
		
		
		IModel<T> mo=new ObjectModel<T> ( (T)((KbeeContext)getModel().getObject()).getContent());
		
		if (is_file_info) {
			try {
				add(new FileMetaInfoPanel<T>("file-metadata-info", mo));
			} catch (Exception e) {
				logger.error(e);
				add(new ErrorPanel("file-metadata-info", e));
			}
		} else {
			add(new InvisiblePanel("file-metadata-info"));	
		}
		
		
		if (is_information_model) {
			add(new TaskInformationModelAccessPanel<T>("information-model", mo, getModel()));
			
		} else {
			add(new InvisiblePanel("information-model"));	
		}
		
		
	}
}