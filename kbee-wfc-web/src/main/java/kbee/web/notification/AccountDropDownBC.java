package kbee.web.notification;

import kbee.web.nav.AccountBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.MyNotesBC;
import kbee.web.nav.UserAlertsBC;
import kbee.web.nav.UserSettingsBC;

public class AccountDropDownBC extends DropDownMenuBC<Void> {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AccountDropDownBC() {
						
		addElement(new  AccountBC());
		addElement(new  UserSettingsBC());
		//addElement(new UserAlertsBC());
		//addElement(new  MyNotesBC());
	}
	
}
