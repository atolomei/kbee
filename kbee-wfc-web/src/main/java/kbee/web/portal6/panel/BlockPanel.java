package kbee.web.portal6.panel;


import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.AreaSection;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.portal6.IBlockWebPanel;
import kbee.web.portal6.event.PortalAjaxEvent;
import kbee.web.service.PortalPanelService;


/**
 * Structural element that renders the Block payload
 * 
 */
public class BlockPanel extends PortalIWebPanel<Block> implements IBlockWebPanel {
			
	private static final long serialVersionUID = 1L;

	private int tab_index = -1;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BlockPanel.class.getName());
	
	private WebMarkupContainer bpc;

	private Integer parent_list_size = null;

	/**
	private boolean show_archived = false;
	private boolean show_deleted = false;
	private	boolean show_controller;
	private	boolean show_payload = true;
	**/
	
	public BlockPanel(String id, IModel<Block> model) {
		this(id, model, -1, PortalViewMode.PRODUCTION, null);
	}

	public BlockPanel(String id, IModel<Block> model, int tab_index, PortalViewMode view_mode,  Map<String, String>  parameters) {
		super(id, model, view_mode, parameters);
		this.tab_index=tab_index;
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
	}
	
	/**
	 * If called -> the panel is not INVISIBLE
	 * Dummy -> for Title and also for Panels that are "leaves" in the tree (ie. blocks).
	 * Only Blocks can render a Dummy for Body, because they are leaves of the Portal Tree
	 */
	@Override
	public Panel getHeaderPanel() {
			return getHeaderPanel(getViewMode(),  getParameters());
	}
	
	/**
	 * If called -> the panel is not INVISIBLE
	 */
	@Override
	public Panel getHeaderPanel(PortalViewMode viewMode, Map<String, String> parameters) {
		try {
			Panel panel = ServiceLocator.getService(PortalPanelService.class).getInternalHeaderPanel("header", getModel().getObject(), viewMode, parameters);
			return panel;
		} catch (Exception e) {
			logger.error(e);					
			return new PortalErrorPanel<Block>("header", getModel(), e);
		}
	}

	/**
	 * If called -> the panel is not INVISIBLE
	 */
	@Override
	public Panel getBodyPanel() {
		return getBodyPanel(getViewMode(),  getParameters());
	}
	
	
	/**
	 * If called -> the panel is not INVISIBLE
	 */
	@Override
	public Panel getBodyPanel(PortalViewMode viewMode, Map<String, String> parameters) {
		try {
				Panel panel = ServiceLocator.getService(PortalPanelService.class).getInternalBodyPanel("body", getModel().getObject(), viewMode, parameters);
				return panel;
		} catch (Exception e) {
			logger.error(e);					
			return new PortalErrorPanel<Block>("body", getModel(), e);
		}
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
	 
		parent_list_size = null;
		
		bpc	= new WebMarkupContainer("block-panel-container");
		xAdd(bpc);
		
		
		if (getModel().getObject().getCss()!=null)
			bpc.add( new AttributeModifier("class", getModel().getObject().getCss() + " block-panel-container"));
		
		
		
		try {
			if (getModel().getObject().isHeader()) 
				bpc.add(getHeaderPanel());
			else
				bpc.add(getVoidPanel("header"));
		}
			catch (Exception e) {
				logger.error(e);
				bpc.addOrReplace(new PortalErrorPanel<Block>("body", getModel(), e));
		}	
		try {
			bpc.add(getBodyPanel(getViewMode(), getParameters()));
		} 
		catch (Exception e) {
					bpc.addOrReplace(new PortalErrorPanel<Block>("body", getModel(), e));		
					logger.error(e);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public IModel<String> getClassInfo() {
		StringBuilder str = new StringBuilder();
		str.append("<span class=\"highlight\">"+getModel().getObject().getClassKey()+"</span>");
		return new Model<String>(str.toString());
		
	}
	@Override
	protected IModel<String>  getMenuLabel(String string) {
		return new StringResourceModel("block-"+string, BlockPanel.this, null);
	}
	
	@Override
	protected boolean isAddEnabled() {
		return false;
	}
	
	
	protected int getParentInstances() {
		
		if (parent_list_size!=null)
			return parent_list_size.intValue();
		try {
			Area area =(com.novamens.portal6.model.Area) getModel().getObject().getParent();
			if (area!=null) {
				AreaSection as = getModel().getObject().getAreaSection();
				
				List<Block> list = area.getBlocks(as);
				if (list!=null) {
					parent_list_size = Integer.valueOf (list.size());
					return parent_list_size.intValue();
				}
			}
			
			parent_list_size = Integer.valueOf(0);
			
			
		} catch (Exception e) {
			logger.error(e);
			parent_list_size = Integer.valueOf(0);
		}
		
		   return parent_list_size.intValue();
	   
	}
	
	protected boolean isMoveUpEnabled() 	{
	
		Area a=((Area) getModel().getObject().getParent());
		if (a!=null)
			return a.canMoveUp(getModel().getObject());
		logger.error("parent is null  for -> " + getModel().getObject().toString());
		return false;
		
	}
	protected boolean isMoveDownEnabled() 	{
	
		Area a=((Area) getModel().getObject().getParent());
		if (a!=null)
			return a.canMoveDown(getModel().getObject());
		logger.error("parent is null  for -> " + getModel().getObject().toString());
		return false;

	}
	
	protected boolean isEditEnabled() 		{return true;}
	protected boolean isArchiveEnabled() 	{return getModel().getObject().getState()!=ObjectState.ARCHIVED;}
	protected boolean isDeleteEnabled() 	{return getModel().getObject().getState()!=ObjectState.DELETED;}
	

	
	@Override
	protected List<MenuItemFactory<Block>> getMenuItems() {
		
		List<MenuItemFactory<Block>> list = super.getMenuItems();

		list.add(new MenuItemFactory<Block>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 220;
			}

			@Override
			public AbstractMenuItemPanelV5<Block> getItem(String id) {
				return new AjaxMenuItemPanelV5<Block>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							BlockPanel.this.add(target, getModel());
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<>(target, e));
						}
					}
					@Override 
					public String getLabel() {
						return "Add Block below";
					}
					
					@Override
					public boolean isEnabled() {
						return BlockPanel.this.isAddEnabled();
					}
				};
			}
		});

 
		
		return list;
	}
	
	


}
