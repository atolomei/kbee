package test.com.novamens.kbee.idoc.webapi;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kbee.api.model.ApiFile;
import kbee.api.model.ITransaction;

public class DealRoomUpdate {
	
	@Test
	public void test01() {
		try {
			
			String uri = "https://testing-rpdm.realpage.com/api/file/onesitedm/demo/2c317ea9-b9f4-4f07-9654-7e43b431fe12";
			uri = "http://demo-rpddapi.realpage.com/api/file/onesitedm/demo/2c317ea9-b9f4-4f07-9654-7e43b431fe12";

//			String j = "{"+ 
//				"\"externalid\":\" f4e37ecd-b0a0-43aa-bc8b-30c9fd2905fe\","+
//				"\"application\":\"onesitedm\","+
//				"\"classname\":\"DealRoom\","+
//				"\"title\":\"Test DR2\","+
//				//"\"lastModifiedDate\":\"2019-10-29T15:34:02.9970000-05:00\","+
//				"\"domain\":\"demo\","+
//				"\"seededattributes\":["+
//					"{"+ 
//						"\"attribute\":{"+ 
//							"\"name\":\"Filter Condition\""+
//						"},"+
//						"\"values\":["+ 
//							"{"+ 
//								"\"name\":\"Filter Category : [Resident] and Filter Document Type : [Lease Documents].\""+
//							"}"+
//						"]"+	
//					"}"+
//				"],"+		
//				"\"resources\":["+ 
//					"{"+ 
//						"\"href\":\"http://int2016b.pct.realpage.com/WebServices/DocumentManagement/Document.asmx\","+
//						"\"controlattributes\":["+
//							"{"+ 
//								"\"attribute\":\"PMCID\","+
//								"\"value\":\"8000108\""+
//							"},"+
//							"{"+	 
//								"\"attribute\":\"SiteID\","+
//								"\"value\":\"8000109\""+
//							"},"+
//							"{"+ 
//								"\"attribute\":\"UserID\","+
//								"\"value\":\"68519525\""+
//							"},"+
//							"{"+ 
//								"\"attribute\":\"SessionGUID\","+
//								"\"value\":\"4554DFA3-BA7C-498F-A3E1-C47D01518791\""+
//							"}"+
//						"]"+
//					"}"+
//				"]"+		
//			"}";
			

			String j = "{\"version\":0,\"title\":\"RPDD_02182020\",\"application\":\"onesitedm\",\"externalid\":\"2c317ea9-b9f4-4f07-9654-7e43b431fe12\",\"classname\":\"DealRoom\",\"resources\":[{\"href\":\"http://datafix2016a.onesite.realpage.com/WebServices/DocumentManagement/Document.asmx\",\"controlattributes\":[{\"attribute\":\"PMCID\",\"value\":\"4630849\"},{\"attribute\":\"SiteID\",\"value\":\"4630850\"},{\"attribute\":\"UserID\",\"value\":\"86474846\"},{\"attribute\":\"SessionGUID\",\"value\":\"4554DFA3-BA7C-498F-A3E1-C47D01518791\"}]}],\"seededattributes\":[{\"attribute\":{\"name\":\"Filter Condition\"},\"values\":[{\"name\":\"Filter Category : [Non Resident]\"},{\"name\":\"Filter Document Type : [Lease Documents]\"}]}],\"domain\":\"demo\"}";
		
			GsonBuilder b = new GsonBuilder();
			Gson gson = b.create();

			ApiFile file = gson.fromJson(j, ApiFile.class);
			
			file.setLastModifiedDate(OffsetDateTime.now());
			
			String json = gson.toJson(file);
			
			// System.out.println(json);
			
			RestTemplate restTemplate = new RestTemplate();
			
			List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
			converters.add(new GsonHttpMessageConverter());
			restTemplate.setMessageConverters(converters);
			
			ResponseEntity<ITransaction> response = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<ApiFile>(file, getHeaders()), ITransaction.class);
			
			// System.out.println(response.getBody());
			// System.out.println(response);
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			// System.out.println(e.getMessage());
		}
	}
	
	private HttpHeaders getHeaders(){
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + getCredentials());
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		return headers;
	}
	
	private String getCredentials(){
	//	String plainCredentials="root@aim:1Aqqqqqq";
	//	String plainCredentials="root@windsor:w1nds0rR00t";
	//	String plainCredentials="root@kbee:id0cB4sic";
	//	String plainCredentials="root@kbee:1Aqqqqqq";
		String plainCredentials="root@kbee:r00tKbee";
		String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
		return base64Credentials;
	}
}
