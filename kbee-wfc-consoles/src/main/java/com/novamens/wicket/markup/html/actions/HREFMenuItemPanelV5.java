package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.util.InvisiblePanel;

public class HREFMenuItemPanelV5<T> extends AbstractLinkMenuItemPanelV5<T> {

	private static final long serialVersionUID = 1L;

	private String url;
	private IModel<String> label;
	
	public HREFMenuItemPanelV5(String id, String url, IModel<String> label) {
		super(id);
		this.url=url;
		this.label=label;
	}

	@Override
	public void onClick() throws Exception {
		// no se usa
	}

	@Override
	public String getLabel() {
		return this.label.getObject();
	}

	@Override
	public String getBeforeClick() {
		return null;
	}

	@Override
	protected void addComponents() {

		WebMarkupContainer mc = new WebMarkupContainer("lcontainer");
		mc.setOutputMarkupId(true);
		add(mc);
		
		WebMarkupContainer ln=new WebMarkupContainer("item-link"); 
		
		ln.add(new AttributeModifier("href", this.url));
		
		WebMarkupContainer w = new WebMarkupContainer ("item-icon") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return  getIconCssClass()!=null;
			}
		};
		
		ln.add(w);
		
		w.add(new AttributeModifier("class", new Model<String>() {
				private static final long serialVersionUID = 1L;
				@Override
				public String getObject() {
					return getIconCssClass()!=null?getIconCssClass():"";
				}
			}));

		Label label = new Label("item-label", new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				return HREFMenuItemPanelV5.this.getLabel();
			}
		});
		
		if (isEscapeModelString()) 
			label.setEscapeModelStrings(true);
		else
			label.setEscapeModelStrings(false);
		
		ln.add(label);
		mc.add(ln);
		
		mc.add(new InvisiblePanel("contextual-help"));
		add(new InvisiblePanel("contextual-help-detail-container"));
		
		
		
	}

	@Override
	protected AbstractLink getNewLink(String id) {
		return new Link<Void>(id) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
			}
			
		};
	}
	
}
