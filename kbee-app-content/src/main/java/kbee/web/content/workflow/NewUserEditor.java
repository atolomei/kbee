package kbee.web.content.workflow;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.entity.Person;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.TextField;

@SuppressWarnings("serial")
public class NewUserEditor extends ObjectEditor<Void> {
	private static final long serialVersionUID = 1L;
	
	private String name, lastName, email;
	private boolean opened;
	
	class EMailValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String email = validatable.getValue();
			List<Person> users = getContentDao().findPersonByEmail(email);
			if (!users.isEmpty()) {
				validatable.error(new ValidationError(this, "uniqueness"));
			}
		}
	}

	public NewUserEditor(String id) {
		super(id);
	}
	
	public void open(AjaxRequestTarget target) {
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean opened) {
		this.opened = opened;
	}
	
	public void onClose(AjaxRequestTarget target) {
		
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new TextField<String>("name", new PropertyModel<String>(this, "name")) {	
			public void onUpdate(AjaxRequestTarget target) {
				updateModel();
			}
		});
		add(new TextField<String>("lastName", new PropertyModel<String>(this, "lastName")) {
			public void onUpdate(AjaxRequestTarget target) {
				updateModel();
			}
		});
		add(new TextField<String>("email", new PropertyModel<String>(this, "email"), true, new EMailValidator()) {
			public void onUpdate(AjaxRequestTarget target) {
				updateModel();
			}
		});
		
		add(new AjaxLink<Void>("update") {
			public void onClick(AjaxRequestTarget target) {
				
			}
		});
		
		add(new AjaxLink<Void>("cancel") {
			public void onClick(AjaxRequestTarget target) {
				setOpened(false);
				onClose(target);
			}
		});

//		roes no nativos		
//		[]sdsd
//		[]ssdsd
//		[]sdsdsd
//		[]sdsdsd
	}
	
	@Override
	public boolean isVisible() {
		return isOpened();
	}
	

}