package kbee.web.query;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.indexer.query.Filter;
import com.novamens.kbee.content.enoti.KbeeENotiRule;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

public class ENotiRulesQuery extends HibernateQuery {
	private static final long serialVersionUID = 1L;

	public ENotiRulesQuery() {
		this(null);
	}
	
	public ENotiRulesQuery(User owner) {
		Domain domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		if (owner==null)
			setStatement("from KbeeENotiRule R where R.domain.id=" + String.valueOf(domain.getId()) + " order by R.lastModifiedDate desc");
		else
			setStatement("from KbeeENotiRule R where R.owner.id=" + owner.getId().toString() + " order by R.lastModifiedDate desc");
	}
	
	@Override
	public SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
	
	
	@Override
	public String getStatement() {
		
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		
		Object textparameter = getParameters().get("text");
		String text = textparameter!=null && textparameter instanceof Filter ? (String)((Filter)textparameter).getValue() : (textparameter!=null ? (String)textparameter : null);
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


		String domainQueryValue = String.valueOf(domain.getId());
		statement.append("from " +KbeeENotiRule.class.getSimpleName()+"  R where R.domain.id=" + domainQueryValue +"  " + (txt!=null?txt:""));

		String sizeQuery = "select count (*) FROM  "+ KbeeENotiRule.class.getSimpleName() +  " R WHERE R.domain.id=" + domainQueryValue +" " + (txt!=null?txt:"");
		setSizeQuery(sizeQuery);
		
		String str_order = ((getParameters().get("ascending")!=null && getParameters().get("ascending").equals("true")) ? "":" desc");
		
		
		if ("title".equals(getParameters().get("sort")) || "title_sort".equals(getParameters().get("sort"))) {
			statement.append(" order by lower(R.displayName) " + str_order);
		}

		else {
			statement.append(" order by R.lastModifiedDate "+  str_order);
		}
		
		setStatement(statement.toString());

		return statement.toString();
	}
		
}
