package kbee.web.model;

import com.novamens.content.model.DataSet;
import com.novamens.wicket.util.BCElement;

public class DataSetsBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	public DataSetsBC() {
		super("bc.datasets");
	}
	
	@Override
	public void onClick() {
		setResponsePage(new kbee.web.model.DataSetsPage<DataSet>());
	}
}
