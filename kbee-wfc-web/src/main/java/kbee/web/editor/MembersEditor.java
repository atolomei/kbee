package kbee.web.editor;

import java.util.*;

import com.novamens.indexer.query.QuerySortOrder;
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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.springframework.util.Assert;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.SuggestionService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.event.wicket.EditorEvent;
import kbee.web.form.AutoCompleteFieldV5;

@SuppressWarnings("serial")
public class MembersEditor<T extends Classificable> extends ObjectEditorPanel<T>  {
			
	private static final long serialVersionUID = 1L;
	
	static Logger logger = LogManager.getLogger(MembersEditor.class.getName());
	
	private boolean updated = false;
	private DataSetMember member;
	private boolean readonly = false;// Si el campo es es ReadOnly
	private IModel<ClassifierTemplate> templatemodel;
	private Disposition disposition;
	private List<IModel<DataSetMember>> members = new ArrayList<IModel<DataSetMember>>();
	
	public class MembersFragment extends Fragment {
		public MembersFragment(String id) {
			super(id, "members-fragment", MembersEditor.this);
			
			ListView<IModel<DataSetMember>> membersview = new ListView<IModel<DataSetMember>>("members", new PropertyModel<List<IModel<DataSetMember>>>(MembersEditor.this, "membersModels")) {
				public void populateItem(final ListItem<IModel<DataSetMember>> item) {

					DataSetMember member = item.getModelObject().getObject();

					final String label = (member != null && member.getDisplayName() != null) ? member.getDisplayName() : "--";
					item.add(new Label("member", label));
					
					item.add(new AjaxLink<Void>("remove-link") {
						@Override
						public void onClick(AjaxRequestTarget target) {
							removeMember(item.getModelObject());
							target.add(MembersEditor.this);
							MembersEditor.this.onUpdate(target);
						}
						@Override
						public boolean isVisible() {
							if (isReadOnly() || getTemplate().isReadOnly())
								return false;
							return getEditor().isEditionEnabled(); 
						}
					});
					
					item.detach();
				}
			};
			
			add(membersview);
			
			WebMarkupContainer selectorpanel = new WebMarkupContainer("selector-panel") {
				@Override
				public boolean isVisible() {
					if (isReadOnly() || getTemplate().isReadOnly())
						return false;
					return getEditor().isEditionEnabled(); 
				}
			};
			
			AutoCompleteFieldV5<DataSetMember> selector = new AutoCompleteFieldV5<DataSetMember>("member", new PropertyModel<DataSetMember>(MembersEditor.this, "member")) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					addMember(getValue());
					setStringValue(null);
					setSuggestion(null);
					fire(new EditorEvent(target));
					target.focusComponent(getInput());
					target.add(MembersEditor.this);
					MembersEditor.this.onUpdate(target);
				}	
				@Override
				public Disposition getDisposition() {
					return Disposition.VERTICAL;
				}
				@Override 
				public boolean isVisible() {
//					if (isReadOnly() || getTemplate().isReadOnly())
//						return false;
					return getEditor().isEditionEnabled(); 
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
					return getClassifier().getService(SuggestionService.class).getSuggestions(pattern, parameters);
				}

				public List<Suggestion> getSuggestions(String pattern) {
					return getClassifier().getService(SuggestionService.class).getSuggestions(pattern); 
				}
				@Override 
				public String getHistoryKey() {
					return "classifcation-"+getClassifier().getUniqueName().toLowerCase(); 
				}
			};
			
			selectorpanel.add(selector);
			
			add(selectorpanel);
			
