
package com.novamens.kbee.whatsapp;



import java.util.ArrayList;

import java.util.List;

import java.util.Properties;



import com.messagebird.MessageBirdClient;

import com.messagebird.MessageBirdService;

import com.messagebird.MessageBirdServiceImpl;

import com.messagebird.exceptions.GeneralException;

import com.messagebird.exceptions.UnauthorizedException;

import com.messagebird.objects.conversations.Conversation;

import com.messagebird.objects.conversations.ConversationContent;

import com.messagebird.objects.conversations.ConversationContentHsm;

import com.messagebird.objects.conversations.ConversationContentType;

import com.messagebird.objects.conversations.ConversationHsmLanguage;

import com.messagebird.objects.conversations.ConversationHsmLanguagePolicy;

import com.messagebird.objects.conversations.ConversationStartRequest;

import com.messagebird.objects.conversations.MessageComponent;

import com.messagebird.objects.conversations.MessageComponentType;

import com.messagebird.objects.conversations.MessageParam;

import com.messagebird.objects.conversations.TemplateMediaType;

import com.novamens.util.KbeeRuntimeException;

import com.novamens.whatsapp.HsmComponent;

import com.novamens.whatsapp.HsmParameter;

import com.novamens.whatsapp.WhatsAppService;



import kbee.util.PropertiesFactory;



public class KbeeWhatsAppService implements WhatsAppService {

	public void startConversation(String template, String phone, List<HsmComponent> components) {

		

		

       	Properties properties = PropertiesFactory.getInstance("kbee").getProperties(); 

    	String key = properties.getProperty("messagebird.key", null);

    	String channelId = properties.getProperty("messagebird.channel", null);

    	String namespace = properties.getProperty("messagebird.namespace", null);

    	

    	

		MessageBirdService wsr = new MessageBirdServiceImpl(key);

        MessageBirdClient messageBirdClient = new MessageBirdClient(wsr);



        ConversationContent conversationContent = new ConversationContent();

        

        ConversationContentHsm conversationHsm = new ConversationContentHsm();

        conversationHsm.setNamespace(namespace);

        

        List<MessageComponent> hsmcomponents = new ArrayList<MessageComponent>();

        for (HsmComponent component : components) {

        	MessageComponent messagecomponent = new MessageComponent();

        	if (HsmComponent.Section.Header.equals(component.getSection())) {

            	messagecomponent.setType(MessageComponentType.HEADER);

        	}

        	if (HsmComponent.Section.Body.equals(component.getSection())) {

            	messagecomponent.setType(MessageComponentType.BODY);

        	}

        	if (HsmComponent.Section.Button.equals(component.getSection())) {

            	messagecomponent.setType(MessageComponentType.BUTTON);

            	messagecomponent.setSub_type("url");

        	}

        	List<MessageParam> parameters = new ArrayList<>();

        	for (HsmParameter parameter : component.getParameters()) {

        		MessageParam messageparameter = new MessageParam();

        		if ("text".equals(parameter.getType()) ) {

               		messageparameter.setType(TemplateMediaType.TEXT);

        		} 

        		messageparameter.setText(parameter.getValue());

        		parameters.add(messageparameter);

        	}

        	messagecomponent.setParameters(parameters);

        	hsmcomponents.add(messagecomponent);

        }

        conversationHsm.setComponents(hsmcomponents);

        conversationHsm.setLanguage(new ConversationHsmLanguage("es", ConversationHsmLanguagePolicy.DETERMINISTIC));

        conversationHsm.setTemplateName(template);

        

        conversationContent.setHsm(conversationHsm); 

               



        ConversationStartRequest request = new ConversationStartRequest();

        request.setTo(phone);

        request.setType(ConversationContentType.HSM);

        request.setContent(conversationContent);

        request.setChannelId(channelId);

        

        try {

            Conversation conversation = messageBirdClient.startConversation(request);

            messageBirdClient.listConversationMessages(conversation.getId());

            System.out.println(conversation);

        } 

        catch (GeneralException | UnauthorizedException exception) {

			throw new KbeeRuntimeException(exception);

        }		

	}

}
