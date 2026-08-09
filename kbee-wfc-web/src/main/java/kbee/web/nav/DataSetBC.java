package kbee.web.nav;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.model.DataSet;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

@SuppressWarnings("serial")
public class DataSetBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	private IModel<DataSet> model;
	
	public DataSetBC(IModel<DataSet> model) {
		super();
		this.model = model;
	}
	
	public DataSetBC(DataSet dataset) {
		super();
		model = new ObjectModel<DataSet>(dataset);
	}
	
	@Override
	public void onClick() {
		PageParameters pa= new PageParameters();
	    pa.add("id", model.getObject().getId().toString());
		setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("model-dataset-page", pa));
	}	

	@Override
	public void onDetach() {
		super.onDetach();
		model.detach();
	}
	
	@Override
	protected IModel<String> newLabel() {
		return new Model<String>() {
			public String getObject() {
				return DataSetBC.this.model.getObject().getName() + " " + (DataSetBC.this.model.getObject().isAggregation()? "<span class=\"ago\">(Built-in)</span>":"");
			}
		};
	}
	
}
