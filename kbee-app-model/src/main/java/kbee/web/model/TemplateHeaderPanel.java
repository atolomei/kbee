package kbee.web.model;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.ContentTemplate;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.model.contentclass.ContentClassesBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.object.TitleHeaderPanel;

public class TemplateHeaderPanel extends TitleHeaderPanel<ContentTemplate> {
	private static final long serialVersionUID = 1L;

	IModel<String> icon = new Model<String>("far fa-code");
	
	public TemplateHeaderPanel(IModel<ContentTemplate> model) {
		super("template-panel", model);
		setOutputMarkupId(true);
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
		bc.addElement(new SettingsDropDownBC());
		bc.addElement(new InformationModelDropDownBC());
		bc.addElement(new ContentClassesBC());
		bc.addElement(new BCElement(new Model<String>(model.getObject().getDisplayName())));
		setBreadCrumbPanel(bc);
	} 
	
	@Override
	protected IModel<String> getGlyphicon() {
		return icon; 
	}
}