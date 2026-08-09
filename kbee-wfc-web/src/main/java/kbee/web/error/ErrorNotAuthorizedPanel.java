package kbee.web.error;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.markup.html.panel.KBPanel;


public class ErrorNotAuthorizedPanel<T> extends KBPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IModel<String> title;
	private IModel<String> message;

	public ErrorNotAuthorizedPanel(String id) {
		this(id,  null);
	}
	
	public ErrorNotAuthorizedPanel(String id, IModel<T> model) {
		super(id, model);
		this.title=new StringResourceModel("not-authorized", this, null);
		this.message=new StringResourceModel("not-authorized-text", this, null);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);

		Label xtitle=new Label("title", title);
		xtitle.setVisible(title!=null);
		xtitle.setEscapeModelStrings(false);
		add(xtitle);
		
		Label m = new Label("text", message);
		m.setEscapeModelStrings(false);
		m.setVisible(message!=null);
		add(m);
	}
}