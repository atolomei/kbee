package kbee.web.model.procedure;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.workflow.Procedure;

public class ProcedureBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	private IModel<Procedure> model;
	
	public  ProcedureBC(IModel<Procedure> model) {
		super();
		this.model = new ObjectModel<Procedure>(model.getObject().getMaster());
	}
	
	public  ProcedureBC(Procedure procedure) {
		super();
		this.model = new ObjectModel<Procedure>(procedure.getMaster());
	}
	
	@Override
	public IModel<String> getLabel() {
		return new Model<String>(this.model.getObject().getDisplayName() + " <span class=\"ago\"> (" + getLabelString("procedure") +")</span>");
	}
	
	@Override
	public void onClick() {
		setResponsePage(new ProcedurePage(this.model));
	}
}