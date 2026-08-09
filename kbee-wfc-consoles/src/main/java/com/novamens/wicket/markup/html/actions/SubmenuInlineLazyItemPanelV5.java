package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.util.InvisiblePanel;

public class SubmenuInlineLazyItemPanelV5<T> extends AjaxMenuItemPanelV5<T> {
	
	private static final long serialVersionUID = 1L;
	
	public SubmenuInlineLazyItemPanelV5(String id, IModel<T> model) {
		super(id);
		setModel(model);
		setOutputMarkupId(true);
	}

	@Override
	public void onClick(AjaxRequestTarget target) throws Exception {
					
		if (!(get("menu") instanceof InvisiblePanel)) {
			boolean b=get("menu").isVisible();
			get("menu").setVisible(!b);
			target.add(SubmenuInlineLazyItemPanelV5.this);
		}
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
			if (get("menu")==null)
				add(new InvisiblePanel("menu"));
	}
	
	@Override
	public String getLabel() {
		return null;
	}

	public void onDetach() {
		super.onDetach();
	}
	
	
	@Override
	public String getBeforeClick() {
		return null;
	}
	
	public void setPanel( Panel panel) {
		panel.setVisible(false);
		addOrReplace(panel);
		
	}
}
