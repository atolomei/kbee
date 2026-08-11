package com.novamens.content.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.model.*;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.security.Role;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BusinessSystemService;
import com.novamens.service.FactoryService;

import kbee.content.support.SupportTicket;

/**
 *  Information Model Objects (DataSet, Classifier, Attribute, ContentTemplate)
*/
public interface ObjectFactoryService extends BusinessSystemService, FactoryService  {

	/** Model */
	public com.novamens.dom.Object createClassifier() throws ContentCreationException;
	public Object createClassifier(DataSet dataset) throws ContentCreationException;
	
	public com.novamens.dom.Object createDataSet(DataSetType type) throws ContentCreationException;
	
	public com.novamens.dom.Object createTemplate() throws ContentCreationException;
	public com.novamens.dom.Object createTemplate(boolean defaultWorkflow) throws ContentCreationException;
	//public com.novamens.dom.Object cloneTemplate(ContentTemplate source) throws ContentCreationException;
	
	public com.novamens.dom.Object createAttribute() throws ContentCreationException;
	
	public ResourceTag createResourceTag();
	
	public EForm createEForm(ContentTemplate template) throws ContentCreationException;
	public EForm createDefaultEForm(ContentTemplate template) throws ContentCreationException;
	
	/** User */
	public Person createUser(String username) throws ContentMgmtException;
	
	public Person createUser(String firstname, String lastname, String email, String username, ObjectState state);
	
	public Person createUser(String firstname, 
			String lastname, 
			String email, 
			List<ExternalPlatformId> platforms,
			List<Role> roles, 
			Map<ModelElement, List<Object>> classification);
	
	public Person createUser(String firstname, 
			String lastname, 
			String email, 
			String username, 
			ObjectState state, 
			boolean isemail,	
			Set<Group> groups, 
			List<KbeeGlobalRole> global_permissions, 
			List<Role> roles) throws ContentMgmtException;

	/** DataSetMember */
	public com.novamens.dom.Object createMember(DataSet dataSet) throws ContentCreationException;
	public com.novamens.dom.Object createMember(DataSet dataSet, String name, Map<ModelElement, List<Object>> classification) throws ContentCreationException;
	public com.novamens.dom.Object createMemberNoTx(DataSet dataSet) throws ContentCreationException;
	public ActionRule createRule(EntityMember entity, String label);
	

	/** UserList */
	public com.novamens.dom.Object createUserList(User user, String console) throws ContentCreationException;
	public com.novamens.dom.Object createUserList(User user, Site site) throws ContentCreationException;
	
	
	/** SavedQuery */
	public com.novamens.dom.Object createSavedQuery(User user, String console) throws ContentCreationException;
	public com.novamens.dom.Object createSavedQuery          (User sessionUser, String title, String console, String browser, Site site, Map<String, java.lang.Object> parameters) throws ContentCreationException;
	public com.novamens.dom.Object createSavedQueryDashboard (User sessionUser, String title, String console, Site site,	Map<String, Object> parameters);
	
	public com.novamens.dom.Object createUserList(User user, String console, String title) throws ContentCreationException;
	
	/* Workflow Procedure, Launcher, LauncherGroup */
	public com.novamens.dom.Object createLauncherGroup(String na) throws ContentCreationException;
	public com.novamens.dom.Object createLauncherGroup(String na, Domain domain) throws ContentCreationException;
	public com.novamens.dom.Object createLauncher(ContentTemplate template) throws ContentCreationException;
	public com.novamens.dom.Object createLauncher(ContentTemplate template, Domain domain) throws ContentCreationException;

	public UserSet getUserSet();
	
	public SupportTicket createSupportTicket(User user, String subject, String text) throws ContentCreationException;
	com.novamens.dom.Object createUserList(User user, Site site, String title) throws ContentCreationException;
}