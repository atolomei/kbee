package kbee.web.report;



import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;

import kbee.web.console.BaseBrowser;
import kbee.web.console.Console;

public class ReportSelectorButton extends ToolbarItem {

	private static final long serialVersionUID = 1L;

	public ReportSelectorButton(BaseBrowser<?> browser, Align align, Console<?> console) {
		super(browser, align);
		add(new ReportSelector("selector", console));
	}
}
