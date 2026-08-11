package com.novamens.kbee.content.security;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.SecuredMember;
import com.novamens.kbee.content.model.KbeeSecuredMember;
import com.novamens.security.acl.Acl;
import com.novamens.service.ServiceLocator;

@Entity
@PrimaryKeyJoinColumn(name="rule_id")
@Table(name = "KB_MEMBER_SECURITY_RULE")
public class KbeeMemberSecurityRule extends KbeeSecurityRule {

	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeSecuredMember.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="member_id")
	private SecuredMember member;

	public SecuredMember getMember() {
		return member;
	}

	public void setMember(SecuredMember member) {
		this.member = member;
	}
	
	@Override
	public boolean evaluate(Content content) {
		if (super.evaluate(content)) {
			for (SecuredMember member : getMembers(content)) {
				boolean redefined = false;
				for (List<SecuredMember> path : getPaths(member)) {
					redefined = false;
					for (SecuredMember node : path) {
						SecurityRule rule = node.getSecurityRule();
						if (rule!=null && rule.getAcl()!=null && !((Acl)rule.getAcl()).getEntries().isEmpty()) {
							redefined = true;
						}
					}
					if (!redefined) {
						return true;
					}
				}
			}
			return false;
		}
		else {
			return false;
		}
	}
	
	private List<List<SecuredMember>> getPaths(SecuredMember member) {
		List<List<SecuredMember>> paths = new ArrayList<>();
		getPaths(member, new ArrayList<SecuredMember>(), paths);
		return paths;
	}
	
	private void getPaths(SecuredMember node, List<SecuredMember> path, List<List<SecuredMember>> paths) {
		if (node.equals(getMember())) {
			paths.add(path);
			return; 
		}	
		for (DataSetMember parent : node.getParents()) {
			parent = (DataSetMember)getContentDao().reload(parent);
			if (parent instanceof SecuredMember) {
				List<SecuredMember> parentpath = new ArrayList<>();
				parentpath.addAll(path);
				parentpath.add(node);
				getPaths((SecuredMember)parent, parentpath, paths);
			}
		}
	}
	
	private List<SecuredMember> getMembers(Content content) {
		List<SecuredMember> members = new ArrayList<>();
		for (Classification classification : content.getClassification()) {
			if (classification!=null && classification.getDataSetMember().getDataSet().equals(getMember().getDataSet())) {
				DataSetMember member = classification.getDataSetMember();
				if (member instanceof SecuredMember) {
					members.add((SecuredMember)member);
				}
			}
		}
		return members;
	}
	
	protected ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return  (ContentDao) beans.getBean("contentDao");
	}
}