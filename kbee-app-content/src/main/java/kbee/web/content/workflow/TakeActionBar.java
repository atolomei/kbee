package kbee.web.content.workflow;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTakeTaskEvent;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.workflow.WorkflowContext;

@SuppressWarnings("serial")
public class TakeActionBar<T extends Content> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private IModel<WorkflowContext> model;

	public TakeActionBar(String id, IModel<WorkflowContext> model) {
 		super(id, model);
		setOutputMarkupId(true);
		setWorkflowModel(model);
	}
	
	public void setWorkflowModel(IModel<WorkflowContext> model) {
		this.model = model;
	}
	
	public IModel<WorkflowContext> getWorkflowModel() {
		return model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addAction();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	protected Editor<T> getEditor() {
		Assert.isTrue(true, "no editor");
		return null;
	}
	
	private void addAction() {
		AjaxLink<?> sb = new AjaxLink<Void>("take-button") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll(new KbeeTakeTaskEvent(getTask()));
			}
			@Override
			public boolean isEnabled() {
				return isTakeable();
			}
		};
		sb.add(new Label("label", getLabel("take-action-label")));
		addOrReplace(sb);
	}
	
	private WebTask getTask() {
		return ((WebTask)getWorkflowModel().getObject().getTask());
	}
	
	private boolean isTakeable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(getContent());
	}
	
	private Content getContent() {
		return ((KbeeContext)getWorkflowModel().getObject()).getContent();
	}
}