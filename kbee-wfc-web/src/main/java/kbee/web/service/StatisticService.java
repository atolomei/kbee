package kbee.web.service;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.content.model.DataSetMember;
import com.novamens.service.ObjectService;
				
public interface StatisticService extends ObjectService {

	Panel getReport(String id);

	DataSetMember getMember();

}
