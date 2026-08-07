package com.novamens.content.web.content.markup;

import java.io.File;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ContentExportService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class RelationPanel<T extends Content> extends ObjectEditorPanel<T> {
	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RelationPanel.class.getName());

	
	private IModel<RelationTemplate> templatemodel;
	private boolean reverse;

	public RelationPanel(String id, boolean reverse) {
		super(id);
		setOutputMarkupId(true);
		this.reverse = reverse;
	}
	
	public boolean isAggregation() {
		if (getTemplateModel().getObject().isAggregation())
			return true;
		return false;
	}
	
	public boolean isReverseRelation() {
		return reverse;
	}
	
	public void setTemplateModel(IModel<RelationTemplate> model) {
		this.templatemodel = model;
	}
	
	public IModel<RelationTemplate> getTemplateModel() {
		return this.templatemodel;
	}

	protected IModel<String> getResLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
			addForm();
			
			
			ContextMenuPanel<T> dmenu = new ContextMenuPanel<T>("dmenu", getModel());
					
			dmenu.addItem(new MenuItemFactory<T>() {
				@Override
				public AbstractMenuItemPanelV5<T> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<T>(id) {
						 
						/**
						 */
						private static final long serialVersionUID = 1L;

						@Override
						public String getLabel() {	
							return new StringResourceModel("downloadall", RelationPanel.this, null).getObject();
						}
						@Override
						public boolean isVisible() {
							return true;
						}
						@Override
						protected File getFile() {
							return getModelObject().getService(ContentExportService.class).getRelationshipExport( getTemplateModel().getObject(), isReverseRelation() ? "target" : "source");
						}
						@Override
						public boolean isEnabled()  {
							try {
								return (isRoot() || !isSupportUser());
							} catch (Exception e) {
								logger.error(e, getSessionUser().getUserName());
								return false;
							}
						}
					};
				}
			});

			add(dmenu);
			
			
			WebMarkupContainer alc =new WebMarkupContainer("alert-container");
			add(alc);
			alc.setVisible(isReverseRelation() || isAggregation());
			StringBuilder str = new StringBuilder();
			
			if (isReverseRelation()) 
				str.append(getResLabel("reverse").getObject()+"  ");
			if (isAggregation()) 
				str.append(getResLabel("aggregation").getObject());
			
			str.append(getResLabel("read-only").getObject());
			
			alc.add(new Label("alert-text", str.toString()));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (getTemplateModel()!=null)
			getTemplateModel().detach();
	}
	
	public void onBeforeRender() {
		super.onBeforeRender();
		(get("form")).add(new AttributeModifier("class", "form-container " + (isReadOnly()?" readonly": "")));
	}
	
	protected void addForm() {
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		form.add(new ContentRelationEditor(getTemplateModel(), isReverseRelation()));
		add(form);
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot( getSessionUser() );
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	private User getSessionUser() {
		return  ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
}
