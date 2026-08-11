package kbee.web.model;

import org.apache.wicket.model.IModel;

import com.novamens.content.workflow.EndCondition;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.workflow.WorkflowContext;

public class ManualEndConditionModel implements IModel<ManualEndCondition> {
			
	private static final long serialVersionUID = 1L;

	Integer index = null;
	IModel<WorkflowContext> model;
	String label;
	
	public ManualEndConditionModel(String label, IModel<WorkflowContext> model) {

		this.model=model;
		this.label=label;
	}

	public void detach() {
		if (model!=null)
			model.detach();
	}
	
	
	
	@Override
	public ManualEndCondition getObject() {
		
		if (label==null)
			return null;
		
		if (index!=null)
			return (ManualEndCondition) getTask().getEndConditions().get(index.intValue());
		
		int n=0;
		for (EndCondition condition: getTask().getEndConditions()) {
			if (condition instanceof ManualEndCondition) {
				if (condition.getLabel()!=null && condition.getLabel().equals(label)) {
					index=Integer.valueOf(n);
					return  (ManualEndCondition) condition;
				}
			}
			n++;
		}
		
		return null;
	}

	private WebTask getTask() {
		return ((WebTask) getWorkflowModel().getObject().getTask());
	}


	
	private IModel<WorkflowContext> getWorkflowModel() {
		return model;
	}
	

}
