package kbee.importer;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.iql.AttributePredicate;
import com.novamens.content.iql.ClassifierPredicate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.iql.AndExpression;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.iql.OrExpression;
import com.novamens.indexer.iql.PredicateExpression;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.content.security.KbeeDomainRole;
import com.novamens.kbee.content.security.KbeeEntityRole;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.ApiClassifier;
import kbee.api.model.IGroup;
import kbee.api.model.IResultSet;
import kbee.api.model.IRole;
import kbee.api.service.ApiService;

public class RolesImporter extends Importer {
	
	private long total = 0;
	private int updated = 0;

	public RolesImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain); 
	}
	
	@Override
	public void execute() throws ContentMgmtException  {
		int i=0;
		try {
			IResultSet<ApiProxy> roles = getServer().getRoles();
			while (roles.hasNext()) {
				ApiProxy proxy =  roles.next();
				IRole remote = getServer().getRole(proxy.getId());
				if (!remote.isCanonical()) {
					KbeeAbstractRole local = getLocal(KbeeAbstractRole.class, remote);
					if (local==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime()) || forceUpdate()) {
						if (local == null) {
							local = createRole(remote);
							setLocal(remote, local);
						}
						if (!remote.isCanonical()) {
							syncRole(remote, local);
						}
						update(local);
						updated++;
						logger.info("Role "+local.getDisplayName());
					}
					else {
						logger.info("Role "+local.getDisplayName() + " not modified");
					}
				}
				setProgress(++i);
			}
		}
		catch (Throwable e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}
	
	@Override
	public int getTotal() {
		if (total == 0) {
			total = getRemoteRoles().getSize();
		}
		return (int)total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" roles processed. ";
		result += String.valueOf(updated)+" roles updated</p>";
		return result;
	}
	
	private void syncRole(IRole remote, KbeeAbstractRole local) throws ContentMgmtException {
		local.setName(remote.getName());
		local.setAlias(remote.getAlias());
		if (remote.getCondition()!=null) {
			local.setCondition(getLocalCondition(remote.getDomain(), remote.getCondition()));
		}
		
		if (remote.getGroup()!=null) {
			IGroup igroup = getServer().getGroup(remote.getGroup().getId());
			Group group = getLocalGroup(igroup);
			local.setGroup(group);
		}
		
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
		if ("entity".equals(remote.getType())) {
			ApiClassifier scope = getServer().getClassifier(remote.getScope().getId());
			Classifier classifier = getLocal(KbeeClassifier.class, scope);
			((KbeeEntityRole)local).setClassifier(classifier);
		}
		List<Group> groups = new ArrayList<Group>();
		for (ApiProxy groupproxy : remote.getGroups()) {
			IGroup igroup = getServer().getGroup(groupproxy.getId());
			Group group = getLocalGroup(igroup);
			if (group!=null) {
				groups.add(group);	
			}
		}
		List<Permission> permissions = new ArrayList<Permission>();
		local.setGroups(groups);
		if (remote.getPermissions()!=null) {
			for (String permissionvalue : remote.getPermissions()) {
				if (permissionvalue.contains("-")) {
					String tokens[] = permissionvalue.split("-");
					ApiProcedure iprocedure = getServer().getProcedure(tokens[0]);
					if (iprocedure!=null) {
						KbeeProcedure localprocedure = getLocal(KbeeProcedure.class, iprocedure);
						if (localprocedure!=null) {
							permissionvalue = String.valueOf(localprocedure.getId()) + "-" + tokens[1]; 
							if (tokens.length>2) permissionvalue +=  "-" + tokens[2]; 
							Permission permission = KbeePermission.valueOf(permissionvalue);
							permissions.add(permission);
						}		
					}
				}
				else {
					Permission permission = KbeePermission.valueOf(permissionvalue);
					permissions.add(permission);
				}
				
			}
			local.setPermissions(permissions);
		}
		local.setGroups(groups);
		local.setDescription(remote.getDescription());
	}
	
	private String getLocalCondition(String domain, String condition) {
		Expression expression = getSessionDomain().getService(IqlService.class).getExpression(condition);
		String localcondition = getLocalCondition(domain, expression);
		return localcondition;
	}
	
	private String getLocalCondition(String domain, Expression expression) {
		if (expression instanceof PredicateExpression) {
			PredicateExpression predicateexpression = (PredicateExpression)expression;
			String argument = (String)predicateexpression.getArgument();
			if (isDigits(argument)) {
				ApiValue remote = new ApiValue();
				remote.setId(argument);
				remote.setDomain(domain);
				DataSetMember local = getLocal(KbeeDataSetMember.class, remote);
				if (local!=null) {
					argument = String.valueOf(local.getId());
				}
			}
			String predicate;
			if (predicateexpression.getPredicate() instanceof ClassifierPredicate) {
				predicate = "c"+String.valueOf(((ClassifierPredicate)predicateexpression.getPredicate()).getClassifier().getId());
			}
			else
			if (predicateexpression.getPredicate() instanceof AttributePredicate) {
				predicate = "a"+String.valueOf(((AttributePredicate)predicateexpression.getPredicate()).getAttribute().getId());
			}
			else {
				predicate = predicateexpression.getPredicate().getName();
			}
			return predicate + "(" + argument + ")";
		}
		else 
		if (expression instanceof AndExpression) {
			AndExpression andexpression = (AndExpression)expression;
			boolean leafA = andexpression.getExpressionA() instanceof PredicateExpression;
			boolean leafB = andexpression.getExpressionB() instanceof PredicateExpression;
			return (!leafA ? "(" : "") + 
				getLocalCondition(domain, andexpression.getExpressionA()) + 
				(!leafA ? ")" : "") + 
				" AND " + 
				(!leafB ? "(" : "") + 
				getLocalCondition(domain, andexpression.getExpressionB()) + 
				(!leafB ? ")" : "");
		}
		else 
		if (expression instanceof OrExpression) {
			OrExpression orexpression = (OrExpression)expression;
			boolean leafA = orexpression.getExpressionA() instanceof PredicateExpression;
			boolean leafB = orexpression.getExpressionB() instanceof PredicateExpression;
			return (!leafA ? "(" : "") + 
				getLocalCondition(domain, orexpression.getExpressionA()) + 
				(!leafA ? ")" : "") + 
				" OR " + 
				(!leafB ? "(" : "") + 
				getLocalCondition(domain, orexpression.getExpressionB()) + 
				(!leafB ? ")" : "");
		}
			
		return null;
	}
	
	private KbeeAbstractRole createRole(IRole irole) throws ContentCreationException {
		//SecurityContentMgmtService service = ServiceLocator.getService(SecurityContentMgmtService.class);
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		KbeeAbstractRole role;
		if ("entity".equals(irole.getType())) {
			role = new KbeeEntityRole();
			//role = (KbeeAbstractRole)service.createRole(EntityRole.TYPE, domain);
		}
		else {
			role = new KbeeDomainRole();
			//role = (KbeeAbstractRole)service.createRole(DomainRole.TYPE, domain);
		}
		role.setName("new role");
		role.setLastModifiedUser(getUser());
		role.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		role.setCreationOffsetDateTime(OffsetDateTime.now());
		role.setState(ObjectState.ENABLED);
		role.setDomain(domain);
		
		getSecurityDao().save(role);

		return role;
	}
	
	private Group getLocalGroup(IGroup igroup) {
		Group local = null;
		if (igroup.isCanonical()) {
			for (Group group : getSecurityDao().getGroups(getSessionDomain())) {
				if (igroup.getName().equals(group.getName())) {
					local = group;
					break;
				}
			}
		}
		if (local==null) {
			local = getLocal(KbeeGroup.class, igroup);
		}
		return local;
	}
	
	private IResultSet<ApiProxy> getRemoteRoles() {
		return getServer().getRoles();
	}
	
	private boolean isDigits(String argument) {
		for (int c=0; c<argument.length(); c++) {
			if (!Character.isDigit(argument.charAt(c))) {
				return false;
			}
		}
		return true;
	}
}