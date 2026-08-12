package test.com.novamens.kbee.pjsf.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;

import com.novamens.content.webapi.model.IBinaryResource;
import com.novamens.content.webapi.model.IFile;
import com.novamens.content.webapi.model.ITransaction;
import com.novamens.content.webapi.service.ApiException;
import com.novamens.kbee.idoc.webapi.client.ApiService;

public class PostFile {
	@Test
	public void test() {
		try {

			ApiService api = new ApiService("http://localhost:8090/api");

			
			api.setUser("root@pmcdemo");
			api.setPassword("1234");
			
				
			IFile file = new IFile();

			// In this case the server is set up 
			// to dynamically create tags if they don't exist
	        //		
			
			file.setExternalId("123456789");  // this is the id at the source application (OneSite)
			file.setTitle("This is the title of the RPDD File"); 
			file.setDomain("pmcdemo"); // Domain is the client, normally a PMC
			file.setClassName("Compliance File"); // Template to use on RPDD (like Compliance File, Docusign Certificate)
			file.setAttribute("Document Type", "Lease"); 
			file.setAttribute("Site Name", "River Creek");
			file.setAttribute("Status", "Final");

            // We pass the local File as a stream, RPDD library will send it through the web to the server.

			IBinaryResource resource = new IBinaryResource();
			resource.setName(file.getExternalId()+".pdf");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("c:\apitest\testfile.pdf"), "UTF-8"));
			resource.setStream(reader);

			file.addResource(resource);

			ITransaction update = api.update(file);
			
			// System.out.println(update.getTarget().getHRef());
		
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
