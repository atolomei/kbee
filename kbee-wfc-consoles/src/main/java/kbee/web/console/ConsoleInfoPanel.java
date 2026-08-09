package kbee.web.console;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.CloseConsoleTopPanelEvent;

public class ConsoleInfoPanel extends ConsoleErrorPanel {
	
	
	private static final long serialVersionUID = 1L;

	public ConsoleInfoPanel(IModel<String> title, IModel<String> text) {
		this("error-panel", title, text);
	}
	
	public ConsoleInfoPanel(String id, Throwable e) {
		super(id,e);
	}
	
	
	String css;
	
	public ConsoleInfoPanel(String id, IModel<String> title, IModel<String> text) {
		super(id, title, text);
		css="info";
	}

	public ConsoleInfoPanel(String id, IModel<String> title, IModel<String> text, String css) {
		super(id, title, text);
		this.css=css; // info warning error success
	}

	protected String getCssClass() {
		return  (this.css!=null?this.css:"info")+"-top-panel col-lg-12 col-md-12 col-xs-12";
	}
	

	@Override
	protected void onClick(AjaxRequestTarget target) {
		fire (new CloseConsoleTopPanelEvent(target));
	}


}
