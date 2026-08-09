package kbee.web.eform;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import com.novamens.content.form.ETitle;

@SuppressWarnings("serial")
public class ETitlePanel extends EComponentPanel<ETitle> {
	private static final long serialVersionUID = 1L;

	public ETitlePanel(String id, ETitle title) {
		super(id, title);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		getContainer().add(new Label("title", new Model<String>() {
			public String getObject() {
				return getComponent().getTitle();
			}
		}));
	}
}
