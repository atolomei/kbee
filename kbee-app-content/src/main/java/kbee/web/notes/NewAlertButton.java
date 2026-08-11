package kbee.web.notes;


import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;

import kbee.web.console.BaseBrowser;


public abstract class NewAlertButton extends ToolbarItem {
	
	private static final long serialVersionUID = 1L;

	public NewAlertButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new AlertFactoryPanel("factory") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onCreate(String type) {
				NewAlertButton.this.onCreate(type);
			}
		});
	}
	
	protected abstract void onCreate(String type);
	
}
