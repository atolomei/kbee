package com.novamens.kbee.content.workflow;

import java.io.Serializable;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.novamens.content.base.Resource;
import com.novamens.dom.Indexable;
import com.novamens.kbee.content.resource.AbstractResource;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.audit.AuditSet;
import com.novamens.workflow.Activity;
import com.novamens.workflow.ActivityProgressNote;

@Entity
@Table(name = "WF_PROGRESS_NOTE")
public class KbeeActivityProgressNote extends AbstractObject implements ActivityProgressNote, Indexable {

	@Id
	@GenericGenerator(
		name = "progress_note_sequencer",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "workflow_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "pooled-lo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "progress_note_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeWorkflowActivity.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "activity_id", updatable=false, insertable=false)
	private Activity activity;
	
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity=AbstractResource.class)
	@JoinTable(name = "Wf_Note_Resource", 
		joinColumns = {	@JoinColumn(name = "NOTE_ID", nullable = false, updatable = false) }, 
		inverseJoinColumns = { @JoinColumn(name = "RESOURCE_ID", nullable = false, updatable = false) })
	@OrderColumn(name="position")
	List<Resource> resources = new ArrayList<Resource>();

	@Column(name = "TEXT")
	private String text;

	public Long getId() {
		return id;
	}

	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public String getName() {
		return String.valueOf(id);
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public OffsetDateTime getTime() {
		return getLastModifiedOffsetDateTime();
	}
	
	public String getContentId() {
		String id = activity!=null && ((KbeeWorkflowActivity)activity).getContent()!=null ? "kbeeidoc#"+String.valueOf(((KbeeWorkflowActivity)activity).getContent().getId()) : null;
		return id;
	}
	
	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}
	
	public void addResource(Resource resource) {
		this.resources.add(resource);
	}
	
	public void removeResource(Resource resource) {
		this.resources.remove(resource);
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.CONTENT;
	}
}