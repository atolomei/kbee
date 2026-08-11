package com.novamens.content.user;

import java.util.List;
import java.util.Locale;


import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.document.IDoc;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.dom.Domain;
import com.novamens.security.AuthToken;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.service.SystemService;

public interface UserService extends SystemService  {
	
	public UserProfile getSessionUserProfile();
	public List<Principal> getSessionUserPrincipals();
	public Locale getSessionUserLocale();
	public Domain getDomain();

	public AuthToken getAuthToken();

	public List<Classification> getClassification();

	public void onLogin(User user);
	public void logout();
	
	public User findRootUser(Domain domain);

	public void evict();
	
	public void impersonate(User user);
	
	public boolean isUserAdmin();
	public boolean isUserAdmin(Person user);
	public boolean isUserAdmin(PersonMember user);
	public boolean isWriteable(DataSetMember member);
	public boolean isDeleteable(DataSetMember member);
	public boolean isReadable(DataSetMember member);
	public boolean isWriteable(DataSet dataset);
	public boolean isAdmin(DataSet ds);

	public IDoc getUploadAndCreateContainer() throws ContentMgmtException;
}