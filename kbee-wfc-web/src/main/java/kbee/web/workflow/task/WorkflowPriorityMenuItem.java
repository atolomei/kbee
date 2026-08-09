package kbee.web.workflow.task;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.workflow.Priority;
import com.novamens.workflow.WorkflowContext;

public class WorkflowPriorityMenuItem<T extends Content> extends AjaxMenuItemPanelV5<T> {
			
	private static final long serialVersionUID = 1L;

	
	private IModel<Priority> model;
	private IModel<T> content_model;
	private IModel<WorkflowContext> cmodel;
	
	private long time = 0;
	private AjaxLink<?> link;
	
	public WorkflowPriorityMenuItem(String id, IModel<T> content_model, IModel<Priority> model, IModel<WorkflowContext> cmodel) {
		super(id);
		setOutputMarkupId(true);
		this.model = model;
		this.content_model = content_model;
		this.cmodel=cmodel;
	}
	
	@Override
	public String getCssClass() {
		if (isIconVisible())
			return "label-selected";
		else
			return "label-no-selected";
	}

	
	@Override
	public String getIconCssClass() {
		return isIconVisible() ? (CHECK +  " toright fa-fw") : "";
	}
	
	public void onClick(AjaxRequestTarget target) {
		
		long now = System.currentTimeMillis();

		if (now-time<800) 
			return;
		
		time = now;
		this.content_model.getObject().getService(WorkflowService.class).setPriority(this.model.getObject());
		target.add(link);
		onUpdate(target);
	}
	
	
	@Override
	public String getLabel() {
		return model.getObject().getLabel(getSessionUser().getLocale());
	}
	
	private User getSessionUser() {
		return (User) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	@Override
	public void onDetach() {
		this.model.detach();
		this.cmodel.detach();
		if (this.content_model!=null)
			this.content_model.detach();
		
		super.onDetach();
	}

	
	protected boolean isIconVisible() {
		Priority p=cmodel.getObject().getPriority();
		return p.getId()==model.getObject().getId();
	}
	
	@Override
	protected AbstractLink getNewLink(String id) {
		link = new AjaxLink<Void>(id) {
			private static final long serialVersionUID = 1L;
				public void onClick(AjaxRequestTarget target) {
				try {
					WorkflowPriorityMenuItem.this.onClick(target);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		return link;
	}

	public void onUpdate(AjaxRequestTarget target) {
	}
}

