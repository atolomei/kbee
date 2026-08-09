package kbee.web.console;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.event.CloseConsoleTopPanelEvent;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;


public class ConsoleErrorPanel extends KBPanel {

	private static final long serialVersionUID = 1L;

	private IModel<String> title;
	private IModel<String> message;

	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	public ConsoleErrorPanel(IModel<String> title, IModel<String> text) {
		this("error-panel", title, text);
	}
	
	public ConsoleErrorPanel(String id, Throwable e) {
		super(id);
		this.title = new Model<String>(e.getClass().getName());
		this.message = new Model<String>(e.getMessage());
	}
	
	
	public ConsoleErrorPanel(String id, IModel<String> title, IModel<String> text) {
		super(id);
		this.title=title;
		this.message=text;
	}
	
	
	protected String getCssClass() {
		return "error-top-panel col-lg-12 col-md-12 col-xs-12";
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		WebMarkupContainer ec = new WebMarkupContainer("error-container");
		ec.add(new AttributeModifier("class",  getCssClass()));
		add(ec);		
				
		Label xtitle=new Label("title", title);
		xtitle.setVisible(title!=null);
		xtitle.setEscapeModelStrings(false);
		
		
		ec.add(xtitle);
		
		Label m = new Label("text", message);
		m.setEscapeModelStrings(false);
		m.setVisible(message!=null);
		ec.add(m);
		
		
		WorkingIndicatorAjaxLinkV5<Void> close= new WorkingIndicatorAjaxLinkV5<Void>("close", "close") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				ConsoleErrorPanel.this.onClick(target);
			}
		};
		ec.add(close);
	}

	
	protected void onClick(AjaxRequestTarget target) {
		fire (new CloseConsoleTopPanelEvent(target));
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
