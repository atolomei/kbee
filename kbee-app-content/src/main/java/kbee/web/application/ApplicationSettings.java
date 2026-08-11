package kbee.web.application;

import java.lang.ref.WeakReference;

import org.apache.wicket.Page;
import org.apache.wicket.util.lang.Args;

public class ApplicationSettings extends org.apache.wicket.settings.ApplicationSettings {

	private WeakReference<Class<? extends Page>> resourceErrorPage, workflowErrorPage;
	
	public Class<? extends Page> getResourceErrorPage() {
		return resourceErrorPage.get();
	}
	
	public Class<? extends Page> getWorkflowErrorPage() {
		return workflowErrorPage.get();
	}
	
	public ApplicationSettings setResourceErrorPage(final Class<? extends Page> page)	{
		Args.notNull(page, "resourceErrorPage");
		this.resourceErrorPage = new WeakReference<>(page);
		return this;
	}
	
	public ApplicationSettings setWorkflowErrorPage(final Class<? extends Page> page)	{
		Args.notNull(page, "workflowErrorPage");
		this.workflowErrorPage = new WeakReference<>(page);
		return this;
	}
}