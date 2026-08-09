package com.novamens.kbee.wicket.markup.html.areainfo;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;

import com.novamens.wicket.markup.html.repeater.util.ListDataProvider;

				
public class AreaInfoPanel extends Panel {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AreaInfoPanel.class.getName());

	static public final int ONE_SECTION      = 1;  /* 1 column  */
	static public final int TWO_SECTIONS     = 2;  /* 2 columns */
	static public final int THREE_SECTIONS   = 3;  /* 3 columns */
	
	private static final long serialVersionUID = 1L;

	private List<Panel> list = new ArrayList<Panel>();
	private Panel actions_panel;
	private int sections =  ONE_SECTION;
	private String css;
	
	
	public AreaInfoPanel(String id) {
		super(id);
	}

	public List<Panel> getList() {
		return list;
	}
	
	public void setSections(int sections) {
		this.sections=sections;
	}
	
	public int getSections() {
		return this.sections;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	
			
			WebMarkupContainer area = new WebMarkupContainer("area");
			add(area);

			if (getCss()!=null)
				area.add(new AttributeModifier("class", getCss()));

			
			WebMarkupContainer actions = new WebMarkupContainer("actions-container") {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isVisible() {
					return isActionsVisible();
				}
			};
			area.add(actions);
			
			if (getActionsPanel()!=null) 
				actions.add(getActionsPanel());
								
			
			WebMarkupContainer data_container = new WebMarkupContainer("data-container");
			
			data_container.add( new AttributeModifier("class", isActionsVisible() ? "col-md-10" : "col-md-12"));
		
			ListDataProvider<Panel> ldp = new ListDataProvider<Panel>() {
				private static final long serialVersionUID = 1L;
				@Override
				public List<Panel> getList() {	
					return AreaInfoPanel.this.getList();
				}
			};
			
			DataView<Panel> ldata = new DataView<Panel>("grid-element", ldp) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void populateItem(Item<Panel> item) {
	 				try {
						Panel panel = item.getModelObject();
						item.add(panel);
						if 		(getSections()==ONE_SECTION) {item.add(new AttributeModifier("class", "fieldset col-lg-12 col-md-12 col-xs-12"));}
						else if (getSections()==TWO_SECTIONS) {item.add(new AttributeModifier("class", "fieldset col-lg-6 col-md-6 col-xs-12"));}
						else if (getSections()==THREE_SECTIONS) {item.add(new AttributeModifier("class", "fieldset col-lg-4 col-md-4 col-xs-12"));}
						
					}  catch (Exception e) {
						logger.error(e);
	 				}
	
				}
			};
			
			area.add(data_container);
			data_container.add(ldata);
		
	}


	public void setActionsPanel(Panel panel) {
		actions_panel=panel;
	}

	public Panel getActionsPanel() {
		return actions_panel;
	}

	public void addPanel(Panel panel) {
		getList().add(panel);
	}

	
	
	@Override
	public void onDetach() {
			 for( Panel panel: getList()) 
				 panel.detach();
		 super.onDetach();
	}
	
	private boolean isActionsVisible() {
		return getActionsPanel()!=null;
	}

	public void setCss(String css) {
		this.css=css;
	}
	
	public String getCss() {
		return this.css;
	}

}
