package com.novamens.content.web.editor.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.FloatingBehavior7;


@SuppressWarnings("serial")
public class ObjectEditorToolbar<T> extends Panel {

	private static final long serialVersionUID = 1L;

	
	Panel header_right = null;
	
	private IModel<String> title;

	
	public void setHeaderRightPanel(Panel panel) {
		 header_right=panel;
	}
	

	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (this.header_right==null)
			addOrReplace((new Label("header-right")).setVisible(false));
		else
			addOrReplace(this.header_right);
	}
	
	
	/** ----------------------------------------------------------------------------------------------
	 */ 
	public ObjectEditorToolbar(String id) {
		this(id, null, null, null, null);
	}
	
	public ObjectEditorToolbar(String id, final IModel<T> model, Panel header, Panel tools, Panel navigation) {
		this(id, model, header, tools, navigation, null);
	}
	/** ----------------------------------------------------------------------------------------------
	 * 
	 * @param id
	 * @param model
	 * @param header
	 * @param tools
	 * @param navigation
	 * 
	 */
	public ObjectEditorToolbar(String id, final IModel<T> model, Panel header,  Panel tools, Panel navigation, Panel header_right) {
		super(id);
		
		setOutputMarkupId(true);
		
		if (header==null)
			add((new Label("header-left")).setVisible(false));
		else
			add(header);

		this.header_right =  header_right;
		
		Label titlelabel = new Label("title", new Model<String>() {
				@Override
				public String getObject() {
					return getTitle().getObject();
				}
			}) 
			{
				@Override
				public boolean isVisible() {
					return getTitle()!=null;
				}
			};
		
		add(titlelabel);
		
		if (navigation==null)
			add((new Label("navigation").setVisible(false)));
		else {
			add(navigation);
		}	
		
		AjaxLink<?> editionlink = new AjaxLink<Void>("edition-link") {
			public void onClick(AjaxRequestTarget target) {
				onEdit(target);
			}
			@Override
			public boolean isEnabled() {
				return ObjectEditorToolbar.this.isEditEnabled() && 
					(ServiceLocator.getService(SecurityService.class).isRoot() || !ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId()));
			}
			
			//
			// PONER EN FALSE PARA PASAR TODO A BSTRAP
			//@Override
			//public boolean isVisible() {
			//	return false;
			//}
		};
		
		
		
		
		Label edit_label = new Label("edit-label", new Model<String>() {
			@Override
			public String getObject() {
				return new StringResourceModel("object.editor.toolbar.edit", ObjectEditorToolbar.this, null).getString();
			}
		});

		editionlink.add(edit_label);
		add(editionlink);
		
		WebMarkupContainer toolstrigger = new WebMarkupContainer("toolstrigger");
		add(toolstrigger);
		
		if (tools!=null) {
			//tools.add(new FloatingBehavior2(toolstrigger, 15, 10));
			tools.add(new FloatingBehavior7(toolstrigger));
			add(tools);
		} else {
			toolstrigger.setVisible(false);
			add((new Label("menu").setVisible(false)));
		}
	}

	protected boolean isEditionMode() {
		return false;
	}

	public void setTitle(IModel<String> title) {
		this.title=title;
	}
	
	public IModel<String> getTitle() {
		return title;
	}
	
	public void onDetach() {
		if (title!=null)
			title.detach();
		super.onDetach();
	}
	
	public boolean isEditEnabled() {
		return true;
	}
	
	public void onEdit(AjaxRequestTarget target) {
	}
}
