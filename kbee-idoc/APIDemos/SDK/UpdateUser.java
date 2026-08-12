package test.com.novamens.kbee.idoc.webapi;

import org.junit.Test;

import com.novamens.content.webapi.model.IProxy;
import com.novamens.content.webapi.model.IRole;
import com.novamens.content.webapi.model.ITransaction;
import com.novamens.content.webapi.model.IUser;
import com.novamens.content.webapi.model.IUserRole;
import com.novamens.content.webapi.model.IValue;
import com.novamens.content.webapi.service.ApiException;
import com.novamens.kbee.idoc.webapi.client.ApiService;

public class UpdateUserTest {
    
    @Test
    public void test1() {
        try {
            ApiService api = new ApiService("http://localhost:8080/api");
            
            api.setUser("root@windsor");
            api.setPassword("w1nds0rR00t");
            
            IUser user = api.get(IUser.class, "/windsor/users/71800");
            IRole role = api.get(IRole.class, "/windsor/roles/1");
            IValue entity = api.get(IValue.class, "/onesitedm/windsor/datasets/property/values/50853");
            user.setRole(new IUserRole(role, entity));
            ITransaction response = api.update(user);
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