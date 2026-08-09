package kbee.web.eform;

import java.time.OffsetDateTime;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.resource.ExternalResource;
import com.novamens.kbee.wicket.editor.Editor;

import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;

@SuppressWarnings("serial")
public class ResourceEditor extends DomainObjectEditor<Resource> implements Editor<Resource> {
	private static final long serialVersionUID = 1L;

	private boolean enabled = false;

	private boolean updated = false;
	
	
	public ResourceEditor(String id, IModel<Resource> model, IModel<Content> contentmodel) {
		super(id, model);
		setOutputMarkupId(true);
		setContent(contentmodel);
	}
	
	public void setContent(IModel<Content> model) {
	}
	
	public Resource getResource() {
		return getModelObject();
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		Form<Resource> form = new com.novamens.wicket.markup.html.form.Form<Resource>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("title", true) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				updated = true;
			}
			@Override
			public String getPart() {
				return getResource().getName() + " " +super.getPart();
			}
			protected boolean autofocus() {
				return true;
			}
		});
		
		form.add(new TextAreaField<String>("description", 12, 20) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				updated = true;
			}
			@Override
			public String getPart() {
				return getResource().getName() + " " +super.getPart();
			}
		});

		if (getResource() instanceof ExternalResource) {
			form.add(new TextField<String>("url", true) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					updated = true;
				}
				@Override
				public String getPart() {
					return getResource().getName() + " " +super.getPart();
				}
			});
		}
		else
			form.add( (new Label("url", "")).setVisible(false));
					
		form.add(new AjaxSubmitLink("save-link", form) {
			public void onSubmit(AjaxRequestTarget target) {
				if (updated) {
					getModelObject().setLastModifiedOffsetDateTime(OffsetDateTime.now());
					getModelObject().setLastModifiedUser(getSessionUser());
				}
				enabled = false;
				updated = true;
				onUpdate(target);
			}
		});
		
		form.add(new AjaxLink<Void>("cancel-link") {
			public void onClick(AjaxRequestTarget target) {
				enabled = false;
				getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
					@Override
					public void component(Field<?> field, IVisit<Void> visit) {
						field.cancel();
					}
				});
				onClose(target);
			}
		});
		
		add(form);
	}
	
	@Override
	public void edit(final AjaxRequestTarget target) {
		enabled = true;
		super.edit(target);
		((Field<?>)get("form:title")).onBeforeRender();
		target.focusComponent(((Field<?>)get("form:title")).getInput());
	}
	
	@Override
	public boolean isVisible() {
		return enabled;
	}
	
	public void onUpdate(AjaxRequestTarget target) {
		
	}
	
	public void onClose(AjaxRequestTarget target) {
		
	}
}
