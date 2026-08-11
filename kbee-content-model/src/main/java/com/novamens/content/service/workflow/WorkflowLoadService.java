package com.novamens.content.service.workflow;

import java.util.Map;

import com.novamens.security.User;
import com.novamens.service.BusinessObjectService;


/**
 * UserWorkLoad
 */
public interface WorkflowLoadService extends BusinessObjectService {
	
	public UserWorkLoadData getUserWorkLoad(User user);
	public Map<String, Integer> getTaskTypesWorkLoad(User user);
	
	public ComplianceWorkflowRoleData getComplianceWorkflowRoleData(User user);
	
	
	public void evict();
	public String  getWindsorDomainName();
	

}
