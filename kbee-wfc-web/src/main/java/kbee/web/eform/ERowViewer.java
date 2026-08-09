package kbee.web.eform;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;

import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormData;

public class ERowViewer extends ERowPanel {
	private static final long serialVersionUID = 1L;
	
	public ERowViewer(String id, EFormContainer row, IModel<EFormData> data) {
		super(id, row, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		getContainer().add(new AttributeModifier("style", "float:left; width:100%;margin-bottom:0px;"));
	}
}
