package kbee.queue.rabbit;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import kbee.queue.QueueService;
import kbee.util.PropertiesFactory;

public class KbeeRabbitService implements QueueService {

	String  host = PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("kbee.rabbit.host");
	
	String port = PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("kbee.rabbit.port");

	String user = PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("kbee.rabbit.user");
	
	String password = PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("kbee.rabbit.password");
	
	RabbitTemplate template;
	ObjectMapper mapper = null;
	
	public RabbitTemplate getTemplate() {
		if (template==null) {
			template = createTemplate();
		}
		return template;
	}

    public RabbitTemplate createTemplate() {

        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory();

        connectionFactory.setHost(host);
        connectionFactory.setPort(Integer.valueOf(port));
        connectionFactory.setUsername(user);
        connectionFactory.setPassword(password);
        		
        return new RabbitTemplate(connectionFactory);
    }
    
    public ObjectMapper getMapper() {
    	if (mapper==null) {
    		mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    	}
    	return mapper;
    }
    
    public void sendMessage(String queue, Object message) {
    	try {
			
    		String json = getMapper().writeValueAsString(message);
 
            getTemplate().execute(channel -> {

                channel.confirmSelect();

                channel.basicPublish(
                        "",
                        queue,
                        null,
                        json.getBytes()
                );

                return channel.waitForConfirms(5000);
            });
    	} 
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new RuntimeException(e);
    	}    
    }	
}
