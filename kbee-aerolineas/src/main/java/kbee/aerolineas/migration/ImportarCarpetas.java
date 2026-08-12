package kbee.aerolineas.migration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.util.file.File;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.EntitySet;
import com.novamens.content.model.SecuredMember;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.DOMObjectService;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.model.KbeeEntityMember;
import com.novamens.kbee.content.model.KbeeMemberRole;
import com.novamens.kbee.content.model.KbeeSecuredMember;
import com.novamens.kbee.content.repository.MemberRepository;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.repository.DomRepository;
import com.novamens.security.Principal;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.transaction.Transaction;

import kbee.api.model.ApiProxy;
import kbee.api.model.IValue2;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.util.PropertiesFactory;
import kbee.util.logging.Logger;


public class ImportarCarpetas extends AsyncCommand {
	
	private static Logger logger = new Logger(LogManager.getLogger(ImportarCarpetas.class.getName()));
	 
	private static String[] fields = { "id", "documento", "tipo", "descripcion", "empresa", "carpetas", "revision", "revision-fecha", "creaci on-fecha", "autor", "modificacion", "parte", "partes", 
	 	"vigencia-desde", "vigencia-hasta", "version", "permisos", "files",  "index", "relations"
	};
	
	private List<Map<String, String>> rows = null;
	private Map<String, Map<String, String>> rows2 = null;
	private Map<String, Map<String, String>> extra = null;
	
	private int i = 0;
	private int doc = 0;
	private DataSet dataSet;
    private DataSet dataSetDepartamento = null;
	private String logsPath;

	
    @Autowired
    private DomRepository<DataSetMember> memberRepository;
	
	public ImportarCarpetas() {
		setName("Importar Carpetas AA");
	}
	
	public static Map<String, String> values(String... values) {
		Map<String, String> map = new HashMap<String, String>();
		String key = null, value = null;
		for (int v=0; v<values.length; v++) {
			if (key==null) {
				key = values[v];
			}
			else {
				value = values[v];
				map.put(key, value);
				key=null;
			}
		}
		return map;
	}

	public void executeAsync() {
		
		
		try {
			
			String lote = (String)getParameter("lote");
			
			if (lote==null) {
				setResultComments("el parámetro lote es obligatorio");
				throw new RuntimeException("sin lote");
			}
			
	 		logsPath = PropertiesFactory.getInstance("kbee").getProperties().getProperty("aerolineas.logs", "logs").trim();

			setLogger(getLoggerName(lote));
		
			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@aerolineas");
			
			for (i=0; i<getRows().size(); i++) {
				
				Map<String, String> row = getRows().get(i);
				
				IValue2 value = buildValue(row, new ArrayList<String>());
				if (value!=null  && "LV-ZZD".equals(value.getDisplayName())) {
					System.out.print(value);;
				}
				if (value!=null) {
					update(value);
				}
			}
			
			for (i=0; i<getRows().size(); i++) {
				
				Map<String, String> row = getRows().get(i);
	
				IValue2 value = buildParents(row, new ArrayList<String>());
				if (value!=null  && "LV-ZZD".equals(value.getDisplayName())) {
					System.out.print(value);;
				}
				if (value!=null) {
					update(value);
				}
			}
			
 			end();
			rows = null;
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			getLogger().error(e);
			stop();
		}
	}
	
	@Override
	public double getProgress() {
		return (double) i/(double) getTotalItems() * 100;
	}

	@Override
	public long getTotalItems() {
		return getRows().size();
	}
	
	@Override
	public long getTotalItemsProcessed() {
		return i;
	}
	
