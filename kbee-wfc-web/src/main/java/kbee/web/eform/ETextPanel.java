package kbee.web.eform;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.EText;
import com.novamens.kbee.text.KbeeTextTemplate;

@SuppressWarnings("serial")
public class ETextPanel extends EComponentPanel<EText> {
	private static final long serialVersionUID = 1L;

	public ETextPanel(String id, EText text, IModel<EFormData> data) {
		super(id, text, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		getContainer().add(new Label("text", new Model<String>() {
			public String getObject() {
				return ETextPanel.this.getText();
			}
		}));
		
		((Label)getContainer().get("text")).setEscapeModelStrings(false);
	}
	
	protected String getText() {
		KbeeTextTemplate template = new KbeeTextTemplate(getComponent().getText());
		String text = template.process(getData().getObject());
		return 	text;
	}
}
