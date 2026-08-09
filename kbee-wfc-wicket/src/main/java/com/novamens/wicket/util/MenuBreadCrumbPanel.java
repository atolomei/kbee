package com.novamens.wicket.util;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.panel.KBPanel;


public class MenuBreadCrumbPanel<T> extends KBPanel {
																												
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MenuBreadCrumbPanel.class.getName());
	
	private static final long serialVersionUID = 1L;

	private List<Panel> list; 

	private String active_css = "active";
	
	
	private IModel<T> model;
	
	public MenuBreadCrumbPanel() {
		super("breadcrumb");
		list = new ArrayList<Panel>();
	}
	
	public MenuBreadCrumbPanel(String id) {
		this(id, null);
	}

	public MenuBreadCrumbPanel(String id, IModel<T> model) {
		super(id);
		this.model=model;
		list = new ArrayList<Panel>();
	}
	
	
	public MenuBreadCrumbPanel(String id, IModel<T> model, List<BCElement> bl) {
		super(id);
		this.model=model;
		list = new ArrayList<Panel>();
		for (int e=0; e<bl.size(); e++) 
			list.add(new MenuBCElement(bl.get(e)));
	}
	
	
	public MenuBreadCrumbPanel(String id, IModel<T> model, BCElement... elements) {
		super(id);
		this.model=model;
		list = new ArrayList<Panel>();
		for (int e=0; e<elements.length; e++) 
			list.add(new MenuBCElement(elements[e]));
	}
	
	public MenuBreadCrumbPanel(List<Panel> pl, IModel<T> model) {
		super("breadcrumb");
		this.model=model;
		list = new ArrayList<Panel>();
		for (Panel p: pl) 
			list.add(p);
	}
	
	
	

	public IModel<T> getModel() {
		return this.model;
	}

	
	public MenuBreadCrumbPanel<T> addElement(Panel p) {
		list.add(p);
		return this;
	}
	
	public MenuBreadCrumbPanel<T> addElement(BCElement b) {
		list.add(new MenuBCElement(b));
		return this;
	}
	
	
	public MenuBreadCrumbPanel<T> addElement(AjaxIBCElement b) {
		list.add(new MenuAjaxBCElement(b));
		return this;
	}
	
	public void onDetach() {
		super.onDetach();
		try {
		
		if (model!=null)
			model.detach();
			
		if (list==null)
			return;
			
		for (Panel panel: list) 
			panel.detach();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	public void onInitialize() {
		super.onInitialize();
		
		addComponents();
	}

	protected void addComponents() {
						
		add(new ListView<Panel>("breadcrumb-element", list) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<Panel> item) {
				Panel element = item.getModelObject();
				item.add(element);
				if ((item.getIndex()==(list.size()-1)) && (getActiveCss()!=null)) 
					item.add(new AttributeModifier("class", getActiveCss()));
			}	
		});
 	}

	public void setActiveCss(String s) {
		this.active_css=s;
	}
	
	protected String getActiveCss() {
		return active_css;
	}
}
