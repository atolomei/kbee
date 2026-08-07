package com.novamens.content.web.base.page.component;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;

public class KbeeContentFooter extends Panel {
	private static final long serialVersionUID = 1L;

	public KbeeContentFooter(String id) {
		super(id);

		//add(new Label("copyright", new StringResourceModel("kbee.footer.copyright", this, null, new Object[] {String.valueOf(Calendar.getInstance().get(Calendar.YEAR))})));
		// add((new Label("title", "")).setVisible(false));
		// add(new Label("about", new StringResourceModel("kbee.footer.about", this, null)));
		// add(new Label("tutorial", new StringResourceModel("kbee.footer.tutorial", this, null)));
		// add(new Label("contact", new StringResourceModel("kbee.footer.contact", this, null)));
	}
	
	public KbeeContentFooter(String id, IModel<? extends Content> model) {
		super(id);
		//String type = model!=null?model.getObject().getContentTemplate().getName():"Type N/A";
		//add(new Label("title",   type + ". id: " + (model!=null?model.getObject().getId():"N/A")));
	}
}
