package kbee.web.portal6.editor;



import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.portal.model.KbeePortalMenu;
import com.novamens.kbee.portal.model.KbeePortalMenuItem;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal6.model.PortalMenu;
import com.novamens.portal6.model.PortalMenuItem;
import com.novamens.portal6.model.PortalPersistentMenu;

import kbee.web.panel.ListSimplePanel;
import kbee.web.portal6.event.PortalMenuEditEvent;
import kbee.web.portal6.event.PortalMenuEditUpdateEvent;
import kbee.web.portal6.panel.PortalErrorPanel;
import kbee.web.portal6.panel.PortalPanel;


public class PortalMenuItemListPanel extends PortalPanel<PortalMenu> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalMenuItemListPanel.class.getName());
	
	/**
	 */

	private static final long serialVersionUID = 1L;
	
	private boolean is_editing = false;
	private WebMarkupContainer addc;
	
	private List<IModel<PortalMenuItem>> list_model;
	private  IModel<PortalPersistentMenu> model_owner;
	
	
	public PortalMenuItemListPanel(String id, IModel<PortalMenu> model, IModel<PortalPersistentMenu> model_owner, boolean is_editing) {
		super(id, model);
		setOutputMarkupId(true);
		this.is_editing = is_editing;
		this.model_owner=model_owner;
	}
	

	public void onInitialize() {
		super.onInitialize();
		logger.debug("onInitialize() -> " +  getModel().getObject().getDisplayName() );
		
		addc = new WebMarkupContainer("add-container") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return is_editing;
			}
		};
		add(addc);
				
		AjaxLink<PortalMenu> add_submenu = new AjaxLink<PortalMenu>("add-submenu", PortalMenuItemListPanel.this.getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					PortalMenuItem pmi = new KbeePortalMenu("sub menu " + String.valueOf(PortalMenuItemListPanel.this.getModel().getObject().getPortalMenuItems().size()));
					PortalMenuItemListPanel.this.getModel().getObject().add(pmi);
					target.add(PortalMenuItemListPanel.this);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		};
		addc.add(add_submenu);

		
		AjaxLink<PortalMenu> add_item = new AjaxLink<PortalMenu>("add-labelitem", PortalMenuItemListPanel.this.getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					PortalMenuItem pmi = new KbeePortalMenuItem("item " + String.valueOf(PortalMenuItemListPanel.this.getModel().getObject().getPortalMenuItems().size()));
					PortalMenuItemListPanel.this.getModel().getObject().add(pmi);
					target.add(PortalMenuItemListPanel.this);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		};
		addc.add(add_item);
		
		
		
		
		String key = "menu/"+getModel().getObject().getLabel()+"/items";
		
		ListSimplePanel<PortalMenuItem> itm = new ListSimplePanel<PortalMenuItem>("menu-items", key, getModelItems()) {
			
			private static final long serialVersionUID = 1L;
			@Override
			 public WebMarkupContainer getExpandedPanel(String id, IModel<PortalMenuItem> model) {
					
				return getItemPanel(id, model);
			}

			protected IModel<String> getItemLabel(IModel<PortalMenuItem> modelObject) {
				return new Model<String>(modelObject.getObject().getDisplayName() + ((modelObject.getObject() instanceof PortalMenu) ?
						(" <span class=\"ago atright\">( sub-menu )</span>") : ""));
			}
			
			protected boolean isExpanderEnabled(IModel<PortalMenuItem> model) {
				return true;
			}
			
			@Override
			protected void onClick(IModel<PortalMenuItem> modelObject, int index) {
			}
		}; 
		
		
		itm.setMenu(true);
		itm.setExpand(true);
		
		add(itm);

	}
	
	

	public void onDetach() {
		super.onDetach();
		if (list_model!=null) {
			for (IModel<PortalMenuItem> m:list_model)
				m.detach();
		}
	}

	public List<IModel<PortalMenuItem>> getModelItems() {
		
		if (list_model!=null)
			return list_model;
		
	 list_model = new ArrayList<IModel<PortalMenuItem>>();
		for (PortalMenuItem it:getModel().getObject(). getPortalMenuItems()) {
			list_model.add( new Model<PortalMenuItem>(it));
		}
		return list_model;
	}
	
	
	public void setEdition(boolean b) {
		this.is_editing=b;
	}
	


	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<PortalMenuEditEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalMenuEditEvent event) {
					if (event.isEditing()) {
						is_editing = true;
					}
					else
						is_editing = false;
					
					logger.debug(getModel().getObject().getDisplayName() + " edit: " + (event.isEditing()?"true":"false"));
			}
		});
		
		add(new WicketEventListener<PortalMenuEditUpdateEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalMenuEditUpdateEvent event) {
				 // PortalMenuItemListPanel.this.update(event.getRequestTarget());
				logger.debug(" PortalMenuEditUpdateEvent -> " + getModel().getObject().getDisplayName());
			}
		});
		
	}
	
	
	protected WebMarkupContainer getItemPanel(String id, IModel<PortalMenuItem> model) {
		try { 
				if (model.getObject() instanceof PortalMenu) {
					PortalSubMenuEditor  pa=new PortalSubMenuEditor (id, new Model<PortalMenu>((PortalMenu) model.getObject()), model_owner,  is_editing);
					pa.add(new AttributeModifier("style", "padding-left: 68px; margin-top: 30px;"));
					return pa;
				}
				else {
					PortalMenuItemEditor  pa=new PortalMenuItemEditor (id, new Model<PortalMenuItem>((PortalMenuItem) model.getObject()), is_editing);
					pa.add(new AttributeModifier("style", "padding-left: 68px; margin-top: 30px;"));
					return pa;


				}
				
		} catch (Exception e) {
			return new PortalErrorPanel<>(id, e);	
		}
	}





	protected void onExpand() {
	}
	
	
	
}
