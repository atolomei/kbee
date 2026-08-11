package kbee.web.security.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import com.novamens.content.entity.Person;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.BaseBrowser;
import kbee.web.event.wicket.ClickSetGroupEvent;

public class UsersBatchStartPage extends ToolbarItem {
			
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger logger = LogManager.getLogger(UsersBatchStartPage.class.getName());
	
	public UsersBatchStartPage(BaseBrowser<Person> browser, Align align, boolean isicon) {
		super(browser, align, isicon);
		setOutputMarkupId(true);
		
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(UsersBatchStartPage.this);
			}
		});
	}

	public void close(AjaxRequestTarget target) {
		target.add(getPage());
	}

	
	@Override
	public boolean isEnabled() {
		return !super.getBrowser().getSelection().isEmpty();
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (get("set-global-permission-modal")==null) {
			add(new AjaxLink<Void>("link") {
				private static final long serialVersionUID = 1L;
				public void onClick(AjaxRequestTarget target) {
					fire(new ClickSetGroupEvent(target));
				}
			});
		}
	}
}

