package com.novamens.content.web.console.markup;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;



import com.novamens.kbee.wicket.model.ModelPanel;

import kbee.util.PropertiesFactory;

public class GlyphiconColumnPanel<T> extends ModelPanel<T> {
	
	public static final String PROPERTY_UNREAD = "unread";
	private static final long serialVersionUID = 1L;
	private static final String ICON = PropertiesFactory.getInstance("kbee").getProperties().getProperty("icon.unread", "cell-icon fa fa-square");
	
	
	@SuppressWarnings("serial")
	public GlyphiconColumnPanel(String id, IModel<T> model) {
		super(id, model);
		
		 WebMarkupContainer newi = new WebMarkupContainer("new-icon") {
			 public boolean isVisible() {
				 return GlyphiconColumnPanel.this.isVisible(); 
			 }
		};
			
		newi.add(new AttributeModifier("class", getCss()));
		add(newi);
			
	 	if (getAnchorTitle()!=null)
	 		newi.add(new AttributeModifier("title", getAnchorTitle()));
	 	
	 	if (getCssStyle()!=null)
	 		newi.add(new AttributeModifier("style", getCssStyle()));

	}
	
	public String getCss() {
		return ICON;  
	}
	
	public String getCssStyle() {
		return null;  
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}

	protected IModel<String> getAnchorTitle() {
		return new Model<String>("Unread");
	}
}
