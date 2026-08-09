package kbee.web.console.grid;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserRole;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.model.KbeeEntityMember;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

public class UserRoleColumn extends GridColumn<SearchResult, String> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserRoleColumn.class.getName());
																									
	private IModel<Role> model;
	private IModel<User> model_session_user = null;

	
	public UserRoleColumn(String id, IModel<Role> model, IModel<String> displayModel) {
		super(id, displayModel);
		this.model=model;
	}

	@Override
	public void detach() {
		
		if (model!=null)
			model.detach();
		
		if (model_session_user!=null)
			model_session_user.detach();
	}
	
	
	public IModel<Role> getModel() {
		return this.model;
	}

	@Override
	protected IModel<String> getLabelModel(SearchResult result) {
		try {
			if (result.getObject()==null) 
				return new Model<String>("err");

			KbeeEntityMember member = ((KbeeEntityMember)result.getObject());
			
			if (member==null)
				return new Model<String>("err");
			
			StringBuilder str = new StringBuilder();
			Role role = getModel().getObject();

			if (role!=null) {
				List<UserRole> list = getContentSecurityDao().findUserRolesByEntityMember(role, member);
				
				if (list!=null) {
						// NOTE. there some bug by which getPerson() gives null pointer when the UserRole is new and 
						// we click on column name. The bug is related to the query being cacheable
						// it does not fail with user.getLastFirstName()
						
					//if (list.size()<100)
					//	list.forEach(item -> str.append( (str.length()>0?" | ":"") + (item.getUser()!=null?item.getUser().getLastFirstName():"null")));
					//else {
						int n=0;
						for (UserRole r: list) {
								str.append( (str.length()>0?" <span class=\"separator\">| </span>":"") + (r.getUser()!=null?
										"<span>"+r.getUser().getLastFirstName()+"</span>":"<span>null</span>"));
							if (n++>150) {
								str.append("<span><br/>---<br/><b>WARNING</b>: Listed 1-"+String.valueOf(n) + " of "  + String.valueOf(list.size())+"<br/></span>");
								break;
							}
						}
					//}
						
				}
			}
 			return  new Model<String>(str.toString());
		} catch (Exception e) {
			logger.error(e);
			return new Model<String>(e.getClass().getName()+" " + e.getMessage());
		}
	}

	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	protected KbeeUser getSessionUser() {
		try {
			if (model_session_user != null && model_session_user.getObject() != null)
				return (KbeeUser) model_session_user.getObject();
			User session_user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			model_session_user = new ObjectModel<User>(session_user);
			return (KbeeUser) model_session_user.getObject();
		} catch (Exception e) {
			logger.error(" {} | {} | {} | {}", "getSessionUser() gave the error", e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
			return null;
		}
	}
 
}



  