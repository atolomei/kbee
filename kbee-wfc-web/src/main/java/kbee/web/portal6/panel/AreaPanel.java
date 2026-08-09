package kbee.web.portal6.panel;


import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.portal6.IAreaWebPanel;
import kbee.web.service.PortalPanelService;

		
/**
 * Structural element that renders the Area panel/s
 * 
 */
public class AreaPanel extends PortalIWebPanel<Area> implements IAreaWebPanel {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AreaPanel.class.getName());
	
	private static final long serialVersionUID = 1L;


	private WebMarkupContainer ap_c;

	private int tab_index = -1;
	
	private Integer parent_list_size = null;
			

	
	
	public AreaPanel(String id, IModel<Area> model) {
		this(id, model, -1, PortalViewMode.PRODUCTION, null);
	}
	
	public AreaPanel(String id, IModel<Area> model, PortalViewMode view_mode) {
			this(id, model, -1,view_mode, null);
	}
	
	public AreaPanel(String id, IModel<Area> model, int tab_index, PortalViewMode view_mode, Map<String, String> parameters) {
		super(id, model, view_mode, parameters);
		this.tab_index=tab_index;
	}
	
	


	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
	
		
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
	}

	
	
	/**
	 * If reached this point, the panel is not INVISIBLE
	 * Dummy -> for Title and also for Panels that are "leaves" in the tree (ie. blocks).
	 */
	@Override
	public Panel getHeaderPanel() {
			return getHeaderPanel(getViewMode(), getParameters());
	}
	
	@Override
	public Panel getHeaderPanel(PortalViewMode view_mode,  Map<String, String> parameters) {

		try {
			if (view_mode==PortalViewMode.PRODUCTION) {
				return new PortalTitlePanel<Area>("header", getModel());
			}
			else {
				return new PortalDummyBlockPanel<Area>("header", getModel());
			}
			
		} catch (Exception e) {
			logger.error(e);
			return new PortalErrorPanel<Area>("header", getModel(), e);
		}
	}
	
	@Override
	public Panel getBodyPanel() {
		return getBodyPanel(getViewMode(), getParameters());
	}
	
	@Override
	public Panel getBodyPanel(PortalViewMode viewMode,  Map<String, String> parameters) {
		try {
			Panel panel = ServiceLocator.getService(PortalPanelService.class).getInternalBodyPanel("body",  getModel().getObject(), viewMode,  parameters); 
			return panel;
		}
		catch (Exception e) {
			logger.error(e);
			return new PortalErrorPanel<Area>("body", getModel(), e);
		}
	}
	
	@Override
	public IModel<String> getClassInfo() {
		StringBuilder str = new StringBuilder();
		str.append("<span class=\"highlight\">"+getModel().getObject().getClassKey()+"</span>");
		str.append("  | Type: <span class=\"highlight\">"+getModel().getObject().getAreaType().getLabel()+"</span>");
		return new Model<String>(str.toString());
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
				
		setOutputMarkupId(true);
		
		parent_list_size = null;
				
		this.ap_c = new WebMarkupContainer("area-panel-container");
		xAdd(this.ap_c);
		
		if (getModel().getObject().getCss()!=null)
			this.ap_c.add( new AttributeModifier("class", getModel().getObject().getCss() + " area-panel-container"));
		
		
		if (getModel().getObject().isHeader()) 
			this.ap_c.add(getHeaderPanel());
		else
			this.ap_c.add(new InvisiblePanel("header"));
		
		this.ap_c.add(getBodyPanel());
		
	
		String css = getModel().getObject().getCss();
		if (css!=null)
			this.ap_c.add( new AttributeModifier("class", css + " area-panel-container"));
		
		
		
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	/**
	 * Edit
	 * Add Area Below
	 * 
	 * Add Block
	 * Move up
	 * Move down
	 * 
	 * archive
	 * delete
	 * 
	 * 
	 */
	@Override
	protected List<MenuItemFactory<Area>> getMenuItems() {
		
		List<MenuItemFactory<Area>> list = super.getMenuItems();

		
		list.add(new MenuItemFactory<Area>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 120;
			}

			@Override
			public AbstractMenuItemPanelV5<Area> getItem(String id) {
				return new AjaxMenuItemPanelV5<Area>(id, getModel()) {
					
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							AreaPanel.this.add(target, getModel());
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<>(target, e));
						}
					}
					@Override 
					public String getLabel() {
						return "Add Area below";
					}
					
					@Override
					public boolean isEnabled() {
						return AreaPanel.this.isAddEnabled();
					}
				};
			}
		});
		
		
		
		return list;
		
	}
	

	
	protected IModel<String>  getMenuLabel(String string) {
		return getLabel(getModel().getObject().getClassKey()+"-"+string);
	}
	

	protected int getParentInstances() {
		if (parent_list_size!=null)
			return parent_list_size.intValue();
	   parent_list_size = Integer.valueOf ( (( com.novamens.portal6.model.PageSection) getModel().getObject().getParent()).getAreas().size());
	   return parent_list_size.intValue(); 
	}
	
	protected boolean isMoveUpEnabled()   {return true; } // return getModel().getObject().getOrder()>0;
	protected boolean isMoveDownEnabled() {return true ;} // getModel().getObject().getOrder()<(getParentInstances()-1)
	
	
	protected boolean isEditEnabled() {return true;}
	protected boolean isArchiveEnabled() {return getModel().getObject().getState()!=ObjectState.ARCHIVED;}
	protected boolean isDeleteEnabled() {return getModel().getObject().getState()!=ObjectState.DELETED;}
	

}
