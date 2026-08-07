package com.novamens.content.web.content.markup;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.TextAreaField;

/** 
 * @param <T>
 */
@SuppressWarnings("serial")
public class AbstractPanel<T extends Content> extends ObjectEditorPanel<T>  {
	private static final long serialVersionUID = 1L;
	private String text;
	private boolean editionEnabled = true;


	public AbstractPanel() {
		this("abstract-panel");
	}

	

	public AbstractPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}

	
	public AbstractPanel(String id, boolean edition_enabled) {
		super(id);
		setOutputMarkupId(true);
		setEditionEnabled(false);
	}


	public void setText(String text) {
		this.text = text;
	}


	public String getText() {
		return text;
	}


	@Override
	public void updateModel() {
		if ((text==null && getModelObject().getAbstract()!=null) ||
			(text!=null && getModelObject().getAbstract()==null) || 
			(text!=null && getModelObject().getAbstract()!=null && !text.equals(getModelObject().getAbstract().asString()))) {
			getModelObject().setAbstract(text);
			setUpdatedPart("Abstract");
		}
	}
	


	@Override
	public void onInitialize() {
		super.onInitialize();
		if (get("container-text:xcontent-text")==null) {
			if (getModelObject().getAbstract()!=null)
			setText(getModelObject().getAbstract().asString());
			addComponents();
		}
	}
	
	
	protected void addComponents() {
		
		WebMarkupContainer addcontainer = new WebMarkupContainer("add-container") {
			@Override
			public boolean isVisible() {
				return getEditor().isEditionEnabled(); 
			}
		};
		
		AjaxLink<Void> addb = new AjaxLink<Void>("add") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				editionEnabled = true;
				target.focusComponent(((Field<?>)AbstractPanel.this.get("container-editor:ztext-editor")).getInput());
				target.add(AbstractPanel.this);
			}
			@Override
			public boolean isEnabled() {
				return !editionEnabled; 
			}
			@Override
			public boolean isVisible() {
				return getEditor().isEditionEnabled() && !editionEnabled;
			}
		};
		add(addcontainer);
		addcontainer.add(addb);

		Label addoredit = new Label("edit", new Model<String>() {
			@Override
			public String getObject() {
				String key = getText()==null||getText().length()<1?"add":"edit";
				return new StringResourceModel(key, AbstractPanel.this, null).getString(); 
			}
		});
		
		
		addb.add(addoredit);
		
		
		WebMarkupContainer container_text = new WebMarkupContainer("container-text") {
			@Override
			public boolean isVisible() {
				return !isEditionEnabled(); 
			}
		};
		
		WebMarkupContainer container_editor = new WebMarkupContainer("container-editor")  {
			@Override
			public boolean isVisible() {
				return isEditionEnabled(); 
			}
		};
		
		add(container_text);
		add(container_editor);
		
		container_text.add(new Label("xcontent-text", new Model<String>() { 
			public String getObject() { 
				if (getText()==null || getText().length()==0) { 
					if (AbstractPanel.this.isEditionEnabled())
						return new StringResourceModel("edit", AbstractPanel.this, null).getString();
					return "";
				}
				else			
					return getText(); 
			}}) {
			@Override
			public boolean isVisible() {
				return !isEditionEnabled();
			}
		});
		
		get("container-text:xcontent-text").add(new AjaxEventBehavior("click") {
			@Override
			protected void onEvent(AjaxRequestTarget target) {
				editionEnabled = !editionEnabled;
				((Field<?>)AbstractPanel.this.get("container-editor:ztext-editor")).onBeforeRender();
				target.focusComponent(((Field<?>)AbstractPanel.this.get("container-editor:ztext-editor")).getInput());
				target.add(AbstractPanel.this);
			}
		});
		
		container_editor.add(new TextAreaField<String>("ztext-editor", new PropertyModel<String>(this, "text"), 6, 40) {
			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
			public IModel<String> getLabel() {
				return new Model<String>("");
			}
		});
		
		container_editor.add(new AjaxSubmitLink("save-link", getEditor().getForm()) {
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				editionEnabled = !editionEnabled;
				target.focusComponent(((Field<?>)AbstractPanel.this.get("container-editor:ztext-editor")).getInput());
				target.add(AbstractPanel.this);
			}
			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
		});
		
		container_editor.add(new AjaxLink<Void>("cancel-link") {
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				editionEnabled = false;
				((TextAreaField<String>)(AbstractPanel.this.get("container-editor:ztext-editor"))).cancel();
				target.add(AbstractPanel.this);
			}
			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
		});
	}
	


	protected void setEditionEnabled(boolean e) {
		this.editionEnabled=e;
	}


	protected boolean isEditionEnabled() {
		return getEditor().isEditionEnabled() && editionEnabled;
	}
}
