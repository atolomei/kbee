package kbee.web.query;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

public class XPlorerQuery extends HibernateQuery {

	private static final long serialVersionUID = -8861828583154950507L;
	
	public XPlorerQuery() {
		makeStatement();
	}
	
	@Override
	public ResultSet execute() {
		makeStatement();
		return super.execute();
	}
	
	
	public void setSortCriteria(String sort) {
			getParameters().put("sort", sort);
	}
	
	public void setText(String text) {
			getParameters().put("text", text);
	}
	
	public Domain getDomain() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = profile.getDomain();
		return domain;
	}
	
	public User getUser() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		return profile.getUser();
	}
	
	
	private void makeStatement() {
		
		String orderby, txt = null;
		
		String sort = (String) getParameters().get("sort");
		String text = (String) getParameters().get("text");
		
		if (sort!=null && sort.equals("name"))
			orderby=" order by lower(K.name)";
		else
			orderby=" order by K.lastModifiedDate desc";
		
		if (text!=null) {
			if (text.startsWith("'") && text.endsWith("'")) {
				txt=" and lower(K.name) like "+text.toLowerCase();
			}
			else if (text.startsWith("where:") && text.length()>6) {
				txt = " and " + text.substring(6);
 			}
	 		else
				txt=" and lower(K.name) like '%"+text.toLowerCase().replace(" ", "%")+"%'";
		} 
		
		String stm = "FROM " + KBFileImpl.class.getSimpleName() + " K WHERE K.domain.id= '" + getDomain().getId().toString() +"'" + (txt!=null?txt:"")  + orderby;
		setStatement(stm);
				
		String sizeQuery = "select count (*) FROM " + KBFileImpl.class.getSimpleName() + " K WHERE K.domain.id= '" + getDomain().getId().toString() +"'" + (txt!=null?txt:"");
		setSizeQuery(sizeQuery);
		
	}
	
}
;