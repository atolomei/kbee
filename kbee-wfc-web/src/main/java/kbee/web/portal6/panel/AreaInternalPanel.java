package kbee.web.portal6.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.AreaSection;
import com.novamens.portal6.model.AreaType;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.wicket.model.ListModel;



import kbee.web.portal6.PortalObjectViewerRenderService;
import kbee.web.portal6.event.PortalAjaxEvent;

public class AreaInternalPanel extends PortalPanel<Area> {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AreaInternalPanel.class.getName());

	private int tab_index = -1;
	
	private WebMarkupContainer ap_left;
	private WebMarkupContainer ap_center;
	private WebMarkupContainer ap_right;
	private PortalViewMode view_mode;


	 
	private boolean show_archived = false;
	private boolean show_deleted = false;
	private	boolean show_controller;
		
	 
	public AreaInternalPanel(String id, IModel<Area> model, PortalViewMode view_mode,  Map<String, String> parameters) {
		super(id, model, parameters);
		this.view_mode=view_mode;
		//this.parameters= parameters;
	}

	public PortalViewMode getViewMode() {
		return this.view_mode;
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (getParameters()!=null) {
			if (getParameters().containsKey(PortalAjaxEvent.ARCHIVED_VISIBLE)) 				show_archived=getParameters().get(PortalAjaxEvent.ARCHIVED_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_ARCHIVED_YES));
			if (getParameters().containsKey(PortalAjaxEvent.DELETED_VISIBLE))				show_deleted=getParameters().get(PortalAjaxEvent.DELETED_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_DELETED_YES));
			if (getParameters().containsKey(PortalAjaxEvent.CONTROLLER_VISIBLE))			show_controller=getParameters().get(PortalAjaxEvent.CONTROLLER_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_CONTROLLER_YES));
		}
		
		ap_left = getSubSectionPanel("block-list-left", new ListModel<Block>(new Model<Panel>(AreaInternalPanel.this), "blocksLeft"), AreaSection.LEFT, getModel().getObject().getAreaType().getSubSectionLeftCss());		
		
		if (getModel().getObject().getAreaType()==AreaType.AREA_3S_3x33) 
			ap_center = getSubSectionPanel("block-list-center", new ListModel<Block>(new Model<Panel>(AreaInternalPanel.this), "blocksCenter"), AreaSection.CENTER, getModel().getObject().getAreaType().getSubSectionCenterCss());
		
		
		else if (getModel().getObject().getAreaType()==AreaType.AREA_3S_40x40x20) 
			ap_center = getSubSectionPanel("block-list-center", new ListModel<Block>(new Model<Panel>(AreaInternalPanel.this), "blocksCenter"), AreaSection.CENTER, getModel().getObject().getAreaType().getSubSectionCenterCss());
		
		else if (getModel().getObject().getAreaType()==AreaType.AREA_3S_20x40x40) 
			ap_center = getSubSectionPanel("block-list-center", new ListModel<Block>(new Model<Panel>(AreaInternalPanel.this), "blocksCenter"), AreaSection.CENTER, getModel().getObject().getAreaType().getSubSectionCenterCss());
		
		else 
			ap_center = getVoidPanel("block-list-center");
		
		
		if (getModel().getObject().getAreaType()!=AreaType.AREA_1S) 
			ap_right 	= getSubSectionPanel("block-list-right", new ListModel<Block>(new Model<Panel>(AreaInternalPanel.this), "blocksRight"), AreaSection.RIGHT, getModel().getObject().getAreaType().getSubSectionRightCss());		
		
		else 
			ap_right = getVoidPanel("block-list-right");
		
		add(ap_left);
		add(ap_center);
		add(ap_right);
	}

	
	public List<Block> getBlocks() {
		List<Block> list = getModel().getObject().getBlocks(tab_index);
		if (list==null)
			return new ArrayList<Block>();
		return list;
	}
	
	public List<Block> getBlocksCenter() {
		List<Block> list = getModel().getObject().getBlocks(tab_index);
		List<Block> ret = new ArrayList<Block>();
		if (list==null)
			return ret;
		for(Block b:list) {
			
			
			if  (b.getAreaSection()==AreaSection.CENTER) {
				if (b.getState()==ObjectState.ENABLED)	ret.add(b);
				if (b.getState()==ObjectState.ARCHIVED && this.show_archived)	ret.add(b);
				if (b.getState()==ObjectState.DELETED && this.show_deleted)		ret.add(b);
			}
		}
		return ret;
	}

	public List<Block> getBlocksRight() {
		List<Block> list = getModel().getObject().getBlocks(tab_index);
		List<Block> ret = new ArrayList<Block>();
		if (list==null)
			return ret;
		for(Block b:list) {
			if  (b.getAreaSection()==AreaSection.RIGHT) {
				if (b.getState()==ObjectState.ENABLED)	ret.add(b);
				if (b.getState()==ObjectState.ARCHIVED && this.show_archived)	ret.add(b);
				if (b.getState()==ObjectState.DELETED && this.show_deleted)		ret.add(b);
			}
		}
		return ret;
	}
	
	public List<Block> getBlocksLeft() {
		List<Block> list = getModel().getObject().getBlocks(tab_index);
		List<Block> ret = new ArrayList<Block>();
		if (list==null)
			return ret;
		for(Block b:list) {
			if  (b.getAreaSection()==AreaSection.LEFT) {
				if (b.getState()==ObjectState.ENABLED)	ret.add(b);
				if (b.getState()==ObjectState.ARCHIVED && this.show_archived)	ret.add(b);
				if (b.getState()==ObjectState.DELETED && this.show_deleted)		ret.add(b);
			}
		}
		return ret;
	}
	
 
	
	

	/**
	 *
	 * ListModel<Block> lm = new ListModel<Block>(new Model<Panel>(this), "blocks");
	 * 
	 * @param id
	 * @return
	 */
	private WebMarkupContainer getSubSectionPanel(String id, ListModel<Block> lm, AreaSection area_section, String css) {

		css=css+" block-list-container";
		
		WebMarkupContainer su = new WebMarkupContainer(id);
		su.add(new AttributeModifier("class", css));
		
		try {
				
				org.apache.wicket.markup.html.list.ListView<Block> lp = new ListView<Block>("block", lm) {
					private static final long serialVersionUID = 1L;
					@Override
					protected void populateItem(ListItem<Block> item) {
						try {
							Block a=item.getModelObject();
							Panel panel = a.getService(PortalObjectViewerRenderService.class).build("block-panel", -1, getViewMode(), getParameters());
							item.add(panel);
							item.setOutputMarkupId(true);
						} 
						catch (Exception e) {
							logger.error(e);
							item.addOrReplace(new PortalErrorPanel<Area>("area-panel", e));
							
						}	
					}
				};
				
				su.add(lp);
				
				if (show_controller)
					su.add(new PortalAreaSectionBottomBar("bottom-bar", getModel(), area_section));
				else
					su.add(new InvisiblePanel("bottom-bar"));
				
				
		} catch (Exception e) {
			logger.error(e);
			su.addOrReplace(new PortalErrorPanel<Area>("blocks", e));
		}
		
		return su;
		
	}
	
	protected Panel getVoidPanel(String id) {
		return getVoidPanel(id, null, null);
  }

	protected Panel getVoidPanel(String id, IModel<String> lefts, IModel<String> rights) {
			return new InvisiblePanel(id);
	}
}
