package com.novamens.wicket.markup.html.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.UpdatedField;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.panel.KBPanel;


/**
 * 
 * 
 * 
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class ObjectEditor<T> extends KBPanel implements Editor<T> {
	private static final long serialVersionUID = 1L;
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectEditor.class.getName());
	
	private IModel<T>  model;
	private boolean editionEnabled = true;
	private boolean readonly = false;
	
	private Editor<T> editor; 
	
	private Component focus;
	private List<String> updatedParts = new ArrayList<String>();
	private List<UpdatedField> updatedFields = new ArrayList<UpdatedField>();
	
	private boolean is_new = false;
	
	
	public class SubmitButton extends AjaxButton {
		
		public SubmitButton(Form<?> form) {
			super("submit", form);
		}
		
		public SubmitButton(String id, Form<?> form) {
			super(id, form);
		}	
		@Override 
		protected void onSubmit(AjaxRequestTarget target) {
			update(target);
		}
		@Override 
		protected void onError(final AjaxRequestTarget target) {
			getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
				@Override
				public void component(Field<?> field, IVisit<Void> visit) {
					if (field.hasErrorMessage()) {
						target.focusComponent(field.getInput());
					}
				}
			});
			target.appendJavaScript("document.getElementById('"+getMarkupId()+"').innerHTML = '"+getLabel().getObject()+"'");
			target.add(getForm());
		}
		
		@Override
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
					String s = null, s1=null;
					if (getWorkingLabel()!=null) {
					String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+(getLabel()!=null?getLabel().getObject():"")+"';";
						s ="setTimeout(function () {"+s1+"}, 520);";
					}
					return s;
				}
				@Override
				public CharSequence getBeforeSendHandler(Component component) {
					return null;
				}
				@Override
				public CharSequence getBeforeHandler(Component component) {
					String s = SubmitButton.this.getBeforeHandler();
					s += "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<span class=\"far fa-sync fa-spin fa-fw spinning\"></span> "+getWorkingLabel().getObject()+"'";
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
		}
		public String getBeforeHandler() {
			return "";
		}
		
		protected IModel<String> getWorkingLabel() {
			return new StringResourceModel("saving", ObjectEditor.this, null);
		}
		
		@Override
		public IModel<String> getLabel() {
			return  ObjectEditor.this.getSubmitLabel();
		}
	};


	/**
	 * @param id
	 * @param model
	 */
	public ObjectEditor(String id, IModel<T> model) {
		super(id);
		setOutputMarkupId(true);
		setModel(model);
	}
	
	public ObjectEditor(String id) {
		super(id);
	}
	
	public void update(AjaxRequestTarget target) {
		
	}
	
	public void update(T object) {
		
	}
	
	public void setEditionEnabled(boolean editionEnabled) {
		this.editionEnabled = editionEnabled;
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
	
	public IModel<String> getSubmitLabel() {
		return new StringResourceModel("save", ObjectEditor.this, null);
	}
	
	public void edit(final AjaxRequestTarget target) {
		setEditionEnabled(true);
		getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
			@Override
			public void component(Field<?> field, IVisit<Void> visit) {
				if (focus==null) {
					target.focusComponent(field.getInput());
					focus = field;
				}
			}
		});
		target.add(this);
	}

	public void cancel(AjaxRequestTarget target) {
		
		setEditionEnabled(false);
		
		getForm().visitChildren(ObjectEditorPanel.class, new IVisitor<ObjectEditorPanel<?>, Void>() {
			@Override
			public void component(ObjectEditorPanel<?> panel, IVisit<Void> visit) {
				panel.cancel();
			}
		});
		getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
			@Override
			public void component(Field<?> field, IVisit<Void> visit) {
				field.cancel();
			}
		});
		target.add(this);
	}
	
	public T getModelObject() {
		return model!=null ? model.getObject() : getEditor().getModelObject();
	}
	
	public void setUpdatedPart(String updatedPart) {
		if (!updatedParts.contains(updatedPart) && !"".equals(updatedPart))
			updatedParts.add(updatedPart);
	}

	public List<String> getUpdatedParts() {
		return updatedParts;
	}
	
	@Override
	public void setUpdatedField(UpdatedField updatedField) {
		if (!"".equals(updatedField.getField()))
			updatedFields.add(updatedField);
	}
	
	@Override
	public List<UpdatedField> getUpdatedFields() {
		return updatedFields;
	}
	
	public void reset() {
		updatedParts.clear();
	}
	
	public Form<?> getForm() {
		return (Form<?>)get("form");
	}
	
	@Override
	public boolean isFullWidth() {
		return false;
	}
	
	public boolean isReadOnly() {
		return this.readonly;
	}
	
	public void setReadOnly(boolean re) {
		this.readonly=re;
	}
	
	public boolean isNew() {
		return this.is_new;
	}
	
	public void setIsNew(boolean is_new) {
		this.is_new=is_new;
	}

	public List<Locale> getLocales() {
		List<Locale> locales = new ArrayList<Locale>();
		locales.add(new Locale("en"));
		locales.add(new Locale("es"));
		return locales;
	}
	
	@SuppressWarnings("unchecked")
	public Editor<T> getEditor() {
		
		
		if (this.editor==null) {
			MarkupContainer parent = getParent();
			Editor<T> editor = null;
			while (editor==null && parent!=null) {
				if (parent instanceof Editor) {
					editor = (Editor<T>)parent;
					this.editor = editor;
				}
				else
					parent = parent.getParent();
			}
		}
		
		if (this.editor == null) {
			logger.debug("Editor is null -> probably some issue or bug to check");
		}
		
		return this.editor;
	}
	
	
	@Override
	public void onDetach() {
	
		this.focus = null;
		
		if (getModel()!=null)
			getModel().detach();
		
		if (getForm()!=null)
			getForm().detach();
		
		super.onDetach();
	}

	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
	protected ContentDao getContentDao() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		return (ContentDao) beans.getBean("contentDao");
	}
}
