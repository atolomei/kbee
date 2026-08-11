package test.com.novamens.kbee.idoc.webapi;


import com.novamens.kbee.idoc.webapi.client.KbeeApiService;

import kbee.api.model.ApiProxy;
import kbee.api.model.ITransaction;
import kbee.api.model.ApiUser;
import kbee.api.model.IUserRole;
import kbee.api.service.ApiException;

public class UpdateUserTest {
	
	public void test1() {
		try {
			KbeeApiService api = new KbeeApiService("http://localhost:8080/api");
			
			api.setUser("root@novamens");
			api.setPassword("1Aqqqqqq");
			
			ApiUser user = api.get(ApiUser.class, "/users/39020");
			
			//user.setExternalId("100");
				
			//IRole role = api.get(IRole.class, "/windsor/roles/1");
			
			//IValue entity = api.get(IValue.class, "/onesitedm/windsor/datasets/property/values/50853");
			
			//user.setRole(new IUserRole(new IProxy("/windsor/roles/1"), new IProxy("/onesitedm/windsor/datasets/property/values/50853")));
			//user.setRole(new IUserRole(role, entity));
						
			//ITransaction response = api.update(user);
			ITransaction response = api.delete(user);

			// System.out.println(response);
		}
		catch (ApiException e) {
			// System.out.println(e.getHttpStatus());
			// System.out.println(e.getErrorCode());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			// System.out.println(e.getMessage());
		}
	}
}
