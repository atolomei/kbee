package kbee.web.command.panel;


import java.io.Serializable;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.command.Command;
import com.novamens.kbee.command.CommandService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.console.ConsolePage;
import kbee.web.error.ErrorPanel;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;


public class CommandPage extends ApplicationPage<Command> {
					
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
 	final boolean is_support 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CommandPage.class.getName());

			
	public CommandPage(PageParameters parameters) {
		Command command = getCommand(parameters);
		if (command!=null) {
			setTopNavigation(getMainTopbar());       // setNavigation(new GlobalNavigationBar<Person>("navigation"));
			setMenu(getMainLaternalMenu());       // setMenu(new NavBarLateralMenu("menu", getApplicationMenuSection().getKey()));
	
			setModel(new CommandModel(command));
			addComponents(getModel(), false); 
		}
		else {
			addOrReplace(new ErrorPanel("command_status", "command not found", ""));
		}
	}

	
	public CommandPage(IModel<Command> model) {
		super(model);
		
		logger.debug(model!=null && model.getObject()!=null ? model.getObject().getTitle() : " null");
		
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());  

		addComponents(model, false);
	}
	
	public CommandPage(IModel<Command> model, Panel navigationPanel, boolean edition) {
		super(model, navigationPanel);
		
		setTopNavigation(getMainTopbar());       // setNavigation(new GlobalNavigationBar<Person>("navigation"));
		setMenu(getMainLaternalMenu());       // setMenu(new NavBarLateralMenu("menu", getApplicationMenuSection().getKey()));
		addComponents(model, edition);
	}
 	@Override
 	public void onDetach() {
 		super.onDetach();
 		
 		if (getModel()!=null)
 			getModel().detach();
 	}
 	
 	
 	@Override
	public boolean hasPermissions() {
		
 		if (getModel()==null || getModel().getObject()==null)
				return false;
		
		// if (!isDomainKbee())
		//	 return false;
 		//
		
		return is_domain_admin || is_root; 
 	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DATA_MANAGEMENT;
	}

	protected void addComponents(IModel<Command> model, boolean edition) {
		setPageTitle(new Model<String>(model.getObject().getName()));
		CommandStatusPanelV5 panel = new CommandStatusPanelV5("command_status", model);
		add(panel);
		getPageParameters().set("id", model.getObject().getId());
	}
	
	protected Command getCommand(PageParameters parameters) {
		Command command = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			command = getCommandService().getCommand( (Serializable) id);
		}	
		return command;
	}
	
	private CommandService getCommandService() {
		return ServiceLocator.getService(CommandService.class);
		
	}
}
