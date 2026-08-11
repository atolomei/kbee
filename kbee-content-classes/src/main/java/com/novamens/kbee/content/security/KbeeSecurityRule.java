package com.novamens.kbee.content.security;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.novamens.content.base.Content;
import com.novamens.content.security.IQLRule;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeAcl;

import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

import kbee.util.logging.Logger;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_SECURITY_RULE")
@Inheritance(strategy=InheritanceType.JOINED)
public class KbeeSecurityRule implements IQLRule {
			
	private static Logger logger = Logger.getLogger(KbeeSecurityRule.class.getName());
	
	@Id 
	@SequenceGenerator(name = "rule_sequencer", sequenceName = "security_sequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rule_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "lastModifiedDate")
	private OffsetDateTime lastModifiedDate;
	
	@Column(name = "creationDate")
	private OffsetDateTime creationDate;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "lastModifiedUser")
	private User lastModifiedUser;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDomain.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "domain_id")
	private Domain domain;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "condition")
	private String condition;
	
	@Column(name = "displaycondition")
	private String displayCondition;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "type")
	private int type;
	
	@Column(name = "derived")
	private boolean derived;

	@Column(name = "notes")
	private String notes;
	
	@Column(name = "parent_objectid")
	private String parent_objectid;
	
	// Aggregation
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeAcl.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "acl")
	private Acl acl;

	@Column(name = "role_rule")
	private boolean role_rule = false;
	
	@Column(name = "state")
	@Enumerated(EnumType.ORDINAL)
	@Type(type = "com.novamens.kbee.dom.ObjectStateUserType")
	private ObjectState state;
	
	@Override
	public ObjectState getState() {
		return state;
	}
	
	public void setState(ObjectState state) {
		this.state = state;
	}
	
	private transient Expression conditionExpression;
	
	@Override
	public Long getId() {
		return id;
	}
	
	
	public void setId(Serializable id) {
		this.id = (Long) id;
	}

	@Override
	public boolean evaluate(Content content) {
		if (condition==null || "".equals(condition)) 
			return false;
		
		try {JavaIqlEvaluator evaluator = new JavaIqlEvaluator(getConditionExpression());
			boolean evaluation = evaluator.evaluate(content);
			return evaluation;
		} 
		catch (RuntimeException e) {
			logger.error(e, "Rule: " + this.toString());
			return false;		
		}
		catch (Exception e) {
			logger.error(e, "Rule: " + this.toString());
			return false;		
		}
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		if (getName()!=null)
			str.append(getName() + " | ");
		
		if (getCondition()!=null)
		str.append(getCondition() + " | ");
		
		if (this.description!=null)
			str.append(getDescription() + " | ");
		
		str.append("Id: "+getId());
		
		return str.toString();				
	}
	
	
	public boolean isRoleRule() {
		return role_rule;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean isDerived() {
		return derived;
	}
	
	
	public void setDerived(boolean value) {
		this.derived = value;
	}
	
	/**  
	 *  <code>type</code> can be whether 
	 *  Rule was created via a Wizard
	 *  or writing a IQL sentence freely.
	 */
	@Override
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	@Override
	public Domain getDomain() {
		return this.domain;
	}
	
	@Override
	public String getDescription() {
		return description == null ? condition : description;
	}
	
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String getDisplayCondition() {
		return displayCondition == null ? condition : displayCondition;
	}
	
	@Override
	public void setDisplayCondition(String condition) {
		this.displayCondition = condition;
	}
	
	@Override
	public String getCondition() {
		return condition;
	}

	@Override
	public void setCondition(String condition) {
		this.conditionExpression = null;
		this.condition = condition;
	}
	
	@Override
	public Acl getAcl() {
		return acl;
	}
	
	public void setAcl(Acl acl) {
		this.acl = acl; 
	}
	
	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		return this.creationDate;
	}
	
	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return lastModifiedDate;
	}
	
	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	@Override
	public User getLastModifiedUser() {
		return lastModifiedUser;
	}
	
	public void setLastModifiedUser(User lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
	
	public Expression getConditionExpression() {
		if (this.conditionExpression == null) 
			this.conditionExpression = getDomain().getService(IqlService.class).getExpression(getCondition());
		return this.conditionExpression;
	}
	
	@Override
	public String getParentObjectId() {
		return this.parent_objectid;
	}
	
	@Override
	public void setParentObjectId(String objectid) {
		this.parent_objectid=objectid;
		
	}
	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	@Override
	public String getDisplayName() {
		return getName();
	}

	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		this.creationDate=date;
		
	}

	public String getNotes() {
		return notes;
	}
	
	public void setNotes(String description) {
		this.notes = description;
	}

	@Override
	public void setDefaultAudit() {
	   
		if (this.getCreationOffsetDateTime()==null)
			this.setCreationOffsetDateTime(OffsetDateTime.now());
		
		if (this.getLastModifiedOffsetDateTime()==null)
			this.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		if (this.getLastModifiedUser()==null)
			this.setLastModifiedUser(getSessionUser());
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
	
	public AuditSet getAuditSet() {
		return AuditSet.SECURITY;
	}

	@Override
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
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
		return service.timeElapsed(date, ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
	}
}
