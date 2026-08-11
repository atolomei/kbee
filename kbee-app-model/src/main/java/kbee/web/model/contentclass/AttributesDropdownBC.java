package kbee.web.model.contentclass;

import java.util.Comparator;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.model.AttributeBC;
import kbee.web.model.AttributesBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.SeparatorBC;

public class AttributesDropdownBC extends DropDownMenuBC<Void> {
			
	private static final long serialVersionUID = 1L;

	/**	
	 * 
	 */
	public AttributesDropdownBC() {
		
		addElement(new AttributesBC(), true);
		addElement(new AttributesBC());
		addElement(new SeparatorBC());
		
		List<Attribute> list = getContentDao().getAttributes(getDomain());
		
		list.sort(new Comparator<Attribute>() {
			@Override
			public int compare(Attribute o1, Attribute o2) {
				try {
					return o1.getName().compareToIgnoreCase(o2.getName());
				} catch (Exception e) {
				return 0;
				}
			}
		});
		
		for (Attribute t: list) 
			addElement( new AttributeBC(new ObjectModel<Attribute> (t)));
		
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}
