package com.novamens.content.web.workflow.markup;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.text.template.ContentTextTemplate;
import com.novamens.content.web.text.template.markup.TemplateEditorMainPanel;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.text.template.TemplateData;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.WorkflowContext;

import kbee.web.nav.TabNavigationBar;
import kbee.web.page.BootstrapApplicationPage;

@SuppressWarnings("serial")
public class ResolutionEditorPage<T extends Content> extends BootstrapApplicationPage<WorkflowContext> {
	private static final long serialVersionUID = 1L;
	
	public ResolutionEditorPage(IModel<WorkflowContext> model, ContentTextTemplate template) {
		super(model, new TabNavigationBar<WorkflowContext>("navigation", new Model<String>(template.getTitle())));
		
		setPageTitle(new Model<String>(template.getTitle()));
		
		IModel<T> contentmodel = new ObjectModel<T>(getContent());
		
		IModel<TemplateData> datamodel = new Model<TemplateData>(getTemplateData(getWorkflowContext()));
		
		add(new TemplateEditorMainPanel<T>(contentmodel, datamodel, template) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				Map<String, String> values = getData().getValues();
				values.put("template", getTemplate().getContentId());
				getWorkflowService().setParameters(values);
				getWorkflowService().setResolution(getTemplateText(), getTemplateTitle());
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	protected T getContent() {
		return (T)((KbeeContext)getWorkflowContext()).getContent();
	}
	
	protected WorkflowContext getWorkflowContext() {
		return getModelObject();
	}
	
	protected WorkflowService getWorkflowService() {
		return getContent().getService(WorkflowService.class);
	}
	
	protected TemplateData getTemplateData(WorkflowContext context) {
		TemplateData data = new TemplateData();
		data.setValues(((KbeeContext)context).getParameters());
		return data;
	}
	
	protected void updateContext(TemplateData data) {
		((KbeeContext)getModelObject()).setParameters(data.getValues());
	}
}

 
