package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.SessionFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.beans.BeansService;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EResourceDistributionModel;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.service.ServiceLocator;

@JsonTypeName("resourcedistribution")
public class KbeeEResourceDistributionFieldModel extends KbeeEResourceSystemFieldModel implements EResourceDistributionModel<ResourceNode>{
	private static final long serialVersionUID = 1L;
	
	private String targetTagId;
	private String doneTagId;
	
	@JsonProperty("launchers")
	private List<String> launchersId;
	
	public void setLaunchersId(List<String> ids) {
		this.launchersId = ids;
	}
	
	public List<String> getLaunchersId() {
		return launchersId;
	}
	
	@JsonIgnore
	public List<ProcessLauncher> getLaunchers() {
		List<ProcessLauncher> launchers = new ArrayList<>();
		if (launchersId!=null) {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			for (String launcherId : launchersId) {
				ProcessLauncher launcher = null;
				try {
					launcher = (ProcessLauncher)sf.getCurrentSession().get(KbeeProcessLauncher.class, Long.valueOf(launcherId));
				}
				catch (ObjectNotFoundException e) {
				}
				if (launcher!=null) {
					launchers.add(launcher);
				}
			}
		}
		return launchers;
	}
	
	public String getTargetTagId() {
		return targetTagId;
	}

	public void setTargetTagId(String targetTagId) {
		this.targetTagId = targetTagId;
	}
	
	public void setTargetTag(ResourceTag tag) {
		this.targetTagId = tag!=null ? String.valueOf(((KbeeResourceTag)tag).getId()) : null;
	}

	public String getDoneTagId() {
		return doneTagId;
	}

	public void setDoneTagId(String doneTagId) {
		this.doneTagId = doneTagId;
	}
	
	public void setDoneTag(ResourceTag tag) {
		this.doneTagId = tag!=null ? String.valueOf(((KbeeResourceTag)tag).getId()) : null;
	}

	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return EResourceDistributionModel.GetTypeLabel();
	}
	
	@JsonIgnore
	public ResourceTag getTargetTag() {
		return getTag(targetTagId, null);
	}
	
	@JsonIgnore
	public ResourceTag getDoneTag() {
		return getTag(doneTagId, null);
	}
	
	@Override
	public String getErrorMessage(Object object) {
		String message = super.getErrorMessage(object);
		ResourceTag targetTag = getTargetTag();
		ResourceTag doneTag = getDoneTag();
		if (targetTag==null && getTargetTagId()!=null) {
			if (message==null) message="";
			message += "Target Tag ";
			message += targetTagId;
			message += " not found";
		}
		if (doneTag==null && getDoneTagId()!=null) {
			if (message==null) message="";
			message += "Done Tag ";
			message += doneTagId;
			message += " not found";
		}
		return message;
	}
} 