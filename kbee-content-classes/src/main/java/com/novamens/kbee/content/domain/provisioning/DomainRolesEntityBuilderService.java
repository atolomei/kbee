package com.novamens.kbee.content.domain.provisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.content.security.KbeeEntityRole;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;

/**
 * 
 * PManager
 * DManager
 * Secured
 *
 */
public class DomainRolesEntityBuilderService extends BaseDomainBuilder implements ObjectService {

	/** Logger that works synchronously in the TRX thread */
	//static private Logger txlogger = LogManager.getLogger("TxLogger");
													
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainRolesCanonicalBuilderService.class.getName());
	 
	private boolean trx = true;


	public DomainRolesEntityBuilderService() {
	}
	
	public DomainRolesEntityBuilderService(Domain domain) {
		super(domain);
	}

	
	@Transactional(propagation = Propagation.REQUIRED)
	public void build() throws ContentMgmtException, ContentCreationException {
		xbuild();
	}
	
	
	public void buildNoTrx() throws ContentMgmtException, ContentCreationException {
		this.setTrx(false);
		xbuild();
	}
	
	private void xbuild() throws ContentMgmtException, ContentCreationException {
		
		List<Group> canonical_groups = getContentSecurityDao().getCanonicalGroups(getBuildingDomain());
		List<Role> list = getContentSecurityDao().getRoles(getBuildingDomain());

		List<Role> default_roles = new ArrayList<Role>();
		
		boolean is_pmanager   = false;
		boolean is_passociate = false;
		boolean is_dmanager   = false;
		
		for (Role role: list) {
			try {
				if (role instanceof EntityRole) {
					
					if (role.getAlias()!=null) {
						if (role.getAlias().toLowerCase().trim().equals("property-manager")) { 
							is_pmanager = true;
							default_roles.add(role);
						}
						else if (role.getAlias().toLowerCase().trim().equals("department-manager")) { 
							is_dmanager  = true;
							default_roles.add(role);
						}
						
						else if (role.getAlias().toLowerCase().trim().equals("property-associate")) { 
							is_passociate  = true;
							default_roles.add(role);
						}
						
						
					}
					else if (role.getName()!=null) {
						if (role.getName().toLowerCase().trim().equals("property manager")) {
							is_pmanager = true;
							default_roles.add(role);
						}
						else if (role.getName().toLowerCase().trim().equals("department manager")) {
							is_dmanager  = true;
							default_roles.add(role);
						}
						else if (role.getName().toLowerCase().trim().equals("property-associate")) {
							is_passociate  = true;
							default_roles.add(role);
						}
					}
				}
				
			} 
			catch (Exception e ) {
				is_dmanager = true;
				is_pmanager = true;
				is_passociate  = true;
				logger.error(e);
				return;
			}
		}
		
		// Property Manager -------
		//
		if (!is_pmanager) {
			
			KbeeEntityRole  role = null;
			if (this.isTrx())		role = (KbeeEntityRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRole(EntityRole.TYPE, getBuildingDomain());
			else					role = (KbeeEntityRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRoleNoTrx(EntityRole.TYPE, getBuildingDomain());
			
			role.setAlias("property-manager");
			role.setCanonical(true);
			role.setApiEnabled(true);
			role.setDomain(getBuildingDomain());
			role.setName(getContentDao().findSystemParameterValueByKey("role.property-manager.name", "Property Manager"));
			role.setDescription(getContentDao().findSystemParameterValueByKey("role.property-manager.description", "All Documents"));
			
			List<Group> groups 	=  new ArrayList<Group>();			
			
			if (getBuildingDomain().getDomainType()!=DomainType.EXPRESS) {
				 for (Group g: canonical_groups) {
					 if 		(g.getName().equals(KbeeGlobalRole.WORKSPACE.getId())) groups.add(g);
					 //else if (g.getName().equals(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId())) groups.add(g);
					 else if (g.getName().equals(KbeeGlobalRole.MONITOR_AUDIT.getId())) groups.add(g);
					 else if (g.getName().equals(KbeeGlobalRole.BILLBOARDS.getId())) groups.add(g);
				 }
			}

			for (Library library : getBuildingDomain().getService(LibraryService.class).getLibraries()) {
				if (getBuildingDomain().getDomainType()!=DomainType.EXPRESS) {
					if (library.getKey()!=null && library.getKey().equals(getContentDao().findSystemParameterValueByKey("libraries_basic", "onesite"))) 
						groups.add(((KbeeLibrary)library).getReaders());
				}
				else {
					groups.add(((KbeeLibrary)library).getReaders());
				}
			}
			
			boolean bsave=false;
			
			if (!groups.isEmpty()) {
				role.setGroups(groups);
				logger.debug("Creating Role: property-manager");
				bsave=true;
				default_roles.add(role);
			}
			
			for (Classifier cl:getContentDao().getClassifiers(getBuildingDomain())) {
				if (cl.getUniqueName()!=null && (cl.getUniqueName().trim().toLowerCase().equals("property"))) {
					role.setClassifier(cl);
					bsave=true;
					break;
				}
			}
			
			List<Permission> ps = new ArrayList<Permission>();
			ps.add(KbeePermission.READ);
			ps.add(KbeePermission.PRIVATE);
			role.setPermissions(ps);
			
			if (bsave) {
				if (this.isTrx())	ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save property-manager");
				else				ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save property-manager");
			}
		}
		

 		
		// Property Associate -------
		//
		if (!is_passociate) {
			
			KbeeEntityRole  role = null;
			if (this.isTrx())		role = (KbeeEntityRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRole(EntityRole.TYPE, getBuildingDomain());
			else					role = (KbeeEntityRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRoleNoTrx(EntityRole.TYPE, getBuildingDomain());
			
			role.setAlias("property-associate");
			role.setCanonical(true);
			role.setApiEnabled(true);
			role.setDomain(getBuildingDomain());
			role.setName(getContentDao().findSystemParameterValueByKey("role.property-associate.name", "Property Associate"));
			role.setDescription(getContentDao().findSystemParameterValueByKey("role.property-associate.description", "Exclude sensitive documents"));
								
			List<Group> groups 	=  new ArrayList<Group>();			
			
			if (getBuildingDomain().getDomainType()!=DomainType.EXPRESS) {
				for (Group g: canonical_groups) {
					if (g.getName().equals(KbeeGlobalRole.WORKSPACE.getId())) groups.add(g);
					//else if (g.getName().equals(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId())) groups.add(g);
					else if (g.getName().equals(KbeeGlobalRole.MONITOR_AUDIT.getId())) groups.add(g);
					else if (g.getName().equals(KbeeGlobalRole.BILLBOARDS.getId())) groups.add(g);
				}
			}
			
			for (Library library : getBuildingDomain().getService(LibraryService.class).getLibraries()) {
				if (getBuildingDomain().getDomainType()!=DomainType.EXPRESS) {
					if (library.getKey()!=null && library.getKey().equals(getContentDao().findSystemParameterValueByKey("libraries_basic", "onesite"))) 
						groups.add(((KbeeLibrary)library).getReaders());
				}
				else {
					groups.add(((KbeeLibrary)library).getReaders());
				}
			}

			boolean bsave=false;
			
			if (!groups.isEmpty()) {
				role.setGroups(groups);
				logger.debug("Creating Role: property-associate");
				bsave=true;
				default_roles.add(role);
			}
			
			for (Classifier cl:getContentDao().getClassifiers(getBuildingDomain())) {
				if (cl.getUniqueName()!=null && (cl.getUniqueName().trim().toLowerCase().equals("property"))) {
					role.setClassifier(cl);
					bsave=true;
					break;
				}
			}
			
			List<Permission> ps = new ArrayList<Permission>();
			ps.add(KbeePermission.READ);
			role.setPermissions(ps);
			
			String secured_predicate=getContentDao().findSystemParameterValueByKey("classifier.securedaccess.predicate", "securedaccess").toLowerCase().trim();
			String secured_value=getContentDao().findSystemParameterValueByKey("classifier.securedaccess.value", "Public").trim();
			
			String pred=secured_predicate;
			String dms=secured_value;
			
			DataSet dataset = null;
			List<Classifier> lc = getContentDao().getClassifiers(getBuildingDomain());
			for (Classifier c: lc) {
				if (c.getPredicate().toLowerCase().equals(pred)) {
					dataset = c.getDataSet();
					break;
				}
			}
			if (dataset!=null) {
				DataSetMember dm = getContentDao().findMemberByValue(dataset, dms);
				if (dm!=null) {
					String con= pred + "("+dm.getId().toString()+ ")";
					role.setCondition(con);
					String str_condition = "<span class=\"predicate\"> "+pred+"</span><span class=\"iql-group-start\"> ( </span> <span class=\"iql-value\"> "+ dm.getName() +"</span> <span class=\"iql-group-end\"> ) </span>";
					role.setDisplayCondition(str_condition);
					bsave=true;
				}
			}
			
			if (bsave) {
				if (this.isTrx())	ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save property-associate");
				else				ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save property-associate");
			}
		}

		
		
		// Department Manager -------
		//
		if (!is_dmanager) {

			KbeeEntityRole role = null;
			
			if (isTrx())	 role = (KbeeEntityRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRole(EntityRole.TYPE, getBuildingDomain());
			else			 role = (KbeeEntityRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRoleNoTrx(EntityRole.TYPE, getBuildingDomain());
				
			role.setAlias("department-manager");
			role.setCanonical(true);
			role.setApiEnabled(true);
			role.setDomain(getBuildingDomain());

			role.setName("Department Manager");
			
			role.setName(getContentDao().findSystemParameterValueByKey("role.department-manager.name", "Department Manager"));
			role.setDescription(getContentDao().findSystemParameterValueByKey("role.department-manager.description", ""));

			List<Group> groups = new ArrayList<Group>();

			if (getBuildingDomain().getDomainType()!=DomainType.EXPRESS) {
				for (Group g: canonical_groups) {
					if (g.getName().equals(KbeeGlobalRole.WORKSPACE.getId())) groups.add(g);
					//else if (g.getName().equals(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId())) groups.add(g);
					else if (g.getName().equals(KbeeGlobalRole.MONITOR_AUDIT.getId())) groups.add(g);
					else if (g.getName().equals(KbeeGlobalRole.BILLBOARDS.getId())) groups.add(g);
					else if (g.getName().equals(KbeeGlobalRole.ARCHIVE.getId())) groups.add(g);
					else if (g.getName().equals(KbeeGlobalRole.DATASET_VALUES_READ.getId())) groups.add(g);
				}
			}
			
			for (Library library : getBuildingDomain().getService(LibraryService.class).getLibraries()) {
				if (getBuildingDomain().getDomainType()!=DomainType.EXPRESS) {
					if (library.getKey()!=null && library.getKey().equals(getContentDao().findSystemParameterValueByKey("libraries_basic", "onesite"))) 
						groups.add(((KbeeLibrary)library).getReaders());
				}
				else {
					groups.add(((KbeeLibrary)library).getReaders());
				}
			}
			
			boolean bsave=false;
			
			if (!groups.isEmpty()) {
				role.setGroups(groups);
				bsave=true;
				default_roles.add(role);
			}
			
			for (Classifier cl:getContentDao().getClassifiers(getBuildingDomain())) {
				if (cl.getName()!=null && (cl.getName().toLowerCase().trim().equals("department"))) {
					role.setClassifier(cl);
					bsave=true;
					break;
				}
			}

			List<Permission> ps = new ArrayList<Permission>();
			
			ps.add(KbeePermission.READ);
			ps.add(KbeePermission.PRIVATE);
			role.setPermissions(ps);


 			logger.debug("Domain "+ getBuildingDomain().getName() + " Saving Role department-manager ");
			if (bsave) {
				if (isTrx())			ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save department-manager");
				else		  		    ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save department-manager");
			}
		}
	}
	

	public boolean isTrx() {
		return trx;
	}


	public void setTrx(boolean trx) {
		this.trx = trx;
	}
}
