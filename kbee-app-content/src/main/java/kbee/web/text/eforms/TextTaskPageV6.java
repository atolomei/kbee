package kbee.web.text.eforms;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.document.IDoc;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.WorkflowContext;

import kbee.web.content.workflow.TaskToolBarPanel;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.workflow.task.TaskPage;
import kbee.web.workflow.util.WorkflowContextModel;

@SuppressWarnings("serial")
@Deprecated
public class TextTaskPageV6 extends TaskPage<OrganizationalText> {
	private static final long serialVersionUID = 1L;
	
	public TextTaskPageV6(PageParameters parameters) {
		
		WorkflowContext context = getWorkflowContext(parameters);
		
		if (context!=null && ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(((KbeeContext)context).getContent())) {
			setContext(context);
			IModel<WorkflowContext> workflowmodel =  new WorkflowContextModel<IDoc>(context);
			
			setTopNavigation(new TaskToolBarPanel<IDoc>(workflowmodel));
			
			setEditionEnabled(getRunningActivity()!=null && getRunningActivity().getUser().getId().equals(getSessionUser().getId()));

			add(new TextTaskPanelV6(workflowmodel) {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isEditionEnabled() {
					return TextTaskPageV6.this.isEditionEnabled();
				}
			});
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
	public TextTaskPageV6(WorkflowContext context, boolean select_preference) {
		super(context);
				
		if (!ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(((KbeeContext)context).getContent())) {
			add( new ErrorPanel("editor", new Model<String>("Not authorized"), new Model<String>("Please ask admin user to grant permissions to this page.")));
			return;
		}
		
		IModel<WorkflowContext> contextmodel = new WorkflowContextModel<IDoc>(context);
		
		setEditionEnabled(getRunningActivity()!=null && getRunningActivity().getUser().getId().equals(getSessionUser().getId()));
		
		add(new TextTaskPanelV6(contextmodel) {
			@Override
			public boolean isEditionEnabled() {
				return TextTaskPageV6.this.isEditionEnabled();
			}
			@Override
			public void setEditionEnabled(boolean value) {
				TextTaskPageV6.this.setEditionEnabled(value);
			}
			@Override
			public boolean isReadOnly() {
				return TextTaskPageV6.this.isReadOnly();
			}
		});
		
		setTopNavigation(new TaskToolBarPanel<IDoc>(contextmodel));
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}
}