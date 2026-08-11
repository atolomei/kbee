package com.novamens.content.dao;

import java.io.Serializable;
import java.time.OffsetDateTime;

import java.util.List;
import java.util.Map;

import com.novamens.content.user.externalLogin.UserExternalLoginPlatform;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentClass;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.base.SignedData;
import com.novamens.content.base.Source;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.document.TreeFile;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Entity;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.library.Library;
import com.novamens.content.model.*;
import com.novamens.content.notes.UserNote;
import com.novamens.content.notes.Billboard;

import com.novamens.content.notification.Notification;
import com.novamens.content.properties.ContentProperties;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.reportsubscription.ReportSubscription;
import com.novamens.content.resource.KBFile;
import com.novamens.content.resource.KBFileLoader;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.searcher.SearcherHomeBlock;
import com.novamens.content.service.domain.DomainSettings;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserSignature;
import com.novamens.dao.Dao;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.KBFSStorageType;
import com.novamens.dom.ObjectID;
import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Site;
import com.novamens.event.LogEvent;

import com.novamens.content.document.TreeIDoc;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.system.SystemParameter;
import com.novamens.workflow.Activity;

import kbee.content.support.SupportTicket;
import kbee.payment.Payment;

public interface ContentDao extends Dao {
	
	public void saveTreeFile(TreeFile tree_file);

	public KBFile findFileByPath(String path);
	public KBFile findKBFileByObjectName(String bucketName, String objectName);
	
	// Libraries
	public List<Library> getLibraries(Domain domain);
	public List<Library> getLibraries(Domain domain, ObjectState state);
	
	// UserNote
	public List<UserNote> getUserNotes(User user);
	public void delete(UserNote note);
	public void update(UserNote note);
	public void save(UserNote note);
	public void deleteAllNotes(User user);
	public long getTotalUserNotes(User user);
	
	// Billboard
	public List<Billboard> getBillboards(Domain domain);
	public void delete(Billboard note);
	public void update(Billboard note);
	public void save(Billboard note);
	public void deleteAllBillboards(Domain domain);
	
	public Object reload(Object object);
	public Object unproxy(Object object);
	
	// Recycle Bin
	public List<Content> getRecycleBinContents();
	
	// find
	public Object findObjectById(ObjectId id) throws ContentMgmtException;
	public Object findObjectById(ObjectID id); // analizar unificar

	public Content findContentById(Serializable id);
	public Content findContentById(ObjectID id);
	public Content findContentById(ContentId id);
	public Content findContentById(String clazz, String id);
	public Content findContentByOId(Serializable id);
	public Content findLastVersion(Serializable oid);
	public Content findContentByToken(String token);
	public Content findContentByResource(Resource resource);
	public Content findContentByExternalId(String source, String id);
	public Content findContentByExternalId(Source source, String id);
	public Content findContentById(Class<? extends Content> clazz, Serializable id);
	public Content findContentByName(Class<? extends Content> clazz, String name, Serializable domainid);

	public Source findSourceByName(String name);
	public Source findSourceById(Serializable id);
	
	public OrganizationalText findOrganizationalTextById(Long id);
	public TreeIDoc findTreeIDocById(Long id);
	public TreeFile findTreeFileById(Long id);
	
	public List<Content> findContentsByTitle(String title, Domain domain);

	public Resource findResourceById(Class<? extends Resource> clazz, Serializable id);
	public Resource findResourceByName(Class<? extends Resource> clazz,  String name, Serializable domainid);
	
	public ResourceTag findResourceGroupById(Serializable id);

	
	
	public Entity findEntityById(Class<? extends Entity> clazz, Serializable id);
	public DataSet findDataSetByName(String name, Serializable domainid);
	public DataSet findDataSetByAlias(String alias, Serializable domainid);
	DataSet findDataSetById(Serializable id);

