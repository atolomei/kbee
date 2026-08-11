package kbee.web.model;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.Classifier;
import com.novamens.wicket.util.BCElement;

public class ClassifierBC extends BCElement {
				
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	IModel<Classifier> model;
	
	public ClassifierBC(IModel<Classifier> model) {
		super();
		this.model=model;
	}
	
	@Override
	public IModel<String> getLabel() {
		return new Model<String>(model.getObject().getName() );
		
		// + " <span class=\"ago\">("+new StringResourceModel("classifier", this, null).getObject()+")</span>"
	}
	
	@Override
	public void onDetach() {
		this.model.detach();
		super.onDetach();
	}
	
	@Override
	public void onClick() {
		setResponsePage(new ClassifierModelPage(this.model, false, false));
	}

}