	private IValue2 buildValue(Map<String,String> row, List<String> ids) {
		
		IValue2 value = new IValue2();
		
		String id = row.get("id");
		
		if (ids.contains(id)) {
			getLogger().error(id + ", DUPLICATED");
			return null;
		}
		
		ids.add(id);

		
		String folders = row.get("carpetas");
		String title = row.get("documento");
		
		String descripcion = row.get("descripcion");
		if (descripcion!=null && !"".equals(descripcion)) {
			descripcion = descripcion.replace("|n", "</br>");
		}
		
		if (folders==null || !folders.contains("BCV/") ) {
			getLogger().error(id + "NO FOLDERS");
			return null;
		}
		
		String alias ="";
		String[] foldernames =  folders.split("BCV/");
		for (int i=0; i<foldernames.length; i++) {
			String foldername = foldernames[i].trim();
			if (foldername.startsWith(", ")) {
				foldername = foldername.substring(3);
			}
			if (foldername.endsWith(",")) {
				foldername = foldername.substring(0, foldername.length()-1);
			}
			if (!"".equals(foldername)) {
				foldername = "BCV/" + foldername;
				if (folders.contains(foldername)) {
					if (!"".equals(alias)) alias += ", ";
					alias += foldername + "/" + title;
				}
			}
		}
		
		value.setSubline(alias);
		value.setExternalId(id);
		value.setDisplayName(title);
		value.setAttribute("descripcion", descripcion);
		
		if (getExtra().get(id)!=null &&
			getExtra().get(id).get("notes")!=null) {
			value.setLastName(getExtra().get(id).get("notes"));
		}
		
		return  value;
	}
	
	private IValue2 buildParents(Map<String,String> row, List<String> ids) {
		
		IValue2 value = new IValue2();
		String id = row.get("id");
		
		if (ids.contains(id)) 
			return null;
			
		
		Map<String, String> row2 = getRows2().get(id);
		
		/*
		 * if (row2==null) { getLogger().error(id + ", NO ROW2"); } else {
		 * System.out.print("hola"); }
		 */
		
		String folders = row.get("carpetas");
		String title = row.get("documento");
		String perms = row.get("files");
		
		String perms2 = row2!=null ? row2.get("permisos") : null;
		if (perms2!=null) {
			perms= perms2;
		}
		
		String descripcion = row.get("descripcion");
		if (descripcion!=null && !"".equals(descripcion)) {
			descripcion = descripcion.replace("|n", "</br>");
		}
		
		
		String alias ="";
		List<String> names = new ArrayList<>();
		if (folders!=null) {
			String[] foldernames =  folders.split("BCV/");
			for (int i=0; i<foldernames.length; i++) {
				String foldername = foldernames[i].trim();
				if (foldername.startsWith(", ")) {
					foldername = foldername.substring(3);
				}
				if (foldername.endsWith(",")) {
					foldername = foldername.substring(0, foldername.length()-1);
				}
				if (!"".equals(foldername)) {
					foldername = "BCV/" + foldername;
					if (folders.contains(foldername)) {
						if (!"".equals(alias)) alias += ", ";
						names.add(foldername);
					}
				}
			}
		}
		
		
		
		List<ApiProxy> parents = new ArrayList<>();
		for (String name : names) {
			DataSetMember member = findMemberByPath(name);
			if (member!=null) {
				parents.add(new ApiProxy(String.valueOf(member.getId()), null, null, null));
			}
		}
		
		value.setParents(parents);

		value.setExternalId(id);
		value.setDisplayName(title);
		value.setAttribute("descripcion", descripcion);
		if (perms!=null && !"".equals(perms.trim()))
		value.setAttribute("perms", perms);
		
		
		return  value;
	}
	