	public ModelObject findModelObjectById(Class<? extends ModelObject> clazz, Serializable id);
	public ModelObject findModelObjectByName(Class<? extends ModelObject> clazz,  String name, Serializable domainid);
	public ModelObject findModelObjectByName(Class<? extends ModelObject> clazz,  ModelObject type, String name);
	
	public DataSetMember findMemberByValue(DataSet dataSet, String value);
	public List<DataSetMember> findMembersByValue(DataSet dataSet, String value);

	public DataSetMember findMemberById(Serializable id);
	public DataSetMember findMemberByExternalId(String id);

	public List<DataSetMember> findMembersByEntity(Entity entity);
	
	public List<SignedData> findSignedBySignature(UserSignature signature);
	public List<SignedData> findSignedByDevice(UserDevice device);

	public List<ClassifierTemplate> findClassifiersByContentTemplate(Serializable contentTemplateId);
	
	public Domain findDomainByName(String name);
	public Domain findDomainById(Serializable id);

	public UserProfile findUserProfileByUser(User user);
	public UserProfile findUserProfileByUserId(Serializable id);

	public List<UserExternalLoginPlatform> findUserExternalLoginPlatform(int platformId, int userPlatformIdType, String userPlatformId);
	
	public List<UserProfile> findUserProfileByDomain(Domain domain);

	List<ReportSubscription> findReportSubscriptionsByUser(Serializable userId);
	
	public ContentClass findContentClassByName(String name);

	public ContentTemplate findContentTemplateById( Serializable id);
	public ContentTemplate findContentTemplateByName(String name, Serializable domainid);
	
	
	
	public  KBFileLoader findFileLoaderByName(String name);
	
	public ContentProperties getContentProperties(Content content);
	
	
	@SuppressWarnings("rawtypes")
	public List getAuditTrail(Content content);

	@SuppressWarnings("rawtypes")
	public List getAuditTrail(User user);
	
	@SuppressWarnings("rawtypes")
	public List getActivity(User user);
	
	@SuppressWarnings("rawtypes")
	public List getAuditTrail(ModelObject object);
	
	@SuppressWarnings("rawtypes")
	public List getAuditTrail(Object object);
	
	@SuppressWarnings("rawtypes")
	public List getAuditTrail(Activity activity, EForm form);
	
	@SuppressWarnings("rawtypes")
	public List getAuditTrailDataSet(DataSet object);
	
	public void save(Content content) 						throws ContentMgmtException;
	public void save(Content content, boolean defaults) 	throws ContentMgmtException;
	public void delete(Content content) 					throws ContentMgmtException;
	
	public void save(ContentProperties contentProperties ) 	throws ContentMgmtException;
	public void delete(ContentProperties contentProperties) throws ContentMgmtException;
	
	public void save(Acl acl) 								throws ContentMgmtException;
	public void save(SecurityRule rule) 					throws ContentMgmtException;
	
	public void save(Resource resource) 					throws ContentMgmtException;
	public void saveTX(Resource resource) 					throws ContentMgmtException;
	public void delete(Resource resource) 					throws ContentMgmtException;

	public List<SecurityRule> getSecurityRules(Domain domain);
	
	public void save(ModelObject modelobj) throws ContentMgmtException;
	public void delete(ModelObject modelobj) throws ContentMgmtException, ConstraintException;
	
	public void save(Entity entity) throws ContentMgmtException;
	public void delete(Entity entity) throws ContentMgmtException, ConstraintException;
						
	public void save(Notification noti) throws ContentMgmtException;
	public void delete(Notification noti) throws ContentMgmtException;
	public void deleteNotifications(User user) throws ContentMgmtException;
	public List<Notification> getNotifications(User user) throws ContentMgmtException;
	public int getTotalNotifications(User user) throws ContentMgmtException;
	
	//public void save(ActionRule rule) throws ContentMgmtException;
	public void delete(ActionRule rule) throws ContentMgmtException, ConstraintException;
	public ActionRule findActionRuleByContentOId(Long contentOId);
	
	
	public void save(UserProfile profile) throws ContentMgmtException;
	
