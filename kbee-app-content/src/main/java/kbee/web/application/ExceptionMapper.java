package kbee.web.application;

import org.apache.wicket.Application;
import org.apache.wicket.DefaultExceptionMapper;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.ComponentNotFoundException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import com.novamens.workflow.WorkflowRuntimeException;

import kbee.util.logging.Logger;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPage;
import kbee.web.resource.ResourceException;

public class ExceptionMapper extends DefaultExceptionMapper {

	private static Logger logger = Logger.getLogger(ExceptionMapper.class.getName());
	
	public IRequestHandler map(Exception e)	{
		logger.error(e);
		e.printStackTrace();
		Application application = Application.get();
		boolean debug = application.getDebugSettings().isDevelopmentUtilitiesEnabled();
		if (!debug && e instanceof MarkupException) {
			return createPageRequestHandler(new PageProvider(application.getHomePage()));
		}
		
		if (e instanceof ResourceException) {
			return createPageRequestHandler(new PageProvider(getErrorPage(e, ((ApplicationSettings)application.getApplicationSettings()).getResourceErrorPage())));
		}
		if (e instanceof WorkflowRuntimeException) {
			return createPageRequestHandler(new PageProvider(((ApplicationSettings)application.getApplicationSettings()).getWorkflowErrorPage()));
		}
		if (e instanceof IllegalArgumentException) {
			return createPageRequestHandler(new PageProvider(application.getApplicationSettings().getInternalErrorPage()));
		}
		if (e instanceof ComponentNotFoundException) {
//		if (!debug && e instanceof ComponentNotFoundException) {
			return createPageRequestHandler(new PageProvider(((ApplicationSettings)application.getApplicationSettings()).getWorkflowErrorPage()));
			//return createPageRequestHandler(new PageProvider(application.getApplicationSettings().getInternalErrorPage()));
		}
		else
			return super.map(e);
		
	}
	
	protected Page getErrorPage(Exception e, Class <? extends Page> pageClass) {
		Page page;
		try {
			page = pageClass.getDeclaredConstructor().newInstance();
			if (page instanceof ErrorPage) {
				((ErrorPage)page).setError(e);
			}
		}
		catch (Exception e1) {
			page = new ApplicationErrorPage<>(e1);
		}
		return page;
	}
	
	protected RenderPageRequestHandler createPageRequestHandler(PageProvider pageProvider) {
		RequestCycle requestCycle = RequestCycle.get();

		if (requestCycle == null)
		{
			throw new IllegalStateException(
				"there is no current request cycle attached to this thread");
		}

		/*
		 * Use NEVER_REDIRECT policy to preserve the original page's URL for non-Ajax requests and
		 * always redirect for ajax requests
		 */
		RenderPageRequestHandler.RedirectPolicy redirect = RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT;

		if (isProcessingAjaxRequest())
		{
			redirect = RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT;
		}

		return new RenderPageRequestHandler(pageProvider, redirect);
	}
	
	protected boolean isProcessingAjaxRequest() {
		RequestCycle rc = RequestCycle.get();
		Request request = rc.getRequest();
		if (request instanceof WebRequest) {
			return ((WebRequest)request).isAjax();
		}
		return false;
	}
}
