package kbee.web.query;

import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;

import kbee.web.security.UsersQuery;

public class DashboardRulesQuery extends UsersQuery {

	private static final long serialVersionUID = 1L;

	public DashboardRulesQuery(Index index, DataSet dataset, IModel<Classifier> iModel) {
		super(index, dataset);
		getParameters().put("state", "["+String.valueOf(ObjectState.ENABLED.getId())+", "+String.valueOf(ObjectState.ARCHIVED.getId())+"]");
		getParameters().put("+"+ iModel.getObject().getUniqueName().trim()+"member", "*");
		// getParameters().put("+propertymember", "*");
		
	}

	
	/**
	 * UserProfile
	 * SiteProfile
	 * PMCProfile
	 * ComplianceProfile
	 * member=propertymember/52304*,sort=modified,state=[1,  2],type=datasetmember,ascending=false, dataset=50002
	 */

}
