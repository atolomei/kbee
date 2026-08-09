package com.novamens.wicket.markup.html.actions;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.console.panel.MenuPanel;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

@SuppressWarnings("serial")
public class SideMenuPanel<T> extends KBPanel implements MenuPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private IModel<T> model;
	private int index;
	private boolean expanded = false;
	
	
	private List<MenuItemFactory<T>> items = new ArrayList<MenuItemFactory<T>>();
										
	private  final String product = ServiceLocator.getService(BrandingService.class).getProductKey();
	
	public SideMenuPanel(IModel<T> model) {
		this("menu", model);
	}
	
	
	/**
	 
	 *  CURATORIALVN@GMAIL.COM
	 
	 * @param id
	 * @param model
	 */
	public SideMenuPanel(String id, IModel<T> model) {
		super(id);
	
		setModel(model);

		setOutputMarkupId(true);
		
		ListView<MenuItemFactory<T>> itemsview = new ListView<MenuItemFactory<T>>("item", this.items) {
			public void populateItem(ListItem<MenuItemFactory<T>> factoryitem) {
				AbstractMenuItemPanelV5<T> item = factoryitem.getModelObject().getItem("panel");
				item.setOutputMarkupId(true);
				item.setMarkupId("panel"+factoryitem.getIndex());
				item.setVisible(factoryitem.isVisible());
				item.setIndex(getIndex());
				item.setModel(new IModel<T>() {
					public void setObject(T object) {
						SideMenuPanel.this.getModel().setObject(object);
					}
					public T getObject() {
						return SideMenuPanel.this.getModel().getObject();
					}
					public void detach() {
						if (SideMenuPanel.this.getModel()!=null)
							SideMenuPanel.this.getModel().detach();
					}
				});
				
				if (item.getCssClass()!=null)
					factoryitem.add(new AttributeModifier("class", item.getCssClass()));
				
				factoryitem.add(item);
				factoryitem.setVisible(item.isVisible());
			}
		};
		
		
		WebMarkupContainer cont = new WebMarkupContainer("sidebar-container");
		add(cont);
		
		cont.add(new AttributeModifier("class", "sidebar sidebar-main " + getUitheme()));
		cont.add(itemsview);
	}
	
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public IModel<T> getModel() {
		return this.model;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public List<MenuItemFactory<T>> getItems() {
		return this.items;
	}
	
	@Override
	public boolean isEmpty() {
		return getItems().isEmpty();
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public void addItem(MenuItemFactory<T> item) {
		this.items.add(item);
	}	 
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		if (expanded)
			 response.render(OnDomReadyHeaderItem.forScript("$('body').addClass('sidebar-expanded');"));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.model!=null) 
			this.model.detach();
	}
	
	protected String getUitheme() {
		return "brand-"+product;
	}
}
