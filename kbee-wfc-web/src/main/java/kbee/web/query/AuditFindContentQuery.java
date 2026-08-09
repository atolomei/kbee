package kbee.web.query;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;

import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;

public class AuditFindContentQuery extends HibernateQuery {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AuditFindContentQuery.class.getName());
	
	private static final long serialVersionUID = 1L;

	public AuditFindContentQuery() {
		getParameters().put("fromdate", OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS));
		getParameters().put("todate", OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS));
	}
	
	
	@Override
	public String getStatement() {
		
	
		
		
	String domain_q = isDomainKbee() ?  " R.domainId is not null " : " R.domainId=" + getDomain().getId().toString() + " "; 
		
	StringBuilder statement = new StringBuilder();

	String id_q ="";
	String id_connector = "";
	if (getParameters().get("id")!=null && (!getParameters().get("id").equals("null"))) {
		id_q = " R.contentId=" + getParameters().get("id").toString().trim();  // bigint
		id_connector = " and ";
	}
	

	String oid_q ="";
	String oid_connector = "";
	if (getParameters().get("oid")!=null && (!getParameters().get("oid").equals("null"))) {
		oid_q = " R.contentOId='" + getParameters().get("oid").toString().trim()+"'";
		oid_connector = " and ";
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
		to_date = to_date.plusDays(1).truncatedTo(ChronoUnit.DAYS);
		to_date_q = (from_date_q.length()>0?" and " : "") + " R.time <'" + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(to_date)+"'";
	}
	
	date_q  =  from_date_q +  to_date_q;
	if (date_q.length()>0)
		date_q="("+date_q+")";
	
	String date_connector = date_q.length()>0 ? " and " : "";

			
	String title_q ="";
	String title_connector = "";
	if (getParameters().get("title")!=null && (!getParameters().get("title").equals("null"))) {
		title_q = " R.title like '%" + getParameters().get("title").toString().replace(" ", "%").trim() + "%' ";
		title_connector =  " and ";
	}

	String auditSet_q = "R.auditSet.id=" + String.valueOf(AuditSet.CONTENT.getId());
	String auditSet_connector = " and ";
										
	auditSet_q = "";
	auditSet_connector = "";
			
	
	String where_q = domain_q + date_connector + date_q + auditSet_connector + auditSet_q + id_connector + id_q + oid_connector + oid_q + title_connector + title_q;
	
	where_q = where_q.length()>0 ? " where " + where_q :  "";
	
	statement.append("from  ContentEvent R " + where_q); 
	String sizeQuery = "select count (*) FROM  ContentEvent R " + where_q;
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

}
