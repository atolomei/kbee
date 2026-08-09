package kbee.web.page;

import org.apache.wicket.markup.head.IHeaderResponse;

import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebPage;

public class ClosePage extends WebPage {
	private static final long serialVersionUID = 1L;

	public ClosePage() {
	}
	
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		StringBuilder script = new StringBuilder();
		script.append("	var agent = navigator.userAgent;\n");
		script.append("	if (agent.indexOf('Edge') > 0 || agent.indexOf('Trident') > 0) {\n");
		script.append("		window.open('', '_self', '');\n");
		script.append("	}\n");
		script.append("	window.close();\n");
		response.render(OnLoadHeaderItem.forScript(script));
	}	
}
