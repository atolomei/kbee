package com.novamens.content.web.security.markup;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.wicket.model.PropertyModel;

import com.novamens.content.user.UserService;
import com.novamens.content.web.command.batch.markup.BatchCommandStatusPanel;
import com.novamens.dom.Domain;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.TestCommand;

import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class UsersBatchCreationPanel extends Panel {

	
private static final long serialVersionUID = 1L;

private BatchCreationForm form;


static private Logger logger = LogManager.getLogger(UsersBatchCreationPanel.class.getName());


/** --------------------------------------------------------------------------------------------
*/

public enum State {
	PREPARING 		(1, "preparing"), 
	EXECUTING 		(2, "executing"),
	TERMINATED		(3, "terminated"); 
	private String label;
	private int id;
	private  State(int code, String label) {this.label = label;this.id = code;}
	public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
	public String getLabel() {return label;}
	public int getId() {return id;}
}


private State command_state = State.PREPARING;


public UsersBatchCreationPanel(String id) {
	super(id);
	
	
	form = new BatchCreationForm ("form");
	
	add(form);
	setOutputMarkupId(true);
	
	add (new Panel("status") {
		private static final long serialVersionUID = 1L;
		public boolean isVisible() {
			return false;
		}
	});
	}




/** --------------------------------------------------------------------------------------------
*/
public class BatchCreationForm extends Form<Void> {
 
private static final long serialVersionUID = 1L;
private String elements;


public BatchCreationForm(String id) {
	super(id);

	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean has_permission = is_root || is_domain_admin || is_model;				
	
	
	TextArea<String> statement = new TextArea<String>("elements") {
		private static final long serialVersionUID = 1L;
		@Override
		public boolean isEnabled() {
			return true;
		}
	};
	
	statement.setModel(new PropertyModel<String>(this,"elements"));
	add(statement);
	
		
	
	add(new AjaxButton("submit-button", this) {

		private static final long serialVersionUID = 1L;

		@Override
		protected void onSubmit(AjaxRequestTarget target) {
			
			try {

				if (getElements()!=null) {
				
					Domain domain = getDomain();
					
					if (domain!=null) {
						
						TestCommand cmd;
						
						cmd = new TestCommand();
						// MemberBatchCreationCommand(getDataSet().getId(), getDataSet().getDomain().getId(), getSessionUser().getId(), getElements());
						
						if (cmd!=null) {	
							CommandService service = ServiceLocator.getService(CommandService.class);
							service.add(cmd);
							setState(State.EXECUTING);
							BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId(), false) {
								private static final long serialVersionUID = 1L;
								@Override
								public void onAfterExecution(AjaxRequestTarget target) {
									setState(State.TERMINATED);
									target.add(UsersBatchCreationPanel.this);
								}
							};
							
							UsersBatchCreationPanel.this.replace(panel);
							logger.debug("Sending "+ cmd.getId().toString());
							target.add(UsersBatchCreationPanel.this);
						}
					}
					else {
						error("Domain is null.");
					}
				}
			}
				catch (Exception e) {
					logger.error(e);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream(baos);
					e.printStackTrace(ps);
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					String message =  baos.toString();
					form.error(message);
					
				}
			
			target.add(BatchCreationForm.this);
		}
		
		@Override
		public boolean isVisible() {
			return getState()==State.PREPARING;
		}

		@Override
		public boolean isEnabled() {
			return has_permission && getState()!=State.EXECUTING;
		}
	});
	
	
	
	add(new AjaxButton("close-button", this) {
		private static final long serialVersionUID = -5848063566372226285L;
		
		@Override
		protected void onSubmit(AjaxRequestTarget target) {
				UsersBatchCreationPanel.this.onClose();
		}
		
		@Override
		public boolean isVisible() {
			return getState()!=State.EXECUTING;
		}

		@Override
		public boolean isEnabled() {
			return getState()!=State.EXECUTING;
		}
	});
	

	add(new AjaxButton("stop-button", this) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected void onSubmit(AjaxRequestTarget target) {
			((BatchCommandStatusPanel) UsersBatchCreationPanel.this.get("status")).stop(target);
		}
		
		@Override
		public boolean isVisible() {
			return getState()==State.EXECUTING;
		}

		@Override
		public boolean isEnabled() {
			return getState()==State.EXECUTING;
		}
	});

	
	add(new FeedbackPanel("feedback"));
    }


	public String getElements() {
		return this.elements;
	}

	public void setElements(String elements) {
		this.elements = elements;
	}


}

/** --------------------------------------------------------------------------------------------
*/
								
public State getState() { 
	return this.command_state;
}

/** --------------------------------------------------------------------------------------------
*/

public void setState(State state) {
this.command_state = state;
}

///** --------------------------------------------------------------------------------------------
//*/
//
//private ContentDao getContentDao() {
//return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//}


/** --------------------------------------------------------------------------------------------
*/
private Domain getDomain() {
	return ServiceLocator.getService(UserService.class).getDomain();
}

/** --------------------------------------------------------------------------------------------
*/
public User getSessionUser() {
return ServiceLocator.getService(SecurityService.class).getSessionUser();
}

/** --------------------------------------------------------------------------------------------
*/

protected void onClose() {


}
}
