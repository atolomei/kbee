package kbee.web.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.service.DOMObjectService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.workflow.KbeeActivityProgressNote;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.ActivityProgressNote;


public class ProgressNotesListPanel<T extends Content> extends ModelPanel<Activity>{
			
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ProgressNotesListPanel.class.getName());
	

	private IModel<T> content_model;
	
	
	public class NoteViewFragment extends Fragment {
		private static final long serialVersionUID = 1L;
		IModel<ActivityProgressNote> model;
		boolean edition = false;
		String text;


		public NoteViewFragment(IModel<ActivityProgressNote> model) {
			super("view", "note-view-fragment", ProgressNotesListPanel.this);
			setOutputMarkupId(true);
			setNote(model);
			
			Label datelabel = new Label("date", ServiceLocator.getService(DateTimeService.class).timeElapsed(getNote().getTime()));
			datelabel.setEscapeModelStrings(false);
			add(datelabel);
			
			
			Label userlabel = new Label("user",  getNote().getLastModifiedUser().getFirstLastName());
			userlabel.setEscapeModelStrings(false);
			add(userlabel);
			
			Label textlabel = new Label("text", cleanUp(getNote().getText())) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public boolean isVisible() {
					return !edition;
				}
			};
			textlabel.setEscapeModelStrings(false);
			add(textlabel);
			Label stl = new Label("state", () -> getLabel("state.label", getNote().getState().getLabel(  
					getSessionUser().getLocale()
					 )).getObject());
			
			stl.setEscapeModelStrings(false);
			stl.add(new AttributeModifier("class", getNote().getState().getCss()));
			stl.setVisible(getNote().getState()!=ObjectState.ENABLED);
			
			add(stl);
		}
		public void setNote(IModel<ActivityProgressNote> model) {
			this.model = model;
		}
		public KbeeActivityProgressNote getNote() {
			return (KbeeActivityProgressNote)model.getObject();
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public void updateModel() {
			((KbeeActivityProgressNote)getNote()).getService(DOMObjectService.class).update();
		}
		protected void onUpdate(AjaxRequestTarget target) {
			
		}
	}	



	
	
	public ProgressNotesListPanel(String id, IModel<Activity> model) {
		super(id, model);
	}

	
	public String cleanUp(String text) {
		if (text==null)
			return "";
		return text.trim();
		
	}

	@SuppressWarnings("serial")
	public void onInitialize() {
		super.onInitialize();
		
		add(new ListView<IModel<ActivityProgressNote>>("note", () -> getNotes()) {
			public void populateItem(ListItem<IModel<ActivityProgressNote>> item) {
				item.add(new NoteViewFragment(item.getModelObject()) {
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						target.add(ProgressNotesListPanel.this);
					}
				});
			}
		});
	}

	protected List<IModel<ActivityProgressNote>> getNotes() {
		List<IModel<ActivityProgressNote>> notes = new ArrayList<IModel<ActivityProgressNote>>();
		for (ActivityProgressNote note : getModelObject().getProgressNotes()) {
			if (note.getState()!=ObjectState.DELETED)
				notes.add(new ObjectModel<ActivityProgressNote>(note));
		}
		
		Collections.sort(notes, new Comparator<IModel<ActivityProgressNote>>() {
			@Override
			public int compare(IModel<ActivityProgressNote> a, IModel<ActivityProgressNote> b) {
				try {
					return ((KbeeActivityProgressNote) a.getObject()).getCreationOffsetDateTime().compareTo(((KbeeActivityProgressNote) b.getObject()).getCreationOffsetDateTime());
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		 
		return notes;
	}

	
	public IModel<T> getContentModel() {
		return this.content_model;
	}
	
	public void setContent(IModel<T> model) {
		this.content_model=model;
	}
	
	
	public T getContent() {
		
		if (getModel()==null) {
			
			if (getContentModel()==null)
				return null;
			
			return getContentModel().getObject();
		}
		
		return (T)((KbeeContext)getModel().getObject()).getContent();
		
	}


}
