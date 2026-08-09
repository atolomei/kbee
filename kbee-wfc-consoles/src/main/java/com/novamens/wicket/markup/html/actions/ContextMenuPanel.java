package com.novamens.wicket.markup.html.actions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class ContextMenuPanel<T> extends KBPanel  {
	private static final long serialVersionUID = 1L;
																								
	private static Logger logger =  Logger.getLogger(ContextMenuPanel.class.getName());
	
	private IModel<T> model;
	private int index;
	private List<MenuItemFactory<T>> items = new ArrayList<MenuItemFactory<T>>();
	private ListView<MenuItemFactory<T>> itemsview;
	
	private boolean popper = true;
	private boolean wicket = false;
	boolean sort = false;
	
	private static final ResourceReference POPPER_WICKET_JS = 
		new JavaScriptResourceReference(ContextMenuPanel.class, "popper-wicket.js");
	
	
	public ContextMenuPanel(IModel<T> model) {
			this("menu", model);
	}
	
	public ContextMenuPanel(String id, IModel<T> model) {
		super(id);
		setModel(model);
		setOutputMarkupId(true);
	}
	
	
	public void onInitialize() {
		super.onInitialize();
		
		if (itemsview==null) {
			
			int n= 0;
			
			if (isSort()) {
				this.items.sort(new Comparator<MenuItemFactory<T>>() {
					@Override
					public int compare(MenuItemFactory<T> o1, MenuItemFactory<T> o2) {
						try {
							return o1.getItem(String.valueOf(n)).getLabel().compareToIgnoreCase(o2.getItem(String.valueOf(n)).getLabel());
						} catch (Exception e) {
							logger.error(e);
						}
						return 0;
					}
					
					
				});
			}

			itemsview = new ListView<MenuItemFactory<T>>("item", this.items) {
				public void populateItem(ListItem<MenuItemFactory<T>> factoryitem) {

					AbstractMenuItemPanelV5<T> item = factoryitem.getModelObject().getItem("panel");

					item.setEscapeModelStrings(false);
					item.setMarkupId("cpanel"+factoryitem.getIndex());
					item.setVisible(factoryitem.isVisible());
					item.setIndex(getIndex());
					item.setModel(new IModel<T>() {
						public void setObject(T object) {
							ContextMenuPanel.this.getModel().setObject(object);
						}
						public T getObject() {
							return ContextMenuPanel.this.getModel().getObject();
						}
						public void detach() {
							if (ContextMenuPanel.this.getModel()!=null)
							ContextMenuPanel.this.getModel().detach();
						}
					});

					if (item.getCssClass()!=null)
						factoryitem.add(new AttributeModifier("class", item.getCssClass()));

					factoryitem.add(item);
					factoryitem.setVisible(item.isVisible());
				}
			};

			add(itemsview);
		}
	}

	public void setSort(boolean b) {
		this.sort=b;
	}
	
	public boolean isSort() {
		return  sort;
	}

	
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		if (isPopper()) {
			response.render(OnDomReadyHeaderItem.forScript(
					"tryBindPopperDropdown( $('#" + this.getMarkupId() + "'));"
			));
		}
		
		if (isWicket()) {
			response.render(JavaScriptHeaderItem.forReference(POPPER_WICKET_JS));
			String id = getParent().getMarkupId();
			response.render(OnDomReadyHeaderItem.forScript(
				    "bindDropupSmart('#" + id + "');"
				));
		}
	}
	
	public void setPopper( boolean b) {
		this.popper = b;
		this.wicket = !b;
	}
	
	public boolean isPopper() {
		return popper;
	}
	
	public void setWicket( boolean b) {
		this.wicket = b;
		this.popper= !b;
	}
	
	public boolean isWicket() {
		return wicket;
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
	
	public int getIndex() {
		return this.index;
	}
	
	public void addItem(MenuItemFactory<T> item) {
		this.items.add(item);
	}	 
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.model!=null) 
			this.model.detach();
	}
}
