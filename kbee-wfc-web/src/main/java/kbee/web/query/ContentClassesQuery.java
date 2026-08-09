package kbee.web.query;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.service.ServiceLocator;

/**
 * TODO:  DEPRECATED
 *  
  * It cant be removed until the Portal stops using the old browser selector.
 *
 */

@Deprecated
public class ContentClassesQuery extends HibernateQuery {
				
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ContentClassesQuery() {
	}
	
	@Override
	public String getStatement() {
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		String statement = "from KbeeClassifier C where C.domain.id= '" + String.valueOf(domain.getId()) +"'";
		if ("title".equals(getParameters().get("sort"))) {
			statement += "order by C.name";
		}
		else {
			statement += "order by C.lastModifiedDate desc";
		}
		return statement;
	}
		
	@Override
	public SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
}
