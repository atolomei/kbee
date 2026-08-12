package kbee.aerolineas.migration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.EntitySet;
import com.novamens.content.model.PersonMember;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserProfileType;
import com.novamens.content.user.UserRole;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.model.KbeeEntityMember;
import com.novamens.kbee.content.model.KbeeMemberRole;
import com.novamens.kbee.content.repository.MemberRepository;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.repository.DomRepository;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.transaction.Transaction;
import com.novamens.util.KbeeRuntimeException;

import kbee.api.model.ApiProxy;
import kbee.api.model.ApiUser;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.util.PropertiesFactory;
import kbee.util.logging.Logger;

public class ImportarUsuarios extends AsyncCommand {
	
	private static Logger logger = new Logger(LogManager.getLogger(ImportarUsuarios.class.getName()));
	
	private static String[] fields = { "apellido","nombre","direccion","mail","telefono","empresa","legajo", 
		"area", "editor","funciones","usuario",	"creacion","creador","expiracion", "ultimo login",
		"inhabilitado"
	};
	
	private List<Map<String, String>> rows = null;
	private Map<String, Map<String, String>> extra = null;
	private int index = 0;
	
    @Autowired
    private DomRepository<DataSetMember> memberRepository;
    
    private DataSet dataSetDepartamento = null;
	private String logsPath;
	private boolean migrateAll = false;

	public ImportarUsuarios() {
		setName("Importar Usuarios AA");
	}

	public void executeAsync() {
		
		try {
			migrateAll = "true".equals(getParameter("migrateall"));
			String lote = (String)getParameter("lote");
	 		logsPath = PropertiesFactory.getInstance("kbee").getProperties().getProperty("aerolineas.logs", "logs").trim();
			if (lote==null) {
				setResultComments("el parámetro lote es obligatorio");
				throw new RuntimeException("sin lote");
			}
			
			setLogger(getLoggerName(lote));

		
			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@aerolineas");
	
			for (index=0; index<getRows().size(); index++) {
				Map<String, String> row = getRows().get(index);
				ApiUser user = buildUser(row);
				if (migra(row)) {
					update(user);
				}
				else {
					delete(user);
					getLogger().info(user.getExternalId() + ", WARN, no migra");
				}
			}
			
			for (ApiUser user : getUsers()) {
				checkdelete(user);
			}
			
  			end();
  			rows = null;
		}
		catch (Exception e) {
			rows = null;
			logger.error(e);
			stop();
		}
	}
	
	public boolean migra(Map<String, String> row) {
		try {
			if (migrateAll) {
				return true;
			}
			if (row.get("usuario")==null || "".equals(row.get("usuario").trim())) {
				return false;
			}
			if ("si".equals(row.get("inhabilitado"))) {
				return false;
			}
			if (row.get("empresa").equals("Proveedores / Outsourced Services")) {
				//String email = row.get("mail");
				//return email!=null && email.contains("@aerolineas");
				return true;
			}
			if (!row.get("empresa").equals("Aerolíneas Argentinas")) {
				return false; 
			}
		}
		catch (Exception e) {
			System.out.println("no las logon");
			return false;
		}
		return true;
	}
	
	@Override
	public double getProgress() {
		return (double) index/(double) getTotalItems() * 100;
	}
	
	
	@Override
	public long getTotalItems() {
		return getRows().size();
	}
	
	@Override
	public long getTotalItemsProcessed() {
		return index;
	}
	
	private ApiUser buildUser(Map<String,String> row) {
		ApiUser user = new ApiUser();
		
		user.setName(getUserName(row));
		
		if ("501995".equals(row.get("legajo"))) {
			user.setExternalId(row.get("legajo"));
		}
		
		if (row.get("legajo")!=null) {
			user.setExternalId(row.get("legajo"));
		}
		
		user.setFirstName(row.get("nombre"));
		user.setLastName(row.get("apellido"));
		user.setPhone(row.get("telefono"));
		user.setEmail(row.get("mail"));
		user.setTimeZone("America/Argentina/Buenos_Aires");
		user.setEnabled(!"si".equals(row.get("inhabilitado")));
		user.setDomain("aerolineas");
		
		user.setAttribute("address", row.get("direccion"));
		user.setAttribute("funciones", row.get("funciones"));
		user.setAttribute("area", row.get("area"));
		user.setAttribute("legajo", row.get("legajo"));
		user.setAttribute("empresa", row.get("empresa"));
		
		
		Map<String, String> extra = getExtra().get(row.get("legajo"));
		if (extra!=null) {
			String username = extra.get("user");
			String modified = extra.get("modified");
			OffsetDateTime odt = OffsetDateTime.now();
			try {
				odt = OffsetDateTime.parse(modified);
			}
			catch (Exception e) {
				
			}
			user.setLastModifiedUser(new ApiProxy(username, null));
			user.setLastModifiedDate(odt);
		}
		
		user.setAttribute("editor", row.get("editor"));
		
		user.setDisplayName(row.get("usuario"));
		
		return user;
	}
	
