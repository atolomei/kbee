package kbee.web.nav;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

public class RefreshParentBehavior extends AbstractAjaxBehavior {
	private static final long serialVersionUID = 1L;
	@Override
	public void onRequest() {
	}
	@Override
	public void renderHead(final Component component, final IHeaderResponse response) {
		super.renderHead(component, response);
		String script = "if (window.opener && window.opener.refresh) {  window.opener.refresh(); }";
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
};
