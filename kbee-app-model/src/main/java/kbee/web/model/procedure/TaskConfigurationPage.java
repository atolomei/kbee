package kbee.web.model.procedure;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.util.logging.Logger;
import kbee.web.error.ErrorPanel;
import kbee.web.model.InformationModelDropDownBC;
import kbee.web.model.contentclass.ContentTemplatesDropDownBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

public class TaskConfigurationPage extends ApplicationPage<Task> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TaskConfigurationPage.class.getName());

	static final String CHAR = "_";
	
//	IModel<Procedure> procedureModel;
	
	/**
	 * "/model/task/${procedure}/${task}"
	 * @param parameters
	 */
	public TaskConfigurationPage(PageParameters parameters) {
		
		Procedure procedure = getProcedure(parameters);
		
		if (procedure != null) {
			Task task = getTask(procedure, parameters);
			if (task != null) {
				setModel(new TaskModel(task));
			}
		}	
	}

	public TaskConfigurationPage(IModel<Task> model) {
		super(model);
		//this.procedureModel = proc_model;						
	}
	
//	public void setProcedure(Procedure procedure) {
//		this.procedureModel = new ObjectModel<Procedure>(procedure);
//	}
	
	public ContentProcedure getProcedure() {
		return (ContentProcedure)getModel().getObject().getProcedure();
	}

	public void onInitialize() {
		super.onInitialize();

		if ((getModel() != null) && hasPermissions()) {
			setLogVisit(true);
			PageContentHeaderPanel<?> panel = new PageContentHeaderPanel<>();
			MenuBreadCrumbPanel<?> bc = new MenuBreadCrumbPanel<>();
			bc.addElement(new SettingsDropDownBC());
			bc.addElement(new InformationModelDropDownBC());
			bc.addElement(new ContentTemplatesDropDownBC());
			bc.addElement(new ContentTemplateBC(((ContentProcedure)getProcedure()).getContentTemplate()));
			bc.addElement(new ProcedureBC(getProcedure()));
			bc.addElement(new TaskConfigurationDropDownBC(new ObjectModel<Procedure>(getProcedure()), getModel()));

			panel.setBreadcrumbPanel(bc);
			setTopNavigation(getMainTopbar());
			setMenu(getMainLaternalMenu());

			setPageTitle(new Model<String>(getModel().getObject().getDisplayName()));
			panel.setTitle(getModel().getObject().getDisplayName());
			setSearchPanel(false);
			setAdvancedSearch(false);
			setSuggester(false);
			setPageContentHeader(panel);

			if (getModel().getObject() instanceof WebTask)
				add(new TaskMainPanel(getModel()));
			else
				add(new TaskForkJoinMainPanel(getModel()));

			String task = getModel().getObject().getId().replace(" ", CHAR);

			getPageParameters().set("procedure", getProcedure().getMaster().getId().toString());
			getPageParameters().set("task", task);
		}
		else
			add(new ErrorPanel("editor"));
	}

//	public IModel<Procedure> getProcModel() {
//		return this.procedureModel;
//	}

//	@Override
//	public void onDetach() {
//		super.onDetach();
//		if (this.procedureModel != null)
//			procedureModel.detach();
//		if (templateModel!=null)
//			templateModel.detach();
//	}

	@Override
	protected boolean hasPermissions() {
		final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
		final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class)
				.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		final boolean is_model = ServiceLocator.getService(SecurityService.class)
				.isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
		final boolean is_model_read = ServiceLocator.getService(SecurityService.class)
				.isMember(KbeeGlobalRole.MODEL_READ.getId());
		final boolean has_permission = is_root || is_domain_admin || is_model || is_model_read;
		return has_permission;
	}

	private Task getTask(Procedure procedure, PageParameters parameters) {
		if (procedure != null) {
			StringValue id = parameters.get("task");
			if (!id.isNull() && !id.isEmpty()) {
				String tanm = id.toString().replace(CHAR, " ");
				Task task = procedure.getTask(tanm);
				return task;
//				for (Task task : this.procedureModel.getObject().getTasks()) {
//					if (tanm.equals(task.getId()))
//						return task;
//				}
			}
		}
		return null;
	}

	private Procedure getProcedure(PageParameters parameters) {
		Procedure template = null;
		StringValue id = parameters.get("procedure");
		if (!id.isNull() && !id.isEmpty()) {
			try {
				template = getDomain().getService(WorkflowDomainService.class).getProcedures().stream()
						.filter(p -> p.getId().equals(id.toLong())).findFirst().orElse(null);
				if (template != null && template instanceof KbeeProcedure) {
					if (!((KbeeProcedure) template).getDomain().equals(getDomain())) {
						template = null;
					}
				}
			} 
			catch (Exception e) {
				logger.error(e);
				template = null;
			}
		}
		return template;
	}
}