package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.model.PropertyModel;

import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public class SelectAllButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;
	private boolean value;

	public SelectAllButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("check")==null) {
			addCheck();
		}
	}
	
	public void setValue(boolean value) {
		this.value = value;
	}
	
	public boolean getValue() {
		return value;
	}
	
	protected void addCheck() {
		add(new AjaxCheckBox("check", new PropertyModel<Boolean>(this, "value")) {
			protected void onUpdate(AjaxRequestTarget target) {
				DataViewPanel<?> dataview = getBrowser().getPanel(DataViewPanel.class);
				dataview.selectAll(SelectAllButton.this.getValue());
				target.add(getBrowser());
			}
		});
	}

}
