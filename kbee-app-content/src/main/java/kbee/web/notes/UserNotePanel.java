package kbee.web.notes;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.notes.UserNote;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class UserNotePanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	IModel<UserNote> model;
	
	static private int LIST = 0;
	static private int EDITOR = 1;
	
	private int state = LIST;
	
	private boolean editor_created = false;
	private boolean is_new = false;
	private boolean start_open = false;
	
	public UserNotePanel(String id, IModel<UserNote> model, boolean isNew) {
		this(id, model, isNew, false);
	}
	
	public UserNotePanel(String id, IModel<UserNote> model, boolean isNew, boolean start_open) {
		super(id);
		setModel(model);
		
		this.is_new=isNew;
		this.start_open=start_open;
		
		setState((this.is_new || this.start_open) ? EDITOR : LIST);
		this.setOutputMarkupId(true);
	}
	

	// State LIST and EDITOR
	// in EDITOR state all these elements are hidden and the only visible component is the Editor
	//
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		AjaxLink<UserNote> link = new AjaxLink<UserNote>("title-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				setState(EDITOR);
				setEditorVisible();
				target.add(UserNotePanel.this);
			}
			@Override
			public boolean isVisible() {
				return getState()==LIST;
			}
		};

		Label title = new Label("title", getModel().getObject().getTitle()) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return getState()==LIST;
			}
		};
		
		Label modified = new Label("modified", getModel().getObject().getLastModifiedOffsetDateTimeColloquial()) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return getState()==LIST;
			}
		};
		modified.setEscapeModelStrings(false);
		link.add(title);
		link.add(modified);
		add(link);

		
		if (this.is_new || this.start_open)
			setEditorVisible(); 	
		else
			add(new InvisiblePanel("editor"));
	}
	
	public IModel<UserNote> getModel() {
		return model;
	}
	
	public void setModel(IModel<UserNote> model) {
		this.model=model;
	}
	

	public void setEditionEnabled(boolean b) {
		((UserNoteEditor) get("editor")).setEditionEnabled(b);
		setState(EDITOR);
	}

	
	/**--
	 * Editor always starts open, the is_new parameter
	 * tells whether start in view or editing mode.
	 */
	protected void setEditorVisible() {
		
		if (!this.editor_created) {
			
			UserNoteEditor editor = new UserNoteEditor(getModel(), this.is_new) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void collapse(AjaxRequestTarget target) {
						setEditionEnabled(false);
						setState(LIST);
						target.add(UserNotePanel.this);
				}
				
				@Override
				protected void reload(AjaxRequestTarget target) {
					UserNotePanel.this.reload(target, null);
				}
				
				@Override
				protected void reload(AjaxRequestTarget target, Long id_open) {
						setEditionEnabled(false);
						setState(LIST);
						UserNotePanel.this.reload(target, id_open);
				}
				
				public void onUpdate(AjaxRequestTarget target) {
					target.add(UserNotePanel.this);
				}
				
				@Override
				public boolean isVisible() {
					return getState()==EDITOR;
				}
			};
			
			addOrReplace(editor);
			this.editor_created=true;
		}
		UserNoteEditor editor = (UserNoteEditor) get("editor");
		editor.setVisible(true);
		
	}

	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected void reload(AjaxRequestTarget target, Long id_open) {
		// TODO Auto-generated method stub
	}
	
	private void setState(int state) {
		this.state=state;
	}
	
	private int getState() {
		return this.state;
	}





	
}