	public void save(com.novamens.dom.Object object) throws ContentMgmtException;
	public void delete(com.novamens.dom.Object object) throws ContentMgmtException, ConstraintException;
	
	public void save(User user) throws ContentMgmtException;
	
	public void save(SignedData data) throws ContentMgmtException;
	
	public void delete(UserSignature data) throws ContentMgmtException;
	
	public void save(ModelSection user) throws ContentMgmtException;
	
	public void refresh(Object object);
	
	public List<ContentTemplate> getTemplates();
	public List<ContentTemplate> getTemplates(Domain domain);
	
	public Domain getDomain();
	
	public List<Domain> getDomains();
	public List<Domain> getDomains(DomainType state);
	public List<Domain> getTemplateDomains();
	
	public List<DataSet> getDataSets(Serializable domainid);
	List<DataSet> getDataSets(String alias, long domainId);
	public List<DataSet> getDataSets(Serializable domainid, ObjectState state);
	public List<DataSet> getDataSets(Domain domain); 
	
	public List<DataSetMember> getMembers(DataSet dataSet, String orderby);
	
	public List<UserSet> getUserSets();
	public List<UserSet> getUserSets(Domain domain);
	public UserSet getUserSet();
	
	public List<Classifier> getClassifiers(Serializable domainid);
	public List<Classifier> getClassifiers(Domain domain);
	public List<Classifier>	getClassifiers(Serializable domainid, ObjectState state);
	public List<Classifier>	getClassifiers(String alias, Serializable domainid);
	
	public Classifier findClassifierByName(String name, Serializable domainid);
	public Classifier findClassifierByAlias(String alias, Serializable domainid);
	
	
	public List<Attribute> getAttributes(Domain domain);
	public List<Attribute> getAttributes(String alias, long domainId);
	public Attribute findAttributeByName(String name, Serializable domainid);

	public List<RelationshipByCriteriaTemplate> getRelationshipsByCriteria(Domain domain);
	public List<Relation> getRelationsByTemplate(RelationTemplate template);
	public List<RelationTemplate> getRelations(Domain domain);

	public List<? extends Content> getContent(Class<? extends Content> clazz, Serializable domainid);


	public List<ReportSubscription> findReportSubscriptionsForReportSchedule(Serializable reportScheduleId);

	public long getDatabaseSize();
	public String getDatabaseVersion();

	public long getTotalContents(Domain domain);
	public long getTotalContents(Domain domain, String cabinet_key);
	public long getTotalResources(Domain domain);
	
	public long getTotalResources(Domain domain, KBFSStorageType type);
	public long getTotalUsers(Domain domain);

	public long getTotalBillableUsers(Domain domain);
	
	/** ------------------------------------------------------------------------------------------ 
	 * INFORMATION MODEL
	 */
	public List<ContentClass> getClasses();
	
	/**  
	 * DomainSettings
	 * for Global Settings domain_id=0 
	 */
	public void save(DomainSettings settings);
	public void delete(DomainSettings settings);
	
	public DomainSettings findDomainSettings(Domain domain);
	public DomainSettings findDomainSettings(Domain domain, String category);
	
	
	public Content getNextVersion(Content content);
	
	/**  
	 * Monitor consistency 
	 * Workspace total based on HQL 
	 * @param user
	 * @return 
	 * Number of Content in user#s workspace calculated via HQL
	 * This value is to be checked against the result given by the SolrQuery
	 *  
		public int getTotalWorkspaceHQL(User user);
		public List<Content> getContentsWorkspaceHQL(User user);
	 *  
	 */
	public List<Content> getContents(Domain domain);
	
	public Content findContentByClassCodeOid(String class_code, String content_oid);
	public void cleanHibernateCache();
	
	public long getTotalClassifications(DataSetMember member);
	
