package test.com.novamens.kbee.idoc.webapi;


import org.junit.jupiter.api.Test;

import com.novamens.kbee.idoc.webapi.client.KbeeApiService;

import kbee.api.model.ApiFile;
import kbee.api.model.ITransaction;
import kbee.api.model.ApiUser;
import kbee.api.service.ApiException;

public class UpdateFileTest {
	
	@Test
	public void test1() {
		try {
			KbeeApiService api = new KbeeApiService("http://localhost:8080/api");
			
			api.setUser("root@aerolineas");
			api.setPassword("1Aqqqqqq");
			
			ApiFile file = api.get(ApiFile.class, "/file/0/582450");
			file.setAttribute("estado", "Edición");
			
			ITransaction response = api.update(file);
		}
		catch (ApiException e) {
			System.out.println(e.getHttpStatus());
			System.out.println(e.getErrorCode());
			System.out.println(e.getMessage());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
