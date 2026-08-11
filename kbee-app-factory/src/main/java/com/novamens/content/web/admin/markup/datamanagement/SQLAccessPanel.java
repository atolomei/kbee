package com.novamens.content.web.admin.markup.datamanagement;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.content.web.sql.markup.SQLGatewayPage;

@Deprecated
public class SQLAccessPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	public SQLAccessPanel(String id) {
		super(id);

		add(new Link<Void>("sql")  {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
					setResponsePage(new SQLGatewayPage());	
			}
		});	
		
	}

}
