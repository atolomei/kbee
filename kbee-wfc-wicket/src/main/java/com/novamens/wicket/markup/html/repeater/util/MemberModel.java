package com.novamens.wicket.markup.html.repeater.util;

import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.Member;

public class MemberModel implements IModel<Member> {
	private static final long serialVersionUID = 1L;
	private Member object;
	
	public MemberModel(Member member) {
		setObject(member);
	}
	
	public Member getObject() {
		return object;
	}
	
	public String getDisplayName() {
		return getObject().getDisplayName();
	}
	
	public String getFacet() {
		return getObject().getFacet();
	}
	
	public String getFacetDisplayName() {
		return getObject().getFacetDisplayName();
	}
	
	public void setObject(Member member) {
		object = member;
	}
	
	public void detach() {
	}
}