package kbee.web.nav;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.service.ServiceLocator;

public class SecurityDropDownMenuBC extends DropDownMenuBC<Void> {
		
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SecurityDropDownMenuBC() {
		
		 addElement(new SecurityBC());
		 addElement(new UsersBC());
		 addElement(new RolesBC());
			 
			 if (is_root) {
			 	  addElement(new GroupsBC(new Model<String>((new StringResourceModel("bc.groups", this, null).getObject())+" <span class=\"only-root\">(root)</span>")));
			 	  addElement(new RulesBC2(new Model<String>((new StringResourceModel("bc.rules", this, null).getObject())+" <span class=\"only-root\">(root)</span>")));
			 }
		 
	}
	
	//protected boolean isFreeVersion() {
	//	return getDomain().getDomainType()==DomainType.FREE;
	//}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
	
}
