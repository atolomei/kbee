package kbee.web.resource;

import java.time.OffsetDateTime;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.UpdatedField;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;

@SuppressWarnings("serial")
@Deprecated
public class ResourceEditor extends Panel implements Editor<Resource> {
	private static final long serialVersionUID = 1L;

	private boolean enabled = false;
	private boolean updated = false;

	private IModel<Resource> model;
	//private IModel<ResourceGroup> groupmodel;
	private boolean groupupdated = false;

	public ResourceEditor(IModel<Resource> model) {
		super("editor");
		
		setOutputMarkupId(true);
		setModel(model);
		
		//setGroup(ResourcesPanel.this.getGroup(model.getObject()));
		
		Form<Resource> form = new Form<Resource>("form", Disposition.VERTICAL);
		
		form.setOutputMarkupId(true);
		
		form.add(new TextField<String>("title", true) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				updated = true;
			}
			@Override
			public String getPart() {
				return ResourceEditor.this.getModel().getObject().getName() + " " +super.getPart();
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
				return ResourceEditor.this.getModel().getObject().getName() + " " +super.getPart();
			}
		});

//		if (model.getObject() instanceof ExternalResource) {
//			form.add(new TextField<String>("Url", true) {
//				@Override
//				public void onUpdate(AjaxRequestTarget target) {
//					updated = true;
//				}
//				@Override
//				public String getPart() {
//					return ResourceEditor.this.getModel().getObject().getName() + " " +super.getPart();
//				}
//			});
//		}
//		else
//			form.add( (new Label("Url", "")).setVisible(false));

		form.add(new BooleanField("inPortalVersion") {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				//updated = true;
			}
			public boolean isVisible() {
				//return ResourcesPanel.this.isPublicArea();
				return true;
			}
		});
		
//		form.add(new ChoiceField<ResourceGroup>("group", new PropertyModel<ResourceGroup>(this, "group") ,()->getGroups()) {
//			@Override
//			public void onUpdate(AjaxRequestTarget target) {
//				groupupdated = true;
//			}
//			public boolean isVisible() {
//				//return !ResourcesPanel.this.getGroups().isEmpty();
//				return true;
//			}
//		});
//					
		form.add(new AjaxSubmitLink("save-link", form) {
			public void onSubmit(AjaxRequestTarget target) {
				if (groupupdated) {
					//ResourcesPanel.this.setGroup(getModelObject(), getGroup());
					//target.add(ResourcesPanel.this);
				}
				if (updated) {
					getModelObject().setLastModifiedOffsetDateTime(OffsetDateTime.now());
					//getModelObject().setLastModifiedUser(getSessionUser());
				}
				enabled = false;
				updated = true;
				target.add(ResourceEditor.this.getParent().getParent());
			}
		});
		
		form.add(new AjaxLink<Void>("cancel-link") {
			public void onClick(AjaxRequestTarget target) {
				enabled = false;
//				getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
//					@Override
//					public void component(Field<?> field, IVisit<Void> visit) {
//						field.cancel();
//					}
//				});
				target.add(ResourceEditor.this.getParent());
			}
		});
		
		add(form);
	}
	
	@SuppressWarnings("unchecked")
	public void enable(AjaxRequestTarget target) {
		enabled = true;
		((TextField<String>)get("form:title")).onBeforeRender();
		//target.add(ResourceEditor.this.getParent());
		target.focusComponent(((TextField<String>)get("form:title")).getInput());
	}
	
	public void setModel(IModel<Resource> model) {
		this.model = model;
	}
	
	public IModel<Resource> getModel() {
		return model;
	}
	
	public void update(AjaxRequestTarget target) {
	}
	
	public void update(Resource resource) {
	}
	
	public void edit(AjaxRequestTarget target) {
		this.enabled = true;
	}
	
	public void close(AjaxRequestTarget target) {
		enabled = false;
	}
	
	public Form<?> getForm() {
		return null;
	}
	
	public Resource getModelObject() {
		return getModel().getObject();
	}
	
	public boolean isEditionEnabled() {
		return true;
	}
	
	public boolean isReadOnly() {
		return false;
	}
	
	public List<String> getUpdatedParts() {
		return null;
	}
	
	public void setUpdatedPart(String updatedPart) {
	}
	
	public List<UpdatedField> getUpdatedFields() {
		return null;
	}
	public void setUpdatedField(UpdatedField updatedField) {
	}
	@Override 
	public boolean isVisible() {
		return enabled;
	}
	
	@Override
	public boolean isFullWidth() {
		return false;
	}
	
	@Override
	public boolean isNew() {
		return false;
	}
	
	@Override
	public void setIsNew(boolean isnew) {
		
	}
	
	public ResourceTag getGroup() {
		//return groupmodel!=null ? groupmodel.getObject() : getDefaultGroup();
		return null;
	}
	
	public void setGroup(ResourceTag group) {
		//groupmodel = group!=null ? new ObjectModel<ResourceGroup>(group) : null;
	}
}
