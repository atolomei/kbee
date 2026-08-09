package com.novamens.kbee.wicket.markup.html.console.grid;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListService;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;


public abstract class UserListsColumn extends GridColumn<SearchResult, String> {
	private static final long serialVersionUID = 1L;
											
	private static Logger logger = Logger.getLogger(UserListsColumn.class.getName());
	
	public UserListsColumn(String id, IModel<String> displayModel) {
		super(id, displayModel, null);
	}
	
	@Override
	protected IModel<String> getLabelModel(SearchResult object) {
		try {
			List<UserList> list = ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(getConsole(), (Content) object.getObject());
			if (list==null)
				return new Model<String>("");
			StringBuilder str=new StringBuilder(); 
			for (UserList u:list) {
				if (str.length()>0)
					str.append(", ");
				str.append(u.getTitle());
			}
			return new Model<String>(str.toString());
		} 
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new Model<String>(e.getClass().getSimpleName());
		}
	}
	
	@Override
	public String getCssClass()	{
		return super.getCssClass() + " mylist";
	}
	
	protected abstract String getConsole();
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
