package com.novamens.kbee.content.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.entity.Person;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.EntitySet;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.model.MemberRole;
import com.novamens.content.model.UserSet;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.dao.SecurityDao;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.content.security.KbeeEntityRole;
import com.novamens.kbee.content.security.KbeeSecurityRule;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.template.KbeeValueTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.logging.SecurityUpdateEvent;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue(value="6")
public class KbeeEntityMember extends KbeeValueMember implements EntityMember {
	
	static Logger logger = LogManager.getLogger(KbeeEntityMember.class.getName());
	static Logger txLogger = LogManager.getLogger("TxLogger");	
	
	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeSecurityRule.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="RULE_ID")
	private SecurityRule securityRule;
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeMemberRole.class)
	@JoinColumn(name = "ENTITY_ID", nullable=false) 
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="entity")
	List<MemberRole> roles = new ArrayList<MemberRole>();
	
	@OneToOne(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeGroup.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="GROUP_ID")
	private Group group;
	
	public KbeeEntityMember() {
		super();
	}
	
	public KbeeEntityMember(EntitySet ds) {
		super(ds);
	}
	
	public KbeeEntityMember(UserSet ds) {
		super(ds);
	}
	
	@Override
	public void setRole(Role role, Person person) {
		Assert.isInstanceOf(EntityRole.class, role, "no entity role");
		User user = getUser(person);
		if (user==null) return;
		MemberRole memberRole = getMemberRole(role);
		SecurityRule securityRule = getSecurityRule(memberRole, person);
		updatePermissions(memberRole, securityRule, person);
	}
	
	@Override
	public void removeRole(Role role, Person person) {
		Assert.isInstanceOf(EntityRole.class, role, "no entity role");
		User user = getUser(person);
		if (user==null) return;
		MemberRole memberRole = getMemberRole(role);
		if (memberRole!=null && memberRole.getSecurityRule()!=null) {
			removePermissions(memberRole.getSecurityRule().getAcl(), user);
		}
		else {
			if (getSecurityRule()!=null) {
				KbeeAcl acl = (KbeeAcl)getSecurityRule().getAcl();
				boolean updated0=false, updated1=false, updated2=false;
				if (getPermissions(person, true).isEmpty() && getPermissions(person, false).isEmpty()) {
					updated1 = removePermissions(acl, user);
				}
				else {
					updated1 = updatePermissions(acl, user, getPermissions(person, false), false);
					updated2 = updatePermissions(acl, user, getPermissions(person, true), true);
				}
				if (updated0 || updated1 || updated2) {
					getSecurityRule().setLastModifiedOffsetDateTime(OffsetDateTime.now());
					getSecurityRule().setLastModifiedUser(getSessionUser());
					txLogger.info(new SecurityUpdateEvent(getSecurityRule(), "Remove permissions to "+person.getDisplayName()));
				}
			 }
		}
	}
	
	public SecurityRule getSecurityRule() {
		return securityRule;
	}
	
	public void setSecurityRule(SecurityRule rule) {
		this.securityRule = rule; 
	}
	
	public List<MemberRole> getRoles() {
		return roles;
	}
	
	public String getDisplayName() {
		return (String)getValue();
	}

	@Override
	public Object getValue() {
		return super.getStrValue();
	}
	
	public Group getGroup() {
		return group;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	public Group getGroup(Role role) {
		for (MemberRole memberRole : getRoles()) {
			if (memberRole.getRole().equals(role)) {
				return ((KbeeMemberRole)memberRole).getGroup();
			}
		}
		return null;
	}
	
	public MemberRole getMemberRole(Role role) {
		KbeeMemberRole memberRole = null;
		for (MemberRole existingrole : getRoles()) {
			if (existingrole.getRole().equals(role)) {
				memberRole = (KbeeMemberRole)existingrole;
				break;
			}
		}
		if (memberRole == null) {
			memberRole = new KbeeMemberRole();
			memberRole.setRole(role);
			memberRole.setEntity(this);
			getRoles().add(memberRole);
		}	
		return memberRole;
	}
	
	protected SecurityRule getSecurityRule(MemberRole memberRole, Person person) {
		
		SecurityRule securityRule;
		KbeeAbstractRole role = (KbeeAbstractRole)memberRole.getRole();
		role = (KbeeAbstractRole)Proxy.Unproxy(role);

		if ((role.getCondition()==null || "".equals(role.getCondition()) && 
			(getSecurityRule()==null || getCondition(role).equals(getSecurityRule().getCondition())))) {
			
			if (memberRole.getSecurityRule()!=null) {
				((KbeeMemberRole)memberRole).setSecurityRule(null);
			}
			if (getSecurityRule()==null) {
				securityRule = createSecurityRule(role);
				setSecurityRule(securityRule);
			}
			else {
				securityRule = getSecurityRule();
			}
			if (!securityRule.getName().equals(getName())) {
				securityRule.setName(getName());
			}
			if (!securityRule.getDisplayCondition().equals(getDisplayCondition(role))) {
				securityRule.setDisplayCondition(getDisplayCondition(role));
			}
			if (!securityRule.getDescription().equals(getSecurityRuleDescription(role))) {
				securityRule.setDescription(getSecurityRuleDescription(role));
			}
		}
		else {
			securityRule = memberRole.getSecurityRule();
			if (securityRule == null) {
				securityRule = createSecurityRule(role);
				((KbeeMemberRole)memberRole).setSecurityRule(securityRule);
			}
			else {
				String condition = getCondition(role);
				if (!condition.equals(securityRule.getCondition())) {
					securityRule.setCondition(condition);
				}
			}
			String rulename = role.getName() + " - " + getName();
			if (!rulename.equals(securityRule.getName())) {
				securityRule.setName(rulename);
			}
			if (!securityRule.getDisplayCondition().equals(getDisplayCondition(role))) {
				securityRule.setDisplayCondition(getDisplayCondition(role));
			}
			if (!securityRule.getDescription().equals(getSecurityRuleDescription(role))) {
				securityRule.setDescription(getSecurityRuleDescription(role));
			}
			if (getSecurityRule()!=null) {
				User user = getUser(person);
				Group group = getGroup(memberRole);
				KbeeAcl acl = (KbeeAcl)getSecurityRule().getAcl();
				boolean updated0=false, updated1=false, updated2=false;
				if (getPermissions(person, true).isEmpty() && getPermissions(person, false).isEmpty()) {
					updated1 = removePermissions(acl, user);
				}
				else {
					updated1 = updatePermissions(acl, user, getPermissions(person, false), false);
					updated2 = updatePermissions(acl, user, getPermissions(person, true), true);
				}
				updated0 = removePermissions(acl, group);
				if (updated0 || updated1 || updated2) {
					getSecurityRule().setLastModifiedOffsetDateTime(OffsetDateTime.now());
					getSecurityRule().setLastModifiedUser(getSessionUser());
				}
			}
		}
		
		return securityRule;
	}
	
	public Group getGroup(MemberRole memberRole) {
		Group group, entityGroup;
		
		KbeeMemberRole memberrole =  (KbeeMemberRole)memberRole;
		
		if (memberrole.getGroup()==null) {
			group = createGroup(memberRole);
			memberrole.setGroup(group);
		}
		else {
			group = memberrole.getGroup();
			if (!group.getName().equals(getGroupName(memberRole))) {
				group.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				group.setName(getGroupName(memberRole));
			}
			if (group.getDescription()==null || !group.getDescription().equals(getGroupDescription(memberRole))) {
				group.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				group.setDescription(getGroupDescription(memberRole));
			}
		}
		
		getRoleGroup(memberrole);
		
		Role role = memberRole.getRole();
		if (role.isEntity()) {
			KbeeEntityMember entity = (KbeeEntityMember)memberRole.getEntity();
			EntitySet entitySet = (EntitySet)Proxy.Unproxy(entity.getDataSet());
			if (entitySet.hasEntityGroup()) {
				if (entity.getGroup()==null) {
					entityGroup = createGroup(entity);
					entity.setGroup(entityGroup);
				}
				else {
					entityGroup = entity.getGroup();
					if (!entityGroup.getName().equals(getGroupName(entity))) {
						entityGroup.setLastModifiedOffsetDateTime(OffsetDateTime.now());
						entityGroup.setName(getGroupName(entity));
					}
					if (entityGroup.getDescription()==null || !entityGroup.getDescription().equals(getGroupDescription(entity))) {
						entityGroup.setLastModifiedOffsetDateTime(OffsetDateTime.now());
						entityGroup.setName(getGroupName(entity));
					}
				}
			}
		}

		return group;
	}
	
	protected Group getRoleGroup(MemberRole memberRole) {
		Group rolegroup;
		
		KbeeMemberRole memberrole =  (KbeeMemberRole)memberRole;
		
		if (((KbeeAbstractRole)memberrole.getRole()).getGroup()==null) {
			rolegroup = createGroup(memberrole.getRole());
			((KbeeAbstractRole)memberrole.getRole()).setGroup(rolegroup);
		}	
		else {
			rolegroup = ((KbeeAbstractRole)memberrole.getRole()).getGroup();
			if (!rolegroup.getName().equals(getGroupName(memberrole.getRole()))) {
				rolegroup.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				rolegroup.setName(getGroupName(memberrole.getRole()));
			}
			if (rolegroup.getDescription()==null || !rolegroup.getDescription().equals(getGroupDescription(memberrole.getRole()))) {
				rolegroup.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				rolegroup.setDescription(getGroupDescription(memberrole.getRole()));
			}
		}
		
		return rolegroup;
	}
	
	public Group createGroup(MemberRole role) {
		KbeeGroup group = new KbeeGroup();
		
		group.setDerived(true);
		group.setDescription(getGroupDescription(role));
		group.setDomain(getDomain());
		group.setCreationOffsetDateTime(OffsetDateTime.now());
		group.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		group.setLastModifiedUser(getSessionUser());
		
		group.setName(getGroupName(role));
		
		// lo siguiente se hace por que el cascade type del principal del acl entry no esta en all
		// queda pendiente probar ese cambio para podes sacar estas lineas
		
		try {
			SecurityDao dao = (SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
			dao.save(group);
		}
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
		
		return group;
	}
	
	public Group createGroup(EntityMember entity) {
		KbeeGroup group = new KbeeGroup();
		
		group.setDerived(true);
		group.setDescription(getGroupDescription(entity));
		group.setDomain(getDomain());
		group.setCreationOffsetDateTime(OffsetDateTime.now());
		group.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		group.setLastModifiedUser(getSessionUser());
		
		group.setName(getGroupName(entity));
		
		try {
			SecurityDao dao = (SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
			dao.save(group);
		}
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
		
		return group;
	}
	
	protected Group createGroup(Role role) {
		KbeeGroup group = new KbeeGroup();
		
		group.setDerived(true);
		group.setDescription(getGroupDescription(role));
		group.setDomain(getDomain());
		group.setCreationOffsetDateTime(OffsetDateTime.now());
		group.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		group.setLastModifiedUser(getSessionUser());
		
		group.setName(getGroupName(role));
		
		try {
			SecurityDao dao = (SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
			dao.save(group);
		}
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
		
		return group;
	}
	
	public String getGroupName(MemberRole memberRole) {
		String name = "";
		
		Role role = (Role)Proxy.Unproxy(memberRole.getRole());
		
		if (role instanceof EntityRole) {
			String templatesource = ((KbeeEntityRole)role).getPrincipalNameTemplate();
			if (templatesource!=null) {
				KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
				model.setModel("name", role.getName());
				model.setModel("entity", new KbeeValueTemplateModel(this));
				KbeeTextTemplate template = new KbeeTextTemplate(templatesource);
				try {
					name = template.process(model);
				}
				catch (Exception e) {
					
				}
			}
		}
		
		if (name==null || "".equals(name)) {
			name += getName()!=null ? getName() : "-";
			if (role.getName()!=null) {
				name += " (" +  role.getName().trim() + ")";
			}
		}
		
		return name;
	}
	
	public String getGroupName(EntityMember entity) {
		String name = entity.getDisplayName();
		try {
			ExtractionRule rule =  entity.getDataSet().getSublineRule();
			if (rule!=null) {
				name += " - "  + (String)rule.extract(entity);
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
		return name;
	}
	
	public String getGroupName(Role role) {
		String name = null;
		
		role = (Role)Proxy.Unproxy(role);
		
		Locale locale;
		if (getSessionUser()!=null)
			locale = getSessionUser().getLocale();
		else
			locale = Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeeEntityMember.this.getClass().getName(), locale);
		name =  String.format(res.getString("role-name"), role.getName());
		return name;
	}
	
	protected SecurityRule createSecurityRule(Role role) {
		
		KbeeSecurityRule rule = new KbeeSecurityRule();
		
		rule.setName(getName());
		
		rule.setDerived(true);
		rule.setDomain(getDomain());
		rule.setCreationOffsetDateTime(OffsetDateTime.now());
		rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		rule.setLastModifiedUser(getSessionUser());
		
		rule.setDescription(getSecurityRuleDescription(role));
		
		rule.setType(SecurityRule.RULE_WIZARD_IQL);
		rule.setCondition(getCondition(role));
		rule.setDisplayCondition(getDisplayCondition(role));

		KbeeAcl acl = new KbeeAcl();
		acl.setCreationOffsetDateTime(OffsetDateTime.now());
		acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		acl.setLastModifiedUser(getSessionUser());
		rule.setAcl(acl);
		
		return rule;
	}
	
	protected String getSecurityRuleDescription(Role role) {
		Locale locale;
		if (getSessionUser()!=null)
			locale = getSessionUser().getLocale();
		else
			locale = Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeeEntityMember.this.getClass().getName(), locale);
		String description = "";
		if (role.getCondition()==null || "".equals(role.getCondition())) {
			description = String.format(res.getString("entityrule-description"), getDisplayName());
		}
		else {
			description = String.format(res.getString("entityrulecondition-description"), role.getName(), getDisplayName());
		}
		return description;
	}
	
	protected String getGroupDescription(MemberRole memberRole) {
		Locale locale;
		
		String description = null;
		Role role = (Role)Proxy.Unproxy(memberRole.getRole());
		
		
		if (description == null) {
			if (getSessionUser()!=null)
				locale = getSessionUser().getLocale();
			else
				locale = Locale.getDefault();
			ResourceBundle res = ResourceBundle.getBundle(KbeeEntityMember.this.getClass().getName(), locale);
			description = String.format(res.getString("entityrolegroup-description"), role.getName(), getDisplayName());
		}
		return description;
	}
	
	protected String getGroupDescription(EntityMember entity) {
		return "";
	}
	
	protected String getGroupDescription(Role role) {
		Locale locale;
		if (getSessionUser()!=null)
			locale = getSessionUser().getLocale();
		else
			locale = Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeeEntityMember.this.getClass().getName(), locale);
		String description = "";
		description = String.format(res.getString("rolegroup-description"), role.getName());
		return description;
	}
	
	protected String getCondition(Role role) {
		StringBuilder condition = new StringBuilder();
		condition.append("c"+String.valueOf(((KbeeEntityRole)role).getClassifier().getId())+"("+String.valueOf(getId())+")");
		if (role.getCondition()!=null && !"".equals(role.getCondition()))
			condition.append(" and ("+role.getCondition()+")");
		if (!enable(role, KbeeGlobalRole.MONITOR_AUDIT)) {
			condition.append(" and isHead(true)");
		}
		return condition.toString();
	}
	
	protected boolean enable(Role role, KbeeGlobalRole globalrole) {
		for (Group group : role.getGroups()) {
			if (group.getName().equals(globalrole.getId())) {
				return true;
			}
		}
		return false;
	}
	
	protected String getDisplayCondition(Role role) {
		StringBuilder condition = new StringBuilder();
				
		String predicate = ((KbeeEntityRole)role).getClassifier().getPredicate();
		condition.append("<span class= \"predicate\" >" + predicate+"</span>");
		condition.append("<span class= \"iql-group-start\"> ( </span> ");
		condition.append("<span class= \"iql-value\" >"+ getDisplayName()+"</span> ");
		condition.append("<span class= \"iql-group-end\"> ) </span> ");
		if (((KbeeEntityRole)role).getCondition()!=null && !"".equals(((KbeeEntityRole)role).getCondition())) {
			condition.append("<span class= \"logical-operator\" >" +" and "+"</span>");
			condition.append(((KbeeEntityRole)role).getDisplayCondition());
		}
		return condition.toString();
	}
	
	protected void updatePermissions(MemberRole memberRole, SecurityRule securityRule, Person person) {
		KbeeAcl acl = (KbeeAcl)securityRule.getAcl();
		
		User user = getUser(person);
		if (user==null) return;
		
		Group group = getGroup(memberRole);
		Group rolegroup = getRoleGroup(memberRole);
			
		boolean updated1 = updatePermissions(acl, group, user, getPermissions(memberRole, false), false);
		boolean updated2 = updatePermissions(acl, group, user, getPermissions(memberRole, true), true);
		boolean updated3 = removePermissions(acl, rolegroup);

		
		if (updated1 || updated2 || updated3) {
			acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			acl.setLastModifiedUser(getSessionUser());
			securityRule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			securityRule.setLastModifiedUser(getSessionUser());
			txLogger.info(new SecurityUpdateEvent(securityRule, "Update permissions to "+person.getDisplayName()));
		}
	}
	
	protected List<Permission> getPermissions(MemberRole memberRole, Person person, boolean negative) {
		List<Permission> permissions;
		
		KbeeAbstractRole kbeerole = (KbeeAbstractRole)memberRole.getRole();
		
		if (memberRole.getSecurityRule()==null) {
			permissions = getPermissions(person, negative);
			permissions.addAll(negative ? kbeerole.getNegativePermissions() : kbeerole.getPermissions()); 
		}
		else {
			permissions = negative ? kbeerole.getNegativePermissions() : kbeerole.getPermissions(); 
		}
		
		return permissions;
	}
	
	protected List<Permission> getPermissions(MemberRole memberRole, boolean negative) {
		List<Permission> permissions;
		
		KbeeAbstractRole kbeerole = (KbeeAbstractRole)memberRole.getRole();
		
		permissions = negative ? kbeerole.getNegativePermissions() : kbeerole.getPermissions(); 
		
		return permissions;
	}
	
	protected boolean updatePermissions(Acl acl, User user, List<Permission> permissions, boolean negative) {
		boolean updated = false;
		KbeeAcl kbeeacl = (KbeeAcl)acl;
		
		KbeeAclEntry aclEntry = null;
		for (AclEntry entry : kbeeacl.getEntries()) {
			if (entry.getPrincipal().equals(user) && entry.isNegative()==negative) {
				aclEntry = (KbeeAclEntry)entry;
				break;
			}
		}
		
		if (aclEntry == null) {
			if (!permissions.isEmpty()) {
				aclEntry = new KbeeAclEntry(acl, user, negative);
				kbeeacl.addEntry(getSessionUser(), aclEntry);
			}
		}
		else {
			if (permissions.isEmpty()) {
				kbeeacl.removeEntry(getSessionUser(), aclEntry);
				aclEntry = null;
			}
		}
		
		if (aclEntry!=null) {
			if (!equals(permissions, aclEntry.getPermissions())) {
				aclEntry.setPermissions(permissions);
				aclEntry.setPrincipal(user);
				updated = true;
			}
		}
		
		return updated;
	}
	
	protected boolean updatePermissions(Acl acl, Group group, User user, List<Permission> permissions, boolean negative) {
		boolean updated = false;
		KbeeAcl kbeeacl = (KbeeAcl)acl;

		KbeeAclEntry userEntry = null;
		for (AclEntry entry : kbeeacl.getEntries()) {
			if (entry.getPrincipal().equals(user) && entry.isNegative()==negative) {
				userEntry = (KbeeAclEntry)entry;
				break;
			}
		}
		
		if (userEntry != null) {
			kbeeacl.removeEntry(getSessionUser(), userEntry);
			updated = true;
		}
		
		KbeeAclEntry groupEntry = null;
		for (AclEntry entry : kbeeacl.getEntries()) {
			if (entry.getPrincipal().equals(group) && entry.isNegative()==negative) {
				groupEntry = (KbeeAclEntry)entry;
				break;
			}
		}
		
		if (groupEntry == null) {
			if (!permissions.isEmpty()) {
				groupEntry = new KbeeAclEntry(acl, group, negative);
				kbeeacl.addEntry(getSessionUser(), groupEntry);
			}
		}
		else {
			if (permissions.isEmpty()) {
				kbeeacl.removeEntry(getSessionUser(), groupEntry);
				updated = true;
				groupEntry = null;
			}
		}
		
		if (groupEntry!=null) {
			if (!equals(permissions, groupEntry.getPermissions())) {
				groupEntry.setPermissions(permissions);
				groupEntry.setPrincipal(group);
				updated = true;
			}
		}
		
		return updated;
	}
	
	protected boolean removePermissions(Acl acl, User user) {
		boolean updated = false;
		KbeeAcl kbeeacl = (KbeeAcl)acl;
		
		KbeeAclEntry aclEntry = null;
		
		boolean found = true;
		while (found) {
			found = false;
			for (AclEntry entry : kbeeacl.getEntries()) {
				if (entry.getPrincipal().equals(user)) {
					found = true;
					aclEntry = (KbeeAclEntry)entry;
					break;
				}
			}
			if (aclEntry!=null) {
				kbeeacl.removeEntry(getSessionUser(), aclEntry);
				updated = true;
			}
		}
		
		if (updated) {
			kbeeacl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			kbeeacl.setLastModifiedUser(getSessionUser());
		}
		
		return updated;
	}
	
	protected boolean removePermissions(Acl acl, Group group) {
		boolean updated = false;
		KbeeAcl kbeeacl = (KbeeAcl)acl;
		
		KbeeAclEntry aclEntry = null;
		
		boolean found = true;
		while (found) {
			found = false;
			for (AclEntry entry : kbeeacl.getEntries()) {
				if (entry.getPrincipal().equals(group)) {
					found = true;
					aclEntry = (KbeeAclEntry)entry;
					break;
				}
			}
			if (aclEntry!=null) {
				kbeeacl.removeEntry(getSessionUser(), aclEntry);
				updated = true;
			}
		}
		
		if (updated) {
			kbeeacl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			kbeeacl.setLastModifiedUser(getSessionUser());
		}
		
		return updated;
	}
	
	protected User getUser(Person person) {
		if (person==null) {
			logger.error("person is null | KbeeEntityMember id: " + String.valueOf(getId()));
			return null;
		}
		
		UserProfile userprofile = person.getProfile(UserProfile.class);
		
		if (userprofile==null) { 
			logger.error("userProfile is null | KbeeEntityMember id: " + String.valueOf(getId()));
			return null;
		}
		
		return userprofile.getUser();
	}
	
	/**
	 * La suma de los permisos de los distintos roles que pueda cumplir la persona sobre la entidad.
	 * esto es necesario para roles sin condidciones porque se suman los permisos en una unica acl de la entidad.   
	*/ 
	protected List<Permission> getPermissions(Person person, boolean negative) {
		List<Permission> permissions = new ArrayList<Permission>();
		UserProfile userprofile = person.getProfile(UserProfile.class);
		for (UserRole userRole : userprofile.getRoles()) {
			if (this.equals(userRole.getEntity())) {
				KbeeAbstractRole kbeerole = (KbeeAbstractRole)userRole.getRole();
				MemberRole memberRole = getMemberRole(kbeerole);
				if ((kbeerole.getCondition()==null || "".equals(kbeerole.getCondition())) && ((KbeeMemberRole)memberRole).getGroup()==null) {
					List<Permission>  rolepermissions = negative ? kbeerole.getNegativePermissions() : kbeerole.getPermissions(); 
					permissions.addAll(rolepermissions);
				}	
			}
		}
		return permissions;
	}
	
	private boolean equals(List<Permission> permissions1, List<Permission> permissions2) {
		if (permissions1.size()!=permissions2.size())
			return false;
		for (Permission permission1 : permissions1) {
			boolean found = false;
			for (Permission permission2 : permissions2) {
				if (permission1.toString().equals(permission2.toString())) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
}
