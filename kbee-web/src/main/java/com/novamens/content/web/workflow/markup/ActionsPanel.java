package com.novamens.content.web.workflow.markup;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.workflow.EndCondition;
import com.novamens.dom.Versionable;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.workflow.WorkflowContext;



/** 
*
* Workflow Resolution 
* Actions depend on the Procedure definition
* 
* We enable actions that are enabled to all the contents in the selection
* 
* 
*
* 
*/
@SuppressWarnings("serial")
public class ActionsPanel<T extends Content> extends ObjectEditorPanel<T> { 

	private static final long serialVersionUID = 1L;
	
	private IModel<WorkflowContext> model;
	private List<IModel<Content>> selection;
	
	public class ConditionsPanel extends Fragment {
		
		public ConditionsPanel() {
			super("step", "conditions-fragment", ActionsPanel.this);
			
			setOutputMarkupId(true); 
			
			ListView<ManualEndCondition> conditionsview = new ListView<ManualEndCondition>("condition", getEndConditions()) {
			
				public void populateItem(ListItem<ManualEndCondition> item) {
					final ManualEndCondition condition = item.getModelObject();
					AjaxSubmitLink button = new AjaxSubmitLink("button", getEditor().getForm()) {
						@Override
						public void onSubmit(AjaxRequestTarget target) {
							getEditor().update(target);
							setStep(getStep(condition), target);
						}
					};
					
					button.setOutputMarkupId(true);
					
					if (item.getIndex()==0)
						item.add(new AttributeModifier("class", "action-container first"));
					else
						item.add(new AttributeModifier("class", "action-container"));
					
					button.add(new Label("label", condition.getLabel()));
					
					item.add(button);
				}
			};
			
			add(conditionsview);
		}
	}

	
	
	/** 
	 * 
	 * 
	 */
	public ActionsPanel(IModel<WorkflowContext> model, List<IModel<Content>> selection) {
		super("task-actions");

		this.selection=selection;
		setOutputMarkupId(true);
		setWorkflowModel(model);
		setStep(new ConditionsPanel());
	}
	
	protected List<IModel<Content>> getSelection() {
		return this.selection;
	}
	
	public void setWorkflowModel(IModel<WorkflowContext> model) {
		this.model = model;
	}
	
	public IModel<WorkflowContext> getWorkflowModel() {
		return model;
	}
	
	protected void onSubmit(AjaxRequestTarget target) {
		
	}
	
	public void onNavigate(AjaxRequestTarget target) {
	}
	
	public void updateModel() {
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
		
		if (this.selection!=null) {
			for (IModel<Content> mo: this.selection)
				mo.detach();
		}
	}
	
	private void setStep(WebMarkupContainer step) {
		addOrReplace(step);
	}
	
	private void setStep(WebMarkupContainer step, AjaxRequestTarget target) {
		replace(step);
		target.add(ActionsPanel.this);
		target.appendJavaScript("window.location.hash = '#"+step.getMarkupId()+"'");
	}
	
	public List<ManualEndCondition> getEndConditions() {
		List<ManualEndCondition> conditions = new ArrayList<ManualEndCondition>();
	
	//	int i = 0;
		for (EndCondition condition : getTask().getEndConditions()) {
			if (condition instanceof ManualEndCondition && isEnabled((ManualEndCondition)condition) && !condition.isInfrequent() && ((ManualEndCondition) condition).isBatch()) {
				conditions.add((ManualEndCondition)condition);
		//		i++;
				//if (i>2) {
				//	break;
				//}
			}
		};
		return conditions;
	}
	

	/**
	 * 
	 * 
	 * @param condition
	 * @return
	 */
	protected boolean isEnabled(ManualEndCondition condition) {
		
		if (!condition.isEnabled())
			return false;
		
		if (condition.getPerms()==null) 
			return true;
		
		String perm = condition.getPerms();
		
		for (IModel<Content> mo: getSelection()) {
			Content content = mo.getObject();
			@SuppressWarnings("unchecked")
			Content clone = (Content)((Versionable<T>)content).clone();
			if ("!write".equals(perm) && isWriteable(clone)) 
				return false;
			if ("write".equals(perm) && !isWriteable(clone)) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean isWriteable(Content content) {
		if (ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId())) {
			return true;
		};
		Acl acl = ServiceLocator.getService(ContentSystemSecurityService.class).getAcl(content);
		return acl.checkPermission(getUser(), KbeePermission.WRITE);
	}
	
	private User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private WebTask getTask() {
		return ((WebTask)getWorkflowModel().getObject().getTask());
	}
	
	
	private WebMarkupContainer getStep(ManualEndCondition condition) {
		
		WebMarkupContainer step = new ConditionEditor<T>("step", getWorkflowModel(), condition) {
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				ActionsPanel.this.onSubmit(target);
			}
			@Override
			public void onCancel(AjaxRequestTarget target) {
				setStep(new ConditionsPanel(), target);
			}
			@Override
			public void onDetach() {
				super.onDetach();
				ActionsPanel.this.onDetach();
			}
		};
		
		return step;
	}
}
