package com.novamens.kbee.content.model;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.novamens.content.base.SecurityRule;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.content.security.KbeeMemberSecurityRule;
import com.novamens.kbee.content.security.KbeeSecurityRule;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.content.model.SecuredMember;
import com.novamens.content.model.SecuredSet;

@Entity
@DiscriminatorValue(value="20")
public class KbeeSecuredMember extends KbeeValueMember implements SecuredMember {

	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeSecurityRule.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "rule_id")
	private SecurityRule rule;
	
	public KbeeSecuredMember() {
		super();
	}
		
	public KbeeSecuredMember(DataSet dataset) {
		super(dataset);
	}
	
	@Override
	public void setValue(Object value) {
		super.setValue(value);
		
		String name = value!=null?value.toString():""; 

		if (getSecurityRule()!=null) {
			if (!name.equals(getSecurityRule().getName())) {
				getSecurityRule().setName(name);
				getSecurityRule().setDescription(getRuleDescription());
				getSecurityRule().setDisplayCondition(getRuleDisplayCondition());
			}
		}
		else {
			setSecurityRule(createSecurityRule());
		}
	}
	
	public void setStrValue(String value) {
		setValue(value);
	}
	
	@Override
	public Object getValue() {
		return getStrValue();
	}
	
	public String getDisplayName() {
		return getStrValue();
	}
	
	public SecurityRule getSecurityRule() {
		return rule;
	}
	
	public void setSecurityRule(SecurityRule rule) {
		this.rule = rule;
	}
	
	public Acl getInheritedAcl() {
		KbeeAcl acl = (KbeeAcl)getSecurityRule().getAcl();
		if (acl.getEntries().isEmpty()) {
			acl = new KbeeAcl();
			for (DataSetMember parent : getParents()) {
				if (parent instanceof KbeeSecuredMember) {
					KbeeAcl parentAcl = (KbeeAcl)((KbeeSecuredMember)parent).getInheritedAcl();
					acl.merge(null, parentAcl);
				}
			}
		}
		return acl;
	}
	
	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		super.setLastModifiedOffsetDateTime(date);
		if (getSecurityRule()!=null) {
			getSecurityRule().setLastModifiedOffsetDateTime(getLastModifiedOffsetDateTime());
		}
	}
	
	@Override
	public void setLastModifiedUser(User user)	{
		super.setLastModifiedUser(user);
	}
	
	private SecurityRule createSecurityRule() {
		KbeeMemberSecurityRule rule = new KbeeMemberSecurityRule();
		
		rule.setName(getName());
		
		rule.setDerived(true);
		rule.setDomain(getDomain());
		rule.setCreationOffsetDateTime(OffsetDateTime.now());
		rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		rule.setLastModifiedUser(getDataSet().getLastModifiedUser());
		
		rule.setDescription(getRuleDescription());
		
		rule.setType(SecurityRule.RULE_SECURED_MEMBER);
		rule.setCondition(getCondition());
		rule.setMember(this);
		rule.setDisplayCondition(getRuleDisplayCondition());

		KbeeAcl acl = new KbeeAcl();
		acl.setCreationOffsetDateTime(OffsetDateTime.now());
		acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		acl.setLastModifiedUser(getSessionUser());
		rule.setAcl(acl);
		
		return rule;
	}
	
	public String getCondition() {
		StringBuilder condition = new StringBuilder();
		condition.append("c"+String.valueOf(getClassifier().getId())+"("+String.valueOf(getId())+")");
		return condition.toString();
	}
	
	private Classifier getClassifier() {
		Object object =  getDataSet();
		if (object instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy)object;
			LazyInitializer initializer = proxy.getHibernateLazyInitializer();
			object = (DataSet)initializer.getImplementation();
		}
		return ((SecuredSet)object).getClassifier();
	}
	
	private String getRuleDescription() {
		Locale locale = Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeeSecuredMember.this.getClass().getName(), locale);
		String description = res.getString("rule-generated") + " " + getStrValue() + " (" + getDataSet().getName()+")";
		return description;
	}
	
	private String getRuleDisplayCondition() {
		StringBuffer condition = new StringBuffer();
		String predicate = getClassifier().getPredicate();
		condition.append("<span class= \"predicate\" >" + predicate+"</span>");
		condition.append("<span class= \"iql-group-start\"> ( </span> ");
		condition.append("<span class= \"iql-value\" >"+ getDisplayName()+"</span> ");
		condition.append("<span class= \"iql-group-end\"> ) </span> ");
		return condition.toString();
	}
}
