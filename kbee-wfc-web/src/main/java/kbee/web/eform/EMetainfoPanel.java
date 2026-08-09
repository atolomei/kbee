package kbee.web.eform;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public abstract class EMetainfoPanel extends Panel {
	private static final long serialVersionUID = 1L;

	public EMetainfoPanel(String id) {
		super("panel");
	}
	
	public abstract List<Serializable> getMessages();

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		addOrReplace(new ListView<Serializable>("messages", getMessages()) {
			protected void populateItem(ListItem<Serializable> item) {
				item.add(new Label("message", item.getModelObject()));
			}
		});
	}
}