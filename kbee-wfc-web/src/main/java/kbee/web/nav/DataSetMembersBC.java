package kbee.web.nav;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.DataSet;
import com.novamens.wicket.util.HREFBCElement;

public class DataSetMembersBC extends HREFBCElement {

	private static final long serialVersionUID = 1L;
							
	private IModel<DataSet> ds;
	
	public DataSetMembersBC(IModel<DataSet> ds, Model<String> label) {
		super("/dataset/"+ds.getObject().getId().toString());
		setLabel(label);
		this.ds=ds;
	}
	
	public DataSetMembersBC(IModel<DataSet> ds) {
		super("/dataset/"+ds.getObject().getId().toString());
		
		if (ds.getObject().isAggregation()) 
			setLabel(new Model<String>(ds.getObject().getName() + " <span class=\"ago\">("+ new StringResourceModel("built-in", DataSetMembersBC.this, null).getObject() +")</span>"));
		else
			setLabel(new Model<String>(ds.getObject().getName() ));

		this.ds=ds;
	}
	
	
    @Override
    public void onDetach() {
    	super.onDetach();
    	if (ds!=null)
    		ds.detach();
    	
    }

}
