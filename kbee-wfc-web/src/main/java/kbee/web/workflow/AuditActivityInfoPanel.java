package kbee.web.workflow;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Task;

import kbee.web.user.UserAvatarPanel;

public class AuditActivityInfoPanel extends ModelPanel<Activity> {
			
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AuditActivityInfoPanel.class.getName());

	public AuditActivityInfoPanel(String id, IModel<Activity> model) {
		super(id, model);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		
		add(new UserAvatarPanel("photo", new ObjectModel<User>(getModel().getObject().getUser())));
		
		Person person = getContentDao().findUserProfileByUser(getModel().getObject().getUser()).getPerson();					

		add(new Label("user", person!=null?person.getFirstLastName():""));
		
		Label bti=new Label("btitle", person!=null?person.getBusinessTitle():"");
		bti.setVisible((person!=null) && (person.getBusinessTitle()!=null));
		add(bti);
		String task_action = "";
		
		Task task = getTask(getModel().getObject());
		
		if (getModel().getObject().getEvent()!=null) {
			String label = ((com.novamens.kbee.content.workflow.KbeeProcedure) getModel().getObject().getProcess().getProcedure()).getLabel(getModel().getObject().getEvent());
			task_action = label;
		}
		
		
		String task_proc_name;
		try {
			task_proc_name=getModel().getObject().getProcess().getProcedure().getName();
		
		} catch (Exception e) {
			logger.error(e);
			task_proc_name = e.getClass().getSimpleName();
		}
		
		Label l_task_proc       =   new Label("task-procedure", task_proc_name);
		Label l_task_taskname   =   new Label("task-taskname", task.getName());
		Label l_task_action     =    new Label("task-action", task_action);
		Label l_task_start      =    new Label("task-start", ServiceLocator.getService(DateTimeService.class).format(getModel().getObject().getStartTime(), getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_Year_hh_mm_z));
		Label l_task_end     	=    new Label("task-end", ServiceLocator.getService(DateTimeService.class).format(getModel().getObject().getEndTime(), getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_Year_hh_mm_z));

		
		add(l_task_proc);
		add(l_task_taskname);
		add(l_task_action);
		add(l_task_start);
		add(l_task_end);
		
		
		if (getModel().getObject() instanceof KbeeWorkflowActivity) {  
			Content content = ((KbeeWorkflowActivity) getModel().getObject()).getContent();
			Label l_file_name       =   new Label("file-name", 		content.getTitle());
			Label l_file_template   =   new Label("file-template",	content.getContentTemplate().getDisplayName());
			Label l_file_version  	=   new Label("file-version", 	String.valueOf(content.getVersion()));
			Label l_file_id 	    =   new Label("file-id", 		content.getIdInfo());
			add(l_file_name);
			add(l_file_version);
			add(l_file_id);
			add(l_file_template);
		}
		else {
			add(new Label("file-name","[no content]"));
			add(new Label("file-template",""));
			add(new Label("file-version",""));
			add(new Label("file-id",""));
		}
		
	}
	
	@Override	
	 public void onDetach() {
		 super.onDetach();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected Task getTask(Activity activity) {
		for (Task task : activity.getProcess().getProcedure().getTasks()) {
			if (task.getId().equals(((KbeeWorkflowActivity)activity).getTaskName()))
				return task;
		}
		return null;
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
}
