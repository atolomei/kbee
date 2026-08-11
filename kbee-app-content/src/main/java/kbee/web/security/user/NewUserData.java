package kbee.web.security.user;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.security.acl.Group;
import com.novamens.wicket.model.ObjectModel;

public class NewUserData implements IDetachable, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String firstName;
	private String lastName;
	private String userName;
	private String email;
	private Boolean resetPassword 			= Boolean.valueOf(false);
	private Boolean my_tasks 				= Boolean.valueOf(true);

	private Boolean auditor 				= Boolean.valueOf(true);
	private Boolean content_base 			= Boolean.valueOf(true);
	private Boolean templates 				= Boolean.valueOf(true);
	private Boolean archive 				= Boolean.valueOf(false);
	
	private Boolean datasets_elements 		= Boolean.valueOf(false);
	private Boolean datasets_elements_read 	= Boolean.valueOf(false);
	private Boolean information_model 		= Boolean.valueOf(false);
	private Boolean security 				= Boolean.valueOf(false);
	
	private Boolean domain_admin 			= Boolean.valueOf(false);
	
	
	private Boolean user_edit 			= Boolean.valueOf(true);
	private Boolean user_email 			= Boolean.valueOf(false);

	private String startPage;
	
	private Set<IModel<Group>> groupsmodels = new HashSet<IModel<Group>>();
	
	
	public Boolean get_mytasks() {
		return my_tasks;
	}

	public void set_mytasks(Boolean mytasks) {
		this.my_tasks = mytasks;
	}

	public Boolean getAuditor() {
		return auditor;
	}

	public void setAuditor(Boolean auditor) {
		this.auditor = auditor;
	}

	public Boolean getContent_base() {
		return content_base;
	}

	public void setContent_base(Boolean content_base) {
		this.content_base = content_base;
	}

	public Boolean getTemplates() {
		return templates;
	}

	public void setTemplates(Boolean templates) {
		this.templates = templates;
	}

	public Boolean getArchive() {
		return archive;
	}

	public void setArchive(Boolean archive) {
		this.archive = archive;
	}

	public Boolean getDatasets_elements() {
		return datasets_elements;
	}

	public void setDatasets_elements_read(Boolean datasets_elements_read) {
		this.datasets_elements_read = datasets_elements_read;
	}

	public Boolean getDatasets_elements_read() {
		return datasets_elements_read;
	}

	public void setDatasets_elements(Boolean datasets_elements) {
		this.datasets_elements = datasets_elements;
	}

	
	public Boolean getInformation_model() {
		return information_model;
	}

	public void setInformation_model(Boolean information_model) {
		this.information_model = information_model;
	}

	public Boolean getSecurity() {
		return security;
	}

	public void setSecurity(Boolean security) {
		this.security = security;
	}

	public Boolean getDomain_admin() {
		return domain_admin;
	}

	public void setDomain_admin(Boolean domain_admin) {
		this.domain_admin = domain_admin;
	}

	public Set<IModel<Group>> getGroupsmodels() {
		return groupsmodels;
	}

	public void setGroupsmodels(Set<IModel<Group>> groupsmodels) {
		this.groupsmodels = groupsmodels;
	}
	
	public void setLastName(String name) {
		this.lastName = name;
	}
	
	public String getLastName() {
		return this.lastName;
	}
	
	public void setFirstName(String name) {
		this.firstName = name;
	}
	
	public String getFirstName() {
		return this.firstName;
	}
	
	public void setUserName(String name) {
		this.userName = name;
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Boolean getResetPassword() {
		return this.resetPassword;
	}
	
	public void setResetPassword(Boolean value) {
		this.resetPassword = value;
	}
	
	public void addGroup(Group g) {
		groupsmodels.add(new ObjectModel<Group>(g));
	}
	
	public void setGroups(Set<Group> groups) {
		groupsmodels.clear();
		for (Group group : groups) {
			groupsmodels.add(new ObjectModel<Group>(group));
		}
	}
	
	public Set<Group> getGroups() {
		Set<Group> groups = new HashSet<Group>();
		for (IModel<Group> model : this.groupsmodels) {
			groups.add(model.getObject());
		}
		return groups;
	}

	public void setUser_email(Boolean user_email) {
		this.user_email = user_email;
	}

	public Boolean getUser_edit() {
		return user_edit;
	}

	public void setUser_edit(Boolean user_edit) {
		this.user_edit = user_edit;
	}

	public Boolean getUser_email() {
		return user_email;
	}
	
	@Override
	public void detach() {
		for (IModel<Group> model : groupsmodels) {
			model.detach();
		}
	}

	public String getStartPage() {
		return startPage;
	}

	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}
}
