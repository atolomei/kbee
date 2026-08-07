package com.novamens.content.web.base.page.component;

import java.util.Calendar;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

public class AbstractKbeeFooter extends Panel {

private static final long serialVersionUID = -6552531441810718152L;

private static String cp = "(c) 2004 - " + String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) +". Novamens";
	

	public AbstractKbeeFooter(String id) {
		super(id);

		add(new Label("about", new StringResourceModel("kbee.footer.about", this, null)));
		add(new Label("copyright", cp));
		add(new Label("tutorial", new StringResourceModel("kbee.footer.tutorial", this, null)));
		add(new Label("contact", new StringResourceModel("kbee.footer.contact", this, null)));
	}
}
