package com.novamens.content.web.console.markup;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.workflow.Process;

import kbee.web.console.BaseBrowser;
import kbee.web.console.TaskFactoryPanel;

@SuppressWarnings("serial")
public class NewContentButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	public NewContentButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(new TaskFactoryPanel("new-task") {
			protected void onStart(Process process) {
				NewContentButton.this.onStart(process); 
			}
		});
	}
	
	protected void onStart(Process process) {
	}
}