	public long getTotalHardDisk(Domain domain);
	public long getTotalHardDisk(Domain domain, KBFSStorageType type);
	public long getTotalHardDisk(KBFSStorageType type);
	
	
	public long getTotalElements(DataSet dataSet);
	public List<? extends Content> getContent(ContentTemplate ct, Domain domain);
	public String pingDataBase();
	public void setSchema(String schema);
	public String getSchema();
	
	@SuppressWarnings("rawtypes")
	public List getDatabaseSettings();
	public List<LogEvent> getLabelsAuditTrail();
	public List<LogEvent> getAddResourcesAuditTrail(Content content);
	public List<LogEvent> getAddResourcesAuditTrail(Activity activity);
	
	
	public double findPersonEstimate(String lastname, String name, String email, Domain domain);
	public void deleteAllVersions(Content content) throws ContentMgmtException;
	
	public List<KBFile> getFiles(Domain domain);
	List<Domain> getAllDomains();
	List<Domain> getDomains(ObjectState state);
	List<Domain> getDomains(ObjectState state, int limit);

	public List<UserProfile> findUserProfileByPersonEmail(String email);
	public List<Person> findPersonByEmail(String email);
	public List<Person> findPersonByDisplayName(String displayname, Serializable domainid);
	
	public EmailTemplate	 	findEmailTemplate(Domain domain, String language, String key);
	public List<EmailTemplate>  getEmailTemplates(Domain domain);
	
	// KBEE DOMAIN
	public List<KBFile> getDefaultUserImages();
	
	public void flush();
	public void save(LogEvent objEvent);
	//public List<Content> getWorkspaceContents(User user);
	
	List<Content> getWorkspaceContents(User user, boolean order_by_newest, int limit);
	
	public void save(Library library) throws ContentMgmtException;
	
	// System Parameters
	//
	public List<SystemParameter> getSystemParameters();
	public void delete(SystemParameter value) throws ContentMgmtException;
	public void save(SystemParameter value) throws ContentMgmtException;
	public SystemParameter findSystemParameterByKey(String key);
	public String findSystemParameterValueByKey(String key, String default_value);
	
	// Support users
	//
	public List<User> findSupportAllUsers();
	//public List<Library> getAllCabinets(Domain domain);


	// returns the max oldest contents
	//
	public List<Content> getRecycleBinContents(int max);
	
	
	public void deleteNotification(User receiver, Billboard note) throws ContentMgmtException;
	
	public void deleteWorkNoteNotification(OffsetDateTime earlier_than);
	public List<String> getTables();
	void sessionFlush();
	
	
	

	void delete(EmailTemplate template) throws ContentMgmtException;
	void save(EmailTemplate template) throws ContentMgmtException;

	// KBFS 
	public List<KBFile> getKBFilesFromFileServer(String fileserverName, int limit) throws ContentMgmtException;
	long getTotalHardDisk(Domain domain, KBFSStorageType type, int shard);
	long getTotalHardDisk(KBFSStorageType type, int shard);
	long getTotalContents();
	long getTotalResources();
	long getTotalResources(KBFSStorageType type);
	long getTotalStoredHardDisk();
	public long getTotalUsers();
	public long getTotalResources(KBFSStorageType s, int shard);
	
	public String pingAPI();
	public List<UserProfile> getUserProfiles();
	Map<String, String> getDBServerMemInfo();
	List<String> getDBServerLoadAvg();
	boolean isPostgreSQL();
	
	//
	
	// total including versions
	public long getTotalExternalContents();
	
	// only head
	public long getTotalExternalLibraryContents();
	public long getTotalExternalArchiveContents();
	public long getTotalExternalRecycleContents();
	
	
	// total including versions
	public long getTotalExternalContents(Domain domain);
	
	// only head
	public long getTotalExternalLibraryContents(Domain domain);
	public long getTotalExternalArchiveContents(Domain domain);
	public long getTotalExternalRecycleContents(Domain domain);

	void save(ReportSubscription userReportSubscription) throws ContentMgmtException;


// public String getContentClassName(Content content) ;

