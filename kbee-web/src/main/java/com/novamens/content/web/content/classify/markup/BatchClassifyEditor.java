package com.novamens.content.web.content.classify.markup;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserService;
import com.novamens.content.web.content.markup.AttributeEditor;
import com.novamens.content.web.content.markup.DateEditor;
import com.novamens.content.web.content.markup.MembersEditor;
import com.novamens.content.web.content.markup.ContentSelectionPanel;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.model.SerializableModel;
import com.novamens.workflow.Procedure;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.util.Property;


/** 
 * 
 *  Creo un Content con todos los classifiers correspondientes
 *  
 *  Los clasificadores quedan con valores si estos son comunes a toda la
 *  selección
 *  
 *  Si los clasificadores tienen valores no comunes la opción default será
 *  dejar los valores existentes
 */

@SuppressWarnings("serial")
@Deprecated
public class BatchClassifyEditor extends ObjectEditor<Content> {
	private static final long serialVersionUID = 1L;
	
	static Logger logger = LogManager.getLogger(BatchClassifyEditor.class.getName());
	
	private List<IModel<Content>> selection;
	private List<String> nones;
	private List<Panel> memberspanels;
	//private Boolean sameclass;
	
	private class BatchModel implements IModel<Content> {
		private Content content;
		public Content getObject() {
			if (content!=null)
				return content;
			content = getContent();
			nones = new ArrayList<String>();
//			for (ClassifierTemplate template : content.getContentTemplate().getClassifiers()) {
			for (ClassifierTemplate template : getTemplates()) {
				if (!template.getClassifier().getDataSet().getDataSetType().equals(DataSetType.DATE)) {
 					List<DataSetMember> members = new ArrayList<DataSetMember>();
					boolean first = true, equals = false, none = true;
					int contents = 0;
					for (IModel<Content> model : getSelection()) {
						equals = false;
						for (Classification classification : model.getObject().getClassification()) {
							if (classification!=null && classification.getClassifier()!=null && classification.getClassifier().equals(template.getClassifier())) {
								DataSetMember member = classification.getDataSetMember();
								none = false;
								if (first) {
									members.add(member);
									equals = true;
								}
								else {
									if (!members.contains(member)) {
										equals = false;
									}
								}
							}
						}
						if (!equals && !none) {
							members.clear();
							break;
						}
						else
							contents++;
						first = false;
					}
					if (none) {
						nones.add(String.valueOf(template.getClassifier().getId()));
					}
					if (equals && contents!=getSelection().size()) {
						members.clear();
					}
					content.setClassification(template.getClassifier(), members);
				}
				else {
					List<OffsetDateTime> values = new ArrayList<OffsetDateTime>();
					boolean  equals = true, none = true;
					int contents = 0;
					for (IModel<Content> model : getSelection()) {
						for (Classification classification : model.getObject().getClassification()) {
							if (classification!=null && classification.getClassifier()!=null && classification.getClassifier().equals(template.getClassifier())) {
								contents++;
								OffsetDateTime date = classification.getDateValue();
								none = false;
								if (!values.isEmpty() && !values.contains(date)) {
									equals = false;
									break;
								}
								else {
									if (values.isEmpty())
									values.add(date);
								}
							}
						}
						if (!equals) {
							values.clear();
							break;
						}
					}
					if (none) {
						nones.add(String.valueOf(template.getClassifier().getId()));
					}
					if (equals && contents!=getSelection().size()) {
						values.clear();
					}
					content.setValues(template.getClassifier(), values);
				}
			}
			for (AttributeTemplate template : getAttributes()) {
				List<String> values = null;
				boolean equals = true;
				for (IModel<Content> model : getSelection()) {
					List<String> modelvalues = model.getObject().getAttributeValues(template.getAttribute());
					if (values==null) {
						values = modelvalues;
					}
					else {
						equals = true;
						if (modelvalues.size()!=values.size()) {
							if (values==null || values.isEmpty()) values = modelvalues;
							equals = false;
							break;
						}
						for (String value : values) {
							if (!modelvalues.contains(value)) {
								if (values==null || values.isEmpty()) values = modelvalues;
								equals = false;
								break;
							}
						}
					}
				}
				if (values==null || values.isEmpty()) {
					nones.add(String.valueOf(template.getAttribute().getId()));
				}
				if (!equals && values!=null) {
					values.clear();
				}
				content.setAttributeValues(template.getAttribute(), values);
			}
			return  content;
		}
		public void setObject(Content content) {
		}
		public void detach() {
			content = null;
		}
		private Content getContent() {
			String java_class = getSelection().get(0).getObject().getContentTemplate().getContentClass().getJavaClass();
			try {
				Content content = (Content) Class.forName(java_class).newInstance();
				content.setContentTemplate(getSelection().get(0).getObject().getContentTemplate());
				return content;
			}	
			catch (InstantiationException | IllegalAccessException  | ClassNotFoundException e) {
				logger.error(e);
				throw new KbeeRuntimeException(e);
			} 
		}
	}

	
	/**
	 * 
	 * 
	 * 
	 * @param id
	 * @param selection
	 */
	 
