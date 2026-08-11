package kbee.web.notes;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.notes.UserNote;
import com.novamens.content.notes.UserNotesService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ListModel;

public class UserNotesPanel extends ConsoleSidePanel {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserNotesPanel.class.getName());

	private static final long serialVersionUID = 1L;

	private boolean open_and_edit = false;
	private Long id_to_open = null;

	boolean is_dashboard = true;
	
	public UserNotesPanel() {
		this("user-notes", true);
	}
	
	public UserNotesPanel(String id, boolean is_dashboard) {
		super(id);
		this.setOutputMarkupId(true);
		this.is_dashboard=is_dashboard;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
 		 WorkingIndicatorAjaxLinkV5<UserNote> link = new WorkingIndicatorAjaxLinkV5<UserNote>("create-note", 
				 new StringResourceModel("new", UserNotesPanel.this, null).getObject()
				 
				 ) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					UserNote note = ((KbeeUser) getSessionUser()).getService(UserNotesService.class).createUserNote();
					((KbeeUser) getSessionUser()).getService(UserNotesService.class).update(note);
					open_and_edit = true;
					target.add(UserNotesPanel.this);
					
				} catch (ContentMgmtException | ServiceNotFoundException | ContentCreationException e) {
					logger.error(e);
				}
			}
			@Override
			protected String getWorkingLabel() {
				return  new StringResourceModel("working", UserNotesPanel.this, null).getObject();
			}
		};
		add(link);
		
		if (this.is_dashboard) {
			 link.add(new AttributeModifier("class", "btn btn-default btn-xs"));
		}
		else {
			 link.add(new AttributeModifier("class", "btn btn-primary btn-md"));
		}
		
		WorkingIndicatorAjaxLinkV5<UserNote> deleteall = new WorkingIndicatorAjaxLinkV5<UserNote>("delete-all") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				StringResourceModel sm = new StringResourceModel("confirm-delete-notes", UserNotesPanel.this, null); 
				ConfirmationDialog di = getConfirmationDialog();
				if (di!=null) {	
					di.open(target, 
							sm, 
							Dialog.Delete, 
							new Dialog.Handler() {
								private static final long serialVersionUID = 1L;
	
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										try {
											((KbeeUser) getSessionUser()).getService(UserNotesService.class).removeAllNotes();
											target.add(UserNotesPanel.this);
										} catch (ContentMgmtException | ServiceNotFoundException e) {
											logger.error(e);
										}					
									}
								}
						});
				}
				else {
					logger.error(Thread.currentThread().getStackTrace()[1].getMethodName());
					logger.error("Can not find ConfirmationDialog | " +  UserNotesPanel.this.getClass().getName());
				}
			}
			
			@Override
			protected String getWorkingLabel() {
				return  new StringResourceModel("working", UserNotesPanel.this, null).getObject();
			}
		};
		add(deleteall);

		deleteall.setVisible(!this.is_dashboard);
		
		ListModel<UserNote> ldp = new ListModel<UserNote>(new Model<Panel>(this), "notes");
		ListView<UserNote> ldata = new ListView<UserNote>("user-notes", ldp) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void populateItem(ListItem<UserNote> item) {
					try {
						UserNotePanel panel = null;
						boolean start_open	  = ( (id_to_open!=null) && (id_to_open.equals(item.getModel().getObject().getId()))?true:false);
						boolean start_editing = (open_and_edit && item.getIndex()==0);
						if (!start_open && !start_editing) { 
							String selected=getSessionUser().getService(PreferencesService.class).getValue(UserNoteEditor.class.getSimpleName(), "selected");
							if (selected!=null && selected.equals(item.getModel().getObject().getId().toString())) 
								start_open=true;
						}
						panel = new UserNotePanel("user-note-panel", item.getModel(), start_editing, start_open) {
								private static final long serialVersionUID = 1L;
								protected void reload(AjaxRequestTarget target, Long id_open) {
									UserNotesPanel.this.reload(target, id_open);
								}
						};

						item.add(panel);
						
						if (start_editing)
							open_and_edit  = false;
						
						item.setOutputMarkupId(true);
						
					}  catch (Exception e) {
						logger.error(e);
						item.setVisible(false);
					}
				}
			};
	
			add(ldata);
			ldata.setOutputMarkupId(true);
	}
	
	public List<UserNote> getNotes() {
		return ((KbeeUser) getSessionUser()).getService(UserNotesService.class).getUserNotes();
	}

	@Override
	public void onClose(AjaxRequestTarget target) {
	}

	/** ---------
	 *  
	 * The confirmation Dialog is provided by the ApplicationPage
	 * It must be overriden by the creating Panel. Normally {@code AbstractConsole}
	 * 
	 */
	protected ConfirmationDialog getConfirmationDialog() {
		return null;
	}
	
	protected void reload(AjaxRequestTarget target, Long id_open) {
		this.id_to_open = id_open;
		target.add(this);
	}

	protected void openNote(UserNote note) {}

	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}