	private void update(ApiUser user) {
		Transaction transaction = null;
		try {
			transaction = beginTransaction();
			Person person = getOrCreatePerson(user);
			List<String> updates = update(person, user);
			updates.addAll(updateRoles(person, user));
			if (!updates.isEmpty()) {
				getContentDao().flush();
				ServiceLocator.getService(SecurityContentMgmtService.class).update(person, updates);
				if (user.getLastModifiedUser()!=null) {
					person.setLastModifiedOffsetDateTime(user.getLastModifiedDate());
					person.setLastModifiedUser(getUser(user.getLastModifiedUser()));
				}
		        getSessionFactory().getCurrentSession().save(person);
				getLogger().info(user.getExternalId() + ", UPDATED");
			}
			else {
				if (user.getLastModifiedUser()!=null &&
					!person.getLastModifiedUser().equals(getUser(user.getLastModifiedUser()))) {
					person.setLastModifiedOffsetDateTime(user.getLastModifiedDate());
					person.setLastModifiedUser(getUser(user.getLastModifiedUser()));
			        getSessionFactory().getCurrentSession().save(person);
				}
			}
			getContentDao().flush();
 			transaction.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			getLogger().warn(user.getExternalId(), "USER ERROR");
			getLogger().error(e);
			logger.error(e);
			transaction.rollback();
		}
	}
	
