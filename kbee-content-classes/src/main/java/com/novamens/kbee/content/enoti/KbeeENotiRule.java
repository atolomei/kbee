package com.novamens.kbee.content.enoti;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Content;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.security.Role;
import com.novamens.datetime.DateTimeService;

import com.novamens.event.LogEvent;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.content.security.JavaIqlEvaluator;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.security.KbeePrincipal;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.logging.ContentEvent;
import com.novamens.logging.TaskPendingEvent;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;


/**
 * <p>Email Notification Rule. The main types are:
 * 
 * . Self Service User defined
 *  The receiver is the user that created the rule
 * 
 * . Created by Admin users
 *   There is a list of receivers defined by the admin user that created the rule
 * </p>
 * 
 * 
 * see: {@link ENotiRuleService}
 * 
 *
 */
@Entity
@Table(name = "KB_ENOTIRULE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
public class KbeeENotiRule extends AbstractObject implements ENotiRule {
	
	
	static final public String CLASS_CODE = "ENOTIRULE";
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeENotiRule.class.getName());
	
	@Id
	@SequenceGenerator(name = "rule_sequencer", sequenceName = "security_sequence", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rule_sequencer")
	@Column(name = "id")
	private Long id;
	
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "owner")
	private User owner;

	@ManyToMany(fetch = FetchType.LAZY, targetEntity = KbeePrincipal.class)
	@JoinTable(name = "KB_ENOTIRULE_PRINCIPAL", joinColumns = {
			@JoinColumn(name = "RULE_ID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "PRINCIPAL_ID", nullable = false, updatable = false) })
	List<Principal> receivers = new ArrayList<Principal>();
	
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = KbeeAbstractRole.class)
	@JoinTable(name = "KB_ENOTIRULE_ROLE", joinColumns = {
			@JoinColumn(name = "RULE_ID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "ROLE_ID", nullable = false, updatable = false) })
	List<Role> rolereceivers = new ArrayList<Role>();

	@Column(name = "condition")
	private String condition;

	@Column(name = "event_type")
	private int event_type;
	
	@Column(name = "event_types")
	private String event_types;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "notes")
	private String notes;

	@Column(name = "key")
	private String key;
	
	@Column(name = "isalert")
	private boolean isalert;
	
	@Column(name = "isemail")
	private boolean isemail = true;
	
	@Column(name = "is_system")
	private boolean is_system;
	
	@Column(name = "enabled")
	private boolean enabled;

	private transient Expression conditionExpression;
	private transient String subject;
	private transient String text;
							
	private transient Serializable actionRuleId = null;
	private transient String actionRuleName = null;
	
	public Serializable getActionRuleId() {		return actionRuleId;	}
	public void setActionRuleId(Serializable actionRuleId) {		this.actionRuleId = actionRuleId;	}
	
	public String getActionRuleName() {		return actionRuleName;	}
	public void setActionRuleName(String actionRuleName) {		this.actionRuleName = actionRuleName;	}

	private transient String rule_source = null;
	public void setRuleSource(String src) {this.rule_source=src;}

	/**
	 * Self Service
	 * System Alert
	 * Manual ?
	 * Timed
	 */
	public String getRuleSource() {
		if (rule_source==null) {
			return isSystem() ? ENotiRule.SOURCE_SYSTEM_ALERT_RULE : ENotiRule.SOURCE_SELF_SERVICE;
		}
		return rule_source;
	}
	
	@Override
	public String getMetadataAsString() {
		return  getName() +". " + getCondition();
	}

	@Override
	public Long getId() {
		return id;
	}
	
	@Override
	public void setId(Serializable id) {
		this.id = (Long) id;
	}

	public int getType() {
		return 4;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getDisplayCondition() {
		return getDescription();
	}

	public String getDescription() {
		return description == null ? condition : description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getSubject() {		return subject;	}
	public void setSubject(String description) {		this.subject = description;	}
											
	public String getText() {		return text;	}
	public void setText(String description) {		this.text = description;	}
	
	
	public String getNotes() {
		return notes;
	}

	public void setNotes(String description) {
		this.notes = description;
	}

	public void setDisplayCondition(String description) {
		setDescription(description);
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.conditionExpression = null;
		this.condition = condition;
	}

	@Override
	public User getOwner() {
		return owner;
	}

	@Override
	public void setOwner(User user) {
		owner = user;
	}

	@Override
	public List<Principal> getReceivers() {
		return receivers;
	}

	public void setReceivers(List<Principal> receivers) {
		this.receivers.clear();
		this.receivers.addAll(receivers);
	}
	
	@Override
	public List<Role> getRoleReceivers() {
		return rolereceivers;
	}

	public void setRoleReceivers(List<Role> receivers) {
		this.rolereceivers.clear();
		this.rolereceivers.addAll(receivers);
	}
	
	@Override
	public boolean evaluate(LogEvent event) {

		if (event instanceof ContentEvent) {
			return (evaluate((Content) ((ContentEvent) event).getContent()));
		}

		if (getEventType() == ENotiRule.EVENT_PENDING_TASK && event instanceof TaskPendingEvent) {

			Content content = (Content) ((TaskPendingEvent) event).getContent();

			if (content == null || content.getWorkspace() == null || content.getDomain() == null) // ||// !content.getWorkspace().toString().equals(wkuser.getId().toString()))
				return false;

			// Si Evento es Task to Pending, y el Content fue a Pending y el Content cumple
			// la condición
			//
			return (evaluate((Content) ((TaskPendingEvent) event).getContent()));
		}

		return false;
	}

	@Override
	public boolean evaluate(Content content) {
		if (condition == null || condition.length() == 0)
			return false;
		JavaIqlEvaluator evaluator = new JavaIqlEvaluator(getConditionExpression());
		boolean evaluation = evaluator.evaluate(content);
		return evaluation;
	}

	public Expression getConditionExpression() {
		if (conditionExpression == null) {
			if (getCondition() == null)
				return null;
			try {
				conditionExpression = getDomain().getService(IqlService.class).getExpression(getCondition());
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return conditionExpression;
	}

	@Override
	public String getDisplayName() {
		return (getName()!=null)?getName(): "null";
	}

	@Override
	public boolean isDerived() {
		return false;
	}

	@Override
	public boolean isSystem() {
		return this.is_system;
	}
	
	public void setIsSystem( boolean b) {
		this.is_system=b;
	}

	public String getEventKey(int eventType) {
		if (eventType == EVENT_PUBLISH_CONTENT)			return  "publish";
		else if (eventType == EVENT_PENDING_TASK)		return "pending-task";
		return "publish";
	}
	
	public String getEventName(int eventType, Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(KbeeENotiRule.class.getName(), locale);
		if (eventType == EVENT_PUBLISH_CONTENT)			return res.getString("publish");
		else if (eventType == EVENT_PENDING_TASK)		return res.getString("pending-task");
		return  res.getString("event");
	}

	@Override
	public void setDefaultAudit() {
		if (this.getCreationOffsetDateTime() == null)
			this.setCreationOffsetDateTime(OffsetDateTime.now());
		if (this.getLastModifiedOffsetDateTime() == null)
			this.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		if (this.getLastModifiedUser() == null)
			this.setLastModifiedUser(getSessionUser());
	}

	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	public void setRequireConfirm(boolean b) {
	}
	public boolean isRequireConfirm() {
		return false;
	}
	public boolean getRequireConfirm() {
		return false;
	}
	
	
	
	public void setEmail(boolean isemail) {
		this.isemail=isemail;
	}
	
	public void setIsEmail(boolean isemail) {
		this.isemail=isemail;
	}
	
	public void setAlert(boolean isalert) {
		this.isalert=isalert;
	}
	
	public void setIsAlert(boolean isalert) {
		this.isalert=isalert;
	}
	
	@Override
	public boolean isEmail() {
		return this.isemail;
	}

	@Override
	public boolean isAlert() {
		return this.isalert;
	}

	@Override
	public int getEventType() {
		return this.event_type;
	}

	@Override
	public void setEventType(int type) {
		this.event_type=type;
	}
	
	@Override
	public String getEventTypeStr(Locale locale) {
		return getEventName(getEventType(), locale);
	}
	
	public List<String> getEventTypes() {
		List<String> types = new ArrayList<String>();
		if (event_types!=null) {
			StringTokenizer tokenizer = new StringTokenizer(event_types, ",");
			while (tokenizer.hasMoreTokens()) {
				types.add(tokenizer.nextToken().trim());
			}
		}
		return types;
	}
	
	public void setEventTypes(List<String> types) {
		String typesvalue = "";
		for (String type : types) {
			if (!"".equals(typesvalue)) typesvalue += ", ";
			typesvalue += type;
		}
		this.event_types = typesvalue;
	}
	
	public boolean includes(String type) {
		return this.event_types!=null && this.event_types.contains(type);
	}

	@Override
	public String getEmailTemplate() {
		if (getEventType()==ENotiRule.EVENT_PENDING_TASK) {
			return PENDING_EMAIL_TEMPLATE; // "pending-task";
		}
		else if (getEventType()==ENotiRule.EVENT_PUBLISH_CONTENT) {
			return PUBLISH_EMAIL_TEMPLATE; // "alert-rule-publish";
		}
		return "";
	}
	
	@Override
	public String getLastModifiedOffsetDateTimeColloquial() {
		return getLastModifiedOffsetDateTimeColloquial("ago");
	}
	
	@Override
	public String getCreationOffsetDateTimeColloquial() {
		return getLastModifiedOffsetDateTimeColloquial(getCreationOffsetDateTime(), "ago");
	}
	
	public String getLastModifiedOffsetDateTimeColloquial(String classago) {
		return getLastModifiedOffsetDateTimeColloquial(getLastModifiedOffsetDateTime(), classago);
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Id -> " + (id!=null? id.toString():"null"));
		str.append(" | Name -> " + (getName()!=null? getName():"null"));
		str.append(" | Owner -> " + (owner!=null? owner.getDisplayName():"null"));		
		str.append(" | Condition -> " + (condition!=null? condition.toString():"null"));
		str.append(" | Key -> " + (owner!=null? owner.getDisplayName():"null"));
		str.append(" | Source -> " + (getRuleSource()!=null? getRuleSource():"null"));
		return str.toString();
	}
	
	@Override
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
	}

	public String getClassCode() { 
		return KbeeENotiRule.CLASS_CODE; 
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
	
	private String getLastModifiedOffsetDateTimeColloquial(OffsetDateTime date, String classago) {
		if (date==null) 
			return "";
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getSessionUser();
		
		String zid = null;
		if (user!=null)
			zid=service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null)
			zid=ZoneId.systemDefault().getId();
		Locale locale = null;
		if (user!=null)
				locale=user.getLocale();
		else
			locale=Locale.getDefault();
		
		return service.timeElapsed(date, ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, classago);
	}
}
