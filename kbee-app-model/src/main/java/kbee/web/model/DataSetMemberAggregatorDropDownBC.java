package kbee.web.model;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.query.AggregationQuery;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.HREFBCElement;


import kbee.web.nav.DropDownMenuBC;

public class DataSetMemberAggregatorDropDownBC extends DropDownMenuBC<Void> {
	private static final long serialVersionUID = 1L;

	public DataSetMemberAggregatorDropDownBC(DataSet dataset, DataSetMember aggregator) {
		
		addElement(new HREFBCElement("bc-menu-item",
				"/dataset/"+dataset.getId().toString(), 
				new Model<String>(dataset.getName() + (dataset.isAggregation()? "<span class=\"ago\"> (" + new StringResourceModel("built-in", DataSetMemberAggregatorDropDownBC.this, null).getObject()+ ")</span>":"")
			)));
		
		AggregationQuery query = new AggregationQuery(getQueryIndex(), dataset, aggregator);
		query.setSort("title", true);
		ResultSet aggregations = query.execute();
		while (aggregations.hasNext()) {
			DataSetMember member = (DataSetMember)aggregations.next().getObject();
			//
			// String parent_ds=aggregator.getDataSet().getId().toString();
			// String parent_instance=aggregator.getId().toString(); 
			//
			//addElement(new HREFBCElement("bc-menu-item", 
			//			"/dataset/" + parent_ds + "/" + parent_instance + "/" + member.getId(), 
			//			new Model<String>(member.getDisplayName())));
			//
			addElement(new HREFBCElement("bc-menu-item", "/dataset/" + dataset.getId().toString() + "/" + member.getId(), new Model<String>(member.getDisplayName())));
		}
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

//	
//	protected ContentDao getContentDao() {
//		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
//
	
}