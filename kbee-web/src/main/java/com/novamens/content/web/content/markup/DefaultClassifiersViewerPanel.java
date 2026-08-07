package com.novamens.content.web.content.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;


import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.model.ModelPanel;

public class DefaultClassifiersViewerPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;

	public DefaultClassifiersViewerPanel(String id, IModel<T> model) {
		super(id, model);
	}
	
	
	
	protected List<String> getValues() {
		return null;
	}
	
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("value")==null) {
			List<String> list = getValues();
			
			if (list==null)
				list = new ArrayList<String>();
			
			StringBuilder str = new StringBuilder();
			for (String s: list) {
				if (str.length()>0)
					str.append("<br/>");
				str.append(s);
			}
			add( (new Label("value", str.toString())).setEscapeModelStrings(false));
		}
	}
}
