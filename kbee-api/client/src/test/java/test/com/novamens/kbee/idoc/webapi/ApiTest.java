package test.com.novamens.kbee.idoc.webapi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Predicate;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.jupiter.api.Test;

import com.novamens.kbee.idoc.webapi.client.KbeeApiService;

import kbee.api.model.*;
import kbee.api.service.ApiException;

public class ApiTest {
	

	//@Test
	public void test0() {
		try {
			KbeeApiService api = new KbeeApiService("http://localhost:8080/api");

			api.setUser("root@novamens");
			api.setPassword("1Aqqqqqq");

			ApiFile file = new ApiFile();
			
			file.setTitle("RECURSO");
			
			file.setClassName("Recurso");
			

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
	public void tokenTest() {
		try {
			
			int s = 25;
			int t = 26;
			
			int h = t / s + (t % s>0 ? 1 : 0);
			int p =  t % s;
			
			
			KbeeApiService api = new KbeeApiService("http://localhost:8080/api");


			api.setUser("root@novamens");
			api.setPassword("1Aqqqqqq");

			IDevice device = new IDevice();
			
			device.setId("phone");

			IToken response = api.getToken();
			
			// System.out.println(response.getValue());
			// System.out.println(response.getLifeTime());
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
	
	@Test
	public void test1() {
		try {
			KbeeApiService api = new KbeeApiService("http://localhost:8080/api");


			api.setUser("root@kbee");
			api.setPassword("1Aqqqqqq");

			ApiFile file = new ApiFile();
			
			file.setTitle("DEMO UNIT TEST 01");
			
			file.setDomain("novamens");
			file.setClassName("Documento");
			

			
			file.setAttribute("Status", "Final");

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
			
			api.setUser("root@windsor");
			//api.setPassword("1Aqqqqqq");
			api.setPassword("w1nR00tw");
 			
			List<ApiDataSet> datasets = api.getDataSets();
			
			for (ApiDataSet dataset : datasets) {
				// System.out.println(dataset.getDisplayName());
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
	
	//@Test
	public void test3() {
		
		try {
			
			KbeeApiService api = new KbeeApiService("http://localhost:8087/api");
			
			api.setUser("root@windsor");
			api.setPassword("w1nR00tw");
			
			
 			
			ApiDataSet dataset = api.getDataSet("50002");
			
			// System.out.println(dataset.getDisplayName());
			
			IResultSet<ApiValue> values = api.getValues(dataset);
			
			int i = 0;
			while (values.hasNext()) {
				ApiValue value = values.next();
				// System.out.println(String.valueOf(i++) + " " + value.getName());
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
	
	//@Test
	public void test4() {
		try {
			
			KbeeApiService api = new KbeeApiService("http://localhost:8087/api");
			
			api.setUser("root@windsor");
			api.setPassword("w1nR00tw");
 			
			List<ApiClassifier> classifiers = api.getClassifiers();
			
			for (ApiClassifier classifier : classifiers) {
				ApiDataSet dataSet = api.get(ApiDataSet.class, classifier.getDataSet().getHRef());
				// System.out.println(classifier.getDisplayName());
				// System.out.println(dataSet.getDisplayName());
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
	
	//@Test
	public void test5() {
		try {
			
			KbeeApiService api = new KbeeApiService("http://localhost:8087/api");
			
			api.setUser("root@windsor");
			api.setPassword("w1nR00tw");
 			
			IResultSet<ApiProxy> users = api.getUsers();
			
			int i = 0;
			while (users.hasNext()) {
				ApiProxy user = users.next();
				// System.out.println(String.valueOf(i++) + " " + user.getName());
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
	
	//@Test
	public void test6() {
		try {
			
			KbeeApiService api = new KbeeApiService("http://localhost:8080/api");
			
			api.setUser("root@windsor");
			api.setPassword("w1nR00tw");
 			
			IResultSet<ApiProxy> groups = api.getGroups();
			
			int i = 0;
			while (groups.hasNext()) {
				ApiProxy proxy = groups.next();
				IGroup group = api.get(IGroup.class, proxy.getHRef());
				// System.out.println(String.valueOf(i++) + " " + group.getName());
			}

		}
		catch (ApiException e) {
			// System.out.println(e.getHttpStatus());
			// System.out.println(e.getErrorCode());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			// System.out.println(e.getMessage());
		}
 	}
	
	//@Test
	public void test7() {
		try {
			
			KbeeApiService api = new KbeeApiService("http://localhost:8080/api");
			
			api.setUser("root@windsor");
			api.setPassword("1Aqqqqqq");
 			
			//IResultSet<ApiFile> files = api.select("property(Lumina Square)");
			
			ApiFile file = api.getFile("/file/1351750/1295100");
			
			//while (files.hasNext()) {
				//ApiFile file = files.next();
				for (ApiResource resource : file.getResources()) {
					// System.out.println(resource.getHRef());
					api.getResource(resource.getHRef());
				}
				// System.out.println(file.getDisplayName());
			//}
		}
		catch (ApiException e) {
			// System.out.println(e.getHttpStatus());
			// System.out.println(e.getErrorCode());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			// System.out.println(e.getMessage());
		}
		
 	}


	//@Test
	public void test8() {
		
		try {
			for (int a=0; a<2; a++) {
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
			javax.script.Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
			bindings.put("polyglot.js.allowHostAccess", true);
			bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) s -> true);
			Integer amount = new Integer(50);
			bindings.put("amount", amount);
			Object eval = engine.eval("if (amount > 10) {'task1'} else {'task2'}");
			// System.out.println(eval);
			}
		}
		catch (ScriptException e) {
			e.printStackTrace();
		}

		KbeeApiService api = new KbeeApiService("http://localhost:8080/api");


		api.setUser("root@kbee");
		api.setPassword("roott");
		//api.setUser("root@edgewood");
		//api.setPassword("root");

		ApiFile file = new ApiFile();

		file.setClassName("Compliance File");
		//file.setClassName("DocuSign Certificate");

		//file.setTitle("Certificate TEST");
		file.setTitle("OneSite TEST v2");
		file.setDomain("windsor");

		//file.setWorkspace();

		file.setApplication("onesitedm");
		//file.setExternalId("020");
		file.setExternalId("13");

		file.setAttribute("File Type", "TC Transfer");
		//file.setAttribute("Document Type", "DocuSign Certificate");
		//file.setAttribute("Cabinet", "Leasing2");
		file.setAttribute("Status", "Active");

		file.setAttribute("Effective Date", "2019-02-29");

		file.setAttribute("property", "Willow Oaks");

		file.setAttribute("unit", "360F");

		for (int i = 0; i < 1; i++) {
			String resourceName = "file" + i +".txt";

			String resourceContent = "Texto de prueba" + i;
			InputStream inputStream = new ByteArrayInputStream(resourceContent.getBytes(Charset.forName("UTF-8")));

			IBinaryResource resource = new IBinaryResource();
			resource.setTitle("file" + i );
			resource.setName(resourceName);
			resource.setStream(inputStream);
			file.addResource(resource);
		}

		ITransaction response = api.update(file);

		String hRef = response.getTarget().getHRef();
		ApiFile ApiFile = api.get(ApiFile.class, hRef);


		// System.out.println(response);

	}
	
//	@Test
//	public void test8() {
//		try {
//			
//			ApiService api = new ApiService("http://localhost:8080/api");
//			
//			api.setUser("root@windsor");
//			api.setPassword("w1nR00tw");
//			
//			api.testbin();
// 			
//		}
//		catch (ApiException e) {
//			// System.out.println(e.getHttpStatus());
//			// System.out.println(e.getErrorCode());
//			// System.out.println(e.getMessage());
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			// System.out.println(e.getMessage());
////		}
// 	}

}
