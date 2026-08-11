package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.resource.KBFile;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.dom.Json;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.Principal;
import com.novamens.security.audit.AuditSet;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.WorkflowContext;

import kbee.util.logging.Logger;

import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.Process;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Task;

/**
 * 
 * This is the definition of a Workflow procedure
 *
 * Wf_Procedure
  *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.INTEGER)
@Table(name = "Wf_Procedure")
public class KbeeProcedure extends AbstractObject implements com.novamens.content.workflow.KbeeProcedure  {
				
	private static Logger logger = Logger.getLogger(KbeeProcedure.class.getName());
	
	@Id   
	@SequenceGenerator(name = "procedure_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "procedure_sequencer")
	@Column(name = "Id")
	private Long id;
	
	@Column(name = "Alias")
	private String alias;
	
	@Column(name = "code")
	private String code;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "TYPE", insertable=false, updatable=false)
	private int type;
	
	@Column(name = "version")
	private Integer version;
	
	@Column(name = "launcher")
	private String launcher;
	
	@Column(name = "Tasks")
	private String jsontasks;
	
	@Column(name = "Initial_Rules")
	private String jsonrule;
	
	@Column(name = "Roles")
	private String jsonroles;
	
	@Column(name = "Phases")
	private String jsonphases;
	
	@Column(name = "Subprocedures")
	private String jsonsubprocedures;
	
	@Column(name = "States")
	private String statesclass;

	@Column(name = "description")
	private String description;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KBFileImpl.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "diagram")
	private KBFile diagram;
	
	transient private List<Task> tasks;
	transient private WorkflowRule initialRule;
	transient private StatesMachine states;
	transient private String bean;
	transient private List<RoleInProcess> roles;
	transient private List<ProcedurePhase> phases;
	transient private Map<String, String> event_labels = new HashMap<String, String>();
	transient private List<Procedure> subprocedures;
	
	public KbeeProcedure() {
	}
	
	public KbeeProcedure(Procedure procedure) {
		
		setName(procedure.getName());
		setCode(procedure.getCode());
		
		setStates(((KbeeProcedureBean)procedure).getStates());
		this.bean = ((KbeeProcedureBean)procedure).getBeanName();
		setLastModifiedOffsetDateTime(OffsetDateTime.now());
		setVersion(((KbeeProcedureBean)procedure).getVersion());
		
		setState(ObjectState.ENABLED);
		setAlias(String.valueOf(procedure.getId()));
		setTasks(procedure.getTasks());
		if (procedure.getRoles()!=null)
		setRoles(procedure.getRoles());
		
		this.setPhases(procedure.getPhases());
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	@Override
	public Serializable getId()	{
		return id;
	}
	
	public void setDescription(String d) {
		this.description=d;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public void setDisplayName(String name) {
		setName(name);
	}
	
	public String getDisplayName() {
		return getName();
	}
	
	public String getBean() {
		return bean;
	}
	
	public void setLauncher(String name) {
		this.launcher = name;
	}
	
	public String getLauncher() {
		return launcher==null ? getName() : launcher;
	}
	
	public void setAlias(String name) {
		this.alias = name;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		
		if (code!=null) {
			return code.trim();
		}else if(getName()!=null) {
			if(getName().length()>2) {
		return getName().substring(0, 2).toUpperCase();
			}else {
				return getName().toUpperCase();		
				}
		}else {
			return null;
		}
	}
	
	public Activity initiate(WorkflowContext context) {
		return this.handle(StatesMachine.Initialization_Event, context);
	}

	public Process start(WorkflowContext context) {
		return getNewProcess(context).start();
	}
	
	public void cancel(WorkflowContext context) {
		this.handle(StatesMachine.Cancelation_Event, context);
	}
	
	public Activity handle(String event, WorkflowContext context) {
		return getStates().handle(event, context);
	}
	
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
		this.jsontasks = getString(getJsonTasks(tasks));
		tasks =jsontasks!=null ? parseTasks(getJson(jsontasks)) : new ArrayList<>();
	}
	
	public List<Task> getTasks() {
		if (tasks==null) {
			tasks = parseTasks(getJson(jsontasks));
		}	
		return tasks;
	}
	
	public List<Procedure> getSubprocedures() {
		if (subprocedures==null) {
			subprocedures = parseSubprocedures(getJson(jsonsubprocedures));
		}	
		return subprocedures;
	}
	
	public void setSubprocedures(List<Procedure> procedures) {
		this.subprocedures = procedures;
		this.jsonsubprocedures = getString(getJsonProcedures(procedures));
		subprocedures = jsonsubprocedures!=null ? parseSubprocedures(getJson(jsonsubprocedures)) : new ArrayList<>();
	}
	
	public Task getTask(String name) {
		for (Task task : getTasks()) {
			if (task.getId()!=null && task.getId().equals(name)) {
				return task;
			}
		}
		for (Task task : getTasks()) {
			if (task.getName().equals(name) || (((KbeeTask)task).getAlias()!=null && ((KbeeTask)task).getAlias().equals(name))) {
				return task;
			}
		}
		for (Procedure procedure : getSubprocedures()) {
			Task task = procedure.getTask(name);
			if (task!=null) return task;
		}
		return null;
	}
	
	public Task getInitial() {
		for (Task task : getTasks()) {
			if (task.isInitial()) {
				return task;
			}
		}
		return null;
	}

	
	public void setInitialRule(WorkflowRule rule) {
		this.jsonrule = getString(getJson(rule));
		this.initialRule = rule;
	}
	
	public WorkflowRule getInitialRule() {
		if (initialRule==null) {
			if (jsonrule!=null) {
				initialRule = parseRule(getJson(jsonrule));
			}	
		}	
		return initialRule;
	}
	
	public void setStates(StatesMachine states) {
		this.states = states;
		this.statesclass = states.getClass().getName();
	}
	
	public StatesMachine getStates() {
		if (this.states==null) {
			try {
				Class<?> javaclass = Class.forName(this.statesclass);
				@SuppressWarnings("deprecation")
				Object states = javaclass.newInstance();
				if (!(states instanceof StatesMachine))
					throw new InstantiationException();
				this.states = (StatesMachine)states;
			}
			catch (ClassNotFoundException e) {
				logger.error(e);
				throw new RuntimeException(e);
			}
			catch (InstantiationException e)  {
				logger.error(e);
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e)  {
				logger.error(e);
				throw new RuntimeException(e);
			}
		}
		return this.states;
	}
	
	public void setRoles(List<RoleInProcess> roles) {
		this.jsonroles = getString(getJsonRoles(roles));
		this.roles = roles;
	}
	
	public List<RoleInProcess> getRoles() {
		if (this.roles == null) {
			if (this.jsonroles == null) {
				this.roles = getStates().getRoles();
				setRoles(this.roles);		
			}
			else {
				this.roles = parseRoles(getJson(this.jsonroles));
				for (RoleInProcess staterole : getStates().getRoles()) {
					boolean found = false;
					for (RoleInProcess role : this.roles) {
						if (role.getName().equals(staterole.getName())) {
							found = true;
							break;
						}
					}
					if (!found) {
						this.roles.add(staterole);
					}
				}
			}	
		}
		return this.roles;
	}
	
	public Map<RoleInProcess, List<Principal>> getRoles(WorkflowContext context) {
		return getStates().getRoles(context);
	}
	
	public List<ProcedurePhase> getPhases() {
		if (phases==null) {
			phases = PhaseParser.Get().getPhases(getJson(this.jsonphases));
		}
		return phases; 
	}
	
	public void setPhases(List<ProcedurePhase> phases) {
		this.jsonphases = getString(getJsonPhases(phases));
		this.phases = phases;
	}
	
	public String getLabel(String eventId) {
		if (this.event_labels.containsKey(eventId))
			return this.event_labels.get(eventId);
		
		for (Task task : getTasks()) {
			if (task instanceof KbeeTask) {
				for (com.novamens.content.workflow.EndCondition condition : ((KbeeTask)task).getEndConditions()) {
					if (eventId.equals(condition.getEvent())) {
						this.event_labels.put(eventId, condition.getLabel());
						return condition.getLabel();
					}
				}
			}
		}
		return null;
	}

	public KBFile getDiagram() {
		return diagram;
	}
	
	public void setDiagram(KBFile diagram) {
		this.diagram = diagram;
	}
	
	public Procedure getMaster() {
		return this;
	}
	
	public Procedure clone() {
		KbeeProcedure clone = new KbeeProcedure();
		super.onClone(clone);
		
		clone.setCode(getCode());
		clone.setName(getName());
		clone.setAlias(getAlias());
		clone.setStates(getStates());
		clone.setTasks(getTasks());
		clone.setPhases(getPhases());
		clone.setLastModifiedUser(getLastModifiedUser());
		clone.setLastModifiedOffsetDateTime(getLastModifiedOffsetDateTime());

		if (getInitialRule()!=null)
			clone.setInitialRule(getInitialRule());
		
		clone.setDiagram(getDiagram());
		return clone;
	}
	
	public void update() {
		setTasks(getTasks());
		setSubprocedures(getSubprocedures());
	}
	
	public boolean isBean() {
		return false;
	}
	
	@Override
	public Locale getLocale() {
		return Locale.getDefault();
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.MODEL;
	}
	
	protected Process getNewProcess(WorkflowContext context) {
		return context.getFactory().createProcess(this, context);
	}
	
	private Json getJsonTasks(List<Task> tasks) {
		return TaskParser.Get().getJson(tasks);
	}
	
	private Json getJsonProcedures(List<Procedure> procedures) {
		return ProcedureParser.Get().getJson(procedures);
	}
	
	private Json getJson(WorkflowRule rule) {
		return RuleParser.Get().getJson(rule);
	}
	
	private Json getJsonRoles(List<RoleInProcess> roles) {
		return RoleParser.Get().getJson(roles);
	}
	
	private Json getJsonPhases(List<ProcedurePhase> phases) {
		return PhaseParser.Get().getJson(phases);
	}
	
	private List<Task> parseTasks(Json json) {
		List<Task> tasks = TaskParser.Get().getTasks(json, this);
		return tasks;
	}
	
	private List<Procedure> parseSubprocedures(Json json) {
		List<Procedure> procedures = ProcedureParser.Get().getProcedures(json, this);
		return procedures;
	}
	
	private List<RoleInProcess> parseRoles(Json json) {
		List<RoleInProcess> roles = RoleParser.Get().getRoles(json);
		return roles;
	}
	
	private WorkflowRule parseRule(Json json) {
		WorkflowRule rule = RuleParser.Get().getRule(json);
		return rule;
	}
	
	private Json getJson(String string) {
		try {
			return string==null ? null : new KbeeJson(string);
		} 
		catch (Exception e) {
			logger.error(e, string);
			return new KbeeJson();
		}
	}
	
	private String getString(Json json) {
		return json!=null ? json.toString() : null;
	}
}