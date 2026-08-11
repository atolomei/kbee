package com.novamens.content.service.workflow;

import com.novamens.security.User;
import com.novamens.security.acl.Group;

public class ComplianceWorkflowRoleData {

	public User user;
	
	public int total;
	
	public Group gaudit;
	public User uaudit;
	
	public Group b_gaudit;
	public User b_uaudit;
	
	public Group greview;
	public User ureview;

	public Group b_greview;
	public User b_ureview;

	public int total_review;
	public int total_audit;
	
	public long timestamp;
	
}
