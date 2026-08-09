package kbee.web.portal6.editor;

import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.nav.SitesBC;


public class SiteDropDownBC extends DropDownMenuBC<Site> {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SiteDropDownBC.class.getName());

	/**
	 * SitesBC
	 * Site
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SiteDropDownBC(IModel<Site> model) {
		super(model);
		 
		 // addElement(new SiteEditorBC(getModel()));
		 // addElement(new SitePagesBC());
		 // addElement(new SiteContentsBC(getModel()));
		 // addElement(new SiteReportsBC());
		 // addElement(new SiteSecurityBC());
	}
	
	public void onInitialize() {
		
		addElement(new SiteBC(getModel()), true);
		
		addElement(new SiteBC(getModel()));
		
		
		addElement(new SeparatorBC());
		
		
		List<Page> pages=getModel().getObject().getPages();
		pages.sort(new Comparator<Page>(){

			@Override
			public int compare(Page o1, Page o2) {

				try {
					
					if (o1.getOrder()<o2.getOrder()) return -1;
					if (o1.getOrder()>o2.getOrder()) return 1;
					
					if (o1.getTitle()==null) return 1;
					if (o2.getTitle()==null) return -1;
					
					return o1.getTitle().compareToIgnoreCase(o2.getTitle());
					
				} catch (Exception e) {
					logger.error(e);
				}
				return 0;

			}
			
		});
		
		for (Page p: pages) {
			if (p.getState()==ObjectState.ENABLED && !p.isSiteSection())
				addElement(new SitePageBC( new ObjectModel<Page>(p)));
		}
		
		//addElement(new SiteHomeBC(getModel()));
		//addElement(new SiteEditorBC(getModel()));
		//addElement(new SiteContentsBC(getModel()));
		
		super.onInitialize();
			
	}
}
