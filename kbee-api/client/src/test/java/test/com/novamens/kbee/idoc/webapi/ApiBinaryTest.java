package test.com.novamens.kbee.idoc.webapi;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.novamens.kbee.idoc.webapi.client.KbeeApiService;

import kbee.api.model.ApiFile;
import kbee.api.model.IBinaryResource;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiException;

public class ApiBinaryTest {

	@Test
	public void test1() {
		
		try {
			KbeeApiService api = new KbeeApiService("http://localhost:8080/api");
			
			api.setUser("root@windsor");
			api.setPassword("1Aqqqqqq");
				
			ApiFile file = new ApiFile();
			
			file.setClassName("Resource");
			file.setTitle("TEST");
			file.setDomain("windsor");
			file.setExternalId("001");
			
			file.setAttribute("File Type", "Resource");
			
			file.addResource(new IBinaryResource(new File("c:\\temp\\Image1.JPG")));
			
			ITransaction response = api.update(file);

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
	
	//@Test
	public void test2() {
		try {
			KbeeApiService api = new KbeeApiService("http://localhost:8080/api");
			
			api.setUser("root@aerolineas");
			api.setPassword("1Aqqqqqq");
				
			ApiFile file = new ApiFile();
			
			file.setTitle("TEST 3");
			file.setApplication("aerolineas");
			file.setDomain("aerolineas");
			file.setClassName("Registro");
			file.setExternalId("003");
			
			file.setAttribute("Tipo Documento", "TEST");
			file.setAttribute("Grupo Emisor", "Administracion BCV");
			file.setAttribute("Confidencialidad", "Corporativa");
			file.setAttribute("Estado", "Approved");
			
			IBinaryResource resource1 = new IBinaryResource(new File("d:\\temp\\PhotoScan.JPG"));
			file.addResource(resource1);
			
			IBinaryResource resource2 = new IBinaryResource(new File("d:\\temp\\PhotoScan2.JPG"));
			file.addResource(resource2);
			
			ITransaction response = api.update(file);

			// System.out.println(response);
		}
		catch (ApiException e) {
			// System.out.println(e.getHttpStatus());
			// System.out.println(e.getErrorCode());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
