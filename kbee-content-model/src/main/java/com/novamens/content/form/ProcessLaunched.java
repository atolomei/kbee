package com.novamens.content.form;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.base.Resource;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.security.User;
import com.novamens.workflow.Procedure;

@JsonTypeName("process launched")
public class ProcessLaunched extends AbstractUpdatedField {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("resources")
	private List<String> resources;
	
	@JsonProperty("procedure")
	private String procedure;
	
	@JsonProperty("sendTo")
	private String sendTo;
	
	public ProcessLaunched() {
	}
	
	public ProcessLaunched(EForm form, String field, ProcessLauncher launcher, List<Resource> resources, User user) {
		setForm(form);
		setField(field);
		setResources(resources);
		setProcedure(launcher.getProcedure());
		setSendTo(user);
	}
	
	@JsonIgnore
	public void setResources(List<Resource> resources) {
		List<String> names = new ArrayList<String>();
		for (Resource resource : resources) {
			names.add(resource.getDisplayName());
		}
		this.resources = names;
	}
	
	public List<String> getResources() {
		return resources;
	}

	@JsonIgnore
	public void setSendTo(User user) {
		this.sendTo = user!=null ? user.getDisplayName() : null;
	}
	
	public String getSendTo() {
		return sendTo;
	}
	
	@JsonIgnore
	public void setProcedure(Procedure procedure) {
		this.procedure = procedure.getDisplayName();
	}
	
	public String getProcedure() {
		return procedure;
	}

	@Override
	@JsonIgnore
	public String getAction() {
		return "";
	}
	
	@Override
	@JsonIgnore
	public String getLabel() {
		String label;
		label = getProcedure();
		label +=" was created with ";
		int i=0;
		for (String resource : resources) {
			if (i>0) {
				if (i<resources.size()-1) {
					label += ", ";
				}
				else {
					label += " and ";
				}
			}
			label += resource;
			i++;
		}
		label += " and sended to " + getSendTo();
		return label;
	}
	
	public String getType() {
		return "process launched";
	}
	
	public boolean same(UpdatedField field) {
		if (!(field instanceof ProcessLaunched)) return false;
		return false;
	}
}