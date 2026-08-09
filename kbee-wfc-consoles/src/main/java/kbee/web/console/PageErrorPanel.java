package kbee.web.console;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

public class PageErrorPanel extends ConsoleErrorPanel {
			
	private static final long serialVersionUID = 1L;

	public PageErrorPanel (IModel<String> title, IModel<String> text) {
		this("info-panel", title, text);
	}
	
	public PageErrorPanel (String id, Throwable e) {
		super(id,e);
	}
	
	
	String css;
	
	public PageErrorPanel (String id, IModel<String> title, IModel<String> text) {
		super(id, title, text);
		css="error";
	}

	public PageErrorPanel (String id, IModel<String> title, IModel<String> text, String css) {
		super(id, title, text);
		this.css=css; // info warning error success
	}

	protected String getCssClass() {
		return  (this.css!=null?this.css:"error")+"-top-panel col-lg-12 col-md-12 col-xs-12";
	}
	

	@Override
	protected void onClick(AjaxRequestTarget target) {
		fire (new CloseInfoTopPanel(target));
	}
}
