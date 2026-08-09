package kbee.web.console.grid;

import com.novamens.workflow.Priority;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class TaskPriorityColumn<T extends Content> extends GridColumn<SearchResult, String> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskPriorityColumn.class.getName());


	public  TaskPriorityColumn(String id, IModel<String> displayModel, String sortProperty) {
		super(id, displayModel, sortProperty);
	}
	
	public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
		if (resultmodel==null || resultmodel.getObject()==null || resultmodel.getObject().getObject()==null) {
			logger.error("resultmodel.getObject().getObject() has a null");
			TaskPriorityPanel pa =new TaskPriorityPanel(componentId, null);
			pa.setError("err");
			cellItem.add(pa);
			return;
		}
		Content object = (Content) resultmodel.getObject().getObject();
		Priority priority = getPriority(object);
		cellItem.add(new TaskPriorityPanel(componentId, priority));
	}


	private Priority getPriority(Content object) {
		WorkflowService workflowService = object.getService(WorkflowService.class);

		Priority priority;
		if (    workflowService==null ||
				workflowService.getContext()==null ||
				workflowService.getContext().getPriority()==null)
			priority=null;
		else
			priority = workflowService.getContext().getPriority();
		return priority;
	}

	@Override
	protected IModel<String> getLabelModel(SearchResult object) {
		Priority priority = getPriority((Content) object.getObject());
		return () -> priority != null ? priority.getLabel( getSessionUser().getLocale()) : "";
	}
	
	
	protected User getSessionUser() {
				return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
}
