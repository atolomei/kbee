package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.workflow.Procedure;

@Entity
@Table(name = "Wf_Procedure")
@DiscriminatorValue(value="1")
public class KbeeContentProcedure extends KbeeProcedure implements ContentProcedure {

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeContentTemplate.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "ContentTemplate_Id", insertable=false, updatable=false, nullable=false)
	private ContentTemplate contenttemplate;
	
	@OneToMany(fetch = FetchType.LAZY, targetEntity = KbeeProcessLauncher.class)
	@JoinColumn(name = "procedure_id") 
	List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
	
	public KbeeContentProcedure() {
	}
	
	public KbeeContentProcedure(Procedure procedure) {
		super(procedure);
	}

	public ContentTemplate getContentTemplate() {
		return contenttemplate;
	}

	public void setContentTemplate(ContentTemplate contenttemplate) {
		this.contenttemplate = contenttemplate;
	}

	public List<ProcessLauncher> getProcessLaunchers() {
		return launchers;
	}
	
	public void addLauncher(ProcessLauncher launcher) {
		((KbeeProcessLauncher)launcher).setContentTemplate(getContentTemplate());
		((KbeeProcessLauncher)launcher).setProcedure(this);
		launchers.add(launcher);
	}
	
	public void removeLauncher(ProcessLauncher launcher) {
		launchers.remove(launcher);
	}

	public void setProcessLaunchers(List<ProcessLauncher> launchers) {
		this.launchers = launchers;
	}
}
