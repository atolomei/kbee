package com.novamens.content.web.workflow.markup;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.WorkflowContext;

@Deprecated
@SuppressWarnings("serial")
public class ProcessInfoPanel extends ModelPanel<WorkflowContext>  {
	private static final long serialVersionUID = 1L;
	
	public ProcessInfoPanel(IModel<WorkflowContext> workflowmodel) {
		super("process-info", workflowmodel);
		
		setOutputMarkupId(true);
		
		add(new Label("procedure.name", new Model<String>() {
			public String getObject() {
				return getModelObject().getProcedure().getName();
			}
		}));
		
		Label startedlabel = new Label("process.started", new Model<String>() {
			public String getObject() {
				return ServiceLocator.getService(DateTimeService.class).timeElapsed(getProcess().getStartTime());
						 // zd, ZoneId.of(zid), session.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, css);
				// return	KbeeDateTimeFomatter.format(getProcess().getStartTime());
				
			}
		});
		
		startedlabel.setEscapeModelStrings(false);
		
		add(startedlabel);
		
		/*
		add(new AjaxLink<Void>("cancel-button") {
			public void onClick(AjaxRequestTarget target) {
				((Dialog)ProcessInfoPanel.this.get("cancel-dialog")).open(target, new Dialog.Handler() {
					@Override
					public void onClick(AjaxRequestTarget target, Button button) {
						if (button.key().equals("dialog.cancel.button")) {
							getContent().getService(WorkflowService.class).cancel();
							fire(new NavigationEvent());
						}
					}
				}, getProcedure().getName(), getContentTitle());
			}
			@Override
			public boolean isVisible() {
				return  ((WebTask)getContext().getTask()).enableCancel() && !isSupportUser() || isAdminUser();
			}	
		});
		
		add(new Dialog("cancel-dialog", "dialog.cancel.title", "dialog.cancel.message", Dialog.Cancel, new Dialog.Button("dialog.cancel.button", "btn btn-sm btn-danger")));
		*/
		
	}
	
	public com.novamens.workflow.Process getProcess() {
		return getModelObject().getProcess();
	}
	
	public Procedure getProcedure() {
		return getModelObject().getProcedure();
	}
	
	public Content getContent() {
		return ((KbeeContext)getModelObject()).getContent();
	}
	
	public WorkflowContext getContext() {
		return getModelObject();
	}
	
	public String getContentTitle() {
		if (getContent().getTitle()!=null)
			return  getContent().getTitle();
		
		if (getContent().getContentTemplate()!=null)
			return getContent().getContentTemplate().getName();
		
		if (getContent().getOId()!=null)
			return getContent().getOId().toString();
		
		return new StringResourceModel("notitle", this, null).getString();
	}
}
