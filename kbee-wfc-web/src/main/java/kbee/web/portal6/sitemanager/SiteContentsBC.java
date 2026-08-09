package kbee.web.portal6.sitemanager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.portal6.model.Site;
import com.novamens.wicket.util.AjaxBCElement;

public class SiteContentsBC extends AjaxBCElement<Site> {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public SiteContentsBC(IModel<Site> model) {
		super("bc.site-contents", model);
	}
	
	
	@Override
	public IModel<String> getLabel() {
		return new Model<String> (this.getClass().getName());
	}

	
	public void onClick(AjaxRequestTarget target) {
		fire (new SiteManagerNavigationEvent(target, 
				 getModel(), 
				 this.getClass().getSimpleName().toLowerCase()));
	}
	
	
}
