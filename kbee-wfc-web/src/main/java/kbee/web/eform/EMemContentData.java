package kbee.web.eform;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;

public class EMemContentData extends EMemData implements EFormContentData {
	private static final long serialVersionUID = 1L;
	
	private IModel<Content> model;
	
	public EMemContentData(IModel<EForm> formModel, IModel<Content> contentModel) {
		super(formModel);
		setContent(contentModel);
	}
	
	public Content getContent() {
		return model.getObject();
	}
	
	public void setContent(IModel<Content> model) {
		this.model = model;
	}
	
	@Override
	public String getObjectTitle() {
		return getContent().getTitle();
	}
	
	public EFormData clone() {
		EMemData clone = new EMemContentData(getModel(), model);
		clone.setData(getData());
		return clone;
	}
	
	@Override
	public void detach() {
		super.detach();
		if (model!=null)
			model.detach();
	}
}