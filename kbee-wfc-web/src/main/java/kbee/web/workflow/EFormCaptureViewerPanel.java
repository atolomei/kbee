package kbee.web.workflow;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormData;
import com.novamens.kbee.content.form.KbeeEFormActivityData;
import com.novamens.kbee.wicket.model.ModelPanel;

@SuppressWarnings("serial")
public class EFormCaptureViewerPanel extends ModelPanel<EFormData>  {
	private static final long serialVersionUID = 1L;

	public EFormCaptureViewerPanel(String id, IModel<EFormData> model) {
		super(id, model);
	}
	
	public String getCapture() {
		return ((KbeeEFormActivityData)getModelObject()).getCapture(); 
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Label eformsource = new Label("eform", new Model<String>()  {
			public String getObject(){
				return getCapture();
			}
		});
		
		eformsource.setEscapeModelStrings(false);
		
		add(eformsource);
	}
}