	private DataSetMember update(IValue2 value) {

		Transaction transaction = null;
		try {
			transaction = beginTransaction();
			DataSetMember member = getOrCreateValue(value);
			List<String> updates = update(member, value);
			if (!updates.isEmpty()) {
				String message = value.getExternalId() + ", OK, ok (" + ++doc + ")";
				getLogger().info(message) ;
 				transaction.commit();
			}	
			else {
				String message =value.getExternalId() + ", WARN, sin cambios (" + ++doc + ")";
				getLogger().info(message);
				transaction.rollback();
			}	
			return member;
		}
		catch (Exception e) {
			e.printStackTrace();
			getLogger().info(value.getExternalId() + ", ERROR, "+e.getMessage());
			logger.error(e);
//			if (transaction.isActive() || !transaction.isCompleted()) {
//				transaction.rollback();
//			}
		}
		return null;
	}
	
	
	private List<String> update(DataSetMember member, IValue2 value) throws IOException {
		List<String> updates = new ArrayList<String>();
		
		
		if (value.getAttributeValue("perms")!=null) {
			if (updatePerms(member, value.getAttributeValue("perms"))) {
				updates.add("Perms");
			}
		}
		
		if (value.getSubline()!=null && !equals(member.getAlternativeDisplayName(), value.getSubline())) {
			member.setAlternativeDisplayName(value.getSubline());
			updates.add("Alternative Display Name");
		}
		
		
		if (!equals(member.getDisplayName(), value.getDisplayName())) {
			member.setStrValue(value.getDisplayName());
			updates.add("Display Name");
		}
		
		if (!equals(member.getExternalId(), value.getExternalId())) {
			member.setExternalId(value.getExternalId());
			updates.add("External Id");
		}
		
		if (!equals(member.getAttributeValues(getAttribute("descripcion")), value.getAttributeValue("descripcion"))) {
			List<String> values = new ArrayList<>();
			values.add(value.getAttributeValue("descripcion"));
			member.setAttributeValues(getAttribute("descripcion"), values);
			updates.add("Description");
		}
		
		if (value.getLastName()!=null && 
			(member.getNotes()==null || !equals(member.getNotes().toString(), value.getLastName()))) {
			member.setNotes(value.getLastName());
			updates.add("Notes");
		}
		
		List<DataSetMember> parents = new ArrayList<>();
		if (value.getParents()!=null) {
			for (ApiProxy parentproxy : value.getParents()) {
				DataSetMember parent = getContentDao().findMemberById(Long.valueOf(parentproxy.getId()));
				parents.add(parent);
			}
			
			if (!equals(member.getParents(), parents)) {
				((KbeeDataSetMember)member).setParents(parents);
				updates.add("Parents");
			}
		}
		
		if (!updates.isEmpty()) {
			member.getService(DOMObjectService.class).update();

		}
		return updates;
	}
	
