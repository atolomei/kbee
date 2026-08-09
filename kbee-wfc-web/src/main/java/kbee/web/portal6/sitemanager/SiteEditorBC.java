package kbee.web.portal6.sitemanager;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.event.Event;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.util.AjaxBCElement;
import com.novamens.wicket.util.IBCElement;


public class SiteEditorBC extends AjaxBCElement<Site>  implements IBCElement {
	
	private static final long serialVersionUID = 1L;

	public SiteEditorBC(IModel<Site> model) {
		super("bc.site-editor", model);
	}
	
	@Override
	public void onClick(AjaxRequestTarget target) {
			fire (new SiteManagerNavigationEvent(target, 
											 getModel(), 
											 SiteEditorBC.this.getClass().getSimpleName().toLowerCase()));
	}
	

	@Override
	public IModel<String> getLabel() {
		return new Model<String> (this.getClass().getName());
	}


}
