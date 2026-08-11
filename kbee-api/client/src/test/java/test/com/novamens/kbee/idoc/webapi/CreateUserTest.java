package test.com.novamens.kbee.idoc.webapi;
//
//import java.time.OffsetDateTime;
//import java.util.Locale;
//
//import org.junit.Test;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.novamens.content.webapi.model.IFile;
//import com.novamens.content.webapi.model.IProxy;
//import com.novamens.content.webapi.model.ITransaction;
//import com.novamens.content.webapi.model.IUser;
//import com.novamens.content.webapi.model.IUserRole;
//import com.novamens.content.webapi.service.ApiException;
//import com.novamens.kbee.idoc.webapi.client.ApiService;
//
//public class CreateUserTest {
//
//	@Test
//	public void test1() {
//		try {
//
//		//	ApiService api = new ApiService("https://testing-rpdd.realpage.com/api");
//			ApiService api = new ApiService("http://localhost:8080/api");
//
//			api.setUser("root@greystar");
//			api.setPassword("1Aqqqqqq");
//
//			IUser user = new IUser();
//
//			//user.setId("94752");
//			user.setName("alejo");
//			user.setDomain("greystar");
//			user.setFirstName("Jhon");
//			user.setLastName("Smith");
//			user.setEmail("juan@novamens");
//			user.setLocale(Locale.ENGLISH);
//			user.setTimeZone("US/Eastern");
//			user.setRole(new IUserRole(new IProxy("/greystar/roles/50501"), null));
//			//user.setRole(new IUserRole(new IProxy("/greystar/roles/50501"), new IProxy("/greystar/datasets/sitename/values/50766")));
//
//			GsonBuilder b = new GsonBuilder();
//			Gson gson = b.create();
//
//			//String j ="{\"name\":\"s0906dmru\",\"firstName\":\"sandeep\",\"lastName\":\"0906Dmru\",\"email\":\"sandeep_0906Dmru@test.com\",\"timeZone\":\"US/Central\",\"enabled\":true,\"locale\":\"en\",\"roles\":[{\"role\":{\"id\":\"79850\",\"href\":\"/cwsapartments/roles/79850\",\"name\":\"Auditor\"}},{\"role\":{\"id\":\"79901\",\"href\":\"/cwsapartments/roles/79901\",\"name\":\"Deparment Manager\"}}],\"groups\":[],\"domain\":\"cwsapartments\"}";
//
//			//user = gson.fromJson(j, IUser.class);
//
//			//ITransaction response;
//			ITransaction response = api.create(user);
//			//ITransaction response = api.update(user);
//
//			//// System.out.println(response);
//			//user = api.get(IUser.class, \"/demoapi/users/52702");
//
//			response = api.delete(user);
//
//			// System.out.println(response);
//		}
//		catch (ApiException e) {
//			// System.out.println(e.getHttpStatus());
//			// System.out.println(e.getErrorCode());
//			// System.out.println(e.getMessage());
//		}
//		catch (Exception e) {
//			// System.out.println(e.getMessage());
//		}
//	}
//}
