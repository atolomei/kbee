package test.com.novamens.kbee.sms;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.messagebird.MessageBirdClient;
import com.messagebird.MessageBirdService;
import com.messagebird.MessageBirdServiceImpl;
import com.messagebird.exceptions.GeneralException;
import com.messagebird.exceptions.UnauthorizedException;
import com.messagebird.objects.MessageResponse;

public class SmsTest {
	
	
	
	@Test
	public void test() {

	      // First create your service object
	      final MessageBirdService wsr = new MessageBirdServiceImpl("xO2aY0BYu6jorGmUJLFYE4iLi");

	      // Add the service to the client
	      final MessageBirdClient messageBirdClient = new MessageBirdClient(wsr);

	      try {
	    	  
	    	  BigInteger phoneNumber = new BigInteger("+541166089157");
	    	  final List<BigInteger> phones = new ArrayList<BigInteger>();
	    	  phones.add(phoneNumber);

	    	  final MessageResponse response = messageBirdClient.sendMessage("kbee", "codigo de seguridad kbee 65433", phones);
	    	  //final MessageResponse response = messageBirdClient.sendMessage("+541166089157", "codigo de seguridad kbee 65432", phones);
	          // Get Hlr using msgId and msisdn
	          // System.out.println("getting message info message:");
	 

	      } 
	      catch (UnauthorizedException unauthorized) {
	          if (unauthorized.getErrors() != null) {
	              // System.out.println(unauthorized.getErrors().toString());
	          }
	          unauthorized.printStackTrace();
	      } 
	      catch (GeneralException generalException) {
	          if (generalException.getErrors() != null) {
	              // System.out.println(generalException.getErrors().toString());
	          }
	          generalException.printStackTrace();
	      } 
//	      catch (NotFoundException notFoundException) {
//	          if (notFoundException.getErrors() !=null) {
//	              // System.out.println(notFoundException.getErrors().toString());
//	          }
//	          notFoundException.printStackTrace();
//	      }
	
	  }
	
}
