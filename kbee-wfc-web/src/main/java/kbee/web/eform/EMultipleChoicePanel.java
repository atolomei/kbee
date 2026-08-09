package kbee.web.eform;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormChoice;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormMultipleChoice;

@SuppressWarnings("serial")
public class EMultipleChoicePanel extends EComponentPanel<EFormMultipleChoice> {
	private static final long serialVersionUID = 1L;

	public EMultipleChoicePanel(EFormMultipleChoice field, IModel<EFormData> data) {
		this("component", field, data);
	}
	
	public EMultipleChoicePanel(String id, EFormMultipleChoice field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	public List<EFormChoice> getChoices() {
		return getComponent().getChoices();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		getContainer().add(new Label("label", new Model<String>() {
			public String getObject() {
				return getComponent().getLabel()!=null ? getComponent().getLabel() : getComponent().getText(); 
			}
		}));
		addChoices();
	}
	
	protected void addChoices() {
		getContainer().add(new ListView<EFormChoice>("choice", getChoices()) {
			public void populateItem(ListItem<EFormChoice> item) {
				Panel panel = getPanel("panel", item.getModelObject());
				if (panel!=null) {
					item.add(panel);
				}
			}
		});
	}
}
