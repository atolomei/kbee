package com.novamens.kbee.wicket.markup.html.console.browser;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;

import com.novamens.content.user.UserService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

/**
 *  
 * Grid Toolbar
 * 
 */
@SuppressWarnings("serial")
public class Toolbar extends KBPanel {
	private static final long serialVersionUID = 1L;

	private List<ToolbarItem> items;
	
	private List<ToolbarItem> tl_items;
	private List<ToolbarItem> tr_items;

	private List<ToolbarItem> br_items;
	private List<ToolbarItem> bl_items;
	
	private String ccs_tnav = "navbar grid-toolbar";
	private String ccs_tn = "col-lg-12 col-xs-12 col-md-12 top-toolbar";
	private String ccs_bn = "col-lg-12 col-xs-12 col-md-12 bottom-toolbar";
	
	private String theme = "";
	
	
	public Toolbar(String id, List<ToolbarItem> items) {
		super(id);
		setItems(items);
		setOutputMarkupId(true);
	}
	
	public void setItems(List<ToolbarItem> items) {
		this.items = items;
	}
	
	public List<ToolbarItem> getItems() { 
		return items;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addItems();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	
		tl_items=null;
		tr_items=null;
		
		if (items!=null) {
			for (ToolbarItem item: items) 
				item.detach();
		}
		
		bl_items=null;
		br_items=null;
	}

	
	protected List<ToolbarItem> getTopRightItems() {

		if (tr_items!=null)
			return tr_items;
		
		 tr_items = new ArrayList<ToolbarItem>();

		 for (ToolbarItem item : getItems()) {
			if (item.getAlign().equals(Align.TOP_RIGHT)) 
				 tr_items.add(item);
		}
		return tr_items;
	}
	
									
	protected List<ToolbarItem> getBottomRightItems() {

		if (br_items!=null)
			return br_items;
		
		 br_items = new ArrayList<ToolbarItem>();
		
		 for (ToolbarItem item : getItems()) {
			if (item.getAlign().equals(Align.BOTTOM_RIGHT)) 
				 br_items.add(item);
		}
		return br_items;
	}
	
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}

	/**
	 * @return
	 */
	protected List<ToolbarItem> getTopLeftItems() {

		if (tl_items!=null)
			return tl_items;
		
		tl_items = new ArrayList<ToolbarItem>();
		
		for (ToolbarItem item : getItems()) {
			if (item.getAlign().equals(Align.TOP_LEFT) || item.getAlign().equals(Align.TOP_NONE)) 
				tl_items.add(item);
		}
		return tl_items;
	}
	
									
	protected List<ToolbarItem> getBottomLeftItems() {

		if (bl_items!=null)
			return bl_items;
		
		bl_items = new ArrayList<ToolbarItem>();
		
		for (ToolbarItem item : getItems()) {
			if (item.getAlign().equals(Align.BOTTOM_LEFT) || item.getAlign().equals(Align.BOTTOM_NONE)) 
				bl_items.add(item);
		}
		return bl_items;
	}

	
	
	public String getCssToolbarNavBar() {
		return ccs_tnav;
	}

	public String getCssTopToolBar() {
		return ccs_tn;
	}

	
	public String getCssBottomToolBar() {
		return ccs_bn;
	}

	
	public void setGlobalCss(String theme) {
		this.theme=theme;
	}
	
	public String getGlobalCss() {
		return this.theme;
	}
	
	
	protected void addItems() {

		WebMarkupContainer tnav=new WebMarkupContainer("toolbar-navbar");
		
		tnav.add(new AttributeModifier("class", new Model<String>() {
					public String getObject() {
						return getCssToolbarNavBar() + " " + getGlobalCss() + (isTwoRows() ? " toolbar-two-rows" :  " toolbar-one-row");
					}
				}));
		add(tnav);
		
							
		WebMarkupContainer tn=new WebMarkupContainer("top-toolbar") {
			@Override
			public boolean isVisible() {
				List<ToolbarItem> l1=getTopRightItems();
				List<ToolbarItem> l2=getTopLeftItems();
				return  (l1!=null && l1.size() > 0) || 
						(l2!=null && l2.size() > 0);
			}
		};

		tn.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getCssTopToolBar() ;
			}
		}));

		
		tn.add(new ListView<ToolbarItem>("item-left", getTopLeftItems()) {
			public void populateItem(ListItem<ToolbarItem> item) {
				item.add(item.getModelObject());
			}
		});
		
		tn.add(new ListView<ToolbarItem>("item-right", getTopRightItems()) {
			public void populateItem(ListItem<ToolbarItem> item) {
				item.add(item.getModelObject());
			}
		});
		
		tnav.add(tn);
		
		
		WebMarkupContainer bn = new WebMarkupContainer("bottom-toolbar") {
			@Override
			public boolean isVisible() {
				List<ToolbarItem> l1 = getBottomRightItems();
				List<ToolbarItem> l2 = getBottomLeftItems();
				boolean visible = false;
				for (ToolbarItem ti : l1) {
					visible = ti.isVisible();
					if (visible) break;
				}
				if (!visible)
				for (ToolbarItem ti : l2) {
					visible = ti.isVisible();
					if (visible) break;
				}
				return  visible && ((l1!=null && l1.size()>0) || (l2!=null && l2.size()>0));
			}
		};

		bn.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getCssBottomToolBar();
			}
		}));

		
		tnav.add(bn);
		
		bn.add(new ListView<ToolbarItem>("item-bottom-left", getBottomLeftItems()) {
			public void populateItem(ListItem<ToolbarItem> item) {
				item.add(item.getModelObject());
			}
		});
		
		bn.add(new ListView<ToolbarItem>("item-bottom-right", getBottomRightItems()) {
			public void populateItem(ListItem<ToolbarItem> item) {
				item.add(item.getModelObject());
			}
		});
		
	}
		
	
	protected boolean isTwoRows() {
		List<ToolbarItem> l2=getBottomLeftItems();
		return   (l2!=null && l2.size() > 0);
	}
}
