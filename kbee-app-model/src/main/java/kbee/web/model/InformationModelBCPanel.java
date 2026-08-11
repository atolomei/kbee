package kbee.web.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.model.contentclass.ContentClassesBC;
import kbee.web.model.procedure.LauncherGroupsBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.InformationModelSectionBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.nav.SettingsDropDownBC;


/** 
 * 
 * use instead:  InformationModelDropDownBC
 *
 */

public class InformationModelBCPanel extends MenuBreadCrumbPanel<Void> {
	private static final long serialVersionUID = 1L;
	
	private String key;
	
	public InformationModelBCPanel( String key ) {
		this.key=key;
	}
	
	public void onInitialize() {
		super.onInitialize();
	
		addElement( new HomeBC());
		
		addElement(new SettingsDropDownBC());
		addElement(new InformationModelDropDownBC());
		
		
		
		/**
		DropDownMenuBC<Void> dd = new DropDownMenuBC<Void>();
		dd.addElement(new InformationModelBC(), true);
		dd.addElement(new InformationModelSectionBC());
		dd.addElement(new SeparatorBC());
		List<BCElement> list = new ArrayList<BCElement>();
		list.add(new AttributesBC());
		list.add(new DataSetsBC());
		list.add(new ClassifiersBC());
		list.add(new ContentClassesBC());
		list.add(new SeparatorBC());
		list.add(new ResourceTagsBC());  
		list.add(new LauncherGroupsBC());
		list.sort( 
			new Comparator<BCElement>() {
			@Override
			public int compare(BCElement a, BCElement b) {
				try {
						return a.getLabel().getObject().compareToIgnoreCase(b.getLabel().getObject());
				} catch (Exception e) {
					return 0;
				}
			}
		});
		list.forEach( item -> dd.addElement(item));
		addElement(dd);
		**/
  		
		addElement(new BCElement(key));
	}
}
