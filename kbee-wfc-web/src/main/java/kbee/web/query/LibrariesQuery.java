package kbee.web.query;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.indexer.query.Filter;
import com.novamens.service.ServiceLocator;

public class LibrariesQuery extends HibernateQuery {
	
	private static final long serialVersionUID = 1L;
	
	public LibrariesQuery() {
	}
	
	@Override
	public String getStatement() {
		
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		
		Object textparameter = getParameters().get("text");
		String text = textparameter!=null && textparameter instanceof Filter ? (String)((Filter)textparameter).getValue() : (textparameter!=null ? (String)textparameter : null);
		String txt = null;

		if (text!=null) {
			if (text.startsWith("'") && text.endsWith("'")) {
				txt=" and lower(R.displayName) like "+text.toLowerCase();
			}
			else if (text.startsWith("where:") && text.length()>6) {
				txt = " and " + text.substring(6);
 			}
	 		else
				txt=" and lower(R.displayName) like '%"+text.toLowerCase().replace(" ", "%")+"%'";
		} 

		StringBuilder statement = new StringBuilder();
		
		statement.append("from KbeeLibrary R where R.domain.id=" + String.valueOf(domain.getId()) +"  " + (txt!=null?txt:""));
		
		String sizeQuery = "select count (*) FROM KbeeLibrary R WHERE R.domain.id=" + domain.getId().toString() +"  " + (txt!=null?txt:"");
		setSizeQuery(sizeQuery);
		
		String str_order = ((getParameters().get("ascending")!=null && getParameters().get("ascending").equals("true")) ? "":" desc");
		
		
		if ("title".equals(getParameters().get("sort")) || "title_sort".equals(getParameters().get("sort"))) {
			statement.append(" order by lower(R.displayName) " + str_order);
		}
		else if ("order".equals(getParameters().get("sort"))) {
			statement.append(" order by listOrder " + str_order);
		}

		//else if ("state".equals(getParameters().get("sort"))) {
		//	statement.append(" order by R.state.label "+  str_order);
		//}

		else {
			statement.append(" order by R.lastModifiedDate "+  str_order);
		}
		
		setStatement(statement.toString());

		return statement.toString();
	}
		
	@Override
	public SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
}