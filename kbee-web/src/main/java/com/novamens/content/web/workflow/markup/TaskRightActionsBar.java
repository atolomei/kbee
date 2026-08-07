package com.novamens.content.web.workflow.markup;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;


@SuppressWarnings("serial")
public class TaskRightActionsBar extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String selected;
	
	public TaskRightActionsBar(String id) {
		super(id);
	}
	
	public String getSelected() {
		return selected;
	}
	
	public void setSelected(String id) {
		selected=id;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		
		if (get("workflow-container")==null) {
			
			WebMarkupContainer wk_container  = new WebMarkupContainer("workflow-container");
			
			wk_container.add(new AttributeModifier("class", new Model<String>() {
				 @Override
				public String getObject() {
					return getSelected().equals(TaskRightActionsBar.this.get("workflow-container:workflow-link").getId()) ? "active" : "";
				}
			}));
			
			add(wk_container);
			
			AjaxLink<Void> wk = new AjaxLink<Void>("workflow-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setSelected(getId());
					onWorkflowClick(target);
				}
			};
			
			wk_container.add(wk);
			
			
			WebMarkupContainer file_container  = new WebMarkupContainer("file-container");
			file_container.add(new AttributeModifier("class", new Model<String>() {
				 @Override
				public String getObject() {
					return getSelected().equals(TaskRightActionsBar.this.get("file-container:file-link").getId()) ? "active" : "";
				}
			}));
			
			add(file_container);
			
			AjaxLink<Void> file = new AjaxLink<Void>("file-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setSelected(getId());
					onFileClick(target);
				}
			};
			
	
			AjaxLink<Void> audit = new AjaxLink<Void>("audit-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setSelected(getId());
					onAuditClick(target);
				}
			};
			
			/*
			audit.add(new AttributeModifier("class", new Model<String>() {
				 @Override
				public String getObject() {											
					return getSelected().equals(TaskRightActionsBar.this.get("audit-link").getId()) ? "active" : "";
				}
			}));
			*/
			
			
			file_container.add(file);
			add(audit);
			
			//add(new Label("title", getTitle()));
		}
		
	}

						
	protected void onAuditClick(AjaxRequestTarget target) {
		
	}
	
	protected void onFileClick(AjaxRequestTarget target) {
		
	}
	
	protected void onWorkflowClick(AjaxRequestTarget target) {
		
	}
}
