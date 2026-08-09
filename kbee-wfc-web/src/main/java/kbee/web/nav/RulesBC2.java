package kbee.web.nav;

import org.apache.wicket.model.IModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class RulesBC2 extends BCElement {
	private static final long serialVersionUID = 1L;

	public RulesBC2() {
		super("bc.rules");
	}
	
	public RulesBC2(IModel<String> title) {
		super(title);
	}
	
	@Override
	public void onClick() {
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("security-rules-page"));
	}

	protected boolean isFreeVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
		
	}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
	
}
