package com.novamens.content.web.editor.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

import com.novamens.content.web.markup.EditorForm;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxButton;

import kbee.web.console.AjustableHeightBehavior;

@Deprecated
public abstract class DeprecatedObjectEditor<T> extends Panel implements Editor<T> {
	private static final long serialVersionUID = 1L;

	private IModel<T>  model;
	
	private List<String> updatedParts = new ArrayList<String>();

	private boolean editionEnabled 	= false;
	private boolean isNew 			= false;
	private boolean checkout 		= false;
	private boolean isReadOnly 		= false;
	
	public DeprecatedObjectEditor(IModel<T> model) {
		this(model, true);
	}
		
	public DeprecatedObjectEditor(IModel<T> model, boolean autosave) {
		this(model, autosave, true);
	}

	public DeprecatedObjectEditor(IModel<T> model, boolean autosave, boolean fitsize) {
		this("editor", model, autosave, fitsize);
	}

	public DeprecatedObjectEditor(String id, IModel<T> model, boolean autosave, boolean fitsize) {
		super(id);
		
		setModel(model);
		setOutputMarkupId(true);
		WebMarkupContainer editor = new WebMarkupContainer("editor");
		
		if (fitsize)
			editor.add(new AjustableHeightBehavior(108));
		
		add(editor);
		
		if (autosave) 
			add(new Submitener());
	}
 
	public class Submitener extends AbstractDefaultAjaxBehavior implements IFormSubmitter {
		private static final long serialVersionUID = 1L;
		@Override
		protected void respond(AjaxRequestTarget target) {
			Request request = RequestCycle.get().getRequest();
			String autosave = request.getRequestParameters().getParameterValue("autosave").toString("");
			getForm().process(this);
			DeprecatedObjectEditor.this.getForm().update("true".equals(autosave));
		}
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			
			StringBuilder script = new StringBuilder();
			
			script.append("function onunload() {\n ");
			script.append("top.isautosave = false; submit();\n");
			script.append("clearTimeout(top.autosave);\n");
			script.append("}\n");
			
			script.append("function submit() {\n ");
			script.append(getCallbackScript());
			script.append("}\n");
			
			script.append("function doAutosave() {\n ");
			script.append("top.isautosave = true; submit();\n");
			script.append("setAutosave();\n");
			script.append("}\n");
			
			script.append("function setAutosave() {\n ");
			script.append("top.autosave=setTimeout(\"doAutosave()\", 30000)");
			script.append("}\n");
			
			response.render(new JavaScriptContentHeaderItem(script.toString(), "submit"));
			
			if (isEditionEnabled())
				response.render(OnDomReadyHeaderItem.forScript("setAutosave()"));
		}	
		
		public Form<?> getForm() {
			return DeprecatedObjectEditor.this.getForm();
		}
		
		public boolean getDefaultFormProcessing() {
			return true;
		}
		public void onSubmit() {
		}
		public void onAfterSubmit() {
		}
		public void onError() {
		}
		
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);

			Form<?> form = getForm();
			
			attributes.setFormId(form.getMarkupId());

			String formMethod = form.getMarkupAttributes().getString("method");
			if (formMethod == null || "POST".equalsIgnoreCase(formMethod)) {
				attributes.setMethod(Method.POST);
			}

			if (form.getRootForm().isMultiPart()){
				attributes.setMultipart(true);
				attributes.setMethod(Method.POST);
			}

			if (getComponent() instanceof IFormSubmittingComponent)	{
				String submittingComponentName = ((IFormSubmittingComponent)getComponent()).getInputName();
				attributes.setSubmittingComponentName(submittingComponentName);
			}
			
			attributes.getDynamicExtraParameters().add("return {autosave: top.isautosave};");
			
			updateSubmitenerAttributes(attributes);
		}
	}
	
	public class SubmitButton extends WorkingIndicatorAjaxButton {
		private static final long serialVersionUID = 1L;
		public SubmitButton(String id, Form<?> form) {
			super(id, form);
		}	

		@Override 
		protected void onSubmit(AjaxRequestTarget target) {

			// Update de los componentes
			String updatemessage = update(false);
			
			// Si hay errores "error" y "onError" 
			//
			if (updatemessage!=null) {
				getForm().error(updatemessage);
				onError(target);
			}
			else {
				// sino "onInfo" y "onAfterSubmit"
				//
				//info(new StringResourceModel("objecteditor.saving", this, null).getString());
				DeprecatedObjectEditor.this.getForm().onInfo(target);
				DeprecatedObjectEditor.this.onAfterSubmit(target);
			}
			reset();
		}
		
		@Override
		public boolean isEnabled() {
			return true;
		}
		
		@Override
		public boolean isVisible() {
			return true;
		}
		
		@Override
		protected void onError(AjaxRequestTarget target) {
			DeprecatedObjectEditor.this.getForm().onError(target);
		}
		
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
		}
	};
	
	public void edit(AjaxRequestTarget target) {
		setEditionEnabled(true);
		target.add(this);
	}
	
	@Override
	public void onDetach() {
		getModel().detach();
		getForm().detach();
		super.onDetach();
	}
	
	public String update() {
		return update(false);
	}
	
	public String update(boolean auto) {
		return null;
	}
	
	public void update(AjaxRequestTarget target) {
	}
	
	public void setEditionEnabled(boolean editionEnabled) {
		this.editionEnabled = editionEnabled;
	}
	
	public void setIsNew(boolean value) {
		this.isNew = value;
	}
	
	public boolean isNew() {
		return this.isNew;
	}
	
	public boolean isEditionEnabled() {
		return this.editionEnabled;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public T getModelObject() {
		return model.getObject();
	}
	
	public void error(String  message) {
		getForm().error(message);
	}

	public void setUpdatedPart(String updatedPart) {
		if (!updatedParts.contains(updatedPart))
			updatedParts.add(updatedPart);
	}
	
	public List<String> getUpdatedParts() {
		return updatedParts;
	}
	
	public EditorForm getForm() {
		return (EditorForm)get("editor").get("form");
	}
	 
	public void setForm(EditorForm form) {
		((WebMarkupContainer)get("editor")).add(form);
	}
	
	public void setReadOnly(boolean b) {
		isReadOnly=b;
	}
	
	public boolean isReadOnly() {
		return isReadOnly; 
	}
	
	public void update(T object) {
		
	}
	
	protected void reset()  {
		updatedParts = new ArrayList<String>();
		checkout = false;
	}
	
	protected void setCheckout(boolean value) {
		checkout = true;
	}
	
	protected boolean isCheckout() {
		return checkout;
	}
	
	protected void onAfterSubmit(AjaxRequestTarget target) {
	}
	
	protected void updateSubmitenerAttributes(AjaxRequestAttributes attributes) {
	}

	@Override
	public boolean isFullWidth() {
		// TODO Auto-generated method stub
		return false;
	}
}
