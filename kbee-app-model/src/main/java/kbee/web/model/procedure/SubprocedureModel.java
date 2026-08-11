package kbee.web.model.procedure;

import org.apache.wicket.model.IModel;

import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;

public class SubprocedureModel implements IModel<Procedure> {
	private static final long serialVersionUID = -9135543358877718292L;
	
	private long id;
	private IModel<Procedure> proceduremodel;
	private transient Procedure subprocedure;
	
	public SubprocedureModel(Procedure procedure) {
		proceduremodel = new ObjectModel<Procedure>(procedure.getMaster());
		this.subprocedure = procedure;
	}
	
	public Procedure getObject() {
		if (this.subprocedure==null) {
			Procedure master = proceduremodel.getObject();
			for (Procedure procedure : master.getSubprocedures()) {
				if ((long)procedure.getId()==id) {
					this.subprocedure = procedure;
					break;
				}
			}
			
		}
		return subprocedure;
	}
	
	public void detach() {
		if (subprocedure!=null) {
			this.id = (long)subprocedure.getId();
			subprocedure=null;
			proceduremodel.detach();
		}
	}
}