package com.novamens.kbee.wicket.markup.html.console.grid;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePhoto;



public abstract class ImageColumnPanel<T> extends ModelPanel<T> {
													
 	private static final long serialVersionUID = 1L;

 	public ImageColumnPanel(String id,  IModel<T> mobject) {
		super(id, mobject);
	}


 	@Override
 	public void onInitialize() {
 		super.onInitialize();
 		
 		WebMarkupContainer uc = new WebMarkupContainer("img-container");
 		add(uc);
 		
 		Link<Void> link = new Link<Void>("link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				fire(new ClickEvent<T>(null, ImageColumnPanel.this.getModel(), 0));
			}
 		};
 		
 		if (getTarget()!=null)
 			link.add(new AttributeModifier("target", getTarget()));
 		
 		uc.add(link);
 		
 		if (getCss()!=null) 
 			uc.add(new AttributeModifier("class", getCss()));
 		
 		Image img = getImage("img");
 		
  		if (img==null)
 			img = new InvisiblePhoto("img");

 		if (getAnchorTitle()!=null)
 			  link.add(new AttributeModifier("title", getAnchorTitle()));
 		
 		link.add(img);
 		
 		
 	}
 	

 	
	protected IModel<String> getAnchorTitle() {
		return null;
	}

	protected String getCss() {
		return "thumbnailcolumn";
	}
	
	protected String getTarget() {
		return "_blank";
	}
	
	protected abstract Image getImage(String id);
	

}
