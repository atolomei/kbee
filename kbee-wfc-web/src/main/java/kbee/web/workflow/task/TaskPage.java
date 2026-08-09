package kbee.web.workflow.task;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.wicket.markup.html.event.WicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.event.EventHandler;
import com.novamens.kbee.wicket.markup.html.event.EventListenerWicket;

import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;

import kbee.web.nav.NavigablePage;
import kbee.web.nav.Navigator;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ConsoleObjectPage;
import kbee.web.workflow.util.WorkflowContextModel;

public class TaskPage<T extends Content> extends ConsoleObjectPage<T> implements EventHandler, NavigablePage<Content>, com.novamens.kbee.content.workflow.TaskPage<T> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskPage.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private boolean editionEnabled = false;
	private boolean readonly = false;
	private Navigator<Content> navigator;
	private IModel<WorkflowContext> workflowmodel;
	
	public TaskPage() {
	}
	
	public TaskPage(WorkflowContext context) {
		setContext(context);
		setPageTitle(new Model<String>( parseTitle( ((KbeeContext)context).getContent().getDisplayName())));
		String task_name = context.getTask().getId().replaceAll("\\s", "-").toLowerCase();
		getPageParameters().set("task", task_name);
		getPageParameters().set("content", ((KbeeContext)context).getContent().getId());
	}
	
	
	public TaskPage(IModel<WorkflowContext> model) {
		WorkflowContext context = model.getObject();
		setWorkflowModel(model);
		setPageTitle(new Model<String>(parseTitle( ((KbeeContext)context).getContent().getDisplayName())));
		String task_name = context.getTask().getId().replaceAll("\\s", "-").toLowerCase();
		getPageParameters().set("task", task_name);
		getPageParameters().set("content", ((KbeeContext)context).getContent().getId());
	}
	
	
	@Override
	public void handle(final WicketAjaxEvent event) {
		visitChildren(new IVisitor<Component, Void>() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void component(Component component, IVisit<Void> visit) {
 				List<EventListenerWicket> listeners = component.getBehaviors(EventListenerWicket.class);
				for (EventListenerWicket listener : listeners) {
					if (listener.handle(event))
						listener.onEvent(event);
				}
			}
		});
		visitChildren(new IVisitor<Component, Void>() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void component(Component component, IVisit<Void> visit) {
 				List<WicketEventListener> listeners = component.getBehaviors(WicketEventListener.class);
				for (WicketEventListener listener : listeners) {
					if (listener.handle(event))
						listener.onEvent(event);
				}
			}
		});
	}
	
	public void setWorkflowModel(IModel<WorkflowContext> model) {
		this.workflowmodel = model;
	}
	
	public IModel<WorkflowContext> getWorkflowModel() {
		return this.workflowmodel;
	}
	
	public void setEditionEnabled(boolean value) {
		this.editionEnabled = value;
	}
	
	public boolean isEditionEnabled() {
		return editionEnabled;
	}
	
	public void setReadOnly(boolean  value) {
		this.readonly = value;
	}
						
	public boolean isReadOnly() {
		return this.readonly;
	}
	
	public Activity getRunningActivity() {
		List<Activity> activities = getWorkflowModel().getObject().getProcess().getActivities();
		if (activities==null)
			return null;
		
		Activity activity = !activities.isEmpty() && activities.get(0).isRunning() ? activities.get(0) : null;
		return activity;
	}
	
	public Navigator<Content> getNavigator() {
		return this.navigator;
	}
	
	
	public void setNavigator(Navigator<Content> navigator) {
		this.navigator = navigator;
		//if (getTopNavigation() instanceof NavigablePage<?>) {
		//	((NavigablePage<Content>)getTopNavigation()).setNavigator(navigator);
		//}
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}
	
	@Override
	public void onDetach() {
		if (this.workflowmodel!=null)
			this.workflowmodel.detach();
		if (this.navigator!=null)
			navigator.detach();
		super.onDetach();
	}
	
	@SuppressWarnings("unchecked")
	protected void setContext(WorkflowContext context) {
		setWorkflowModel(new WorkflowContextModel<T>(context));
		setModel(((WorkflowContextModel<T>)getWorkflowModel()).getModel());
	}
	
	@SuppressWarnings("unchecked")
	protected WorkflowContext getWorkflowContext(PageParameters parameters) {
		
		try {
			WorkflowContext context = null;
			T content = null;
			Class<T> contentclass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
			StringValue id = parameters.get("content");
			StringValue task = parameters.get("task");
			if (!id.isNull() && !id.isEmpty() && !task.isNull() && !task.isEmpty()) {
				content = (T)getContentDao().findContentById(contentclass, Long.valueOf(id.toString()));
				if (content!=null) {
					WorkflowService workflowService = content.getService(WorkflowService.class);
					
					if (workflowService!=null && workflowService.getTask()!=null) {
						String task_id = workflowService.getTask().getId().replaceAll("\\s", "-").toLowerCase();
						if (task_id.equals(task.toString()))
							context = workflowService.getContext();
					}
				}
			}
			return context;
		} 
		catch (Exception e) { 
			logger.error(e);
			return null;
		}
	}
	
	@Override
	protected String getPageType() {
		return "task";
	}
	
	@Override
	protected boolean hasLateralMenu() {
		return true;
	}

	public void onBeforeRender() {
		super.onBeforeRender();
		this.add(new AttributeModifier("class", "kbee"));
	}
	
	private String parseTitle( String title ) {
		if (title==null)
			return "";
		if (title.length()>30)
			return title.substring(0,29)+"...";
		return title;
	}


}