package kbee.web.model;


import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;

import kbee.web.console.BaseBrowser;


public class NewDataSetButton extends ToolbarItem {
		
	private static final long serialVersionUID = 1L;

	public NewDataSetButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new DataSetFactoryPanel("new-dataset") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void create(String string) {
				NewDataSetButton.this.create(string);
			}
		});
	}
	
	protected void create(String s) {
	}
	
}
