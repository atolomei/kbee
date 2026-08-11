package kbee.web.emailtemplate;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.email.EmailTemplate;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.EmailTemplatesBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.object.TitleHeaderPanel;

public class EmailTemplateHeaderPanel extends TitleHeaderPanel<EmailTemplate> {
			
	private static final long serialVersionUID = 1L;
	
	IModel<String> icon = new Model<String>("far fa-envelope");

	public EmailTemplateHeaderPanel(IModel<EmailTemplate> model) {
		super("email-panel", model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
		bc.addElement(new SettingsDropDownBC());
		bc.addElement(new EmailTemplatesBC());
		bc.addElement(new BCElement(new Model<String>(getModel().getObject().getTitle() +" (" + getModel().getObject().getLanguage()+")" )));
		setBreadCrumbPanel(bc);
	}

	protected IModel<String> getGlyphicon() {
		return icon; 
	}
}
