package com.novamens.kbee.content.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.ExternalMember;


@Entity
@DiscriminatorValue(value="7")
public class KbeeExternalMember extends KbeeDataSetMember implements ExternalMember {
			
	@Column(name = "EXTERNAL_URL")
	private String external_url;

	@Column(name = "EXTERNAL_MEMBER_ID")
	private Long external_member_id;
	
	public KbeeExternalMember() {
		super();
	}
		
	public KbeeExternalMember(DataSet dataset) {
		super(dataset);
	}
	
	public KbeeExternalMember(Serializable id, String value, DataSet dataset) {
		super(value, dataset);
		setExternalMemberId((Long)id);
		setDomain(dataset.getDomain());
	}

	
	public KbeeExternalMember(Serializable id, String value, String url, DataSet dataset) {
		super(value, dataset);
		setExternalMemberId((Long)id);
		setExternalUrl(url);
		setDomain(dataset.getDomain());
	}

	@Override
	public void setExternalMemberId(Long id) {
		this.external_member_id = id;
	}
	
	@Override
	public Long getExternalMemberId() {
		return this.external_member_id;
	}
	
	public String getDisplayName() {
		return getStrValue();
	}

	
	public void setExternalUrl(String url) {
		this.external_url=url;
	}
	
	@Override
	public String getExternalUrl() {
		return external_url;
	}

	@Override
	public boolean isOnlyRootEdit() {
		return false;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}



}
