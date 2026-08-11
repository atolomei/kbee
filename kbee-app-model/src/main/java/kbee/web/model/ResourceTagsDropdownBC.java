package kbee.web.model;

import java.util.Comparator;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.SeparatorBC;

public class ResourceTagsDropdownBC extends DropDownMenuBC<Void> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ResourceTagsDropdownBC.class.getName());
	
	public ResourceTagsDropdownBC() {
		addElement(new ClassifiersBC(), true);
		addElement(new ClassifiersBC());
		addElement(new SeparatorBC());
		
		List<Classifier> list=getContentDao().getClassifiers(getDomain());
		
		list.sort(new Comparator<Classifier>() {

			@Override
			public int compare(Classifier a, Classifier b) {
			    try {
			    	return a.getName().compareToIgnoreCase(b.getName());
				} catch (Exception e) {
					logger.error(e);
				}
				return 0;
			}
			
		});
		
		for (Classifier t: list)
			if (t.getState()!=ObjectState.DELETED)
				addElement(new ClassifierBC(new ObjectModel<Classifier> (t)));
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}	

}
