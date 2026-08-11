package kbee.web.content.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.model.ModelSection;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.util.KeyValue;
import com.novamens.workflow.WorkflowContext;

@SuppressWarnings({ "serial", "deprecation" })
public class TaskFormsPanel<T extends Content> extends ModelPanel<WorkflowContext>  {
	private static final long serialVersionUID = 1L;
	
	public TaskFormsPanel(IModel<WorkflowContext> workflowmodel) {
		this("task-forms", workflowmodel);
	}
	
	public TaskFormsPanel(String id, IModel<WorkflowContext> workflowmodel) {
		super(id, workflowmodel);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		add(new ListView<KeyValue<String>>("eform", getForms()) {
			public void populateItem(ListItem<KeyValue<String>> item) {
				AjaxLink<Void> link = new AjaxLink<Void>("eform-link") {
					public void onClick(AjaxRequestTarget target) {
						fireScanAll(new EOpenFormEvent(target, (String)item.getModelObject().getKey()));
					}
				};
				link.add(new Label("eform-name", item.getModelObject().getValue()));
				item.add(link);
			}
		});
		
	}
	
	private List<KeyValue<String>> getForms() {
		List<KeyValue<String>> names = new ArrayList<KeyValue<String>>();
		if (!getTask().getForms().isEmpty()) {
			for (EForm eform : getTask().getForms()) {
				names.add(new KeyValue<String>(eform.getName(), eform.getDisplayName()));
			}
		}
		else {
			//names = getDefaultForms();
		}
		return names;
	}
	
	protected WebTask getTask() {
		return ((WebTask) getModel().getObject().getTask());
	}
	
//	private List<KeyValue<String>> getDefaultForms() {
//		List<KeyValue<String>> forms = new ArrayList<KeyValue<String>>();
//		List<ModelSection> sections;
//		WorkflowContext context = getModel().getObject();
//		if (context.getTask()!=null && 
//				context.getTask() instanceof WebTask &&
//				!((WebTask)context.getTask()).getSections().isEmpty()) {
//			sections = ((WebTask)context.getTask()).getSections();
//		}
//		else {
//			//sections = ((KbeeContext)context).getContent().getContentTemplate().getSections();
//		}	
//		for (ModelSection section : sections) {
//			forms.add(new KeyValue<String>(section.getName(), section.getName()));
//		}
//		return forms;
//	}
}