	private boolean updatePerms(DataSetMember member, String perms) {
		//StringTokenizer permstoken = new StringTokenizer(perms, ",");
		SecuredMember secured = (SecuredMember)member;
		SecurityRule rule = secured.getSecurityRule();
		boolean response = false;
		if (rule==null || (rule.getCondition()!=null && rule.getCondition().contains("null"))) {
			secured.setStrValue(secured.getStrValue());
			rule = secured.getSecurityRule();
			if (rule.getCondition()!=null && rule.getCondition().contains("null"))
			rule.setCondition(((KbeeSecuredMember)secured).getCondition());
			response = true;
		}
		KbeeAcl acl = (KbeeAcl)rule.getAcl();
		List<AclEntry> entries = new ArrayList<>();
		List<String> permslist = parsePerms(perms);
		for (String perm : permslist) {
			EntityMember entity = null;
			String permsvalue = null;
			String rolename = null;
			
			if (perm.contains("|")) {
				int i1 = perm.indexOf("|");
				int i2 = perm.lastIndexOf("/");
				String name = perm.substring(0,i1);
				String parentname = perm.substring(i1+1,i2);
				permsvalue =perm.length()>i2+1 ? perm.substring(i2+1) : "";
				
				DataSetMember parent = getOrganization(parentname);
				
				if (parent==null) {
					getLogger().info(member.getId() + ", WARN, Area "+parentname+ " no encontrada");
				}
				else {
					String lname = name.trim().toLowerCase(); 
					int w = lname.split(" ").length; 
					if (lname.contains("administrador") && w==1)
						rolename = "Administrador";
					if (lname.contains("editor") && w==1)
						rolename = "Editor";
					if (lname.contains("usuario") && w==1)
						rolename = "Usuario";
	
					if ("area".equals(parent.getDataSet().getAlias())) {
						if (rolename==null) {
							EntityMember departamento = (EntityMember)getDepartamento((EntityMember)parent, name.trim());
							entity = departamento;
							rolename = "Usuario";
						}
						else {
							entity = (EntityMember)parent; 
						}
					}
					else {
						if ("empresa".equals(parent.getDataSet().getAlias())) {
							DataSetMember area = getArea(name, (EntityMember)parent);
							if (area!=null) {
								entity = (EntityMember)area;
							}
							else {
								entity = (EntityMember)createArea(parent, name);
							}
						}
					}
				}
			}
			else {
				int i2 = perm.lastIndexOf("/");
				String parentname = perm.substring(0,i2);
				permsvalue = perm.substring(i2+1);
				DataSetMember organization = getOrganization(parentname);
				
				if (organization==null) {
					getLogger().info(member.getId() + ", WARN, Area "+parentname+ " no encontrada");
				}
				else {
					if ("empresa".equals(organization.getDataSet().getAlias())) {
						entity = (EntityMember)organization;
						rolename = "Empleado";
					}
				}
			}
			
			if (entity!=null) {
				Group principal;
				if (rolename!=null) {
					Role role = getRole((EntitySet)entity.getDataSet(), rolename);
					principal = ((KbeeEntityMember)entity).getGroup(role);
					if (principal == null) {
						KbeeMemberRole memberRole = (KbeeMemberRole)((KbeeEntityMember)entity).getMemberRole(role);
						principal = ((KbeeEntityMember)entity).getGroup(memberRole);
						response = true;
					}
					//if (principal.isEmpty()) {
						String pname = principal.getName();
						KbeeMemberRole memberRole = new KbeeMemberRole();
						memberRole.setEntity(entity);
						memberRole.setRole(role);
						memberRole.setGroup(principal);
						principal = ((KbeeEntityMember)entity).getGroup(memberRole);
						if (!pname.equals(principal.getName())) {
							response = true;
						}
					//}
				}
				else {
					principal = entity.getGroup();
				}
				if (principal==null) {
					if ("area".equals(entity.getDataSet().getAlias())) {
						Group group = ((KbeeEntityMember)entity).createGroup(entity);
						((KbeeEntityMember)entity).setGroup(group);
						response = true;
						principal = group;
					}
				}
				if (principal!=null) {
					AclEntry entry = new KbeeAclEntry(acl, principal, false);
					List<Permission> permissions = new ArrayList<Permission>();
					if (permsvalue.contains("r"))
					permissions.add(KbeePermission.READ);
					if (permsvalue.contains("w"))
					permissions.add(KbeePermission.WRITE);
					if (permsvalue.contains("d"))
					permissions.add(KbeePermission.DELETE);
					if (permsvalue.contains("c"))
					permissions.add(KbeePermission.CHILDS);
					entry.setPermissions(permissions);
					entries.add(entry);
				}
				else {
					getLogger().info(member.getExternalId() + ", ERROR, perm not found "+perm);
				}
			}
			else {
				getLogger().info(member.getExternalId() + ", ERROR, perm not found "+perm);
			}

		}
		

		boolean aclchange = false;
		if (acl.getEntries().size()!=entries.size()) {
			aclchange = true;
		}
		else {
			int e = 0;
			for (AclEntry entry1 : acl.getEntries()) {
				Principal principal1 = (Principal)entry1.getPrincipal();
				AclEntry entry2 = entries.get(e++);
				Principal principal2 = entry2.getPrincipal();
				if (!principal1.getId().equals(principal2.getId())) {
					aclchange = true;
				}
				if (((KbeeAclEntry)entry1).getPermissions().size()!=
					((KbeeAclEntry)entry2).getPermissions().size()) {
					aclchange = true;
				}
			}
		}

		if (aclchange) {
			acl.clearEntries();
			for (AclEntry entry : entries) {
				acl.addEntry(entry.getPrincipal(), entry);
			}
			response = true;
		}
		
		return response;
	}
	
	private List<String> parsePerms(String permsvalue) {
		List<String> perms = new ArrayList<>();
		
		int i = 0, p=0, b=0;
		
		while (i>=0) {
			i = permsvalue.indexOf(",",b);
			if ((i<0 || 
					(i>1 && permsvalue.charAt(i-1)=='/') ||
					(i>2 && permsvalue.charAt(i-2)=='/') || 
					(i>3 && permsvalue.charAt(i-3)=='/') || 
					(i>4 && permsvalue.charAt(i-4)=='/') || 
					(i>5 && permsvalue.charAt(i-5)=='/') ||
					(i>5 && permsvalue.charAt(i-6)=='/'))) {
				String perm =  i>0 ? permsvalue.substring(p,i) : permsvalue.substring(p);
				if (perm.contains(",")) {
					System.out.print(perm);
					//perms.addAll(parsePerms2(perm));
				}
				//else {
					perms.add(perm.trim());
				//}
				p = i+1;
				b = p;
			}
			else {
				i = i>0 ? i+1 : i;
				b=i;
			}
		}		
		return perms;
		
	}
	
//	private List<String> parsePerms2(String permsvalue) {
//		List<String> perms = new ArrayList<>();
//		
//		int i = 0, p=0, b=0;
//		
//		while (i>=0) {
//			i = permsvalue.indexOf(",",b);
//			if ((i<0 || 
//					(i>1 && permsvalue.charAt(i-1)=='/') ||
//					(i>2 && permsvalue.charAt(i-2)=='/') || 
//					(i>3 && permsvalue.charAt(i-3)=='/') || 
//					(i>4 && permsvalue.charAt(i-4)=='/') || 
//					(i>5 && permsvalue.charAt(i-5)=='/'))) {
//				String perm =  i>0 ? permsvalue.substring(p,i) : permsvalue.substring(p);
//				perms.add(perm.trim());
//				p = i+1;
//				b = p;
//			}
//			else {
//				i = i>0 ? i+1 : i;
//				b=i;
//			}
//		}		
//		return perms;
//		
//	}
 	
