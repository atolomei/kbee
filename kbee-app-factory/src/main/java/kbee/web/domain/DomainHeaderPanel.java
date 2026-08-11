package kbee.web.domain;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.DomainsBC;
import kbee.web.nav.SettingsBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.object.TitleHeaderPanel;
	

/**
 *  Settings /  Windsor Compliance 
 */
public class DomainHeaderPanel extends TitleHeaderPanel<Domain> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainHeaderPanel.class.getName());

	IModel<String> icon = new Model<String>("far fa-building");
	
	public DomainHeaderPanel(IModel<Domain> model) {
		super("domain-panel", model);
		
		if (isDomainKbee()) {
			MenuBreadCrumbPanel  bc = new MenuBreadCrumbPanel();
			bc.addElement(new SettingsBC());
			bc.addElement(new DomainsBC());
			bc.addElement(new BCElement(new Model<String>(model.getObject().getDisplayName())));
			setBreadCrumbPanel(bc);
		}
		else {
			MenuBreadCrumbPanel  bc = new MenuBreadCrumbPanel();
			
			if (model.getObject().getDomainType()!=DomainType.EXPRESS)
				bc.addElement(new SettingsDropDownBC());
			else
				bc.addElement(new BCElement(new Model<String>("Settings")));
			
			bc.addElement(new BCElement( new StringResourceModel("general", this, null)));
			setBreadCrumbPanel(bc);
		}
	}

	public IModel<String> getTitle() {
		if (getModel().getObject().getOrganization()!=null)
			return  new PropertyModel<String>(getModel(), "organization");
	else
		 return new PropertyModel<String>(getModel(), "name"); 
	}
	
	
	protected boolean isDomainKbee() {
		try {
			return getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
	protected IModel<String> getGlyphicon() {
		return icon; 
	}


}
