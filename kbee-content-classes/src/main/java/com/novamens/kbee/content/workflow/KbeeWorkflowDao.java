package com.novamens.kbee.content.workflow;


import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.codesnippets4all.json.generators.JSONGenerator;
import com.codesnippets4all.json.generators.JsonGeneratorFactory;
import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.EventService;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowThread;
import com.novamens.workflow.WorkflowThreadStatus;
import com.novamens.workflow.WorkflowThreadStatus.Status;

import kbee.util.logging.Logger;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import com.novamens.workflow.Factory;
import com.novamens.workflow.ForkJoinTask;
import com.novamens.workflow.Priority;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.Process;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Task;

public class KbeeWorkflowDao implements WorkflowDao, EventListener  {
			
	private final static String Context_Cache 	 = "workflow";
	private final static String Context_Property = "workflow";
	private final static String Process_Property = "process";
	
	private static Logger logger = kbee.util.logging.Logger.getLogger(KbeeWorkflowDao.class.getName());
	
	private SessionFactory sessionFactory;
	private Factory factory = null;
	private Cache contextCache;
	
	private Map<String, Procedure> procedures = Collections.synchronizedMap(new HashMap<String, Procedure>());;

	public KbeeWorkflowDao() {
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Transactional
	public void update(Procedure procedure) {
		((KbeeProcedure)procedure).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeProcedure)procedure).setLastModifiedUser(getSessionUser());
		ServiceLocator.getService(EventService.class).fire(new ProcedureUpdateEvent(procedure));
		sessionFactory.getCurrentSession().save(procedure);
	}
	
	public void update(Process process) {
		if (process.isRunning()) {
			update(process.getContext());
		}
		if (process instanceof ProcessProxy)
			sessionFactory.getCurrentSession().save(((ProcessProxy)process).getProcess());
		else
			sessionFactory.getCurrentSession().save(process);
	}
	
	public void update(Activity activity) {
		sessionFactory.getCurrentSession().save(activity);
	}
	
	public void delete(ProcessLauncher launcher) {
		sessionFactory.getCurrentSession().delete(launcher);
	}
 	
	public void refresh(Long contentId) {
		getContextCache().remove(contentId);
	}
	
	public Process getActiveProcess(Content content) {
		WorkflowContext context = getContext(content);
		return context==null ? null : context.getProcess();
	}
	
	public WorkflowContext reload(WorkflowContext context) {
		KbeeContext kbeecontext = (KbeeContext)context;
		Content content = kbeecontext.getContent();
		content.getService(PropertyService.class).reloadProperty(Context_Property);
		context = getContext(content);
		return context;
	}
	
	@Override
	public Object reload(Object object) {
		try {
			Class<?>  clazz = Hibernate.getClass(object);
			Serializable id = object instanceof Content ? ((Content)object).getId() : ((com.novamens.security.Identifiable)object).getId();
			object = sessionFactory.getCurrentSession().load(clazz, id);
			return object;
		} 
		catch (Exception e) {
			logger.error(e);
			return object;
		}
	}
	
	public Process findProcessById(Long id) {
		KbeeProcess process = (KbeeProcess)sessionFactory.getCurrentSession().get(KbeeProcess.class, id);
		if (process == null) 
			return null;
		Procedure procedure = process.getProcedure();
		if (process.getProcedure()==null) {
			
		}
		else {
			// esto se hace porque las variables tansient como la de las tareas no quedan 
			// en la cache
			procedure = getProcedure(procedure.getId());
		}
		
		if (procedure==null)
			return null;

		for (Activity activity : process.getActivities()) {
			String taskName = ((KbeeWorkflowActivity)activity).getTaskName();
			Task task = procedure.getTask(taskName);
			((KbeeWorkflowActivity)activity).setTask(task);
		}
		
		return process;
	}
	
	@SuppressWarnings("rawtypes")
	public Activity findActivityById(Long id) {
		String hql = "FROM KbeeWorkflowActivity A WHERE A.id = " + id; 
		org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		List results = query.list();
		if (results.isEmpty()) return null;
		KbeeWorkflowActivity activity = (KbeeWorkflowActivity)results.get(0);
		Process process = activity.getProcess();
		Procedure procedure = getProcedure(((KbeeProcess)process).getProcedureName());
		((KbeeProcess)process).setProcedure(procedure);
		String taskName = activity.getTaskName();
		Task task = procedure.getTask(taskName);
		activity.setTask(task);
		return activity;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean hasProcesses(Procedure procedure) {
		String hql = "FROM KbeeProcess p WHERE p.procedure2.id = " + procedure.getId(); 
		org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		List results = query.list();
		if (results.isEmpty()) return false;
 		return true;
	}
	
	public boolean hasActivities(Task task) {
		if (task.getId()==null) return false;
		if (task.getProcedure()==null) return false;
		Procedure procedure = ((KbeeTask)task).getProcedure().getMaster();
		if (procedure==null) return false;
		String hql = "SELECT count(*) FROM KbeeWorkflowActivity a WHERE a.process.procedureName = '" + procedure.getId() + "' and a.taskName = '"+ task.getId()+ "'"; 
		org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("metrics");
		Long count = (Long)query.uniqueResult();
 		return count > 0;
	}	
	
	public void setFactory(Factory factory) {
		this.factory = factory;
	}
	
	public Factory getFactory() {
		return this.factory;
	}
	
	public List<Procedure> getProcedures(Domain domain) {
		try {
			List<Procedure> procedures = new ArrayList<Procedure>();
			for (Procedure procedure : getProcedures().values()) {
				if (((KbeeProcedure)procedure).getDomain().equals(domain)) 
					procedures.add(procedure);
			}
			return procedures;
		}
		catch (Exception e) {
			procedures = Collections.synchronizedMap(new HashMap<String, Procedure>());;
			List<Procedure> procedures = new ArrayList<Procedure>();
			for (Procedure procedure : getProcedures().values()) {
				if (((KbeeProcedure)procedure).getDomain().equals(domain)) 
					procedures.add(procedure);
			}
			return procedures;
		}
	}
	
	public Map<String, Procedure> getProcedures() {
		if (this.procedures.isEmpty()) {
			synchronized (this) {
				
				CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
				CriteriaQuery<KbeeProcedure> criteria = criteriabuilder.createQuery(KbeeProcedure.class);
				Root<KbeeProcedure> procedures = criteria.from(KbeeProcedure.class);
				criteria.select(procedures);
				criteria.orderBy(criteriabuilder.asc(procedures.get("name")));
			
				TypedQuery<KbeeProcedure> query = sessionFactory.getCurrentSession().createQuery(criteria);
				query.setHint("org.hibernate.cacheable", true);
				query.setFlushMode(FlushModeType.COMMIT);
				
				for (KbeeProcedure procedure : query.getResultList()) {
					this.procedures.put(String.valueOf(procedure.getId()), procedure);
				};
			}
		}	
		return procedures;
	}
	
	@Override
	public Procedure getProcedure(Serializable id) {
		Procedure procedure = getProcedures().get(String.valueOf(id));
		if (procedure==null) {
			for (Procedure p : getProcedures().values()) {
				if (((KbeeProcedure)p).getAlias().equals(id)) {
					procedure = p;
					break;
				}
			}
			if (procedure==null) {
				return null;
			}
		}
		Domain domain = ((KbeeProcedure)procedure).getDomain();
		if (domain instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy)domain;
			LazyInitializer initializer = proxy.getHibernateLazyInitializer();
	        if (initializer.isUninitialized()) {
	        	Long domainid= (Long) initializer.getIdentifier();
	            String classname = proxy.getClass().getName();
				int i = classname.indexOf("_");
				if (i>0) classname = classname.substring(0, i);
				i = classname.indexOf("$");
				if (i>0) classname = classname.substring(0, i);
				Class<?> clazz = null;
				try {
					clazz = Class.forName(classname);
		            domain = (Domain)sessionFactory.getCurrentSession().load(clazz, domainid);
		            ((KbeeProcedure)procedure).setDomain(domain);
				}
				catch (Exception e) {
					
				}
	        }
		}

		return procedure;
	}
	
	@Override
	public Procedure findProcedureById(Serializable id) {
		Procedure procedure = getProcedure(id);
		if (procedure!=null) {
			sessionFactory.getCurrentSession().refresh(procedure);
			sessionFactory.getCurrentSession().refresh(((KbeeProcedure)procedure).getDomain());
		}
		return procedure;
	}

	
	@Override
	public ProcessLauncher getProcessLauncher(Serializable id) {
		String hql = "FROM KbeeProcessLauncher P WHERE P.id=" + id.toString(); 
		org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		return (ProcessLauncher) query.uniqueResult();
	}
	
	@Override
	public List<ProcessLauncher> getLaunchers(Domain domain) {
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeProcessLauncher> criteria = criteriabuilder.createQuery(KbeeProcessLauncher.class);
		Root<KbeeProcessLauncher> launchers = criteria.from(KbeeProcessLauncher.class);
		ParameterExpression<Long> domainparameter = criteriabuilder.parameter(Long.class);
		criteria.select(launchers).where(criteriabuilder.equal(launchers.get("domain").get("id"), domainparameter));;
		criteria.orderBy(criteriabuilder.asc(launchers.get("label")));
	
		TypedQuery<KbeeProcessLauncher> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(domainparameter, (long)domain.getId());
	 	List<ProcessLauncher> result = new ArrayList<ProcessLauncher>();
	 	result.addAll(query.getResultList());
		return result;
	}


	public List<ProcessLauncher> getLaunchers(LauncherGroup lg) {
			return getLaunchers(lg, null);
	}
	
	/**
	 * Lauuncher grop + label
	 */
	@Override
	public List<ProcessLauncher> getLaunchers(LauncherGroup lg, ObjectState state) {
		
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeProcessLauncher> criteria = criteriabuilder.createQuery(KbeeProcessLauncher.class);
		Root<KbeeProcessLauncher> launchers = criteria.from(KbeeProcessLauncher.class);
		
		criteria.orderBy(criteriabuilder.asc(launchers.get("label")));
		TypedQuery<KbeeProcessLauncher> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);

		
		if (state!=null) {
			
			ParameterExpression<Long> stateparameter = criteriabuilder.parameter(Long.class);
			ParameterExpression<Long> lgparameter = criteriabuilder.parameter(Long.class);

			//criteria.select(launchers).where(criteriabuilder.and (criteriabuilder.equal(launchers.get("id"), lgparameter),  
			//								 criteriabuilder.equal(launchers.get("state").get("id"), stateparameter)));
			
			criteria.select(launchers).where(criteriabuilder.and (criteriabuilder.equal(launchers.get("launcherGroup").get("id"), lgparameter),   
					     criteriabuilder.equal(launchers.get("state").get("id"), stateparameter)));
			
			query.setParameter(lgparameter, (long) lg.getId());
			query.setParameter(stateparameter, (long) state.getId());
			
		} else {
			
			ParameterExpression<Long> domainparameter = criteriabuilder.parameter(Long.class);
			criteria.select(launchers).where(criteriabuilder.equal(launchers.get("launcherGroup").get("id"), domainparameter));;
			query.setParameter(domainparameter, (long) lg.getId());
		}

	 	List<ProcessLauncher> result = new ArrayList<ProcessLauncher>();
	 	result.addAll(query.getResultList());
		return result;
	}


	/**
	 * 
	 * All Launcher groups
	 */
	@Override
	public List<LauncherGroup> getLauncherGroups(Domain domain, ObjectState state) {
		
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		
		CriteriaQuery<KbeeLauncherGroup> criteria = criteriabuilder.createQuery(KbeeLauncherGroup.class);
		
		Root<KbeeLauncherGroup> launchers = criteria.from(KbeeLauncherGroup.class);
		ParameterExpression<Long> domainparameter = criteriabuilder.parameter(Long.class);
        ParameterExpression<ObjectState> stateparameter = criteriabuilder.parameter(ObjectState.class);
		
		criteria.select(launchers).where(criteriabuilder.and(
			criteriabuilder.equal(launchers.get("domain").get("id"), domainparameter),
			criteriabuilder.equal(launchers.get("state"), stateparameter)));
		
		criteria.orderBy(criteriabuilder.asc(launchers.get("name")));
		 
		TypedQuery<KbeeLauncherGroup> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);

		query.setParameter(domainparameter, (long)domain.getId());
		query.setParameter(stateparameter, state);
		
		List<LauncherGroup> result = new ArrayList<LauncherGroup>();
	 	result.addAll(query.getResultList());
		return  result;
	}

	
	/**
	 * 
	 */
	public List<ProcessLauncher> getLaunchers(Procedure procedure) {
		return null;
	}

	/**
	 * 
	 */
	@Override
	public List<ProcessLauncher> getLaunchers(Domain domain, ObjectState state) {
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeProcessLauncher> criteria = criteriabuilder.createQuery(KbeeProcessLauncher.class);
		Root<KbeeProcessLauncher> launchers = criteria.from(KbeeProcessLauncher.class);
		ParameterExpression<Long> domainparameter = criteriabuilder.parameter(Long.class);
		criteria.select(launchers).where(criteriabuilder.and (criteriabuilder.equal(launchers.get("domain").get("id"), domainparameter),   criteriabuilder.equal(launchers.get("state"), state)));
		criteria.orderBy(criteriabuilder.asc(launchers.get("label")));
		TypedQuery<KbeeProcessLauncher> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(domainparameter, (long)domain.getId());
	 	List<ProcessLauncher> result = new ArrayList<ProcessLauncher>();
	 	result.addAll(query.getResultList());
		return result;
	}


	
	private WorkflowContext getContext(Content content) {
		String contextstring = (String)content.getService(PropertyService.class).getProperty(Context_Property);
		if (contextstring==null) return null;
		KbeeContext context;
		if (isJSon(contextstring))

			
			
			
			context = parseJSon(contextstring);
		else
			context = parseCSV(contextstring);
		
		if (context==null) {
			logger.error(contextstring + " context is null");
			return null;
		}
		
		context.setContent(content);
		
		return context;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void update(WorkflowContext context) {
		
		KbeeContext kbeecontext = (KbeeContext)context;
		
		Content content = kbeecontext.getContent();
		
		KbeeJson json = new KbeeJson();
		
		if (kbeecontext.getProcess()!=null)
			json.put("process", String.valueOf(kbeecontext.getProcess().getId()));
		
		if (kbeecontext.getProcedure().getId()!=null)
			json.put("procedure", String.valueOf(kbeecontext.getProcedure().getId()));
		else
			json.put("procedure", kbeecontext.getProcedure().getName());
		
		if (kbeecontext.getTask().getId()!=null)
			json.put("task", kbeecontext.getTask().getId());
		else
			json.put("task", kbeecontext.getTask().getName());
		
		if (kbeecontext.getCallerTask()!=null)
			if (kbeecontext.getCallerTask().getId()!=null)
				json.put("callerTask", kbeecontext.getCallerTask().getId());
			else
				json.put("callerTask", kbeecontext.getCallerTask().getName());
		
		if (kbeecontext.getThread()!=null) 
			json.put("thread", kbeecontext.getThread());
		
		if (kbeecontext.getParentActivity()!=null)
			json.put("parentActivity", String.valueOf(kbeecontext.getParentActivity().getId()));
		
		if (kbeecontext.getActivity()!=null)
			json.put("activity", String.valueOf(kbeecontext.getActivity().getId()));
		
		if (kbeecontext.getState()!=null && !"".equals(kbeecontext.getState().trim()))
			json.put("state", kbeecontext.getState());
		
		json.put("priority", kbeecontext.getPriority().name());
		json.put("user", String.valueOf(kbeecontext.getUser().getId()));
		if (kbeecontext.getTime()!=null)
 		json.put("time", String.valueOf(kbeecontext.getTime().toInstant().toEpochMilli()));   // VER DATE JAVA 8 [AF]
		if (kbeecontext.getDueDate()!=null)
		json.put("duedate", String.valueOf(kbeecontext.getDueDate().toInstant().toEpochMilli()));   // VER DATE JAVA 8 [AF]
		if (kbeecontext.getInitiator()!=null)
		json.put("initiator", String.valueOf(kbeecontext.getInitiator().getId()));
		if (kbeecontext.getRequester()!=null)
		json.put("requester", String.valueOf(kbeecontext.getRequester().getId()));
		if (kbeecontext.getCollaborator()!=null)
		json.put("collaborator", String.valueOf(kbeecontext.getCollaborator().getId()));
		if (kbeecontext.getReason()!=null) {
			Map<String, String> reason = new HashMap<>();
			reason.put("code", kbeecontext.getReason().getCode());
			reason.put("label", kbeecontext.getReason().getLabel());
			json.put("reason", reason);
		}

		if (kbeecontext.getNote()!=null)
			json.put("note", escape(kbeecontext.getNote()));
		
		if (!kbeecontext.getParameters().isEmpty()) {
			Map<String, String> parameters = kbeecontext.getParameters();
			for (String parameter : parameters.keySet()) {
				String value = parameters.get(parameter);
				value = value!=null ? escape(value) : null;
				if (value==null || "".equals(value))
					value = "-";
				parameters.put(parameter, value);
			}
			json.put("parameters", parameters);
		}
		
		if (kbeecontext.getCurrentPhase()!=null) {
			json.put("phase", kbeecontext.getCurrentPhase().getName());
		}
		
		if (kbeecontext.getRoles()!=null && !kbeecontext.getRoles().isEmpty()) {
			Map<RoleInProcess, User> roles = kbeecontext.getRoles();
			Map<String, String> jroles = new HashMap<>();
			for (RoleInProcess role : roles.keySet()) {
				User user = roles.get(role);
				if (user!=null)
				jroles.put(role.getName(), String.valueOf(user.getId()));
			}
			json.put("roles", jroles);
		}
		
		if (kbeecontext.getThreads()!=null && !kbeecontext.getThreads().isEmpty()) {
			List<Map<?,?>> threads = new ArrayList<>();
			for (WorkflowThreadStatus thread : kbeecontext.getThreads()) {
				Map jthread = new HashMap();
				KbeeWorkflowThreadStatus kstatus = (KbeeWorkflowThreadStatus)thread;
				if (kstatus.getContent()!=null)
					jthread.put("content", String.valueOf(kstatus.getContent().getId()));
				jthread.put("status", kstatus.getStatus().name());
				jthread.put("thread", kstatus.getThread().getName());
				if (thread.getReason()!=null) {
					Map jreason = new HashMap<>();
					jreason.put("code", thread.getReason().getCode());
					jreason.put("label", thread.getReason().getLabel());
					jthread.put("reason", jreason);
				}
				threads.add(jthread);
			}
			json.put("threads", threads);
		}
		
		if (kbeecontext.getResolution()!=null) {
			json.put("resolution", escape(kbeecontext.getResolution()));
		}
		
		if (kbeecontext.getResolutionTitle()!=null) {
			json.put("resolution-title", escape(kbeecontext.getResolutionTitle()));
		}
		
		if (kbeecontext.isApi()) {
			json.put("isapi", "true");
		}
		
		JsonGeneratorFactory factory = JsonGeneratorFactory.getInstance();
		JSONGenerator generator = factory.newJsonGenerator();
		String stringvalue = generator.generateJson(json.getData());
		
		content.getService(PropertyService.class).setProperty(Context_Property, stringvalue);
		content.getService(PropertyService.class).setProperty(Process_Property, String.valueOf(kbeecontext.getProcess().getId()));
		
		//getContextCache().put(new Element(content.getId(), context));
	}
	
	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return event.getObject() instanceof Procedure;
	}

	@Override
	public void onEvent(Event event) {
		procedures.clear();
		if (event instanceof EvictCacheServiceEvent)
			evict();
	}

	private KbeeContext parseCSV(String contextstring) {
		KbeeContext context = new KbeeContext(getFactory());
		
		StringTokenizer tokens = new StringTokenizer(contextstring,";");
		
		if (!tokens.hasMoreTokens()) return null;
		String processId = tokens.nextToken();
		
		String procedureName = tokens.nextToken();
		Procedure procedure = getProcedure(procedureName);
		Assert.isTrue(procedure!=null, "not procedure");
		
		ProcessProxy process = new ProcessProxy(this);
		process.setId(Long.valueOf(processId));
		process.setProcedure(procedure);
		process.setContext(context);
				
		String taskName = tokens.nextToken();
		Task task = procedure.getTask(taskName);
		Assert.isTrue(task!=null, "not task");
		context.setTask(task);
		
		String priorityvalue = tokens.nextToken();
		Priority priority = Priority.valueOf(priorityvalue);
		context.setPriority(priority);
		
		String userId = tokens.nextToken();
		if (!"".equals(userId)) {
			User user = ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(userId));
			Assert.isTrue(user!=null, "not user");
			context.setUser(user);
		}
		
		String time = tokens.nextToken();
		
		// TODO REVISAR CON AF FECHAS JAVA 8
		//
		OffsetDateTime date = OffsetDateTime.ofInstant(Instant.ofEpochMilli(Long.valueOf(time)), ZoneId.systemDefault()); // [AF] 

		context.setTime(date);
		
		String initiatorId = tokens.nextToken();
		
		if (!"-".equals(initiatorId)) {
			User initiator = ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(initiatorId)); 
			
			context.setInitiator(initiator);
		}
		
		String requesterId = tokens.nextToken();
		if (!"-".equals(requesterId)) {
			User requester = ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(requesterId)); 			
			context.setRequester(requester);
		}

		String collaboratorId = tokens.nextToken();
		if (!"-".equals(collaboratorId)) {
			User collaborator = ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(collaboratorId)); 
			
			context.setCollaborator(collaborator);
		}
		
		String note = tokens.nextToken();
		if (!"-".equals(note)) {
			context.setNote(note);
		}
		
		return context;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private KbeeContext parseJSon(String contextstring) {
		
		if (contextstring == null) 
			return null;
		
		JsonParserFactory factory = JsonParserFactory.getInstance();
		JSONParser parser = factory.newJsonParser();

		Map roots = parser.parseJson(contextstring);
		
		List root = (List)roots.get("root");
		
		Map jsonData = (Map)root.get(0);
		
		KbeeJson json = new KbeeJson(jsonData);
		
		KbeeContext context = new KbeeContext(getFactory());
		
		String processId = (String)json.get("process");
		
		String procedureName = (String)json.get("procedure");

		//if (procedureName.equals("Move In"))
		//	procedureName = "Submission";
		
		
		if (json.get("thread")!=null) {
			context.setThread((String)json.get("thread"));
			Long parentId = json.get("parentActivity")!=null ?
				Long.valueOf((String)json.get("parentActivity")) :
				null;
			Long activityId = json.get("activity")!=null && !"null".equals(json.get("activity")) ?
				Long.valueOf((String)json.get("activity")) :
				null;
			Process p = findProcessById(Long.valueOf(processId));
			for (Activity activity : p.getActivities()) {
				if (parentId!=null && parentId.equals(activity.getId())) {
					context.setParentActivity(activity);
					procedureName = String.valueOf(activity.getProcess().getProcedure().getId());
				}
				if (activityId!=null && activityId.equals(activity.getId())) {
					context.setActivity(activity);
				}
			}
		}
		
		Procedure procedure = getProcedure(procedureName);
		
		
		ProcessProxy process = new ProcessProxy(this);
		process.setId(Long.valueOf(processId));
		process.setProcedure(procedure);
		process.setContext(context);
	
		String taskName = (String)json.get("task");
		Task task = procedure.getTask(taskName);
		
		if (task==null) {
			logger.error(taskName + " Task is null");
			return null;
		}
		
		context.setTask(task);
		
		if (json.get("callerTask")!=null) {
			Task callerTask = procedure.getTask((String)json.get("callerTask"));
			if (callerTask!=null) {
				context.setCallerTask(callerTask);
			}
		}

		
		if (json.get("threads")!=null) {
			List<WorkflowThreadStatus> threads = new ArrayList<>();
			List<Map> jthreads = (List<Map>)json.get("threads");
			for (Map jthread : jthreads) {
				KbeeWorkflowThreadStatus threadstatus = new KbeeWorkflowThreadStatus();
				String name = (String)jthread.get("thread");
				WorkflowThread thread = null;
				if (task instanceof ForkJoinTask) {
					for (WorkflowThread t : ((ForkJoinTask)task).getThreads()) {
						if (t.getName().equals(name)) {
							thread = t;
							break;
						}
					}
				}
				if (thread!=null) {
					threadstatus.setThread(thread);
					threadstatus.setStatus(Status.valueOf((String)jthread.get("status")));
					if (jthread.get("content")!=null) {
						Content content = getContentDao().findContentById(Long.valueOf((String)jthread.get("content")));
						threadstatus.setContent(content);
					}
					if (jthread.get("reason")!=null) {
						Map<String, String> jreason = (Map<String,String>)jthread.get("reason");
						KbeeReason reason = new KbeeReason();
						reason.setCode(jreason.get("code"));
						reason.setLabel(jreason.get("label"));
						threadstatus.setReason(reason);
					}	
					threads.add(threadstatus);
				}
			}
			context.setThreads(threads);
		}
		
		String state = (String)json.get("state");
		context.setState(state);
		
		String priorityvalue = (String)json.get("priority");
		Priority priority = null;
		try {
			priority =Priority.valueOf(priorityvalue);
		} 
		catch (RuntimeException e) {
			priority = Priority.Standard;
		}
		context.setPriority(priority);

		String userId = (String)json.get("user");
		if (!"".equals(userId)) {
			User user = ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(userId));
			context.setUser(user);
		}
		
		String time = (String)json.get("time");
		if (time!=null) {
			Instant instant = Instant.ofEpochMilli(Long.valueOf(time));
			context.setTime(OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()));
		}
		
		String duedate = (String)json.get("duedate");
		if (duedate!=null) {
			Instant instant = Instant.ofEpochMilli(Long.valueOf(duedate));
			context.setDueDate(OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()));
		}
		
		String initiatorId = (String)json.get("initiator");
		if (initiatorId!=null) {
			User initiator = ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(initiatorId));
			Assert.isTrue(initiator!=null, "invalid initiator");
			context.setInitiator(initiator);
		}
		
		String requesterId = (String)json.get("requester");
		if (requesterId!=null && !"null".equals(requesterId)) {
			User requester = ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(requesterId));
			Assert.isTrue(requester!=null, "invalid requester");
			context.setRequester(requester);
		}

		String collaboratorId = (String)json.get("collaborator");
		
		if (collaboratorId!=null) {
			User collaborator = ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(collaboratorId));
			Assert.isTrue(collaborator!=null, "invalid collaborator");
			context.setCollaborator(collaborator);
		}
		
		if (json.get("reason")!=null) {
			Map<String, String> jreason = (Map<String,String>)json.get("reason");
			KbeeReason reason = new KbeeReason();
			reason.setCode(jreason.get("code"));
			reason.setLabel(jreason.get("label"));
			context.setReason(reason);
		}	
		
		String note = (String)json.get("note");
		if (note!=null) {
			context.setNote(unescape(note));
		}
		
		if (json.get("parameters")!=null) {
			Map<String, String> parameters = (Map<String,String>)json.get("parameters");
			for (String parameter : parameters.keySet()) {
				String value = parameters.get(parameter);
				value = unescape(value);
				parameters.put(parameter, value);
			}
			context.setParameters(parameters);
		}
		
		if (json.get("phase")!=null) {
			String phasename = ((String)json.get("phase")).toLowerCase();
			for (ProcedurePhase phase : procedure.getPhases()) {
				if (phase.getName()!=null && phasename.equals(phase.getName().toLowerCase())) {
					context.setCurrentPhase(phase);
					break;
				}
			}
		}
		
		if (json.get("roles")!=null) {
			Map<String, String> jroles = (Map<String,String>)json.get("roles");
			Map<RoleInProcess, User> roles = new HashMap<RoleInProcess, User>();
			for (String rolename : jroles.keySet()) {
				String userid = jroles.get(rolename);
				User user = ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(userid));
				RoleInProcess role = getRole(rolename, procedure);
				if (user!=null && role!=null) {
					roles.put(role, user);
				}
			}
			if (!roles.isEmpty())
			context.setRoles(roles);
		}
		
		if (json.get("resolution")!=null) {
			context.setResolution(unescape((String)json.get("resolution")));
		}
		
		if (json.get("resolution-title")!=null) {
			context.setResolutionTitle(unescape((String)json.get("resolution-title")));
		}
		
		if (json.get("isapi")!=null) {
			context.setApi("true".equals((String)json.get("isapi")));
		}
		
		return context;
	}
	
	private RoleInProcess getRole(String rolename, Procedure procedure) {
		for (RoleInProcess role : procedure.getRoles()) {
			if (rolename.equals(role.getName()))
				return role;
		}
		return null;
	}
	
	private boolean isJSon(String value) {
		return value.startsWith("[");
	}
	
	private String escape(String value) {
		value = value.replace("\\", "");
		value = value.replace("\"", "\\'");
		return value;
	}
	
	private String unescape(String value) {
		value = value.replace("\\'", "\"");
		return value;
	}
	
	@Override
	public synchronized void evict() {
		logger.info("Clearing Workflow Dao Cache");
		this.procedures.clear();
		getContextCache().removeAll();
	}
	
	private Cache getContextCache() {
		if (contextCache==null) {
			synchronized (this) {
				if (contextCache==null) {
					List<CacheManager> managers = CacheManager.ALL_CACHE_MANAGERS;
					for (CacheManager manager : managers) {
						contextCache = manager.getCache(Context_Cache);
						if (contextCache!=null) {
							break;
						}
					}
				}
			}
		}
		return contextCache;
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
