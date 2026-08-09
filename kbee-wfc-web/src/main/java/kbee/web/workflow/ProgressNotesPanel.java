package kbee.web.workflow;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EForm;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.form.KbeeEMemContentData;
import com.novamens.kbee.content.workflow.KbeeActivityProgressNote;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.HtmlField;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.WorkflowContext;

import kbee.util.NumberFormatter;
import kbee.web.eform.EAjaxFormResourceAddedEvent;
import kbee.web.eform.EAjaxRefreshEvent;
import kbee.web.resource.ResourceLink;
import kbee.web.resource.ResourcesPanel;
import kbee.web.uploader.UploadBehavior;
import kbee.web.user.UserAvatarPanel;
import kbee.wicket.froala.FroalaField;

import com.novamens.workflow.ActivityProgressNote;


/**
 * <p>
 * CREATE  -> todos los que puede ver el Contenido en el Monitor (y la Tarea incluye notas de Tarea)
 * READ    -> todos los que puede ver el Contenido en el Monitor (y la Tarea incluye notas de Tarea)
 * 
 * EDIT    -> el autor puede editar
 * DELETE  -> el autor borrarla
 * </p>  
 * 
 *  <p>
 * No esta completamente definido el permiso requerido para ver y crear notas de tarea.
 * Deberia ser un permiso de tarea como "Reasignar"
 * </p>
 */