	List<Content> getRecycleBinContents(int limit, Domain domain);
	
	
	// Searcher
	public void save (SearcherHomeBlock block);
	public List<SearcherHomeBlock> getSearcherHomeBlock(Domain domain);

	void deleteContentPublishNotification(OffsetDateTime earlier_than);
	void deleteNotification(User receiver, Content note) throws ContentMgmtException;

	public List<LogEvent> getAuditTrail(Site site);

	boolean hasEmailTemplates(Domain domain);

	public EmailTemplate findEmailTemplateById(Serializable id);

	public int getTotalBillboardNotifications(User user);

	public List<Notification> getAlertNotifications(User user) throws ContentMgmtException;
	public List<Notification> getBillboardNotifications(User user) throws ContentMgmtException;

	public Billboard findWorkNote(Long id);
	
	int getTotalUsersRead(Billboard note);

	Content findWorkspaceCopyContentByOId(Serializable oid);

    List<AttributeTemplate> findAttributesByContentTemplate(Serializable id);

    long getDataSetMemberWithContents(Domain domain, DataSet ds);

	long getTotalMembers(DataSet dataset);

	long getTotalDatasets(Domain domain);

	long getTotalClassifiers(Domain domain);

	long getTotalAttributes(Domain domain);

	long getTotalContentTemplates(Domain domain);

	List<ContentTemplate> getTemplates(Domain dm, ObjectState state);

	public long getTotalContents(ContentTemplate obj);

	long getAllStatesTotalMembers(DataSet dataset);


	public List<EmailTemplate> getEmailTemplates(Domain domain, String lang);

	public long getTotalEncryptedResources();

	public List<Classification> getBuiltInClassification(Classifier clasi, DataSet builtin, DataSetMember aggregator);


	public List<DataSetMember> getMembers(DataSet dataSet, String orderby, int limit);


	public List<ContentTemplate> getContentTemplates(Domain domain);
	public  List<ContentTemplate> getContentTemplates(Domain domain, ObjectState state);


	public List<DataSetMember> getDeletedDataSetMembers(OffsetDateTime since, int MAX_ITEMS_TO_PROCESS);


	/**
	*
	*/
	public void executeSelectNativeQuery(String native_query) throws ContentMgmtException;
	public void executeUpdateNativeQuery(String native_query) throws ContentMgmtException;
	public Integer executeCountNativeQuery(String native_query) throws ContentMgmtException;

	void save(Payment payment) throws ContentMgmtException;

	Person findPersonById(Serializable id);


	public List<Content> getContents(Domain domain, ObjectState state, int maxitems);


	
	SupportTicket findSupportTicket(Long id);


	void delete(SupportTicket note);


	void update(SupportTicket note);


	void save(SupportTicket note);


	List<SupportTicket> getSupportTickets(Domain domain);


	/**
	
	 * pending tickets where status is < 6
	 * 
	 * @param max_errorCount
	 * @return
	 */
	List<SupportTicket> getPendingSupportTickets(int max_errorCount);


	public long getTotalCountTasks(User user);


	List<Notification> getAlertNotifications(User user, int limit) throws ContentMgmtException;


	public long getTotalCountNotifications(User user);


	public Payment findPaymentById(Serializable id);

	public Payment findPaymentsByTrxReference(String trxReference);

	public List<Payment> findPaymentsByKey(String key, boolean confirmedOnly);

	public List<Payment> findPaymentsPending(OffsetDateTime sinceDate, int maxResults);



	public DataSetMember findMemberByKey(DataSet dataSet, String key);


	/**
	*
	*/
	List<DataSetMember> getMembers(DataSet dataSet, String orderby, ObjectState state, int limit);


	public long getTotalNotEncryptedResources(Domain domain);

	public List<? extends Object> getResultSet(String hql);
	
	public List<? extends Object> getResultSet(String hql, Map<String, Object> parameters);
	
	

	
	
	
	
}
