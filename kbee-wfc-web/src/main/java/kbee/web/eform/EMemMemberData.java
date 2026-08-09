package kbee.web.eform;

import org.apache.wicket.model.IModel;

import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormMemberData;
import com.novamens.content.model.DataSetMember;

public class EMemMemberData extends EMemData implements EFormMemberData {
	private static final long serialVersionUID = 1L;
	
	private IModel<DataSetMember> model;
	
	public EMemMemberData(IModel<EForm> formModel, IModel<DataSetMember> memberModel) {
		super(formModel);
		setMember(memberModel);
	}
	
	public DataSetMember getMember() {
		return model.getObject();
	}
	
	public void setMember(IModel<DataSetMember> model) {
		this.model = model;
	}
	
	@Override
	public String getObjectTitle() {
		return getMember().getDisplayName();
	}
	
	public EFormData clone() {
		EMemData clone = new EMemMemberData(getModel(), model);
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