	private DataSetMember getOrganization(String name) {
		DataSetMember empresa = null;
		
		for (DataSetMember e : getEmpresas()) {
			if (name.endsWith(e.getDisplayName())) {
				empresa = e;
				int i = name.lastIndexOf(e.getDisplayName());
				name = name.substring(0,i);
				name = name.trim();
				break;
				
			}
		}
		
		if ("".equals(name) && empresa!=null) {
			return empresa;
		}
		
		if (empresa==null) {
			return null;
		}
		
		DataSetMember area = getArea(name, (EntityMember)empresa);
		
		if (area==null) {
			//throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, "no area");
		}
		
		return area;
	}
	
	private DataSetMember getArea(String name, EntityMember empresa) {
		Classifier classifierempresa = getClassifier("empresa");
		DataSet areaset =  getDataSet("Area");
		DataSetMember found = null;
		List<DataSetMember> areas = getContentDao().findMembersByValue(areaset, name);
		for (DataSetMember area : areas) {
			if (found!=null) {
				break;
			}
			for (Classification classification : area.getClassification(classifierempresa)) {
				if (classifierempresa.equals(classification.getClassifier())) {
					if (classification.getDataSetMember().equals(empresa)) {
						found = area;
						break;
					}
				}
			}
		}
		return found;
	}
	
	private DataSetMember getDepartamento(EntityMember area, String name) {
		DataSetMember departamento = null;
		for (DataSetMember member : getMemberRepository().findAggregationValues(area, getDataSetDepartamento())) {
			List<String> values = member.getAttributeValues(getAttribute("nombre"));
			if (values.contains(name)) {
				departamento = (DataSetMember)getContentDao().reload(member);
				break;
			}
		}
		if (departamento==null) {
			departamento = createDepartamento(area, name);
		}
		return departamento;
	}
	
	private DataSetMember createDepartamento(EntityMember area, String name) {
		DataSetMember departamento = getDataSetDepartamento().createMember();
		departamento.setClassification(getClassifier("area"), area);
		List<String> values = new ArrayList<>();
		values.add(name);
		departamento.setAttributeValues(getAttribute("nombre"), values);
		departamento.setStrValue(name);
		getContentDao().save(departamento);
		return departamento;
	}
	
	private DataSetMember createArea(DataSetMember empresa, String name) {
		DataSetMember area = getDataSet("area").createMember();
		area.setClassification(getClassifier("empresa"), empresa);
		List<String> values = new ArrayList<>();
		values.add(name);
		area.setAttributeValues(getAttribute("nombre"), values);
		area.setStrValue(name);
		getContentDao().save(area);
		return area;
	}
	
	private Role getRole(EntitySet entitySet, String rolename) {
		for (Role role : getContentSecurityDao().getRoles(getDomain())) {
			role = (Role)getContentDao().reload(role);
			if (role instanceof EntityRole &&
				((EntityRole)role).getClassifier().getDataSet().equals(entitySet) &&
				rolename.toLowerCase().equals(role.getName().toLowerCase())) {
				return role;
			}
		}
		return null;
	}
	
	private DataSet getDataSetDepartamento() {
		if (dataSetDepartamento==null) {
			dataSetDepartamento = getDataSet("departamento");
		}
		return dataSetDepartamento;
	}
	
