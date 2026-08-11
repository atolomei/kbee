package com.novamens.kbee.content.service;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.dom.Domain;

public class KbeeMemberUrlService extends KbeeAbstractUrlService {
			
	private DataSetMember member;
	
	public KbeeMemberUrlService() {
	}

	public KbeeMemberUrlService(DataSetMember member) {
		this.member = member;
	}
	
	public String getUrl() {
		return "/dataset/"+getMember().getDataSet().getId() +"/"+ getMember().getId();
	}
	
	public String getPublicUrl(Person person) {
		return null;
	}
	
	public String getPublicUrl() {
		return null;
	}
	
	public DataSetMember getMember() {
		return member;
	}
	
	protected Domain getDomain() {
		return getMember().getDomain();
	}

	@Override
	public String getRelativeUrl() {
		return getUrl();
	}

	@Override
	public String getUrl(boolean include_server) {
		return getUrl();
	}

	@Override
	public String getPublicUrl(String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPublicUrl(Person person, String password) {
		// TODO Auto-generated method stub
		return null;
	}
}