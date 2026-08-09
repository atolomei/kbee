package kbee.web.portal6.sitemanager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.portal6.model.Site;
import com.novamens.wicket.util.AjaxBCElement;
import com.novamens.wicket.util.IBCElement;

public class SiteHomeBC extends AjaxBCElement<Site> implements IBCElement {
	
	public SiteHomeBC(IModel<Site> model) {
		super("bc.site-home", model);
	}
	
	@Override
	public void onClick(AjaxRequestTarget target) {
			fire (new SiteManagerNavigationEvent(target, 
											 getModel(), 
											 SiteHomeBC.this.getClass().getSimpleName().toLowerCase()));
	}
	

	@Override
	public boolean isAjax() {
		return true;
	}

	@Override
	public IModel<String> getLabel() {
		return new Model<String>(this.getClass().getSimpleName());
	}

}
