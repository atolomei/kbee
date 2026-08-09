package kbee.web.console;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;

import kbee.web.console.Browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


public class MenuButtonToolbarItem<T> extends ToolbarItem {
						
	private static final long serialVersionUID = 1L;
	
	//final boolean is_root 		= ServiceLocator.getService(SecurityService.class).isRoot();
	//final boolean is_support 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	//final boolean is_security	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	//final boolean is_domain_admin = is_root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	
	private ContextMenuPanel<T> menu;
	
	private IModel<String> title;
	
	public void setTitle (IModel<String> title) {
		this.title=title;
	}
	
	public IModel<String> getTitle() {
		return this.title;
	}
	
	
	public MenuButtonToolbarItem(Browser<?> browser, Align align) {
		this( browser, align, false);
	}
	
	
	public MenuButtonToolbarItem(Browser<?> browser, Align align, boolean isicon) {
		super(browser, align, isicon);
	}

	
	
	public void setMenuPanel(ContextMenuPanel<T> menu) {
		this.menu=menu;
	}
		
	public ContextMenuPanel<T> getMenuPanel() {
		return this.menu;
	}
	
	public String getAddCss() {
		return "btn btn-default";
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	
		WebMarkupContainer newm = new WebMarkupContainer ("multiple-button");
		newm.add(new AttributeModifier("class", " dropdown-toggle " + getAddCss()));
		newm.add(new AttributeModifier("data-toggle", "dropdown"));
		add(newm);
		
		if (getTitle()==null)
			setTitle(new Model<String>(""));
		
		Label t=new Label("title", getTitle());
		newm.add(t);
		
		Panel me =getMenuPanel();
		if (me==null)
			me = new InvisiblePanel("menu");
		addOrReplace(me);
	}

}




