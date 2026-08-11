package com.novamens.kbee.content.webapi.type;

import com.novamens.content.model.LauncherGroup;
import com.novamens.content.workflow.ProcessLauncher;

import kbee.api.model.ApiProxy;
import kbee.api.model.ILauncher;

public class ILauncherAdapter implements Adapter<ProcessLauncher, ILauncher> {
	
	public ILauncherAdapter() {
	}
	
	public ILauncher adapt(ProcessLauncher launcher) {
		ILauncher ilauncher = new ILauncher();
		ilauncher.setId(String.valueOf(launcher.getId()));
		ilauncher.setDomain(launcher.getContentTemplate().getDomain().getName());
		ilauncher.setDisplayName(launcher.getDisplayName());
		ilauncher.setNewDocumentEnabled(launcher.isEnabled());
		ilauncher.setLibraryEnabled(launcher.isLibrary());
		ilauncher.setApiEnabled(launcher.isApiEnabled());
		ilauncher.setMobile(launcher.isMobile());
		ilauncher.setGroup(launcher.getLauncherGroup()!=null?getProxy(launcher.getLauncherGroup()):null);
		ilauncher.setAcl((new IAclAdapter()).adapt(launcher.getAcl()));
		ilauncher.setProcedure(new ApiProxy(String.valueOf(launcher.getProcedure().getId()), launcher.getProcedure().getName(), UriHelper.getUri(launcher.getProcedure()), "procedure"));
		ilauncher.setTemplate(new ApiProxy(String.valueOf(launcher.getContentTemplate().getId()), launcher.getContentTemplate().getName(), UriHelper.getUri(launcher.getContentTemplate()), "template"));
		ilauncher.setDescription(launcher.getDescription());
		return ilauncher;	
	}
	
	public ApiProxy getProxy(LauncherGroup group) {
		ApiProxy proxy = new ApiProxy(UriHelper.getUri(group));
		proxy.setId(String.valueOf(group.getId()));
		proxy.setRel("launchergroup");
		proxy.setName(group.getAlias());
		return proxy;
	}
}