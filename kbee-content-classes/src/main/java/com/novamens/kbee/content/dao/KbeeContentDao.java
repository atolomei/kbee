package com.novamens.kbee.content.dao;


import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import com.novamens.content.model.*;
import com.novamens.content.reportsubscription.ReportSubscription;
import com.novamens.content.user.externalLogin.UserExternalLoginPlatform;
import kbee.payment.KbeePayment;
import kbee.payment.Payment;
import kbee.payment.PaymentStatus;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.postgresql.util.PSQLException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentClass;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.ContentResource;
import com.novamens.content.base.Relation;
import com.novamens.content.base.RelationshipByCriteria;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.base.SignedData;
import com.novamens.content.base.Source;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.QuestionAnswerDao;
import com.novamens.content.document.IDoc;
import com.novamens.content.document.TreeFile;
import com.novamens.content.document.TreeFileDir;
import com.novamens.content.document.TreeFileKBFile;
import com.novamens.content.document.TreeIDoc;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Entity;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.library.Library;
import com.novamens.content.notes.UserNote;
import com.novamens.content.notes.Billboard;
import com.novamens.content.notification.Notification;
import com.novamens.content.notification.NotificationState;
import com.novamens.content.orgchart.OrgChart;
import com.novamens.content.properties.ContentProperties;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.questionanswer.Answer;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.resource.KBFileLoader;
import com.novamens.content.resource.HTMLText;
import com.novamens.content.resource.KBGallery;
import com.novamens.content.resource.KBImage;
import com.novamens.content.resource.KBVideo;
import com.novamens.content.resource.TreeFileResource;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.searcher.SearcherHomeBlock;
import com.novamens.content.security.Role;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.service.TokenService;
import com.novamens.content.service.domain.DomainSettings;
import com.novamens.content.social.Comment;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.user.UserSignature;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.Json;
import com.novamens.dom.KBFSStorageType;
import com.novamens.dom.ObjectID;
import com.novamens.dom.ObjectState;
import com.novamens.event.BeforeUpdateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.EventService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.base.KbeeContentClass;
import com.novamens.kbee.content.base.KbeeContentProxy;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.base.KbeeSignedData;
import com.novamens.kbee.content.base.KbeeSource;
import com.novamens.kbee.content.communication.KbeeOrganizationalText;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.document.KbeeTreeFile;
import com.novamens.kbee.content.document.KbeeTreeFileDir;
import com.novamens.kbee.content.document.KbeeTreeFileKBFile;
import com.novamens.kbee.content.document.KbeeTreeIDoc;
import com.novamens.kbee.content.email.KbeeEmailTemplate;
import com.novamens.kbee.content.entity.KbeeOrganization;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.form.KbeeEFormData;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeContentResource;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.model.KbeeDateSet;
import com.novamens.kbee.content.model.KbeeEntitySet;
import com.novamens.kbee.content.model.KbeeExternalSet;
import com.novamens.kbee.content.model.KbeeLabelMember;
import com.novamens.kbee.content.model.KbeeLabelSet;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.kbee.content.model.KbeeMemberClassification;
import com.novamens.kbee.content.model.KbeePersonMember;
import com.novamens.kbee.content.model.KbeePersonSet;
import com.novamens.kbee.content.model.KbeeRelationTemplate;
import com.novamens.kbee.content.model.KbeeSecuredSet;
import com.novamens.kbee.content.model.KbeeUserListClassification;
import com.novamens.kbee.content.model.KbeeUserSet;
import com.novamens.kbee.content.model.KbeeUserSubset;
import com.novamens.kbee.content.model.KbeeValueSet;
import com.novamens.kbee.content.notes.KbeeUserNote;
import com.novamens.kbee.content.notes.KbeeBillboard;
import com.novamens.kbee.content.notes.KbeeWorkNoteUserRead;
import com.novamens.kbee.content.notification.KbeeContentConditionNotification;
import com.novamens.kbee.content.notification.KbeeContentPublishNotification;
import com.novamens.kbee.content.notification.KbeeWorkNoteNotification;
import com.novamens.kbee.content.orgchart.KbeeOrgChart;
import com.novamens.kbee.content.properties.KbeeContentProperties;
import com.novamens.kbee.content.questionanswer.KbeeAnswer;
import com.novamens.kbee.content.questionanswer.KbeeQuestion;
import com.novamens.kbee.content.resource.AbstractResource;
import com.novamens.kbee.content.resource.HTMLTextImpl;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.resource.KBFileLoaderImpl;
import com.novamens.kbee.content.resource.KBeeFileProxy;
import com.novamens.kbee.content.rule.KbeeActionRule;
import com.novamens.kbee.content.searcher.KbeeSearcherHomeBlock;
import com.novamens.kbee.content.resource.KBGalleryImpl;
import com.novamens.kbee.content.resource.KBImageImpl;
import com.novamens.kbee.content.resource.KBVideoImpl;
import com.novamens.kbee.content.security.KbeeDomainRole;
import com.novamens.kbee.content.security.KbeeEntityRole;
import com.novamens.kbee.content.security.KbeeMemberSecurityRule;
import com.novamens.kbee.content.security.KbeeSecurityRule;
import com.novamens.kbee.content.security.KbeeSiteSecurityRule;
import com.novamens.kbee.content.social.KbeeComment;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.content.userlist.KbeeUserList;
import com.novamens.kbee.content.userlist.KbeeUserListItem;
import com.novamens.kbee.content.workflow.KbeeActivityProgressNote;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.dom.KbeeModelObject;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.portal.model.KbeeArea;
import com.novamens.kbee.portal.model.KbeeBlock;
import com.novamens.kbee.portal.model.KbeeBlockListView;
import com.novamens.kbee.portal.model.KbeePage;
import com.novamens.kbee.portal.model.KbeePageSection;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.kbee.portal.model.KbeeViewDetailContent;
import com.novamens.kbee.portal.model.library.KbeeBlockGenericContentList;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.logging.AbstractLogEvent;
import com.novamens.logging.SendEmailEvent;
import com.novamens.portal6.model.Site;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.SystemParameter;
import com.novamens.system.properties.SystemPropertiesService;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.workflow.Activity;

import kbee.content.support.KbeeSupportTicket;
import kbee.content.support.SupportTicket;
import kbee.util.PropertiesFactory;


/**
 * Public
 * ------
 * Generic finders
 * Generic save
 * Generic delete
 * <p>
 * <p>
 * Private
 * -------
 * Specific finders
 * Specific save
 * Specific delete
 * <p>
 * <p>
 * <p>
 * ---
 * Restict
 * Cascade
 * <p>
 * <p>
 * REL
 * ---
 * TEM, SRC, TGET, position
 * <p>
 * <p>
 * src delete cascade
 * tget on delete restrict
 */
@SuppressWarnings({"unused"})
public class KbeeContentDao implements ContentDao, EventListener {

    private final String default_ping_api_query = "select count(*) from api_logevent  where (event_status=412 or  event_status=403 or event_status=500)  and event_time >(now() - INTERVAL '5 minute')\\:\\:timestamp";

    static private KBFSStorageType defaultStorageType;
    static private int MAX_FNAME_LENGTH = 440;

    private SessionFactory sessionFactory;
    private String schema;

    private FileServerV1 fileserver;
    private QuestionAnswerDao qadao;

    private Boolean bpostgres = null;
    private Boolean boracle = null;

    // TODO HA
    //
    private Map<String, ContentClass> contentclassesbyid;
    private Map<String, ContentClass> contentclassesbyname;

