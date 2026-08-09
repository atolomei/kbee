package kbee.web.print;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;


public class PrintBehavior extends Behavior {
	private static final long serialVersionUID = 1L;
	
	public PrintBehavior() {
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		
		response.render(CssHeaderItem.forReference(
			new CssResourceReference(PrintBehavior.class, "js/print.js-1.5.0/print.min.css")));

		response.render(JavaScriptHeaderItem.forReference(
			new JavaScriptResourceReference(PrintBehavior.class, "js/print.js-1.5.0/print.js")));
		
	}
}
