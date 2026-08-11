package kbee.web.model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.nav.DataSetBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.SeparatorBC;

public class DataSetsDropDownBC extends DropDownMenuBC<Void> {
	private static final long serialVersionUID = 1L;

	public DataSetsDropDownBC() {
		addElement(new DataSetsBC(), true);
		addElement(new DataSetsBC());
		addElement(new SeparatorBC());
		for (DataSet t:getContentDao().getDataSets(getDomain())) {
			addElement( new DataSetBC(new ObjectModel<DataSet> (t)));
		}
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}