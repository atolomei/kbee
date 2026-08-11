package kbee.web.model;


import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.indexer.query.Filter;
import com.novamens.service.ServiceLocator;

public class DataSetsQuery extends HibernateQuery {
	private static final long serialVersionUID = 1L;
	
	boolean deleted_visible = true;
	

	public DataSetsQuery() {
			this(true);
	}
	
	
	public DataSetsQuery(boolean deleted_visible) {
		this.deleted_visible = deleted_visible;
	}
	
	@Override
	public String getStatement() {
		
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		
		Object textparameter = getParameters().get("text");
		String text = textparameter!=null && textparameter instanceof Filter ? (String)((Filter)textparameter).getValue() : (textparameter!=null ? (String)textparameter : null);


		String str_order = ((getParameters().get("ascending")!=null && getParameters().get("ascending").equals("true")) ? "":" desc");
		
		String states= getStates(this.deleted_visible);
		
		StringBuilder h_text = new StringBuilder();
		
		h_text.append(" and " + states);


		if (text!=null) {
			if (text.startsWith("'") && text.endsWith("'")) {
				h_text.append(" and lower(R.name) like "+text.toLowerCase());
			}
			else if (text.startsWith("where:") && text.length()>6) {
				h_text.append( " and " + text.substring(6));
 			}
	 		else
	 			h_text.append(" and lower(R.name) like '%"+text.toLowerCase().replace(" ", "%")+"%'");
		} 

		StringBuilder statement = new StringBuilder();
		
		statement.append("from KbeeDataSet R where R.domain.id=" + String.valueOf(domain.getId()) +"  " +h_text.toString());
		
		String sizeQuery = "select count (*) FROM KbeeDataSet R WHERE R.domain.id=" + domain.getId().toString() +"  "  +h_text.toString();
		setSizeQuery(sizeQuery);
		
		
		if ("title".equals(getParameters().get("sort"))) {
			statement.append(" order by lower(R.name) " + str_order);
		}
		
		else if ("status".equals(getParameters().get("sort"))) {
			statement.append(" order by R.state " + str_order);
		}
		
		
		else if ("type".equals(getParameters().get("sort"))) {
			statement.append(" order by R.type " + str_order);
		}
		
		else if ("id".equals(getParameters().get("sort"))) {
			statement.append(" order by R.id " + str_order);
		}
		
		else {
			statement.append(" order by R.lastModifiedDate " + str_order);
		}

		setStatement(statement.toString());
		
		return statement.toString();
	}

	protected String getStates(boolean deleted_visible) {
		
		if (deleted_visible) {
			return " (R.state="+String.valueOf(ObjectState.ENABLED.getId())+" or R.state="+String.valueOf(ObjectState.ARCHIVED.getId())+" or R.state="+String.valueOf(ObjectState.DELETED.getId())+") ";	
		}
		else {
			return " (R.state="+String.valueOf(ObjectState.ENABLED.getId())+" or R.state="+String.valueOf(ObjectState.ARCHIVED.getId())+") ";
		}
	}
	
	@Override
	public SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
}
