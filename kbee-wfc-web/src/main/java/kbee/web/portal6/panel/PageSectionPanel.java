package kbee.web.portal6.panel;


import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.portal6.IPageSectionWebPanel;
import kbee.web.portal6.event.PortalAjaxEvent;
import kbee.web.service.PortalPanelService;

public class PageSectionPanel extends PortalIWebPanel<PageSection> implements IPageSectionWebPanel {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PageSectionPanel.class.getName());

	private static final long serialVersionUID = 1L;

	WebMarkupContainer pspc;
	
	private int tab_index = -1;

	boolean reload_required = false;
	
	/**
	boolean show_archived;
	boolean show_deleted;
	boolean show_controller;
	**/
	

	
	public PageSectionPanel(String id, IModel<PageSection> model) {
			this(id, model, -1, PortalViewMode.EDIT,null);
	}
	
	public PageSectionPanel(String id, IModel<PageSection> model, int tab_index, PortalViewMode view_mode, Map<String, String>  parameters) {
		super(id, model, view_mode, parameters);
		this.tab_index=tab_index;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (getParameters()!=null) {
			//if (getParameters().containsKey(PortalAjaxEvent.ARCHIVED_VISIBLE)) 				show_archived=getParameters().get(PortalAjaxEvent.ARCHIVED_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_ARCHIVED_YES));
			//if (getParameters().containsKey(PortalAjaxEvent.DELETED_VISIBLE))				show_deleted=getParameters().get(PortalAjaxEvent.DELETED_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_DELETED_YES));
			//if (getParameters().containsKey(PortalAjaxEvent.CONTROLLER_VISIBLE))			show_controller=getParameters().get(PortalAjaxEvent.CONTROLLER_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_CONTROLLER_YES));
			
			reload_required = (getParameters().containsKey("reload") && getParameters().get("reload").equals("yes"));
		}
		
		  if (reload_required)	
			 	load();
		
	}
	
	
	@Override
	public void addListeners() {
		super.addListeners();
	}

	
	protected boolean isMoveUpEnabled() {return false;}
	protected boolean isMoveDownEnabled() {return false;}
	
	protected boolean isMoveEnabled() {return false;}
	
	protected boolean isEditEnabled() {return true;}
	protected boolean isArchiveEnabled() {return true;}
	
	protected boolean isDeleteEnabled() {return false;}

	

	
	/**
	 * Edit
	 * Add Area 
	 * 
	 * archive
	 * delete
	 * 
	 * 

	@Override
	protected List<MenuItemFactory<PageSection>> getMenuItems() {
		
		List<MenuItemFactory<PageSection>> list = super.getMenuItems();

		
		list.add(new MenuItemFactory<PageSection>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 120;
			}

			@Override
			public AbstractMenuItemPanelV5<PageSection> getItem(String id) {
				return new AjaxMenuItemPanelV5<PageSection>(id, getModel()) {
					
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							//AreaPanel.this.add(target, getModel());
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent(target, e));
						}
					}
					@Override 
					public String getLabel() {
						return "Add Area below";
					}
					
					@Override
					public boolean isEnabled() {
						return true;
						//return AreaPanel.this.isAddEnabled();
					}
				};
			}
		});
		
		
		return list;
	}
 */
	

	
	@Override
	public IModel<String> getClassInfo() {
		StringBuilder str = new StringBuilder();
		str.append("<span class=\"highlight\">"+getModel().getObject().getClassKey()+"</span>");
		str.append("  | Disposition: <span class=\"highlight\">"+getModel().getObject().getPageSectionDisposition().getLabel()+"</span>");
		str.append("  | Type: <span class=\"highlight\">"+getModel().getObject().getPageSectionType().getLabel()+"</span>");
		return new Model<String>(str.toString());
	}
	

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
		load();
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
			if (view_mode==PortalViewMode.PRODUCTION)
				return new PortalTitlePanel<PageSection>("header", getModel());
			else
				return new DummyBlockPanel("header", new Model<String>("header ->"+getModel().getObject().getTitle()),	null,"dummy-area-header");
	
		} catch (Exception e) {
			logger.error(e);
			return new PortalErrorPanel<PageSection>("header", getModel(), new Model<String>("header ->"+getModel().getObject().getTitle()));
		}
		
	}

	@Override
	public Panel getBodyPanel() {
		return getBodyPanel(getViewMode(), getParameters());
	}

	@Override
	public Panel getBodyPanel(PortalViewMode viewMode,  Map<String, String> parameters) {
		try {
			
			Panel panel= ServiceLocator.getService(PortalPanelService.class).getInternalBodyPanel("body", getModel().getObject(), viewMode, parameters);
			return panel;
			 
		} catch (Exception e) {	
			logger.error(e);							
			return new PortalErrorPanel<PageSection>("body", getModel(), new Model<String>("body ->"+getModel().getObject().getTitle()));
		}
	}

	@Override
	protected IModel<String>  getMenuLabel(String string) {
		return getLabel(getModel().getObject().getClassKey()+"-"+string);
	}

	
	private void load() {
		
		pspc = new WebMarkupContainer("ps-panel-container");
		
		if (getModel().getObject().getCss()!=null)
			this.pspc.add( new AttributeModifier("class", getModel().getObject().getCss() + " ps-panel-container"));

		
		
		xAddOrReplace(pspc);
		if (getModel().getObject().isHeader()) 
			pspc.addOrReplace(getHeaderPanel());
		else
			pspc.addOrReplace(new InvisiblePanel("header"));
		
		pspc.addOrReplace(getBodyPanel());
		
	}

}


