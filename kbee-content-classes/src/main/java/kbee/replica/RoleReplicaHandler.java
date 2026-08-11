package kbee.replica;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.iql.AttributePredicate;
import com.novamens.content.iql.ClassifierPredicate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
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
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;

import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.ApiClassifier;
import kbee.api.model.IGroup;
import kbee.api.model.IRole;

public class RoleReplicaHandler extends AbstractReplicaHandler<IRole, KbeeAbstractRole> {

	public RoleReplicaHandler(Replica replica, IRole irole) {
		super(replica, irole);
	}
	
	@Override
	protected void replicateIn(KbeeAbstractRole local) throws ReplicaException {
		IRole remote = getObject();
		local.setName(remote.getName());
		local.setAlias(remote.getAlias());
		if (remote.getCondition()!=null) {
			local.setCondition(getLocalCondition(remote.getDomain(), remote.getCondition()));
		}
		
		if (remote.getGroup()!=null) {
			IGroup igroup = getReplicaApi().getGroup(remote.getGroup().getId());
			Group group = getLocalGroup(igroup);
//			if (group==null) {
//				throw new ReplicaException("no group");
//			}
			local.setGroup(group);
		}
		
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
		if ("entity".equals(remote.getType())) {
			ApiClassifier scope = getReplicaApi().getClassifier(remote.getScope().getId());
			Classifier classifier = getLocal(KbeeClassifier.class, scope);
			if (classifier==null) throw new ReplicaException("no classifier");
			((KbeeEntityRole)local).setClassifier(classifier);
		}
		List<Group> groups = new ArrayList<Group>();
		for (ApiProxy groupproxy : remote.getGroups()) {
			IGroup igroup = getReplicaApi().getGroup(groupproxy.getId());
			Group group = getLocalGroup(igroup);
			//if (group==null) throw new ReplicaException("no group");
			if (group!=null) {
				groups.add(group);	
			}
		}
		local.setGroups(groups);
		List<Permission> permissions = new ArrayList<Permission>();
		if (remote.getPermissions()!=null) {
			for (String permissionvalue : remote.getPermissions()) {
				if (permissionvalue.contains("-")) {
					String tokens[] = permissionvalue.split("-");
					ApiProcedure iprocedure = getReplicaApi().getProcedure(tokens[0]);
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
		local.setDescription(remote.getDescription());
	}

	@Override
	protected KbeeAbstractRole createLocal() {
		IRole remote = getObject();
		Domain domain = getSessionDomain();
		KbeeAbstractRole role;
		if ("entity".equals(remote.getType())) {
			role = new KbeeEntityRole();
		}
		else {
			role = new KbeeDomainRole();
		}
		role.setName("new role");
		role.setLastModifiedUser(getSessionUser());
		role.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		role.setCreationOffsetDateTime(OffsetDateTime.now());
		role.setState(ObjectState.ENABLED);
		role.setDomain(domain);
		
		getSecurityDao().save(role);

		return role;
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
	
	private boolean isDigits(String argument) {
		for (int c=0; c<argument.length(); c++) {
			if (!Character.isDigit(argument.charAt(c))) {
				return false;
			}
		}
		return true;
	}
}