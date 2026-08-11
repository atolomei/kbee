package com.novamens.kbee.content.workflow;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeePermission;

import com.novamens.security.acl.Acl;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;
import com.novamens.workflow.WorkflowContext;

import kbee.util.logging.Logger;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "Wf_Launcher")
public class KbeeProcessLauncher extends AbstractObject implements ProcessLauncher {
	
	private static Logger logger = Logger.getLogger(KbeeProcessLauncher.class.getName());
			 
	@Id 
	@SequenceGenerator(name = "launcher_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "launcher_sequencer")
	@Column(name = "Id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDomain.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "domain_id", updatable=false)
	private Domain domain;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeProcedure.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "Procedure_Id", insertable=false, updatable=false, nullable=false)
	private Procedure procedure;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeContentTemplate.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "ContentTemplate_Id")
	private ContentTemplate template;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeAcl.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "acl")
	private Acl acl;
												
	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.DETACH, targetEntity = KbeeLauncherGroup.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "launcherGroup_Id")
	private LauncherGroup launcherGroup;
	
	@Column(name = "condition")
	private String condition;
	
	@Override
	public LauncherGroup getLauncherGroup() {
		return this.launcherGroup;
	}
	
	public void setLauncherGroup(LauncherGroup  s) {
		this.launcherGroup=s;
	}
	
	@Column(name = "label")
	private String label;
 	
	@Column(name = "Contextual")
	private boolean enabledContext;
	
	@Column(name = "isenabled")
	private boolean isenabled;
	
	@Column(name = "apienabled")
	private boolean apienabled;
	
	@Column(name = "mobile")
	private boolean mobile;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "rules")
	private String jsonrule;
	
	@Column(name = "router")
	private String router;
	
	@Column(name = "usetemplate")
	private boolean usetemplate;

	
	transient private WorkflowRule rule;
	
	public KbeeProcessLauncher() {
		super();
	}

	public KbeeProcessLauncher(KbeeProcessLauncher src) {
		super();
		
		this.domain=src.domain;
		this.template=src.template;
		this.label=src.label;
		this.enabledContext=src.enabledContext;
		this.isenabled=src.isenabled;
		this.procedure=src.procedure;
		this.description=src.description;
	}
	
	public Serializable getId() {
		return id;
	}
	
	@Override
	public void setId(Serializable id) {
		this.id=(Long) id;
	}
	
	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public Domain getDomain() {
		return this.domain;
	}

	@Override
	public ContentTemplate getContentTemplate() {
		return template;
	}
	
	public void setContentTemplate(ContentTemplate template) {
		this.template = template;
	}
	
	
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public String getRouter() {
		return router;
	}

	public void setRouter(String condition) {
		this.router = condition;
	}

	@Override
	public Procedure getProcedure() {
		return procedure;
	}
	
	public void setProcedure(Procedure procedure) {
		
		if (procedure instanceof KbeeProcedureBean)
			this.procedure = new KbeeProcedure(procedure);
		else
			this.procedure = procedure;
		
		if (((KbeeProcedure)this.procedure).getDomain()==null)
			((KbeeProcedure)this.procedure).setDomain(getDomain());
		
		if (((KbeeProcedure)this.procedure).getLastModifiedUser()==null)
			((KbeeProcedure)this.procedure).setLastModifiedUser(getSessionUser());
		
		((KbeeProcedure)this.procedure).setLauncher(getLabel());
	}
	
	@Override
	public String getLabel() {
		return label!=null ? label : (procedure!=null ? procedure.getName() : "-");
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public Acl getAcl() {
		return acl;
	}

	public void setAcl(Acl acl) {
		this.acl = acl; 
	}
	
	public void setLibrary(boolean value) {
		this.enabledContext = value;
	}

	public boolean getLibrary() {
		return  this.enabledContext;
	}
	
	public boolean isLibrary() {
		return enabledContext;
	}

	@Override
	public boolean isEnabled() {
		return this.isenabled;
	}
	
	public void setEnabled(boolean b) {
		this.isenabled=b;
	}

	public boolean isApiEnabled() {
		return apienabled;
	}
	
	public boolean isUseTemplate() {
		return usetemplate;
	}
	
	public boolean useTemplate() {
		return usetemplate;
	}
	
	public void setUseTemplate(boolean value) {
		usetemplate = value;
	}

	public void setApiEnabled(boolean apienabled) {
		this.apienabled = apienabled;
	}
	
	public boolean isMobile() {
		return mobile;
	}

	public void setMobile(boolean mobile) {
		this.mobile = mobile;
	}

	@Override
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String b) {
		this.description=b;
	}
	
	@Override
	public String getDisplayName() {
		return label;
	}

	@Override
	public String getName() {
		return this.getDisplayName();
	}
	
	public boolean isEnabled(Content content) {
		if (condition!=null) {
			KbeeContext context = new KbeeContext(null);
			context.setContent(content);
			Object evaluation = (new JsEvaluator(getCondition())).evaluate(context);
			return Boolean.TRUE.equals(evaluation);
		}
		return true;
	}
	
	public void setRule(WorkflowRule rule) {
		this.jsonrule = getString(getJson(rule));
		this.rule = rule;
	}
	
	public WorkflowRule getRule() {
		if (rule==null) {
			if (jsonrule!=null) {
				rule = parseRule(getJson(jsonrule));
			}	
		}	
		return rule;
	}
	
	@Override
	public boolean executeable() {
		
		if (getSessionUser().getName().startsWith("root@")) 
			return true;
		
		if (ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()))
			return true;
		
		if (ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId()))
			return false;
		
		if (getAcl()!=null)
			return getAcl().checkPermission(getSessionUser(), KbeePermission.CREATE);
		
		return true;
	}
	
	public Process startProcess() throws ContentCreationException, ContentMgmtException {
		Content content = ServiceLocator.getService(ContentFactoryService.class).create(getContentTemplate().getName());
		Process process = content.getService(WorkflowService.class).startProcess(getProcedure());
		if (getRule()!=null) {
			getRule().execute(process.getContext());
		}
		return process;
	}
	
	public Process startProcess(Content template) throws ContentCreationException, ContentMgmtException {
		Content content = template.getService(ContentService.class).clone();
		Process process = content.getService(WorkflowService.class).startProcess(getProcedure());
		if (getRule()!=null) {
			getRule().execute(process.getContext());
		}
		return process;
	}

	
	public Process startProcess(WorkflowContext context) {
		Process process = getProcedure().start(context);
		if (getRule()!=null) {
			getRule().execute(process.getContext());
		}
		return process;
	}
	
	public Process startProcess(WorkflowContext context, Object initialData) {
		Process process = getProcedure().start(context);
		if (getRule()!=null) {
			KbeeContext processContext = (KbeeContext)process.getContext();
			processContext.setInitialData(initialData);
			getRule().execute(processContext);
		}
		return process;
	}

	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.MODEL;
	}
	
	private Json getJson(String string) {
		try {
			return string==null ? null : new KbeeJson(string);
		} 
		catch (Exception e) {
			logger.error(e);
			return new KbeeJson();
		}
	}
	
	private Json getJson(WorkflowRule rule) {
		return RuleParser.Get().getJson(rule);
	}
	
	private WorkflowRule parseRule(Json json) {
		WorkflowRule rule = RuleParser.Get().getRule(json);
		return rule;
	}
	
	private String getString(Json json) {
		return json!=null ? json.toString() : null;
	}
}