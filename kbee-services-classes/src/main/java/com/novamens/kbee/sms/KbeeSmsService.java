package com.novamens.kbee.sms;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


import com.messagebird.MessageBirdClient;
import com.messagebird.MessageBirdService;
import com.messagebird.MessageBirdServiceImpl;
import com.messagebird.exceptions.GeneralException;
import com.messagebird.exceptions.UnauthorizedException;
import com.messagebird.objects.MessageResponse;
import com.novamens.sms.SmsMessage;
import com.novamens.sms.SmsService;
import com.novamens.util.KbeeRuntimeException;

public class KbeeSmsService implements SmsService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSmsService.class.getName());
			
	private static kbee.util.logging.Logger smsLogger = kbee.util.logging.Logger.getLogger("sms");
	
	// static private Logger DBLogger = LogManager.getLogger("DBEventLogger");
	
	
	public void sendMessage(SmsMessage message)  {
		final MessageBirdService wsr = new MessageBirdServiceImpl("xO2aY0BYu6jorGmUJLFYE4iLi");

		// Add the service to the client
		final MessageBirdClient messageBirdClient = new MessageBirdClient(wsr);

		try {
			
			//			BigInteger phoneNumber = new BigInteger("+541166089157");
			//			final List<BigInteger> phones = new ArrayList<BigInteger>();
			//			phones.add(phoneNumber);

			final MessageResponse response = messageBirdClient.sendMessage("8087", message.getMessage(), getPhones(message));
			
			smsLogger.info(response.toString());
			
			logger.debug(response.toString());
		 } 
		catch (UnauthorizedException unauthorized) {
			logger.error(unauthorized);  
			smsLogger.error(unauthorized);
			throw  new SecurityException(unauthorized);
		} 
		catch (GeneralException generalException) {
			logger.error(generalException);
			smsLogger.error(generalException);
			throw new KbeeRuntimeException(generalException);
		} 
	}
	
	
	
	private List<BigInteger> getPhones(SmsMessage message) {
		List<BigInteger> phones = new ArrayList<BigInteger>();
		for (String to : message.getTo()) {
			phones.add(BigInteger.valueOf(Long.valueOf(to)));
		}
		return phones;
	}
}