package kbee.web.eform;

import org.apache.wicket.model.IModel;

import com.novamens.content.form.EFormData;

public class EFormSharedViewer extends EFormViewer {
	private static final long serialVersionUID = 1L;
	
	private EPanelFactory panelfactory;
	
	public EFormSharedViewer(String id, IModel<EFormData> model) {
		super(id, model);
	}
	
	public EPanelFactory getPanelFactory() {
		if (panelfactory==null) {
			panelfactory = new EViewerSharedFactory(getModel());
		}
		return panelfactory;
	}
}