package com.novamens.content.web.content.markup;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.content.model.SubsectionTemplate;

import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeClassification;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;

import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.model.SerializableModel;

@SuppressWarnings("serial")
public class
ContentClassificationEditor<T extends Content> extends ObjectEditorPanel<T> implements kbee.web.content.editor.ClassificationPanel<T> {
	private static final long serialVersionUID = 1L;
			
	static Logger logger = LogManager.getLogger(ContentClassificationEditor.class.getName());

	private List<Panel> modelpanels;
	
	private boolean select_preference  	= false;
	private boolean all_open  			= false;
	private boolean show_only_not_empty = false; // this is for Read only pages, that may want to display only non-empty fields

	private String caller_console = null;
	
	private List<ModelElementTemplate> structure = null;
	private IModel<ModelSection> sectionmodel = null;
	
	private IModel<String> css = new Model<String>("btn btn-default btn-sm");

	public class Toolbar extends Fragment {
		public Toolbar() {
			super("toolbar", "toolbar-fragment", ContentClassificationEditor.this);
		}

		@Override
		public void onBeforeRender() {
			super.onBeforeRender();

			if (get("menulink")!=null)
				return;

			// Defaults ----------------------------------------------------------------------------
			//
			//
			AjaxLink<Void> exp = new AjaxLink<Void>("expand-all") {
				
				@Override
				public boolean isVisible() {
					return ContentClassificationEditor.this.getModel().getObject().getWorkspace()!=null &&
							ContentClassificationEditor.this.getModel().getObject().getWorkspace().equals( (Long) getSessionUser().getId());
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					if (!isReadOnly()) {
						setAllOpen(!isAllOpen());

						getSessionUser().getService(PreferencesService.class).setValue(ContentClassificationEditor.class.getSimpleName() + (getCallerConsole()!=null? ("-"+getCallerConsole()):""),"allopen", isAllOpen() ? "yes":"no");
						target.add(ContentClassificationEditor.this);
					}
				}
			};
			
			add(exp);
			
			IModel<String> etm = new Model<String>() {
				public String getObject() {
					if (isAllOpen()) 
						return new StringResourceModel("collapse", ContentClassificationEditor.this, null).getString();
					return new StringResourceModel("edit", ContentClassificationEditor.this, null).getString();
				}
			};

			Label edittext = new Label("edit-text", etm);

			exp.add(edittext);
			
			
			
			// Defaults ----------------------------------------------------------------------------
			//
			//
			ContextMenuPanel<T> menu = new ContextMenuPanel<T>(getModel());
				
			menu.addItem(new MenuItemFactory<T>() {
				@Override
				public AbstractMenuItemPanelV5<T> getItem(String id) {
					return new AjaxMenuItemPanelV5<T>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							setAsDefault();
						}
						@Override
						public String getLabel() {	
							return ContentClassificationEditor.this.getStringLabel("menu.setdefaults");
						}
						@Override
						public String getWorkingLabel() {	
							return ContentClassificationEditor.this.getStringLabel("menu.settingdefaults");
						}
					};
				}
			});
			
			menu.addItem(new MenuItemFactory<T>() {
				@Override
				public AbstractMenuItemPanelV5<T> getItem(String id) {
					return new AjaxMenuItemPanelV5<T>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							clearDefault();
						}
						@Override
						public String getLabel() {	
							return ContentClassificationEditor.this.getStringLabel("menu.cleardefaults");
						}
						@Override
						public String getWorkingLabel() {	
							return ContentClassificationEditor.this.getStringLabel("menu.clearingdefaults");
						}
					};
				}
			});
			
			Panel menuPanel = menu;
			
			WebMarkupContainer menulink = new WebMarkupContainer("menulink");
			add(menulink);
			add(menuPanel);
		}
		
		@Override
		public boolean isVisible() {
			return !getEditor().isReadOnly();
		}
	}
	
	public ContentClassificationEditor() {
		this("classification");
	}
	
	public ContentClassificationEditor(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public ContentClassificationEditor(String id, boolean readOnly) {
		super(id);
		setOutputMarkupId(true);
		setReadOnly(readOnly);
	}
	
	public ContentClassificationEditor(String id, IModel<ModelSection> sectionmodel, boolean readOnly) {
		super(id);
		setOutputMarkupId(true);
		setSection(sectionmodel);
		setReadOnly(readOnly);
	}

	public boolean isAllOpen() {
		return all_open;
	}

	public void setAllOpen( boolean b) {
		this.all_open=b;

		for (Panel panel: getModelPanels()) {
			if (panel instanceof MembersEditor) {
				((MembersEditor<?>) panel).setEditionEnabled(this.all_open);
			}
			else if (panel instanceof AttributeEditor) {
				((AttributeEditor<?>) panel).setEditionEnabled(this.all_open);
			}
		}
	}
	
	public void setSection(IModel<ModelSection> model) {
		this.sectionmodel = model;
	}
	
	public ModelSection getSection() {
		return this.sectionmodel == null ? null : this.sectionmodel.getObject();
	}
	
	public List<Panel> getModelPanels() {
		
		if (this.modelpanels!=null)
			return this.modelpanels;
		
		this.modelpanels = new ArrayList<Panel>();

		int index = 0;
		
		for (ModelElementTemplate template : getStructure()) {
			if (template instanceof AttributeTemplate) {
				this.modelpanels.add(getAttributeEditor("members", getModel((AttributeTemplate)template), index));
				index++;
			}
			else
			if (template instanceof SubsectionTemplate) {
				SubsectionPanel sp=new SubsectionPanel("members", getModel((SubsectionTemplate)template));
				sp.add(new AttributeModifier("class", "subsection " + (index==0?" first ": "")));
				this.modelpanels.add(sp);
				index++;
			}
			else
			if (template instanceof ClassifierTemplate) {
				this.modelpanels.add(getMembersEditor("members", getModel((ClassifierTemplate)template), index));
				index++;
			}
		}
		
		return this.modelpanels;
	}

	@SuppressWarnings("unchecked")
	public void setFocus(AjaxRequestTarget target, Classifier classifier) {
		onBeforeRender();
		for (Panel panel : getModelPanels()) {
			if (panel instanceof MembersEditor) {
				if (((MembersEditor<T>)panel).getClassifier().equals(classifier)) {
					((MembersEditor<T>)panel).setFocus(target);
				}
			}
		}	
	}
	
	@SuppressWarnings("unchecked")
	public void setFocus(AjaxRequestTarget target, AttributeTemplate template) {
		onBeforeRender();
		for (Panel panel : getModelPanels()) {
			if (panel instanceof AttributeEditor) {
				if (((AttributeEditor<T>)panel).getTemplate().getAttribute().getId().equals(template.getAttribute().getId())) {
					((AttributeEditor<T>)panel).setFocus(target);
				}
			}
		}	
	}
	
	@SuppressWarnings("unchecked")
	public Panel getEditor(AttributeTemplate template) {
		//onBeforeRender();
		for (Panel panel : getModelPanels()) {
			if (panel instanceof AttributeEditor) {
				if (((AttributeEditor<T>)panel).getTemplate().getAttribute().getId().equals(template.getAttribute().getId())) {
					return panel;
				}
			}
		}	
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Panel getEditor(ClassifierTemplate template) {
		//onBeforeRender();
		for (Panel panel : getModelPanels()) {
			if (panel instanceof MembersEditor) {
				if (((MembersEditor<T>)panel).getClassifier().equals(template.getClassifier())) {
					return panel;
				}
			}
		}	
		return null;
	}
	
	public List<ModelElementTemplate> getStructure() {
		
		if (structure!=null)
			return structure;
		
		this.structure = new ArrayList<ModelElementTemplate>();
		
		WorkflowService ws =  getModelObject().getService(WorkflowService.class);
		
		Map<Long, Long> classifiers_not_empty = new HashMap<Long, Long>();
		if (this.show_only_not_empty) {
			for (Classification clasification : getModel().getObject().getClassification()) {
				classifiers_not_empty.put((Long) clasification.getClassifier().getId(), (Long) clasification.getClassifier().getId());
			}
		}
			
		List<ModelElementTemplate> structure;
		
		if (getSection()!=null) {
			structure = getSection().getStructure(); 
		}
		else {
			if (ws!=null && 
				ws.getTask()!=null && 
				ws.getTask() instanceof WebTask && 
				//((WebTask)ws.getTask()).getStructure()!=null &&
				!((WebTask)ws.getTask()).getStructure().isEmpty()) {
				structure = ((WebTask)ws.getTask()).getStructure();
			}
			else {
				structure = getModelObject().getContentTemplate().getStructure();
			}
			
		}
			
		for (ModelElementTemplate template : structure) {
			if (template instanceof ClassifierTemplate) {
				ClassifierTemplate classifiertemplate = (ClassifierTemplate)template;
				//if (classifiertemplate.getSubsection()!=null && !"".equals(classifiertemplate.getSubsection().trim())) {
				//	this.structure.add(new KbeeSubsectionTemplate2(new KbeeSubsection(classifiertemplate.getSubsection())));
				//}
				if (classifiertemplate.isVisible() && 
					classifiertemplate.getClassifier().getState()==ObjectState.ENABLED &&
					(!this.show_only_not_empty || classifiers_not_empty.containsKey(classifiertemplate.getClassifier().getId())))
					this.structure.add(classifiertemplate);
			}
			if (template instanceof AttributeTemplate) {
				AttributeTemplate attributetemplate = (AttributeTemplate)template;
				//if (attributetemplate.getSubsection()!=null && !"".equals(attributetemplate.getSubsection().trim())) {
				//	this.structure.add(new KbeeSubsectionTemplate2(new KbeeSubsection(attributetemplate.getSubsection())));
				//}
				if (attributetemplate.isVisible()) {
					if (this.show_only_not_empty) {
						List<String> values = getModel().getObject().getAttributeValues(attributetemplate.getAttribute());
						if (values!=null && !values.isEmpty()) {
							this.structure.add(attributetemplate);
						}
					}
					else {
						this.structure.add(attributetemplate);
					}
				}	
			}
			if (template instanceof SubsectionTemplate) {
				this.structure.add(template);
			}
		}
		
		return this.structure;
	} 
	
	@SuppressWarnings("unchecked")
	public List<DataSetMember> getClassificationMembers() {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (Panel panel : getModelPanels()) {
			if (panel instanceof MembersEditor) {
				for (DataSetMember member : ((MembersEditor<T>)panel).getMembers()) {
					members.add(member);
				}
			}
		}
		return members;
	}
	
	@SuppressWarnings("unchecked")
	public List<DataSetMember> getClassificationMembers(Classifier classifier) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (Panel panel : getModelPanels()) {
			if (panel instanceof MembersEditor) {
				for (DataSetMember member : ((MembersEditor<T>)panel).getMembers()) {
					members.add(member);
				}
			}
		}
		return members;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public List<Classification> getClassification() {
		List<Classification> classification = new ArrayList<Classification>();
		for (Panel panel : getModelPanels()) {
			if (panel instanceof MembersEditor) {
				for (DataSetMember member : ((MembersEditor<T>)panel).getMembers()) {
					classification.add(new KbeeClassification(((MembersEditor<T>)panel).getClassifier(), member, getModelObject()));
				}
			}
			if (panel instanceof DateEditor) {
				for (OffsetDateTime date : ((DateEditor<T>)panel).getOffsetDates()) {
					classification.add(new KbeeClassification(((DateEditor<T>)panel).getClassifier(), date, getModelObject()));
				}
			}
		}
		return classification;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean includes(Classifier classifier) {
		for (Panel panel : getModelPanels()) {
			if (panel instanceof MembersEditor) {
				if (((MembersEditor<T>)panel).getClassifier().equals(classifier)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Classifier> getClassifiers() {
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (Panel panel : getModelPanels()) {
			if (panel instanceof MembersEditor) {
				classifiers.add(((MembersEditor<T>)panel).getClassifier());
			}	
		}
		return classifiers;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean includes(Attribute attribute) {
		for (Panel panel : getModelPanels()) {
			if (panel instanceof AttributeEditor) {
				if (((AttributeEditor<T>)panel).getAttribute().equals(attribute)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public List<String> getAttributeValue(Attribute attribute) {
		for (Panel panel : getModelPanels()) {
			if (panel instanceof AttributeEditor) {
				if (((AttributeEditor<?>)panel).getTemplate().getAttribute().equals(attribute)) {
					List<String> values = ((AttributeEditor<?>)panel).getValues();
					return values;
				}
			}
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void update(T content) {
		for (Panel panel : getModelPanels()) {
			if ((panel instanceof AttributeEditor) && ((AttributeEditor<?>)panel).isUpdated()) {
				((AttributeEditor<T>)panel).update(content);
			}
			else {
				if ((panel instanceof MembersEditor) && ((MembersEditor<T>)panel).isUpdated()) {
					((MembersEditor<T>)panel).update(content);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean isUpdated() {
		for (Panel panel : getModelPanels()) {
			if ((panel instanceof AttributeEditor) && ((AttributeEditor<?>)panel).isUpdated()) {
				return true;
			}
			else {
				if ((panel instanceof MembersEditor) && ((MembersEditor<T>)panel).isUpdated()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void validate() {
		
	}
	
	public IModel<String> getCss() {
		return new Model<String>("content-classification");
	}
	
	public T getModelObject() {
		return getEditor().getModelObject();
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		if (get("classification")==null) {
			
			// if the user is the owner of the task
			// and !selectpreference
			//
			if 	 ( (getModelObject().getWorkspace()!=null) && 
				   (getModelObject().getWorkspace().equals((Long) getSessionUser().getId()))) {
				 
				setAllOpen((getSessionUser().getService(PreferencesService.class).getValue(ContentClassificationEditor.class.getSimpleName() + (getCallerConsole()!=null? ("-"+getCallerConsole()):""), "allopen", "no").equals("yes")));
			}
			else {
				setReadOnly(true);
				setAllOpen(false);
			}
			
			add(new Toolbar() {
				public boolean isVisible() {
					return !isReadOnly();
				}
			});
			
			add(new ListView<Panel>("classification", getModelPanels()) {
				protected void populateItem(ListItem<Panel> item){
					item.setOutputMarkupId(true);
					item.add(new AttributeModifier("class", getCssClassification()));
					item.getModelObject().add(new AttributeModifier("class", getCssClassificationElement()));
					item.add(item.getModelObject());
					item.setVisible(item.getModelObject().isVisible());
					item.detach();
				}
			});	
			
			add (new InvisiblePanel("eid"));
		}
	}	
	
	public boolean isSelectPreference() {
		return this.select_preference;
	}
	
	public void setSelectPreference(boolean select_preference) {
		this.select_preference=select_preference;		
	}
	
	public void  setCallerConsole(String str) {
		caller_console = str;
	}
	
	public String getCallerConsole() {
		return caller_console;
	}

	public boolean showOnlyNotEmpty() {
		return this.show_only_not_empty;
	}
	
	public void setShowOnlyNotEmpty(boolean b) {
		this.show_only_not_empty=b;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (getEditor()!=null)
		for (Panel panel : getModelPanels()) {
			panel.detach();
		}
		this.structure=null;
		if (sectionmodel!=null)
			sectionmodel.detach();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setAsDefault() {
		for (Panel panel : getModelPanels()) {
			if (panel instanceof MembersEditor) {
				MembersEditor<T> memberseditor = (MembersEditor) panel;
				memberseditor.setAsDefault();
			}
			else if (panel instanceof AttributeEditor) {
				AttributeEditor<T> attributeeditor = (AttributeEditor) panel;
				attributeeditor.setAsDefault();
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void clearDefault() {
		for (Panel panel : modelpanels) {
			if (panel instanceof MembersEditor) {
				MembersEditor<T> memberspanel = (MembersEditor) panel;
				memberspanel.clearDefault();
			}
			else if (panel instanceof AttributeEditor) {
				AttributeEditor<T> attributepanel = (AttributeEditor) panel;
				attributepanel.clearDefault();
			}
		}
	}
	
	protected IModel<String> getCssEditButton() {
		return css;
	}
	
	protected AttributeEditor<T> getAttributeEditor(String id, IModel<AttributeTemplate> templatemodel, int index) {
		AttributeEditor<T> editor = new AttributeEditor<T>(id, templatemodel, index) {
			@SuppressWarnings("unchecked")
			public void onEdit(AjaxRequestTarget target) {
				for (Panel panel : getModelPanels()) {
					if (panel instanceof MembersEditor && ((MembersEditor<T>)panel).isEditionEnabled() && !panel.equals(this) && !isAllOpen()) {
						((MembersEditor<T>)panel).setEditionEnabled(false);
						target.add(panel);
					}
					if (panel instanceof AttributeEditor && ((AttributeEditor<T>)panel).isEditionEnabled() && !panel.equals(this) && !isAllOpen()) {
						((AttributeEditor<T>)panel).setEditionEnabled(false);
						target.add(panel);
					}
				}
			}
			@Override
			@SuppressWarnings("unchecked")
			public void onBlur(AjaxRequestTarget target) {
				boolean next = false;
				for (Panel panel : getModelPanels()) {
					if (next) {
						if (panel instanceof MembersEditor) {
							((MembersEditor<T>)panel).setFocus(target);
							break;
						}
						if (panel instanceof AttributeEditor) {
							((AttributeEditor<T>)panel).setFocus(target);
							break;
						}
					}
					else {
						next = panel.equals(this);
					}
				}
			}
			@SuppressWarnings("unchecked")
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				for (Panel panel : getModelPanels()) {
					if (panel instanceof MembersEditor && !panel.equals(this)) {
						((MembersEditor<T>)panel).onUpdate(target, getAttribute());
					}
					if (panel instanceof AttributeEditor) {
						((AttributeEditor<T>)panel).onUpdate(target, getAttribute());
					}
				}
			}
			@Override
			public List<DataSetMember> getClassification() {
				return ContentClassificationEditor.this.getClassificationMembers();
			}
			@Override
			public List<DataSetMember> getClassification(Classifier classifier) {
				return ContentClassificationEditor.this.getClassificationMembers(classifier);
			}
		};
		editor.setReadOnly(isReadOnly() || templatemodel.getObject().isReadOnly());
		editor.setEditionEnabled(isAllOpen());
		editor.setIsEditable(!isReadOnly());
		
		return editor;
	}
	
	@SuppressWarnings("deprecation")
	protected MembersEditor<T> getMembersEditor(String id, IModel<ClassifierTemplate> templatemodel, int index) {
		if (templatemodel.getObject().getClassifier().getDataSetType().equals(DataSetType.DATE)) {
			MembersEditor<T> editor = new DateEditor<T>(id, templatemodel, index) {
				@Override
				@SuppressWarnings("unchecked")
				public void onEdit(AjaxRequestTarget target) {
					for (Panel panel : getModelPanels()) {
						if (panel instanceof MembersEditor && ((MembersEditor<T>)panel).isEditionEnabled() && !panel.equals(this)) {
							((MembersEditor<T>)panel).setEditionEnabled(false);
							target.add(panel);
						}
						if (panel instanceof AttributeEditor && ((AttributeEditor<T>)panel).isEditionEnabled() && !panel.equals(this)) {
							((AttributeEditor<T>)panel).setEditionEnabled(false);
							target.add(panel);
						}
					}
				}
			};
			
			editor.setIsReadOnly(isReadOnly() || templatemodel.getObject().isReadOnly());
			editor.setIsEditable(!isReadOnly());
			if (!editor.isReadOnly())
				editor.setEditionEnabled(isAllOpen());
			
			return editor;
		}
		else {
			MembersEditor<T> editor = new MembersEditor<T>(id, templatemodel,  index) {
				@Override
				@SuppressWarnings("unchecked")
				public void onEdit(AjaxRequestTarget target) {
					for (Panel panel : getModelPanels()) {
						if (panel instanceof MembersEditor && ((MembersEditor<T>)panel).isEditionEnabled() && !panel.equals(this)) {
							((MembersEditor<T>)panel).setEditionEnabled(false);
							target.add(panel);
						}
						if (panel instanceof AttributeEditor && ((AttributeEditor<T>)panel).isEditionEnabled() && !panel.equals(this)) {
							((AttributeEditor<T>)panel).setEditionEnabled(false);
							target.add(panel);
						}
					}
				}
				@Override
				@SuppressWarnings("unchecked")
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					for (Panel panel : getModelPanels()) {
						if (panel instanceof MembersEditor && !panel.equals(this)) {
							((MembersEditor<T>)panel).onUpdate(target, getClassifier());
						}
						if (panel instanceof AttributeEditor) {
							((AttributeEditor<T>)panel).onUpdate(target, getClassifier());
						}
					}
				}
				@Override
				public List<DataSetMember> getValues() {
					return ContentClassificationEditor.this.getClassificationMembers();
				}
				@Override
				public List<Classification> getClassification() {
					return ContentClassificationEditor.this.getClassification();
				}
				@Override
				public List<DataSetMember> getClassification(Classifier classifier) {
					return ContentClassificationEditor.this.getClassificationMembers(classifier);
				}
			};
			
			editor.setIsReadOnly(isReadOnly() || templatemodel.getObject().isReadOnly());
			editor.setIsEditable(!isReadOnly());
			
			if (!editor.isReadOnly())
				editor.setEditionEnabled(isAllOpen());
			
			return editor;
		}
	}	

	protected IModel<String> getCssIconTextEditButton() {
		return new Model<String>("x-centered");
	}

	protected IModel<String> getCssClassification() {
		return new Model<String>("col-lg-12 col-md-12 col-xs-12 nopadding");
	}

	protected IModel<String> getCssClassificationElement() {
		return new Model<String>("col-lg-12 col-xs-12 toleft");
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected String getStringLabel(String resourceKey) {
		return ((new StringResourceModel(resourceKey, this, null)).getString());
	}
	
	
	protected IModel<ClassifierTemplate> getModel(ClassifierTemplate template) {
		if (template instanceof Identifiable)
			return new ObjectModel<ClassifierTemplate>(template);
		else
			return new SerializableModel<ClassifierTemplate>(template);
	}

	protected IModel<AttributeTemplate> getModel(AttributeTemplate template) {
		if (template instanceof Identifiable)
			return new ObjectModel<AttributeTemplate>(template);
		else
			return new SerializableModel<AttributeTemplate>(template);
	}
	
	protected IModel<SubsectionTemplate> getModel(SubsectionTemplate template) {
		if (template instanceof Identifiable) {
			return new ObjectModel<SubsectionTemplate>(template);
		}	
		else
			return new SerializableModel<SubsectionTemplate>(template);
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
