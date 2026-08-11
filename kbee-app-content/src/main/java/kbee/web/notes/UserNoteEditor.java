package kbee.web.notes;

import java.time.OffsetDateTime;
import java.time.format.TextStyle;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.novamens.content.base.ContentMgmtException;

import com.novamens.content.notes.UserNote;
import com.novamens.content.notes.UserNotesService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.Buttons;
import kbee.web.form.TextEditorField;
import kbee.wicket.tinymce.settings.TinyMCESettings;

import com.novamens.wicket.markup.html.form.TextField;


public class UserNoteEditor extends DomainObjectEditor<UserNote> {
			
private static final long serialVersionUID = 1L;

												
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserNoteEditor.class.getName());

	
	static private String FORMATS = "style_formats : ["
		+ "{title : 'Highlight inline'		, inline : 'span',     classes : 'highlight-inline'},"
		+ "{title : 'Parameter'				, inline : 'span',     classes : 'parameter'},"
		+ "{title : 'Table row 1', selector : 'tr', classes : 'tablerow1'}]";


	private String text;
	private String title;
				
	
	public UserNoteEditor(IModel<UserNote> model, boolean isNew) {
		this("editor", model);
		
		setOutputMarkupId(true);
		setIsNew(isNew);
		setReadOnly(true);
		setEditionEnabled(isNew);
		getSessionUser().getService(PreferencesService.class).setValue(UserNoteEditor.class.getSimpleName(), "selected", getModel().getObject().getId().toString());
		
	}

	 
	public UserNoteEditor(String id, IModel<UserNote> model) {
		super(id, model);
		setOutputMarkupId(true);
		setIsNew(false);
		setReadOnly(true);
		setEditionEnabled(false);
		getSessionUser().getService(PreferencesService.class).setValue(UserNoteEditor.class.getSimpleName(), "selected", getModel().getObject().getId().toString());
	}

	
	public String getText() {
		return this.text;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String t) {
		this.title=t;
	}
	
	public void setText(String t) {
		this.text=t;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		setTitle(getModel().getObject().getTitle());
		setText(getModel().getObject().getText());
		
		WebMarkupContainer view = new WebMarkupContainer("viewer") {
			private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return !UserNoteEditor.this.isEditionEnabled();
				}
		};
		
		add(view);		
							
		AjaxLink<UserNote> expander = new AjaxLink<UserNote>("title-link", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				String selected=getSessionUser().getService(PreferencesService.class).getValue(UserNoteEditor.class.getSimpleName(), "selected");
				if ((selected!=null) && selected.equals(getModel().getObject().getId().toString())) 
					getSessionUser().getService(PreferencesService.class).setValue(UserNoteEditor.class.getSimpleName(), "selected", "0");			
				collapse(target);
			}
		};
		
		view.add(expander);
		Label view_title 		= new Label("title", getModel().getObject().getTitle());
		Label view_modified 	= new Label("modified", getModel().getObject().getLastModifiedOffsetDateTimeColloquial());
		Label view_text 		= new Label("text", getModel().getObject().getText());
		
		view_modified.setVisible(false);
		view_text.setEscapeModelStrings(false);
		expander.add(view_title);
		expander.add(view_modified);
		view.add(view_text);
		
		WorkingIndicatorAjaxLinkV5<UserNote> edit = new WorkingIndicatorAjaxLinkV5<UserNote>("edit",  getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				setEditionEnabled(true);
				target.add(UserNoteEditor.this.getParent());	
			}
			public boolean isVisible() {
				return !UserNoteEditor.this.isEditionEnabled();
			}
			@Override
			protected String getWorkingLabel() {
				return "working";
			}
		};

		view.add(edit);
		
		WorkingIndicatorAjaxLinkV5<UserNote> delete = new WorkingIndicatorAjaxLinkV5<UserNote>("delete", UserNoteEditor.this.getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					
					String selected=getSessionUser().getService(PreferencesService.class).getValue(UserNoteEditor.class.getSimpleName(), "selected");
					if ((selected!=null) && (getModel()!=null) && (getModel().getObject()!=null) && (selected.equals(getModel().getObject().getId().toString()))) {
						getSessionUser().getService(PreferencesService.class).setValue(UserNoteEditor.class.getSimpleName(), "selected", "0");			
					}
					((KbeeUser) getSessionUser()).getService(UserNotesService.class).remove(getModel().getObject());
					
				} catch (ContentMgmtException | ServiceNotFoundException e) {
					logger.error(e);
				
				} catch (Exception e) {
					logger.error(e);
				}
				
				reload(target);
			}
			public boolean isVisible() {
				return !UserNoteEditor.this.isEditionEnabled();
			}
		};
		
		view.add(delete);
		
		AjaxLink<UserNote> close = new AjaxLink<UserNote>("close",  getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				setEditionEnabled(false);
				String selected=getSessionUser().getService(PreferencesService.class).getValue(UserNoteEditor.class.getSimpleName(), "selected");
				if ((selected!=null) && (getModel()!=null) && (getModel().getObject()!=null) && (selected.equals(getModel().getObject().getId().toString()))) 
					getSessionUser().getService(PreferencesService.class).setValue(UserNoteEditor.class.getSimpleName(), "selected", "0");			
				collapse(target);
			}
			public boolean isVisible() {
				return !UserNoteEditor.this.isEditionEnabled();
			}
			
		};
		view.add(close);

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL) {
			private static final long serialVersionUID = 1L;
			@Override	
			public boolean isVisible() {
				return isEditionEnabled();
			}
		};
		add(form);
		
		form.add(new TextEditorField("text", new PropertyModel<String>(this,"text")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getStyleFormats() {
				return FORMATS ;
			}
			
			@Override
			protected TinyMCESettings.Theme getTheme() {
				return TinyMCESettings.Theme.simple;
			}
			
			
		});
		
		form.add(new TextField<String>("title", new PropertyModel<String>(this,"title")));
		
		form.add(new Buttons<UserNote>(this) {
			private static final long serialVersionUID = 1L;
				@Override
				protected String getCss() {
					return "btn-link";
				}
				@Override
				protected String getSubmitCss() {
					return "btn-link";
				}
				@Override
				protected String getBeforeSubmitHandler() {
					return "tinyMCE.triggerSave(true,true);";
				}
			});
	}

	
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		 onCancel(target); 
	}
	 

	@Override
	public void update(AjaxRequestTarget target) {
		try {

			
		//	if (!getUpdatedParts().isEmpty()) {
				String text1 = ((TextEditorField) get("form:text")).getModel().getObject();
				String text_to_save = text1;
				
				//if (text1!=null) {
				//	Document text_parsed = Jsoup.parse(text1);
				//	text_to_save = text_parsed.text();
				//}
				
				
				/**
				if (getTitle()==null || getTitle().length()==0) {
					if (text1!=null) {
						Document doc = Jsoup.parse(text1);
						String title2 = doc.text();
						if (doc.select("p")!=null) {
							Element el = doc.select("p").first();
							if (el!=null) 
								title2 = el.text();
						}
						if (title2.length()>1 && title2.endsWith("."));
								title2=title2.substring(0, title2.length()-1);
						if (title2.length()>48)
							title2=title2.substring(0, 48)+"...";
						getModel().getObject().setTitle(title2);
						
						
						
						
					}
					else
						getModel().getObject().setTitle("Note - " + String.valueOf(OffsetDateTime.now().getDayOfMonth())+" " + OffsetDateTime.now().getMonth().getDisplayName(TextStyle.SHORT, getSessionUser().getLocale())+ " " +String.valueOf(OffsetDateTime.now().getYear()));
				}
				else*/
				
				getModel().getObject().setTitle(getTitle());
				getModel().getObject().setText(text_to_save);
				
				((KbeeUser) getSessionUser()).getService(UserNotesService.class).update(getModel().getObject());
				reset();
				reload(target, getModel().getObject().getId());
			// }
			//else {
			//	reset();
				onUpdate(target);
			//}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));
		}
	}
	
	public void onUpdate(AjaxRequestTarget target) {}
	public void onCancel(AjaxRequestTarget target) {}

	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	
	protected void reload(AjaxRequestTarget target, Long id_open) {}
	protected void reload(AjaxRequestTarget target) {}
	protected void collapse(AjaxRequestTarget target) {}



	
	
}
