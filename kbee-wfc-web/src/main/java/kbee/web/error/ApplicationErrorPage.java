package kbee.web.error;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebResponse;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.page.AbstractApplicationPage;

/**
 *   
 */
public class ApplicationErrorPage<T> extends AbstractApplicationPage<T> implements ErrorPage {
	private static final long serialVersionUID = 1L;
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ApplicationErrorPage.class.getName());
	
	final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	private IModel<String> title = new Model<String>("Error");
	private IModel<String> message = new Model<String>("Message");
	 
	Exception e;

	public ApplicationErrorPage() {
	}
	 
	public ApplicationErrorPage(Exception e) {
		this.title=new Model<String>(e.getClass().getName());
		setMessage(e);
		this.e=e;
	}
	
	
	public ApplicationErrorPage(Exception e, IModel<T> model) {
		super(model);
		this.title=new Model<String>(e.getClass().getName());
		setMessage(e);
		this.e=e;
	}
	 
	 public ApplicationErrorPage(IModel<String> message) {
		 this(null, message);
	 }
	 
	 public ApplicationErrorPage(IModel<String> message, IModel<String> title) {
		 this.title=title;
		 this.message=message;
	 }
	 
	 public void setError(Exception e) {
		 this.e = e;
		 setMessage(e);
	 }
	 
	 
	 
	 public void setMessage(Exception e) {
		 String message = e.getMessage();
		 if (is_root || is_domain_admin) {
			 Throwable t = e;
			 while (t.getCause()!=null) {
				 t = t.getCause();
				 message += "</br>" + t.getMessage(); 
			 }
		 }
		 this.message = new Model<String>(message);
	 }
	 
	 
	 @Override
	 public void onAfterRender() {
		 super.onAfterRender();
		 
		WebResponse response = (WebResponse)getResponse();
		response.setHeader("Content-Length", null);
	 }

	 @Override
	 public void onInitialize() {
		 super.onInitialize();
		 if (getPerson()!=null) {
			setTopNavigation(getMainTopbar());
			setMenu(getMainLaternalMenu());
		 }
		 
	
		 add(new ErrorPanel("error-panel", title, message));
		 setPageTitle(this.title);
		 
		 if (logger.isDebugEnabled() && e!=null) {
			 StringWriter sw = new StringWriter();
			 PrintWriter pw = new PrintWriter(sw);
			 e.printStackTrace(pw);
			 add( (new Label("stacktrace", sw.toString().replace("\n", "<br />"))).setEscapeModelStrings(false));
		 }
		 else {
			 add(new Label("stacktrace", ""));
		 }
	 }
}