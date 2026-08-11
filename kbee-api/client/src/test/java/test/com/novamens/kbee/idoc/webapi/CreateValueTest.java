package test.com.novamens.kbee.idoc.webapi;


import org.junit.jupiter.api.Test;

import com.novamens.kbee.idoc.webapi.client.KbeeApiService;

import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiException;

public class CreateValueTest {

	@Test
	public void createValue() {
		try {
			
			KbeeApiService api = new KbeeApiService("https://test.kbee.io/api");
			api.setUser("root@indraiml");
			api.setPassword("1Aqqqqqq");
			
			ApiValue value = new ApiValue();
			value.setDataSet(new ApiProxy("/indraiml/datasets/type"));
			value.setDomain("indraiml");
			value.setName("TEST");
						
			ITransaction response = api.update(value);

			// System.out.println(response.getTarget().getHRef());
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
