package com.novamens.content.user;

import java.util.List;

import com.novamens.content.model.LabelScope;
import com.novamens.security.User;
import com.novamens.service.BusinessObjectService;
import com.novamens.service.FactoryService;

/**
 * 
 */
public interface UserLabelsService extends BusinessObjectService, FactoryService {
	
	public List<UserLabel> getLabels();
	public void update(UserLabel label);
	public void delete(UserLabel label);
	public UserLabel create(User user);
	public User getUser();
	public void setLabelDao(UserLabelDao dao);
	
	public UserLabel create();
	public UserLabel create(String string, String string2);
	public UserLabel create(String string);
	public UserLabel create(String label, String css, LabelScope scope);
	
	List<UserLabel> getLabels(LabelScope scope);
	
	public UserLabel create(String strlabel, User user_creator);
	public UserLabel create(String label, String css, LabelScope scope,  User user_creator);
	
}
