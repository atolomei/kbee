package kbee.web.console;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnEventHeaderItem;

public class AjustableHeightBehavior extends Behavior {
	private static final long serialVersionUID = 1L;
	private int value;

	public AjustableHeightBehavior(int value) {
		this.value = value;
	}
	
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		StringBuilder script = new StringBuilder();
		
		String id = component.getMarkupId();
		
		script.append("function setHeight"+id+"() {");
		script.append("	var h = viewport().height - "+String.valueOf(value)+" + \"px\"; console.log(h);");
		script.append("	document.getElementById('"+component.getMarkupId()+"').style.height=h;");
		script.append("}\n");
		
		script.append("function viewport() {");
		script.append("	var e = window, a = 'inner';");;
		script.append("	if (!('innerWidth' in window )) {");
		script.append("		a = 'client';");
		script.append("		e = document.documentElement || document.body;");
		script.append("	}");
		script.append("	return { width : e[ a+ 'Width' ] , height : e[ a+'Height' ] };");
		script.append("}");
	
		response.render(JavaScriptHeaderItem.forScript(script.toString(), "height"+id));
		response.render(OnDomReadyHeaderItem.forScript("setHeight"+id+"()"));
		response.render(OnEventHeaderItem.forScript("window", "resize", "setHeight"+id+"();"));
	}
}
