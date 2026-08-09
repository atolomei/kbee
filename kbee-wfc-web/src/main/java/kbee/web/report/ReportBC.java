package kbee.web.report;

import org.apache.wicket.model.Model;


import com.novamens.wicket.util.HREFBCElement;

public class ReportBC extends HREFBCElement {

	public ReportBC(ReportFactory tf) {
		super("link", "/reports/"+tf.getReportGroup()+"/"+tf.getKey(), new Model<String>(tf.getDisplayName()));
	}
	
	
	
}
