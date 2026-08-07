package test.com.novamens.kbee.whapp;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.messagebird.MessageBirdClient;
import com.messagebird.MessageBirdService;
import com.messagebird.MessageBirdServiceImpl;
import com.messagebird.exceptions.GeneralException;
import com.messagebird.exceptions.UnauthorizedException;
import com.messagebird.objects.conversations.ConversationContent;
import com.messagebird.objects.conversations.ConversationContentType;
import com.messagebird.objects.conversations.ConversationFallbackOption;
import com.messagebird.objects.conversations.ConversationSendRequest;
import com.messagebird.objects.conversations.ConversationSendResponse;
import com.messagebird.objects.conversations.ConversationStartRequest;
import com.novamens.kbee.whatsapp.KbeeWhatsAppService;
import com.novamens.service.ServiceLocator;
import com.novamens.whatsapp.HsmComponent;
import com.novamens.whatsapp.HsmParameter;
import com.novamens.whatsapp.WhatsAppService;
import com.novamens.whatsapp.HsmComponent.Section;

public class WhatsAppTest {
	
	
//	@Test
//	public void test() {
//
//	      // First create your service object
//	      final MessageBirdService wsr = new MessageBirdServiceImpl("xO2aY0BYu6jorGmUJLFYE4iLi");
//
//	      // Add the service to the client
//	      final MessageBirdClient messageBirdClient = new MessageBirdClient(wsr);
//
//	      try {
//	    	  
//	          ConversationContent conversationContent = new ConversationContent();
//	          conversationContent.setText("Hello world from java sdk");
//
//	          // Optional source parameter, that identifies the actor making the request.
//	          Map<String, Object> source = new HashMap<>();
//	          source.put("Salesman", "Sir. John Doe");
//	          
//	          ConversationFallbackOption fallbackOption = new ConversationFallbackOption();
//
//	          ConversationSendRequest request = new ConversationSendRequest(
//	                  "+541166089157",
//	                  ConversationContentType.TEXT,
//	                  conversationContent,
//	                  "29eba147-6992-47e8-9c5c-5ff460d7ef5c",
//	                  "",
//	                  fallbackOption,
//	                  source,
//	                  null);
//	 
//	            ConversationSendResponse sendResponse = messageBirdClient.sendMessage(request);
//	            System.out.println(sendResponse.toString());
//	      } 
//	      catch (UnauthorizedException unauthorized) {
//	          unauthorized.printStackTrace();
//	      } 
//	      catch (GeneralException generalException) {
//	          generalException.printStackTrace();
//	      } 
//	
//	  }
	
	@Test
	public void test2() {

		final MessageBirdService wsr = new MessageBirdServiceImpl("xO2aY0BYu6jorGmUJLFYE4iLi");
	
		List<HsmComponent> components = new ArrayList<>();
		
		HsmComponent component = new HsmComponent(Section.Header);
		component.setParameters(new HsmParameter("text", "KBEE"));
		components.add(component);
	
		component = new HsmComponent(Section.Body);
		component.setParameters(new HsmParameter("text", "999999"));
		components.add(component);
		
		KbeeWhatsAppService service = new KbeeWhatsAppService(); 
	
		service.startConversation("security_token", "59898274879", components);
	}
	
}