    // CACHE
    private Map<Serializable, OffsetDateTime> last_notification_check = new ConcurrentHashMap<Serializable, OffsetDateTime>();
    private Map<Serializable, List<Notification>> last_notification_check_list = new ConcurrentHashMap<Serializable, List<Notification>>();

    
    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeContentDao.class.getName());

    
    public KbeeContentDao() throws IOException {
        // Esto pasarlo a Spring
        qadao = KbeeQuestionAnswerDao.getInstance();
    }


    public void setDataSource(DataSource dataSource) {
    }

    /**
     * Set up by Spring
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     *
     */
    public User getSessionUser() {
        try {
            return ServiceLocator.getService(SecurityService.class).getSessionUser();
        } catch (Exception e) {
            return null;
        }
    }

    private String getSessionUserName() {
        User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
        if (user != null)
            return user.getUserName();
        return null;
    }

    /**
     * returns the Session User's Domain
     */
    public Domain getDomain() {
        User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
        if (user == null) return null;
        Domain domain = ((KbeeUser) user).getDomain();
        return domain;
    }

    /**
     * Returns Domains
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Domain> getDomains() {
        return (List<Domain>) getResultSet("FROM " + KbeeDomain.class.getSimpleName() + "  K order by lower(K.name)");
    }

    /**
     * Returns all Domains
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Domain> getAllDomains() {
        return (List<Domain>) getResultSet("FROM " + KbeeDomain.class.getSimpleName() + " K order by lower(K.name)");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Domain> getDomains(ObjectState state) {
   	 final HashMap<String, Object> parameters = new HashMap<>();
     parameters.put("state", state);
     return (List<Domain>) getResultSet("FROM " + KbeeDomain.class.getSimpleName() + " K where K.state = :state order by lower(K.name)",  parameters);
    	
    }
    	
    /**
     * Returns all Domains in State = state
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Domain> getDomains(ObjectState state, int limit) {
    	 final HashMap<String, Object> parameters = new HashMap<>();
         parameters.put("state", state);
         return (List<Domain>) getResultSet("FROM "+ KbeeDomain.class.getSimpleName() + " K where K.state = :state order by lower(K.name)",  parameters, limit);
      
         
         
    }

    /**
     * Returns all Templates in State ENABLED
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Domain> getTemplateDomains() {
   	 //final HashMap<String, Object> parameters = new HashMap<>();
     //parameters.put("state", String.valueOf(String.valueOf(ObjectState.ENABLED.getId())));
     //
     return (List<Domain>) getResultSet("FROM KbeeDomain K where K.state.id=" + String.valueOf(ObjectState.ENABLED.getId()) + " and K.istemplate=true order by lower(K.name)");
    }

    /**
     * Returns all Domains of type type  in State ENABLED
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Domain> getDomains(DomainType type) {
    	
    	//final HashMap<String, Object> parameters = new HashMap<>();
         //parameters.put("type", String.valueOf(String.valueOf(ObjectState.ENABLED.getId())));
         // return (List<Domain>) getResultSet("FROM KbeeDomain K where K.type = :type order by lower(K.name)", parameters);
         return (List<Domain>) getResultSet("FROM KbeeDomain K where K.type=" + String.valueOf(type.getId()) + " order by lower(K.name)");
    }

    /**
     *
     */
    public Domain findDomainByName(String name) {

    	if (name == null)
            return null;

   	 	final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("name", name.toLowerCase().replace("'", "''").trim());
        String hql = "FROM " + KbeeDomain.class.getSimpleName() +   " D WHERE lower(D.name) = :name";
        List results = getResultSet(hql, parameters);
        if (results.isEmpty())
            return null;
        return (Domain) results.get(0);
    }

    public Domain findDomainById(Serializable id) {
        return (Domain) this.sessionFactory.getCurrentSession().load(KbeeDomain.class, id);
    }


    public void save(Domain domain) {
        setDefaults(domain);
        this.sessionFactory.getCurrentSession().save(domain);
    }

    @Override
    public void save(Acl acl) throws ContentMgmtException {
        try {
            this.sessionFactory.getCurrentSession().save(acl);
        } catch (HibernateException e) {
        	logger.error(e);
            throw new ContentMgmtException(e);
        }
    }

    public void save(Source source) {
        setDefaults((KbeeSource) source);
        this.sessionFactory.getCurrentSession().save(source);
    }

    public void save(ModelSection source) {
        //setDefaults((KbeeSource)source);
//		this.sessionFactory.getCurrentSession().save(source);
    }

    @Override
    public void save(SecurityRule rule) throws ContentMgmtException {
        try {
            this.sessionFactory.getCurrentSession().save(rule);
        } catch (HibernateException e) {
        	logger.error(e);
            throw new ContentMgmtException(e);
        }
    }

    public void delete(Domain domain) {
        sessionFactory.getCurrentSession().delete(domain);
    }

    /**
     * Meta Classes & Templates
     */
    public ContentClass findContentClassByName2(Class<? extends Content> clazz) {
        ContentClass contentclass = (ContentClass) sessionFactory.getCurrentSession().get(KbeeContentClass.class, clazz.getName());
        return contentclass;
    }

    public ContentClass findContentClassByName(String contentClassName) {
        return getContentClassesByName().get(contentClassName);
    }

    /**
     * ContentClass: el id es la clase Java simple (sin el package)
     *
     * @param content
     */
    public ContentClass findContentClassByContent(Content content) {
        String classname = getContentClassName(content);
        return getContentClassesById().get(classname);
    }

    public String getContentClassName(Content content) {
        String classname = content.getClass().getSimpleName();
        int i = classname.indexOf("_");
        if (i > 0)
            classname = classname.substring(0, i);
        i = classname.indexOf("$");
        if (i > 0)
            classname = classname.substring(0, i);
        return classname;
    }

    @Override
    public ContentTemplate findContentTemplateById(Serializable id) {

    	String hql = "FROM KbeeContentTemplate U WHERE U.id = :id";
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        final List<?> resultSet = getResultSet(hql, parameters);
        if (!resultSet.isEmpty())
            return (ContentTemplate) resultSet.get(0);
        return null;
    }

    
    
    @Override
    @SuppressWarnings("rawtypes")
    public ContentTemplate findContentTemplateByName(String name, Serializable domainid) {

    	if (name == null)
            return null;
    	
    	// TBA
    	String hql = "FROM KbeeContentTemplate U WHERE lower(U.name) = '" + name.trim().replace("'", "''").toLowerCase() + "' AND U.domain.id= " + domainid.toString();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        if (results.isEmpty())
            return null;
        ContentTemplate contenttemplate = (ContentTemplate) results.get(0);
        return contenttemplate;
    }

    public List<UserSet> getUserSets() {
        return getUserSets(getDomain());
    }

    @Override
    public List<UserSet> getUserSets(Domain domain) {
        List<UserSet> usersets = new ArrayList<UserSet>();
        for (DataSet dataset : getDataSets(domain)) {
            if (dataset.getDataSetType().equals(DataSetType.USER)) {
            	dataset = (DataSet)unproxy(dataset);
                usersets.add((UserSet) dataset);
            }
        }
        return usersets;
    }


    public UserSet getUserSet() {
        List<UserSet> datasets = getUserSets(getDomain());
        if (datasets.size() != 1)
            logger.error("users set is 0 or more than 1");
        return datasets.get(0);
    }


    @Override
    public List<ContentTemplate> getTemplates(Domain dm) {
        return getTemplates(dm, null);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<ContentTemplate> getTemplates(Domain dm, ObjectState state) {
    	if (dm == null)
            return new ArrayList<ContentTemplate>();
    	// TBA
        String w = (state != null ? (" AND T.state=" + String.valueOf(state.getId()) + " ") : "");
        String hql = "FROM KbeeContentTemplate T WHERE T.domain.id=" + dm.getId().toString() + w + " order by T.name";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        List results = query.list();
        if (results.isEmpty())
            return new ArrayList<ContentTemplate>();
        for (Object result : results) {
            Hibernate.initialize(result);
        }

        return results;
    }


    @Override
    public List<ContentTemplate> getTemplates() {
        Domain domain = getDomain();
        return getTemplates(domain, null);
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<SecurityRule> getSecurityRules(Domain domain) {
        if (domain == null)
            return null;
        
    	// TBA
        String hql = "FROM KbeeSecurityRule T WHERE T.domain.id=" + domain.getId().toString() + " order by T.name";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        //query.setCacheable(true);
        //query.setCacheRegion("entity");
        List<?> results = query.list();
        if (results.isEmpty())
            return new ArrayList<SecurityRule>();
        return (List<SecurityRule>) results;
    }


    @Override
    public List<Library> getLibraries(Domain domain) {
        return getLibraries(domain, null);
    }


    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Library> getLibraries(Domain domain, ObjectState state) {

        if (domain == null)
            return new ArrayList<Library>();

    	// TBA
        String st = (state != null ? (" and T.state=" + String.valueOf(state.getId()) + " ") : "");
        String hql = "FROM KbeeLibrary T WHERE T.domain.id=" + domain.getId().toString() + " " + st + " order by T.listOrder";// order by T.orden";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        // query.setCacheRegion("entity");

        List results = query.list();

        if (results.isEmpty())
            return new ArrayList<Library>();

        return results;
    }


    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<RelationshipByCriteriaTemplate> getRelationshipsByCriteria(Domain domain) {
        if (domain == null)
            return new ArrayList<RelationshipByCriteriaTemplate>();

        
    	// TBA
        String hql = "FROM KbeeRelationshipByCriteriaTemplate T WHERE T.sourceTemplate.domain.id= '" + domain.getId().toString() + "'";

        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("content");
        List results = query.list();
        if (results.isEmpty())
            return new ArrayList<RelationshipByCriteriaTemplate>();
        return results;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Relation> getRelationsByTemplate(RelationTemplate template) {
        
    	
    	// TBA
    	String hql = "FROM KbeeRelation R WHERE R.template.id = " + template.getId().toString();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setMaxResults(100);
        List results = query.getResultList();
        return results;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<RelationTemplate> getRelations(Domain domain) {

    	if (domain == null)
            return new ArrayList<RelationTemplate>();

    	// TBA
        String hql = "FROM KbeeRelationTemplate R WHERE R.domain.id=" + domain.getId().toString();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        // query.setCacheRegion("entity");
		List results = query.list();
        if (results.isEmpty())
            return new ArrayList<RelationTemplate>();

        return results;
    }


    public Map<String, ContentClass> getContentClassesById() {
        return getContentClasses(true);
    }

    public Map<String, ContentClass> getContentClassesByName() {
        return getContentClasses(false);
    }

    public void save(ContentTemplate template) {

        setDefaults(template);

        for (AttributeTemplate attribute : template.getAttributes()) {
            sessionFactory.getCurrentSession().save(attribute);
        }
        for (ProcessLauncher launcher : template.getProcessLaunchers()) {
            sessionFactory.getCurrentSession().save(launcher);
        }
        sessionFactory.getCurrentSession().save(template);
    }


    public void delete(ContentTemplate template) {
        sessionFactory.getCurrentSession().delete(template);
    }

    /**
     * Resources
     */
    public Resource findResourceByName(Class<? extends Resource> clazz, String name, Serializable domainid) {

        if (name == null)
            return null;
        String hql = "FROM " + clazz.getSimpleName() + " F WHERE F.name = '" + name.replace("'", "''") + "' and F.domain.id='" + domainid.toString() + "'";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        @SuppressWarnings("rawtypes")
        List results = query.list();
        if (results.isEmpty())
            return null;
        Resource res = (Resource) results.get(0);
        return res;
    }


    @Override
    public KBFile findFileByPath(String path) {
        if (path == null)
            return null;
        String hql = "FROM KBFileImpl F WHERE F.url = '" + path.replace("'", "''").trim() + "'";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        @SuppressWarnings("rawtypes")
        List results = query.list();
        if (results.isEmpty())
            return null;
        KBFile file = (KBFile) results.get(0);
        return file;
    }

    public KBFile findKBFileByObjectName(String bucketName, String objectName) {
    	   
    	if (bucketName == null)
            return null;
    	
    	if (objectName == null)
               return null;
    	
         String hql = "FROM KBFileImpl F WHERE F.objectName='"+objectName.replace("'", "''").trim()+ "' and F.bucketName='" + bucketName.replace("'", "''").trim() + "'";
           
         logger.debug(hql);
         
         org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
          @SuppressWarnings("rawtypes")
          List results = query.list();
          if (results.isEmpty())
              return null;
          KBFile file = (KBFile) results.get(0);
          return file;
    }
    
    /**
     * ----------------------------------------------------------------------------------------
     */
    public List<KBFile> getFiles(Domain domain) {
        String hql = "FROM KBFileImpl R WHERE R.domain.id=" + domain.getId().toString() + " ORDER BY R.version desc ";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        @SuppressWarnings("unchecked")
        List<KBFile> results = (List<KBFile>) query.list();
        if (results.isEmpty())
            return null;
        return results;
    }


    /**
     *
     */
    public Resource findResourceById(Class<? extends Resource> clazz, Serializable id) {
        if (id == null)
            return null;
        try {
            Long lid = (id instanceof Long ? (Long) id : Long.valueOf(id.toString()));
            if (clazz.equals(KBFile.class)) return (KBFile) sessionFactory.getCurrentSession().load(KBFileImpl.class, lid);
            if (clazz.equals(KBFileImpl.class)) return (KBFileImpl) sessionFactory.getCurrentSession().load(KBFileImpl.class, lid);
            if (clazz.equals(KBImage.class)) return (KBImage) sessionFactory.getCurrentSession().load(KBImageImpl.class, lid);
            if (clazz.equals(KBVideo.class)) return (KBVideo) sessionFactory.getCurrentSession().load(KBVideoImpl.class, lid);
            if (clazz.equals(KBGallery.class)) return (KBGallery) sessionFactory.getCurrentSession().load(KBGalleryImpl.class, lid);
            if (clazz.equals(HTMLText.class)) return (HTMLText) sessionFactory.getCurrentSession().load(HTMLTextImpl.class, id);
            if (clazz.equals(KbeeTreeFile.class)) return (KbeeTreeFile) sessionFactory.getCurrentSession().load(KbeeTreeFile.class, id);
            if (clazz.equals(Resource.class)) return (AbstractResource) sessionFactory.getCurrentSession().load(AbstractResource.class, id);
            return null;
        } catch (IllegalArgumentException e) {
            logger.error(e, getSessionUserName());
            return null;
        }
    }

    /**
     *
     */
    @Override
    public ResourceTag findResourceGroupById(Serializable id) {
        if (id == null)
            return null;
        try {
            return (KbeeResourceTag) sessionFactory.getCurrentSession().load(KbeeResourceTag.class, id);
        } catch (IllegalArgumentException e) {
            logger.error(e, getSessionUserName());
            return null;
        }
    }


    /**
     *
     */
    public void save(Resource resource) throws ContentMgmtException {
        if (resource instanceof KBFile) save((KBFile) resource);
        else if (resource instanceof HTMLText) save((HTMLText) resource);
        else if (resource instanceof TreeFileResource) save((TreeFileResource) resource);
        else if (resource instanceof ResourceFolder) save((ResourceFolder) resource);
        else
            Assert.isTrue(true, "Class not supported " + resource.getClass().getName());
    }

    /**
     * 
     */
    @Transactional
    public void saveTX(Resource resource)  {
    	sessionFactory.getCurrentSession().save(resource);
    }

    public void delete(Resource resource) throws ContentMgmtException {
        if (resource instanceof KBFile) {
            delete((KBFile) resource);
        } else
            Assert.isTrue(true, "Class not supported " + resource.getClass().getName());
    }

    public void delete(KBFile file) {
        sessionFactory.getCurrentSession().delete(file);
    }

    /**
     * Model
     */
    public ModelObject findModelObjectByName(Class<? extends ModelObject> clazz, String name, Serializable domainid) {
        if (domainid == null)
            return null;
        
        if (clazz.equals(DataSet.class)) 		return findDataSetByName(name, domainid);
        if (clazz.equals(Classifier.class)) 	return findClassifierByName(name, domainid);
        
        if (clazz.equals(DataSetMember.class))
            throw new KbeeRuntimeException("Can not call findModelObjectByName with parameter " + clazz + "  " + name);
        
        return null;
    }

    public ModelObject findModelObjectByName(Class<? extends ModelObject> clazz, ModelObject type, String name) {
        if (clazz.equals(DataSetMember.class))
            return findMemberByValue((DataSet) type, name);
        return null;
    }

    public ModelObject findModelObjectById(Class<? extends ModelObject> clazz, Serializable id) {
        if (id == null)
            return null;
        try {
            Long lid = (id instanceof Long ? (Long) id : Long.valueOf(id.toString()));
            if (clazz.equals(LabelSet.class)) return (LabelSet) sessionFactory.getCurrentSession().get(KbeeLabelSet.class, lid);
            if (clazz.equals(DataSet.class)) return (DataSet) sessionFactory.getCurrentSession().get(KbeeDataSet.class, lid);         // findDataSetById (lid);
            if (clazz.equals(DataSetMember.class)) return (DataSetMember) sessionFactory.getCurrentSession().get(KbeeDataSetMember.class, lid);  // findDataSetMemberById (lid);
            if (clazz.equals(Classifier.class)) return (Classifier) sessionFactory.getCurrentSession().get(KbeeClassifier.class, lid);        // findClassifierById (lid);
            if (clazz.equals(Attribute.class)) return (Attribute) sessionFactory.getCurrentSession().get(KbeeAttribute.class, lid);        // findClassifierById (lid);
            if (clazz.equals(ContentTemplate.class)) return (ContentTemplate) sessionFactory.getCurrentSession().get(KbeeContentTemplate.class, lid);        // findClassifierById (lid);

            logger.error("findModelObjectById. Class not mapped: " + clazz.getName());
            return null;
        } catch (IllegalArgumentException e) {
            logger.error(e, getSessionUserName());
            return null;
        }
    }

    public Entity findEntityById(Class<? extends Entity> clazz, Serializable id) {
        if (id == null)
            return null;
        try {
            Long lid = (id instanceof Long ? (Long) id : Long.valueOf(id.toString()));
            if (clazz.equals(KbeePerson.class) || clazz.equals(Person.class)) return (KbeePerson) sessionFactory.getCurrentSession().load(KbeePerson.class, lid);       // findPersonById (lid);
            if (clazz.equals(KbeeOrganization.class)) return (KbeeOrganization) sessionFactory.getCurrentSession().load(KbeeOrganization.class, lid); // findOrganizationById (lid);
            return null;
        } catch (IllegalArgumentException e) {
            logger.error(e, getSessionUserName());
            return null;
        }
    }


    @Override
    @SuppressWarnings("rawtypes")
    public DataSet findDataSetByName(String name, Serializable domainid) {
        if (name == null)
            return null;
        String hql = "FROM KbeeDataSet D WHERE lower(D.name) = '" + name.trim().replace("'", "''").toLowerCase() + "' AND D.domain.id=" + domainid.toString();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        if (results.isEmpty())
            return null;
        DataSet dataset = (DataSet) results.get(0);
        return dataset;
    }


    @Override
    @SuppressWarnings("rawtypes")
    public DataSet findDataSetByAlias(String alias, Serializable domainid) {
        if (alias == null)
            return null;
        String hql = "FROM KbeeDataSet D WHERE D.alias = '" + alias.trim().replace("'", "''").toLowerCase() + "' AND D.domain.id=" + domainid.toString();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        if (results.isEmpty())
            return null;
        DataSet dataset = (DataSet) results.get(0);
        return dataset;
    }

    
    @Override
    @SuppressWarnings("rawtypes")
    public Classifier findClassifierByAlias(String alias, Serializable domainid) {
        if (alias == null)
            return null;
        String hql = "FROM KbeeClassifier D WHERE D.alias = '" + alias.trim().replace("'", "''").toLowerCase() + "' AND D.domain.id=" + domainid.toString();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        if (results.isEmpty())
            return null;
        Classifier classifier = (Classifier) results.get(0);
        return classifier;
    }
    
    
    @Override
    public DataSet findDataSetById(Serializable id) {
        if (id == null)
            return null;
        try {
            return (DataSet) sessionFactory.getCurrentSession().get(KbeeDataSet.class, id);
        } catch (IllegalArgumentException e) {
            logger.error(e, getSessionUserName());
            return null;
        }
    }

    
    /**
     * create index datasetmember_state_idx on datasetmember (state, lastmodifieddate desc);
     * 
     * @param max
     * @param since_date_deleted
     * @return
     */
    public List<DataSetMember> getDeletedDataSetMembers(OffsetDateTime since_date_deleted, int max) {
    	
        if (max == 0)
            return null;

        String str_since = "";
        
        if (since_date_deleted!=null)
        	str_since = " AND  D.lastModifiedDate >= " +  DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(since_date_deleted) + " ";
        
        String hql = "FROM KbeeDataSetMember D WHERE D.state="+String.valueOf(ObjectState.DELETED.getId()) + str_since + " order by D.lastModifiedDate desc";
        
        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        List<DataSetMember> members = (List<DataSetMember>) results;
        return members;
    }
    
    
    @Override
	public DataSetMember findMemberByKey(DataSet dataset, String key) {

		if (key == null || dataset == null)
            return null;

        String hql = "FROM KbeeDataSetMember D WHERE D.key='" + key.trim().replace("'", "''") + "' AND D.dataset.id=" + dataset.getId().toString();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        
        List<DataSetMember> members = (List<DataSetMember>) results;

        if (members.isEmpty()) 
        	return null;
        
        //if (members.size() > 1) {
         //   for (DataSetMember member : members) {
          //      if (member.getStrValue().equals(key)) {
           //         return member;
            //    }
           // }
        //}
        return members.get(0);
	}
	
    
    @Override
    public DataSetMember findMemberByValue(DataSet dataset, String value) {

        if (value == null || dataset == null)
            return null;

        String hql = "FROM KbeeDataSetMember D WHERE lower(D.strvalue)='" + value.trim().replace("'", "''").toLowerCase() + "' AND D.dataset.id=" + dataset.getId().toString();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        List<DataSetMember> members = (List<DataSetMember>) results;
        if (members.isEmpty()) return null;
        if (members.size() > 1) {
            for (DataSetMember member : members) {
                if (member.getStrValue().equals(value)) {
                    return member;
                }
            }
        }
        return members.get(0);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<DataSetMember> findMembersByValue(DataSet dataset, String value) {

        if (value == null || dataset == null)
            return null;

        String hql = "FROM KbeeDataSetMember D WHERE lower(D.strvalue)='" + value.trim().replace("'", "''").toLowerCase() + "' AND D.dataset.id=" + dataset.getId().toString();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        List<DataSetMember> members = (List<DataSetMember>) results;
        return members;
    }



    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<DataSetMember> findMembersByEntity(Entity entity) {
        long s = System.currentTimeMillis();
        String hql = "FROM KbeeDataSetMember WHERE entity_id = " + entity.getId();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);

        query.setCacheable(true);

        // query.setCacheRegion("entity");

        List results = query.list();
        List<DataSetMember> members = (List<DataSetMember>) results;

        logger.debug(String.valueOf(System.currentTimeMillis() - s) + " ms -> " + hql);
        return members;
    }


    @SuppressWarnings("unchecked")
    public List<ClassifierTemplate> findClassifiersByContentTemplate(Serializable contentTemplateId) {

        Map<String, Object> params = new HashMap<>();
        params.put("id", contentTemplateId);

        List<KbeeContentTemplate> results = (List<KbeeContentTemplate>) getResultSet("FROM KbeeContentTemplate WHERE id = :id", params);
        if (results.size() == 0)
            return null;

        return results.get(0).getClassifiers();
    }

    @SuppressWarnings("unchecked")
    public List<AttributeTemplate> findAttributesByContentTemplate(Serializable contentTemplateId) {

        Map<String, Object> params = new HashMap<>();
        params.put("id", contentTemplateId);

        List<KbeeContentTemplate> results = (List<KbeeContentTemplate>) getResultSet("FROM KbeeContentTemplate WHERE id = :id", params);
        if (results.size() == 0)
            return null;

        return results.get(0).getAttributes();
    }

    /**
     * ----------------------------------------------------------------------------------------
     */
    public DataSetMember findMemberById(Serializable id) {
        if (id == null)
            return null;
        try {
            return (DataSetMember) sessionFactory.getCurrentSession().get(KbeeDataSetMember.class, id);
        } 
        catch (IllegalArgumentException e) {
            logger.error(e, getSessionUserName());
            if (logger.isDebugEnabled()) {
                logger.error(e, "finding member");
            }
            return null;
        }
    }

    /**
     * ----------------------------------------------------------------------------------------
     */
    @SuppressWarnings("unchecked")
    public DataSetMember findMemberByExternalId(String id) {
    	
    	
        if (id == null) return null;
        DataSetMember member = null;
        String hql = "FROM KbeeDataSetMember WHERE externalId = '" + id + "'";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List<?> results = query.list();
        List<DataSetMember> members = (List<DataSetMember>) results;
        if (!members.isEmpty())
            member = members.get(0);
        return member;
    }
    
    
    /**
     * ----------------------------------------------------------------------------------------
     */
    @SuppressWarnings("unchecked")
    public List<SignedData> findSignedBySignature(UserSignature signature) {
    	Map<String, Object> params = new HashMap<>();
        params.put("id", signature.getId());
        String stm = "FROM KbeeSignedData WHERE signature.id = :id";
        List<SignedData> signed = (List<SignedData>) getResultSet(stm, params);
        return signed;
    }
    
    /**
     * ----------------------------------------------------------------------------------------
     */
    @SuppressWarnings("unchecked")
    public List<SignedData> findSignedByDevice(UserDevice device) {
    	Map<String, Object> params = new HashMap<>();
        params.put("id", device.getId());
        String stm = "FROM KbeeSignedData WHERE device.id = :id";
        List<SignedData> signed = (List<SignedData>) getResultSet(stm, params);
        return signed;
    }

    /**
     * ----------------------------------------------------------------------------------------
     */
    public KBFileLoader findFileLoaderByName(String name) {

        KBFileLoader loader;

        CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<KBFileLoaderImpl> criteria = criteriabuilder.createQuery(KBFileLoaderImpl.class);
        Root<KBFileLoaderImpl> loaders = criteria.from(KBFileLoaderImpl.class);
        ParameterExpression<String> nameparameter = criteriabuilder.parameter(String.class);
        criteria.select(loaders).where(criteriabuilder.equal(loaders.get("name"), nameparameter));
        TypedQuery<KBFileLoaderImpl> query = sessionFactory.getCurrentSession().createQuery(criteria);
        query.setHint("org.hibernate.cacheable", true);
        query.setFlushMode(FlushModeType.COMMIT);
        query.setParameter(nameparameter, name);
        loader = !query.getResultList().isEmpty() ? query.getSingleResult() : null;
        return loader;
    }

    /**
     * ----------------------------------------------------------------------------------------
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Classifier findClassifierByName(String name, Serializable domainid) {

        if (name == null)
            return null;
        										
        String hql = "FROM KbeeClassifier C WHERE lower(C.name) = '" + name.toLowerCase().replace("'", "''").trim() + "' AND C.domain.id=" + domainid.toString();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        if (results.isEmpty())
            return null;
        Classifier classifier = (Classifier) results.get(0);
        return classifier;
    }


    public void save(DataSet dataset) {
        setDefaults(dataset);
        sessionFactory.getCurrentSession().save(dataset);
    }


    public void save(DataSetMember member) {
        if (member.getDataSet().getDataSetType() == DataSetType.DATE)
            return;

        setDefaults(member);

        if (member instanceof PersonMember) {
            setDefaults(((PersonMember) member).getPerson());
            sessionFactory.getCurrentSession().save(((PersonMember) member).getPerson());
        }

        sessionFactory.getCurrentSession().save(member);
    }


    public void delete(PersonMember member) throws ContentMgmtException {
    	Person person = ((PersonMember)member).getPerson();
        ServiceLocator.getService(SecurityContentMgmtService.class).delete(person);
    }
    
    public void delete(DataSetMember member) throws ContentMgmtException {
        try {
        	sessionFactory.getCurrentSession().delete(member);
        } 
        catch (Exception e) {
            logger.error(e, getSessionUserName());
            throw new ContentMgmtException(e);
        }
    }

    /**
     * 
     */
    public void save(Classifier classifier) {
        setDefaults(classifier);
        sessionFactory.getCurrentSession().save(classifier);
    }

    /**
     * ----------------------------------------------------------------------------------------
     */
    public void save(Attribute attribute) {
        setDefaults(attribute);
        sessionFactory.getCurrentSession().save(attribute);
    }

    /**
     * ----------------------------------------------------------------------------------------
     * Security & Users
     */
    @SuppressWarnings("rawtypes")
    public User findUserProfileByUsername(String username, Serializable domainid) {
        if (username == null)
            return null;
        String hql = "FROM KbeeUserProfile U where U.user.name = '" + username.replace("'", "''") + "' AND U.domain.id= " + domainid.toString();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        List results = query.list();
        if (results.isEmpty())
            return null;
        User user = (User) results.get(0);
        return user;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<UserProfile> findUserProfileByPersonEmail(String email) {
        Query query = sessionFactory.getCurrentSession().createQuery("select uspro from KbeeUserProfile uspro, KbeePerson per where per.email = :email and uspro.entity.id = per.id");
        query.setParameter("email", email);
        query.setCacheable(true);
        query.setCacheRegion("query");
        query.setHibernateFlushMode(FlushMode.COMMIT);
        List list = query.list();
        return list;
    }


    /**
     * Security & Users
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<Person> findPersonByEmail(String email) {

        if (email == null)
            return null;

        String hql = "FROM KbeePerson U where U.email = '" + email.replace("'", "''").trim() + "'";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        //query.setCacheable(true);
        //query.setCacheRegion("entity");
        List results = query.list();
        if (results.isEmpty())
            return null;
        return (List<Person>) results;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<ReportSubscription> findReportSubscriptionsByUser(Serializable userId) {
        if (userId == null)
            return new ArrayList<>();
        String hql = "FROM KbeeReportSubscription U where U.usr = '" + userId + "'";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        return (List<ReportSubscription>) results;
    }

    /**
     * ----------------------------------------------------------------------------------------
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<Person> findPersonByDisplayName(String displayname, Serializable domainid) {

        if (displayname == null)
            return null;

        String hql = "FROM KbeePerson U where U.lastName LIKE '%" + displayname.replace("'", "''") + "%' AND U.domain.id= '" + domainid.toString() + "'";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        //query.setCacheable(true);
        //query.setCacheRegion("entity");
        List results = query.list();
        List<Person> persons = (List<Person>) results;
        if (persons.isEmpty()) {
            hql = "FROM KbeePerson U where concat(U.firstName, ' ', U.lastName) LIKE '%" + displayname.replace("'", "''") + "%' AND U.domain.id= '" + domainid.toString() + "'";
            query = sessionFactory.getCurrentSession().createQuery(hql);
            //query.setCacheable(true);
            //query.setCacheRegion("entity");
            results = query.list();
            persons = (List<Person>) results;
        }
        return persons;
    }

    /**
     * ----------------------------------------------------------------------------------------
     */
    public UserProfile findUserProfileByUser(User user) {
        if (user == null)
            return null;

        try {
            UserProfile profile;

            CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<KbeeUserProfile> criteria = criteriabuilder.createQuery(KbeeUserProfile.class);
            Root<KbeeUserProfile> profiles = criteria.from(KbeeUserProfile.class);
            ParameterExpression<Long> parameter = criteriabuilder.parameter(Long.class);
            criteria.select(profiles).where(criteriabuilder.equal(profiles.get("user").get("id"), parameter));

            TypedQuery<KbeeUserProfile> query = sessionFactory.getCurrentSession().createQuery(criteria);
            query.setHint("org.hibernate.cacheable", true);
            query.setFlushMode(FlushModeType.COMMIT);

            query.setParameter(parameter, (long) user.getId());
            profile = !query.getResultList().isEmpty() ? query.getSingleResult() : null;

            return profile;
        } catch (javax.persistence.PersistenceException e) {
            logger.error(e, "Can not connect to database , raise alert  ????");
            throw (e);
        } catch (Exception e) {
            logger.error(e);
            throw (e);
        }
    }


    public void save(UserProfile profile) throws ContentMgmtException {
        ((KbeeUserProfile) profile).setLastModifiedOffsetDateTime(OffsetDateTime.now());
        ((KbeeUserProfile) profile).setLastModifiedUser(getSessionUser());

        //if (profi le.getQ ueries()!=null)
        //for (Sav edQuery query : profile.ge tQueries()) {
        //	sessionF actory.getCurren tSession().s ave(query);
        //}


        if (profile.getStartPage() == null)
            profile.setStartPage(((profile.getDomain() != null) && (profile.getDomain().getDomainType() == DomainType.EXPRESS)) ? "library" : "mytasks");

        sessionFactory.getCurrentSession().save(profile);
    }


    public UserProfile findUserProfileByUserId(Serializable id) {
        UserProfile profile;

        CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<KbeeUserProfile> criteria = criteriabuilder.createQuery(KbeeUserProfile.class);
        Root<KbeeUserProfile> profiles = criteria.from(KbeeUserProfile.class);
        ParameterExpression<Long> parameter = criteriabuilder.parameter(Long.class);
        criteria.select(profiles).where(criteriabuilder.equal(profiles.get("user").get("id"), parameter));

        TypedQuery<KbeeUserProfile> query = sessionFactory.getCurrentSession().createQuery(criteria);
        query.setFlushMode(FlushModeType.COMMIT);
        query.setParameter(parameter, (long) id);
        ((Query<?>) query).setCacheable(true);
        ((Query<?>) query).setCacheRegion("query");
        profile = query.getSingleResult();

        return profile;
    }

    @SuppressWarnings("unchecked")
	public List<UserExternalLoginPlatform> findUserExternalLoginPlatform(int platformId, int userPlatformIdType, String userPlatformId){
        String hql = "FROM KbeeUserExternalLoginPlatform l WHERE l.platformId=:platformId AND l.userPlatformIdType = :userPlatformIdType AND l.userPlatformId =:userPlatformId ";
        HashMap<String, Object> params = new HashMap<>();
        params.put("platformId", platformId);
        params.put("userPlatformId", userPlatformId);
        params.put("userPlatformIdType", userPlatformIdType);
        return (List<UserExternalLoginPlatform>) getResultSet(hql, params);

    }


    public void save(Principal principal) throws IOException {
        sessionFactory.getCurrentSession().save(principal);
    }


    public void delete(Principal principal) throws IOException {
        sessionFactory.getCurrentSession().delete(principal);
    }

    public void save(User user) {
        sessionFactory.getCurrentSession().save(user);
    }
    
    public void save(SignedData data) {
        sessionFactory.getCurrentSession().save(data);
    }

    /**
     *
     */
    public void save(ModelObject modelobj) throws ContentMgmtException {
        if (modelobj instanceof DataSet) save((DataSet) modelobj);
        else if (modelobj instanceof Classifier) save((Classifier) modelobj);
        else if (modelobj instanceof Attribute) save((Attribute) modelobj);
        else if (modelobj instanceof DataSetMember) save((DataSetMember) modelobj);
        else if (modelobj instanceof ContentTemplate) save((KbeeContentTemplate) modelobj);
        else
            Assert.isTrue(true, "Class not supported " + modelobj.getClass().getName());
    }

    public void delete(ModelObject modelobject) throws ContentMgmtException, ConstraintException {
        try {
            if (modelobject instanceof DataSet) delete((DataSet) modelobject);
            else if (modelobject instanceof Classifier) delete((Classifier) modelobject);
            else if (modelobject instanceof Attribute) delete((Attribute) modelobject);
            else if (modelobject instanceof ContentTemplate) delete((ContentTemplate) modelobject);
            else if (modelobject instanceof PersonMember) delete((PersonMember) modelobject);
            else if (modelobject instanceof DataSetMember) delete((DataSetMember) modelobject);
            else
                Assert.isTrue(true, "Class not supported " + modelobject.getClass().getName());
        } 
        catch (ConstraintViolationException e) {
            throw new ConstraintException(e);
        } 
        catch (Exception e) {
            logger.error(e);
            throw new ContentMgmtException(e);
        }
    }

    public void delete(DataSet dataSet) {
        sessionFactory.getCurrentSession().delete(dataSet);
    }

    public void delete(Classifier classifier) {
        sessionFactory.getCurrentSession().delete(classifier);
    }

    public void delete(Attribute attribute) {
        sessionFactory.getCurrentSession().delete(attribute);
    }

    public List<DataSet> getDataSets(Domain domain) {
        return getDataSets(domain.getId());
    }

    @Override
    public List<DataSet> getDataSets(Serializable domainid) {
        return getDataSets(domainid, ObjectState.ENABLED);
    }

    @SuppressWarnings("unchecked")
    public List<DataSet> getDataSets(String alias, long domainId) {
        //language=HQL
        String hql = "FROM KbeeDataSet ds WHERE ds.alias = :alias and ds.domain.id = :domainId";
        HashMap<String, Object> params = new HashMap<>();
        params.put("alias", alias);
        params.put("domainId", domainId);
        return (List<DataSet>) getResultSet(hql, params);
    }

    @Override
    public List<DataSet> getDataSets(Serializable domainid, ObjectState state) {
        CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<KbeeDataSet> criteria = criteriabuilder.createQuery(KbeeDataSet.class);
        Root<KbeeDataSet> datasets = criteria.from(KbeeDataSet.class);
        ParameterExpression<Long> domainparameter = criteriabuilder.parameter(Long.class);
        ParameterExpression<ObjectState> stateparameter = criteriabuilder.parameter(ObjectState.class);
        if (state != null)
            criteria.select(datasets).where(criteriabuilder.and(criteriabuilder.equal(datasets.get("domain").get("id"), domainparameter), criteriabuilder.equal(datasets.get("state"), stateparameter)));
        else
            criteria.select(datasets).where(criteriabuilder.equal(datasets.get("domain").get("id"), domainparameter));
        criteria.orderBy(criteriabuilder.asc(datasets.get("name")));
        TypedQuery<KbeeDataSet> query = sessionFactory.getCurrentSession().createQuery(criteria);
        query.setFlushMode(FlushModeType.COMMIT);
        query.setParameter(domainparameter, (long) domainid);

        if (state != null)
            query.setParameter(stateparameter, state);

        List<DataSet> result = new ArrayList<DataSet>();
        result.addAll(query.getResultList());
        return result;
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<DataSetMember> getMembers(DataSet dataSet, String orderby, int limit) {
        return (List<DataSetMember>) getResultSet("FROM KbeeDataSetMember WHERE dataset.id=" + dataSet.getId().toString() + (orderby != null ? (" order by " + orderby) : ""), limit);
    }

    /**
    *
    */
   @SuppressWarnings("unchecked")
   @Override
   public List<DataSetMember> getMembers(DataSet dataSet, String orderby, ObjectState state, int limit) {
       return (List<DataSetMember>) getResultSet("FROM KbeeDataSetMember WHERE dataset.id=" + 
   dataSet.getId().toString() + 
   (" AND state=" + String.valueOf(state.getId()) + " ") 
   +
   (orderby != null ? (" order by " + orderby) : ""), limit);
   }


   
   
    // " order by lower(M.strvalue)"

    
    
    /**
     *
     */
    public List<Classifier> getClassifiers(Domain domain) {
        return getClassifiers(domain.getId(), null);
    }

    /**
     *
     */
    public List<Classifier> getClassifiers(Serializable domainid) {
        return getClassifiers(domainid, null);
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public List<Classifier> getClassifiers(Serializable domainid, ObjectState state) {

        String str_state = (state != null ? ("and state=" + String.valueOf(state.getId())) : "");
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeClassifier C WHERE C.domain.id=" + domainid.toString() + "  " + str_state + " order by C.order");

        try {
            query.setCacheable(true);
            query.setCacheRegion("query");
            List<?> result = query.list();
            return (List<Classifier>) result;
        } catch (Exception e) {
            logger.error(e, "hql " + query.toString());
            throw (e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Classifier> getClassifiers(String alias, Serializable domainid) {
        //language=HQL
        String hql = "FROM KbeeClassifier clf WHERE clf.alias = :alias and clf.domain.id = :domainId";
        HashMap<String, Object> params = new HashMap<>();
        params.put("alias", alias);
        params.put("domainId", domainid);
        return (List<Classifier>) getResultSet(hql, params);
    }


    /**
     *
     */
    public List<Attribute> getAttributes(Domain domain) {
        return getAttributes(domain.getId(), null);
    }

    @SuppressWarnings("unchecked")
    public List<Attribute> getAttributes(String alias, long domainId) {
        String hql = "FROM KbeeAttribute ds WHERE ds.alias = :alias and ds.domain.id = :domainId";
        HashMap<String, Object> params = new HashMap<>();
        params.put("alias", alias);
        params.put("domainId", domainId);
        return (List<Attribute>) getResultSet(hql, params);
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public Attribute findAttributeByName(String name, Serializable domainid) {
        if (name == null)
            return null;
        String hql = "FROM KbeeAttribute ds WHERE lower(ds.name) = '" + name.trim().toLowerCase() + "' AND ds.domain.id= '" + domainid.toString() + "'";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        if (results.isEmpty())
            return null;
        Attribute attribute = (Attribute) results.get(0);
        return attribute;
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public List<Attribute> getAttributes(Serializable domainid, ObjectState state) {
        String str_state = (state != null ? ("and state=" + String.valueOf(state.getId())) : "");
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeAttribute A WHERE A.domain.id=" + domainid.toString() + "  " + str_state + " order by A.order");
        query.setCacheable(true);
        query.setCacheRegion("query");
        List<?> result = query.list();
        return (List<Attribute>) result;
    }


    @Override
    public List<ContentTemplate> getContentTemplates(Domain domain) {
        return getContentTemplates(domain, null);
    }

    @Override
    public List<ContentTemplate> getContentTemplates(Domain domain, ObjectState state) {
        String str_state = (state != null ? ("and state=" + String.valueOf(state.getId())) : "");
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeContentTemplate A WHERE A.domain.id=" + domain.getId().toString() + "  " + str_state + " order by A.name");
        query.setCacheable(true);
        query.setCacheRegion("query");
        List<?> result = query.list();
        return (List<ContentTemplate>) result;
    }

    
    /**
     * 
     * Entities
     */
    public void save(Entity entity) {
        setDefaults(entity);
        ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(entity));
        sessionFactory.getCurrentSession().save(entity);
    }


    public void delete(Entity entity) throws ContentMgmtException, ConstraintException {
        for (DataSetMember member : findMembersByEntity(entity)) {
        	if (member instanceof KbeePersonMember) {
        		if (entity.equals(((KbeePersonMember)member).getPerson())) {
        			((KbeePersonMember)member).setPerson(null);
                    delete(member);
        		}    
        	}
        	else {
        		delete(member);
        	}
        }
        sessionFactory.getCurrentSession().delete(entity);
        sessionFactory.getCache().evict(KbeeAclEntry.class);
    }

    public void save(Person person) throws ContentMgmtException {
        setDefaults(person);
        UserProfile profile = person.getProfile(UserProfile.class);
        save(profile);
        ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(person));
        sessionFactory.getCurrentSession().save(person);
    }



    public void save(KbeeOrganization organization) throws ContentMgmtException {
        setDefaults(organization);
        sessionFactory.getCurrentSession().save(organization);
    }

    @Override
    public void save(Payment payment) throws ContentMgmtException {
        setDefaults(payment);
        sessionFactory.getCurrentSession().save(payment);
    }

    @Override
    public Person findPersonById(Serializable id) {
        return (Person) sessionFactory.getCurrentSession().load(KbeePerson.class, id);
    }


    //@Override
    //public void save(SavedQuery query) {
    //	sessionFactory.getCurrentSession().save(query);
    //}


    //@Override
    //public void delete(SavedQuery query) {
    //	sessionFactory.getCurrentSession().delete(query);
    //}


    @Override
    public void save(DomainSettings settings) {
        sessionFactory.getCurrentSession().save(settings);
    }

    /**
     * ----------------------------------------------------------------------------------------
     */

    @Override
    public void delete(DomainSettings settings) {
        sessionFactory.getCurrentSession().delete(settings);
    }


    @Override
    public DomainSettings findDomainSettings(Domain domain) {
        return findDomainSettings(domain, domain.getName());
    }

    @Override
    public DomainSettings findDomainSettings(Domain domain, String category) {
        String hql = "FROM KbeeDomainSettings U where U.domain.id = '" + domain.getId().toString() + "' AND lower(U.category)='" + category.toLowerCase() + "'";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        
        // query.setCacheable(true);
        // query.setCacheRegion("entity");
        
        @SuppressWarnings("rawtypes")
        List results = query.list();
        if (results.isEmpty())
            return null;
        DomainSettings settings = (DomainSettings) results.get(0);
        return settings;
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */

    @Override
    public void save(com.novamens.dom.Object object) throws ContentMgmtException {
        if (object instanceof ContentTemplate)
            save((ContentTemplate) object);
        else if (object instanceof ModelObject)
            save((ModelObject) object);
        else if (object instanceof Person)
            save((Person) object);
        else if (object instanceof DataSet)
            save((DataSet) object);
        else if (object instanceof ContentTemplate)
            save((ContentTemplate) object);
        else if (object instanceof ModelSection)
            save((ModelSection) object);
        else if (object instanceof Domain)
            save((Domain) object);
        else if (object instanceof TreeFile)
            saveTreeFile((TreeFile) object);
        else if (object instanceof Source)
            save((Source) object);
        else if (object instanceof UserSignature)
            save((UserSignature) object);
        else if (object instanceof SupportTicket)
            save((SupportTicket) object);
        else
            sessionFactory.getCurrentSession().save(object);
    }

    @Override
    public void delete(com.novamens.dom.Object object) throws ContentMgmtException, ConstraintException {
        if (object instanceof ModelObject)
            delete((ModelObject) object);
        else if (object instanceof Entity)
            delete((Entity) object);
        else if (object instanceof Domain)
            delete((Domain) object);
        else if (object instanceof ContentTemplate)
            delete((ContentTemplate) object);
        else if (object instanceof TreeFile)
            delete((TreeFile) object);
        else if (object instanceof Library)
            delete((Library) object);
        else if (object instanceof ActionRule)
            delete((ActionRule) object);
        else if (object instanceof Source)
            delete((Source) object);
        else if (object instanceof SupportTicket)
            delete((SupportTicket) object);
        else if (object instanceof UserSignature)
            delete((UserSignature) object);
    }

    @Override
    public EmailTemplate findEmailTemplateById(Serializable id) {
        return (KbeeEmailTemplate) sessionFactory.getCurrentSession().get(KbeeEmailTemplate.class, id);
    }


    /**
     * Content Queries
     */
    @Override
    public Content findContentById(Class<? extends Content> clazz, Serializable id) {
        if (id == null) return null;
        try {
            Long lid = (id instanceof Long ? (Long) id : Long.valueOf(id.toString()));

            if (clazz.equals(KbeeIDoc.class) || clazz.equals(IDoc.class))
                return (KbeeIDoc) sessionFactory.getCurrentSession().get(KbeeIDoc.class, lid);

            if (clazz.equals(KbeeTreeIDoc.class) || clazz.equals(TreeIDoc.class))
                return (KbeeTreeIDoc) sessionFactory.getCurrentSession().get(KbeeTreeIDoc.class, lid);

            if (clazz.equals(KbeeOrgChart.class))
                return (OrgChart) sessionFactory.getCurrentSession().get(KbeeOrgChart.class, lid);

            if (clazz.equals(OrganizationalText.class))
                return findOrganizationalTextById(lid);

            return null;
        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            return null;
        }
    }
    
    
    @Override
    public Content findContentByToken(String token) {
    	try {
	    	Json data = ServiceLocator.getService(TokenService.class).decode(token);
	    	
	    	if (data==null) 
	    		return null;
	    	
	    	String id = (String)data.get("id");
	    	String oid = (String)data.get("oid");
	    	
	    	Content content = null;
	    	
	    	if (data.get("process")==null) {
	    		if (oid==null) return null;
	    		content = findContentByOId(Long.valueOf(oid));
	    	}
	    	else {
	    		if (id==null) 
	    			return null;
	    		
	    			content = findContentById(Long.valueOf(id));
	    			
		    	if (content==null) 
		    		return null;
		    	
	    		WorkflowService ws = content.getService(WorkflowService.class);
	    		
	    		if (!ws.active()) 
	    			return null;
	    		
	    	
	    		com.novamens.workflow.Process process = ws.getContext().getProcess();
	    		if (!String.valueOf(process.getId()).equals(data.get("process"))) 
	    			return null;
	    		
	    	}

	    	if (content==null) 
	    		return null;
	    	
	     	if (data.get("token")!=null) {
	     		if (!data.get("token").equals(content.getService(PropertyService.class).getProperty("token")))
	     			return null;
	    	}
	    	
	     	if (!content.getCreationOffsetDateTime().toString().equals(data.get("date"))) 
	     		return null;
	     	
	    	if (!String.valueOf(content.getDomain().getId()).equals(data.get("domain"))) 
	    		return null;
	    	
	    	return content;
    	}
    	catch (Exception e) {
    		logger.error(e);
    	}
    	return null;
    }
    
    @Override
    public Content findContentByName(Class<? extends Content> clazz, String name, Serializable domainid) {

        if (clazz.equals(Question.class))
            return qadao.findQuestionByName(name, domainid);

        if (clazz.equals(Answer.class))
            return qadao.findAnswerByName(name, domainid);

        String hql = "FROM " + clazz.getSimpleName() + " U WHERE lower(U.name) = '" + name.toLowerCase().trim() + "'AND U.ishead=true AND U.domain.id= '" + domainid.toString() + "'";


        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);

        //query.setCacheable(true);
        //query.setCacheRegion("content");

        @SuppressWarnings("rawtypes")
        List results = query.list();

        if (results.isEmpty())
            return null;

        Content content = (Content) results.get(0);
        return content;
    }


    @Override
    public List<Content> findContentsByTitle(String title, Domain domain) {
        CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();

        CriteriaQuery<KbeeContent> criteria = cb.createQuery(KbeeContent.class);

        Root<KbeeContent> content = criteria.from(KbeeContent.class);

        ParameterExpression<String> titleparameter = cb.parameter(String.class);
        ParameterExpression<Long> domainparameter = cb.parameter(Long.class);

        criteria.select(content).where(cb.and(
                cb.equal(content.get("domain").get("id"), domainparameter),
                cb.equal(content.get("title"), titleparameter),
                cb.equal(content.get("ishead"), true)));

        TypedQuery<KbeeContent> query = sessionFactory.getCurrentSession().createQuery(criteria);
        query.setHint("org.hibernate.cacheable", true);
        query.setFlushMode(FlushModeType.COMMIT);
        query.setParameter(titleparameter, title);
        query.setParameter(domainparameter, (long) domain.getId());

        List<KbeeContent> result = query.getResultList();
        List<Content> contents = new ArrayList<Content>();
        contents.addAll(result);

        return contents;
    }

    public Content findContentById(ObjectID id) {
        return findContentById(id.getClassName(), id.getId());
    }

    public Content findContentById(ContentId id) {
        return (findContentById(id.getClassName(), id.getId()));
    }
    
    @Override
    public Content findContentByResource(Resource resource) {
        String hql = "FROM KbeeContentResource CR WHERE CR.resource.id= " + resource.getId() + " order by CR.content.id desc";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        @SuppressWarnings("rawtypes")
        List results = query.list();
        if (results.isEmpty())
            return null;
        KbeeContentResource contentresource = (KbeeContentResource) results.get(0);
        return contentresource.getContent();
    }

    /**
     * ----------------------------------------------------------------------------------------------------
     *
     * @param clazz
     * @param id
     */
    @Override
    public Content findContentById(String clazz, String id) {
        Content content = null;
        switch (clazz.toLowerCase()) {
            case "kbeeidoc":
                content = (Content) sessionFactory.getCurrentSession().get(KbeeIDoc.class, Long.valueOf(id));
                break;
            case "kbeeorganizationaltext":
                content = (Content) sessionFactory.getCurrentSession().get(KbeeOrganizationalText.class, Long.valueOf(id));
                break;
            case "kbeequestion":
                content = (Content) sessionFactory.getCurrentSession().get(KbeeQuestion.class, Long.valueOf(id));
                break;
            case "kbeetext":
                content = findOrganizationalTextById(Long.valueOf(id));
                break;
            case "kbeetreeidoc":
                content = (Content) findTreeIDocById(Long.valueOf(id));
                break;
            case "kbeeorgchart":
                content = (Content) sessionFactory.getCurrentSession().get(KbeeOrgChart.class, Long.valueOf(id));
                break;
        }
        ;
        return content;
    }

    /**
     * ------------------------------------------------------------------------------------------------
     *
     * @param id
     */
    @Override
    public Object findObjectById(ObjectId id) throws ContentMgmtException {

        Object object = null;
        switch (id.getClassName().toLowerCase()) {
            //
            //
            case "idoc":
                object = (Content) sessionFactory.getCurrentSession().get(KbeeIDoc.class, Long.valueOf(id.getId()));
                break;
            case "kbeeidoc":
                object = (Content) sessionFactory.getCurrentSession().get(KbeeIDoc.class, Long.valueOf(id.getId()));
                break;
            case "treeidoc":
                object = (Content) sessionFactory.getCurrentSession().get(KbeeTreeIDoc.class, Long.valueOf(id.getId()));
                break;
            case "kbeetreeidoc":
                object = (Content) sessionFactory.getCurrentSession().get(KbeeTreeIDoc.class, Long.valueOf(id.getId()));
                break;

            case "kbfile":
                object = (KBFile) sessionFactory.getCurrentSession().get(KBFileImpl.class, Long.valueOf(id.getId()));
                break;
            case "kbfileimpl":
                object = (KBFileImpl) sessionFactory.getCurrentSession().get(KBFileImpl.class, Long.valueOf(id.getId()));
                break;
            case "kbeefileproxy":
                object = (KBeeFileProxy) sessionFactory.getCurrentSession().get(KBeeFileProxy.class, Long.valueOf(id.getId()));
                break;

            case "kbeeresourcegroup":
                object = (KbeeResourceTag) sessionFactory.getCurrentSession().get(KbeeResourceTag.class, Long.valueOf(id.getId()));
                break;

            case "treefilekbfile":
                object = (TreeFileKBFile) sessionFactory.getCurrentSession().get(KbeeTreeFileKBFile.class, Long.valueOf(id.getId()));
                break;
            case "kbeetreefilekbfile":
                object = (KbeeTreeFileKBFile) sessionFactory.getCurrentSession().get(KbeeTreeFileKBFile.class, Long.valueOf(id.getId()));
                break;
            case "treefiledir":
                object = (TreeFileDir) sessionFactory.getCurrentSession().get(KbeeTreeFileDir.class, Long.valueOf(id.getId()));
                break;
            case "kbeetreefiledir":
                object = (KbeeTreeFileDir) sessionFactory.getCurrentSession().get(KbeeTreeFileDir.class, Long.valueOf(id.getId()));
                break;
            case "treefile":
                object = (TreeFile) sessionFactory.getCurrentSession().get(KbeeTreeFile.class, Long.valueOf(id.getId()));
                break;
            case "kbeetreefile":
                object = (KbeeTreeFile) sessionFactory.getCurrentSession().get(KbeeTreeFile.class, Long.valueOf(id.getId()));
                break;

            case "library":
                object = (Library) sessionFactory.getCurrentSession().get(KbeeLibrary.class, Long.valueOf(id.getId()));
                break;
            case "kbeelibrary":
                object = (KbeeLibrary) sessionFactory.getCurrentSession().get(KbeeLibrary.class, Long.valueOf(id.getId()));
                break;
            case "kbeeformtemplate":
                object = (KbeeEForm) sessionFactory.getCurrentSession().get(KbeeEForm.class, Long.valueOf(id.getId()));
                break;
            case "kbeeperson":
                object = (Person) sessionFactory.getCurrentSession().get(KbeePerson.class, Long.valueOf(id.getId()));
                break;

            case "organizationaltext":
            case "kbeeorganizationaltext":
                object = findOrganizationalTextById(Long.valueOf(id.getId()));
                break;
            case "kbeeusermember":
            case "kbeevaluemember":
            case "kbeepersonmember":
            case "kbeeentitymember":
            case "kbeesecuredmember":
            case "kbeeexternalmember":
            case "kbeedatasetmember":
            case "kbeepersonsubsetmember":
                object = sessionFactory.getCurrentSession().get(KbeeDataSetMember.class, Long.valueOf(id.getId()));
                break;
            case "kbeelabelmember":
                object = sessionFactory.getCurrentSession().get(KbeeLabelMember.class, Long.valueOf(id.getId()));
                break;
            case "kbeeorgchart":
                object = (OrgChart) sessionFactory.getCurrentSession().get(KbeeOrgChart.class, Long.valueOf(id.getId()));
                break;

            case "kbeeemailtemplate":
                object = sessionFactory.getCurrentSession().get(KbeeEmailTemplate.class, Long.valueOf(id.getId()));
                break;

            case "kbeeuserset":
                object = (UserSet) sessionFactory.getCurrentSession().get(KbeeUserSet.class, Long.valueOf(id.getId()));
                break;
            case "kbeedateset":
                object = (DataSet) sessionFactory.getCurrentSession().get(KbeeDateSet.class, Long.valueOf(id.getId()));
                break;
            case "kbeeusersubset":
                object = (UserSubset) sessionFactory.getCurrentSession().get(KbeeUserSubset.class, Long.valueOf(id.getId()));
                break;
            case "kbeepersonset":
                object = (PersonSet) sessionFactory.getCurrentSession().get(KbeePersonSet.class, Long.valueOf(id.getId()));
                break;
            case "kbeeexternalset":
                object = (ExternalSet) sessionFactory.getCurrentSession().get(KbeeExternalSet.class, Long.valueOf(id.getId()));
                break;
            case "kbeelabelset":
                object = (LabelSet) sessionFactory.getCurrentSession().get(KbeeLabelSet.class, Long.valueOf(id.getId()));
                break;
            case "kbeedataset":
                object = (DataSet) sessionFactory.getCurrentSession().get(KbeeDataSet.class, Long.valueOf(id.getId()));
                break;
            case "kbeeclassifier":
                object = (Classifier) sessionFactory.getCurrentSession().get(KbeeClassifier.class, Long.valueOf(id.getId()));
                break;
            case "kbeecontentclass":
                object = (ContentClass) sessionFactory.getCurrentSession().get(KbeeContentClass.class, Long.valueOf(id.getId()));
                break;
            case "kbeeattribute":
                object = (Attribute) sessionFactory.getCurrentSession().get(KbeeAttribute.class, Long.valueOf(id.getId()));
                break;
            case "kbeedomain":
                object = (Domain) sessionFactory.getCurrentSession().get(KbeeDomain.class, Long.valueOf(id.getId()));
                break;

            case "modelobject":
                object = (ModelObject) sessionFactory.getCurrentSession().get(KbeeModelObject.class, Long.valueOf(id.getId()));
                break;
            case "kbeemodelobject":
                object = (KbeeModelObject) sessionFactory.getCurrentSession().get(KbeeModelObject.class, Long.valueOf(id.getId()));
                break;

            case "kbeequestion":
                object = (Content) sessionFactory.getCurrentSession().get(KbeeQuestion.class, Long.valueOf(id.getId()));
                break;
            case "kbeerelationtemplate":
                object = (RelationTemplate) sessionFactory.getCurrentSession().get(KbeeRelationTemplate.class, Long.valueOf(id.getId()));
                break;

            // Workflow
            case "kbeeprocedure":
                object = (KbeeProcedure) sessionFactory.getCurrentSession().get(KbeeProcedure.class, Long.valueOf(id.getId()));
                break;
                
            case "kbeeactivityprogressnote":
                object = (KbeeActivityProgressNote) sessionFactory.getCurrentSession().get(KbeeActivityProgressNote.class, Long.valueOf(id.getId()));
                break;

            // Events
            case "logevent":
            case "securityevent":
            case "loginevent":
            case "logoutevent":
            case "securitycreateevent":
            case "securitydeleteevent":
            case "securityupdateevent":

            case "userupdateevent": // deprecated
            case "ruleupdateevent": // deprecated

            case "worknotedeleteevent":
            case "worknotecreateevent":
            case "worknoteupdateevent":


            case "librarycreateevent":
            case "libraryupdateevent":

            case "sourcecreateevent":
            case "sourceupdateevent":

            case "emailtemplatecreateevent":
            case "emailtemplateupdateevent":


            case "objectupdateevent":

                // Event-Content
                //
            case "applicationdeployevent":
            case "applicationstartevent":
            case "assignationevent":
            case "checkinevent":
            case "creationevent":
            case "checkoutevent":
            case "dropcheckoutevent":
            case "readevent":
            case "removeevent":
            case "updateevent":
            case "updateaddresourceevent":

            // Event-Workflow
            //
            case "taskstartevent":
            case "taskreassignedformerownerevent":
            case "taskpendingevent":
            case "taskendevent":
            case "duedatealertevent":
            case "progressnoteevent":

                // Event-DataSet
                //
            case "datasetvaluecreateevent":
            case "datasetvaluedeleteevent":
            case "datasetvalueupdateevent":

                // Event-Model
                //
            case "modelevent":
            case "modelcreateevent":
            case "modelupdateevent":
            case "modeldeleteevent":
            case "notificationevent":

                // Event-Domain
                //
            case "domaincreateevent":
            case "domaindeleteevent":
            case "domainupdateevent":

                // Event-TreeFile
                //
            case "treefilecreationevent":
            case "treefileupdateevent":
            case "treefiledeleteevent":
            case "treefileevent":

            case "downloadevent":

                // Event-Site
                //
            case "sitecreationevent":
            case "sitedeleteevent":
            case "siteupdateevent":
            case "siteviewcontentpublishevent":
            case "siteviewcontentunpublishevent":
            case "emptyrecyclebinevent":
                object = sessionFactory.getCurrentSession().get(AbstractLogEvent.class, Long.valueOf(id.getId()));
                break;


            case "sendemailevent":
                object = sessionFactory.getCurrentSession().get(SendEmailEvent.class, Long.valueOf(id.getId()));
                break;

            // Event-Security
            //
            case "user":
                object = ServiceLocator.getService(SecurityService.class).findUserById(id.getId());
                break;
            case "kbeegroup":
                object = (KbeeGroup) sessionFactory.getCurrentSession().get(KbeeGroup.class, Long.valueOf(id.getId()));
                break;
            case "principal":
                object = ServiceLocator.getService(SecurityService.class).findPrincipalById(id.getId());
                break;

            case "kbeesecurityrule":
                object = (KbeeSecurityRule) sessionFactory.getCurrentSession().get(KbeeSecurityRule.class, Long.valueOf(id.getId()));
                break;
            case "kbeemembersecurityrule":
                object = (KbeeMemberSecurityRule) sessionFactory.getCurrentSession().get(KbeeMemberSecurityRule.class, Long.valueOf(id.getId()));
                break;
            case "kbeesitesecurityrule":
                object = (KbeeSiteSecurityRule) sessionFactory.getCurrentSession().get(KbeeSiteSecurityRule.class, Long.valueOf(id.getId()));
                break;

            case "kbeedomainrole":
                object = (KbeeDomainRole) sessionFactory.getCurrentSession().get(KbeeDomainRole.class, Long.valueOf(id.getId()));
                break;
            case "kbeeentityrole":
                object = (KbeeEntityRole) sessionFactory.getCurrentSession().get(KbeeEntityRole.class, Long.valueOf(id.getId()));
                break;

            case "kbeeusernote":
                object = (KbeeUserNote) sessionFactory.getCurrentSession().get(KbeeUserNote.class, Long.valueOf(id.getId()));
                break;
            case "kbeebillboard":
                object = (KbeeBillboard) sessionFactory.getCurrentSession().get(KbeeBillboard.class, Long.valueOf(id.getId()));
                break;
            case "kbeeworknoteuserread":
                object = (KbeeWorkNoteUserRead) sessionFactory.getCurrentSession().get(KbeeWorkNoteUserRead.class, Long.valueOf(id.getId()));
                break;

            // Event-Notification
            //
            case "kbeeworknotenotification":
                object = (KbeeWorkNoteNotification) sessionFactory.getCurrentSession().get(KbeeWorkNoteNotification.class, Long.valueOf(id.getId()));
                break;
            case "kbeecontentpublishnotification":
                object = (KbeeContentPublishNotification) sessionFactory.getCurrentSession().get(KbeeContentPublishNotification.class, Long.valueOf(id.getId()));
                break;
            case "kbeecontentconditionnotification":
                object = (KbeeContentConditionNotification) sessionFactory.getCurrentSession().get(KbeeContentConditionNotification.class, Long.valueOf(id.getId()));
                break;
            case "worknotenotification":
                object = (KbeeWorkNoteNotification) sessionFactory.getCurrentSession().get(KbeeWorkNoteNotification.class, Long.valueOf(id.getId()));
                break;
            case "contentpublishnotification":
                object = (KbeeContentPublishNotification) sessionFactory.getCurrentSession().get(KbeeContentPublishNotification.class, Long.valueOf(id.getId()));
                break;


            
            //case "searcherhomeblock":               object = (KbeeSearcherHomeBlock) sessionFactory.getCurrentSession().get(KbeeSearcherHomeBlock.class, Long.valueOf(id.getId()));      break;
            //case "kbeesearcherhomeblock":           object = (KbeeSearcherHomeBlock) sessionFactory.getCurrentSession().get(KbeeSearcherHomeBlock.class, Long.valueOf(id.getId()));      break;


            case "kbeesite":
                object = (KbeeSite) getSessionFactory().getCurrentSession().get(KbeeSite.class, Long.valueOf(id.getId()));
                break;
            case "kbeepage":
                object = (KbeePage) getSessionFactory().getCurrentSession().get(KbeePage.class, Long.valueOf(id.getId()));
                break;

            case "kbeepagesection":
                object = (KbeePageSection) getSessionFactory().getCurrentSession().get(KbeePageSection.class, Long.valueOf(id.getId()));
                break;

                
            case "kbeearea":
                object = (KbeeArea) getSessionFactory().getCurrentSession().get(KbeeArea.class, Long.valueOf(id.getId()));
                break;
            
            case "Kbeeblocklistview":
                object = (KbeeBlockListView) getSessionFactory().getCurrentSession().get(KbeeBlockListView.class, Long.valueOf(id.getId()));
                break;
                
            case "kbeeblockmostviewedcontents":
                object = ( KbeeBlockGenericContentList) getSessionFactory().getCurrentSession().get( KbeeBlockGenericContentList.class, Long.valueOf(id.getId()));
                break;
                
                
            case "Kbeeblock":
                object = (KbeeBlock) getSessionFactory().getCurrentSession().get(KbeeBlock.class, Long.valueOf(id.getId()));
                break;
            
            case "kbeeviewbklink":
                object = getSessionFactory().getCurrentSession().get(com.novamens.kbee.portal.model.KbeeViewBKLink.class, Long.valueOf(id.getId()));
                break;
            case "kbeeviewdetailcontent":
                object = getSessionFactory().getCurrentSession().get(KbeeViewDetailContent.class, Long.valueOf(id.getId()));
                break;
                
            case "kbeeviewbkiql":
                object = getSessionFactory().getCurrentSession().get(com.novamens.kbee.portal.model.KbeeViewBKIQL.class, Long.valueOf(id.getId()));
                break;


            case "kbeeuserlistclassification":
                object = (KbeeUserListClassification) getSessionFactory().getCurrentSession().get(KbeeUserListClassification.class, Long.valueOf(id.getId()));
                break;
            case "kbeeuserlist":
                object = (KbeeUserList) getSessionFactory().getCurrentSession().get(KbeeUserList.class, Long.valueOf(id.getId()));
                break;
            case "kbeeuserlistitem":
                object = (KbeeUserListItem) getSessionFactory().getCurrentSession().get(KbeeUserListItem.class, Long.valueOf(id.getId()));
                break;

            case "kbeelaunchergroup":
                object = (KbeeLauncherGroup) getSessionFactory().getCurrentSession().get(KbeeLauncherGroup.class, Long.valueOf(id.getId()));
                break;

            case "kbeesupportticket":
                object = (KbeeSupportTicket) getSessionFactory().getCurrentSession().get(KbeeSupportTicket.class, Long.valueOf(id.getId()));
                break;

            case "kbeeuser":
                object = (KbeeUser) getSessionFactory().getCurrentSession().get(KbeeUser.class, Long.valueOf(id.getId()));
                break;
                

            case "kbeecontent":
                object = (KbeeContent) getSessionFactory().getCurrentSession().get(KbeeContent.class, Long.valueOf(id.getId()));
                break;
                
            case "kbeecontentproxy":
                object = (KbeeContentProxy) getSessionFactory().getCurrentSession().get(KbeeContentProxy.class, Long.valueOf(id.getId()));
                break;


            case "kbeevalueset":
                object = (KbeeValueSet) getSessionFactory().getCurrentSession().get(KbeeValueSet.class, Long.valueOf(id.getId()));
                break;

            case "kbeeentityset":
                object = (KbeeEntitySet) getSessionFactory().getCurrentSession().get(KbeeEntitySet.class, Long.valueOf(id.getId()));
                break;
                

            case "kbeesecuredset":
                object = (KbeeSecuredSet) getSessionFactory().getCurrentSession().get(KbeeSecuredSet.class, Long.valueOf(id.getId()));
                break;


            case "kbeecontenttemplate":
                object = (KbeeContentTemplate) getSessionFactory().getCurrentSession().get(KbeeContentTemplate.class, Long.valueOf(id.getId()));
                break;

                
                
                
                
                
            default: {

                logger.error("--------------------------------------------------------------------------------------");
                logger.error(getSessionUserName() + ": " + Thread.currentThread().getStackTrace()[1].getMethodName() + " " + "findObjectById " + id.toString() + " is not mapped");
                logger.error("--------------------------------------------------------------------------------------");

                throw new ContentMgmtException("KbeeContentDao -> findObjectById( " + id.toString() + ") is not in case.");
            }
        }
        ;

        return object;

    }

    @Override
    @SuppressWarnings("unchecked")
    public Content findWorkspaceCopyContentByOId(Serializable oid) {
        Content result = null;
        String hql = "FROM KbeeContent Where OId = " + oid.toString() + " and workspace is not null order by lastmodifieddate";

        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        List<Content> results = (List<Content>) query.list();
        if (results.size() > 0)
            return results.get(0);
        return null;

    }

    /**
     * Returns the head version
     * If there is no head version but there is a version in some user´s workspace
     * returns that version.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Content findContentByOId(Serializable id) {
        Content result = null;
        String hql = "FROM KbeeContent Where OId = " + id.toString() + " order by lastmodifieddate";

        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        //query.setCacheable(true);
        //query.setCacheRegion("content");
        List<Content> results = (List<Content>) query.list();

        if (results.size() == 1)
            return results.get(0);

        for (Content content : results) {
            if (content.isHeadVersion()) {
                result = content;
                break;
            } else if (content.getWorkspace() != null) {
                if (results.size() == 1) {
                    result = content;
                    break;
                }
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Content findLastVersion(Serializable oid) {
        Content result = null;
        String hql = "FROM KbeeContent Where OId = " + oid.toString() + " order by lastmodifieddate desc";

        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List<Content> results = (List<Content>) query.list();

        if (results.size() == 1)
            return results.get(0);

        int lastversion = -1;
        for (Content content : results) {
            if (content.getVersion()>lastversion) {
            	lastversion = content.getVersion();
                result = content;
            }
        }
        return result;
    }



    /**
     *
     */
    @SuppressWarnings("unchecked")
    @Override
    public Content findContentById(Serializable id) {
        Content result = null;
        String hql = "FROM KbeeContent C Where C.id = " + id;
        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        //query.setCacheable(true);
        //query.setCacheRegion("query");
        List<Content> results = (List<Content>) query.list();
        if (!results.isEmpty()) {
            result = results.get(0);
        }
        return result;
    }


    @Override
    public Content findContentByExternalId(String sourcename, String id) {
        Content result = null;

        Source source = findSourceByName(sourcename);

        if (source == null) {
            return null;
        }

        result = findContentByExternalId(source, id);

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Content findContentByExternalId(Source source, String id) {
        Content result = null;

        if (id == null) {
            return null;
        }

        String hql = "FROM KbeeContent Where source.id=" + ((KbeeSource) source).getId() + " and externalId = '" + id.toLowerCase() + "' order by lastmodifieddate desc";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);

        query.setCacheable(true);
        query.setCacheRegion("query");

        List<Content> results = (List<Content>) query.list();
        for (Content content : results) {
            if (content.isHeadVersion() || content.getWorkspace() != null) {
                result = content;
                break;
            }
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Source findSourceByName(String name) {
        Source result = null;
        if (name == null) return null;
        String hql = "FROM KbeeSource Where Name = '" + name.toLowerCase() + "' and domain.id=" + getDomain().getId();
        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        List<Source> results = (List<Source>) query.list();
        if (!results.isEmpty()) {
            result = results.get(0);
        }
        return result;
    }

    @Override
    public Source findSourceById(Serializable id) {
        return (KbeeSource) sessionFactory.getCurrentSession().get(KbeeSource.class, id);
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Content> getContent(Class<? extends Content> clazz, Serializable domainid) {

        if (clazz.equals(IDoc.class)) return (List<IDoc>) getResultSet("FROM KbeeIDoc U WHERE U.domain.id=" + domainid.toString() + " order by U.title");
        if (clazz.equals(KbeeIDoc.class)) return (List<KbeeIDoc>) getResultSet("FROM KbeeIDoc U WHERE U.domain.id=" + domainid.toString() + " order by U.title");

        if (clazz.equals(TreeIDoc.class)) return (List<TreeIDoc>) getResultSet("FROM KbeeTreeIDoc U WHERE U.domain.id=" + domainid.toString() + " order by U.title");
        if (clazz.equals(KbeeTreeIDoc.class)) return (List<KbeeTreeIDoc>) getResultSet("FROM KbeeTreeIDoc U WHERE U.domain.id=" + domainid.toString() + " order by U.title");

        if (clazz.equals(OrganizationalText.class)) return (List<OrganizationalText>) getResultSet("FROM KbeeOrganizationalText U WHERE U.domain.id=" + domainid.toString());
        if (clazz.equals(KbeeOrganizationalText.class)) return (List<KbeeOrganizationalText>) getResultSet("FROM KbeeOrganizationalText U WHERE U.domain.id=" + domainid.toString());

        if (clazz.equals(Comment.class)) return (List<Comment>) getResultSet("FROM KbeeComment U WHERE U.domain.id=" + domainid.toString());
        if (clazz.equals(KbeeComment.class)) return (List<KbeeComment>) getResultSet("FROM KbeeComment U WHERE U.domain.id=" + domainid.toString());

        if (clazz.equals(Question.class)) return (List<Question>) getResultSet("FROM KbeeQuestion U WHERE U.domain.id=" + domainid.toString() + " order by U.title");
        if (clazz.equals(KbeeQuestion.class)) return (List<KbeeQuestion>) getResultSet("FROM KbeeQuestion U WHERE U.domain.id=" + domainid.toString() + " order by U.title");

        if (clazz.equals(Answer.class)) return (List<Answer>) getResultSet("FROM KbeeAnswer U WHERE U.domain.id=" + domainid.toString() + " order by U.title");
        if (clazz.equals(KbeeAnswer.class)) return (List<KbeeAnswer>) getResultSet("FROM KbeeAnswer U WHERE U.domain.id=" + domainid.toString() + " order by U.title");

        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<ReportSubscription> findReportSubscriptionsForReportSchedule(Serializable reportScheduleId) {
        if (reportScheduleId == null)
            return new ArrayList<>();

        String hql = "FROM KbeeReportSubscription U where U.reportExportScheduleId = '" + reportScheduleId + "' and enabled = true";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        List results = query.list();
        return (List<ReportSubscription>) results;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Content> getContent(ContentTemplate ct, Domain domain) {

        if (ct == null || domain == null)
            return null;

        String str = ct.getContentClass().getId().toLowerCase();
        String domainid = domain.getId().toString();

        if (str.equals("kbeeidoc")) {
            return (List<IDoc>) getResultSet("FROM KbeeIDoc U WHERE U.domain.id= '" + domainid.toString() + "'  AND U.contenttemplate.id='" + ct.getId().toString() + "'");
        }
        if (str.equals("kbeeorganizationaltext")) {
            return (List<OrganizationalText>) getResultSet("FROM KbeeOrganizationalText U WHERE U.domain.id='" + domainid.toString() + "'  AND U.contenttemplate.id='" + ct.getId().toString() + "'");
        }
        if (str.equals("kbeetreeidoc")) {
            return (List<com.novamens.content.document.TreeIDoc>) getResultSet("FROM KbeeTreeIDoc U WHERE U.domain.id= '" + domainid.toString() + "'  AND U.contenttemplate.id='" + ct.getId().toString() + "'");
        }
        if (str.equals("kbeequeestion")) {
            return (List<Question>) getResultSet("FROM KbeeQuestion U WHERE U.domain.id= '" + domainid.toString() + "'  AND U.contenttemplate.id='" + ct.getId().toString() + "'");
        }
        if (str.equals("kbeeanswer")) {
            return (List<Answer>) getResultSet("FROM KbeeAnswer U WHERE U.domain.id= '" + domainid.toString() + "'  AND U.contenttemplate.id='" + ct.getId().toString() + "'");
        }
        if (str.equals("kbeecomment")) {
            return (List<Comment>) getResultSet("FROM KbeeComment U WHERE U.domain.id= '" + domainid.toString() + "'  AND U.contenttemplate.id='" + ct.getId().toString() + "'");
        }
        return null;
    }


    @Override
    public Object reload(Object object) {
        try {
    		if (object instanceof HibernateProxy) {
    			HibernateProxy proxy = (HibernateProxy)object;
    			LazyInitializer initializer = proxy.getHibernateLazyInitializer();
    	        if (initializer.isUninitialized()) {
    	        	
    	        	if (object instanceof HibernateProxy) {
    	        		object = Hibernate.unproxy(object);
    	        		return object;
    	        	}
    	        	
    	        	Long id= (Long) initializer.getIdentifier();
    	            String classname = proxy.getClass().getName();
    				int i = classname.indexOf("_");
    				if (i>0) classname = classname.substring(0, i);
    				i = classname.indexOf("$");
    				if (i>0) classname = classname.substring(0, i);
    				Class<?> clazz = Class.forName(classname);
    	            object = sessionFactory.getCurrentSession().find(clazz, id);
    	            return object;
    	        }
    		}
            Class<?> clazz = Hibernate.getClass(object);
            Serializable id = object instanceof Content ? ((Content) object).getId() : ((com.novamens.security.Identifiable) object).getId();
            object = sessionFactory.getCurrentSession().load(clazz, id);
            return object;
        } catch (Exception e) {
            logger.error(e);
            return object;
        }
    }
    
    @Override
    public Object unproxy(Object object) {
        try {
    		if (object instanceof HibernateProxy) {
    			HibernateProxy proxy = (HibernateProxy)object;
    			LazyInitializer initializer = proxy.getHibernateLazyInitializer();
    			object = initializer.getImplementation();
    		}
            return object;
        } 
        catch (Exception e) {
            logger.error(e);
            return object;
        }
    }


    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @Override
    public void refresh(Object object) {
//		sessionFactory.getCurrentSession().evict(object);
        sessionFactory.getCurrentSession().refresh(object);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     * Content Save & Delete
     * Transactions are managed by the calling service
     */
    @Override
    public void save(Content content) {
        this.save(content, true);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */

    public void save(Content content, boolean defaults) {
        ContentClass contentClass = findContentClassByContent(content);
        Assert.notNull(contentClass, "Class not supported " + content.getClass().getName() + " ( findContentClassByContent returned " + contentClass + ")");
        if (defaults)
            setDefaults(content);
        ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(content));
        if (content instanceof ResourceContainer) {
            for (Resource resource : ((ResourceContainer) content).getResources()) {
                if (resource instanceof ExternalResource) {
                    save((ExternalResource) resource);
                }
            }
        }
        sessionFactory.getCurrentSession().save(content);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void deleteAllVersions(Content content) throws ContentMgmtException {
        ContentClass contentClass = findContentClassByContent(content);
        Assert.notNull(contentClass, "Class not supported " + content.getClass().getName());
        List<Content> list = (List<Content>) getResultSet("FROM KbeeContent K WHERE K.oid=" + content.getOId().toString() + " order by K.version desc");
        for (Content co : list) {
            sessionFactory.getCurrentSession().delete(co);
            logger.info("deleting " + co.getTitle() + " . version: " + String.valueOf(co.getVersion()) + " .  oid: " + co.getOId().toString() + " . id: " + co.getId().toString());
        }
    }

    @Override
    public void delete(Content content) throws ContentMgmtException {
        ContentClass contentClass = findContentClassByContent(content);
        Assert.notNull(contentClass, "Class not supported " + content.getClass().getName());
        sessionFactory.getCurrentSession().delete(content);
    }

    public void delete(Library library) throws ContentMgmtException {
        sessionFactory.getCurrentSession().delete(library);
    }

    public void delete(Source source) throws ContentMgmtException {
        sessionFactory.getCurrentSession().delete(source);
    }

    // Content.IDoc -------------------------------------------------------------------
    //
    //
    public void save(IDoc idoc) {
        setDefaults(idoc);
        for (KBFile f : idoc.getFiles()) {
            setDefaults(f);
        }
        sessionFactory.getCurrentSession().save(idoc);
    }

    public void save(TreeIDoc tree_idoc) {
        setDefaults(tree_idoc);
        // KBFIlees

        sessionFactory.getCurrentSession().save(tree_idoc);
    }

    // Content.Question  -------------------------------------------------------------------
    //
    //
    public void save(Question question) throws ContentMgmtException {
        setDefaults(question);
        ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(question));
        if (question.getTitle() != null && question.getTitle().length() > 256)
            question.setTitle(question.getTitle().substring(0, 256));
        sessionFactory.getCurrentSession().save(question);
    }

    // Content.OrgChart  -------------------------------------------------------------------
    //
    public void save(KbeeOrgChart orgchart) throws ContentMgmtException {
        setDefaults(orgchart);
        sessionFactory.getCurrentSession().save(orgchart);
    }

    @Override
    public void save(com.novamens.event.LogEvent objEvent) {
        sessionFactory.getCurrentSession().save(objEvent);
    }

    // Content.Communication -------------------------------------------------------------------
    //
    @Override
    public OrganizationalText findOrganizationalTextById(Long id) {
        return (KbeeOrganizationalText) sessionFactory.getCurrentSession().get(KbeeOrganizationalText.class, id);
    }

    @Override
    public TreeIDoc findTreeIDocById(Long id) {
        return (TreeIDoc) sessionFactory.getCurrentSession().get(KbeeTreeIDoc.class, id);
    }

    @Override
    public TreeFile findTreeFileById(Long id) {
        return (TreeFile) sessionFactory.getCurrentSession().get(KbeeTreeFile.class, id);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @Override
    public ContentProperties getContentProperties(Content content) {
        ContentProperties cp = (ContentProperties) sessionFactory.getCurrentSession().get(KbeeContentProperties.class, Long.valueOf(content.getId().toString()));
        if (cp == null) {
            cp = new KbeeContentProperties();
            cp.setContent(content);
        }
        return cp;
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @Override
    public void save(ContentProperties contentProperties) throws ContentMgmtException {
        contentProperties.setLastModifiedOffsetDateTime(OffsetDateTime.now());
        if (contentProperties.getLastModifiedUser() == null)
            contentProperties.setLastModifiedUser(getSessionUser());
        sessionFactory.getCurrentSession().saveOrUpdate(contentProperties);
    }


    @Override
    public void delete(ContentProperties contentProperties) throws ContentMgmtException {
        sessionFactory.getCurrentSession().delete(contentProperties);
    }

    /**
     * (1)
     * Para Contenidos. Devuelve el audit de todas las versiones anteriores ademas de la actual.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<LogEvent> getAuditTrail(Content content) {
        try {
            return (List<LogEvent>) getResultSet("FROM AbstractLogEvent E WHERE E.contentOId = '" + String.valueOf(content.getOId()) + "' order by E.time desc", 15000);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LogEvent> getAddResourcesAuditTrail(Content content) {
        try {
            return (List<LogEvent>) getResultSet("FROM UpdateAddResourceEvent E WHERE E.contentOId = '" + String.valueOf(content.getOId()) + "' order by E.time ");
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<LogEvent> getAddResourcesAuditTrail(Activity activity) {
        try {
            return (List<LogEvent>) getResultSet("FROM UpdateAddResourceEvent E WHERE E.event_activity_id = " + String.valueOf(activity.getId()) + "order by E.time ");
        } 
        catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<LogEvent> getAuditTrail(Activity activity, EForm form) {
        try {
            return (List<LogEvent>) getResultSet("FROM UpdateFormEvent E WHERE " + 
           		"E.event_activity_id = " + String.valueOf(activity.getId())	+ " and " + 
           		"E.formId = " + String.valueOf(((KbeeEForm)form).getId())	+ 
           		"order by E.time desc ");
        } 
        catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LogEvent> getAuditTrail(User user) {
        try {
            return (List<LogEvent>) getResultSet("FROM AbstractLogEvent E WHERE E.objectId = '" + (new ObjectId(user)).toString() + "' and E.class!='LoginEvent' order by E.time desc", 20000);
        } 
        catch (Exception e) {
            logger.error(e);
            return null;
        }
    }


    @Override
    public List<LogEvent> getAuditTrail(Site site) {
        try {
            return (List<LogEvent>) getResultSet("FROM AbstractLogEvent E WHERE E.objectId = '" + (new ObjectId(site)).toString() + "' and E.class!='LoginEvent' order by E.time desc", 20000);
        } 
        catch (Exception e) {
            logger.error(e);
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public List<LogEvent> getAuditTrail(ModelObject object) {
        try {
            return (List<LogEvent>) getResultSet("FROM AbstractLogEvent E WHERE E.objectId = '" + (new ObjectId(object)).toString() + "' order by E.time desc", 20000);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<LogEvent> getLabelsAuditTrail() {
        try {
            User user = ServiceLocator.getService(UserService.class).findRootUser(getDomain());
            List<LogEvent> list = (List<LogEvent>) getResultSet("FROM DataSetValueEvent E WHERE E.user.id = '" + user.getId().toString() + "'and E.kbeeclass='User Label' order by E.time desc", 20000);
            return list;
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<LogEvent> getAuditTrailDataSet(DataSet object) {
        try {
            List<LogEvent> list = null;

            if (object.getDataSetType() == DataSetType.STRING)
                list = (List<LogEvent>) getResultSet("FROM ModelEvent E WHERE E.objectId = '" + "kbeevalueset#" + object.getId().toString() + "' order by E.time desc");
            else if (object.getDataSetType() == DataSetType.ENTITY)
                list = (List<LogEvent>) getResultSet("FROM ModelEvent E WHERE E.objectId = '" + "kbeeentityset#" + object.getId().toString() + "' order by E.time desc");
            else if (object.getDataSetType() == DataSetType.USER)
                list = (List<LogEvent>) getResultSet("FROM ModelEvent E WHERE E.objectId = '" + "kbeeuserset#" + object.getId().toString() + "' order by E.time desc");
            else if (object.getDataSetType() == DataSetType.EXTERNAL)
                list = (List<LogEvent>) getResultSet("FROM ModelEvent E WHERE E.objectId = '" + "kbeeexternal#" + object.getId().toString() + "' order by E.time desc");
            else list = (List<LogEvent>) getResultSet("FROM ModelEvent E WHERE E.objectId = '" + (new ObjectId((KbeeDataSet) object)).toString() + "' order by E.time desc");

            return list;

        } catch (java.lang.Exception e) {
            logger.error(e, getSessionUserName());
            return null;
        }
    }

    /**
     * (2) Solo devuelve el de la ultima version del objeto.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<LogEvent> getAuditTrail(Object object) {

        if (object instanceof DataSet)
            return getAuditTrailDataSet((DataSet) object);

        try {
            List<LogEvent> list = (List<LogEvent>) getResultSet("FROM AbstractLogEvent E WHERE E.objectId = '" + (new ObjectId(object)).toString() + "' and E.class!='LoginEvent' and E.class!='LogoutEvent' order by E.time desc", 10000);
            return list;
        } catch (java.lang.NullPointerException e) {
            logger.error(e);
            return null;
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<LogEvent> getActivity(User user) {
        try {
            List<LogEvent> list = (List<LogEvent>) getResultSet("FROM AbstractLogEvent E WHERE E.user.id = '" + user.getId().toString() + "' order by E.time desc", 20000);
            return list;
        } 
        catch (java.lang.NullPointerException e) {
            logger.error(e);
            return null;
        }
    }

    private void save(KBFile file) throws ContentMgmtException {
        setDefaults(file);
        try {
            sessionFactory.getCurrentSession().save(file);
        } 
        catch (Exception e) {
            logger.error(e, getSessionUserName());
            throw new ContentMgmtException(e);
        }
    }
    
    private void save(ResourceFolder folder) throws ContentMgmtException {
        setDefaults(folder);
        try {
            sessionFactory.getCurrentSession().save(folder);
        } 
        catch (Exception e) {
            logger.error(e, getSessionUserName());
            throw new ContentMgmtException(e);
        }
    }

    private void save(TreeFileResource resource) throws ContentMgmtException {
        setDefaults(resource);
        sessionFactory.getCurrentSession().save(resource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UserProfile> findUserProfileByDomain(Domain domain) {
        String hql = "FROM KbeeUserProfile U where U.domain.id=" + domain.getId().toString();
        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);

        //query.setCacheable(true);
        //query.setCacheRegion("query");

        List<?> results = query.list();
        return (List<UserProfile>) results;
    }


    @SuppressWarnings("unchecked")
    public List<UserProfile> getUserProfiles() {
        String hql = "FROM KbeeUserProfile U";
        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        List<?> results = query.list();
        return (List<UserProfile>) results;
    }

    // Totals -------------------------------------------------------------------------------------------------------------

    /***
     * Metrics Cache has 2 hours ttl.
     * ehcach.xml
     * <expiry>
     <ttl unit="seconds">7200</ttl>
     </expiry>

     total users (including deleted)
     */
    @Override
    public long getTotalUsers() {
        String hql = "select count(*) FROM KbeeUser";
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    
	@Override
	public long getTotalCountTasks(User user) {
        String hql = "select count(*) FROM KbeeContent C where C.workspace=" + String.valueOf(user.getId())	+ " and C.state="+String.valueOf(ObjectState.ENABLED.getId());
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();

	}
    


	
	

    
    @Override
    public long getTotalUsers(Domain domain) {
        String hql = "select count(*) FROM KbeeUserProfile U where U.domain.id=" + domain.getId().toString();
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    @Override
    public long getTotalContents(ContentTemplate obj) {
        String hql = "select count(*) FROM KbeeContent U where U.contenttemplate.id=" + obj.getId().toString();
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    @Override
    public long getTotalBillableUsers(Domain domain) {
        try {
            // AT patch 2020-11-01
			String hql = "select count(*) FROM KbeeUserProfile U where U.domain.id=:domainId and U.user.active = true and U.user.state = " + String.valueOf(ObjectState.ENABLED.getId()) + " and U.user.isBillable = true";
            @SuppressWarnings({"rawtypes", "unchecked"})
            Map<String, Object> parameters = new HashMap();
            parameters.put("domainId", domain.getId());
            List<?> resultSet = getResultSet(hql, parameters);
            return ((Number) resultSet.get(0)).longValue();
        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            throw (e);
        }
    }

    /**
     * 4hs cache
     */
    @Override
    public long getTotalContents(Domain domain) {
        String hql = "select count(*) FROM KbeeContent U where U.domain.id=" + domain.getId().toString();
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * ENABLED MEMBERS
     */
    @Override
    public long getTotalMembers(DataSet dataset) {
        String hql = "select count(*) FROM KbeeDataSetMember DM where DM.dataset.id=" + dataset.getId().toString() + " and (DM.state=" + String.valueOf(ObjectState.ENABLED.getId()) + " or DM.state=" + String.valueOf(ObjectState.ARCHIVED.getId()) + ") ";
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * ENABLED MEMBERS
     */
    @Override
    public long getAllStatesTotalMembers(DataSet dataset) {
        String hql = "select count(*) FROM KbeeDataSetMember DM where DM.dataset.id=" + dataset.getId().toString();
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * ENABLED DATASETS
     */
    @Override
    public long getTotalDatasets(Domain domain) {
        String hql = "select count(*) FROM KbeeDataSet DA where DA.domain.id=" + domain.getId().toString() + " and (DA.state=" + String.valueOf(ObjectState.ENABLED.getId()) + " or DA.state=" + String.valueOf(ObjectState.ARCHIVED.getId()) + ") ";
        
        
        
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * ENABLED CLASSIFIERS
     */
    @Override
    public long getTotalClassifiers(Domain domain) {
        String hql = "select count(*) FROM KbeeClassifier DA where DA.domain.id=" + domain.getId().toString() + " and (DA.state=" + String.valueOf(ObjectState.ENABLED.getId()) + " or DA.state=" + String.valueOf(ObjectState.ARCHIVED.getId()) + ") ";
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * ENABLED ATRIBUTES
     */
    @Override
    public long getTotalAttributes(Domain domain) {
        String hql = "select count(*) FROM KbeeAttribute DA where DA.domain.id=" + domain.getId().toString() + " and (DA.state=" + String.valueOf(ObjectState.ENABLED.getId()) + " or DA.state=" + String.valueOf(ObjectState.ARCHIVED.getId()) + ") ";
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * ENABLED CONTENT TEMPLATES
     */
    @Override
    public long getTotalContentTemplates(Domain domain) {
        String hql = "select count(*) FROM KbeeContentTemplate DA where DA.domain.id=" + domain.getId().toString() + " and (DA.state=" + String.valueOf(ObjectState.ENABLED.getId()) + " or DA.state=" + String.valueOf(ObjectState.ARCHIVED.getId()) + ") ";
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * 4hs cache
     * includes all states and all versions
     */
    @Override
    public long getTotalExternalContents(Domain domain) {
        String hql = "select count(*) FROM KbeeContent U where U.externalId is not null and U.domain.id=" + domain.getId().toString();
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * 4hs cache
     * <p>
     * Only head
     */
    @Override
    public long getTotalContents(Domain domain, String Library_key) {

        String hql;

        if (Library_key == Library.EXTERNAL)
            hql = "select count(*) FROM KbeeContent U where U.ishead=true and U.externalId is not null and U.domain.id=" + domain.getId().toString();

        else if (Library_key == Library.ALL)
            hql = "select count(*) FROM KbeeContent U where U.ishead=true and U.domain.id=" + domain.getId().toString();
        else
            throw new KbeeRuntimeException(" getTotalContents(Domain domain, Library Library) -> Library " + Library_key + " not supported.");

        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * 4hs cache
     * includes all versions
     */
    @Override
    public long getTotalContents() {
        String hql = "select count(*) FROM KbeeContent";
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }



    @Override
    public long getTotalNotEncryptedResources(Domain domain) {
        String hql = "select count(*) FROM KBFileImpl K where K.isEncrypted=false and K.exists_in_object_storage=true and K.domain.id=" + domain.getId().toString();
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }

    
    
    
    
    
    
    
    
    
    /**
     * 4hs cache
     * includes all versions all states
     */
    @Override
    public long getTotalExternalContents() {
        String hql = "select count(*) FROM KbeeContent where externalId is not null";
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * 4hs cache
     */
    @Override
    public long getTotalExternalArchiveContents() {
        String hql = "select count(*) FROM KbeeContent U where  U.ishead=true and  U.externalId is not null and  U.state=" + String.valueOf(ObjectState.ARCHIVED.getId());
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * 4hs cache
     */
    @Override
    public long getTotalExternalLibraryContents() {
        String hql = "select count(*) FROM KbeeContent where externalId is not null and ishead=true and state=" + String.valueOf(ObjectState.ENABLED.getId());
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * 4hs cache
     */
    @Override
    public long getTotalExternalRecycleContents() {
        String hql = "select count(*) FROM KbeeContent where externalId is not null and state=" + String.valueOf(ObjectState.DELETED.getId());
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * 4hs cache
     */
    @Override
    public long getTotalExternalArchiveContents(Domain domain) {
        String hql = "select count(*) FROM KbeeContent U where U.domain.id=" + String.valueOf(domain.getId()) + " and U.externalId is not null and U.state=" + String.valueOf(ObjectState.ARCHIVED.getId());
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * 4hs cache
     */
    @Override
    public long getTotalExternalLibraryContents(Domain domain) {
        String hql = "select count(*) FROM KbeeContent U where U.domain.id=" + String.valueOf(domain.getId()) + " and (U.externalId is not null) and U.ishead=true and  U.state=" + String.valueOf(ObjectState.ENABLED.getId());
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    /**
     * 4hs cache
     */
    @Override
    public long getTotalExternalRecycleContents(Domain domain) {
        String hql = "select count(*) FROM KbeeContent U where U.domain.id=" + String.valueOf(domain.getId()) + " and U.externalId is not null and U.state=" + String.valueOf(ObjectState.DELETED.getId());
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }

    @Override
    public void save(ReportSubscription userReportSubscription) throws ContentMgmtException {
        sessionFactory.getCurrentSession().save(userReportSubscription);
    }


    @Override
    public long getTotalResources() {
        String hql = "select count(*) FROM AbstractResource";
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }

    @Override
    public long getTotalResources(Domain domain) {
        String hql = "select count(*) FROM AbstractResource U " + (domain != null ? ("where U.domain.id=" + domain.getId().toString()) : "");
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }

    @Override
    public long getTotalResources(KBFSStorageType type) {
        String hql = "select count(*) FROM KBFileImpl where storageType=" + String.valueOf(type.getId());
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();

    }


    @Override
    public long getTotalResources(KBFSStorageType type, int shard) {
        String hql = "select count(*) FROM KBFileImpl where storageType=" + String.valueOf(type.getId()) + " AND shard=" + String.valueOf(shard);
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }

    /**
     * HQL
     */
    @Override
    public long getTotalResources(Domain domain, KBFSStorageType type) {
        String hql = "select count(*) FROM KBFileImpl U " + (domain != null ? ("where U.domain.id=" + domain.getId().toString() + " and") : " where ") + " U.storageType=" + String.valueOf(type.getId());
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
    }


    public long getTotalEncryptedResources() {

        String sql = null;

        long start = System.currentTimeMillis();

        try {
            sql = "select count(*) from kfile where isencrypted=true";

            String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);

            if (database == null)
                return 0;

            database = database.trim();

            if (database.contains("oracle")) {
                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
                query.setCacheable(false);
                java.math.BigDecimal res = (java.math.BigDecimal) query.uniqueResult();
                logger.debug(sql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
                return (res != null ? res.longValue() : (long) 0);

            } else {
                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
                query.setCacheable(false);
                java.math.BigInteger res = (java.math.BigInteger) query.uniqueResult();
                return (res != null ? res.longValue() : (long) 0);
            }
        } catch (Exception e) {
            logger.error(e, (sql != null ? sql : ""));
            return (long) -1;
        }
    }


    /**
     * SQL
     */
    @Override
    public long getTotalHardDisk(Domain domain, KBFSStorageType type) {

        String sql = null;

        long start = System.currentTimeMillis();

        try {
            sql = "select sum(kfsize) from kfile, kresource where  id=resource_id and domain_id=" + domain.getId().toString() + " and storagemode=" + String.valueOf(type.getId());

            String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);

            if (database == null)
                return 0;


            database = database.trim();

            if (database.contains("oracle")) {
                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
                query.setCacheable(false);
                //query.setCacheRegion("metrics"); cache gives error IllegalState (??)
                java.math.BigDecimal res = (java.math.BigDecimal) query.uniqueResult();
                logger.debug(sql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
                return (res != null ? res.longValue() : (long) 0);

            } else {
                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
                query.setCacheable(false);
                //query.setCacheable(true);
                //query.setCacheRegion("metrics");
                java.math.BigInteger res = (java.math.BigInteger) query.uniqueResult();
                return (res != null ? res.longValue() : (long) 0);
            }
        } catch (Exception e) {
            // select sum(kfsize) from kfile, kresource where  id=resource_id and domain_id=250 and storagemode=1
            logger.error(e, (sql != null ? sql : ""));
            return (long) 0;
        }
    }

    /***
     * SQL
     */
    @Override
    public long getTotalHardDisk(Domain domain, KBFSStorageType type, int shard) {

        String sql = null;

        long start = System.currentTimeMillis();

        try {
            sql = "select sum(kfsize) from kfile, kresource where  id=resource_id and domain_id=" + domain.getId().toString() + " and shard=" + String.valueOf(shard) + " and storagemode=" + String.valueOf(type.getId());

            String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);

            if (database == null)
                return 0;

            logger.debug(sql);

            database = database.trim();

            if (database.contains("oracle")) {

                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
                //query.setCacheable(true);
                //query.setCacheRegion("metrics");
                // XX
                java.math.BigDecimal res = (java.math.BigDecimal) query.uniqueResult();
                logger.debug(sql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
                return (res != null ? res.longValue() : (long) 0);

            } else {
                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);

                query.setCacheable(false);
                //query.setCacheable(true);
                //query.setCacheRegion("metrics");
                java.math.BigInteger res = (java.math.BigInteger) query.uniqueResult();
                logger.debug(sql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
                return (res != null ? res.longValue() : (long) 0);
            }
        } catch (Exception e) {
            logger.error(e, (sql != null ? sql : ""));
            return (long) 0;
        }
    }


    /**
     * SQL
     */

    @Override
    public long getTotalHardDisk(KBFSStorageType type, int shard) {

        String sql = null;

        long start = System.currentTimeMillis();

        try {
            sql = "select sum(kfsize) from kfile where storagemode=" + String.valueOf(type.getId()) + " and shard=" + String.valueOf(shard);

            String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);

            if (database == null)
                return 0;

            database = database.trim();

            if (database.contains("oracle")) {

                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
                //query.setCacheable(true);
                //query.setCacheRegion("metrics");

                // XX
                java.math.BigDecimal res = (java.math.BigDecimal) query.uniqueResult();
                logger.debug(sql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
                return (res != null ? res.longValue() : (long) 0);

            } else {
                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);

                query.setCacheable(false);
                //query.setCacheable(true);
                //query.setCacheRegion("metrics");

                java.math.BigInteger res = (java.math.BigInteger) query.uniqueResult();
                logger.debug(sql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
                return (res != null ? res.longValue() : (long) 0);
            }
        } catch (Exception e) {
            logger.error(e, getSessionUserName());

            return (long) 0;
        }
    }


    /**
     * SQL
     */
    @Override
    public long getTotalHardDisk(KBFSStorageType type) {

        String sql = null;

        long start = System.currentTimeMillis();

        try {
            sql = "select sum(kfsize) from kfile where storagemode=" + String.valueOf(type.getId());

            String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);

            if (database == null)
                return 0;

            logger.debug(sql);

            database = database.trim();

            if (database.contains("oracle")) {

                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);

                query.setCacheable(true);
                query.setCacheRegion("metrics");

                java.math.BigDecimal res = (java.math.BigDecimal) query.uniqueResult();
                logger.debug(sql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
                return (res != null ? res.longValue() : (long) 0);

            } else {
                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
                query.setCacheable(false);
                // XX
                java.math.BigInteger res = (java.math.BigInteger) query.uniqueResult();
                logger.debug(sql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
                return (res != null ? res.longValue() : (long) 0);
            }
        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            return (long) 0;
        }
    }


    /**
     * SQL
     */
    @Override
    public long getTotalStoredHardDisk() {

        String sql = null;

        try {
            sql = "select sum(kfsize) from kfile where 							       storagemode=" + String.valueOf(KBFSStorageType.KBFS1.getId()) + " or " +
                    "storagemode=" + String.valueOf(KBFSStorageType.Minio.getId()) + " or " +
                    "storagemode=" + String.valueOf(KBFSStorageType.Odilon.getId()) + " or " +
                    "storagemode=" + String.valueOf(KBFSStorageType.MinioArchive.getId());

            String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);

            if (database == null)
                return 0;

            logger.debug(sql);

            database = database.trim();

            if (database.contains("oracle")) {

                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);

                query.setCacheable(true);
                query.setCacheRegion("metrics");

                java.math.BigDecimal res = (java.math.BigDecimal) query.uniqueResult();
                return (res != null ? res.longValue() : (long) 0);

            } else {
                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
                query.setCacheable(false);

                java.math.BigInteger res = (java.math.BigInteger) query.uniqueResult();
                return (res != null ? res.longValue() : (long) 0);
            }
        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            return (long) 0;
        }
    }

    /**
     * SQL
     * Counts Total storage in kbee/rpdd (does not count gateway or those that lazy not downloaded)
     */
    @Override
    public long getTotalHardDisk(Domain domain) {
        String hql = null;
        try {
            hql = "select sum(kfsize) from kfile, kresource where  id=resource_id and domain_id=" + domain.getId().toString() +

                    " and (storagemode=" + String.valueOf(KBFSStorageType.KBFS1.getId()) +
                    " or   storagemode=" + String.valueOf(KBFSStorageType.Minio.getId()) +
                    " or   storagemode=" + String.valueOf(KBFSStorageType.Odilon.getId()) +
                    " or   storagemode=" + String.valueOf(KBFSStorageType.MinioArchive.getId()) + ") ";


            String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);

            if (database == null)
                return 0;

            database = database.trim();

            logger.debug(hql);

            if (database.contains("oracle")) {

                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(hql);

                query.setCacheable(true);
                query.setCacheRegion("metrics");

                java.math.BigDecimal res = (java.math.BigDecimal) query.uniqueResult();
                return (res != null ? res.longValue() : (long) 0);

            } else {
                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(hql);
                query.setCacheable(false);

                java.math.BigInteger res = (java.math.BigInteger) query.uniqueResult();
                return (res != null ? res.longValue() : (long) 0);
            }
        } catch (RuntimeException e) {
            logger.error(e, getSessionUserName(), (hql != null ? hql : ""));
            return (long) 0;
        }
    }


    /**
     *
     */
    @Override
    public String pingAPI() {

        try {

            String strquery = findSystemParameterValueByKey("api.ping_query", default_ping_api_query);

            if (strquery.equals("no") || strquery.equals("disabled"))
                return "ok";

            String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);

            database = database.trim();

            // Oracle is not supported for the API
            if ((database == null) || database.contains("oracle"))
                return "ok";


            NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(strquery);

            java.math.BigInteger res = (java.math.BigInteger) query.uniqueResult();

            if (res != null) {
                if (res.longValue() < 5)
                    return "ok";
                else
                    return "API requests error: " + String.valueOf(res);
            } else {
                return "ok";
            }
        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            return e.getClass().getName();
        }


    }


    @Override
    public String pingDataBase() {

        String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);

        if (database == null)
            return "error: jdbc.url";

        database = database.trim();
        String sql = null;

        if (database.contains("oracle"))
            sql = "SELECT 1 FROM DUAL";
        else
            sql = "SELECT count(*) from " + getSchema() + "domain";

        try {

            logger.debug(sql);

            NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
            query.setCacheable(false);
            Object o = query.uniqueResult();
        } catch (Exception e) {
            logger.error(e, getSessionUserName(), (sql != null ? sql : ""));
        }
        return null;
    }


    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getSchema() {
        if (schema != null && schema.length() > 0)
            return schema + ".";
        return "";
    }


    @Override
    public long getDatabaseSize() {

        String hql = null;

        try {

            String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);

            database = database.trim();

            if (database == null)
                return 0;


            // Oracle
            if (database.contains("oracle")) {

                hql = "select ( select sum(bytes)/1024/1024/1024 data_size from dba_data_files ) +" +
                        " ( select nvl(sum(bytes),0)/1024/1024/1024 temp_size from dba_temp_files ) + " +
                        " ( select sum(bytes)/1024/1024/1024 redo_size from sys.v_$log ) + " +
                        " ( select sum(BLOCK_SIZE*FILE_SIZE_BLKS)/1024/1024/1024 controlfile_size from v$controlfile) \"Size in GB\" from dual;";

                logger.debug(hql);

                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(hql);
                //query.setCacheable(true);
                //query.setCacheRegion("metrics");

                java.math.BigDecimal res = (java.math.BigDecimal) query.uniqueResult();
                return res.longValue();


            } else {

                // PostgresSQL

                String arr[] = database.split("/");
                if (arr.length < 1)
                    return 0;

                String database_name = arr[arr.length - 1];

                hql = "select pg_database_size('" + database_name + "')";

                logger.debug(hql);

                NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(hql);
                query.setCacheable(false);


                java.math.BigInteger res = (java.math.BigInteger) query.uniqueResult();
                return res.longValue();

                //List<Object[]> list = (List<Object[]>)query.list();//
                //		for (Iterator<Object[]> iterator = list.iterator(); iterator.hasNext();) {
                //		    Object[] e = iterator.next();
                //		    String param1 = (String)e[0];//case object type to another by youself...
                //		}

            }
        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            return -1;
        }
    }


    @Override
    public String getDatabaseVersion() {
        String hql = "select version()";
        try {
            logger.debug(hql);
            NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(hql);
            query.setCacheable(false);
            return (String) query.uniqueResult();
        } catch (Exception e) {
            logger.error(e);
            return "N/A";
        }
    }


    @Override
    public boolean isPostgreSQL() {
        if (bpostgres != null)
            return bpostgres.booleanValue();
        String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);
        if (database != null && database.trim().toLowerCase().contains(":postgresql"))
            bpostgres = Boolean.valueOf(true);
        else
            bpostgres = Boolean.valueOf(false);
        return bpostgres.booleanValue();
    }


    @SuppressWarnings("rawtypes")
    @Override
    public List getDatabaseSettings() {

        // Sólo sirve para PostgreSQL
        //
        String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);

        if (isOracle()) {
            return new ArrayList<String>();
        }

        database = database.trim();
        String strquery = "select name, context, unit, setting, boot_val, reset_val from pg_settings order by lower(name)";
        logger.debug(strquery);

        try {
            NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(strquery);
            List list = query.list();
            return list;

        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            return new ArrayList<String>();
        }
    }

    @Override
    public List<ContentClass> getClasses() {
        List<ContentClass> classes = new ArrayList<ContentClass>();
        classes.addAll(getContentClasses(false).values());
        return classes;
    }


    @Override
    public Object findObjectById(ObjectID id) {
        ObjectId oid = new ObjectId(id.getClassName(), id.getId());
        return findObjectById(oid);
    }


    @Override
    public Content getNextVersion(Content content) {
        String hql = "FROM KbeeContent U WHERE U.oid = " + content.getOId() + " AND U.previousVersion=" + content.getId();
        logger.debug(hql);
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        @SuppressWarnings("rawtypes")
        List results = query.list();
        if (results.isEmpty()) return null;
        Content nextContent = (Content) results.get(0);
        return nextContent;
    }


    @Override
    public void sessionFlush() {
        try {
            logger.debug("SessionFlush");
            sessionFactory.getCurrentSession().flush();
            sessionFactory.getCurrentSession().clear();

        } catch (Exception e) {
            logger.error(e);
        }

    }


    @Override
    public void cleanHibernateCache() {
        try {

            this.sessionFactory.getCache().evictAll();

            this.contentclassesbyid = null;
            this.contentclassesbyname = null;

            logger.debug("Hibernate Cache evictAll.");

        } catch (Exception e) {
            logger.error(e, getSessionUserName(), "SessionController", "evict2ndLevelCache", "Error evicting 2nd level hibernate cache entities: ");
        }
    }


    @Override
    public Content findContentByClassCodeOid(String class_code, String oid) {

        Content content = findContentByOId(oid);

        if (content == null)
            return null;

        if (class_code.equals(IDoc.CLASS_CODE))
            return findContentById(IDoc.class, content.getId());

        else if (class_code.equals(TreeIDoc.CLASS_CODE))
            return findContentById(TreeIDoc.class, content.getId());

        else if (class_code.equals(OrganizationalText.CLASS_CODE))
            return findContentById(OrganizationalText.class, content.getId());

        else if (class_code.equals(OrgChart.CLASS_CODE))
            return findContentById(OrgChart.class, content.getId());

        return null;
    }


    @Override
    public long getTotalClassifications(DataSetMember member) {
        String hql = "select count(*) FROM KbeeClassification U where U.datasetmember.id=" + member.getId().toString();
        logger.debug(hql);
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        return ((Long) query.uniqueResult()).longValue();

    }


    @Override
    public long getTotalElements(DataSet dataSet) {
        String hql = "select count(*) FROM KbeeDataSetMember U where U.dataset.id=" + dataSet.getId().toString();
        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);

        query.setCacheable(true);
        query.setCacheRegion("metrics");

        return ((Long) query.uniqueResult()).longValue();
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<Content> getContents(Domain domain) {
        String hql = "from KbeeContent K where K.domain.id=" + domain.getId().toString() + " order by lower(K.title)";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        logger.debug(hql);
        List results = query.list();
        if (results == null)
            return new ArrayList<Content>();
        return
                (List<Content>) results;
    }

	public List<Content> getContents(Domain domain, ObjectState state, int maxitems) {
		
    	String hql = "from KbeeContent K where K.domain.id=" + domain.getId().toString() + " and K.state="+String.valueOf(state.getId()) +" order by K.lastModifiedDate desc";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setMaxResults(maxitems);
        logger.debug(hql);
        List results = query.list();
        if (results == null)
            return new ArrayList<Content>();
        return
                (List<Content>) results;
	}
	
	
	
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<Content> getWorkspaceContents(User user, boolean order_by_newest, int limit) {

        String hql;

        if (user != null) {
        	if (order_by_newest)
        		hql = "from KbeeContent K where K.state=" + String.valueOf(ObjectState.ENABLED.getId()) + " and  K.workspace=" + user.getId().toString() + " order by K.lastModifiedDate";
        	else
        		hql = "from KbeeContent K where K.state = "  + String.valueOf(ObjectState.ENABLED.getId())  +" and K.workspace=" + user.getId().toString() + " order by lower(K.title)";
        }
        else
            hql = "from KbeeContent K where K.state = " + String.valueOf(ObjectState.ENABLED.getId())  + " and K.workspace > 0 order by lower(K.title)";

        logger.debug(hql);

        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);

        if (limit>0) 
        	query.setMaxResults(limit);
        
        List results = query.list();

        if (results == null)
            return new ArrayList<Content>();

        return
                (List<Content>) results;
    }


    @Override
    public boolean hasEmailTemplates(Domain domain) {
        long start = System.currentTimeMillis();
        String hql = "select count(*) FROM KbeeEmailTemplate U WHERE U.domain.id =" + String.valueOf(domain.getId());
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        Long res = (Long) query.uniqueResult();
        return (res != null && res.longValue() > 0);
    }

    @Override
    public EmailTemplate findEmailTemplate(Domain domain, String language, String key) {
        if (domain == null) {
            logger.error("domain is null");
            return null;
        }
        String hql = "FROM KbeeEmailTemplate U where U.domain.id= " + domain.getId().toString() + "  and U.lang='" + language + "' and U.key= '" + key + "'";
        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        return (EmailTemplate) query.uniqueResult();
    }


    public List<EmailTemplate> getEmailTemplates(Domain domain) {
        return getEmailTemplates(domain, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<EmailTemplate> getEmailTemplates(Domain domain, String lang) {

        String lq = (lang != null ? (" and U.lang='" + lang + "'") : "");

        String hql = "FROM KbeeEmailTemplate U where U.domain.id=" + domain.getId().toString() + lq + " order by U.title";

        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        List<?> results = query.list();
        if (results == null)
            return new ArrayList<EmailTemplate>();

        return (List<EmailTemplate>) results;
    }


    @SuppressWarnings("unchecked")
    private Map<String, ContentClass> getContentClasses(boolean byid) {
        if (contentclassesbyid == null || contentclassesbyname == null) {
            synchronized (this) {
                try {
                    org.hibernate.query.Query<ContentClass> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeContentClass order by lower(name)");
                    Iterator<ContentClass> it = query.list().iterator();
                    contentclassesbyid = new HashMap<String, ContentClass>();
                    contentclassesbyname = new HashMap<String, ContentClass>();
                    while (it.hasNext()) {
                        ContentClass contentclass = it.next();
                        contentclassesbyid.put(contentclass.getId(), contentclass);
                        contentclassesbyname.put(contentclass.getName(), contentclass);
                    }
                } catch (Exception e) {
                    throw new KbeeRuntimeException("Application can not run without ContentClasses", e);
                }
            }
        }
        return (byid ? contentclassesbyid : contentclassesbyname);
    }

    private List<? extends Object> getResultSet(String hql, int limit) {

        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);

        if (limit > 0)
            query.setMaxResults(limit);

        long start = System.currentTimeMillis();
        List<?> results = query.list();
        logger.debug(hql + " " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return results;

    }

    @Override
    public List<? extends Object> getResultSet(String hql) {
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        long start = System.currentTimeMillis();
        List<?> results = query.list();
        logger.debug(String.valueOf(System.currentTimeMillis() - start) + " ms -> " + hql);
        return results;
    }

    
    
    @Override
    public List<? extends Object> getResultSet(String hql, Map<String, Object> parameters) {
    		return getResultSet(hql, parameters, 0);
    }
    
    private List<? extends Object> getResultSet(String hql, Map<String, Object> parameters, int limit) {
    	
    	org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);

    	 if (limit > 0) 
    		 query.setMaxResults(limit);
    	 
        for (Map.Entry<String, Object> stringObjectEntry : parameters.entrySet())
            query.setParameter(stringObjectEntry.getKey(), stringObjectEntry.getValue());

        long start = System.currentTimeMillis();
        List<?> results = query.list();
        logger.debug(String.valueOf(System.currentTimeMillis() - start) + " ms -> " + hql);
        return results;
    }

    private void setDefaults(Payment payment) {
      /*  if (payment.getUser() == null)
            payment.setUser(getSessionUser());*/
    }

    private void setDefaults(Content content) {
        setDefaults((com.novamens.dom.Object) content);
        if (content.getDomain() == null)
            content.setDomain(getDomain());
    }


    private void setDefaults(ModelObject object) {
        setDefaults((com.novamens.dom.Object) object);
        if (object.getDomain() == null)
            object.setDomain(getDomain());
    }


    private void setDefaults(Entity object) {
        setDefaults((com.novamens.dom.Object) object);
        if (object.getDomain() == null)
            object.setDomain(getDomain());
    }


    private void setDefaults(com.novamens.dom.Object object) {

        object.setLastModifiedOffsetDateTime(OffsetDateTime.now());

        if (object.getCreationOffsetDateTime() == null)
            object.setCreationOffsetDateTime(OffsetDateTime.now());

        if (object.getLastModifiedUser() == null)
            object.setLastModifiedUser(getSessionUser());

        if (object.getState() == null)
            object.setState(ObjectState.ENABLED);
    }


    private void setDefaults(KBFile file) {

        file.setLastModifiedOffsetDateTime(OffsetDateTime.now());

        if (file.getCreationOffsetDateTime() == null)
            file.setCreationOffsetDateTime(OffsetDateTime.now());

        if (file.getLastModifiedUser() == null)
            file.setLastModifiedUser(getSessionUser());

        if (file.getState() == null)
            file.setState(ObjectState.ENABLED);

        if (file.getStorageType() == null) {
            file.setStorageType(getDefaultKBFSStorageType());

            if (file.getBucketName() == null)
                file.setBucketName(ServiceLocator.getService(SystemPropertiesService.class).getServerIdPrefix() + file.getDomain().getName());

            if (file.getObjectName() == null)
                file.setObjectName(String.valueOf(file.getUrl()));
        }
    }


    private void save(ExternalResource resource) {
        setDefaults(resource);
        sessionFactory.getCurrentSession().save(resource);
    }


    @Transactional(propagation = Propagation.MANDATORY)
    private void save(HTMLText html) throws ContentMgmtException {

        setDefaults(html);
        List<KBFile> files = html.getFiles();
        if (files != null) {
            for (KBFile file : files)
                save(file);
        }
        sessionFactory.getCurrentSession().save(html);
    }


    /**
     * Domain kbee has id=1;
     */

    @SuppressWarnings("unchecked")
    @Override
    public List<KBFile> getDefaultUserImages() {
        
    	String hql = "FROM KBFileImpl U where U.domain.name='kbee' and U.state="+String.valueOf(ObjectState.ENABLED.getId());
    	org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        List<?> results = query.list();
        if (results == null)
            return new ArrayList<KBFile>();
        return ((List<KBFile>) results);
    }


    @Override
    public void flush() {
        if (sessionFactory.getCurrentSession() != null) {
            sessionFactory.getCurrentSession().flush();
        }
    }


    @Override
    public double findPersonEstimate(String lastname, String name, String email, Domain domain) {
        return 0;
    }


    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public void save(Library Library) throws ContentMgmtException {
        if (Library.getKey() == null)
            throw new ContentMgmtException("Key is a mandatory field");
        ((com.novamens.dom.Object) Library).setLastModifiedOffsetDateTime(OffsetDateTime.now());
        sessionFactory.getCurrentSession().save(Library);
    }

    @Override
    public List<Content> getRecycleBinContents() {
        return getRecycleBinContents(0);
    }

    public List<Content> getRecycleBinContents(int limit) {
        return getRecycleBinContents(limit, null);


    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Content> getRecycleBinContents(int limit, Domain domain) {
        String hql = "from KbeeContent K where K.state=" + String.valueOf(ObjectState.DELETED.getId()) + (domain != null ? (" and K.domain.id=" + String.valueOf(domain.getId())) : "") + " order by K.lastModifiedDate ";
        org.hibernate.query.Query<?> query;
        if (limit > 0)
            query = sessionFactory.getCurrentSession().createQuery(hql).setMaxResults(limit);
        else
            query = sessionFactory.getCurrentSession().createQuery(hql);
        List<?> results = query.list();
        if (results == null)
            return new ArrayList<Content>();
        return
                (List<Content>) results;
    }


    /**
     * System Parameters
     */

    @SuppressWarnings("unchecked")
    @Override
    public List<SystemParameter> getSystemParameters() {
        String hql = "FROM KbeeSystemParameter U order by lower(U.key)";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        List<?> results = query.list();
        if (results == null)
            return new ArrayList<SystemParameter>();
        return ((List<SystemParameter>) results);
    }


    @Override
    public void delete(SystemParameter value) throws ContentMgmtException {
        sessionFactory.getCurrentSession().delete(value);
    }

    @Override
    public void save(SystemParameter value) throws ContentMgmtException {
        sessionFactory.getCurrentSession().save(value);
    }


    @Override
    public String findSystemParameterValueByKey(String key, String default_value) {
        SystemParameter sp = findSystemParameterByKey(key);
        return sp != null ? sp.getValue() : default_value;
    }

    @Override
    public SystemParameter findSystemParameterByKey(String key) {
        if (key == null) 
        	return null;
        String hql = "FROM KbeeSystemParameter C WHERE lower(C.key) = '" + key.toLowerCase().trim() + "'";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        List<?> results = query.list();
        if (results.isEmpty()) return null;
        SystemParameter parameter = (SystemParameter) results.get(0);
        return parameter;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<User> findSupportAllUsers() {
        long start = System.currentTimeMillis();
        String hql = "FROM KbeeUser C WHERE C.name LIKE 'support1@%'  or C.name LIKE 'support2@%'";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        List<?> results = query.list();
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        if (results.isEmpty())
            return new ArrayList<User>();
        return (List<User>) results;
    }


    public long getTotalUserNotes(User user) {
        long start = System.currentTimeMillis();
        String hql = "select count(*) FROM KbeeUserNote U WHERE U.user.id =" + String.valueOf(user.getId());
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();

    }


    @SuppressWarnings("unchecked")
    public List<UserNote> getUserNotes(User user) {
        try {
            long start = System.currentTimeMillis();
            String hql = "FROM KbeeUserNote D WHERE D.user.id = " + String.valueOf(user.getId()) + " order by D.created desc";
            org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
            @SuppressWarnings("rawtypes")
            List results = query.list();
            logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
            if (results.isEmpty())
                return new ArrayList<UserNote>();
            return (List<UserNote>) results;

        } catch (HibernateException e) {
            logger.error("Probably the table does not exist");
            logger.error(e, getSessionUserName());
            throw (e);
        }

    }

    public void delete(UserNote note) {
        sessionFactory.getCurrentSession().delete(note);
    }

    public void update(UserNote note) {
        sessionFactory.getCurrentSession().update(note);
    }

    public void save(UserNote note) {
        sessionFactory.getCurrentSession().save(note);
    }

    @Override
    public void deleteAllNotes(User user) {
        List<UserNote> list = getUserNotes(user);
        if (list != null) {
            for (UserNote note : list)
                delete(note);
        }

    }

    @Override
    public Billboard findWorkNote(Long id) {
        if (id == null)
            throw new IllegalArgumentException("id is null");
        return (Billboard) sessionFactory.getCurrentSession().get(KbeeBillboard.class, id);
    }

    public void delete(Billboard note) {
        // note.setLastModifiedOffsetDateTime(OffsetDateTime.now());
        //ServiceLocator.getService(EventsService.class).fire(new BeforeDeUpdateEvent(note));
        sessionFactory.getCurrentSession().delete(note);
    }

    public void update(Billboard note) {
        note.setLastModifiedOffsetDateTime(OffsetDateTime.now());
        ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(note));
        sessionFactory.getCurrentSession().update(note);
    }

    public void save(Billboard note) {
        note.setLastModifiedOffsetDateTime(OffsetDateTime.now());
        ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(note));
        sessionFactory.getCurrentSession().save(note);
    }
    
    @SuppressWarnings("unchecked")
    public List<Billboard> getBillboards(Domain domain) {
        String hql = "FROM KbeeBillboard D WHERE D.domain.id = " + String.valueOf(domain.getId()) + " order by D.created desc";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);

        // returns the 30000 newest
        query.setMaxResults(30000);

        @SuppressWarnings("rawtypes")
        List results = query.list();
        if (results.isEmpty())
            return null;
        return (List<Billboard>) results;
    }


    
    @Override
    public void deleteAllBillboards(Domain domain) {
        List<Billboard> list = getBillboards(domain);
        for (Billboard note : list)
            delete(note);
    }

    @Override
    public int getTotalUsersRead(Billboard note) {
        try {
            String hql = "select count(*) FROM KbeeWorkNoteNotification U where U.billboard is not null and U.billboard.id=" + note.getId().toString() + " and U.dateread is not null";
            Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
            query.setCacheable(true);
            int val = ((Long) query.uniqueResult()).intValue();

            return val;
        } catch (Exception e) {
            logger.error(e);
            throw (e);
        }
    }
    
    

    @Override
    public long getDataSetMemberWithContents(Domain domain, DataSet ds) {
        try {
            String hql = "select count(*) from KbeeDataSetMember dsm where exists(select 1 from KbeeClassification clf where dsm.id = clf.datasetmember.id) and dsm.domain.id = :domainId and dsm.dataset.id = :datasetId";
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("domainId", domain.getId());
            parameters.put("datasetId", ds.getId());

            List<?> resultSet = getResultSet(hql, parameters);
            return ((Number) resultSet.get(0)).longValue();
        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            throw (e);
        }
    }

    @Override
    public void deleteContentPublishNotification(OffsetDateTime earlier_than) {
        Query<?> query;
        try {
            query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeContentPublishNotification K where K.datesent<:ot");
            query.setParameter("ot", earlier_than);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error(e, getSessionUserName());

        }
    }


    @Override
    public void deleteWorkNoteNotification(OffsetDateTime earlier_than) {
        Query<?> query;
        try {
            query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeWorkNoteNotification K where K.datesent<:ot");
            query.setParameter("ot", earlier_than);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error(e, getSessionUserName());

        }
    }


    // Notification  ------------------------------------------------------------------------
    //
    //
    @Override
    public void save(Notification noti) throws ContentMgmtException {
        try {
            sessionFactory.getCurrentSession().save(noti);
        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            throw new ContentMgmtException(e);
        }
    }

    @Override
    @Deprecated
    public void deleteNotification(User receiver, Billboard note) throws ContentMgmtException {
        Query<?> query;
        query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeWorkNoteNotification K where K.receiver.id=" + receiver.getId().toString() + " AND  K.billboard.id=" + String.valueOf(note.getId()));
        query.executeUpdate();
    }

    @Override
    @Deprecated
    public void deleteNotification(User receiver, Content note) throws ContentMgmtException {
        Query<?> query;
        query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeContentPublishNotification K where K.receiver.id=" + receiver.getId().toString() + " AND  K.content.id=" + String.valueOf(note.getId()));
        query.executeUpdate();
    }

    @Override
    @Deprecated
    public void delete(Notification noti) throws ContentMgmtException {
        sessionFactory.getCurrentSession().delete(noti);
    }

    @Override
    @Deprecated
    @SuppressWarnings("unchecked")
    public List<Notification> getNotifications(User user) throws ContentMgmtException {
        return (List<Notification>) getResultSet("FROM KbeeNotification K WHERE K.receiver.id=" + user.getId().toString() + " and K.notification_state=" + String.valueOf(NotificationState.PENDING.getId()) + " order by K.datesent desc");
    }

    public List<Notification> getAlertNotifications(User user) throws ContentMgmtException {
    	return getAlertNotifications(user, -1);
    }

    
    
	@Override
    @SuppressWarnings("unchecked")
    public List<Notification> getAlertNotifications(User user, int limit) throws ContentMgmtException {
		return (List<Notification>) getResultSet(
    			"FROM KbeeNotification K WHERE K.isalert=true and K.receiver.id=" + 
    					user.getId().toString() + " and K.notification_state<>" + 
    					String.valueOf(NotificationState.ARCHIVED.getId()) + " order by K.datesent desc",
    			limit
    			);
    }

	
	@Override
	public long getTotalCountNotifications(User user) {
        String hql = "select count(*) FROM KbeeNotification K where K.receiver.id=" + String.valueOf(user.getId())	+ " and K.notification_state<>"+String.valueOf(NotificationState.ARCHIVED.getId());
        long start = System.currentTimeMillis();
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("metrics");
        logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Long) query.uniqueResult();
	}

	

    @Override
    @SuppressWarnings("unchecked")
    public List<Notification> getBillboardNotifications(User user) throws ContentMgmtException {

        long init = System.currentTimeMillis();
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        /**
         OffsetDateTime lc=last_notification_check.get(user.getId());
         if ((lc!=null) && now.isBefore(lc.plusMinutes(1))) {
         logger.debug(String.valueOf(System.currentTimeMillis()-init) + " ms  [CACHE]");
         return last_notification_check_list.get(user.getId());
         }
         **/

        String dateRange = "and  ((K.startpub is null or K.startpub<='" + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now) + "' ) and " +
                "(K.endpub   is null or K.endpub>'" + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now) + "' ))";
        String str = "FROM KbeeWorkNoteNotification K WHERE K.isbillboard=true " + dateRange
                + " and K.receiver.id=" + user.getId().toString()
                + " and K.notification_state=" + String.valueOf(NotificationState.PENDING.getId())
                + " order by K.datesent desc";

        // return (List<Notification>) getResultSet(str);


        long start = System.currentTimeMillis();
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(str);
        query.setCacheable(true);
        // query.setCacheRegion("notification");

        List<?> results = query.list();
        logger.debug(String.valueOf(System.currentTimeMillis() - start) + " ms -> " + str);


        //last_notification_check.put(user.getId(), now);
        //last_notification_check_list.put(user.getId(), (List<Notification>) results);


        logger.debug(String.valueOf(System.currentTimeMillis() - init) + " ms  [TOTAL]");

        return (List<Notification>) results;


    }

    @Override
    public void deleteNotifications(User user) throws ContentMgmtException {
        try {
            Query<?> query;
            long start = System.currentTimeMillis();
            query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeNotification K where receiver.id=" + user.getId().toString());
            query.executeUpdate();
        } catch (Exception e) {
            throw new ContentMgmtException(e);
        }
    }

    /**
     *
     */
    @Override
    public int getTotalNotifications(User user) throws ContentMgmtException {
        String hql = "select count(*) FROM KbeeNotification U where U.receiver.id=" + user.getId().toString() + "  and U.isalert=true and U.notification_state=" + String.valueOf(NotificationState.PENDING.getId());
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        // we are not using the Cache because the database has a on delete Cascade constraint
        // that prevents us from caching these totals
        // we use a software based cache on KbeeNotificationsService instead.
        // query.setCacheable(false);

        query.setCacheable(true);
        long start = System.currentTimeMillis();
        int val = ((Long) query.uniqueResult()).intValue();
        logger.debug(String.valueOf(System.currentTimeMillis() - start) + " ms -> " + hql);

        return val;
    }


    @Override
    public int getTotalBillboardNotifications(User user) throws ContentMgmtException {
        String hql = "select count(*) FROM KbeeNotification U where U.receiver.id=" + user.getId().toString() + "  and U.isbillboard=true and U.notification_state=" + String.valueOf(NotificationState.PENDING.getId());
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        // we are not using the Cache because the database has a on delete Cascade constraint
        // that prevents us from caching these totals
        // we use a software based cache on KbeeNotificationsService instead.
        query.setCacheable(false);

        query.setCacheable(true);
        long start = System.currentTimeMillis();
        int val = ((Long) query.uniqueResult()).intValue();
        logger.debug(String.valueOf(System.currentTimeMillis() - start) + " ms -> " + hql);


        return val;
    }

    // Rules  ------------------------------------------------------------------------------
    //
    //

    @Override
    public void delete(ActionRule rule) throws ContentMgmtException {
        sessionFactory.getCurrentSession().delete(rule);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getTables() {

        String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);
        if (database == null)
            return new ArrayList<String>();

        database = database.trim();

        if (database.contains("oracle")) {
            return new ArrayList<String>();
        }

        try {
            String strquery = " SELECT tablename FROM pg_catalog.pg_tables where schemaname='" + (getSchema() != null ? getSchema() : "public") + "' order by lower(tablename)";
            NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(strquery);
            List<?> list = query.list();
            return (List<String>) list;

        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            return new ArrayList<String>();
        }
    }


    /**
     *
     */
    @Override
    public Integer executeCountNativeQuery(String native_query) throws ContentMgmtException {
        try {
            long start = System.currentTimeMillis();
            NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(native_query);
            List<?> list = query.list();
            if (list == null)
                return null;

            if (isOracle()) {
                // java.math.BigDecimal res = (java.math.BigDecimal) query.uniqueResult();
                logger.debug("Oracle. " + native_query + " " + String.valueOf(System.currentTimeMillis() - start) + " ms");
                Object o = query.uniqueResult();
                if (o instanceof java.math.BigInteger)
                    return Integer.valueOf(((java.math.BigInteger) o).intValue());
                else if (o instanceof java.math.BigDecimal)
                    return Integer.valueOf(((java.math.BigDecimal) o).intValue());
                else {
                    throw new ContentMgmtException("Result of class " + o.getClass().getName() + " not supported.");
                }
            } else {
                Object o = query.uniqueResult();
                logger.debug("PostgreSQL. " + native_query + "  " + String.valueOf(System.currentTimeMillis() - start) + " ms");
                if (o instanceof java.math.BigInteger)
                    return Integer.valueOf(((java.math.BigInteger) o).intValue());
                else if (o instanceof java.math.BigDecimal)
                    return Integer.valueOf(((java.math.BigDecimal) o).intValue());
                else {
                    throw new ContentMgmtException("Result of class " + o.getClass().getName() + " not supported.");
                }
            }
        } catch (Exception e) {
            logger.error(e, getSessionUserName());

            throw new ContentMgmtException(e);
        }
    }

    /**
     *
     */
    @Override
    public void executeUpdateNativeQuery(String native_query) throws ContentMgmtException {
        try {

            long start = System.currentTimeMillis();
            NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(native_query);
            query.executeUpdate();
            logger.debug(native_query + " | " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        
           } catch (Exception e) {
            logger.error(e, getSessionUserName());
            throw new ContentMgmtException(e);
        }
    }
    

    /**
    *
    */
   @Override
   public void executeSelectNativeQuery(String native_query) throws ContentMgmtException {
       try {

           long start = System.currentTimeMillis();
           NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(native_query);
           query.list();
           logger.debug(native_query + " | " + String.valueOf(System.currentTimeMillis() - start) + " ms");
       
          } catch (Exception e) {
           logger.error(e, getSessionUserName());
           throw new ContentMgmtException(e);
       }
   }

    
    /**
     *
     */
    @Override
    public void delete(EmailTemplate template) throws ContentMgmtException {
        try {
            sessionFactory.getCurrentSession().delete(template);
        } catch (Exception e) {
            logger.error(e);
            throw new ContentMgmtException(e);
        }
    }

    @Override
    public void save(EmailTemplate template) throws ContentMgmtException {
        try {
            sessionFactory.getCurrentSession().save(template);
        } catch (Exception e) {
            logger.error(e);
            throw new ContentMgmtException(e);
        }
    }


    @Override
    public void saveTreeFile(TreeFile tree_file) {
        setDefaults(tree_file);
        ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(tree_file));
        sessionFactory.getCurrentSession().save(tree_file);
    }

    @Override
    public boolean listen(Event event) {
        if (event instanceof EvictCacheServiceEvent)
            return true;
        return false;
    }

    @Override
    public void onEvent(Event event) {
        logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + event.getClass().getName());
        if (event instanceof EvictCacheServiceEvent) {
            synchronized (this) {
                this.cleanHibernateCache();
                contentclassesbyid = null;
                contentclassesbyname = null;
                defaultStorageType = null;
            }
        }
    }

    /**
     * Poner:
     * ReadLock al entrar
     * WriteLock si se escribe
     */
    public KBFSStorageType getDefaultKBFSStorageType() {
        if (defaultStorageType != null)
            return defaultStorageType;
        defaultStorageType = KBFSStorageType.getByKey(findSystemParameterValueByKey("kbfs.storage.default", ServiceLocator.getService(SystemPropertiesService.class).getDefaultKBFSService()));
        return defaultStorageType;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<KBFile> getKBFilesFromFileServer(String fileserverName, int limit) throws ContentMgmtException {

        if (fileserverName == null)
            return null;

        String hql = null;

        long start = System.currentTimeMillis();

        if (fileserverName.equals("minio")) {
            hql = "FROM  KBFileImpl K where K.bucket is not null order by K.lastModifiedDate";
        } else if (fileserverName.equals("kbee")) {
            hql = "FROM  KBFileImpl K where K.bucket is null order by K.lastModifiedDate";
        } else return new ArrayList<KBFile>();

        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);

        // query.setCacheable(true);
        // query.setCacheRegion("metrics");

        if (limit > 0)
            query.setMaxResults(limit);

        List<?> results = query.list();
        List<KBFile> persons = (List<KBFile>) results;

        logger.debug(hql + " " + String.valueOf(System.currentTimeMillis() - start) + " ms");

        return persons;
    }


    /**
     * This works only in Linux
     * and requires the extension <b>file_fdw</b>
     * <br />
     * {@code
    
     * 
    			CREATE EXTENSION if not exists file_fdw; 
				
				CREATE SERVER if not exists fileserver FOREIGN DATA WRAPPER file_fdw;
				
				CREATE FOREIGN TABLE if not exists loadavg 
				(one text, five text, fifteen text, scheduled text, pid text) 
				SERVER fileserver 
				OPTIONS (filename '/proc/loadavg', format 'text', delimiter ' '); 
				
				CREATE FOREIGN TABLE if not exists meminfo 
				(stat text, value text) 
				SERVER fileserver 
				OPTIONS (filename '/proc/meminfo', format 'csv', delimiter ':'); 

}
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     */
    @Override
    @SuppressWarnings("rawtypes")
    public List<String> getDBServerLoadAvg() {
        
    	
        if (!isLinux())
            return new ArrayList<String>();


        

        
        
        boolean exists = false;

        
        String exi="SELECT EXISTS ( SELECT FROM pg_catalog.pg_class c JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace    WHERE  n.nspname = '"+ (getSchema() != null ? getSchema() : "public") +  "' AND    c.relname = ' loadavg'  AND    c.relkind = 'r' );";
        
        try {
            NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(exi);
            List list = query.list();
            
            Boolean orr = (Boolean) list.get(0);
            
            if (!orr.booleanValue()) {
            	return new ArrayList<String>();
            }
            		
                
        } catch (Exception e) {
        	exists = false;
        	logger.error(e);
            return new ArrayList<String>();
        }
            
        
        if (!exists)
        	return new ArrayList<String>();
        

        
        
        String strquery = "select one, five, fifteen from loadavg";
        logger.debug(strquery);


        
        
        try {
            NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(strquery);
            List list = query.list();

            if (list == null || list.isEmpty())
                return new ArrayList<String>();
            List<String> ret = new ArrayList<String>();
            Object[] orr = (Object[]) list.get(0);
            for (Object o : orr)
                ret.add((String) o);
            return ret;
        } catch (HibernateException e) {
            logger.error(e);
            return new ArrayList<String>();
        
        } catch (Exception e) {
            logger.error(e);
            return new ArrayList<String>();
        }
    }


    /**
     * This works only in Linux
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<String, String> getDBServerMemInfo() {
        
    	
        boolean exists = false;
        
        String exi="SELECT EXISTS ( SELECT FROM pg_catalog.pg_class c JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace    WHERE  n.nspname = '"+ (getSchema() != null ? getSchema() : "public") +"' AND    c.relname = ' meminfo'  AND    c.relkind = 'r' );";
        
        try {
            NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(exi);
            List list = query.list();
            
            Boolean orr = (Boolean) list.get(0);
            
            if (!orr.booleanValue())
                return new HashMap<String, String>();
            		
            
        } catch (Exception e) {
        	exists = false;
        	logger.error(e);
            return new HashMap<String, String>();

        }
        
        if (!exists)
            return new HashMap<String, String>();

    	
    	
    	String strquery = "select stat, value from  meminfo";
        logger.debug(strquery);
        try {

            if (!isLinux())
                return new HashMap<String, String>();
            
            

            NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(strquery);
            List list = query.list();
            if (list == null || list.isEmpty())
                return new HashMap<String, String>();
            Map<String, String> ret = new HashMap<String, String>();
            for (Object[] o : (List<Object[]>) list)
                ret.put((String) o[0], (String) o[1]);
            return ret;

        } catch (HibernateException e) {
            logger.error(e);
            return new HashMap<String, String>();
        } catch (Exception e) {
            logger.error(e);
            return new HashMap<String, String>();
        }
    }


    private boolean isOracle() {
        if (boracle != null)
            return boracle.booleanValue();
        String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);
        if (database != null && database.trim().toLowerCase().contains("oracle"))
            boracle = Boolean.valueOf(true).booleanValue();
        else
            boracle = Boolean.valueOf(false).booleanValue();
        return boracle.booleanValue();
    }


    private boolean isLinux() {
        if (System.getenv("OS") != null && System.getenv("OS").toLowerCase().contains("windows"))
            return false;
        return true;
    }


    @Override
    public void save(SearcherHomeBlock block) {
        this.sessionFactory.getCurrentSession().save(block);
    }

	@Override
    @Deprecated
    @SuppressWarnings("unchecked")
    public List<SearcherHomeBlock> getSearcherHomeBlock(Domain domain) {
        String hql = "FROM KbeeSearcherHomeBlock U where U.domain.id=" + domain.getId().toString();
        logger.debug(hql);
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        query.setCacheRegion("query");
        List<?> results = query.list();
        return (List<SearcherHomeBlock>) results;
    }

	@Override
    @SuppressWarnings("unchecked")
    public List<Classification> getBuiltInClassification(Classifier classifier, DataSet builtin, DataSetMember aggregator) {

        String hql = "FROM KbeeMemberClassification K WHERE K.classifier.id=" + classifier.getId().toString() + " and K.sourcemember.dataset.id=" + builtin.getId().toString() + " and K.datasetmember.id=" + aggregator.getId().toString();
        logger.debug(hql);
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        @SuppressWarnings("rawtypes")
        List results = query.list();
        if (results.isEmpty())
            new ArrayList<Classification>();

        if (logger.isDebugEnabled()) {
            List<KbeeMemberClassification> xl = (List<KbeeMemberClassification>) results;
            for (KbeeMemberClassification c : xl) {
                logger.debug(c.getSource().getName() + " -> " + c.getDataSetMember().getName());
            }
        }

        return (List<Classification>) results;
    }

    @Override
    public List<DataSetMember> getMembers(DataSet dataSet, String orderby) {
        return getMembers(dataSet, orderby, -1);
    }
    
    @Override
    public SupportTicket findSupportTicket(Long id) {
        if (id == null)
            throw new IllegalArgumentException("id is null");
        return (SupportTicket) sessionFactory.getCurrentSession().get(KbeeSupportTicket.class, id);
    }
    
	@Override
    public void delete(SupportTicket s) {
        sessionFactory.getCurrentSession().delete(s);
    }
	
	@Override
    public void delete(UserSignature us) {
        sessionFactory.getCurrentSession().delete(us);
    }
    
	@Override
    public void update(SupportTicket note) {
        note.setLastModifiedOffsetDateTime(OffsetDateTime.now());
        ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(note));
        sessionFactory.getCurrentSession().update(note);
    }

	@Override
    public void save(SupportTicket note) {
        note.setLastModifiedOffsetDateTime(OffsetDateTime.now());
        ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(note));
        sessionFactory.getCurrentSession().save(note);
    }
	
    public void save(UserSignature signature) {
        sessionFactory.getCurrentSession().save(signature);
    }
 
	/**
	
	 * pending tickets where status is < 6
	 * 
	 * @param max_errorCount
	 * @return
	 */
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SupportTicket> getPendingSupportTickets(int max_errorCount) {
        String hql = "FROM " + KbeeSupportTicket.class.getSimpleName() + " D WHERE D.deliverystatus=" + String.valueOf(SupportTicket.DELIVERY_STATUS_PENDING) +" and error_count < " + String.valueOf(max_errorCount) + " order by D.creationDate";
        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
    
        // TBA
        // returns the 30000 newest
        query.setMaxResults(30000);
        @SuppressWarnings("rawtypes")
        List results = query.list();
        if (results.isEmpty())
            return new ArrayList<SupportTicket>();
        return (List<SupportTicket>) results;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
    public List<SupportTicket> getSupportTickets(Domain domain) {
    	final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("id", String.valueOf(domain.getId() ));
        String hql = "FROM " + KbeeSupportTicket.class.getSimpleName() + " D WHERE D.domain.id = :id order by D.creationDate desc";
        List results = getResultSet(hql, parameters, 30000);
        if (results.isEmpty())
        	return new ArrayList<SupportTicket>();
        return (List<SupportTicket>) results;
    }

    @Override
    public Payment findPaymentById(Serializable id){
        return (Payment) sessionFactory.getCurrentSession().get(KbeePayment.class, id);
    }

    @Override
    public Payment findPaymentsByTrxReference(String trxReference) {
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("trxReference", trxReference);
        final List<Payment> resultSet = (List<Payment>) getResultSet("FROM KbeePayment p where p.trxReference = :trxReference", parameters);
        return resultSet.isEmpty() ? null : resultSet.get(0);
    }

    
    
    
    @Override
    public List<Payment> findPaymentsByKey(String key, boolean confirmedOnly){
        String hql = "FROM KbeePayment p where p.key = :key";
	    final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("key", key);

        if(confirmedOnly){
            hql+= " and p.status = :status";
            parameters.put("status", PaymentStatus.CONFIRMED);
        }

        return (List<Payment>) getResultSet(hql, parameters);
    }

    public List<Payment> findPaymentsPending(OffsetDateTime sinceCreateDate, int maxResults){
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("status", PaymentStatus.PENDING);
        parameters.put("sinceCreateDate", sinceCreateDate);
        return (List<Payment>) getResultSet("FROM KbeePayment p where p.status = :status and p.createDate >= :sinceCreateDate", parameters, maxResults);
    }

    
    
    @Override
    public ActionRule findActionRuleByContentOId(Long contentOId) {
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("contentOId", contentOId);
        final List<ActionRule> resultSet = (List<ActionRule>) getResultSet("FROM " + KbeeActionRule.class.getSimpleName() + "  p where p.contentOId = :contentOId", parameters);
        return resultSet.isEmpty() ? null : resultSet.get(0);
    }
    

    
}	
