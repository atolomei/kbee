package kbee.web.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.novamens.wicket.util.BCElement;

import kbee.web.model.contentclass.ContentClassesBC;
import kbee.web.model.procedure.LauncherGroupsBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.InformationModelSectionBC;
import kbee.web.nav.SeparatorBC;

public class InformationModelDropDownBC extends DropDownMenuBC<Void> {
	private static final long serialVersionUID = 1L;

	public InformationModelDropDownBC() {
		
		
		
		
		 addElement(new InformationModelBC(), true);
		 
		 
		  
		 
		 addElement(new InformationModelSectionBC());
		 addElement(new SeparatorBC());
		 
		 //addElement(new AttributesBC());
		 //addElement(new ClassifiersBC());
		 //addElement(new ContentClassesBC());
		 //addElement(new DataSetsBC());
		 //addElement(new ResourceTagsBC());
		 
			
			List<BCElement> list = new ArrayList<BCElement>();
			
			list.add(new AttributesBC());
			list.add(new ClassifiersBC());
			list.add(new DataSetsBC());
			list.add(new ContentClassesBC());
			list.add(new SeparatorBC());

			list.add(new ResourceTagsBC());  
			list.add(new LauncherGroupsBC());
			
			
			/**
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
			**/
			
			list.forEach( item -> addElement(item));
	  		
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
}
