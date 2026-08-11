package com.novamens.kbee.content.webapi.controller;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.DataAccessService;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.wicket.util.io.IOUtils;
import org.hibernate.SessionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.command.Command;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EResourceField;
import com.novamens.content.form.EResourceSystemField;
import com.novamens.content.form.EResourcesField;
import com.novamens.content.library.Library;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.ObjectId;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.UserSet;
import com.novamens.content.model.UserSubset;
import com.novamens.content.multidimensional.FacetService;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.tree.Tree;
import com.novamens.content.tree.TreeNode;
import com.novamens.content.tree.TreePath;
import com.novamens.content.tree.TreeService;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.user.UserSignature;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Versionable;
import com.novamens.event.LogEvent;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.query.ContentQuery;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.tree.KbeeTreePath;
import com.novamens.kbee.content.webapi.query.DataSetQuery;
import com.novamens.kbee.content.webapi.query.EmailTemplatesQuery;
import com.novamens.kbee.content.webapi.query.FolderQuery;
import com.novamens.kbee.content.webapi.query.GroupsQuery;
import com.novamens.kbee.content.webapi.query.LibraryQuery;
import com.novamens.kbee.content.webapi.query.MonitorQuery;
import com.novamens.kbee.content.webapi.query.PendingTasksQuery;
import com.novamens.kbee.content.webapi.query.RolesQuery;
import com.novamens.kbee.content.webapi.query.RulesQuery;
import com.novamens.kbee.content.webapi.query.UsersQuery;
import com.novamens.kbee.content.webapi.query.WorkspaceQuery;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.UserTask;
import com.novamens.kbfs.FileServerException;
import com.novamens.portal6.model.Site;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.api.model.INode;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public class KbeeApiDao implements ApiDao {
	
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger("scheduler"));

	@Override
	public List<Classifier> getClassifiers(Domain domain) {
		return getContentDao().getClassifiers(domain);
	}
	
	@Override
	public List<Attribute> getAttributes(Domain domain) {
		return getContentDao().getAttributes(domain);
	}
	
	@Override
	public List<ResourceTag> getResourceTags(Domain domain) {
		return getRepository(ResourceTag.class).findAll(domain);
	}
	
	@Override
	public List<LauncherGroup> getLauncherGroups(Domain domain) {
		return getRepository(LauncherGroup.class).findAll(domain);
	}
	
	@Override
	public List<ProcessLauncher> getLaunchers(Domain domain) {
		return getLaunchers(domain, Multiplicity.M0N);
	}
	
	@Override
	public List<ProcessLauncher> getLaunchers(Domain domain, Multiplicity filesMultiplicity) {
		List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
		for (ProcessLauncher launcher : domain.getService(WorkflowDomainService.class).getLaunchers()) {
			if (launcher.isEnabled() && 
					launcher.executeable() && 
					launcher.getContentTemplate().getState()==ObjectState.ENABLED &&
					isFilesMultiplicity(launcher, filesMultiplicity)) { 
				launchers.add(launcher);
			}
		}
		return launchers;
	}
	
	@Override
	public List<Library> getLibraries(Domain domain) {
		return getRepository(Library.class).findAll(domain);
	}
	
	@Override
	public Library findLibraryById(long id) {
		return getRepository(Library.class).findById(id);
	}

	
	@Override
	public List<Facet> getFacets(Domain domain) {
		return domain.getService(FacetService.class).getFacets(getQueryIndex());
	}
	
	@Override
	public List<DataSet> getDataSets(Domain domain) {
		return getContentDao().getDataSets(domain);
	}
	
	@Override
	public List<ContentTemplate> getTemplates(Domain domain) {
		return getContentDao().getTemplates(domain);
	}
	
	@Override
	public List<Procedure> getProcedures(Domain domain) {
		return getWorkflowDao().getProcedures(domain);
	}
	
	@Override
	public Procedure findProcedureById(long id) {
		Procedure procedure = getWorkflowDao().findProcedureById(id);
		if (procedure==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.PROCEDURE_NOT_FOUND);
		return procedure;
	}
	
	@Override
	public EForm findFormById(long id) {
		EForm form  = getRepository(EForm.class).findById(id);
		if (form==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FORM_NOT_FOUND);
		return form;
	}
	
	@Override
	public ResourceTag findResourceTagById(long id) {
		ResourceTag tag  = getRepository(ResourceTag.class).findById(id);
		if (tag==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.PROCEDURE_NOT_FOUND);
		return tag;
	}
	
	@Override
	public LauncherGroup findLauncherGroupById(long id) {
		LauncherGroup group = getRepository(LauncherGroup.class).findById(id);
		if (group==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.PROCEDURE_NOT_FOUND);
		return group;
	}
	
	@Override
	public Content findContentByExternalId(String source, String id) {
		Content content = getContentDao().findContentByExternalId(source, id);
		return content;
	}
	
	@Override
	public Content findContentById(long id) {
		Content content = getContentDao().findContentById(id);
		return content;
	}
	
	@Override
	public Content findContentByOId(long id) {
		Content content = getContentDao().findContentByOId(id);
		return content;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<LogEvent> getAudit(Object object) {
		List<LogEvent> events;
		if (object instanceof Content)
			events = (List<LogEvent>)getContentDao().getAuditTrail((Content)object);
		else {
			String stm = "FROM AbstractLogEvent E WHERE E.objectId = '" + (new ObjectId(object)).toString() +"' order by E.time desc";
			org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(stm);
			events = (List<LogEvent>) query.list();
		}	
		return events;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Content> getHistory(Content content) {
		List<Content> history = new ArrayList<Content>();
		Versionable<Content> versionable = (Versionable<Content>)content;
		Content version = versionable.getPreviousVersion();
		while (version!=null) {
			history.add((Content)getContentDao().reload(version));
			versionable = (Versionable<Content>)version;
			version = versionable.getPreviousVersion();
		}
		return history;
	}
	
	@Override
	public Domain findDomainByName(String name) {
		Domain domain = (Domain)getContentDao().findDomainByName(name);
		if (domain==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
		return domain;
	}
	
	@Override
	public DataSet findDataSetById(long id) {
		DataSet dataset = (DataSet)getContentDao().findModelObjectById(DataSet.class, id);
		if (dataset==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DATASET_NOT_FOUND);
		return dataset;
	}

	@Override
	public DataSetMember findValueById(long id) {
		DataSetMember value = (DataSetMember)getContentDao().findMemberById(id);
		if (value==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND);
		return value;
	}
	
	@Override
	public DataSetMember findValueByEXternalId(String id) {
		DataSetMember value = (DataSetMember)getContentDao().findMemberByExternalId(id);
		if (value==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND);
		return value;
	}
	
	@Override
	public DataSet findDataSetByName(String name) {
	    for (DataSet ds : getDataSets(getDomain())) {
	        if ((ds.getAlias() != null && ds.getAlias().equalsIgnoreCase(name))
	                || ds.getName().equalsIgnoreCase(name)) {
	            return ds;
	        }
	    }
	    throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DATASET_NOT_FOUND);
	}
	
	@Override
	public ContentTemplate findTemplateById(long id) {
		ContentTemplate template = getContentDao().findContentTemplateById(id);
		if (template==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.CLASS_NOT_FOUND);
		return template;
	}
	
	@Override
	public Classifier findClassifierById(long id) {
		Classifier classifier = (Classifier)getContentDao().findModelObjectById(Classifier.class, id);
		if (classifier==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.CLASSIFIER_NOT_FOUND);
		return classifier;
	}
	
	@Override
	public Attribute findAttributeById(long id) {
		Attribute attribute = (Attribute)getContentDao().findModelObjectById(Attribute.class, id);
		if (attribute==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ATTRIBUTE_NOT_FOUND);
		return attribute;
	}
	
	@Override
	public Person findPersonById(String stringid) {
		if (stringid.startsWith("u")) {
			try {
				long longid = Long.valueOf(stringid.substring(1));
				User user = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserById(longid);
				if (user!=null) {
		 			UserProfile profile = getContentDao().findUserProfileByUser(user);
					List<DataSetMember> members = getContentDao().findMembersByEntity(profile.getEntity());
					DataSetMember personmember = null, usermember = null;
					for (DataSetMember member : members) {
						if (DataSetType.USER.equals(member.getDataSet().getDataSetType()))
							usermember = member;
						else
							personmember = member;
					}
					personmember = personmember!=null ? personmember : usermember;
					if (personmember!=null) {
						return (PersonMember)personmember;
					}
				}
			}
			catch (NumberFormatException e) {
			}
		}
		else
		if (stringid.startsWith("e")) {
			String externalid = stringid.substring(1);
			DataSetMember member = getContentDao().findMemberByExternalId(externalid);
			if (member!=null && member instanceof PersonMember) {
				return (PersonMember)member;
			}
		}
		else
		if (stringid.startsWith("p")) {
			String personid = stringid.substring(1);
			Person person = (KbeePerson)getContentDao().findEntityById(KbeePerson.class, Long.valueOf(personid));
			if (person==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
			return person;
		}
		else {
			try {
				long longid = Long.valueOf(stringid);
				Person person = (PersonMember)getContentDao().findMemberById(longid);
				if (person==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
				return person;
			}
			catch (NumberFormatException e) {
			}
		}
		throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
	}
	
	@Override
	public Person findPersonByExternalId(String stringid) {
		if (stringid!=null && !stringid.startsWith("u")) stringid = "u" + stringid;
		DataSetMember member = getContentDao().findMemberByExternalId(stringid);
		if (member!=null && member instanceof PersonMember) {
			return (PersonMember)member;
		}
		throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
	}
	
	public User findUserById(String stringid) {
		Person person = findPersonById(stringid);
		UserProfile userprofile = person.getProfile(UserProfile.class);
		if (userprofile==null || userprofile.getUser()==null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
		}
		return userprofile.getUser();
	}
	
	@Override
	public Group findGroupById(long id) {
		Group group = (Group)getSecurityDao().findGroupById(id);
		if (group==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.GROUP_NOT_FOUND);
		return group;
	}
	
	@Override
	public SecurityRule findSecurityRuleById(long id) {
		SecurityRule rule = getContentSecurityDao().findRuleById(id);
		if (rule==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.GROUP_NOT_FOUND);
		return rule;
	}
	
	
	@Override
	public Resource findResourceById(long id) {
		Resource resource = getContentDao().findResourceById(KBFile.class, id);
		if (resource==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.RESOURCE_NOT_FOUND);
		return resource;
	}
	
	@Override
	public Site findSiteByName(String name) {
		Site site = getPortalDao().findSiteByURI(name, getDomain());
		return site;
	}
	
	@Override
	public ResultSet getValues(long datasetId, String criteria, List<String> members, String sortCriteria, boolean facets) {
		DataSet dataset = findDataSetById(datasetId);
		Query query = new DataSetQuery(getQueryIndex(), dataset, facets);
		if (members!=null && !members.isEmpty()) {
			query.getParameters().put("members", members);
		}
		if (sortCriteria!=null) {
			if ("modified".equals(sortCriteria)) {
				query.getParameters().put("sort", "modified");
				query.getParameters().put("ascending", "false");
			}
			if ("title".equals(sortCriteria)) {
				query.getParameters().put("sort", "title_sort");
				query.getParameters().put("ascending", "true");
			}
		}
		if (criteria!=null) {
			query.getParameters().put("iql", criteria);
			query.getParameters().put("sort", "relevance");
			query.getParameters().put("ascending", "false");
		}
		ResultSet resultSet = query.execute();
		return resultSet;
	}
	
	public List<TreeNode> getRootValues(String datasetName) {
		DataSet dataSet = findDataSetByName(datasetName);
		
		Tree tree = ServiceLocator.getService(TreeService.class).getTree(dataSet);

		return tree.getRoots();
	}
	
	public List<TreeNode> getChildValues(DataSet dataSet, String path) {
		
		Tree tree = ServiceLocator.getService(TreeService.class).getTree(dataSet);
		
		TreeNode node = null;
		
		TreePath treePath = new KbeeTreePath();
        String nodes[] = path.split("/");
        for (int n=0; n<nodes.length; n++) {
        	String nodeid = nodes[n].trim();
    		DataSetMember value = findValueById(Long.valueOf(nodeid));
    		node = tree.getNode(value, treePath);
    		if (node==null) {
    			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND);
    		}
    		treePath = node.getPath();
        }
 		
		List<TreeNode> childs = tree.getChilds(node);
		
		return childs;
	}
	
	@Override
	public ResultSet getUsers(Domain domain, String criteria) {
		Query query = new UsersQuery(getQueryIndex(), getUserSet());
		if (criteria!=null) query.getParameters().put("iql", criteria);
		ResultSet resultSet = query.execute();
		return resultSet;
	}
	
	@Override
	public ResultSet getGroups(Domain domain, String criteria) {
		Query query = new GroupsQuery(getQueryIndex());
		query.getParameters().put("domain", String.valueOf(domain.getId()));
		if (criteria!=null) query.getParameters().put("iql", criteria);
		ResultSet resultSet = query.execute();
		return resultSet;
	}
	
	@Override
	public ResultSet getRoles(Domain domain, Boolean isApi) {
		Query query = new RolesQuery(domain, isApi);
		ResultSet resultSet = query.execute();
		return resultSet;
	}
	
	@Override
	public Role findRoleById(long id) {
		Role role = getContentSecurityDao().findRoleById(id);
		if (role==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ROLE_NOT_FOUND);
		return role;
	}
	
	@Override
	public ResultSet getSecurityRules(Domain domain) {
		Query query = new RulesQuery(domain);
		ResultSet resultSet = query.execute();
		return resultSet;
	}
	
	
	@Override
	public ResultSet getEmailTemplates(Domain domain) {
		Query query = new EmailTemplatesQuery(domain);
		ResultSet resultSet = query.execute();
		return resultSet;
	}
	
	@Override
	public EmailTemplate findEmailTemplate(Domain domain, String language, String key) {
		EmailTemplate template = (EmailTemplate)getContentDao().findEmailTemplate(domain, language, key);
		if (template==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND);
		return template;
	}
	
	@Override
	public ResultSet executeIql(String statement) throws ApiException {
		return executeIql(statement, false);
	}
	
	@Override
	public ResultSet executeIql(String statement, boolean allstates) throws ApiException {
		Query query = new ContentQuery(getQueryIndex(), statement, allstates);
		ResultSet resultSet = query.execute();
		return resultSet;
	}
	
	@Override
	public Command findCommandById(long id) {
		CommandService service = (CommandService) ServiceLocator.getService(CommandService.class);
		Command command = service.getCommand(id);
		if (command==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.COMMAND_NOT_FOUND);
		return command;
	}
	
	@Override
	public Resource upload(MultipartFile file) {
		
		String filename =  file.getOriginalFilename();
		KBFileImpl kbfile = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(filename);
		kbfile.setDomain(getDomain());
		
		String title = FilenameUtils.getBaseName(filename).replaceAll("(-|_)", " ");
		kbfile.setTitle(title);
		kbfile.setState(ObjectState.ENABLED);
		kbfile.setCreationOffsetDateTime(OffsetDateTime.now());
		kbfile.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		kbfile.setUploadOffsetDateTime(OffsetDateTime.now());

		InputStream is = null;
		try {
			is = file.getInputStream();
			kbfile.getService(KBFSResourceService.class).putObject(filename, is);
			kbfile.getService(KBFSResourceService.class).update();
		} 
		catch (IOException | FileServerException | ServiceNotFoundException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR);
		} 
		finally {
			if (is!=null)
				IOUtils.closeQuietly(is);
		}
		
		return kbfile;
	}
	
	@Override
	public List<UserDevice> getDevices() {
		List<UserDevice> devices = new ArrayList<UserDevice>();
		devices.addAll(getUserProfile().getDevices());
		return devices;
	}
	
	public ResultSet getUserActivities(User user, List<String> members, String sortCriteria, boolean facets) {
		Query query = new WorkspaceQuery(getQueryIndex(), user, facets);
		if (members!=null && !members.isEmpty()) {
			query.getParameters().put("members", members);
		}
		if (sortCriteria!=null) {
			if ("modified".equals(sortCriteria)) {
				query.getParameters().put("sort", "modified");
				query.getParameters().put("ascending", "false");
			}
			if ("title".equals(sortCriteria)) {
				query.getParameters().put("sort", "title_sort");
				query.getParameters().put("ascending", "true");
			}
		}
		ResultSet resultSet = query.execute();
		return resultSet;	
	}
	
	public ResultSet getMonitorActivities(List<String> members, String sortCriteria, boolean facets) {
		Query query = new MonitorQuery(getQueryIndex(), getSessionUser(), facets);
		if (members!=null && !members.isEmpty()) {
			query.getParameters().put("members", members);
		}
		if (sortCriteria!=null) {
			if ("modified".equals(sortCriteria)) {
				query.getParameters().put("sort", "modified");
				query.getParameters().put("ascending", "false");
			}
			if ("title".equals(sortCriteria)) {
				query.getParameters().put("sort", "title_sort");
				query.getParameters().put("ascending", "true");
			}
		}
		else {
			query.getParameters().put("sort", "modified");
			query.getParameters().put("ascending", "false");
		}
		ResultSet resultSet = query.execute();
		return resultSet;	
	}
	
	public ResultSet getWorkItems(List<String> members, String sortCriteria, boolean facets) {
		Query query = new PendingTasksQuery(getQueryIndex(), getSessionUser(), facets);
		if (members!=null && !members.isEmpty()) { 
			query.getParameters().put("members", members);
		}
		if (sortCriteria!=null) {
			if ("modified".equals(sortCriteria)) {
				query.getParameters().put("sort", "modified");
				query.getParameters().put("ascending", "false");
			}
			if ("title".equals(sortCriteria)) {
				query.getParameters().put("sort", "title_sort");
				query.getParameters().put("ascending", "true");
			}
		}
		ResultSet resultSet = query.execute();
		return resultSet;	
	}
	
	public ResultSet getFiles(Library library, List<String> members, String sortCriteria, boolean facets) {
		Query query = new LibraryQuery(getQueryIndex(), library, getSessionUser(), facets);
		if (members!=null && !members.isEmpty()) { 
			query.getParameters().put("members", members);
		}
		if (sortCriteria!=null) {
			if ("modified".equals(sortCriteria)) {
				query.getParameters().put("sort", "modified");
				query.getParameters().put("ascending", "false");
			}
			if ("title".equals(sortCriteria)) {
				query.getParameters().put("sort", "title_sort");
				query.getParameters().put("ascending", "true");
			}
		}
		ResultSet resultSet = query.execute();
		return resultSet;	
	}

	
	public ResultSet getFiles(Library library, Map<String, List<String>> filters, String sortCriteria, Boolean facets) {
		Query query = new LibraryQuery(getQueryIndex(), library, getSessionUser(), facets);
		if (filters.get("facets")!=null) { 
			query.getParameters().put("members", filters.get("facets"));
		}
		if (filters.get("text")!=null && !filters.get("text").isEmpty()) {
			query.getParameters().put("text", filters.get("text").get(0));
		}
		if (sortCriteria!=null) {
			if ("modified".equals(sortCriteria)) {
				query.getParameters().put("sort", "modified");
				query.getParameters().put("ascending", "false");
			}
			if ("title".equals(sortCriteria)) {
				query.getParameters().put("sort", "title_sort");
				query.getParameters().put("ascending", "true");
			}
		}
		ResultSet resultSet = query.execute();
		return resultSet;	
	}
	
	public ResultSet getFolder(Site site, DataSet dataset, DataSetMember folder, String sort) {
		
		Query query = FolderQuery.builder()
			.index(getQueryIndex())
			.site(site)
			.dataset(dataset)
			.folder(folder)
			.build();
			
		ResultSet resultSet = query.execute();
		return resultSet;	
	}


	
	@Override
	public Activity findActivityById(long id) {
		Activity activity = getWorkflowDao().findActivityById(id);
		if (activity==null) throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
		return activity;
	}
	
	public List<Person> getCollaborators(Activity activity, String event) {
		List<Person> collaborators = new ArrayList<Person>();
		Procedure procedure = activity.getProcess().getProcedure();
		KbeeWorkflowActivity kbactivity = (KbeeWorkflowActivity)activity;
		ManualEndCondition action = null;
		for (Task task : procedure.getTasks()) {
			if (task.getId().equals(kbactivity.getTaskName())) {
				UserTask ustask = (UserTask)task;
				for (EndCondition condition : ustask.getEndConditions()) {
					if (event.equals(condition.getEvent())) {
						action = (ManualEndCondition)condition;
						break;
					}
				}
			}
		}
		if (action!=null) {
			Map<String, Object> parameters= new HashMap<String, Object>();
			DataSet collaborationSet = action.getCollaborationSet();
			if (collaborationSet==null) collaborationSet = getUserSet();
			String qf = collaborationSet instanceof UserSet || !action.getCollaborationGroups().isEmpty() ? "isactive(true)" : "";
			if (action.getCollaborationGroups()!=null && !action.getCollaborationGroups().isEmpty()) {
				if (!"".equals(qf)) qf += " AND ";
				qf += getFilter(action.getCollaborationGroups());
			}
			if (!"".equals(qf))
			parameters.put("qf", qf);
			for (Suggestion suggestion : collaborationSet.getService(DataAccessService.class).getSuggestions(null, null, parameters)) {
				collaborators.add((Person)((ObjectModel<?>)suggestion.getObject()).getObject());
			};
		}
		return collaborators;
	}
	
	public List<Domain> getDomains(String email) {
		List<Domain> domains = new ArrayList<Domain>();
		for (UserProfile profile : getContentDao().findUserProfileByPersonEmail(email)) {
			if (!domains.contains(profile.getDomain())) {
				domains.add(profile.getDomain());
			}
		}
		Collections.sort(domains, new Comparator<Domain>() {
			@Override
			public int compare(Domain a, Domain b) {
				try {
					if (a.getOrganization()!=null && b.getOrganization()!=null)
						return a.getOrganization().compareTo(b.getOrganization());
					return 0;
				} 
				catch (Exception e)  {
					logger.error(e);
					return 0;
				}
			}
		}); 

		return domains;
	}
	
	public List<UserSignature> getSignatures() {
		List<UserSignature> signatures = new ArrayList<UserSignature>();
        signatures.addAll(getUserProfile().getSignatures());
		Collections.sort(signatures, new Comparator<UserSignature>() {
			@Override
			public int compare(UserSignature a, UserSignature b) {
				return a.getLastModifiedOffsetDateTime().isAfter(a.getLastModifiedOffsetDateTime()) ?
					-1 :
					1;	
			}
		});	
        return signatures;
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
	
	private SecurityDao getSecurityDao() {
		return (SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
	
	private PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private UserProfile getUserProfile() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile();
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
	private SessionFactory getSessionFactory() {
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		return sf;
	}
	
	private boolean isFilesMultiplicity(ProcessLauncher launcher, Multiplicity filesMultiplicity) {
		if (Multiplicity.M0N.equals(filesMultiplicity))
			return true;
		if (!launcher.isMobile())
			return false;
		for (Task task : launcher.getProcedure().getTasks()) {
			if (task instanceof KbeeTask) {
				for (EForm eform : ((KbeeTask)task).getForms()) {
					for (EFormField<?> field : eform.getFields()) {
						if ((Multiplicity.M01.equals(filesMultiplicity)||Multiplicity.M11.equals(filesMultiplicity)) && field instanceof EResourceField)
							return true;
						if ((Multiplicity.M1N.equals(filesMultiplicity)||Multiplicity.M11.equals(filesMultiplicity)) && field instanceof EResourcesField)
							return true;
						if ((Multiplicity.M1N.equals(filesMultiplicity)||Multiplicity.M11.equals(filesMultiplicity)) && field instanceof EResourceSystemField)
							return true;
					}
				}
			}
		}
		return false;
	}
	
	private String getFilter(List<Group> groups) {
		String statement = "";
		if (groups!=null && !groups.isEmpty()) {
			statement += "(";
			int g = 0;
			for (Group group : groups) {
				if (g++>0)
				statement += " OR ";
				statement += " member("+String.valueOf(group.getId())+")";
			}
			statement += " )";
		}
		return statement;
	}
	
	private UserSet getUserSet() {
		UserSet userset= null;
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (dataset instanceof UserSet && !(dataset instanceof UserSubset)) {
				userset = (UserSet)dataset;
				break;
			}
		}
		Assert.isTrue(userset!=null, "user set not found!");
		return userset;
	}
}
