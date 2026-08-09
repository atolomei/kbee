package com.novamens.wicket.markup.html.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.util.InvisiblePanel;


@SuppressWarnings("serial")
public class SubmenuAjaxItemPanelV5<T> extends AjaxMenuItemPanelV5<T> {
	
	private static final long serialVersionUID = 1L;
	
	private List<MenuItemFactory<T>> items = new ArrayList<MenuItemFactory<T>>();

	String  dropdownid;
	boolean is_created=false;
	
	
	public void addListeners() {
		
	}
	
	
	public SubmenuAjaxItemPanelV5(String id) {
		this(id, null, null, null);
	}
	
	public SubmenuAjaxItemPanelV5(String id, IModel<T> model) {
		this(id, model, null, null);
	}
	
	public SubmenuAjaxItemPanelV5(String id,  String iconcss) {
		this(id, null, iconcss);
	}
	
	public SubmenuAjaxItemPanelV5(String id, IModel<T> model, String iconcss) {
		this(id, model, iconcss, null);
	}

	public SubmenuAjaxItemPanelV5(String id, IModel<T> model, String iconcss, String dropdownid) {
		super(id, iconcss);
		this.dropdownid=dropdownid;
		setModel(model);
		setOutputMarkupId(true);
		addListeners();
	}

	
	protected List<MenuItemFactory<T>> getItems() {
		return this.items;
	}
	
	public void addItem(MenuItemFactory<T> item) {
		this.items.add(item);
	}	 

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new AttributeModifier("class", "dropdown-submenu"));
		
		WebMarkupContainer menu_c = new WebMarkupContainer("menu-container");
		
		menu_c.setOutputMarkupId(true);
		WebMarkupContainer menu = new WebMarkupContainer("menu");
		
		if (getMenuStyle()!=null) 
			menu.add( new AttributeModifier("style", getMenuStyle()));
		
		menu.setVisible(false);
		menu.setOutputMarkupId(true);
		
		setIconCssClass("far fa-angle-up toright");
		
		if (dropdownid!=null) 
			menu.add(new AttributeModifier("id", dropdownid));
			
		menu_c.add(menu);
		add(menu_c);
		
		menu.add(new InvisiblePanel("(item"));
	}
	
	
	protected String getMenuStyle() {
		return null;
	}


	@Override
	public String getBeforeClick() {
		return null;
	}
	
	@Override
	public String getCssClass() {
		return null;
	}
	
	@Override
	protected void onAfterRender() {
		super.onAfterRender();
	}

	@Override
	public String getLabel() {
		return null;
	}

	public void onDetach() {
		super.onDetach();
		if (items!=null)
			items.forEach(i-> i.detach());
		
		
	}
	
	@Override
	public void onClick(AjaxRequestTarget target) throws Exception {
	
		boolean menu_visible = get("menu-container:menu").isVisible();
		
		get("menu-container:menu").setVisible(!menu_visible);
		
		if (get("menu-container:menu").isVisible()) {
			if (!is_created)  
				addSubmenuPanel();
		}
		
		target.add(get("menu-container"));
		target.add(get("lcontainer"));

		if (get("menu-container:menu").isVisible())
			setIconCssClass("far fa-angle-down toright");
		else
			setIconCssClass("far fa-angle-up toright");
	}
	
	
	
	@Override
	protected AbstractLink getNewLink(String id) {
		AjaxLink<?> link = new AjaxLink<Void>(id) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					SubmenuAjaxItemPanelV5.this.onClick(target);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setEventPropagation(EventPropagation.STOP); 
			}
		};
		return link;
	}
	

	
	
	protected void addItems() {
		
	}
	
	protected boolean isListViewCreated() {
		return this.is_created;
	}

	
	private void addSubmenuPanel() {
	
		addItems();
		
		ListView<MenuItemFactory<T>> itemsview = new ListView<MenuItemFactory<T>>("item", getItems()) {
			public void populateItem(ListItem<MenuItemFactory<T>> factoryitem) {
				AbstractMenuItemPanelV5<T> item = factoryitem.getModelObject().getItem("panel");
				item.setMarkupId("panel"+factoryitem.getIndex());
				item.setVisible(factoryitem.isVisible());
				item.setIndex(getIndex());
				item.setModel(new IModel<T>() {
					private static final long serialVersionUID = 1L;
					public void setObject(T object) {
						SubmenuAjaxItemPanelV5.this.getModel().setObject(object);
					}
					public T getObject() {
						return SubmenuAjaxItemPanelV5.this.getModel().getObject();
					}
					public void detach() {
						if (SubmenuAjaxItemPanelV5.this.getModel()!=null)
							SubmenuAjaxItemPanelV5.this.getModel().detach();
					}
				});
				
				if (item.getCssClass()!=null)
					factoryitem.add(new AttributeModifier("class", item.getCssClass()));
				
				factoryitem.add(item);
				factoryitem.setVisible(item.isVisible());
			}
		};
		
		itemsview.setOutputMarkupId(true);

		
		((WebMarkupContainer) get("menu-container:menu")).addOrReplace(itemsview);
		is_created=true;
		
	}

}
