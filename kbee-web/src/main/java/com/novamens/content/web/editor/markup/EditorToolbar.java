package com.novamens.content.web.editor.markup;

import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;

import com.novamens.kbee.wicket.markup.html.event.EventListenerWicket;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.FloatingBehavior7;

import kbee.util.PropertiesFactory;
import kbee.web.event.wicket.EditorEvent;

@SuppressWarnings("serial")
public class EditorToolbar<T extends Content> extends Panel {
	private static final long serialVersionUID = 1L;
	private IModel<T> 	model;
	private boolean  	editionEnabled;
	private boolean 	editionMode;
	
	private static boolean workflow = Arrays.asList(PropertiesFactory.getInstance("kbee").getModules()).contains("workflow");
	
	private static final ResourceReference LOGO_IDOC 	 =  new PackageResourceReference(EditorToolbar.class, "idoc-mini.png");
	
	@SuppressWarnings("unused")
	private static final ResourceReference LOGO_KBEE 	 =  new PackageResourceReference(EditorToolbar.class, "kbee.png");

	@SuppressWarnings("unused")
	private static ResourceReference isologo;
	static {
		isologo = LOGO_IDOC;
	}
	
	
	private Panel header_right = null;
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (this.header_right==null)
			addOrReplace((new Label("header-right")).setVisible(false));
		else
			addOrReplace(this.header_right);
	}

	public void setHeaderRightPanel(Panel panel) {
		 header_right=panel;
	}
	
	
	public EditorToolbar(String id) {
		this(id, null, null, null, true);
	}
	
	/**
	 * 
	 * @param id
	 * @param header
	 * @param tools
	 * @param navigation
	 * @param editionEnabled
	 */
	public EditorToolbar(String id, Panel header, Panel tools, Panel navigation, boolean editionEnabled) {
		super(id);
		
		setOutputMarkupId(true);
		
		if (header==null)
			add((new Label("header-left")).setVisible(false));
		else
			add(header);
		
		add(new Label("title", new Model<String>() {
			public String getObject() {
				IModel<T> model = EditorToolbar.this.getModel();
				String type = model!=null ? model.getObject().getContentTemplate().getName() : "Type N/A";
				String title = type + ". id: " + (model!=null?model.getObject().getOId():"N/A");
				return title;
			}
		}));
		
		if (navigation==null)
			add((new Label("navigation").setVisible(false)));
		else {
			add(navigation);
		}	
		
		WebMarkupContainer editioncontainer = new WebMarkupContainer("edition-container") {
			@Override
			public boolean isVisible() {
				return !inWorkflow();
			}
		}; 
		
		AjaxLink<?> editionlink = new AjaxLink<Void>("edition-link") {
			public void onClick(AjaxRequestTarget target) {
				onEdit(target);
			}
			@Override
			public boolean isEnabled() {
				return getContent()!=null && !getContent().isLocked() &&  
					!(getContent().getState()==ObjectState.DELETED) &&
					ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(getContent());
			}
		};
		
		Label edition_label = new Label("edit-label", new Model<String>() {
			@Override
			public String getObject() {
				if (isEditionMode())
					return new StringResourceModel("editortoolbar.edit-close", EditorToolbar.this, null).getString();
				else
					return new StringResourceModel("editortoolbar.edit", EditorToolbar.this, null).getString();
			}
		});
		
		editionlink.add(edition_label);
		editioncontainer.add(editionlink);
		editioncontainer.setVisible(editionEnabled);
		add(editioncontainer);
		
		WebMarkupContainer startcontainer = new WebMarkupContainer("start-container") {
			@Override
			public boolean isVisible() {
				return inWorkflow();
			}
		};  
		
		AjaxLink<?> startlink = new AjaxLink<Void>("start-link") {
			public void onClick(AjaxRequestTarget target) {
				onStartWorkflow(target);
			}
			@Override
			public boolean isEnabled() {
				return getContent()!=null && !getContent().isLocked() &&  
					!(getContent().getState()==ObjectState.DELETED) &&
					ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(getContent());
			}
		};
		
		Label start_label = new Label("start-label", new Model<String>() {
			@Override
			public String getObject() {
				//if (isEditionMode())
				//	return new StringResourceModel("editortoolbar.edit-close", EditorToolbar.this, null).getString();
				//else
				return new StringResourceModel("editortoolbar.edit", EditorToolbar.this, null).getString();
			}
		});
		
		startlink.add(start_label);
		startcontainer.add(startlink);
		startcontainer.setVisible(editionEnabled);
		add(startcontainer);
		
		WebMarkupContainer toolstrigger = new WebMarkupContainer("toolstrigger");
		add(toolstrigger);
		
		if (tools!=null) {
			// tools.add(new FloatingBehavior2(toolstrigger, -10, 60));
			tools.add(new FloatingBehavior7(toolstrigger));
			add(tools);
		}
		else {
			add((new Label("menu").setVisible(false)));
		}
		
		add(new EventListenerWicket<EditorEvent>(EditorEvent.class) {
			public void onEvent(EditorEvent event) {
				event.getRequestTarget().add(EditorToolbar.this);
			}
		});
	}
	

	public void onEdit(AjaxRequestTarget target) {
	}
	
	public void onStartWorkflow(AjaxRequestTarget target) {
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public T getContent() {
		if (getModel()!=null)
			return getModel().getObject();
		return null;
	}
	
	public boolean isEditionEnabled() {
		return editionEnabled;
	}
	
	protected void setEditionMode(boolean b) {
		editionMode=b;
	}
	
	protected boolean isEditionMode() {
		return editionMode;
	}
	
	private boolean inWorkflow() {
		if (!workflow) return false;
		return getDomain().getService(WorkflowDomainService.class)!=null;
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
