package com.novamens.content.web.workflow.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.user.UserService;
import com.novamens.content.web.content.markup.ContentSelectionPanel;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.dao.KbeeContentDao;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeWorkflowEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.WorkflowContext;

import kbee.web.content.editor.ContentEditor;
import kbee.web.util.Property;
import kbee.web.workflow.util.WorkflowContextModel;

/** 
 * My Tasks -> Batch Actions Workflow
 */

@SuppressWarnings("serial")
public class WorkflowBatchActionsPanel extends ContentEditor<Content> {
			
	private static final long serialVersionUID = 1L;
	
	private boolean done = false;
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WorkflowBatchActionsPanel.class.getName());

	
	public WorkflowBatchActionsPanel (String id, List<IModel<Content>> selection) {
		super(id);
		
		setOutputMarkupId(true);
		
		add(new ContentSelectionPanel(selection) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(WorkflowBatchActionsPanel.this);
			}
			@Override
			protected Page getPage(IModel<Content> model) {
				return WorkflowBatchActionsPanel.this.getPage(model);
			}
			
			@Override
			protected List<Property<Content>> getProperties() {
				return WorkflowBatchActionsPanel.this.getSelectionProperties();
			}
		});
		
		Model<String> feedbackmodel = new Model<String>() {
			public String getObject() {
				if (!done && !isSameTask()) {
					return getLabel("notsametask-message").getObject();
				}
				else {
					if (done)
						if (!WorkflowBatchActionsPanel.this.hasErrors())
							return getLabel("ok-message").getObject();
						else
							return getLabel("errors-message").getObject();
				}
				return null;
			}
		};
		
		WebMarkupContainer fpanel = new WebMarkupContainer("feedback-panel") {
			public boolean isVisible() {
				return !isSameTask() || done;
			}
		};
		
		add(fpanel);
		
		fpanel.add(new Label("feedback", feedbackmodel) {
			public boolean isVisible() {
				return !isSameTask() || done;
			}
		});
		((Label)get("feedback-panel:feedback")).setEscapeModelStrings(false);
		
		WebMarkupContainer actions = new WebMarkupContainer("actions") {
			@Override
			public boolean isVisible() {
				return isSameTask() && !done;
			}
		};
		
		Form<Content> form = new Form<Content>("form", selection.get(0), Disposition.VERTICAL);
				
		form.add(new ActionsPanel<Content>(getWorkflowModel(), getSelection()) {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				done = true;
				target.add(WorkflowBatchActionsPanel.this);
				if (!hasErrors())
					onReturn();
			}
		});
		
		actions.add(form);
		
		add(actions);
		
		add(new WicketEventListener<KbeeWorkflowEvent>() {
			public void onEvent(KbeeWorkflowEvent event) {
				WorkflowContext selectioncontext = ((KbeeContext)getWorkflowModel().getObject()).clone();;
				for (IModel<Content> model : getSelection()) {
					WorkflowBatchActionsPanel.this.handle(event, model, selectioncontext);
				}
			}
		});
	}
	
	@Override
	public boolean isNew() {
		return false;
	}

	@Override
	public void setIsNew(boolean isnew) {
	}
	
	public void onReturn() {
	}

	@Override
	public Form<?> getForm() {
		return (Form<?>)get("form");
	}
	
	
	public IModel<String> getLabel(String key) {
		return new StringResourceModel(key, WorkflowBatchActionsPanel.this, null);
	}
	
	protected Page getPage(IModel<Content> model) {
		return null;
	}


	
	protected void handle(KbeeWorkflowEvent event, IModel<Content> model, WorkflowContext selectioncontext) {
		String validation = validate(model.getObject());
		((ContentSelectionPanel)get("selection")).setStatus(model.getObject(), validation);
		if ("".equals(validation)) {
			WorkflowService workflowService =  model.getObject().getService(WorkflowService.class);
			KbeeContext contentcontext = (KbeeContext)workflowService.getContext();
			contentcontext.setCollaborator(((KbeeContext)selectioncontext).getCollaborator());
			contentcontext.setNote(((KbeeContext)selectioncontext).getNote());
			workflowService.handle(event, contentcontext);
		}
	}
	
	
	protected IModel<WorkflowContext> getWorkflowModel() {
		if (getSelection().isEmpty())
			return null;
		
		Content content = getSelection().get(0).getObject();
		WorkflowService workflowService = content.getService(WorkflowService.class);
		WorkflowContext context = workflowService.getContext();
		IModel<WorkflowContext> model = new WorkflowContextModel<Content>(context);
		
		return model;
	}
	
	
	protected boolean isSameTask() {
		String procedureId = null, taskId = null;
		for (IModel<Content> model : getSelection()) {
			Content content = model.getObject();
			 
			 
			WorkflowService workflowService = content.getService(WorkflowService.class);
			KbeeTask task = workflowService==null || workflowService.getTask()==null ? null : (KbeeTask)workflowService.getTask();
			
			if (task==null) {
				return false;
			}
			
			Procedure procedure = task.getProcedure();
			
			if (procedureId!=null && !String.valueOf(procedure.getId()).equals(procedureId)) {
				return false;
			}
			
			procedureId = String.valueOf(procedure.getId());
			
			if (task==null || (taskId!=null && !String.valueOf(task.getId()).equals(taskId))) {
				return false;
			}
			
			taskId = String.valueOf(task.getId());
		}
		if (procedureId==null)
			return false;
		return true;
	}
	
	
	
	protected String validate(Content content) {
		String message = "";
		for (ClassifierTemplate template : content.getContentTemplate().getClassifiers()) {
			if (template.isMandatory()) {
				if (!classified(content, template.getClassifier())) {
					if (!message.equals("")) message += ", ";
					message += template.getClassifier().getName() + " is required";
				}
			}
		}
		for (AttributeTemplate template : content.getContentTemplate().getAttributes()) {
			if (template.getAttribute().isRequired()) {
				if (content.getAttributeValues(template.getAttribute()).isEmpty()) {
					if (!message.equals("")) message += ", ";
					message += template.getAttribute().getName() + " is required";
				}
			}
		}
		return message;
	}

	
	protected boolean classified(Content content, Classifier classifier) {
		for (Classification classification : content.getClassification()) {
			if (classification.getClassifier().equals(classifier))
				return true;
		}
		return false;
	}
	
	
	protected boolean hasErrors() {
		return ((ContentSelectionPanel)get("selection")).hasErrors();
	}
	
	
	protected List<IModel<Content>> getSelection() {
		return ((ContentSelectionPanel)get("selection")).getSelection();
	}

		
	
	
	protected List<Property<Content>> getSelectionProperties() {
		
		List<Property<Content>> properties = new ArrayList<Property<Content>>();
		
		properties.add(new Property<Content>() {
			public IModel<String> getLabel() {
				return new StringResourceModel("grid.title", WorkflowBatchActionsPanel.this, null);
			}
			public IModel<String> getValue(IModel<Content> model) {
				return new PropertyModel<String>(model, "title");
			}
			public String getCss() {
				return "col-lg-4";
			}
			public boolean isLink() {
				return true;
			}
		});
		

		if (getDomain().getDomainType()!=DomainType.EXPRESS) {
			properties.add(new Property<Content>() {
				public IModel<String> getLabel() {
					return new StringResourceModel("grid.task", WorkflowBatchActionsPanel.this, null);
				}
				public IModel<String> getValue(IModel<Content> model) {
					WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
					String taskname = workflowService==null || workflowService.getTask()==null ? "" : workflowService.getTask().getName();
					String procedurename;
					if (workflowService!=null && workflowService.getTask()!=null &&
							workflowService.getContext() !=null &&
							workflowService.getContext().getProcedure() !=null) {
						procedurename = workflowService.getContext().getProcedure().getCode() + ".  ";
					}
					else
						procedurename = "";
					return new Model<String>(procedurename + taskname);
				}
				public String getCss() {
					return "col-lg-3";
				}
			});

			
			properties.add(new Property<Content>() {
				public IModel<String> getLabel() {
					return new StringResourceModel("grid.class", WorkflowBatchActionsPanel.this, null);
				}
				public IModel<String> getValue(IModel<Content> model) {
					return new PropertyModel<String>(model, "contentTemplate.name");
				}
				public String getCss() {
					return "col-lg-2";
				}
			});
		}
		
		return properties;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}	
	
	
}