			add(new WebMarkupContainer("empty-panel") {
				public boolean isVisible() {
	 				return getEditor()!=null && (!getEditor().isEditionEnabled()||isReadOnly()) && getMembers().isEmpty();
				}
			});
		}
	}	
	
	@Deprecated
	public MembersEditor(String id, IModel<ClassifierTemplate> templatemodel) {
		this(id, templatemodel, false);
	}
	
	@Deprecated
	public MembersEditor(String id, IModel<ClassifierTemplate> templatemodel, boolean isReadOnly) {
		super(id);
		
		setOutputMarkupId(true);
		setReadOnly(isReadOnly);
		
		setTemplate(templatemodel);
		
		Label label = new Label("classifier", getClassifier().getName());
		
		label.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getDisposition()==null||getDisposition()==Disposition.HORIZONTAL ? "col-lg-2 control-label" : "control-label";
			}
		}));
		
		add(label);
	}
	
	@Override
	public void updateModel() {
		
		if (!updated) 
			return;
		
		if (logger.isDebugEnabled()) {
			logger.debug(getClassifier().getDisplayName() + "| " );
			getMembers().forEach(item->logger.debug(item.getDisplayName()));
		}
			
		if (getMembers().isEmpty())
			getEditor().getModelObject().removeAllClassification(getClassifier());
		else
			getEditor().getModelObject().setClassification(getClassifier(), getMembers());
		
		setUpdatedPart(getClassifier().getName().toLowerCase());
		updated = false;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}
	
	public void setTemplate(IModel<ClassifierTemplate> model) {
		this.templatemodel = model;
	}
	
	public ClassifierTemplate getTemplate() {
		return this.templatemodel.getObject();
	}
	
	public Classifier getClassifier() {
		return this.templatemodel.getObject().getClassifier();
	}
	
	public void setMember(DataSetMember member) {
		this.member = member;
	}
	
	public DataSetMember getMember() {
		return this.member;
	}

	public List<DataSetMember> getMembers() {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (IModel<DataSetMember> model : this.members) {
			members.add(model.getObject());
		}
		return members;
	}
	
	public List<IModel<DataSetMember>> getMembersModels() {
		return members;
	}
	
	@Override
	public void cancel() {
		setMembers(getClassifier());
		updated = false;
	}
	
	public Disposition getDisposition() {
		if (this.disposition==null) {
			if (getEditor()!=null) {
				if (getEditor().getForm()!=null && getEditor().getForm() instanceof Form) {
					this.disposition = ((Form<?>)getEditor().getForm()).getDisposition();
				}
			}
		}
		return this.disposition;
	}
	
	public boolean isReadOnly() {
		return readonly;
	}
	
	public void onUpdate(AjaxRequestTarget target) {
		
	}
	
	public List<DataSetMember> getClassification(Classifier classifier) {
		return null;
	}
	
	public void onUpdate(AjaxRequestTarget target, ModelElement element) {
		if (!(element instanceof Classifier) || 
			getTemplate().getParent()==null || 
			!getTemplate().getParent().equals((Classifier)element)) {
			return;
		}	
			
		this.updated = true;
		this.readonly = false;
			
		this.members.clear();
			
		Set<DataSetMember> memberrelations = new HashSet<DataSetMember>();
		
		for (DataSetMember member : getClassification((Classifier)element)) {
			memberrelations.addAll(getMemberClassification(member, getClassifier().getDataSet()));
		}
		
		if (memberrelations.size()==1) {
			for (DataSetMember related : memberrelations) {
				addMember(related);
				this.readonly = true;
			}
		}
			
		target.add(this);
	} 
	
	@Override
	public void onBeforeRender() {
		if (getMembersModels().isEmpty() && !updated) {
			setMembers(getClassifier());
		}
		super.onBeforeRender();
		if (get("horizontal-layout")==null) {
			WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
			layout.add(new MembersFragment("members"));
			add(layout);
			add(new MembersFragment("members"));
			if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
				get("members").setVisible(false);
			}
			else {
				layout.setVisible(false);
			}
		}
		
		if (getTemplate().getParent()!=null) {
			if (!getClassification(((Classifier)getTemplate().getParent())).isEmpty()) {
				ClassifierTemplate template = getParentReverseTemplate();
				if (template!=null && (template.getMultiplicity().equals(Multiplicity.M01) || template.getMultiplicity().equals(Multiplicity.M11))) {
					this.readonly = true;
				}	
			}
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (member!=null) 
			member=null;
		templatemodel.detach();
		members.forEach(item -> item.detach());
	}
	
	protected boolean editionEnabled() {
		return true;
	}

	protected boolean addMember(DataSetMember member) {
		
		if (member==null)
			return false;
		
		if (getIndex(member)>=0)
			return false;
		
		IModel<DataSetMember> model = new ObjectModel<DataSetMember>(member);
		
		if (getTemplate().getMultiplicity().equals(Multiplicity.M11) || 
				getTemplate().getMultiplicity().equals(Multiplicity.M01)) { 
			if (members.isEmpty())
				members.add(model);
			else
				members.set(0, model);
		}
		else if (getTemplate().getMultiplicity().equals(Multiplicity.M1N) || getTemplate().getMultiplicity().equals(Multiplicity.M0N)) 
			members.add(model);
		else {
			logger.error(" {} | {} | {} | {}", getSessionUser().getUserName(), "MembersEditor", Thread.currentThread().getStackTrace()[1].getMethodName(), "Multiplicity not supported");
		}
		
		updated = true;
		
		return true;
	}
	
	protected List<DataSetMember> getMemberClassification(DataSetMember member, DataSet dataset) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (Classification classification : member.getClassification()) {
			if (classification.getClassifier().getDataSet().equals(dataset))
				members.add(classification.getDataSetMember());
		}
		return members;
	}
	
//	protected List<DataSetMember> getClassification(DataSet dataset) {
//		List<DataSetMember> members = new ArrayList<DataSetMember>();
//		for (DataSetMember member : getMembers()) {
//			if (member.getDataSet().equals(dataset)) {
//				members.add(member);
//			}
//		}
//		return members;
//	}
	
	protected ClassifierTemplate getParentReverseTemplate() {
		if (getTemplate().getParent()!=null) {
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

	private void removeMember(IModel<DataSetMember> model) {

		int index = getIndex(model);
		updated = true;
		members.remove(index);
		
	}
	
	private void setMembers(Classifier classifier) {
		Assert.isInstanceOf(Classificable.class, getEditor().getModelObject());

		this.members = new ArrayList<IModel<DataSetMember>>();
		for (Classification classification : ((Classificable)getEditor().getModelObject()).getClassification()) {
			if (classification!=null && classification.getClassifier().equals(classifier)) {
				this.members.add(new ObjectModel<DataSetMember>(classification.getDataSetMember()));
			}
		}
	}
	
	private int getIndex(IModel<DataSetMember> model) {
		return getIndex(model.getObject());
	}
	
	private int getIndex(DataSetMember member) {
		int index = 0;
		boolean found = false;
		for (IModel<DataSetMember> model : members) {
			if (member.equals(model.getObject())) {
				found = true;
				break;
			}
			else
				index++;
		}
		return found ? index : -1; 
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
