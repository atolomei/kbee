package com.novamens.kbee.content.webapi.type;

import org.springframework.util.Assert;

import com.novamens.content.base.SecurityRule;
import com.novamens.content.security.IQLRule;

import kbee.api.model.ApiProxy;
import kbee.api.model.ISecurityRule;

public class ISecurityRuleAdapter implements Adapter<SecurityRule, ISecurityRule> {
	
	public ISecurityRuleAdapter() {
	}
	
	public ISecurityRule adapt(SecurityRule rule) {
		
		Assert.isInstanceOf(IQLRule.class, rule, "invalid rule");
		
		IQLRule iqlrule = (IQLRule)rule;
		
		ISecurityRule irule = new ISecurityRule();
		
		irule.setId(String.valueOf(iqlrule.getId()));
		irule.setDisplayName(rule.getDisplayName());
		irule.setCondition(iqlrule.getCondition());
		irule.setAcl((new IAclAdapter()).adapt(iqlrule.getAcl()));
		irule.setDomain(iqlrule.getDomain().getDisplayName());
		irule.setDescription(iqlrule.getDescription());
		irule.setLastModifiedDate(iqlrule.getLastModifiedOffsetDateTime());
		irule.setLastModifiedUser(new ApiProxy(iqlrule.getLastModifiedUser().getDisplayName(), UriHelper.getUri(iqlrule.getLastModifiedUser()), "user"));
		
		return irule;	
	}
}
