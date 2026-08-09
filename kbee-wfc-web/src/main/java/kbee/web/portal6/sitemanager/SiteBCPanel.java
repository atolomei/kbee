package kbee.web.portal6.sitemanager;

import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Site;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.portal6.editor.SiteDropDownBC;


/**
 * 
 * 
 * Site / Page / Area / Block
 * 
 * Site Editor
 * Site Pages
 * Site Contents
 * Site Reports
 * Site Security
 *  
 *----------------------------
 *
 * Site / Site Editor
 * Site / Site Contents
 * Site / Site Pages
 * 
 * Site / Site Pages / Page_1
 * 
 * 
 *
 *
 */
public class SiteBCPanel extends MenuBreadCrumbPanel<Site> {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String key;
	

	public SiteBCPanel( String key, IModel<Site> model ) {
		super("breadcrumb", model);
		this.key=key;
		setOutputMarkupId(true);
		
	}
	
	public void onInitialize() {
		super.onInitialize();
	
		addElement(new SiteDropDownBC(getModel()));
		
		//DropdownMenuBC dd = new DropdownMenuBC();
		//dd.addElement(new InformationModelBC(), true);
		//dd.addElement(new DataSetsBC());  
		//dd.addElement(new ClassifiersBC());
		//dd.addElement(new AttributesBC());
		//dd.addElement(new ContentClassesBC());
		
		//addElement(dd);
		
		addElement(new BCElement(key));
		
	}

}