@SuppressWarnings("serial")
public class ProgressNotesPanel<T extends Content> extends ModelPanel<WorkflowContext>  {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ProgressNotesPanel.class.getName());
	
	private boolean editionEnabled;
	private IModel<T> content_model;

	private IModel<ActivityProgressNote> _model = null;
	private Long new_id = null;
	
	
	private class ResourceView extends Fragment {
		IModel<Resource> model;
		String tagname = null;
		public ResourceView(IModel<Resource> model) {
			super("view", "resource-view-fragment", ProgressNotesPanel.this);
			setOutputMarkupId(true);
			setModel(model);
			ResourceLink<T> imageLink = new ResourceLink<T>("image-link", model);
			imageLink.add(getIcon(model));
			add(imageLink);
			ResourceLink<T> titleLink = new ResourceLink<T>("title-link", model);
			titleLink.add(new Label("resource-title", () -> model.getObject().getDisplayName()));
			titleLink.add(new Label("resource-size", () -> "( " + getSize() + " ) "));
			titleLink.add(new Label("resource-tag", () -> getTag()) {
				public boolean isVisible() {
					return !"".equals(getTag());
				}
			});
			add(titleLink);
			add(getMenu(model));
		}
		public IModel<Resource> getModel() {
			return model;
		}
		public void setModel(IModel<Resource> model) {
			this.model = model;
		}
		public String getSize() {
			return NumberFormatter.formatFileSize(model.getObject().getSize(), getSessionUser().getLocale());
		}
		public String getTag() {
			if (tagname==null) {
				ResourceTag tag = ((ResourceContainer)getContent()).getTag(getModel().getObject());
				tagname = tag!=null ? tag.getDisplayName() : "";
			}
			return tagname;
		}
		public void refresh(AjaxRequestTarget target) {
			addOrReplace(getMenu(getModel()));
			target.add(this);
		}
		protected Panel getMenu(IModel<Resource> model) {
			ContextMenuPanel<Resource> menu = new ContextMenuPanel<Resource>(model);
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						ResourceView.this.onRemove(target);
					}	
					@Override
					public String getLabel() {	
						return getLabelString("menuresource.delete");
					}
			});
			if (isActivityUser()) {
				boolean sep = false;
				for (ResourceTag tag  : getContent().getContentTemplate().getResourceTags()) {
					IModel<ResourceTag> tagmodel =  new ObjectModel<ResourceTag>(tag);
					if (!((ResourceContainer)getContent()).contains((KBFile)model.getObject())) {
						if (!sep) {
							menu.addItem(id ->
								new SeparatorMenuItemPanelV5<Resource>(id) {
									@Override
									public String getCssClass() {
										return "divider";
									}
								});
							sep = true;
						}
						menu.addItem(id ->
							new AjaxMenuItemPanelV5<Resource>(id) {
								@Override
								public void onClick(AjaxRequestTarget target) {
									moveToTag(getModelObject(), tagmodel.getObject(), target);
									FeedbackHelper.showInfoToast(getLabelString("resourcecopied.message", getModelObject().getName(), tagmodel.getObject().getDisplayName()));
									refresh(target);
								}	
								@Override
								public String getLabel() {	
									return getLabelString("menuresource.copyToTag", tagmodel.getObject().getDisplayName());
								}
							});
					}
				}
			}
			return menu;
		}
		protected void moveToTag(Resource resource, ResourceTag tag, AjaxRequestTarget target) {
			WebTask task = (WebTask)getWorkflowContext().getTask();
			for (EForm form : task.getForms()) {
				KbeeEMemContentData data = new KbeeEMemContentData(form, getContent()); 
				fireScanAll(new EAjaxFormResourceAddedEvent(target, getEditor(), data, resource, tag));
			}
		}
		protected void onRemove(AjaxRequestTarget target) {
		}
		protected WebMarkupContainer getIcon(IModel<Resource> model) {
			WebMarkupContainer icon = new WebMarkupContainer("glyphicon");
			icon.add(new AttributeModifier("class", model.getObject().getGlyphIcon()));
			return icon;
		}
	}	
	
	
	private class ResourcesFragment extends Fragment implements ResourcesPanel {
		IModel<ActivityProgressNote> model;
		
		
		public void onDetach() {
			super.onDetach();
			if (getModel()!=null)
				getModel().detach();
		}

		public ResourcesFragment(IModel<ActivityProgressNote> model, WebMarkupContainer dropelement, WebMarkupContainer picker) {
			super("resources", "resources-fragment", ProgressNotesPanel.this);
			setOutputMarkupId(true);
			setModel(model);
			addOrReplaceView();
			add(new UploadBehavior() {
				@Override
				public boolean isEnabled() {
					return ResourcesFragment.this.isEnabled();
				}
				@Override
				protected String getUrl() {
					return "/formupload?path="+ResourcesFragment.this.getPath();
				}
				@Override
				protected String getDropElement() {
					return dropelement.getMarkupId();
				}
				@Override
				public Component getResourcesPanel() {
					return ResourcesFragment.this.get("resources-view");
				}
				@Override
				public void bind(Component component) {
					boolean found = false;
					for (Behavior behavior : ProgressNotesPanel.this.getBehaviors()) {
						if (behavior instanceof RefreshBehavior) {
							setBehaviorId(((RefreshBehavior)behavior).getId());
							found = true;
							break;
						}
					}
					if (!found) {
						ProgressNotesPanel.this.add(new RefreshBehavior(ProgressNotesPanel.this.getMarkupId()));
					}	
				}
				@Override
				protected void onUpload(AjaxRequestTarget target, String component) {
					fireScanAll(new EAjaxRefreshEvent(target, component));
				}
				@Override
				protected String getBrowseButton() {
					return picker.getMarkupId();
				}
			});
			add(new WicketEventListener<EAjaxRefreshEvent>() {
				@Override
				public void onEvent(EAjaxRefreshEvent event) {
					if (handle(event)) {
						refresh(event.getRequestTarget());
					}
				}
				public boolean handle(EAjaxRefreshEvent event) {
					return ResourcesFragment.this.getMarkupId().equals(event.getComponentId());
				}
			});
		}
		
		public IModel<ActivityProgressNote> getModel() {
			return model;
		}
		
		public void setModel(IModel<ActivityProgressNote> model) {
			this.model = model;
		}
		public void add(Resource resource) {
			getNote().addResource(resource);
			onUpload(resource);
		}
		public void remove(Resource resource) {
			getNote().removeResource(resource);
			onRemove(resource);
		}
		public void addVersion(Resource resource, Resource version) {
		}
		public boolean isEnabled() {
			return true;
		}
		public List<IModel<Resource>> getResources() {
			List<IModel<Resource>> resources = new ArrayList<IModel<Resource>>();
			for (Resource resource : getNote().getResources()) {
				resources.add(new ObjectModel<Resource>(resource));
			}
			return resources;
		}
		public KbeeActivityProgressNote getNote() {
			return (KbeeActivityProgressNote)model.getObject();
		}
		protected void refresh(AjaxRequestTarget target) {
			target.add(get("resources-view"));
		}
		
		//protected void onUpdate(AjaxRequestTarget target) {
		//}
		
		protected void onUpload(Resource resource) {
		}
		protected void onRemove(Resource resource) {
		}
		protected void addOrReplaceView() {
			WebMarkupContainer view = new WebMarkupContainer("resources-view");
			view.setOutputMarkupId(true);
			view.add(new ListView<IModel<Resource>>("resource", () -> getResources()) {
				protected void populateItem(ListItem<IModel<Resource>> item) {
					item.add(new ResourceView(item.getModelObject()) {
						protected void onRemove(AjaxRequestTarget target) {
							ResourcesFragment.this.remove(getModel().getObject());
							ResourcesFragment.this.refresh(target);
						}
					});
				}
			});
			addOrReplace(view);
		}
	}
	
	
	private class NoteViewFragment extends Fragment {
		
		private IModel<ActivityProgressNote> model;
		private boolean edition = false;
		private String text;
		
		public NoteViewFragment(IModel<ActivityProgressNote> model) {
			super("view", "note-view-fragment", ProgressNotesPanel.this);
			setOutputMarkupId(true);
			setNote(model);
			boolean e = false;
			add(new UserAvatarPanel("photo", new ObjectModel<User>(getNote().getLastModifiedUser())));
			
			Label datelabel = new Label("date", ServiceLocator.getService(DateTimeService.class).timeElapsed(getNote().getTime()));
			datelabel.setEscapeModelStrings(false);
			add(datelabel);
			
			Label userlabel = new Label("user",  getNote().getLastModifiedUser().getFirstLastName());
			userlabel.setEscapeModelStrings(false);
			add(userlabel);
			
			add(new FroalaField("editor", new PropertyModel<String>(model, "text")) {
				@Override
				public IModel<String> getLabel() {
					return new Model<String>("");	
				}
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					NoteViewFragment.this.updateModel();
				}
				public void onClose(AjaxRequestTarget target) {
					super.onClose(target);
					edition = false;
					new_id = null;
					target.add(NoteViewFragment.this);
				}
				public void onOpen(AjaxRequestTarget target) {
					edition = true;
					target.add(NoteViewFragment.this.get("toolbar"));
				}
				public boolean isInputEnabled() {
					return true;
				}
				
				// THIS IS FOR THE IAMGES
				//@Override
				//protected String getBaseUrl() {
				//	return "/resource/content/"+ (new ContentId(getContent())).toString() +"/";
				//}
				
				@Override
				protected Content getContent() {
					return ProgressNotesPanel.this.getContent();
				}
				@Override
				public boolean isVisible() {
					return true;
				}
				@Override
				public boolean isEditionEnabled() {
					return NoteViewFragment.this.isEditionEnabled();
				}
				@Override
				protected boolean includeClose() {
					return false;
				}
			});
			
			Label stl = new Label("state", () -> getLabel("state.label", getNote().getState().getLabel(	getSessionUser().getLocale())).getObject());
			
			stl.setVisible(getNote().getState()!=ObjectState.ENABLED);
			
			stl.setEscapeModelStrings(false);
			stl.add(new AttributeModifier("class", getNote().getState().getCss()));
			add(stl);
			
			WebMarkupContainer toolbar = new WebMarkupContainer("toolbar");
			
			toolbar.setOutputMarkupId(true);
					
			toolbar.add(new AjaxLink<Void>("edit-button") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					edition = true;
					new_id = null;
					((FroalaField)getHtmlEditor()).setOpen(true);
					target.add(NoteViewFragment.this);
					_model = null;
				}
				@Override
				public boolean isVisible() {
					
					// who can edit -> el dueño de la tarea por ahora
					//
					return isEditionEnabled() && !edition;
				}
			});
			
			toolbar.add(new AjaxLink<Void>("delete-button") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					edition = false;
					new_id = null;
					_model = null;
					delete(getNote());
					onUpdate(target);
				}
				@Override
				public boolean isVisible() {
					// who can delete -> el dueño de la tarea por ahora
					return isEditionEnabled() && !ObjectState.DELETED.equals(getNote().getState()) && !edition;
				}
			});
			
			toolbar.add(new AjaxSubmitLink("close-button") {
				@Override
				public void onSubmit(AjaxRequestTarget target) {
					getHtmlEditor().onClose(target);
				}
				@Override
				public boolean isVisible() {
					return edition;
				}
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					getHtmlEditor().updateAjaxCloseAttributes(attributes);
				}
			});
			
			WebMarkupContainer resourcepicker = new WebMarkupContainer("resource-picker-button") {
				public boolean isVisible() {
					return isEditionEnabled() && !edition;
				}
			};
			resourcepicker.setOutputMarkupId(true);
			toolbar.add(resourcepicker);
			
			add(toolbar);
			
			addOrReplaceResourcesPanel();
			
			if (ProgressNotesPanel.this._model!=null) {
				e = ProgressNotesPanel.this._model.getObject().equals(model.getObject());
				if (e) {
					Optional<AjaxRequestTarget> optionaltarget = RequestCycle.get().find(AjaxRequestTarget.class);
					if (optionaltarget.get()!=null) {
						((FroalaField)getHtmlEditor()).setOpen(true);
						//optionaltarget.get().appendJavaScript(getHtmlEditor().getFocusScript());
						edition = true;
					}
				}
			}	
			
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
			if (text!=null)
				this.text = text.replace("<p><br></p>", "");
			else
				this.text=text;
		}
		public void updateModel() {
			boolean publish = false;
			KbeeActivityProgressNote note = getNote();
			if (ObjectState.DRAFT.equals(note.getState())) {
				publish = true;
				note.setState(ObjectState.ENABLED);
			}
			note.getService(DOMObjectService.class).update();
			if (publish) {
				publish(note);
			}
		}
		protected void onUpdate(AjaxRequestTarget target) {
		}
		public void setEdit( boolean b) {
			this.edition=b;
		}
		public boolean isEdit() {
			return this.edition;
		}
		public boolean isEditionEnabled() {
  			return ProgressNotesPanel.this.isEditionEnabled() && (isActivityUser() || isAuthorUser());
		}
		protected HtmlField getHtmlEditor() {
			return ((HtmlField)NoteViewFragment.this.get("editor"));
		}
		protected void refresh(AjaxRequestTarget target) {
			target.add(this);
			addOrReplaceResourcesPanel();
		}
		protected void addOrReplaceResourcesPanel() {
			addOrReplace(new ResourcesFragment(model, NoteViewFragment.this, (WebMarkupContainer)get("toolbar:resource-picker-button")) {

				//@Override
				//protected void onUpdate(AjaxRequestTarget target) {
				//}
				@Override
				protected void onUpload(Resource resource) {
					updateModel();
				}
				@Override
				protected void onRemove(Resource resource) {
					updateModel();
				}
			});
		}
		protected boolean isAuthorUser() {
			return getNote().getLastModifiedUser().equals(getSessionUser());
		}
	}	

	/** -----------------------------------------------------------------
	 * 
	 * @param id
	 * @param model
	 * @param content_model
	 * 
	 * 
	 */
	public ProgressNotesPanel(String id, IModel<WorkflowContext> model, IModel<T> content_model) {
		super(id);
		setModel(model);
		setContent(content_model);
		setOutputMarkupId(true);
		new_id = null;
	}
	
	public IModel<T> getContentModel() {
		return this.content_model;
	}
	
	public void setContent(IModel<T> model) {
		this.content_model=model;
	}

	@SuppressWarnings("unchecked")
	public T getContent() {
		if (getModel()==null) {
			if (getContentModel()==null)
				return null;
			return getContentModel().getObject();
		}
		return (T)((KbeeContext)getModel().getObject()).getContent();
	}

	public boolean isEditionEnabled() {
		return editionEnabled;
	}

	public void setEditionEnabled(boolean editionEnabled) {
		this.editionEnabled = editionEnabled;
	}
	
	public WorkflowContext getWorkflowContext() {
		return getModelObject();
	}
	
	
	/**
	 *  
	 *  
	 *  
	 *  
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("new-button") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				addNote();
				target.add(ProgressNotesPanel.this);
			}
			@Override
			public boolean isVisible() {
				return getWorkflowContext()!=null;
			}
			
			@Override
			public boolean isEnabled() {
				
				if (isActivityUser())
					return true;
				
				if (ProgressNotesPanel.this.isEditionEnabled())
						return true;	

				return false;
			}
			
		});
		
		addNotesView();
	}
	
	protected void addNotesView() {
		add(new ListView<IModel<ActivityProgressNote>>("note", () -> getNotes()) {
			public void populateItem(ListItem<IModel<ActivityProgressNote>> item) {
				NoteViewFragment nv = 
				new NoteViewFragment(item.getModelObject()) {
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						target.add(ProgressNotesPanel.this);
					}
				};
				nv.setEdit(new_id!=null && new_id.equals(item.getModelObject().getObject().getId()));
				item.add(nv);
			}
		});
	}
	
	protected List<IModel<ActivityProgressNote>> getNotes() {
		
		List<IModel<ActivityProgressNote>> notes = new ArrayList<IModel<ActivityProgressNote>>();
		
		if (getModelObject().getCurrentActivity()!=null)
			for (ActivityProgressNote note : getModelObject().getCurrentActivity().getProgressNotes()) {
				if (note.getState()!=ObjectState.DELETED)
					notes.add(new ObjectModel<ActivityProgressNote>(note));
			}
		
		Collections.sort(notes, new Comparator<IModel<ActivityProgressNote>>() {
			@Override
			public int compare(IModel<ActivityProgressNote> a, IModel<ActivityProgressNote> b) {
				try {
					return ((KbeeActivityProgressNote) b.getObject()).getCreationOffsetDateTime().compareTo(((KbeeActivityProgressNote) a.getObject()).getCreationOffsetDateTime());
				} 
				catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		}); 
		return notes;
	}
	
	protected void addNote() {
		ActivityProgressNote a = getContent().getService(WorkflowService.class).createProgressNote();
		if (a!=null)
			new_id = a.getId();
		_model = new ObjectModel<ActivityProgressNote>(a);
	}
	
	protected void delete(ActivityProgressNote note) {
		getContent().getService(WorkflowService.class).deleteProgressNote(note);
	}
	
	protected void publish(ActivityProgressNote note) {
		getContent().getService(WorkflowService.class).publish(note);
	}
	
	protected boolean isActivityUser() {
		return getModelObject().getCurrentActivity()!=null && getWorkflowContext().getCurrentActivity().getUser().equals(getSessionUser());
	}
	
	
	private Editor<?> getEditor() {
		MarkupContainer parent = getParent();
		Editor<?> editor = null;
		while (editor==null && parent!=null) {
			if (parent instanceof Editor) {
				editor = (Editor<?>)parent;
			}
			else
				parent = parent.getParent();
		}
		return editor;
	}
}
