package kbee.web.idoc;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.document.IDoc;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.content.web.nav.markup.TaskNavigationBar;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.WorkflowContext;

import kbee.web.page.ApplicationMenuSection;
import kbee.web.workflow.task.TaskPage;
import kbee.web.workflow.util.WorkflowContextModel;

@Deprecated
public class IDocTaskPageV5 extends TaskPage<IDoc> {
	private static final long serialVersionUID = 1L;
	
	public IDocTaskPageV5(PageParameters parameters) {
		
		WorkflowContext context = getWorkflowContext(parameters);
		
		if (context!=null && ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(((KbeeContext)context).getContent())) {
			setContext(context);
			IModel<WorkflowContext> workflowmodel =  new WorkflowContextModel<IDoc>(context);
			setTopNavigation(new TaskNavigationBar<IDoc>(workflowmodel));
			setEditionEnabled(getRunningActivity()!=null && getRunningActivity().getUser().getId().equals(getSessionUser().getId()));
			
			
//			add(new IDocTaskPanel(workflowmodel) {
//				@Override
//				public boolean isEditionEnabled() {
//					return IDocTaskPageV5.this.isEditionEnabled();
//				}
//			});
		}
		else {
			setTopNavigation(new GlobalNavigationBar<IDoc>("navigation"));
			add( new ErrorPanel("editor", new Model<String>("Not authorized"), new Model<String>("Error or not authorized. Please ask admin user to grant permissions to this page.")));
		}
	}
	
	/** 
	 * @param context
	 * @param select_preference
	 */
	public IDocTaskPageV5(WorkflowContext context, boolean select_preference) {
		super(context);
				
		if (!ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(((KbeeContext)context).getContent())) {
			add( new ErrorPanel("editor", new Model<String>("Not authorized"), new Model<String>("Please ask admin user to grant permissions to this page.")));
			return;
		}
		
		IModel<WorkflowContext> contextmodel = new WorkflowContextModel<IDoc>(context);
		
//		add(new IDocTaskPanel(contextmodel, select_preference) {
//			
//			@Override
//			public boolean isEditionEnabled() {
//				return IDocTaskPageV5.this.isEditionEnabled();
//			}
//			@Override
//			public void setEditionEnabled(boolean value) {
//				IDocTaskPageV5.this.setEditionEnabled(value);
//			}
//			@Override
//			public boolean isReadOnly() {
//				return IDocTaskPageV5.this.isReadOnly();
//			}
//		});
		
		setTopNavigation(new TaskNavigationBar<IDoc>(contextmodel));		
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}
	
	@Override
	public void onDetach() {
		if (get("editor")!=null)
			get("editor").detach();
		super.onDetach();
	}

 
}
