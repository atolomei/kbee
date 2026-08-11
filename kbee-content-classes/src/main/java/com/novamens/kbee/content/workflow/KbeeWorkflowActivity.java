package com.novamens.kbee.content.workflow;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.MemberRole;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.form.KbeeEFormActivityData;
import com.novamens.kbee.content.model.KbeeEntityMember;
import com.novamens.kbee.content.model.KbeeMemberRole;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.workflow.Activity;
import com.novamens.workflow.ActivityProgressNote;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.Task;
import com.novamens.workflow.Process;

/**
 * 
 *  Wf_Procedure
 *
 *  WF_ACTIVITY -> Executing Task 
 *  WF_PROCESS -> 
 *
 */
@Entity
@Table(name = "WF_ACTIVITY")
public class KbeeWorkflowActivity implements Activity  {
	
	@Id
	@GenericGenerator(
		name = "activity_sequencer",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "workflow_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "pooled-lo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "activity_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeProcess.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "process_id", updatable=false, insertable=false)
	private Process process;
			
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "USER_ID")
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "ASSIGNED_BY")
	private User assignedBy;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeContent.class)
	@JoinColumn(name="CONTENT_ID")
	private Content content;
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeEFormActivityData.class)
	@JoinColumn(name="activity_id", insertable=false, nullable=false)
	List<EFormData> formsdata = new ArrayList<EFormData>();
	
	private transient Task task;
	
	@Column(name = "TASK")
	private String taskName;
	
	@Column(name = "STARTIME")
	private OffsetDateTime startTime;
	
	@Column(name = "ENDTIME")
	private OffsetDateTime endTime;
	
	@Column(name = "duedate")
	private OffsetDateTime duedate;

	@Column(name = "EVENT")
	private String event;
	
	@Column(name = "NOTE")
	private String note;
	
	@Column(name = "RESOLUTION")
	private String resolution;

	@Column(name = "RESOLUTIONTITLE")
	private String resolution_title;

	@Column(name = "STATUS")
	private String statusValue;
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeActivityProgressNote.class)
	@JoinColumn(name="activity_id", insertable=false, nullable=false)
	List<ActivityProgressNote> progressNotes = new ArrayList<ActivityProgressNote>();

	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeWorkflowActivity.class)
	@JoinColumn(name="parent_id")
	Activity parent;
	
	@Column(name = "THREAD")
	private String thread;

	private transient WorkflowContext context;
	
	public KbeeWorkflowActivity() {
		
	}
	
	public KbeeWorkflowActivity(Task task, WorkflowContext context, User user) {
		setContent(((KbeeContext)context).getContent());
		Process process = context.getProcess();
		
		if (process instanceof ProcessProxy) {
			process = ((ProcessProxy)process).getProcess();
		}
		
		setProcess(process);
		setTask(task);
		setUser(user);
		this.duedate=context.getDueDate();
		start();
	}
	
	public Long getId() {
		return id;
	}
	
	public void start() {
		setStatus(Status.RUNNING);
		setStartTime(OffsetDateTime.now());
	}

	public void end() {
		setStatus(Status.TERMINATED);
		setEndTime(OffsetDateTime.now());
	}
	
	public void cancel() {
		setStatus(Status.CANCELED);
		setEndTime(OffsetDateTime.now());
	}
	
	public void assign(User user) {
		setUser(user);
	}
	
	public Task getTask() {
		return task;
	}
	
	public void setTask(Task task) {
		this.task = task;
		this.taskName = task.getId();
	}
	
	public String getTaskName() {
		return taskName;
	}
	
	public Process getProcess() {
		return process;
	}
	
	public WorkflowContext getContext() {
		return context;
	}
	
	public void setContext(WorkflowContext context) {
		this.context = context;
	}
	
	public void setContent(Content content) {
		this.content = content;
	}
	
	public Content getContent() {
		return content;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getAssignedBy() {
		return assignedBy;
	}
	
	public void setAssignedBy(User user) {
		this.assignedBy = user;
	}
	
	public void setProcess(Process process) {
		this.process = process;
	}
	
	public OffsetDateTime getStartTime() {
		return startTime;
	}
	
	public void setStartTime(OffsetDateTime time) {
		this.startTime = time;
	}
	
	public OffsetDateTime getEndTime() {
		return endTime;
	}
	
	public void setEndTime(OffsetDateTime time) {
		this.endTime = time;
	}
	
	public Status getStatus() {
		return Status.valueOf(statusValue);
	}
	
	public void setStatus(Status status) {
		this.statusValue = status.toString();
	}
	
	public Activity getParent() {
		return parent;
	}

	public void setParent(Activity parent) {
		this.parent = parent;
	}
	
	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	public String getEvent() {
		return event;
	}
	
	public void setEvent(String event) {
		this.event = event;
	}
	
	public String getEventLabel() {
		return event;
	}

	public String getNote() {
		return note;
	}
	
	public void setNote(String note) {
		this.note = note;
	}
	
	public String getResolution() {
		return this.resolution;
	}

	public String getResolutionTitle() {
		return this.resolution_title;
	}
	
	public void setResolutionTitle(String t) {
		this.resolution_title = t;
	}
	
	public void setResolution(String text) {
		this.resolution = text;
	}
	
	public boolean isRunning() {
		return getStatus().equals(Activity.Status.RUNNING);
	}

	@Override
	public String getDisplayName() {
		return this.getClass().getName();
	}
	
	@Override
	public List<Group> getEnabledGroups() {
		List<Group> groups = new ArrayList<Group>();
		groups.addAll(((KbeeTask)getTask()).getEnabledGroups());
		for (Role role : ((KbeeTask)getTask()).getEnabledRoles()) {
			if (role.isEntity()) {
				Classifier classifier = ((EntityRole)role).getClassifier();
				for (Classification classification : getContent().getClassification()) {
					if (classification.getClassifier().equals(classifier)) {
						Group group = getGroup(role, classification.getDataSetMember());
						if (group!=null) {
							groups.add(group);
						}	
					}
				}
			}
			else {
				groups.add(((KbeeAbstractRole)role).getGroup());
			}
		}
		return groups;
	}
	
	public List<EFormData> getFormsData() {
		return formsdata;
	}
	
	public void setFormData(String caputure, EFormData data) {
		EForm eform = data.getForm() instanceof KbeeTaskForm ? ((KbeeTaskForm)data.getForm()).getForm() : data.getForm(); 
		KbeeEFormActivityData activitydata = new KbeeEFormActivityData(this, eform);
		for (EFormField<?> field : data.getForm().getFields()) {
			activitydata.setData(field, data.getData(field));
		}
		activitydata.setCapture(caputure);
		if (data.isSigned()) {
			activitydata.setSignatures(data.getSignatures());
		}
		getFormsData().add(activitydata);
	}
  
	public void setDueDate(OffsetDateTime d) {
		this.duedate=d;
	}
		
	@Override
	public OffsetDateTime getDueDate() {
		return duedate;
	}
	
	@Override
	public List<ActivityProgressNote> getProgressNotes() {
		return progressNotes;
	}
	
	public void setProgressNotes(List<ActivityProgressNote> notes) {
		this.progressNotes = notes;
	}
	
	public void addProgressNote(ActivityProgressNote note) {
		this.progressNotes.add(note);
	}
	
	public void deleteProgressNote(ActivityProgressNote notetodelete) {
		for (ActivityProgressNote note : getProgressNotes()) {
			if (note.equals(notetodelete)) {
				if (ObjectState.DRAFT.equals(note.getState())) {
					progressNotes.remove(note);
					break;
				}
				if (ObjectState.ENABLED.equals(note.getState())) {
					((KbeeActivityProgressNote)note).setLastModifiedOffsetDateTime(OffsetDateTime.now());
					((KbeeActivityProgressNote)note).setState(ObjectState.DELETED);
					break;
				}
			}
		}
	}
	
	private Group getGroup(Role role, DataSetMember member) {
		if (!(member instanceof EntityMember)) return null;
		KbeeEntityMember entity = (KbeeEntityMember)member;
		for (MemberRole memberRole :  entity.getRoles()) {
			if (role==null || role.equals(memberRole.getRole())) {
				return ((KbeeMemberRole)memberRole).getGroup();
			}
		}
		return null;
	}
}
