package kbee.web.query;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.service.ServiceLocator;

/**
 * System Email Notification Rules
 * 
 *  Settings -> Email Notifications
 *  
 */
public class ENotiRulesSystemQuery extends HibernateQuery {
	
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ENotiRulesSystemQuery.class.getName());

	public ENotiRulesSystemQuery() {
	}

	
	@Override
	public SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}

	
	@Override	
	public String getStatement() {
		
		String text = (String) getParameters().get("text");
		String txt = null;

		if (text!=null) {
			if (text.startsWith("'") && text.endsWith("'")) {
				txt=" and lower(R.name) like "+text.toLowerCase();
			}
			else if (text.startsWith("where:") && text.length()>6) {
				txt = " and " + text.substring(6);
 			}
	 		else
				txt=" and lower(R.name) like '%"+text.toLowerCase().replace(" ", "%")+"%'";
		} 

		StringBuilder statement = new StringBuilder();
		
		Domain domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		
		statement.append("from KbeeENotiRule R where R.is_system= true " +
				" and (R.state="+String.valueOf(ObjectState.ENABLED.getId())+" or R.state="+String.valueOf(ObjectState.ARCHIVED.getId())+") " +
				" and R.domain.id=" + String.valueOf(domain.getId()) +"  " + (txt!=null?txt:""));
		
		String sizeQuery = "select count (*) FROM KbeeENotiRule R where R.is_system= true " + 
		                   " and (R.state="+String.valueOf(ObjectState.ENABLED.getId())+" or R.state="+String.valueOf(ObjectState.ARCHIVED.getId())+") " + 
		                   " and R.domain.id=" + domain.getId().toString() +" " + (txt!=null?txt:"");
		setSizeQuery(sizeQuery);
		
		
		if ("title_sort".equals(getParameters().get("sort"))) {
			statement.append(" order by lower(R.name)");
		}
		else {
			statement.append(" order by R.lastModifiedDate ");
		}
		
		
		boolean ascending = "true".equals(getParameters().get("ascending"));
		
		if (!ascending)
			statement.append(" desc " );
		
		
		logger.debug(statement.toString());
		setStatement(statement.toString());
		
		return statement.toString();
	}
	
	
	public void setText(String text) {
		getParameters().put("text", text);
	}
	
}
