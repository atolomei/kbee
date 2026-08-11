package com.novamens.kbee.content.rule;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Content;
import com.novamens.content.base.RuleCondition;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.EntityMember;
import com.novamens.content.properties.ObjectPropertyService;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.rule.EntityRule;
import com.novamens.kbee.content.model.KbeeEntityMember;

@Entity
@DiscriminatorValue("entity")
public class KbeeEntityRule extends KbeeActionRule implements EntityRule {

	public static String Type = "entity";
	
	@Column(name = "condition")
	private String condition;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeEntityMember.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="entity_id")
	private EntityMember entity;
	
	transient
	DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	@Override
	public void evaluate() {
		evaluate(ActionRule.TEST_CONDITION);
	}

	@Override
	public void evaluate(String mode) {
		if (evaluate(getEntity())) {
			getAction().execute(getEntity());
			setApplied(getEntity());
		}
	}
	
	public boolean evaluate(Classificable entity) {
		OffsetDateTime now = OffsetDateTime.now();
		if (entity instanceof EntityMember && ((SchedulerRuleCondition)getRuleCondition()).isTrue(now)) {
			return !isApplied((EntityMember)entity, now);
		}
		return false;

	}
	
	public boolean evaluate(Content content) {
		return false;
	}
	
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public RuleCondition  getRuleCondition() {
		return new SchedulerRuleCondition(getCondition());
	}

	public EntityMember getEntity() {
		return entity;
	}

	public void setEntity(EntityMember entity) {
		this.entity = entity;
	}
	
	public OffsetDateTime getNextExecution() {
		return ((SchedulerRuleCondition)getRuleCondition()).getNextExecution();
	}
	
	public OffsetDateTime getLastExecution() {
		List<OffsetDateTime> executions = getExcecutions();
		return executions.isEmpty() ?  null : executions.get(executions.size()-1);
	}

	@Override
	public String getType() {
		return Type;
	}
	
	private boolean isApplied(EntityMember entity, OffsetDateTime time) {
		time = truncate(time);
		for (OffsetDateTime executiontime : getExcecutions()) {
		    OffsetDateTime time0  = time.minusDays(3);
			if (time.equals(time0) || (time.isAfter(time0) && time.isBefore(executiontime)) || time.equals(executiontime)) {
		    	return true;
			}	
		}
		return false;
	}
	
	private void setApplied(EntityMember entity) {
		String value = (String)entity.getService(ObjectPropertyService.class).getProperty("rule_"+String.valueOf(getId()));
		value = value==null ? "" : value+";";
		value += timeformatter.format(OffsetDateTime.now());
		entity.getService(ObjectPropertyService.class).setProperty("rule_"+String.valueOf(getId()), value);
	}
	
	private OffsetDateTime truncate(OffsetDateTime time) {
		ZoneId zone = ZoneId.systemDefault();
		String datevalue = timeformatter.format(time);
		LocalDate localdate = LocalDate.parse(datevalue, timeformatter);
	    OffsetDateTime truncated = localdate.atStartOfDay(zone).toOffsetDateTime();
	    return truncated;
	}
	
	private List<OffsetDateTime> getExcecutions() {
		List<OffsetDateTime> executions = new ArrayList<OffsetDateTime>();
		String value = (String)entity.getService(ObjectPropertyService.class).getProperty("rule_"+String.valueOf(getId()));
		if (value!=null) {
			ZoneId zone = ZoneId.systemDefault();
			StringTokenizer tokenizer = new StringTokenizer(value, ";");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				try {
					LocalDate localdate = LocalDate.parse(token.trim(), timeformatter);
				    OffsetDateTime time = localdate.atStartOfDay(zone).toOffsetDateTime();
				    executions.add(time);
				}
				catch (Exception e) {
				}
			}
		}
		return executions;
	}
}
