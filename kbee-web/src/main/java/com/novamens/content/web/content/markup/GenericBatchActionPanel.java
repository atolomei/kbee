package com.novamens.content.web.content.markup;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.web.content.markup.ContentSelectionPanel;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxSubmitLink;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.content.editor.ContentEditor;
import kbee.web.util.Property;


/**
 * 
 *  Batch Delete: Tasks, DataSetMembers
 *  
 * 
 *
 */
@SuppressWarnings("serial")
public class GenericBatchActionPanel extends ContentEditor<Content> {
	private static final long serialVersionUID = 1L;
	
	private boolean done = false;
	
	private IModel<String> retlabel;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GenericBatchActionPanel.class.getName());

	
	public GenericBatchActionPanel (String id, List<IModel<Content>> selection) {
		super(id);
		
		setOutputMarkupId(true);
		
		add(new ContentSelectionPanel(selection) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(GenericBatchActionPanel.this);
			}
			
			@Override
			protected List<Property<Content>> getProperties() {
				return GenericBatchActionPanel.this.getSelectionProperties();
			}
			
			@Override
			protected Page getPage(IModel<Content> model) {
				return GenericBatchActionPanel.this.getPage(model);
			}
		});
		
		Model<String> feedbackmodel = new Model<String>() {
			public String getObject() {
				if (done)
					if (!GenericBatchActionPanel.this.hasErrors())
						return getLabel("ok-message").getObject();
					else
						return getLabel("errors-message").getObject();
				return null;
			}
		};
		
		Label feedbackpanel = new Label("feedback", feedbackmodel) {
			public boolean isVisible() {
				return done || !validateSelection();
			}
		};
		
		add(feedbackpanel);
		
		feedbackpanel.add( new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (!GenericBatchActionPanel.this.hasErrors())
					return "col-lg-12 alert alert-info";
				else
					return "col-lg-12 alert alert-danger";
			}
		}));
		
		((Label)get("feedback")).setEscapeModelStrings(false);
		
		Form<Content> form = new Form<Content>("form", selection.get(0), Disposition.VERTICAL) {
			public boolean isVisible() {
				return !done;
			}
		};

		WorkingIndicatorAjaxSubmitLink exec = new WorkingIndicatorAjaxSubmitLink("button", "modal-submit",  form) {
			
			@Override
			public String getAjaxIndicatorMarkupId() {
				return getId();
			}
			
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				executeBatch(target);
				done = true;
			 	target.add(GenericBatchActionPanel.this);
				done = true;
				if (!hasErrors())
					onReturn();
			}
			@Override
			public boolean isVisible() {
				return !done;
			}
			@Override
			public String getLabel() {
				return getExecuteButtonLabel().getObject();
			}
			@Override
			public String getWorkingLabel() {
				return GenericBatchActionPanel.this.getLabel("executing").getObject();
			}
		};
		
		exec.add(new AttributeModifier("class", getExecuteButtonCss()));
		
		exec.add(new Label("execute-label", getExecuteButtonLabel()));
		
		form.add(exec);
		
		form.add(new AjaxSubmitLink("button-abort", form) {
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				done = true;
				onReturn();
			}
			public boolean isVisible() {
				return !done;
			}
		});
		
		add(form);
	}

	 
	public IModel<String> getReturnLabel() {
		return retlabel;
	}
	
	 
	@Override
	public Form<?> getForm() {
		return (Form<?>)get("form");
	}
	
	 
	public IModel<String> getLabel(String key) {
		return new StringResourceModel(key, GenericBatchActionPanel.this, null);
	}
	
	 
	protected Page getPage(IModel<Content> model) {
		return null;
	}
	
	 
	protected void onReturn() {
	}
	
	 
	protected String getExecuteButtonCss() {
		return "btn btn-sm btn-primary";
	}
	
	 
	protected List<Property<Content>> getSelectionProperties() {
		return null;
	}
	 
	protected String executeAction(IModel<Content> model){
		return "";
	}
	
	 
	protected void executeBatch(AjaxRequestTarget target){
		for (IModel<Content> model : getSelection()) {
			String status = executeAction(model);
			((ContentSelectionPanel) get("selection")).setStatus(model.getObject(), status);
		}
	}
	
	 
	protected boolean hasErrors() {
		return ((ContentSelectionPanel)get("selection")).hasErrors();
	}
 
	protected boolean validateSelection() {
		return true;
	}
	
	 
	protected List<IModel<Content>> getSelection() {
		return ((ContentSelectionPanel)get("selection")).getSelection();
	}
 
	protected IModel<String> getExecuteButtonLabel() {
		return GenericBatchActionPanel.this.getLabel("execute");
	}

	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIsNew(boolean isnew) {
		// TODO Auto-generated method stub
		
	}
}
