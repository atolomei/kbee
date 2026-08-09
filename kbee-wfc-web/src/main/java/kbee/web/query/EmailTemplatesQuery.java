package kbee.web.query;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.indexer.query.Filter;
import com.novamens.service.ServiceLocator;

public class EmailTemplatesQuery extends HibernateQuery {
	
	private static final long serialVersionUID = 1L;
	

	public EmailTemplatesQuery() {
	}
	
	/**
	 */
	@Override
	public String getStatement() {
		
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		
		Object textparameter = getParameters().get("text");
		String text = textparameter!=null && textparameter instanceof Filter ? (String)((Filter)textparameter).getValue() : (textparameter!=null ? (String)textparameter : null);
		String txt = null;

		
		Object languageparameter = getParameters().get("language");
		String la = languageparameter!=null ? (la=" and R.lang=" + (String) languageparameter): "";
		
		if (text!=null) {
			if (text.startsWith("'") && text.endsWith("'")) {
				txt=" and lower(R.title) like "+text.toLowerCase() + la;
			}
			else if (text.startsWith("where:") && text.length()>6) {
				txt = " and " + text.substring(6) + la;
 			}
	 		else
				txt=" and (lower(R.title) like '%"+text.toLowerCase().replace(" ", "%")+"%' or lower(R.text) like '%"+text.toLowerCase()+"%') " + la;
		}
		else if (la!=null) {
			txt = languageparameter!=null ? (la=" and R.lang=" + (String) languageparameter): null;;
		}
			
		StringBuilder statement = new StringBuilder();
															
		statement.append("from KbeeEmailTemplate R where R.state!=" + String.valueOf(ObjectState.DRAFT.getId()) + " and R.domain.id= " + String.valueOf(domain.getId()) +"  " + (txt!=null?txt:""));
																						
		String sizeQuery = "select count (*) FROM KbeeEmailTemplate R WHERE R.state!=" + String.valueOf(ObjectState.DRAFT.getId()) +" and R.domain.id= " + domain.getId().toString() +" " + (txt!=null?txt:"");
		setSizeQuery(sizeQuery);
		
		String str_order = ((getParameters().get("ascending")!=null && getParameters().get("ascending").equals("true")) ? "":" desc");
		
		if ("title".equals(getParameters().get("sort")) || "title_sort".equals(getParameters().get("sort"))) { statement.append(" order by lower(R.title) " + str_order);		}
		else if ("order".equals(getParameters().get("sort"))) 												 { statement.append(" order by listOrder " + str_order);			}
		else if ("state".equals(getParameters().get("sort"))) 												 { statement.append(" order by R.state " + str_order);			}
		
		
		else if ("key".equals(getParameters().get("sort"))) 												 { statement.append(" order by R.key " + str_order);			}
		else if ("modified".equals(getParameters().get("sort"))) 											 { statement.append(" order by R.lastModifiedDate " + str_order);			}
		else if ("created".equals(getParameters().get("sort"))) 											 { statement.append(" order by R.creationDate " + str_order);			}
		else if ("subject".equals(getParameters().get("sort"))) 											 { statement.append(" order by R.subject " + str_order);			}
		else if ("id".equals(getParameters().get("sort"))) 													 { statement.append(" order by R.id " + str_order);			}
		else {																								   statement.append(" order by R.lastModifiedDate " +  str_order);	}
		
		
		
		
		setStatement(statement.toString());
		return statement.toString();
	}
	
	/**
	 * 
	 */
	@Override
	public SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
}
