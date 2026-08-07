package com.novamens.content.web.content.markup;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.novamens.indexer.query.QuerySortOrder;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.DataAccessService;
import com.novamens.content.user.UserService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.model.KbeeCodeExecutor;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Identifiable;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.model.ObjectModel;


import kbee.web.event.wicket.EditorEvent;
import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.form.ChoiceFieldWithHistory;
import kbee.web.service.ApplicationSiteMapService;


/** ------------------------------------------------------------------------
 * 
 * Open New -> AllOpen 
 * Open no new -> Task
 * 
 *  Navigate New -> Settings
 *  Navigate no Nwe -> Settings
 *  
 *
 * @param <T>
 * 
 */
@SuppressWarnings("serial")
@Deprecated
public class MembersEditor<T extends Content> extends ModelEditor<T>  {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MembersEditor.class.getName());
	
	private boolean updated = false;
	private DataSetMember member;
	
	private boolean leavevalues = false;
	private String determinant; 
	
	private boolean edition_mode_on  = false;   	// Si es Modo Edicion o Read
	private boolean readonly		 = false;		// Si el campo es es ReadOnly
	private boolean isEditable 		 = false;		// Si el Editor es editable 
	
	final boolean is_root 				= ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean role_admin 			= is_root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_dataset_members_read = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId()) || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	
	private IModel<ClassifierTemplate> templatemodel;
	private List<IModel<DataSetMember>> members = new ArrayList<IModel<DataSetMember>>();
	
	
	Boolean parentsEnabled = null;
	
	final int max_members_in_combo		= ServiceLocator.getService(SystemParameterService.class).getIntegerParameter("max_members_in_combo", 120);

	/** ----------------------------------------------------------------
	 */
	public class SelectorFragment extends Fragment {
		public SelectorFragment(String id) {
			super(id, "selector-fragment", MembersEditor.this);
			
			long totalmembers = getTemplate().getService(DataAccessService.class).getTotalMembers();
			
			//if (totalmembers>max_members_in_combo) {
			if (totalmembers>0) {
				add(new AutoCompleteFieldV5<DataSetMember>("member", new PropertyModel<DataSetMember>(MembersEditor.this, "member")) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						addMember(getValue());
						setSuggestion(null);
						setStringValue(null);
						fireScanAll(new EditorEvent(target, getClassifier()));
						target.focusComponent(getInput());
						target.add(MembersEditor.this);
						MembersEditor.this.onUpdate(target);
					}

					@Override
					public boolean isEnabledAdvancedOptions() {
						return true;
					}

					@Override
					public List<Suggestion> getSuggestions(String pattern, int maxResults, QuerySortOrder querySortOrder) {
						final HashMap<String, Object> parameters = new HashMap<>();
						parameters.put("maxResults",maxResults);
						parameters.put("querySortOrder",querySortOrder);
						return MembersEditor.this.getTemplate().getService(DataAccessService.class).getSuggestions(pattern, getContent(), parameters);
					}
 
					public List<Suggestion> getSuggestions(String pattern) {
						return MembersEditor.this.getTemplate().getService(DataAccessService.class).getSuggestions(pattern, getContent());
					}
					@Override 
					public boolean isVisible() {
						return isEditionEnabled() && !isReadOnly(); 
					}
					@Override 
					public String getHistoryKey() {
						return "classifcation-"+getClassifier().getId(); 
					}
					@Override 
					protected String serialize(IModel<DataSetMember> model) {
						return MembersEditor.this.serialize(model);
					}
					@Override 
					protected IModel<DataSetMember> deserialize(String token) {
						return MembersEditor.this.deserialize(token);
					}
				});
			}
			else {
				add(new ChoiceFieldWithHistory<DataSetMember>("member", 
						new PropertyModel<DataSetMember>(MembersEditor.this, "member"),
						new PropertyModel<List<DataSetMember>>(MembersEditor.this, "dataSetValues")) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						addMember(getValue());
						target.focusComponent(getInput());
						target.add(MembersEditor.this);
						MembersEditor.this.onUpdate(target);
						fireScanAll(new EditorEvent(target, getClassifier()));
						setValue(null);
					}
					@Override 
					public boolean isVisible() {
						return isEditionEnabled() && !isReadOnly(); 
					}
					@Override 
					public String getHistoryKey() {
						return "classifcation-"+getClassifier().getId(); 
					}
					@Override 
					protected String serialize(IModel<DataSetMember> model) {
						return MembersEditor.this.serialize(model);
					}
					@Override 
					protected IModel<DataSetMember> deserialize(String token) {
						return MembersEditor.this.deserialize(token);
					}
				});
			}
			
			IModel<String> relationlabelmodel = new Model<String>() {
				public String getObject() {
					StringResourceModel model = new StringResourceModel("relation.message", MembersEditor.this);
					model.setParameters(getClassifier().getName(), determinant);
					return model.getObject();
				}
			};
			
			add((new Label("relation-message", relationlabelmodel) {
				public boolean isVisible() {
 					return isReadOnly() && determinant!=null;
				}
			}).setEscapeModelStrings(false) );
			
			
			WebMarkupContainer calculationinfo = new WebMarkupContainer("calculation-info") {
				public boolean isVisible() {
 					return getTemplate().getSelectionScript()!=null;
				}
			};
			calculationinfo.add(new Label("calculated-message", getLabel("calculated.message")));
			Label scriptCode = new Label("script-code", getTemplate().getSelectionScript());
			scriptCode.setVisible(false);
			calculationinfo.setOutputMarkupId(true);
			calculationinfo.add(scriptCode);
			calculationinfo.add(new AjaxLink<Void>("script-link") {
				public void onClick(AjaxRequestTarget target) {
					scriptCode.setVisible(!scriptCode.isVisible());
					target.add(calculationinfo);
				}
			});
			add(calculationinfo);
			
			
			WebMarkupContainer leavevalues = new WebMarkupContainer("leavevalues-container") {
				@Override
				public boolean isVisible() {
					return isBatchClassification() && !isReadOnly();
				}
			};
			leavevalues.add(new AjaxCheckBox("check", new PropertyModel<Boolean>(MembersEditor.this, "leaveValues")) {
				protected void onUpdate(AjaxRequestTarget target) {
					if (getLeaveValues()) removeAllMembers();
					target.add(MembersEditor.this.get("container"));
				}
			});
			WebMarkupContainer checklabel = new WebMarkupContainer("label");
			checklabel.add(new AttributeModifier("for", new Model<String>() {
				public String getObject() {
					return leavevalues.get("check").getMarkupId();
				}
			}));
			leavevalues.add(checklabel);
			add(leavevalues);
			
			IModel<String> errorModel = new Model<String>() {
				public String getObject() {
					return getError();
				}
			};
			add((new Label("error-message", errorModel) {
				public boolean isVisible() {
 					return getError()!=null;
				}
			}).setEscapeModelStrings(false) );
			
			add(new AjaxLink<Void>("close-link") {
				public void onClick(AjaxRequestTarget target) {
					setEditionEnabled(false);
					target.add(MembersEditor.this);
				}
				@Override
				public boolean isVisible() {
					return false;
				}
			});
		}
		
		public void clearCache(AjaxRequestTarget target) {
			if (get("member") instanceof AutoCompleteFieldV5<?>) {
				((AutoCompleteFieldV5<?>)get("member")).clearCache(target);
			}
		}
	}
	
	/** ----------------------------------------------------------------
	 */
	public MembersEditor(String id, IModel<ClassifierTemplate> templatemodel, int base) {
		super(id);
		
		int index = base*10;
		
		setOutputMarkupId(true);
		setTemplate(templatemodel);
		WebMarkupContainer container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		container.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				String editioncss = (isEditionEnabled() ? " editing ":"") + (isEditable() ? " editable ": " readonly");
				String errorcss = MembersEditor.this.getError()!=null ? " errors " : "";
				return "toleft col-lg-12 col-md-12 col-xs-12 members-editor "+ editioncss + errorcss;
			}
		}));
		
		container.add(new AttributeModifier("tabindex", String.valueOf(index)));
		
		add(container);		
		container.add(new Label("classifier", getClassifier().getName()));
		
		Link<Void> link = new Link<Void>("open-dataset-values") {
			@Override
			public void onClick() {
				if (role_dataset_members_read) {
					String d_id = MembersEditor.this.getTemplate().getClassifier().getDataSet().getId().toString();
					PageParameters pa= new PageParameters();
					pa.add("id", d_id);
					setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-dataset-members-page", pa));
				}
			}
			public boolean isVisible() {
				
				if (isReadOnly()) 
					return false;
				
				if (!(  MembersEditor.this.getTemplate().getClassifier().getDataSet().getDataSetType()==DataSetType.ENTITY ||
						MembersEditor.this.getTemplate().getClassifier().getDataSet().getDataSetType()==DataSetType.STRING  ||
						MembersEditor.this.getTemplate().getClassifier().getDataSet().getDataSetType()==DataSetType.LABEL))
					return false;
					
				if (!role_dataset_members_read)
					return false;
				
				return isEditionEnabled();
			}
		};
		
		container.add(link);
	
		container.add(new WebMarkupContainer("mandatory") {
			public boolean isVisible() {
				return getTemplate().isMandatory() && !isReadOnly();
			}
		});
		
		WebMarkupContainer mk = new WebMarkupContainer("icon") {
			@Override
			public boolean isVisible() {
				return false;
			}
		};
		
		mk.add(new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				if (isEditionEnabled()) 
					return (MembersEditor.this.isReadOnly()?" readonly " : "");
				else 
					return "far fa-edit"  + (MembersEditor.this.isReadOnly()?" readonly " : "" );
			}
		}));
		
		container.add(mk);
		
		
		
		get("container:classifier").add(new AjaxEventBehavior("click") {
			@Override
			protected void onEvent(AjaxRequestTarget target) {
				
				
				if (!getEditor().isEditionEnabled())  
					return;
				
				setEditionEnabled(!isEditionEnabled());
				
				if (isEditionEnabled())	
					onEdit(target);
				try	{
					if (((Field<?>) MembersEditor.this.get("container:selector:member"))!=null && ((Field<?>) MembersEditor.this.get("container:selector:member")).getInput()!=null)
						((Field<?>) MembersEditor.this.get("container:selector:member")).getInput().add(new AttributeModifier("tabindex", String.valueOf(index+4)));
			
					target.focusComponent(((Field<?>) MembersEditor.this.get("container:selector:member")).getInput());
				} 
				catch (Exception e) {
					logger.error(e);
				}
				target.add(MembersEditor.this);
			}
		});
	}
	
	/** ----------------------------------------------------------------
	 */
	public void setFocus(AjaxRequestTarget target) {
		if (!getEditor().isEditionEnabled()) { 
			return;
		}
		setEditionEnabled(true);
		
		if (isEditionEnabled())	
			onEdit(target);
		
		((Field<?>)MembersEditor.this.get("container:selector:member")).onBeforeRender();
		
		target.focusComponent(((Field<?>) MembersEditor.this.get("container:selector:member")).getInput());
		target.add(MembersEditor.this);
	}
	
	@Override
	public void updateModel() {
		if (!this.updated || getLeaveValues()) 
			return;
		
		getEditor().getModelObject().setClassification(getClassifier(), getMembers());

		if (getMembers().size()==1 && parentsEnabled()) {
			DataSetMember member = getMembers().get(0);
			for (ModelElementTemplate template : getEditor().getModelObject().getContentTemplate().getStructure()) {
				if (template instanceof ClassifierTemplate) {
					ClassifierTemplate classifiertemplate = (ClassifierTemplate)template;
					if (!classifiertemplate.isVisible() && getClassifier().equals(classifiertemplate.getParent())) {
						List<DataSetMember> memberrelations = getMemberClassification(member, classifiertemplate.getClassifier().getDataSet());
						if (memberrelations.size()==1) {
							getEditor().getModelObject().setClassification(classifiertemplate.getClassifier(), memberrelations);
						}
					}
				}
			}
		}
		
		setUpdatedPart(getClassifier().getName().toLowerCase());
		this.updated = false;
	}
	
	public void update(T content) {
		if (!this.updated) 
			return;
		content.setClassification(getClassifier(), getMembers());
	}
	
	public boolean isUpdated() {
		return this.updated;
	}
	
	public void setAsDefault() {
		boolean first = true;
		StringBuffer buffer = new StringBuffer();
		for (IModel<DataSetMember> model : members) {
			if (!first) 
				buffer.append(";");
			buffer.append(model.getObject().getId());
			first = false;
		}
		getUser().getService(PreferencesService.class).setValue("default-"+ getEditor().getModelObject().getContentTemplate().getName(), getClassifier().getUniqueName(), buffer.toString());
	}
	
	public void clearDefault() {
		getUser().getService(PreferencesService.class).setValue("default-"+ getEditor().getModelObject().getContentTemplate().getName(), getClassifier().getUniqueName(), "");
	}
	
	public String getDefaultValuesStr() {
		return getUser().getService(PreferencesService.class).getValue("default-"+ getEditor().getModelObject().getContentTemplate().getName(), getClassifier().getUniqueName());
	}
	
	public void setTemplate(IModel<ClassifierTemplate> model) {
		this.templatemodel = model;
	}
	
	public ClassifierTemplate getTemplate() {
		return this.templatemodel.getObject();
	}
	
	/** ----------------------------------------------------------------
	 */
	public Classifier getClassifier() {
		return this.templatemodel.getObject().getClassifier();
	}

	/** ----------------------------------------------------------------
	 */
	public void setMember(DataSetMember member) {
		this.member = member;
	}
	
	
	public DataSetMember getMember() {
		return this.member;
	}

	/** ----------------------------------------------------------------
	 */
	public List<DataSetMember> getMembers() {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (IModel<DataSetMember> model : this.members) {
			members.add(model.getObject());
		}
		return members;
	}

	/** ----------------------------------------------------------------
	 */
	public List<IModel<DataSetMember>> getMembersModels() {
		return members;
	}
	
	/** ----------------------------------------------------------------
	 */
	public T getContent() {
		T content = getEditor().getModelObject();
		getEditor().update(content);
		return content;
	}
	
	/** ----------------------------------------------------------------
	 */
	public List<DataSetMember> getDataSetValues() {
		return getTemplate().getService(DataAccessService.class).getAll(getContent());
	}

	/** ----------------------------------------------------------------
	 */
	public List<DataSetMember> getValues() {
		return null;
	}
	
	/** ----------------------------------------------------------------
	 */
	public List<Classification> getClassification() {
		return null;
	}

	/** ----------------------------------------------------------------
	 */
	public void setEditionEnabled(boolean value) {
		edition_mode_on = value;
	}

	/** ----------------------------------------------------------------
	 */
	@Override
	public void cancel() {
		setMembers(getClassifier());
		updated = false;
	}
	
	/** ----------------------------------------------------------------
	 */
	public boolean isReadOnly() {
		return readonly;
	}
	
	/** ----------------------------------------------------------------
	 */
	public void setIsReadOnly(boolean b) {
		this.readonly=b;
	}
	
	/** ----------------------------------------------------------------
	 */
	public boolean isBatchClassification() {
		return false;
	}
	
	/** ----------------------------------------------------------------
	 */
	public void setLeaveValues(boolean value) {
		this.leavevalues = value;
	}
	
	/** ----------------------------------------------------------------
	 */
	public boolean getLeaveValues() {
		return leavevalues;
	}
	
	/** ----------------------------------------------------------------
	 */
	@Override
	public void onBeforeRender() {
		if (getMembersModels().isEmpty() && !updated && !getLeaveValues()) {
			setMembers(getClassifier());
		}
		
		super.onBeforeRender();
		
		if (get("container:elements-container")==null) {
			checkRelations();
			addMembersView();
		}
		
		if (getTemplate().getSelectionScript()!=null) {
			this.readonly = true;
		}
		
		if (parentsEnabled() && getTemplate().getParent()!=null) {
			if (!getClassification(((Classifier)getTemplate().getParent()).getDataSet()).isEmpty()) {
				this.determinant = getTemplate().getParent().getName();
				ClassifierTemplate template = getParentReverseTemplate();
				if (template!=null && (template.getMultiplicity().equals(Multiplicity.M01) || template.getMultiplicity().equals(Multiplicity.M11))) {
					this.readonly = true;
				}	
			}
		}
	}
	
	@Override
	public void onAfterRender() {
		super.onAfterRender();
		getFeedbackMessages().clear();
	}

	
	/** ----------------------------------------------------------------
	 */
	@Override
	public void onDetach() {
		this.templatemodel.detach();
		this.member = null;
		for (IModel<DataSetMember> model : this.members) 
			model.detach();
		super.onDetach();
	}
	
	public void setUpdated(boolean value) {
		this.updated = value;
	}
	
	/** ----------------------------------------------------------------
	 */
	protected void onEdit(AjaxRequestTarget target) {
	}

	/** ----------------------------------------------------------------
	 */
	protected void onUpdate(AjaxRequestTarget target) {
	}
	
	@SuppressWarnings("unchecked")
	public void onUpdate(AjaxRequestTarget target, ModelElement element) {
		if (parentsEnabled()) {
			
			if (AccessStrategy.Script.equals(getTemplate().getAccessibility()) &&
				getTemplate().getSelectionScript()!=null) {
				Content content = getModelObject();
				getEditor().update((T)content);
				try {
					this.determinant = null;
					this.members.clear();
					DataSetMember value = null;
					Object evaluation = (new KbeeCodeExecutor()).execute(getTemplate().getSelectionScript(), content);
					if (evaluation!=null) {
						value = getContentDao().findMemberByValue(getTemplate().getClassifier().getDataSet(), evaluation.toString());
					}
					if (value!=null) {
						this.updated = true;
						addMember(value);
					}
				}
				catch (Exception e) {
					logger.error(e);
				}
				target.add(this);
				((SelectorFragment)get("container:selector")).clearCache(target);
				if (this.updated) {
					onUpdate(target);
				}
				return;
			}
			
			if (!(element instanceof Classifier) || 
				getTemplate().getParent()==null || 
				!getTemplate().getParent().equals((Classifier)element)) {
				return;
			}	
			
			this.updated = true;
			this.readonly = false;
			this.determinant = null;
			
			this.members.clear();
			
			for (DataSetMember member : getClassification((Classifier)element)) {
				List<DataSetMember> memberrelations = getMemberClassification(member, getClassifier().getDataSet());
				// si la multiplidad del clasificador parent es 1 y la propia es 1s ellena 
				if (memberrelations.size()==1 || isInherited(getClassifier())) {
					for (DataSetMember related : memberrelations) {
						addMember(related);
						this.determinant = element.getName();
						this.readonly = true;
					}
				}
			}
			
			target.add(this);
		}
		else {
			if (!(element instanceof Classifier) ||
				!getClassifier().getDataSet().isAFunctionOf(((Classifier)element).getDataSet()))
				return;
			
			this.updated = true;
			this.readonly = false;
			this.determinant = null;
			
			this.members.clear();
			
			for (DataSetMember member : getClassification(((Classifier)element).getDataSet())) {
				List<DataSetMember> memberrelations = getMemberClassification(member, getClassifier().getDataSet());
				if (memberrelations.size()==1 || isInherited(getClassifier())) {
					for (DataSetMember related : memberrelations) {
						addMember(related);
						this.determinant = element.getName();
						this.readonly = true;
					}
				}
			}
		}
		
		target.add(this);
		
		((SelectorFragment)get("container:selector")).clearCache(target);
		
		onUpdate(target);
	}
	
	/** ----------------------------------------------------------------
	 */
	protected List<DataSetMember> getClassification(DataSet dataset) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (DataSetMember member : getValues()) {
			if (member.getDataSet().equals(dataset)) {
				members.add(member);
			}
		}
		return members;
	}
	
	/** ----------------------------------------------------------------
	 */
	protected List<DataSetMember> getClassification(Classifier classifier) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		return members;
	}
	
	/** ----------------------------------------------------------------
	 */
	protected DataSetMember getParentValue(IModel<DataSetMember> member) {
		if (getTemplate()!=null && getTemplate().getParent()!=null) {
			for (Classification classification : getClassification()) {
				if (classification.getClassifier().equals(getTemplate().getParent())) {
					DataSetMember parent = classification.getDataSetMember();
					return parent;
				}
			}
		}
		return null;
	}

	
	/** ----------------------------------------------------------------
	 */
	protected List<DataSetMember> getMemberClassification(DataSetMember member, DataSet dataset) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (Classification classification : member.getClassification()) {
			if (classification.getClassifier().getDataSet().equals(dataset))
				members.add(classification.getDataSetMember());
		}
		return members;
	}
	
	/** ----------------------------------------------------------------
	 */
	protected boolean isInherited(Classifier classifier) {
		for (ClassifierTemplate template : getModelObject().getContentTemplate().getClassifiers()) {
			if (template.getClassifier().equals(classifier)) {
				return template.isInherited();
			}
		}
		return false;
	}

	/** ----------------------------------------------------------------
	 */
	public boolean isEditionEnabled() {
 		return edition_mode_on; 
	}
	
	/** ----------------------------------------------------------------
	 */
	public void setIsEditable(boolean b) {
		this.isEditable=b;
	}
	
	
	/** ----------------------------------------------------------------
	 */
	public boolean isEditable() {
		return this.isEditable;
	}
	
	/** ----------------------------------------------------------------
	 */
	protected void addMembersView() {
		
		WebMarkupContainer elementscont = new WebMarkupContainer("elements-container");
		
		elementscont.add( new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				if (isEditable())
					return "elements-container editable";
				else
					return "elements-container readonly";
			}}) {
		}); 
		
		((WebMarkupContainer) get("container")).add(elementscont);
									
		ListView<IModel<DataSetMember>> mems = new ListView<IModel<DataSetMember>>("member", new PropertyModel<List<IModel<DataSetMember>>>(MembersEditor.this, "membersModels")) {
			public void populateItem(final ListItem<IModel<DataSetMember>> item) {
				
				if (item.getModelObject()==null) {
					
					logger.error("item.getModelObject() is null");
					
					item.add(new Label("member-name", "<no data>"));
					item.add (new AjaxLink<Void>("remove-link") {
						public void onClick(AjaxRequestTarget target) {
						}
						public boolean isVisible() {
							return false;
						}
					});
					item.add(new WebMarkupContainer("separator") {
						public boolean isVisible() {
							return false;
						}
					});
					return;
				}

				DataSetMember member = item.getModelObject().getObject();
				
				
				item.add(new Label("member-name", member.getDisplayName()!=null?member.getDisplayName():"--"));
				item.add (new AjaxLink<Void>("remove-link") {
					public void onClick(AjaxRequestTarget target) {
						removeMember(item.getModelObject());
						fireScanAll(new EditorEvent(target, getClassifier()));
						target.add(MembersEditor.this);
						MembersEditor.this.onUpdate(target);
					}
					public boolean isVisible() {
						return isEditionEnabled() && !isReadOnly();
					}
				});
				
				item.add(new WebMarkupContainer("separator") {
					public boolean isVisible() {
						return !isEditionEnabled() && 
						getMembers().size()>1 && item.getIndex()<getMembers().size()-1;
					}
				});
			
			}
		};
		elementscont.add(mems);
		
		((WebMarkupContainer)get("container:elements-container")).add(new WebMarkupContainer("nullmember") {
			public boolean isVisible() {
				return isBatchClassification() && getMembersModels().isEmpty() && !getLeaveValues(); 
			}
		});
		
		((WebMarkupContainer)get("container:elements-container")).add(new WebMarkupContainer("leavevalues-message") {
			public boolean isVisible() {
				return getLeaveValues() && !isReadOnly();
			}
		});
		
		((WebMarkupContainer)get("container")).add(new SelectorFragment("selector") {
			public boolean isVisible() {
				return isEditionEnabled();
			}
		});
	}

	/** ----------------------------------------------------------------
	 */
	protected boolean addMember(DataSetMember member) {
		if (getIndex(member)>=0 || member==null)
			return false;
		
		IModel<DataSetMember> model = new ObjectModel<DataSetMember>(member);
		
		if (getTemplate().getMultiplicity().equals(Multiplicity.M1N) || getTemplate().getMultiplicity().equals(Multiplicity.M0N) || members.isEmpty()) {
			this.members.add(model);
		}
		else {
			if (members.isEmpty())
				members.add(model);
			else
				members.set(0, model);
		}
		
		this.updated = true;
		
		setLeaveValues(false);
		
		return true;
	}

	/** ----------------------------------------------------------------
	 */
	protected void removeMember(IModel<DataSetMember> model) {
		int index = getIndex(model);
		this.updated = true;
		this.members.remove(index);
	}
	
	/** ----------------------------------------------------------------
	 */
	protected void removeAllMembers() {
		this.updated = true;
		this.members.clear();
	}

	/** ----------------------------------------------------------------------
	 * @param classifier
	 */
	protected void setMembers(Classifier classifier) {
		
	Assert.isInstanceOf(Classificable.class, getEditor().getModelObject());
		
		this.members = new ArrayList<IModel<DataSetMember>>();
		
		for (Classification classification : ((Classificable)getEditor().getModelObject()).getClassification()) {
			if (classification!=null && classification.getClassifier().equals(classifier)) {
				this.members.add(new ObjectModel<DataSetMember>(classification.getDataSetMember()));
			}
		}
	}

	/** ----------------------------------------------------------------
	 */
	protected void checkRelations() {
		if (!parentsEnabled()) {
			for (Classification classification : ((Classificable)getEditor().getModelObject()).getClassification()) {
				if (getClassifier().getDataSet().isAFunctionOf(classification.getClassifier().getDataSet())) {
					List<DataSetMember> memberrelations = getMemberClassification(classification.getDataSetMember(), getClassifier().getDataSet());
					if (memberrelations.size()==1 || isInherited(classification.getClassifier())) {
						for (DataSetMember related : memberrelations) {
							addMember(related);
							determinant = classification.getClassifier().getName();
							readonly = true;
						}	
					}
				}
			}
		}
	}
	
	/** ----------------------------------------------------------------
	 */
	protected String serialize(IModel<DataSetMember> model) {
		String classname = model.getObject().getClass().getName();
		int i = classname.indexOf("_");
		if (i>0) classname = classname.substring(0, i);
		i = classname.indexOf("$");
		if (i>0) classname = classname.substring(0, i);
		String serialized = classname+"-"+((Identifiable)model.getObject()).getId();
		DataSetMember parent = MembersEditor.this.getParentValue(model);
		if (parent!=null) {
			serialized += "-" + String.valueOf(parent.getId());
		}
		return serialized;
	}
	
	/** ----------------------------------------------------------------
	 */
	protected IModel<DataSetMember> deserialize(String token) {
		int i0 = token.indexOf("-");
		if (i0<=0) return null;
		String classname = token.substring(0, i0);
		int i1 = token.indexOf("-",i0+1);
		String id;
		IModel<DataSetMember> model = null;
		if (i1>0) {
			id = token.substring(i0+1, i1);
			model = getModel(classname, id);
			if (model!=null) {
				String parentId = token.substring(i1+1);
				DataSetMember parentValue = getParentValue(model);
				if (parentValue==null || !String.valueOf(parentValue.getId()).equals(parentId)) {
					return null;
				}
			}
		}
		else {
			id = token.substring(i0+1);
			model = getModel(classname, id);
			if (getParentValue(model)!=null) {
				return null;
			}
		}
		return model;
	}
	
	/** ----------------------------------------------------------------
	 */
	protected ClassifierTemplate getParentReverseTemplate() {
		if (parentsEnabled() && getTemplate().getParent()!=null) {
			DataSet parentSet = ((Classifier)getTemplate().getParent()).getDataSet();
			for (ModelElementTemplate template : parentSet.getStructure()) {
				if (template!=null && template.getElement()!=null && template.getElement() instanceof Classifier) {
					if (((Classifier)template.getElement()).equals(getClassifier())) {
						return (ClassifierTemplate)template;
					}
				}
			}
		}	
		return null;
	}
	
	protected IModel<DataSetMember> getModel(String classname, String id) {
		ObjectModel<DataSetMember> model = null;
		try {
			Class<?> clazz = Class.forName(classname);
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			Object object = (DataSetMember)sf.getCurrentSession().get(clazz, Long.valueOf(id));
			if (object!=null) {
				model = new ObjectModel<DataSetMember>(clazz, Long.valueOf(id));
				model.getObject();
			}
		}
		catch (Exception e) {
			model = null;
		}
		return model;
	}
	
	private KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	private int getIndex(IModel<DataSetMember> model) {
		return getIndex(model.getObject());
	}
	
	private int getIndex(DataSetMember member) {
		int index = 0;
		boolean found = false;
		for (IModel<DataSetMember> model : this.members) {
			if (member.equals(model.getObject())) {
				found = true;
				break;
			}
			else
				index++;
		}
		return found ? index : -1; 
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
	
	private boolean parentsEnabled() {

		return true;
		
		//if (parentsEnabled == null) {
		//	String value = ServiceLocator.getService(SystemParameterService.class).getParameter("com.novamens.content.contentclass.parentsenabled", "false");
		//	parentsEnabled = "true".equals(value);
		//}
		//return parentsEnabled;
	}
}
