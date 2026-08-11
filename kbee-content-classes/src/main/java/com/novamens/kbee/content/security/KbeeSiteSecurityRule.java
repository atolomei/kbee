package com.novamens.kbee.content.security;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.novamens.content.base.SiteIQLRule;

@Entity
@PrimaryKeyJoinColumn(name="rule_id")
@Table(name = "PO_SITE_SECURITYRULE")
public class KbeeSiteSecurityRule extends KbeeSecurityRule implements SiteIQLRule {
	
	@Column(name = "related_object_id")
	private String related_object_id;

	@Override
	public void setRelatedObjectId(String id) {
		this.related_object_id = id;
	}
	
	@Override
	public String getRelatedObjectId() {
		return related_object_id;
	}
}