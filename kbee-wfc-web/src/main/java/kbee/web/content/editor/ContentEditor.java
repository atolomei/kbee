package kbee.web.content.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.novamens.content.base.Content;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.user.UserService;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.editor.ClassificableEditor;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.panel.KBPanel;

@SuppressWarnings("serial")
public abstract class ContentEditor<T extends Content> extends KBPanel implements ClassificableEditor<T> {
	private static final long serialVersionUID = 1L;

	private IModel<T> model;
	private List<String> updatedParts = new ArrayList<String>();
	private List<UpdatedField> updatedFields = new ArrayList<UpdatedField>();
	
	private boolean editionEnabled = false;   // en modo edicion(true) o (view=false)
	private boolean is_read_only   = false;	      
	
	private List<ClassificationPanel<T>> classificationEditors = null;
	
	private boolean updated = false;
	
	public ContentEditor() {
		super("editor");
		setOutputMarkupId(true);
		add(new Submitener());
	}

	public ContentEditor(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	public ContentEditor(IModel<T> model) {
		this("editor", model);
	}
	
	public ContentEditor(String id, IModel<T> model) {
		super(id);
		setModel(model);
		setOutputMarkupId(true);
		add(new Submitener());
	}
	
	public class Submitener extends AbstractDefaultAjaxBehavior implements IFormSubmitter {
		private static final long serialVersionUID = 1L;
		@Override
		protected void respond(AjaxRequestTarget target) {
			Request request = RequestCycle.get().getRequest();
			String autosave = request.getRequestParameters().getParameterValue("autosave").toString("");
			String save = request.getRequestParameters().getParameterValue("save").toString("");
			if (getForm()==null)
				return;
			getForm().process(this);
			ContentEditor.this.update(target,"true".equals(autosave));
			if ("true".equals(save)) {
				target.add(ContentEditor.this);
			}
		}
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			
			StringBuilder script = new StringBuilder();
			
			script.append("function onunload() {\n ");
			script.append("top.isautosave = false; top.issave = false; submit(); \n");
			script.append("clearTimeout(top.autosave);\n");
			script.append("}\n");
			
			script.append("function submit() { \n ");
			script.append(getCallbackScript());
			script.append("}\n");
			
			script.append("function doAutosave() {\n ");
			script.append("top.isautosave = true; top.issave = false; submit();\n");
			script.append("setAutosave();\n");
			script.append("}\n");
			
			script.append("function doSave() {\n ");
			script.append("top.isautosave = false; top.issave = true;  clearTimeout(top.autosave); submit();\n");
			script.append("}\n");
			
			script.append("function setAutosave() {\n ");
			script.append("top.autosave=setTimeout(\"doAutosave()\", 90000)");
			script.append("}\n");
			
			response.render(new JavaScriptContentHeaderItem(script.toString(), "submit"));
			if (isEditionEnabled())
			response.render(OnDomReadyHeaderItem.forScript("setAutosave()"));
		}	
		public Form<?> getForm() {
			return ContentEditor.this.getForm();
		};
		public boolean getDefaultFormProcessing() {
			return true;
		}
		public void onSubmit() {
		}
		public void onAfterSubmit()  {
		}
		public void onError() {
		};
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);

			Form<?> form = getForm();
			
			if (form==null) return;
			
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
			
			AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
				@Override 
				public CharSequence getBeforeHandler(Component component) { 
					return "if (typeof(tinyMCE) != \"undefined\") tinyMCE.triggerSave(true,true);";
				}
			};
			attributes.getAjaxCallListeners().add(myAjaxCallListener);
			
			attributes.getDynamicExtraParameters().add("return {autosave: top.isautosave, save: top.issave};");
			
			updateSubmitenerAttributes(attributes);
		}
	}
	
	public class SubmitButton extends AjaxButton {
		private static final long serialVersionUID = 1L;
		
		public SubmitButton(String id, Form<?> form) {
			super(id, form);
			add(new Label("label", getLabel()));
		}	
		@Override 
		protected void onSubmit(AjaxRequestTarget target) {
			update(false);
		}
		@Override
		public boolean isEnabled() {
			return isEditionEnabled();
		}
		@Override
		protected void onError(AjaxRequestTarget target) {
			getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
				@Override
				public void component(Field<?> field, IVisit<Void> visit) {
					if (field.hasErrorMessage()) {
						target.focusComponent(field.getInput());
					}
				} 
			});
		}
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
			IAjaxCallListener listener = new IAjaxCallListener() {
				@Override
				public CharSequence getSuccessHandler(Component component) {
					return null;
				}
				@Override
				public CharSequence getPrecondition(Component component) {
					return null;
				}
				@Override
				public CharSequence getFailureHandler(Component component) {
					return null;

				}
				@Override
				public CharSequence getCompleteHandler(Component component) {
					return null;
				}
				@Override
				public CharSequence getBeforeSendHandler(Component component) {
					return null;
				}
				@Override
				public CharSequence getBeforeHandler(Component component) {
					String s = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<span class=\"far fa-sync glyphicon-refresh-animate\"></span> "+getWorkingLabel().getObject()+"'";
					return s;
				}
				@Override
				public CharSequence getAfterHandler(Component component) {
					return null;
				}
				@Override
				public CharSequence getDoneHandler(Component component) {
					return null;
				}
				@Override
				public CharSequence getInitHandler(Component component) {
					return null;
				}
			};
			attributes.getAjaxCallListeners().add(listener);
			AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
				@Override 
				public CharSequence getBeforeHandler(Component component) { 
					return "if (typeof(tinyMCE) != \"undefined\") tinyMCE.triggerSave(true,true)";
				}
			};
			attributes.getAjaxCallListeners().add(myAjaxCallListener);
		}
		
		public IModel<String> getLabel() {
			return new StringResourceModel("button.submit", ContentEditor.this, null);
		}
		
		protected IModel<String> getWorkingLabel() {
			return new StringResourceModel("button.submiting", ContentEditor.this, null);
		}
	};
	
	public void edit(AjaxRequestTarget target) {
		
	}
	
	public void setUpdated(boolean value) {
		updated = true;
	}
	
	public boolean getUpdated() {
		return updated;
	}
	
	public void update() {
		update(false);
	}
	
	public void update(boolean auto) {
	}
	
	public void update(AjaxRequestTarget target, boolean auto) {
		update(false);
	}
	
	public void update(AjaxRequestTarget target) {
		update(false);
	}
	
	@Override
	public void update(T object) {
		for (ClassificationPanel<T> panel : getClassificationEditor(getPage().iterator())) {
			if (!panel.isReadOnly())
			panel.update(object);
		}
	}
	
	public boolean isEditionEnabled() {
		return this.editionEnabled;
	}
	
	@Override
	public boolean isFullWidth() {
		return false;
	}
	
	public void setEditionEnabled(boolean value) {
		this.editionEnabled = value;
	}
	
	public void setReadOnly(boolean value) {
		this.is_read_only = value;
	}
	
	public boolean isReadOnly() {
 		return this.is_read_only;
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
	
	public void setUpdatedPart(String updatedPart) {
		if (!updatedParts.contains(updatedPart) && !"".equals(updatedPart))
			updatedParts.add(updatedPart);
	}
	
	public List<String> getUpdatedParts() {
		return updatedParts;
	}
	
	public void setUpdatedField(UpdatedField updatedField) {
		//if (!updatedField.contains(updatedPart) && !"".equals(updatedPart))
		updatedFields.add(updatedField);
	}
	
	public List<UpdatedField> getUpdatedFields() {
		return updatedFields;
	}
	
	public Form<?> getForm() {
		return (Form<?>)get("form");
	}
	
	public List<String> getAttributeValue(Attribute attribute) {
		List<String> values = new ArrayList<String>();
		if (classificationEditors == null || classificationEditors.isEmpty())
			values = getModelObject().getAttributeValues(attribute);
		else {
			for (ClassificationPanel<T> editor : classificationEditors) {
				if (editor.includes(attribute)) {
					if (editor.isUpdated())
						values.addAll(editor.getAttributeValue(attribute));
					else
						values.addAll(getModelObject().getAttributeValues(attribute));
				}
			}
		}	
		return values;
	}
	
	public void setEditors(Collection<ClassificationPanel<T>> editors) {
		classificationEditors = new ArrayList<ClassificationPanel<T>>();
		classificationEditors.addAll(editors);
	}
	
	public void addEditor(ClassificationPanel<T> editor) {
		if (classificationEditors==null)
		classificationEditors = new ArrayList<ClassificationPanel<T>>();
		classificationEditors.add(editor);
	}
	
	public List<ClassificationPanel<T>> getEditors() {
		return classificationEditors;
	}
	
	public List<Classification> getClassification() {
		List<Classification> classification = null;
		if (classificationEditors == null || classificationEditors.isEmpty())
			classification = getModelObject().getClassification();
		else {
			classification = new ArrayList<Classification>();
			for (ClassificationPanel<T> editor : classificationEditors) {
				if (editor.isUpdated()) {
					classification.addAll(editor.getClassification());
				}
				else {
					for (Classifier classifier : editor.getClassifiers()) {
						classification.addAll(getModelObject().getClassification(classifier));
					}
				}
			}
		}
		return classification;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void onDetach() {
		if (getModel()!=null)
			getModel().detach();
		if (classificationEditors!=null)
			for (ClassificationPanel editor : classificationEditors) {

				// if (editor instanceof ContentClassificationEditor)
				//	 ((ContentClassificationEditor)editor).onDetach();
				
				if (editor instanceof IDetachable)
					((IDetachable) editor).detach();
				((Panel) editor).detach();
			}
		super.onDetach();
	}
	
	protected void reset()  {
		updatedParts = new ArrayList<String>();
		updatedFields = new ArrayList<UpdatedField>();
	}
	
	protected void updateSubmitenerAttributes(AjaxRequestAttributes attributes) {
	}
	
	protected ClassificationPanel<T> getClassificationEditor() {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected List<ClassificationPanel<T>> getClassificationEditor(Iterator<Component> childs) {
		List<ClassificationPanel<T>> editors = new ArrayList<ClassificationPanel<T>>();
		while (childs.hasNext()) {
			Component child = childs.next();
			if (child instanceof ClassificationPanel<?>) {
				editors.add((ClassificationPanel<T>)child);
			}
			else {
				if (child instanceof MarkupContainer) {
					editors.addAll(getClassificationEditor(((MarkupContainer)child).iterator()));
				}
			}
		}
		return editors;
	}
	
	protected boolean isWriteable(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(content);
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	/**
	 * 
	 * URL from HTTP Request received
	 * Wicket based
	 * 
	 * @return
	 */
	protected String getServerUrl() {
		String protocol =((WebRequest)RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host =((WebRequest)RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport =((WebRequest)RequestCycle.get().getRequest()).getUrl().getPort(); 
		String port = (iport.equals(80) || iport.equals(443) ? "":  ( ":" + iport.toString()) );
		return protocol +"://" + host + port;
	}
}