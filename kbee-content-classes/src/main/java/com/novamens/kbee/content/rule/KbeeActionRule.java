package com.novamens.kbee.content.rule;

import java.io.Serializable;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.RuleCondition;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.rule.Action;
import com.novamens.content.rule.ActionRule;
import com.novamens.dom.Json;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "Kb_Action_Rule")
@Proxy(lazy=false)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rule_type", discriminatorType=DiscriminatorType.STRING)
public abstract class KbeeActionRule extends AbstractObject implements ActionRule {
													
	protected static Logger logger 	= Logger.getLogger(KbeeActionRule.class.getName());

	static final public String CLASS_CODE = "ACTIONRULE";
	
	@Id 
	@SequenceGenerator(name = "actionrule_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actionrule_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "display_name")
	private String displayName;
	
	@Column(name = "action")
	private String jsonaction;
	
	@Column(name = "displaycondition")
	private String displayCondition;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "notes")
	private String notes;
	
	@Column(name = "calendar")
	private Boolean calendar = Boolean.valueOf(true);

	@Column(name = "contentoid")
	private Long contentOId;

	@Column(name = "isContentRule")
	private boolean isContentRule;
	
	transient private Action action;

	
	@Override
	public Serializable getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public void setName(String name) {
		this.name = name; 
		this.displayName = name; 
	}
	
	public String getName() {
		return name;
	}
	
	public void setAction(Action action) {
		this.jsonaction = getString(getJson(action));
	}
	
	public Action getAction() {
		if (action==null) {
			if (jsonaction!=null) {
				action = parseAction(getJson(jsonaction));
				action.setActionRuleName(this.getName());
				action.setActionRuleId(this.getId());
			}
		}	
		return action;
	}
	
	public String getDisplayName() {
		return displayName;
	}
					
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String getNotes() {
		return notes;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public abstract RuleCondition getRuleCondition();
		
	public String getDisplayCondition() {
		return displayCondition;
	}
	
	public void setDisplayCondition(String condition) {
		this.displayCondition = condition;
	}

	@Override
	public Boolean getCalendar() {
		return calendar;
	}
	
	@Override
	public boolean isContentRule() {
		return isContentRule;
	}

	public void setContentRule(boolean isContentRule) {
		this.isContentRule = isContentRule;
	}

	@Override
	public Long getContentOId() {
		return  contentOId;
	}
	
	public void setContentOid(Long oid) {
		contentOId=oid;
	}
	
	public void setCalendar(Boolean calendar) {
		this.calendar = calendar;
	}
	
	public boolean isDerived() {
		return false;
	}
	
	public OffsetDateTime getExecutionDate() {
		return isContentRule() ? getExecutionDate(getContent()) : null; 
	}
	
	public OffsetDateTime getExecutionDate(Content content) {
		if (getCondition()==null) return null;
		IqlService iqlservice = getDomain().getService(IqlService.class);
		Expression expression = iqlservice.getExpression(getCondition());
		ExecutionDateCalculator calculator = new ExecutionDateCalculator(expression);
		OffsetDateTime time = calculator.evaluate(content);
		return time;
	}
	
	public Content getContent() {
		if (!isContentRule())
			return null;
		Content content = null;
		try {
			content = getContentDao().findContentByOId(getContentOId());
		}
		catch (Exception e) {
			logger.error(e);
		}
		return content;
	}
	
	public String getType() {
		return "Action";
	}
	
	private String getString(Json json) {
		return json!=null ? json.toString() : null;
	}
	
	private Json getJson(String string) {
		return new KbeeJson(string);
	}
	
	private Json getJson(Action action) {
		return ActionParser.Get().getJson(action);
	}
	
	private Action parseAction(Json json) {
		return ActionParser.Get().getAction(json);
	}
	
	private ContentDao getContentDao() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		return (ContentDao) beans.getBean("contentDao");
	}
}