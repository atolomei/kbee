package kbee.web.security.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.BaseBrowser;

public class UsersBatchSetGlobalRoleButton extends ToolbarItem {

	private static final long serialVersionUID = 1L;
	public UsersBatchSetGlobalRoleButton(BaseBrowser<?> browser, Align align, boolean isicon) {
		super(browser, align, isicon);
		setOutputMarkupId(true);
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(UsersBatchSetGlobalRoleButton.this);
			}
		});
	}
	public void onInitialize() {
		super.onInitialize();
			add(new AjaxLink<Void>("link") {
				private static final long serialVersionUID = 1L;
				public void onClick(AjaxRequestTarget target) {
					fire( new UsersBatchSetRoleButtonEvent(target));
				}
			});
	}

}
