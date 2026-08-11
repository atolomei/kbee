package com.novamens.kbee.content.webapi.query;

import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.kbee.content.email.KbeeEmailTemplate;

public class EmailTemplatesQuery extends HibernateQuery {
	private static final long serialVersionUID = 1L;
	
	private Domain domain;

	public EmailTemplatesQuery(Domain domain) {
		this.domain = domain;
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
		
		statement.append("from " + KbeeEmailTemplate.class.getSimpleName() +  " R where R.domain.id=" + String.valueOf(domain.getId()) +"  " + (txt!=null?txt:""));
		
		String sizeQuery = "select count (*) FROM " + KbeeEmailTemplate.class.getSimpleName() + " R WHERE R.domain.id=" + domain.getId().toString() +"  " + (txt!=null?txt:"");
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
}

