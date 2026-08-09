package kbee.web.query;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.indexer.query.Filter;
import com.novamens.service.ServiceLocator;


/**
 *
 * 
 event_id                bigint                   
 event_type              character varying(64)    
 event_time              timestamp with time zone 
 event_user              bigint                   
 event_domain_id         bigint                   
 event_object_id         character varying(32)    
 email_from              character varying(128)   
 email_to                character varying(128)   
 email_subject           character varying(256)   
 email_text              text                     
 email_attachments       text                     
 event_result            character varying(64)    
 event_generator_action  character varying(128)
 
 Sender
 Domain
 From - To
 Receiver [User] 
 Receiver [Email]
 Area
 Text
    
 */

public class EmailHibernateLogQuery extends HibernateQuery {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailHibernateLogQuery.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	public EmailHibernateLogQuery() {
		getParameters().put("fromdate", OffsetDateTime.now().minusDays(7).truncatedTo(ChronoUnit.DAYS));
		getParameters().put("todate", OffsetDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS));
		
		
	}
	
	@Override
	public String getStatement() {
		
	
	String domain_q = isDomainKbee() ?  (" R.domainId=" +getDomainKbee().getId().toString() ): " R.domainId=" + getDomain().getId().toString() + " "; 
		
	Object textparameter = getParameters().get("text");
	String text = textparameter!=null && textparameter instanceof Filter ? (String)((Filter)textparameter).getValue() : (textparameter!=null ? (String)textparameter : "" );
	String text_q = "";
	
	StringBuilder statement = new StringBuilder();
	
	if (text.length()>0 && (!text.equals("null"))) {
		
		if (text.startsWith("'") && text.endsWith("'")) {
			text_q=" lower(R.email_text) like "+text.toLowerCase();
		}
		else if (text.startsWith("where:") && text.length()>6) {
			text_q =  text.substring(6);
		}
 		else
 			text_q=" lower(R.email_text) like '%"+text.toLowerCase().replace(" ", "%")+"%'";
	} 
	
	String from_date_q ="";
	String to_date_q  ="";
	String date_q  ="";

	if (getParameters().get("fromdate")!=null && (!getParameters().get("fromdate").equals("null"))) {
		OffsetDateTime from_date = (OffsetDateTime) getParameters().get("fromdate");
		from_date_q = " R.time >='" + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(from_date)+"'";
	}
	
	
	if (getParameters().get("todate")!=null && (!getParameters().get("todate").equals("null"))) {
		OffsetDateTime to_date = (OffsetDateTime) getParameters().get("todate");
		to_date_q = (from_date_q.length()>0?" and " : "") + " R.time <='" + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(to_date)+"'";
	}
	
	date_q  =  from_date_q +  to_date_q;
	if (date_q.length()>0)
		date_q="("+date_q+")";
	
	String date_connector = date_q.length()>0 ? " and " : "";
	
	
	String domain_text_connector = domain_q.length()>0 && text_q.length() > 0 ? " and " : "";
	
	String from_q = "";
	String from_connector = "";
										
	if (getParameters().get("from")!=null && (!getParameters().get("from").equals("null")))  {
										
		from_q = " lower(R.email_from) like '%" +getParameters().get("from").toString().toLowerCase().trim()+"%'";
		from_connector = "and ";
	}
	
	String to_q = "";
	String to_connector = "";
	if (getParameters().get("to")!=null && (!getParameters().get("to").equals("null"))) {
		to_q = " lower(R.email_to) like '%" +getParameters().get("to").toString().toLowerCase().trim()+"%'";
		to_connector =  " and ";
	}
	
	
	String result_q = "";
	String result_connector = "";
	if (getParameters().get("result")!=null) {
		
		String s=getParameters().get("result").toString().toLowerCase().trim();
		
		if (s.equals("ok") || s.startsWith("success"))
			result_q = " (R.result = 'Succesful' or R.result='OK') ";
		else		
			result_q = " (R.result != 'Succesful' and R.result!='OK') ";
		
		result_connector =  " and ";
	}
	
	String where_q = domain_q + domain_text_connector + text_q + date_connector + date_q + from_connector + from_q + to_connector + to_q + result_connector + result_q;
	
	where_q = where_q.length()>0 ? " where " + where_q :  "";
	
	statement.append("from  SendEmailEvent R " + where_q); 
	String sizeQuery = "select count (*) FROM SendEmailEvent R " + where_q;
	logger.debug(sizeQuery);
	setSizeQuery(sizeQuery);
	
	statement.append(" order by R.time desc");
	
	logger.debug(statement.toString());
	setStatement(statement.toString());
	
	return statement.toString();
}
	
@Override
public SessionFactory getSessionFactory() {
	return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
}


protected Person getPerson() {
	return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
}

protected Domain getDomain() {
	return ServiceLocator.getService(UserService.class).getDomain();
}

protected boolean isDomainKbee() {
	try {
			return getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
			
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
}

private Domain getDomainKbee() {
	return getContentDao().findDomainByName("kbee");
}

private ContentDao getContentDao() {
	return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
}
}

