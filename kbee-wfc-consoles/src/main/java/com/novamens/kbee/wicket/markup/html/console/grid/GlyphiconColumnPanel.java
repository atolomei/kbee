package com.novamens.kbee.wicket.markup.html.console.grid;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
 
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.model.ModelPanel;

public abstract class GlyphiconColumnPanel<T> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;

	public GlyphiconColumnPanel(String id, IModel<T> model) {
		super(id, model);
		
		WebMarkupContainer uc = new WebMarkupContainer("img-container");
		add(uc);
		
		Link<?> link = getNewLink("link");
		if (getTarget()!=null)
			link.add(new AttributeModifier("target", getTarget()));
		
		uc.add(link);
		
		if (getCss()!=null) 
			uc.add(new AttributeModifier("class", getCss()));
		
		if (getAnchorTitle()!=null)
			  link.add(new AttributeModifier("title", getAnchorTitle()));
		
		WebMarkupContainer gyp = new WebMarkupContainer("icon");
		
		if (getGlyphiconClass()!=null)
			gyp.add(new AttributeModifier("class" , getGlyphiconClass()));
		
		link.add(gyp);
	}
	
	
	protected IModel<String> getAnchorTitle() {
		return null;
	}

	protected abstract String getGlyphiconClass();
	
	protected String getCss() {
		return "thumbnailcolumn";
	}
	
	protected Link<?> getNewLink(String id) {
		return new Link<Void>(id) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				fire(new ClickEvent<T>(null, GlyphiconColumnPanel.this.getModel(), 0));
			}
		};
	}
	
	protected String getTarget() {
		return "_blank";
	}
}