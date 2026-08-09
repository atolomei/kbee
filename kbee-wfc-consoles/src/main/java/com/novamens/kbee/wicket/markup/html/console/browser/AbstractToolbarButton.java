package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.BaseBrowser;

public abstract class AbstractToolbarButton extends ToolbarItem {
	
	@SuppressWarnings("unused")
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractToolbarButton.class.getName());

	private static final long serialVersionUID = 1L;

	
	private IModel<String> ms = new Model<String>("btn-mini");
	private boolean is_spin = true;
	private boolean is_send_on_complete = true;
	
	public AbstractToolbarButton(BaseBrowser<?> browser, Align align) {
				this(browser,  align, true, true);
	}
	
	public AbstractToolbarButton(BaseBrowser<?> browser, Align align, boolean isspin) {
		this(browser,  align, true, true);
}

	public AbstractToolbarButton(BaseBrowser<?> browser, Align align, boolean spin, boolean send_on_complete) {
		super(browser, align);
		this.is_spin=spin;
		this.is_send_on_complete=send_on_complete;
		setOutputMarkupId(true);
	}
	
	public void setSpin(boolean b) {
		this.is_spin=b;
	}
	
	public boolean isSpin() {
		return this.is_spin;
	}

	public boolean isSendOnComplete() {
		return this.is_send_on_complete;
	}
	
	 
	@Override
	public boolean isEnabled() {
		return !super.getBrowser().getSelection().isEmpty();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (get("link")==null) {
			addListeners();
			addLink();
		}
	}
	
	protected void addListeners() {
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(AbstractToolbarButton.this);
			}
		});
	}
	

	protected abstract void addLink();
	
	protected IModel<String> getLinkCss() {
		return ms;
	}

	 protected String getLabelStr() {
		 return null;
	 }
	 
	protected String getIcon() {
		return "far fa-edit";
	}
	
	

}
