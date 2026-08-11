package com.novamens.kbee.content.webapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.command.Command;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.library.Library;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.security.Role;
import com.novamens.content.tree.TreeNode;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserSignature;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.dom.Domain;
import com.novamens.event.LogEvent;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.ResultSet;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Procedure;

import kbee.api.service.ApiException;

public interface ApiDao {

	public List<Classifier> getClassifiers(Domain domain);
	
	public List<Attribute> getAttributes(Domain domain);
	
	public List<ResourceTag> getResourceTags(Domain domain);
	
	public List<ProcessLauncher> getLaunchers(Domain domain);
	
	public List<ProcessLauncher> getLaunchers(Domain domain, Multiplicity filesMultiplicty);
	
	public List<LauncherGroup> getLauncherGroups(Domain domain);
	
	public List<Library> getLibraries(Domain domain);
	
	public Library findLibraryById(long id);
	
	public List<Facet> getFacets(Domain domain);
	
	public Domain findDomainByName(String name);
	
	public Content findContentByExternalId(String source, String id);
	
	public Content findContentById(long id);
	
	public EForm findFormById(long id);
	
	public Site findSiteByName(String name);
	
	public Content findContentByOId(long id);
	
	public List<LogEvent> getAudit(Object object);
	
	public List<Content> getHistory(Content content);
	
	public DataSet findDataSetById(long id);
	
	public DataSetMember findValueById(long id);
	
	public DataSetMember findValueByEXternalId(String id);
	
	public List<TreeNode> getRootValues(String datasetName);
	
	public List<TreeNode> getChildValues(DataSet dataSet, String path);
	
	public DataSet findDataSetByName(String name);
	
	public ContentTemplate findTemplateById(long id);
	
	public List<DataSet> getDataSets(Domain domain);
	
	public List<ContentTemplate> getTemplates(Domain domain);
	
	public List<Procedure> getProcedures(Domain domain);
	
	public Procedure findProcedureById(long id);
	
	public ResultSet getUsers(Domain domain, String criteria);
	
	public ResultSet getGroups(Domain domain, String criteria);
	
	public ResultSet getRoles(Domain domain, Boolean isApi);
	
	public ResultSet getSecurityRules(Domain domain);
	
	public ResultSet getEmailTemplates(Domain domain);
	
	public EmailTemplate findEmailTemplate(Domain domain, String language, String key);
	
	public Person findPersonById(String id);
	
	public Person findPersonByExternalId(String id);
	
	public User findUserById(String id);
	
	public Group findGroupById(long id);
	
	public Role findRoleById(long id);
	
	public Resource findResourceById(long id);
	
	public SecurityRule findSecurityRuleById(long id);
	
	public Classifier findClassifierById(long id);
	
	public Attribute findAttributeById(long id);
	
	public ResourceTag findResourceTagById(long id);
	
	public LauncherGroup findLauncherGroupById(long id);
	
	public Command findCommandById(long id);
	
	public ResultSet getValues(long datasetId, String criteria, List<String> members, String sortCriteria, boolean facets);
	
	public ResultSet executeIql(String statement) throws ApiException;
	
	public ResultSet executeIql(String statement, boolean allstates) throws ApiException;
	
	public Resource upload(MultipartFile file) throws ApiException;
	
	public List<UserDevice> getDevices();
	
	public List<UserSignature> getSignatures();
	
	public ResultSet getUserActivities(User user, List<String> members, String sort, boolean facets);
	
	public ResultSet getMonitorActivities(List<String> members, String sort, boolean facets);
	
	public ResultSet getWorkItems(List<String> members, String sort, boolean facets);
	
	public ResultSet getFiles(Library library, List<String> members, String sort, boolean facets);
	
	public ResultSet getFiles(Library library, Map<String, List<String>> filters, String sort, Boolean facets);
	
	public ResultSet getFolder(Site site, DataSet dataset, DataSetMember folder, String sort);

	public List<Person> getCollaborators(Activity activity, String event);
	
	public Activity findActivityById(long id);
	
	public List<Domain> getDomains(String email);
}
