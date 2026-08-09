package kbee.web.command.panel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.novamens.content.command.Command;
// import com.novamens.content.web.console.markup.WorkLoadHitExpandedPanel;
import com.novamens.kbee.wicket.markup.html.console.browser.HitExpandedPanel;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;

import kbee.web.command.CommandStatusPanelV5;

public class CommandHitExpandedPanel extends Panel implements HitExpandedPanel {
			
	@SuppressWarnings("unused")
	static private Logger logger = LogManager.getLogger(CommandHitExpandedPanel.class.getName());
	
	static PackageResourceReference MENU_ICON = new PackageResourceReference(AbstractKbeeWebPage.class, "menu-red.png");
	
	private static final long serialVersionUID = 1L;

	private IModel<Command> model;

	//private List<Entry<String, Integer>> list = null;

	public CommandHitExpandedPanel(String id, IModel<Command> model) {
		super(id);
		setModel(model);
		setOutputMarkupId(true);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(new CommandStatusPanelV5("command_status", getModel()));
	}

	
	@Override
	public void onDetach() {
		super.onDetach();
		getModel().detach();
	}
	

	protected void setModel(IModel<Command> model) {
		this.model=model;
	}

	
	protected IModel<Command> getModel() {
		return model;
	}


	
	//private KbeeUser getSessionUser() {
	//	return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	//}


}
