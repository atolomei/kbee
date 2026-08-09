package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.grid.GridDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;

import kbee.web.console.BaseBrowser;

public class GridDisplayModeButton extends ToolbarItem {

	private static final long serialVersionUID = 1L;

	public GridDisplayModeButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
			addLink();
	}
	
	protected void addLink() {
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				GridPanel<?> panel = (GridPanel<?>) GridDisplayModeButton.this.getBrowser().getPanel(GridPanel.class);
				
				if (panel!=null) {
					if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE)
						panel.setGridDisplayMode(GridDisplayMode.COMPACT);
					else
						panel.setGridDisplayMode(GridDisplayMode.COMFORTABLE);
				}
				
				target.add(getBrowser());
			}
		};
		
		
		WebMarkupContainer icon = new WebMarkupContainer("icon");
		
		icon.add( new AttributeModifier("class", new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				GridPanel<?> panel = (GridPanel<?>) GridDisplayModeButton.this.getBrowser().getPanel(GridPanel.class);
				if (panel!=null) {
					if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE)
						return "btn-mini far fa-th-large2"; // glyphicon-th";
					else
						return "btn-mini far fa-th-large";// glyphicon-th-large";
				}
				return "btn-mini far fa-th-large";// glyphicon-th-large";
			}
		}));
		link.add(icon);
				
		
		add(link);
	}
}
