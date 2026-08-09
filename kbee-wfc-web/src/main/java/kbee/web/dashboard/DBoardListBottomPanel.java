package kbee.web.dashboard;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.panel.KBPanel;

@SuppressWarnings("serial")
public abstract class DBoardListBottomPanel extends KBPanel {
	private static final long serialVersionUID = 1L;

	public DBoardListBottomPanel(String id) {
		super(id);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		Link<Void> lall = new Link<Void>("all") {
			@Override
			public void onClick() {
				DBoardListBottomPanel.this.onClickAll();
			}
		};

		Label all_label=new Label("all-label", DBoardListBottomPanel.this.getAllString());
		all_label.setVisible(getAllString()!=null);
		all_label.setEscapeModelStrings(false);
		lall.add(all_label);
		add(lall);
		
		Label v = new Label("viewing", ()-> getViewingString().getObject()) {
			public boolean isVisible() {
				return getViewingString()!=null;
			}
		};
		v.setEscapeModelStrings(false);
		add(v);
	}

	protected abstract IModel<String> getViewingString();
	protected abstract  IModel<String> getAllString();
	protected abstract void onClickAll();
}