package kbee.web.eform;


import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormData;
import com.novamens.kbee.wicket.util.InvisiblePanel;

@SuppressWarnings("serial")
public class ERowPanel extends EComponentPanel<EFormContainer> {
	private static final long serialVersionUID = 1L;
	
	public ERowPanel(String id, EFormContainer row, IModel<EFormData> data) {
		super(id, row, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addComponents();
	}
	
	protected void addComponents() {
		getContainer().add(new ListView<EFormComponent>("component", getComponent().getComponents()) {
			public void populateItem(ListItem<EFormComponent> item) {
				Panel panel = getPanel("panel", item.getModelObject());
				if (panel!=null) {
					item.add(panel);
				}
				else {
					item.add(new InvisiblePanel("panel"));
				}
			}
		});
	}
}
