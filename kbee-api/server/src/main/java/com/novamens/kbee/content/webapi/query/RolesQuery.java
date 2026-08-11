package com.novamens.kbee.content.webapi.query;

import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.indexer.query.Filter;
import com.novamens.kbee.content.security.KbeeAbstractRole;

public class RolesQuery extends HibernateQuery {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RolesQuery.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	Domain domain;
	Boolean isApi;
	
	public RolesQuery(Domain domain, Boolean isApi) {
		this.domain = domain;
		this.isApi = isApi;
	}
	
	@Override
	public String getStatement() {
		
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
		
		statement.append("from " + KbeeAbstractRole.class.getSimpleName()+ " R where R.domain.id=" + String.valueOf(domain.getId()) +"  " + (txt!=null?txt:""));
		
		if (isApi!=null) {
			statement.append(" and R.api_enabled="+isApi.booleanValue());
		}
		
		String sizeQuery = "select count (*) FROM " + KbeeAbstractRole.class.getSimpleName()+ "  R WHERE R.domain.id=" + domain.getId().toString() +" " + (txt!=null?txt:"");
		if (isApi!=null) {
			sizeQuery +=" and R.api_enabled="+isApi.booleanValue();
		}
		setSizeQuery(sizeQuery);
		String str_order = ((getParameters().get("ascending")!=null && getParameters().get("ascending").equals("true")) ? "":" desc");
		if ("title".equals(getParameters().get("sort"))) {
			statement.append(" order by lower(R.name) " + str_order);
		}
		else {
			statement.append(" order by R.lastModifiedDate "+  str_order);
		}
		
		logger.debug(statement.toString());
		
		setStatement(statement.toString());

		return statement.toString();
	}

}
