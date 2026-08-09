package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;

public class MyListEditor extends ObjectEditor<UserList> {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MyListEditor.class.getName());
	
	IModel<String> name;
	IModel<UserList> model;
	boolean is_new = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyListEditor(String id, IModel<UserList> model) {
		super(id, model);
		// TODO Auto-generated constructor stub
	}
	
	public IModel<String> getName() {
		return this.name;
	}
	
	public void setName(IModel<String> s) {
		this.name=s;
	}

	public void setModel(IModel<UserList> m) {
		this.model=m;
	}

	public IModel<UserList> getModel() {
		return this.model;
	}
	
	public void onDetach() {
		super.onDetach();
		this.model.detach();
	}

	/**
	 *
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();

		final Form<Void> form = new Form<Void>("form");
		add(form);
		
		String s= MyListEditor.this.getModel().getObject().getTitle();
		
		 MyListEditor.this.setName(new Model<String>(s));
		
		TextField<String> code = new TextField<String>("name", new Model<String>() {
		
			private static final long serialVersionUID = 1L;

			public String getObject() {
				return  MyListEditor.this.getModel().getObject().getTitle();
			}
			
			public void setObject(String s) {
				 MyListEditor.this.getModel().getObject().setTitle(s);
			}
		}, true);
		

		
		TextAreaField<String> dees = new TextAreaField<String>("description", new Model<String>() {
			
			private static final long serialVersionUID = 1L;

			public String getObject() {
				return  MyListEditor.this.getModel().getObject().getDescription();
			}
			
			public void setObject(String s) {
				 MyListEditor.this.getModel().getObject().setDescription(s);
			}
		}, 4, 40);
		dees.setRequired(false);
		
		
		form.add(code);
		form.add(dees);
		
		
		add(new AjaxSubmitLink("save", form) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				try {
					((KbeeUser) getSessionUser()).getService(UserListService.class).save( MyListEditor.this.getModel().getObject());
					fireScanAll(new MyListsUpdateListEvent(target));
					 MyListEditor.this.onClose(target);
				} catch (Exception e) {
					logger.error(e);
				}
				target.add(MyListEditor.this);
				
			}
		});
		
		add(new AjaxLink<Void>("cancel") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				if (is_new) {
					((KbeeUser) getSessionUser()).getService(UserListService.class).delete( MyListEditor.this.getModel().getObject());
				}
				 MyListEditor.this.onClose(target);
				 //target.add(MyListEditor.this);
			}
		});
		
	}

	protected void onClose(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
		
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
