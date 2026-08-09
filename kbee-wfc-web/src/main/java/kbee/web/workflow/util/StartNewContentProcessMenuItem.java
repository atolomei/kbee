package kbee.web.workflow.util;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItem;
import com.novamens.workflow.Process;

public class StartNewContentProcessMenuItem extends AjaxMenuItem {
	private static final long serialVersionUID = 1L;
	private String label;
	
	public StartNewContentProcessMenuItem(ProcessLauncher launcher) {
		setLabel(launcher.getLabel());
	}
	
	public void onClick(AjaxRequestTarget target) throws Exception {
		for(ProcessLauncher launcher : getDomain().getService(WorkflowDomainService.class).getLaunchers()) {
			if (getLabel().equals(launcher.getLabel())) {
				Process process = getDomain().getService(WorkflowDomainService.class).startProcess(launcher);
				onStart(process, target);
				break;
			}
		}
	}
	
	public void onStart(Process process, AjaxRequestTarget target) {
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
	@Override
	public String getTarget() {
		return null;
	}
	
	@Override
	public String getCssClass() {
		return null;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}