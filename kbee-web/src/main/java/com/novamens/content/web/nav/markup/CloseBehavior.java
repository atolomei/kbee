package com.novamens.content.web.nav.markup;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

public class CloseBehavior extends AbstractAjaxBehavior {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void onRequest() {
	}
	
	public void renderHead(final Component component, final IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(OnDomReadyHeaderItem.forScript(getScript()));
	}
	
	public String getScript() {
		String script = "if (window.opener && window.opener.refresh) {  window.opener.refresh(); } window.close();";
		return script;
	}
};
