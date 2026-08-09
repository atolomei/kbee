package kbee.web.content.panel;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EForm;
import com.novamens.content.model.ModelSection;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.model.ModelPanel;

import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.web.command.panel.CommandAttributePanelV5;
import kbee.web.error.ErrorPanel;
import kbee.web.panel.AlertPanel;

public class TaskInformationModelAccessPanel<T extends Content> extends ModelPanel<T> {
				
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskInformationModelAccessPanel.class.getName());
	
	private IModel<WorkflowContext> w_context;
	
	public TaskInformationModelAccessPanel(String id, IModel<T> model, IModel<WorkflowContext> w_context) {
		super(id, model);
		this.w_context=w_context;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addItems();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.w_context!=null)
			this.w_context.detach();
	}
	
	public IModel<WorkflowContext> getWorkflowContext() {
		return w_context;
	}

	public void settWorkflowContext(IModel<WorkflowContext> w_context) {
		this.w_context = w_context;
	}


	protected WebTask getTask() {
	return ((WebTask) getWorkflowContext().getObject().getTask());
	}
	
	protected ProcessLauncher getProcessLauncher() {
		for(ProcessLauncher launcher: 
			getModel().getObject().getDomain().getService(WorkflowDomainService.class).getContextLaunchers(getModel().getObject().getContentTemplate())) {
			if (launcher.getProcedure().equals( getWorkflowContext().getObject().getProcedure())) {
				return launcher;
			}
		}
		return null;
	}
	
	protected Task getPreviousTask() {
		return ((KbeeContext)getWorkflowContext().getObject()).getPreviousTask();	
	}

protected List<EForm> getForms() {
	List<EForm> forms = new ArrayList<EForm>();
	for (EForm form : getTask().getForms()) {
		forms.add(form);
	}
	if (getTask().getIncludeCallerForms() && getPreviousTask()!=null) {
		for (EForm form : ((WebTask)getPreviousTask()).getForms()) {
			forms.add(form);
		}
	}
	if (forms.isEmpty()) {
		forms.addAll(getDefaultForms());
	}
	return forms;
 }


protected List<EForm> getDefaultForms() {
	List<EForm> forms = new ArrayList<EForm>();
	return forms;
}

protected ContentDao getContentDao() {
	return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
}


private void addItems() {
	
	List<KeyValue<String>> list = new ArrayList<KeyValue<String>>();
	

	AlertPanel<Void> pa= new AlertPanel<Void>(
			"modelsetup",
			AlertPanel.INFO,
			null,
			null,
			new StringResourceModel("modelsetup", TaskInformationModelAccessPanel.this, null) );

	
	pa.setIcon(AlertPanel.HELP_INFO);
	add(pa);

	try {
	String p_ct =  getServerUrl()+"/"+"model/contentclass/"+getModel().getObject().getContentTemplate().getId().toString();
	
	String proc_id = getWorkflowContext().getObject().getProcedure().getId().toString();
	
	
	ProcessLauncher pl=getProcessLauncher();
	
	String launcher_id = pl!=null ? pl.getId().toString() : "";
	
	String p_pr =  getServerUrl()+"/"+"model/procedure/"+proc_id+"/"+launcher_id;
	String p_ta =  getServerUrl()+"/"+"model/task/"+proc_id+"/"+launcher_id+"/"+getWorkflowContext().getObject().getTask().getId();
		
	list.add( new KeyValue<String>(getLabel("content-template").getObject(), getModel().getObject().getContentTemplate().getDisplayName(), p_ct ));
	list.add( new KeyValue<String>(getLabel("procedure").getObject(),  getWorkflowContext().getObject().getProcedure().getDisplayName(), p_pr ));
	list.add( new KeyValue<String>(getLabel("task").getObject(), getWorkflowContext().getObject().getTask().getDisplayName(), p_ta));
	
	int n=1;
	for (EForm e: getForms()) {
		if ( e instanceof com.novamens.content.form.EIdentifiableForm) {
			String p_e = getServerUrl()+"/"+"eform/" +  getModel().getObject().getContentTemplate().getId().toString() +"/" + ((com.novamens.content.form.EIdentifiableForm) e).getId().toString();
			list.add( new KeyValue<String>(getLabel("eform").getObject()+ " " + String.valueOf(n++), e.getDisplayName(), p_e ));
		}
	}
	
	
	list.sort(new Comparator<KeyValue<String>>() {
		@Override
		public int compare(KeyValue<String> o1, KeyValue<String> o2) {
			return o1.getKey().toString().compareToIgnoreCase(o2.getKey().toString());
		}
	});
	
	
	List<Panel> panels = new ArrayList<Panel>();
	
	for ( KeyValue<String> kv:list) { 
		CommandAttributePanelV5 cp=new CommandAttributePanelV5("item", new Model<String>(kv.getKey().toString()), new Model<String>(kv.getValue()));
		cp.setLink(kv.getLink());
		panels.add(cp);
	}
	
	 add(new ListView<Panel>("content", panels) {
            private static final long serialVersionUID = 1L;
            protected void populateItem(ListItem<Panel> item) {
                item.setOutputMarkupId(true);
                item.add(item.getModelObject());
                item.setVisible(item.getModelObject().isVisible());
            }
        });
	} catch (Exception e) {
		logger.error(e);
		add(new ErrorPanel("content", e)); 
		
	}
}















}
