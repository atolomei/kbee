package kbee.web.nav;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.DataSetMember;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;

public class MemberBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	private IModel<DataSetMember> model;
	
	public MemberBC(DataSetMember member) {
		super();
		model = new ObjectModel<DataSetMember>(member);
	}
	
	public IModel<String> getLabel() {
		return new Model<String>(getMember().getDisplayName());
	}
	
	@Override
	public void onClick() {
	}	
	
	public DataSetMember getMember() {
		return model.getObject();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		model.detach();
	}
	
	@Override
	protected IModel<String> newLabel() {
		return null;
	}
	
}
