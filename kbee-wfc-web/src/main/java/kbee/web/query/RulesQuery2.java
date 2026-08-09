package kbee.web.query;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.service.ServiceLocator;

public class RulesQuery2 extends HibernateQuery {
	private static final long serialVersionUID = 1L;

	public RulesQuery2() {
	}
	
	@Override
	public String getStatement() {

		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		Domain domain = profile.getDomain();

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
		
		statement.append("from KbeeSecurityRule R where R.domain.id= '" + String.valueOf(domain.getId()) +"' " + (txt!=null?txt:""));
		
		String sizeQuery = "select count (*) FROM KbeeSecurityRule R WHERE R.domain.id= '" + domain.getId().toString() +"'" + (txt!=null?txt:"");
		setSizeQuery(sizeQuery);
		
		
		if ("title".equals(getParameters().get("sort"))) {
			statement.append(" order by lower(R.name)");
		}
		else {
			statement.append(" order by R.lastModifiedDate desc");
		}
		
		setStatement(statement.toString());
		
		return statement.toString();
	}
	
	public void setText(String text) {
		getParameters().put("text", text);
}
	@Override
	public SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
}