	private void checkdelete(ApiUser user) {
		boolean found = false;
		for (int i=0; i<getRows().size(); i++) {
			Map<String, String> row = getRows().get(i);
			if (user.getExternalId().equals(row.get("legajo"))) {
				found = true;
				break;
			}
		}
		if (found) {
			return;
		}
		Transaction transaction = null;
		try {
			transaction = beginTransaction();
			delete(user);
			transaction.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e);
			logger.error(e);
			transaction.rollback();
		}
	}

	
	private User getUser(ApiProxy proxy) {
		String userName = proxy.getName();
		KbeePerson person = findPersonByUserName(userName);
		if (person!=null) {
			UserProfile profile = person.getProfile(UserProfile.class);
			return profile.getUser();
		}
		userName = "root";
		person = findPersonByUserName(userName);
		if (person!=null) {
			UserProfile profile = person.getProfile(UserProfile.class);
			return profile.getUser();
		}
		return getUser();
	}
	
	
	private void delete(ApiUser user) {
		Transaction transaction = null;
		try {
			transaction = beginTransaction();
			Person person = getPerson(user);
			if (person!=null) {
				ServiceLocator.getService(SecurityContentMgmtService.class).delete(person);
				getLogger().info(user.getExternalId() + ", DELETED");
			}
			transaction.commit();
		}
		catch (Exception e) {
			//getLogger().error(e);
			getLogger().info(user.getExternalId()+",ERROR, ONDELETE");
			logger.error(e);
			try {
				transaction.rollback();
			}
			catch (Exception e1) {
				logger.error(e1);
			}
		}
	}
	
	private List<String> update(Person person, ApiUser iuser) {
		List<String> updates = new ArrayList<String>();
		
		String username = iuser.getName() + "@" + iuser.getDomain();
		
		User user = person.getProfile(UserProfile.class).getUser();
		
		if (!equals(user.getName(), username)) {
			((KbeeUser)user).setUserName(username);
			updates.add("user name");
		}
		
		if (!equals(person.getLastName(), iuser.getLastName())) {
			person.setLastName(iuser.getLastName());
			updates.add("last name");
		}
		
		if (!equals(person.getFirstName(), iuser.getFirstName())) {
			person.setFirstName(iuser.getFirstName());
			updates.add("first name");
		}
		
		if (!equals(person.getEmail(), iuser.getEmail())) {
			person.setEmail(iuser.getEmail());
			updates.add("email");
		}
		
		if (!equals(person.getPhone(), iuser.getPhone())) {
			person.setPhone(iuser.getPhone());
			updates.add("phone");
		}
		
		
		if (!equals(person.getAddress(), iuser.getAttributeValue("address"))) {
			person.setAddress(iuser.getAttributeValue("address"));
			updates.add("address");
		}
		
		if (!equals(person.getDescription(), iuser.getDisplayName())) {
			person.setDescription(iuser.getDisplayName());
			updates.add("name");
		}	
		
		if (!equals(user.getTimeZone(), iuser.getTimeZone())) {
			if (!validTimeZone(iuser.getTimeZone())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.USER_INVALID_TIMEZONE);
			}
			user.setTimeZone(iuser.getTimeZone());
			updates.add("time zone");
		}	
		
		UserProfile userprofile = person.getProfile(UserProfile.class);
		if (userprofile.isEditPersonEnabled()) {
			userprofile.setEditPersonEnabled(false);
			updates.add("account edition");
		}
		
		if (userprofile.isChangePasswordEnabled()) {
			userprofile.setChangePasswordEnabled(false);
			updates.add("password edition");
		}

		if (isWorkflow(iuser) && !user.isActive()) {
			((KbeeUser)user).setActive(true);
			updates.add("active");
		}
		
		if (!isWorkflow(iuser) && user.isActive()) {
			((KbeeUser)user).setActive(false);
			updates.add("active");
		}
		
		if (isWorkflow(iuser) && !userprofile.getType().equals(UserProfileType.WORKFLOW_PARTICIPANT)) {
			((KbeeUserProfile)userprofile).setType(UserProfileType.WORKFLOW_PARTICIPANT);
			((KbeeUser)user).setActive(true);
			updates.add("profile type");
		}
		else if (!isWorkflow(iuser) && !userprofile.getType().equals(UserProfileType.READONLY)) { 
			((KbeeUserProfile)userprofile).setType(UserProfileType.READONLY);
			((KbeeUser)user).setActive(false);
			updates.add("profile type");
		}
		
		if (!"bcv".equals(userprofile.getStartPage())) {
			((KbeeUserProfile)userprofile).setStartPage("bcv");
			updates.add("start page");
		}
		
		if (iuser.isEnabled()!=user.isEnabled()) {
			if (iuser.isEnabled()) {
				user.setStateEnabled();
			}
			else {
				user.setStateArchived();
			}
			if (iuser.isEnabled())
				user.setStateEnabled();
			else
				user.setStateArchived();
			updates.add("state");
		}
		
		if (person instanceof PersonMember) {
			PersonMember member = (PersonMember)person; 
			if (!equals(member.getExternalId(), iuser.getExternalId())) {
				if (iuser.getExternalId()!=null) {
					DataSetMember other = getContentDao().findMemberByExternalId(iuser.getExternalId());
					if (other!=null && !other.equals(member)) {
						throw new ContentMgmtException("user already exists");
					}
				}
				member.setExternalId(iuser.getExternalId());
				updates.add("external id");
			}
		}
		
		updates.addAll(updateClassification(person, iuser));
		
		return updates;
	}
	
	private  List<String> updateClassification(Person person, ApiUser iuser) {
		List<String> updates = new ArrayList<String>();
		
		String legajo = iuser.getAttributeValue("legajo");
		
		if (legajo!=null) {
			Attribute legajoattribute = getAttribute("legajo");
			List<String> values = ((PersonMember)person).getAttributeValues(legajoattribute);
			if ((values.size()==1 && !equals(legajo, values.get(0))) || values.size()!=1) {
				values.clear();
				values.add(legajo);
				((PersonMember)person).setAttributeValues(legajoattribute, values);
				updates.add("legajo");
			}
		}
		
		String nombreunidadoperativa = iuser.getAttributeValue("area");
		
		Classifier classifierempresa = getClassifier("empresa");
		String nombreempresa = iuser.getAttributeValue("empresa");
		DataSetMember empresa = null;
		if (classifierempresa!=null && nombreempresa!=null) {
			empresa = getMember(classifierempresa, nombreempresa.trim());
		}	

		
		if (nombreunidadoperativa!=null) {
			Classifier classifierunidad = getClassifier("area");
			if (classifierunidad!=null) {
				String name = nombreunidadoperativa.trim();
				DataSetMember unidadoperativa = getArea(name, (EntityMember)empresa);
				if (!name.equals(unidadoperativa.getStrValue())) {
					unidadoperativa.setStrValue(name);
					updates.add("unidad operativa");
				}
				
				List<Classification> clasificacionunidad = ((PersonMember)person).getClassification(classifierunidad);
				
				if (!unidadoperativa.getClassification(classifierunidad).isEmpty()) {
					unidadoperativa.removeAllClassification(classifierunidad);
					updates.add("unidad operativa");
				}
				
				if ((clasificacionunidad.size()==1 && !clasificacionunidad.get(0).getDataSetMember().equals(unidadoperativa)) || clasificacionunidad.size()!=1) {
					List<DataSetMember> membersunidad = new ArrayList<DataSetMember>();
					membersunidad.add(unidadoperativa);
					((PersonMember)person).setClassification(classifierunidad, membersunidad);
					updates.add("unidad operativa");
				}
			}
		}
		
			if (classifierempresa!=null) {
				List<Classification> clasificacionempresa = ((PersonMember)person).getClassification(classifierempresa);
				if ((clasificacionempresa.size()==1 && !clasificacionempresa.get(0).getDataSetMember().equals(empresa)) || clasificacionempresa.size()!=1) {
					List<DataSetMember> membersempresa = new ArrayList<DataSetMember>();
					membersempresa.add(empresa);
					((PersonMember)person).setClassification(classifierempresa, membersempresa);
					updates.add("empresa");
				}
			}
	
		
		getContentDao().flush();
				
		return updates;
	}
	
	
	private boolean isWorkflow(ApiUser user) {
		if ("si".equals(user.getAttributeValue("editor"))) {
			return true;
		}
		return false;
	}
	
	
	private  List<String> updateRoles(Person person, ApiUser iuser) {
		List<String> updates = new ArrayList<String>();
		
		String functions = iuser.getAttributeValue("funciones");
		
		if (functions==null) return updates;
		
		String function[] = functions.split(",");
		
		Classifier classifierunidad = getClassifier("area");
		
		{
			UserProfile userprofile = person.getProfile(UserProfile.class);
			EntitySet depset = (EntitySet)getDataSetDepartamento();
			Role role = getRole((EntitySet)getDataSetDepartamento(), "Usuario");
			for (UserRole userrole : userprofile.getRoles()) {
				if (userrole.getRole().equals(role) && !userrole.getEntity().getDataSet().equals(depset)) {
					userprofile.getRoles().remove(userrole);
					updates.add("roles");
					break;
				}
			}
			
			if (userprofile.getUser().isActive() && !"si".equals(iuser.getAttributeValue("editor"))) {
				userprofile.getUser().setActive(false);
				updates.add("active");
			}
			if (!userprofile.getUser().isActive() && "si".equals(iuser.getAttributeValue("editor"))) {
				userprofile.getUser().setActive(true);
				updates.add("active");
			}
		}
		
		List<UserRole> userRoles = new ArrayList<>();
		
		for (int f=0; f<function.length; f++) {
			String data[] = function[f].split("\\|");
			String funcion = data[0].toLowerCase();
			
			String rolename = null;
			
			int w = funcion.trim().split(" ").length;
			
			if (w>1) {
				w=2;
			}
			
			if (funcion.contains("administrador") && w==1)
				rolename = "Administrador";
			if (funcion.contains("editor") && w==1)
				rolename = "Editor";
			if (funcion.contains("usuario") && w==1)
				rolename = "Usuario";
			
			String nombreunidadoperativa = data.length>1 ? data[1] : null;
			
			EntityMember area;
			if (data.length>2) {
				String nombreempresa = data[2].trim();
				Classifier classifierempresa = getClassifier("empresa");
				DataSetMember empresa = getMember(classifierempresa, nombreempresa);
				area = (EntityMember)getArea(nombreunidadoperativa.trim(), (EntityMember)empresa);
			}
			else {
				area = nombreunidadoperativa!=null ? (EntityMember)getMember(classifierunidad, nombreunidadoperativa) : null;
			}
			
			if (area!=null) {
				if (area.getGroup()==null) {
					Group group = ((KbeeEntityMember)area).createGroup(area);
					((KbeeEntityMember)area).setGroup(group);
					updates.add("area");
				}
			}
			
			UserProfile userprofile = person.getProfile(UserProfile.class);
							
			if (data.length>1 && rolename!=null && area!=null) {
				
				Role role = getRole((EntitySet)getDataSet("Area"), rolename);
				if (role==null) {
					throw new ContentMgmtException("role is null");
				}
				
				getContentDao().flush();
	 
				boolean found = false;
				for (UserRole userrole : userprofile.getRoles()) {
					if (userrole.getRole().equals(role) && userrole.getEntity().equals(area)) {
						found = true;
						userRoles.add(userrole);
						KbeeEntityMember entity = (KbeeEntityMember)userrole.getEntity();
						//role.setRole(person, area);
						
						if (checkprincipal(entity, userrole.getRole())) {
							updates.add("roles");
						};
						
						break;
					}

				}
				
				if (!found) {
					KbeeUserRole userrole = new KbeeUserRole();
					userrole.setRole(role);
					userrole.setUser(userprofile.getUser());
					userrole.setEntity(area);
					role.setRole(person, area);
					userprofile.getRoles().add(userrole);
					userRoles.add(userrole);
					updates.add("roles");
				}
			}
			else  {
				if (area!=null) {
					EntityMember departamento = (EntityMember)getDepartamento(area, data[0].trim(), updates);
					if (departamento==null) {
						departamento = (EntityMember)createDepartamento(area, data[0].trim());
					}
					Role role = getRole((EntitySet)getDataSetDepartamento(), "Usuario");
					boolean found = false;
					for (UserRole userrole : userprofile.getRoles()) {
						if (userrole.getRole().equals(role) && userrole.getEntity().equals(departamento)) {
							found = true;
							userRoles.add(userrole);
							KbeeEntityMember entity = (KbeeEntityMember)userrole.getEntity();

							if (checkprincipal(entity, userrole.getRole())) {
								updates.add("roles");
							};
							
							break;
						}
					}
					
					if (!found) {
						KbeeUserRole userrole = new KbeeUserRole();
						userrole.setRole(role);
						userrole.setUser(userprofile.getUser());
						userrole.setEntity(departamento);
						role.setRole(person, departamento);
						userprofile.getRoles().add(userrole);
						userRoles.add(userrole);
						updates.add("roles");
					} 
				}
			}
			
			String nombreempresa = iuser.getAttributeValue("empresa");
			Classifier classifierempresa = getClassifier("empresa");
			DataSetMember empresa = getMember(classifierempresa, nombreempresa);
			Role role = getRole("Empleado");
			boolean found = false;
			for (UserRole userrole : userprofile.getRoles()) {
				if (userrole.getRole().equals(role) || userrole.getEntity().equals(empresa)) {
					found = true;
					userRoles.add(userrole);
					KbeeEntityMember entity = (KbeeEntityMember)userrole.getEntity();
					if (checkprincipal(entity, userrole.getRole())) {
						updates.add("roles");
					};
					
					break;
				}
			}
			
			if (!found) {
				KbeeUserRole userrole = new KbeeUserRole();
				userrole.setRole(role);
				userrole.setUser(userprofile.getUser());
				userrole.setEntity((EntityMember)empresa);
				role.setRole(person, (EntityMember)empresa);
				userprofile.getRoles().add(userrole);
				userRoles.add(userrole);
				updates.add("roles");
			}
		}

		{
			UserProfile userprofile = person.getProfile(UserProfile.class);
			List<UserRole> assignedRoles = new ArrayList<>();
			assignedRoles.addAll(userprofile.getRoles());
			boolean remove = true, updated=false;
			while (remove) {
				remove = false;
				for (UserRole assignedrole : assignedRoles) {
					boolean found = false;
					for (UserRole userrole : userRoles) {
						if (userrole.getRole().equals(assignedrole.getRole())) {
							if (!userrole.getRole().isEntity() || userrole.getEntity().equals(assignedrole.getEntity())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						assignedRoles.remove(assignedrole);
						updated = true;
						remove = true;
						break;
					}
				}
			}
			if (updated) {
				userprofile.setRoles(assignedRoles);
				updates.add("roles");
			}
		}
		
		return updates;
	}
	
	private boolean checkprincipal(KbeeEntityMember entity, Role role) {
		//Group principal = entity.getGroup(role);
		KbeeMemberRole memberRole = (KbeeMemberRole)entity.getMemberRole(role);
		Group principal = memberRole.getGroup();
		String name = entity.getGroupName(memberRole);
		if (name!=null && !principal.getName().equals(name)) {
			principal.setName(name);
			return true;
		}
		return false;
	}
	
	private Role getRole(String rolename) {
		for (Role role : getContentSecurityDao().getRoles(getDomain())) {
			role = (Role)getContentDao().reload(role);
			if (role instanceof EntityRole && rolename.toLowerCase().equals(role.getName().toLowerCase())) {
				return role;
			}
		}
		return null;
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

	
	private Attribute getAttribute(String name) {
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if (name.equals(attribute.getName().toLowerCase())) {
				return attribute;
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
		throw new KbeeRuntimeException("classifier not found");
	}
	
	private Person getOrCreatePerson(ApiUser iuser) {
		Person person = null;

		DataSetMember usermember = getContentDao().findMemberByExternalId(iuser.getExternalId());

		if (usermember!=null ) {
			person = (PersonMember)usermember;
		}
		else {
			String username = iuser.getName() + "@" + iuser.getDomain();
			person = (Person) ServiceLocator.getService(ObjectFactoryService.class).createUser(iuser.getFirstName(),
					iuser.getLastName(),
					iuser.getEmail(),
					username,
					ObjectState.ENABLED, 
					true,
					new HashSet<Group>(),
					new ArrayList<KbeeGlobalRole>(), 	 
					new ArrayList<Role>());
			getContentDao().save(person);
		}	
		
		return person;
	}
	
	private Person getPerson(ApiUser iuser) {
		Person person = null;

		List<DataSetMember> members = findMembersByExternalId(iuser.getExternalId());
		Classifier classifierempresa = getClassifier("empresa");
		for (DataSetMember member : members) {
			List<Classification> c = member.getClassification(classifierempresa);
			if (c.size()==1) {
				String empresa = iuser.getAttributeValue("empresa");
				if (empresa.equals(c.get(0).getValue())) {
					person = ((PersonMember)member).getPerson();
					break;
				}
			}
		}
		
		return person;
	}
	
    public List<DataSetMember> findMembersByExternalId(String id) {
        if (id == null) return null;
        String hql = "FROM KbeeDataSetMember WHERE externalId = '" + id + "'";
        org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
        List<?> results = query.list();
        List<DataSetMember> members = (List<DataSetMember>) results;
        return members;
    }

	
	private DataSetMember getMember(Classifier classifier, String value) {
		DataSetMember member = getContentDao().findMemberByValue(classifier.getDataSet(), value);
		if (member==null) {
			member = classifier.getDataSet().createMember();
			member.setStrValue(value);
			getContentDao().save(member);
			member = (DataSetMember)getContentDao().reload(member);
		}
		return member;
	}
	
	private DataSetMember getArea(String name, EntityMember empresa) {
		Classifier classifierempresa = getClassifier("empresa");
		DataSet areaset =  getDataSet("Area");
		boolean found = false;
		DataSetMember area = null;
		List<DataSetMember> areas = getContentDao().findMembersByValue(areaset, name);
		if (areas!=null) {
			for (DataSetMember member : areas) {
				for (Classification classification : member.getClassification(classifierempresa)) {
					if (classification.getDataSetMember().equals(empresa)) {
						found = true;
						area = member;
						break;
					}
				}
			}
		}
		if (areas==null || !found) {
			area = getContentDao().findMemberByValue(areaset, name + " - " +empresa.getDisplayName());
			found = area!=null;
		}
		if (!found) {
			area = areaset.createMember();
			area.setStrValue(name);
			getContentDao().save(area);
			area = (DataSetMember)getContentDao().reload(area);
			List<DataSetMember> members = new ArrayList<>();	
			area.setClassification(classifierempresa, members);
		}
		else {
			if (!name.equals(area.getStrValue())) {
				area.setStrValue(name);
			}
		}
		return area;
	}

	
	
	private DataSetMember getDepartamento(EntityMember area, String name, List<String> updates) {
		DataSetMember departamento = null;
		for (DataSetMember member : getMemberRepository().findAggregationValues(area, getDataSetDepartamento())) {
			List<String> values = member.getAttributeValues(getAttribute("nombre"));
			if (values.contains(name)) {
				departamento = (DataSetMember)getContentDao().reload(member);
				if (!departamento.getDisplayName().equals(name)) {
					departamento.setStrValue(name);
					updates.add("departamento");
				}
				break;
			}
		}
		return departamento;
	}
	
	private DataSetMember createDepartamento(EntityMember area, String name) {
		DataSetMember departamento = getDataSetDepartamento().createMember();
		departamento.setClassification(getClassifier("area"), area);
		List<String> values = new ArrayList<>();
		values.add(name);
		departamento.setAttributeValues(getAttribute("nombre"), values);
		departamento.setStrValue(area.getDisplayName() + " - " + name);
		getContentDao().save(departamento);
		return departamento;
	}

	
	private DataSet getDataSetDepartamento() {
		if (dataSetDepartamento==null) {
			dataSetDepartamento = getDataSet("departamento");
		}
		return dataSetDepartamento;
	}
	
	private DataSet getDataSet(String name) {
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (name.toLowerCase().equals(dataset.getAlias().toLowerCase()) || name.equals(dataset.getName())) {
				return dataset;
			}
		}
		return null;
	}

	
	private String getUserName(Map<String,String> row) {
		String name, legajo;
		legajo = row.get("legajo");
		if (legajo==null) {
			name = row.get("usuario");
		}
		else {
			if (legajo.length()<6) {
				legajo = "000000".substring(0, 6-legajo.length()) + legajo; 
			}
			name = "ar"+legajo;
		}
		return name;
	}
	
	private boolean equals(String s1, String s2) {
		if (s1!=null && !s1.equals(s2))
			return false;
		if (s2!=null && !s2.equals(s1))
			return false;
		return true;
	}
	
	private synchronized List<Map<String, String>> getRows() {
		 
		if (rows!=null)
			return rows;
		BufferedReader reader = null;
		try {
			
			rows = new ArrayList<Map<String, String>>();
		
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(getFileName()), "UTF-8"));
			
			String line;
			
			reader.readLine();
			
			while ((line = reader.readLine()) != null) {
				line = line.replaceAll(";;", "; ;");
				Map<String,String> row = new HashMap<String, String>();
					StringTokenizer columns = new StringTokenizer(line, ";");
					int fieldCount = columns.countTokens();
					if (fieldCount == fields.length  || fieldCount == fields.length-1) {
						int i = 0;
						while (columns.hasMoreElements()) {
							String columnValue = String.valueOf(columns.nextElement()).trim();
							String field = fields[i++];
							row.put(field, columnValue);
						}
						//if (row.get("legajo")!=null && row.get("legajo").trim().equals("32854")) {
							rows.add(row);
						//}
					}
					else {
						logger.error("cantidad no esperada de columnas en archivo de usuarios");
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
		
		return rows;
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
				
				if (fieldCount==4) {
					columns.nextElement();
					String dossier = String.valueOf(columns.nextElement()).trim();
					String user = String.valueOf(columns.nextElement()).trim();
					String modified = String.valueOf(columns.nextElement()).trim();
					Map<String, String> map = new HashMap<>();
					map.put("user", user);
					map.put("modified", modified);
					extra.put(dossier, map);
				}
				else {
					throw new IOException("columns");
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
	
	
	@SuppressWarnings("unchecked")
	private KbeePerson findPersonByUserName(String value) {
		try {
			value = value.replace("'","%");
			value = value.replace("  ", "%");
	        String hql = "FROM KbeePerson P WHERE P.description = '" + value.trim()+"'";
	        org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
	        @SuppressWarnings("rawtypes")
			List results = query.list();
	        List<KbeePerson> members = (List<KbeePerson>) results;
	        if (members.isEmpty()) return null;
	        return members.get(0);
		}
		catch(Exception e) {
			getLogger().error(e);
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
	
	
	private List<ApiUser> getUsers() {
		List<ApiUser> users = new ArrayList<>();
		for (DataSetMember member : getContentDao().getMembers(getContentDao().getUserSet(), null)) {
			if (member.getExternalId()!=null) {
				ApiUser user = new ApiUser();
				user.setExternalId(member.getExternalId());
				users.add(user);
			}
		}
		return users;
	}
	
	private String getLoggerName(String lote) {
		String name = logsPath +"/importacion-" + lote.toLowerCase() + "-";
		DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		name += format.format(new Date());
		name += "-" + String.valueOf(getId()) + ".log";
		return name;
	}
	
	private boolean validTimeZone(String timeZone) {
		return Arrays.asList(TimeZone.getAvailableIDs()).contains(timeZone);
	}
	
	private String getFileName() {
		return ServiceLocator.getService(SystemParameterService.class).getParameter("aerolineas.users.file", "migration\\personas.csv");
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
	
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
	
	private User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}