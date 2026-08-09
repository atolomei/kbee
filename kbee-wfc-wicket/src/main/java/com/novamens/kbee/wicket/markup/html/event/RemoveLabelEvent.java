package com.novamens.kbee.wicket.markup.html.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.LabelMember;

public class RemoveLabelEvent<T extends Classificable> extends AbstractWicketAjaxEvent implements IDetachable {

	IModel<T> model;
	private static final long serialVersionUID = 1L;
	IModel<LabelMember> member_model;
	
	public RemoveLabelEvent(AjaxRequestTarget requestTarget, IModel<T> model, IModel<LabelMember> member_model) {
		super(requestTarget);
		this.model=model;
		this.member_model=member_model;
		
	}

	public IModel<T> getModel() {
		return model;
	}


	public 	IModel<LabelMember> getMemberModel() {
		return member_model; 
	
	}
	
	
	@Override
	public void detach() {
			if (model!=null)
				model.detach();
			
			if (member_model!=null)
				member_model.detach();
	}
	
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append( this.getClass().getName());
		if (model!=null)
			str.append( " | " +model.getObject().getDisplayName());
		return str.toString();
		
	}

}