	public BatchClassifyEditor (String id, List<IModel<Content>> selection) {
		super(id);
		
		setOutputMarkupId(true);
		
		setSelection(selection);
		
		Form<Content> form = new Form<Content>("form", selection.get(0), Disposition.VERTICAL);
		
		Model<String> feedbackmodel = new Model<String>() {
			public String getObject() {
				if (!isSameTask()) {
					return (new StringResourceModel("notsameclass-message", BatchClassifyEditor.this, null)).getObject();
				}
				return null;
			}
		};
		
		WebMarkupContainer feedbackpanel = new WebMarkupContainer("feedback-panel") {
			public boolean isVisible() {
				return !isSameTask();
			}
		};
		
		add(feedbackpanel);
		
		feedbackpanel.add(new Label("feedback", feedbackmodel) {
			public boolean isVisible() {
				return !isSameTask();
			}
		});
		((Label)get("feedback-panel:feedback")).setEscapeModelStrings(false);
		
		form.add(feedbackpanel);
				
		setModel(new BatchModel());
		
		ListDataProvider<Panel> membersPanelsProvider = new ListDataProvider<Panel>() {
			public List<Panel> getData() {
				return getMembersPanels();
			}
		};
						
		DataView<Panel> classificationview = new DataView<Panel>("classification", membersPanelsProvider) {
			@Override
			protected void populateItem(final Item<Panel> item){
				item.add(item.getModelObject());
				item.setOutputMarkupId(true);
				item.detach();
			}
			
			@Override 
			public boolean isVisible() {
				return isSameTask();
			}
			
		};
		
		form.add(classificationview);
	
		form.add(new ContentSelectionPanel(selection) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(BatchClassifyEditor.this);
			}
			@Override
			protected Page getPage(IModel<Content> model) {
				return BatchClassifyEditor.this.getPage(model);
			}
			
			@Override
			protected List<Property<Content>> getProperties() {
				return BatchClassifyEditor.this.getSelectionProperties();
			}
			
			
		});
		
		form.add(new EditButtonsV5<Content>(this) {
			@Override 
			public boolean isEnabled() {
				return isSameTask();
			}
			
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}

			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
		});

		add(form);
		
	}
	
	
	/** --------------------------------------------------------------------------------------------
	 */
	public List<ClassifierTemplate> getTemplates() {
		List<ClassifierTemplate> classifierstemplates = new ArrayList<ClassifierTemplate>();
		
		Content content = getSelection().get(0).getObject();
		ContentTemplate contenttemplate = content.getContentTemplate();
		WorkflowService ws = content.getService(WorkflowService.class);
		
		if (ws!=null && ws.getTask()!=null && ws.getTask() instanceof WebTask && !((WebTask)ws.getTask()).getClassifiers().isEmpty()) {
			for (ClassifierTemplate classifiertemplate : ((WebTask)ws.getTask()).getClassifiers()) {
				if (classifiertemplate.isVisible() && classifiertemplate.getClassifier()!=null && classifiertemplate.getClassifier().getState()==ObjectState.ENABLED)
					classifierstemplates.add(classifiertemplate);
			}
		}
		else {
			for (ClassifierTemplate classifiertemplate : contenttemplate.getClassifiers()) {
				if (classifiertemplate.isVisible() && classifiertemplate.getClassifier().getState()==ObjectState.ENABLED)
					classifierstemplates.add(classifiertemplate);
			}
		}
		return classifierstemplates;
	}
	
	/** --------------------------------------------------------------------------------------------
	 */
	public List<AttributeTemplate> getAttributes() {
		List<AttributeTemplate> attributetemplates = new ArrayList<AttributeTemplate>();
		Content content = getSelection().get(0).getObject();
		ContentTemplate contenttemplate = content.getContentTemplate();
		WorkflowService ws =  content.getService(WorkflowService.class);
		if (ws!=null && ws.getTask()!=null && ws.getTask() instanceof WebTask && !((WebTask)ws.getTask()).getAttributes().isEmpty()) {
			for (AttributeTemplate attributetemplate : ((WebTask)ws.getTask()).getAttributes()) {
				if (attributetemplate.getAttribute()!=null && attributetemplate.getAttribute().getState()==ObjectState.ENABLED) {
					attributetemplates.add(attributetemplate);
				}	
			};
		}
		else {
			for (AttributeTemplate attributetemplate : contenttemplate.getAttributes()) {
				if (attributetemplate.getAttribute().getState()==ObjectState.ENABLED) {
					attributetemplates.add(attributetemplate);
				}	
			}
		}
		return attributetemplates;
	}
	
	/** --------------------------------------------------------------------------------------------
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				for (Panel panel : getMembersPanels()) {
					if (panel instanceof MembersEditor) {
						MembersEditor<Content> memberspanel = ((MembersEditor<Content>)panel);
						if (getUpdatedParts().contains(memberspanel.getClassifier().getName().toLowerCase())) {
							List<DataSetMember> members = memberspanel.getMembers();
							for (IModel<Content> model : getSelection()) {
								Content content = model.getObject();
								content.setClassification(memberspanel.getClassifier(), members);
								//content.getService(ContentService.class).update(getUpdatedParts());
							}
						}
					}
					if (panel instanceof AttributeEditor) {
						AttributeEditor<Content> attributespanel = ((AttributeEditor<Content>)panel);
						if (getUpdatedParts().contains(attributespanel.getAttribute().getName().toLowerCase())) {
							List<String> values = attributespanel.getValues();
							for (IModel<Content> model : getSelection()) {
								Content content = model.getObject();
								content.setAttributeValues(attributespanel.getAttribute(), values);
							}
						}
					}
				}
				for (IModel<Content> model : getSelection()) {
					Content content = model.getObject();
					content.getService(ContentService.class).update(getUpdatedParts());
				}
				super.reset();
			}
			onUpdate(target);
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));

		}
	}
	
	 
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		onUpdate(target);
	}
 
	@Override
	@SuppressWarnings("unchecked")
	public void onDetach() {
		super.onDetach();
		//sameclass = null;
		DataView<Panel> view = (DataView<Panel>)get("form:classification");
		Iterator<Item<Panel>> items = view.getItems();
		while (items.hasNext()) {
			Item<Panel> item = items.next();
			item.getModelObject().detach();
		}
		for (IModel<Content> model : getSelection()) {
			model.detach();
		}
	}
	
 
	protected Page getPage(IModel<Content> model) {
		return null;
	}
	
 
	protected void onUpdate(AjaxRequestTarget target) {
	}
	
 
	protected List<Panel> getMembersPanels() {
		
		if (this.memberspanels!=null)
			return this.memberspanels;
		
		this.memberspanels = new ArrayList<Panel>();
		
		int index = 0;
		
		getModelObject();
		
		for (AttributeTemplate template : getAttributes()) {
			if (template.getAttribute().isRequired()) {
				this.memberspanels.add(getAttributeEditor("members", template, index));
				index++;
			}
		}
		
		for (ClassifierTemplate template : getTemplates()) {
			Panel memberseditor = getMembersEditor("members", template, index);
			memberspanels.add(memberseditor);
			index++;
		}
		
		for (AttributeTemplate template : getAttributes()) {
			if (!template.getAttribute().isRequired()) {
				this.memberspanels.add(getAttributeEditor("members", template, index));
				index++;
			}
		}
		
		return memberspanels;
	}
	
	protected Panel getMembersEditor(String id, ClassifierTemplate template, int index) {
		Classifier classifier = template.getClassifier();
		MembersEditor<Content> memberseditor;
		if (classifier.getDataSetType().equals(DataSetType.DATE)) { 
			memberseditor = new DateEditor<Content>("members", getModel(template), index) {
				@Override
				public void onEdit(AjaxRequestTarget target) {
					BatchClassifyEditor.this.onEdit(target, this);
				}
				@Override
				public boolean isBatchClassification() {
					return true;
				}
			};
		}
		else {							
			memberseditor = new  MembersEditor<Content>("members", getModel(template), index) {
				@Override
				public void onEdit(AjaxRequestTarget target) {
					BatchClassifyEditor.this.onEdit(target, this);
				}
				@Override
				@SuppressWarnings("unchecked")
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					for (Panel panel : getMembersPanels()) {
						if (panel instanceof MembersEditor && !panel.equals(this)) {
							((MembersEditor<Content>)panel).onUpdate(target, getClassifier());
						}
					}
				}
				@Override
				public List<DataSetMember> getValues() {
					return BatchClassifyEditor.this.getValues();
				}
				@Override
				public List<DataSetMember> getClassification(Classifier classifier) {
					return BatchClassifyEditor.this.getValues();
				}
				@Override
				public boolean isBatchClassification() {
					return true;
				}
			};
		}
		memberseditor.setEditionEnabled(true);
		boolean leavevalues = true;
		for (Classification classification : getModelObject().getClassification()) {
			if (classification.getClassifier().equals(classifier)) {
				leavevalues = false;
				break;
			}
		}
		if (nones!=null && nones.contains(String.valueOf(classifier.getId()))) 
			leavevalues = false ;
		memberseditor.setLeaveValues(leavevalues);
		
		return memberseditor;
	}
	
	protected Panel getAttributeEditor(String id, AttributeTemplate template, int index) {
		AttributeEditor<Content> editor = new AttributeEditor<Content>(id, getModel(template), index) {
			@SuppressWarnings("unchecked")
			public void onEdit(AjaxRequestTarget target) {
				for (Panel panel : getMembersPanels()) {
					if (panel instanceof MembersEditor && ((MembersEditor<Content>)panel).isEditionEnabled() && !panel.equals(this)) {
						((MembersEditor<Content>)panel).setEditionEnabled(false);
						target.add(panel);
					}
					if (panel instanceof AttributeEditor && ((AttributeEditor<Content>)panel).isEditionEnabled() && !panel.equals(this)) {
						((AttributeEditor<Content>)panel).setEditionEnabled(false);
						target.add(panel);
					}
				}
			}
			@Override
			public boolean isBatchClassification() {
				return true;
			}
		};
		editor.setReadOnly(isReadOnly() || template.isReadOnly());
		editor.setEditionEnabled(true);
		editor.setIsEditable(!isReadOnly());
		
		boolean leavevalues = true;
		if (!getModelObject().getAttributeValues(template.getAttribute()).isEmpty()) {
			leavevalues = false;
		}
		if (nones!=null && nones.contains(String.valueOf(template.getAttribute().getId()))) 
			leavevalues = false ;
		editor.setLeaveValues(leavevalues);

		
		return editor;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	@SuppressWarnings("unchecked")
	protected void onEdit(AjaxRequestTarget target, Panel memberspanel) {
		for (Panel panel : getMembersPanels()) {
			if (panel instanceof MembersEditor && ((MembersEditor<Content>)panel).isEditionEnabled() && !panel.equals(memberspanel)) {
				((MembersEditor<Content>)panel).setEditionEnabled(false);
				target.add(panel);
			}
			if (panel instanceof AttributeEditor && ((AttributeEditor<Content>)panel).isEditionEnabled() && !panel.equals(memberspanel)) {
				((AttributeEditor<Content>)panel).setEditionEnabled(false);
				target.add(panel);
			}
		}		
	}
	
	public void onReturn() {
		
	}
	
 
	
	protected boolean isSameTask() {
		String procedureId = null, taskId = null;
		for (IModel<Content> model : getSelection()) {
			Content content = model.getObject();
			 
			WorkflowService workflowService = content.getService(WorkflowService.class);
			KbeeTask task = workflowService==null || workflowService.getTask()==null ? null : (KbeeTask)workflowService.getTask();
			
			if (task==null) {
				return false;
			}
			
			Procedure procedure = task.getProcedure();
			
			if (procedureId!=null && !String.valueOf(procedure.getId()).equals(procedureId)) {
				return false;
			}
			
			procedureId = String.valueOf(procedure.getId());
			
			if (task==null || (taskId!=null && !String.valueOf(task.getId()).equals(taskId))) {
				return false;
			}
			
			taskId = String.valueOf(task.getId());
		}
		if (procedureId==null)
			return false;
		//sameclass = true;
		return true;
	}
	
	/**  ----
	* Todos los DataSetMember de los MembersEditor 
	*/
	@SuppressWarnings("unchecked")
	private List<DataSetMember> getValues() {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (Panel panel : getMembersPanels()) {
			if (panel instanceof MembersEditor) {
				members.addAll(((MembersEditor<Content>)panel).getMembers());
			}
		}
		return members;
	}
	
	private void setSelection(List<IModel<Content>> selection) {
		String template = null;
		for (IModel<Content> model : selection) {
			if (template==null) { 
				template = model.getObject().getContentTemplate().getName();
			}
			else if (model.getObject().getContentTemplate().getName().equals(template)) {
				break;
			}
		}
		this.selection = selection;
		Collections.sort(this.selection, (new Comparator<IModel<Content>>() {
			@Override
			public int compare(IModel<Content> a, IModel<Content> b) {
				try {
					return a.getObject().getTitle().compareToIgnoreCase(b.getObject().getTitle());
				} catch (Exception e) {
					return 0;
				}
			}
		}));
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
	
	private List<IModel<Content>> getSelection() {
		return this.selection;
	}
	
	private List<Property<Content>> getSelectionProperties() {
		
		List<Property<Content>> properties = new ArrayList<Property<Content>>();
		
		properties.add(new Property<Content>() {
			public IModel<String> getLabel() {
				return new StringResourceModel("grid.title", BatchClassifyEditor.this, null);
			}
			public IModel<String> getValue(IModel<Content> model) {
				return new PropertyModel<String>(model, "title");
			}
			public String getCss() {
				return "col-lg-4";
			}
			public boolean isLink() {
				return true;
			}
		});
		

			properties.add(new Property<Content>() {
				public IModel<String> getLabel() {
					return new StringResourceModel("grid.task", BatchClassifyEditor.this, null);
				}
				public IModel<String> getValue(IModel<Content> model) {
					WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
					String taskname = workflowService==null || workflowService.getTask()==null ? "" : workflowService.getTask().getName();
					String procedurename;
					if (workflowService!=null && workflowService.getTask()!=null &&
							workflowService.getContext() !=null &&
							workflowService.getContext().getProcedure() !=null) {
						procedurename = workflowService.getContext().getProcedure().getCode() + ".  ";
					}
					else
						procedurename = "";
					return new Model<String>(procedurename + taskname);
				}
				public String getCss() {
					return "col-lg-3";
				}
			});

			
			properties.add(new Property<Content>() {
				public IModel<String> getLabel() {
					return new StringResourceModel("grid.class", BatchClassifyEditor.this, null);
				}
				public IModel<String> getValue(IModel<Content> model) {
					return new PropertyModel<String>(model, "contentTemplate.name");
				}
				public String getCss() {
					return "col-lg-2";
				}
			});
		
		
		return properties;
	}
	
}
