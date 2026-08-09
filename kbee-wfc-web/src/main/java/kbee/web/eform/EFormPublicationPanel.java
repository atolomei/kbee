package kbee.web.eform;

import org.apache.wicket.model.IModel;

import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.kbee.wicket.model.ModelPanel;

public class EFormPublicationPanel extends ModelPanel<EFormData> {
	private static final long serialVersionUID = 1L;
	
	public EFormPublicationPanel(String id, IModel<EFormData> model) {
		super(id, model);
	}
	
	public EForm getForm() {
		return getModelObject().getForm();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(new EFormEditor("eform", getModel()));
	}
}