	private List<DataSetMember> getEmpresas() {
		return getMemberRepository().findAll(getDataSet("empresa"));
	}
	
	private Attribute getAttribute(String name) {
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if (name.equals(attribute.getAlias().toLowerCase())) {
				return attribute;
			}
		}
		return null;
	}
	
	private DataSetMember findMemberById(String value) {
		try {
			DataSetMember member = getContentDao().findMemberByExternalId(value);
			return member;
		}
		catch(Exception e) {
			e.printStackTrace();
			if (logger.isDebugEnabled()) {
				logger.error(e);
			}
			else {
				logger.info("error "+e.getMessage());
			}
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	
	private DataSetMember findMemberByPath(String value) {
		try {
	        String hql = "FROM KbeeDataSetMember D WHERE D.alternative_display like '%" + value.trim()+"' OR  D.alternative_display like '%" + value.trim() +",%'";
	        org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
	        List results = query.list();
	        List<DataSetMember> members = (List<DataSetMember>) results;
	        if (members.isEmpty()) {
	        	return null;
	        }
	        return members.get(0);
		}
		catch(Exception e) {
			e.printStackTrace();
			if (logger.isDebugEnabled()) {
				logger.error(e);
			}
			else {
				logger.info("error "+e.getMessage());
			}
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
	
	private DataSetMember getOrCreateValue(IValue2 value) {
		DataSetMember member = null;
		if (value.getExternalId()!=null) {
			member = getContentDao().findMemberByExternalId(value.getExternalId());
		}
		if (member==null) {
			member = createMember(value);
		}
		return member;
	}
	
	private DataSetMember createMember(IValue2 value) {
		try {
			DataSet dataSet = getDataSet();
			KbeeDataSetMember member = (KbeeDataSetMember)dataSet.createMember();
			member.setDomain(dataSet.getDomain());
			member.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			return member;
		}
		catch (ContentCreationException | ContentMgmtException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	private DataSet getDataSet() {
		if (dataSet==null) {
			for (DataSet dataSet : getContentDao().getDataSets(getDomain())) {
				if ("carpetas".equals(dataSet.getAlias())) {
					this.dataSet = dataSet;
				}
			}
		}
		return this.dataSet;
	}
	
	private boolean equals(String s1, String s2) {
		if (s1!=null && !s1.equals(s2))
			return false;
		if (s2!=null && !s2.equals(s1))
			return false;
		return true;
	}
	
	private boolean equals(List<String> s1, String s2) {
		if ((s1==null || s1.isEmpty()) && s2!=null && !"".equals(s2))
			return false;
		if ((s1!=null && s1.isEmpty()) && (s2==null || "".equals(s2)))
			return true;
		if (s1!=null && !s1.isEmpty() && s1.get(0).equals(s2))
			return true;
		if (s1!=null && s1.isEmpty() && s2==null)
			return true;
		return false;
	}
	
	private boolean equals(List<DataSetMember> list1, List<DataSetMember> list2) {
		if (list1==null || list2==null)
			return false;
		if (list1.size()!=list2.size())
			return false;
		for (int i=0; i<list1.size(); i++) {
			if (!list1.get(i).equals(list2.get(i)))
				return false;
		}
		return true;
	}
	
	
	private synchronized List<Map<String, String>> getRows() {
		 
		if (this.rows!=null)
			return this.rows;
		BufferedReader reader = null;
		try {
			
			List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
 		
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(getFileName()), "UTF-8"));
			
			String line;
			
			Set<String> ids = new HashSet<String>();
			
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				Map<String,String> row = new HashMap<String, String>();
				
				if (line!=null && line.endsWith(";")) line +=" ";
				
				String columns[] = line.split(";");
				
				for (int i=0; i<columns.length; i++) {
					String columnValue = String.valueOf(columns[i].trim());
					if (i<fields.length) {
					String field = fields[i];
					row.put(field, columnValue);
					}
				}
					
				ids.add(row.get("id"));
					
				String title = row.get("documento");
				if ("Folder".equals(row.get("tipo"))) {
					//if ("MANUALES DE HERRAMIENTAS Y EQUIPOS".equals(title)) {
						rows.add(row);
					//}
				}
			}
			this.rows = rows;
		}
		catch (IOException e) {
			logger.error(e);
			getLogger().error(e);
			throw new ContentMgmtException(e);
		}
		finally {
			try {
				if (reader!=null)
				reader.close();
			}
			catch (IOException e) {
				logger.error(e);
				getLogger().error(e);
				throw new ContentMgmtException(e);
			}
		}
		
		return this.rows;
	}
	
	private synchronized Map<String, Map<String, String>> getRows2() {
		 
		if (this.rows2!=null)
			return this.rows2;
		BufferedReader reader = null;
		try {
			
			Map<String, Map<String, String>> rows = new HashMap<>();
 		
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(getFileName()), "UTF-8"));
			
			String line;
			
			Set<String> ids = new HashSet<String>();
			
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				Map<String,String> row = new HashMap<String, String>();
				
				if (line!=null && line.endsWith(";")) line +=" ";
				
				String columns[] = line.split(";");
				
				int fieldCount = columns.length;
				for (int i=0; i<columns.length; i++) {
					String columnValue = String.valueOf(columns[i].trim());
					if (i<fields.length) {
					String field = fields[i];
					row.put(field, columnValue);
					}
				}
					
				String id = row.get("id");
				ids.add(id);
					
				String title = row.get("documento");
				if ("Carpeta".equals(row.get("tipo"))) {
					rows.put(id, row);
				}
			}
			this.rows2 = rows;
		}
		catch (IOException e) {
			logger.error(e);
			getLogger().error(e);
			throw new ContentMgmtException(e);
		}
		finally {
			try {
				if (reader!=null)
				reader.close();
			}
			catch (IOException e) {
				logger.error(e);
				getLogger().error(e);
				throw new ContentMgmtException(e);
			}
		}
		
		return this.rows2;
	}
	
	private synchronized Map<String, Map<String,String>> getExtra() {
		 
		if (extra!=null)
			return extra;
		BufferedReader reader = null;
		try {
			
			extra = new HashMap<String, Map<String, String>>();
		
			String filename = getFileName();
			filename = filename.replace(".csv", "ext.csv");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			
			String line;
			
			reader.readLine();
			
			while ((line = reader.readLine()) != null) {
				line = line.replaceAll(";;", "; ;");
				//Map<String,String> row = new HashMap<String, String>();
				StringTokenizer columns = new StringTokenizer(line, ",");
				
				int fieldCount = columns.countTokens();
				
				if (fieldCount==3) {
					columns.nextElement();
					String id = String.valueOf(columns.nextElement()).trim();
					String notes = String.valueOf(columns.nextElement()).trim();
					notes = notes.replace("<html xmlns=\"http://www.w3.org/1999/xhtml\"/>", "");
					if (!"".equals(notes.trim())) {
						Map<String, String> map = new HashMap<>();
						//notes = String.valueOf(columns.nextElement()).trim();
						notes = notes.replace("<html xmlns=\"http://www.w3.org/1999/xhtml\">", "");
						notes = notes.replace("</html>", "");
						notes = notes.replace("<body>", "");
						notes = notes.replace("</body>", "");
						map.put("notes", notes);
						extra.put(id, map);
					}
				}
				else {
					//throw new IOException("columns");
				}
			}
		}
		catch (IOException e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
		finally {
			try {
				if (reader!=null)
				reader.close();
			}
			catch (IOException e) {
				logger.error(e);
				throw new ContentMgmtException(e);
			}
		}
		
		return extra;
	}
	
	private String getFileName() {
		return ServiceLocator.getService(SystemParameterService.class).getParameter("aerolineas.users.file", "migration"+File.separator+"carpetas.csv");
	}
	
	private String getLoggerName(String lote) {
		String name = logsPath +"/importacion-" + lote.toLowerCase() + "-";
		DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		name += format.format(new Date());
		name += "-" + String.valueOf(getId()) + ".log";
		return name;
	}
	
	private DataSet getDataSet(String name) {
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (name.toLowerCase().equals(dataset.getAlias().toLowerCase()) || name.equals(dataset.getName())) {
				return dataset;
			}
		}
		return null;
	}
	
	private Classifier getClassifier(String name) {
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (name.equals(classifier.getName().toLowerCase())) {
				return classifier;
			}
		}
		return null;
	}
	
    private MemberRepository getMemberRepository() {
    	return (MemberRepository) memberRepository;
    }
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
}