package kbee.web.query;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.service.ServiceLocator;

public class ProgressNotesQuery extends HibernateQuery {
	private static final long serialVersionUID = 1L;
	
	//private static final String ROLE_DOMAIN_ADMIN = KbeeGlobalRole.DOMAIN_ADMIN.getId();

	public ProgressNotesQuery() {
	}
	
	
	
	@Override
	public String getStatement() {
		
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		StringBuilder statement = new StringBuilder();
		
		statement.append("from KbeeActivityProgressNote N where N.activity.statusValue='RUNNING' AND N.state="+  String.valueOf(ObjectState.ENABLED.getId())  + " AND N.domain.id=" + String.valueOf(domain.getId()));

		
		
		
		
		String sizeQuery = "select count (*) FROM KbeeActivityProgressNote N WHERE N.activity.statusValue='RUNNING' AND N.state= " + String.valueOf(ObjectState.ENABLED.getId()) + " AND N.domain.id=" + domain.getId().toString();
		setSizeQuery(sizeQuery);
		
		//String str_order = ((getParameters().get("ascending")!=null && getParameters().get("ascending").equals("true")) ? "":" desc");
		
		String str_order = "desc";
		
		statement.append(" order by N.lastModifiedDate "+  str_order);
		
		setStatement(statement.toString());

		return statement.toString();
	}

}