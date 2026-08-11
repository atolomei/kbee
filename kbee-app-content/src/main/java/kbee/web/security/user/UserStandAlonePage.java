package kbee.web.security.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.entity.Person;
import com.novamens.kbee.wicket.util.InvisiblePanel;

import kbee.web.nav.TabNavigationBar;
import kbee.web.page.AbstractApplicationPage;


public class UserStandAlonePage extends AbstractApplicationPage<Person> {
			
	private static final long serialVersionUID = 1L;

	IModel<Person> model;
	
	public UserStandAlonePage(IModel<Person> model) {
		super(model);
		setModel(model);
	}
	
	
	public void setModel(IModel<Person> model) {
		this.model = model;
	}
	 
	public IModel<Person> getModel() {
		return this.model;
	}
	
	
	public void onDetach() {
		super.onDetach();
		this.model.detach();
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
			
		getPageParameters().set("id", getModel().getObject().getId().toString());
		setMenu(new InvisiblePanel("menu"));
		
		setPageTitle(new Model<String>(getModel().getObject().getDisplayName()));
		
		UserMainPanel editor = new UserMainPanel("editor", getModel(), false, false, false) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onClose(AjaxRequestTarget target) {
				((TabNavigationBar<?>)UserStandAlonePage.this.get("navigation")).onReturn(target);
			}
		};
		editor.setEditionEnabled(false);
		add(editor);

	}

}
