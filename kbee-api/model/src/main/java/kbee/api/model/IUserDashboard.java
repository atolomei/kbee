package kbee.api.model;

import java.io.Serializable;
import java.util.List;

public class IUserDashboard implements Serializable {
	private static final long serialVersionUID = 1L;

	private int workspace;
	private int workspaceUnread;
	private int monitor;
	private boolean monitorEnabled;
	private int pendings;
	private boolean pendingsEnabled;
	private boolean valuesEnabled;
	ApiDomain domain;
	private ApiUser user;
	private List<ILibrary> libraries;
	private int signatureLevel = 0;

	public int getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace(int workspace) {
		this.workspace = workspace;
	}
	
	public int getWorkspaceUnread() {
		return workspaceUnread;
	}
	
	public void setWorkspaceUnread(int workspaceUnread) {
		this.workspaceUnread = workspaceUnread;
	}
	
	
	public boolean isMonitorEnabled() {
		return monitorEnabled;
	}

	public void setMonitorEnabled(boolean monitorEnabled) {
		this.monitorEnabled = monitorEnabled;
	}

	public int getMonitor() {
		return monitor;
	}
	
	public void setMonitor(int monitor) {
		this.monitor = monitor;
	}
	
	public boolean isPendingsEnabled() {
		return pendingsEnabled;
	}

	public void setPendingsEnabled(boolean pendingsEnabled) {
		this.pendingsEnabled = pendingsEnabled;
	}

	public int getPendings() {
		return pendings;
	}

	public void setPendings(int pendings) {
		this.pendings = pendings;
	}
	
	public boolean isValuesEnabled() {
		return valuesEnabled;
	}

	public void setValuesEnabled(boolean valuesEnabled) {
		this.valuesEnabled = valuesEnabled;
	}

	public ApiDomain getDomain() {
		return domain;
	}
	
	public void setDomain(ApiDomain domain) {
		this.domain = domain;
	}
	
	public ApiUser getUser() {
		return user;
	}
	
	public void setUser(ApiUser user) {
		this.user = user;
	}

	public List<ILibrary> getLibraries() {
		return libraries;
	}

	public void setLibraries(List<ILibrary> libraries) {
		this.libraries = libraries;
	}

	public int getSignatureLevel() {
		return signatureLevel;
	}

	public void setSignatureLevel(int signatureLevel) {
		this.signatureLevel = signatureLevel;
	}
}