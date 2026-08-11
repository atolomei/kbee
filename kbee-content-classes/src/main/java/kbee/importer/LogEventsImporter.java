package kbee.importer;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.dom.Domain;
import com.novamens.content.entity.Person;
import com.novamens.event.LogEvent;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.idoc.webapi.client.KbeeApiService;
import com.novamens.logging.AbstractLogEvent;
import com.novamens.logging.AssignationEvent;
import com.novamens.logging.CheckinEvent;
import com.novamens.logging.CheckoutEvent;
import com.novamens.logging.ContentEvent;
import com.novamens.logging.CreationEvent;
import com.novamens.logging.DropcheckoutEvent;
import com.novamens.logging.LoginEvent;
import com.novamens.logging.RemoveEvent;
import com.novamens.logging.UpdateEvent;
import com.novamens.security.User;

import kbee.api.model.ApiFile;
import kbee.api.model.ILogEvent;
import kbee.api.model.ApiUser;

public class LogEventsImporter extends Importer {

	LogEventsImporter(KbeeApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain);
	}
	
	public void execute(ApiUser remote, Person local) throws IOException {
		try {
//			Date todate = new Date();
			List<ILogEvent> events = null;
			//List<ILogEvent> events = getServer().getAudit(remote);
			for (ILogEvent remoteevent : events) {
//				if ((todate.getTime() -  remoteevent.getTime().getTime()) < 31536000000L ) {
					if ("Login".equals(remoteevent.getType())) {
						LoginEvent localevent = getLocal(LoginEvent.class, remoteevent);
						if (localevent == null) {
							localevent = (LoginEvent)createEvent(remoteevent);
							update(localevent);
							setLocal(remoteevent, localevent);
						 	logger.info("ILogEvent "+remoteevent.getId());
						}
					}
//				}
//				else {
//				}
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw e;
		}
	}
	
	public void execute(ApiFile remote, KbeeIDoc local) throws IOException  {
		try {
			Date todate = new Date();
			List<ILogEvent> events = null;
			for (ILogEvent remoteevent : events) {
//				if ((todate.getTime() -  remoteevent.getTime().getTime())<31536000000L && remoteevent.getUser()!=null) {
				if (remoteevent.getUser()!=null) {
					ContentEvent localevent = getLocal(ContentEvent.class, remoteevent);
					Content contentevent = getVersion(local, remoteevent.getVersion());
					if (localevent == null) {
						localevent = createEvent(contentevent, remoteevent);
						localevent.setVersion(remoteevent.getVersion());
						update(localevent);
						setLocal(remoteevent, localevent);
					 	logger.info("ILogEvent "+remoteevent.getId());
					}
					else {
						localevent.setVersion(remoteevent.getVersion());
						localevent.setContent(contentevent);
						update(localevent);
					}
				}
			}

		}
		catch (Throwable e) {
			e.printStackTrace();
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw e;
		}
	}
	
	private ContentEvent createEvent(Content content, ILogEvent remote) {
		ContentEvent local = null;
		switch(remote.getType()) {
		case "Create":
			local = new CreationEvent(content);
			break;
		case "Update":
			local = new UpdateEvent(content);
			local.setParameters(remote.getParameters());
			break;
		case "Checkin":
			local = new CheckinEvent(content);
			break;
		case "Checkout":
			local = new CheckoutEvent(content);
			break;
		case "Assign":
			local = new AssignationEvent();
			local.setContent(content);
			local.setParameters(remote.getParameters());
			break;
		case "Drop checkout":
			local = new DropcheckoutEvent(content);
			break;
		case "Remove":
			local = new RemoveEvent(content);
			break;
		default:
			Assert.isTrue(false, "invalid type");
		}
		local.setEventUser(getLocalUser(remote.getUser()));
		local.setTime(remote.getTime());
		return local;
	}
	
	private LogEvent createEvent(ILogEvent remote) {
		User user = getLocalUser(remote.getUser());
		AbstractLogEvent local = null;
		switch(remote.getType()) {
		case "Login":
			local = new LoginEvent(user);
			break;
		default:
			Assert.isTrue(false, "invalid type");
		}
		local.setTime(remote.getTime());
		return local;
	}

	
	private Content getVersion(KbeeIDoc file, int versionnumber) {
		KbeeIDoc version = file;
		while (version!=null) {
			if (version.getVersion()==versionnumber) {
				return version;
			}
			else {
				if (version.getPreviousVersion()!=null)
				version = (KbeeIDoc)getContentDao().reload(version.getPreviousVersion());
				else
				version = null;
			}
		}
		return file;
	}
}
