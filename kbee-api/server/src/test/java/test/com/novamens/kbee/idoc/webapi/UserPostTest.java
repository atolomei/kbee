package test.com.novamens.kbee.idoc.webapi;

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

import kbee.api.model.ITransaction;
import kbee.api.model.ApiUser;

public class UserPostTest {
	
	@Test
	public void test01() {
		try {

			String uri = "http://localhost:8080/api/demoapi/users/96700";
			
			GsonBuilder b = new GsonBuilder();
			Gson gson = b.create();

	//		String j =	"{\"name\":\"jdavis3\",\"firstName\":\"John\",\"lastName\":\"Davis\",\"email\":\"jdavis@bogusemail.com\",\"timeZone\":\"US/Central\",\"enabled\":true,\"locale\":\"en\",\"roles\":[{\"role\":{\"id\":\"70700\",\"href\":\"/demoapi/roles/70700\",\"name\":\"Property Manager\"},\"entity\":{\"id\":\"74002\",\"href\":\"/onesitedm/demoapi/datasets/sitename/values/74002\",\"name\":\"SAT387 Alamo Hillside\",\"rel\":\"entity\"}},{\"role\":{\"id\":\"70700\",\"href\":\"/demoapi/roles/70700\",\"name\":\"Property Manager\"},\"entity\":{\"id\":\"71650\",\"href\":\"/onesitedm/demoapi/datasets/sitename/values/71650\",\"name\":\"Quarter Mill\",\"rel\":\"entity\"}},{\"role\":{\"id\":\"70700\",\"href\":\"/demoapi/roles/70700\",\"name\":\"Property Manager\"},\"entity\":{\"id\":\"71560\",\"href\":\"/onesitedm/demoapi/datasets/sitename/values/71560\",\"name\":\"atolomei-test-1\",\"rel\":\"entity\"}},{\"role\":{\"id\":\"70700\",\"href\":\"/demoapi/roles/70700\",\"name\":\"Property Manager\"},\"entity\":{\"id\":\"71554\",\"href\":\"/onesitedm/demoapi/datasets/sitename/values/71554\",\"name\":\"The District\",\"rel\":\"entity\"}}],\"groups\":[],\"domain\":\"demoapi\"}";
			String j = "{\"name\":\"alejo@demoapi\",\"firstName\":\"Jhon\",\"lastName\":\"Smith\",\"email\":\"juan@novamens\",\"timeZone\":\"US/Eastern\",\"enabled\":true,\"locale\":\"es\",\"roles\":[{\"role\":{\"id\":\"94900\",\"href\":\"/demoapi/roles/94900\",\"name\":\"Department Manager\",\"rel\":\"role\"},\"entity\":{\"id\":\"95203\",\"href\":\"/demoapi/datasets/department/values/95203\",\"name\":\"Legal\",\"rel\":\"entity\"}},{\"role\":{\"id\":\"96750\",\"href\":\"/demoapi/roles/96750\",\"name\":\"DOMAIN ADMIN\",\"rel\":\"role\"}}],\"groups\":[{\"href\":\"/demoapi/groups/2351\",\"name\":\"enterprise\",\"rel\":\"group\"},{\"href\":\"/demoapi/groups/2355\",\"name\":\"mytasks-bulk-actions\",\"rel\":\"group\"},{\"href\":\"/demoapi/groups/2357\",\"name\":\"pending-tasks\",\"rel\":\"group\"},{\"href\":\"/demoapi/groups/1126\",\"name\":\"mytasks\",\"rel\":\"group\"},{\"href\":\"/demoapi/groups/1123\",\"name\":\"auditor\",\"rel\":\"group\"},{\"href\":\"/demoapi/groups/1120\",\"name\":\"domain-admin\",\"rel\":\"group\"},{\"href\":\"/demoapi/groups/1117\",\"name\":\"user\",\"rel\":\"group\"}],\"id\":\"96700\",\"domain\":\"demoapi\"}";
	
			ApiUser user = gson.fromJson(j, ApiUser.class);
					
			RestTemplate restTemplate = new RestTemplate();
			
			List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
			converters.add(new GsonHttpMessageConverter());
			restTemplate.setMessageConverters(converters);
			
			ResponseEntity<ITransaction> response = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<ApiUser>(user, getHeaders()), ITransaction.class);
			//ResponseEntity<ITransaction> response = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<IUser>(getHeaders()), ITransaction.class);
			
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
		String plainCredentials="root@demoapi:1Aqqqqqq";
		String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
		return base64Credentials;
	}
}
