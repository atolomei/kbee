package kbee.web.object;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
 
/**
 * 
 *  called from {@link BatchDeletePage<T>}
 *  
 * @param <T>
 */
@SuppressWarnings("serial")
public class BatchDeletePanel<T> extends ObjectEditor<T> {
	private static final long serialVersionUID = 1L;
	
	private boolean done = false;
	
	static Logger logger = LogManager.getLogger(BatchDeletePanel.class.getName());

	
	public BatchDeletePanel (String id, String selectionLabel, List<IModel<T>> selection) {
		super(id);
		setOutputMarkupId(true);
		
		if (logger.isDebugEnabled()) {
			for (IModel<T> model: selection) {
			logger.debug(model.getObject().toString());	
			}
		}
			
		add(new SelectionPanel<T>(selectionLabel, selection) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(BatchDeletePanel.this);
			}
		});
		
		Model<String> feedbackmodel = new Model<String>() {
			@Override
			public String getObject() {
				if (done)
					if (!BatchDeletePanel.this.hasErrors())
						return getLabel("ok-message").getObject();
					else
						return getLabel("errors-message").getObject();
				return null;
			}
		};
		
		WebMarkupContainer fpanel = new WebMarkupContainer("feedback-panel") {
			public boolean isVisible() {
				return done;
			}
		};
		
		add(fpanel);
		
		Label fb = new Label("feedback", feedbackmodel) {
			@Override
			public boolean isVisible() {
				return done;
			}
		};

		fb.add(new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
					if (BatchDeletePanel.this.hasErrors())
						return "alert alert-danger";
					else
						return "alert alert-info";
				}
		}));
		
		fpanel.add(fb);
		
		
		((Label)get("feedback-panel:feedback")).setEscapeModelStrings(false);
		
		Form<T> form = new Form<T>("form", selection.get(0), Disposition.VERTICAL) {
			@Override
			public boolean isVisible() {
				return !done;
			}
		};
		
		form.add(new AjaxSubmitLink("button-execute", form) {
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				executeBatch(target);
				target.add(BatchDeletePanel.this);
				done = true;
				BatchDeletePanel.this.onDetach();
				onAfterDelete(target);
			}
			
			@Override
			public boolean isVisible() {
				return !done;
			}
		});
		
		form.add(new AjaxSubmitLink("button-cancel", form) {
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				BatchDeletePanel.this.onClose();
				done = true;
			}

			@Override
			public boolean isVisible() {
				return !done;
			}
		});
		add(form);
	}
	
	protected void onAfterDelete(AjaxRequestTarget target) {
		if (!BatchDeletePanel.this.hasErrors())
			onClose();
	}
	
	@Override
	public Form<?> getForm() {
		return (Form<?>)get("form");
	}
	
	public IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}

	@Override
	public void onDetach() {
		for (IModel<T> model : getSelection()) 
			model.detach();
		get("selection").detach();
		super.onDetach();
	}

	
	protected void onClose() {}
	
	
	@SuppressWarnings("unchecked")
	protected void executeBatch(AjaxRequestTarget target) {
		for (IModel<T> model : getSelection()) {
			String str = executeDelete(model);
			if (str==null)
				((SelectionPanel<T>)get("selection")).setStatus(model.getObject(), "");
			else
				((SelectionPanel<T>)get("selection")).setStatus(model.getObject(), str);
		}
	}
	
	protected String executeDelete(IModel<T> model) {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean hasErrors() {
		return ((SelectionPanel<T>)get("selection")).hasErrors();
	}
	
	@SuppressWarnings("unchecked")
	protected List<IModel<T>> getSelection() {
		return ((SelectionPanel<T>)get("selection")).getSelection();
	}
}
