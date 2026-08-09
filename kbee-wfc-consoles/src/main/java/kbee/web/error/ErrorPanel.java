package kbee.web.error;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;

import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/**
 * 
 * 
 *  [_view logs_]
 *  [_view logs_]
 *  [_view logs_]
 *  [_view logs_]
 *  
 * 
 * 
 * 
 *
 */
public class ErrorPanel extends Panel {
																	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ErrorPanel.class.getName());

	private static final long serialVersionUID = 1L;

	private IModel<String> title;
	private IModel<String> message;
	private IModel<String> stacktrace;

	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	
	public ErrorPanel(String id) {
		this(id, "Error", "Permission denied.");
	}
	
	public ErrorPanel(String id, Throwable e) {
		super(id);
		
		
		this.title= new Model<String>(e.getClass().getName());
		
		if (e.getMessage()!=null) {
			this.message= new Model<String>(e.getMessage());
		}
		
		if (logger.isDebugEnabled()) {
			 StringWriter sw = new StringWriter();
			 PrintWriter pw = new PrintWriter(sw);
			 e.printStackTrace(pw);
			 stacktrace=new Model<String>(sw.toString().replace("\n", "<br />"));
		}
		
	}
	
	public ErrorPanel(String id, IModel<String> text) {
		this(id, null, text);
	}
	
	
	
	
	public ErrorPanel(String id, IModel<String> title, IModel<String> text) {
		super(id);
		this.title=title;
		this.message=text;
	}
	
	public ErrorPanel(String id, String title, String text) {
		super(id);
		this.title=new Model<String>(title);
		this.message=new Model<String>(text);
	}

	@Override
	public void onRender() {
		super.onRender();
	}
	
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		Label xtitle=new Label("title", title);
		xtitle.setVisible(title!=null);
		xtitle.setEscapeModelStrings(false);
		add(xtitle);
		
		Label m = new Label("text", message);
		m.setEscapeModelStrings(false);
		m.setVisible(message!=null);
		add(m);
		
		Label st= new Label("stktrace", stacktrace);
		st.setVisible(stacktrace!=null);
		st.setEscapeModelStrings(false);
		
		add(st);
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	protected UserProfile getUserProfile() {
		return getContentDao().findUserProfileByUser(getSessionUser());
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected boolean isDomainKbee() {
		try {
			Person person = getPerson();
			if (person==null)
				return false;
			return person.getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			return false;
		}
	}

	protected Person getPerson() {
		try {
			return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
		} 
		catch (Exception e) {
			return null;
		}
	}
}
