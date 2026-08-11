package test.com.novamens.kbee.idoc.webapi;


import org.junit.jupiter.api.Test;

import com.novamens.kbee.idoc.webapi.client.KbeeApiService;

import kbee.api.model.ApiDataSet;
import kbee.api.service.ApiException;

public class ListDataSetsTest {

	@Test
	public void listDataSets() {
		try {
			
			KbeeApiService api = new KbeeApiService("https://test.kbee.io/api");
			api.setUser("root@indraiml");
			api.setPassword("1Aqqqqqq");
			
			// api = new KbeeApiService("http://test.kbee.io/api");
			// api.setUser("root@novamens");
			// api.setPassword("1Aqqqqqq");

			for (ApiDataSet dataset : api.getDataSets()) {
				// System.out.println(dataset.getDisplayName());
				// System.out.println(dataset.getAlias());
				// System.out.println("----");
			}
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
