package kbee.web.datamanagement;

import org.apache.wicket.model.StringResourceModel;

public abstract class TagManagementCancelWorkflowAction extends TagManagementAction {

	private static final long serialVersionUID = 1L;

	public TagManagementCancelWorkflowAction(String id) {
        super(id);
    }

    @Override
    public String getActionName() {
        return new StringResourceModel("cancelWorkflow",this, null).getObject();
    }

}
