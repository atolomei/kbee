package 	kbee.web.dataset;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.web.editor.markup.ObjectEditorPanel;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.wicket.model.ObjectModel;

/**
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
@Deprecated
public class MembersPanel<T> extends ObjectEditorPanel<T> {
	private static final long serialVersionUID = 1L;
	private boolean updated = false;
	private Suggestion suggestion;
	private int numMenbers = 0;
	private IModel<Classifier> classifiermodel;
	private List<IModel<DataSetMember>> members = new ArrayList<IModel<DataSetMember>>();
	
	public MembersPanel(String id, Editor<T> editor, IModel<Classifier> classifiermodel) {
		super(id);
		
		setOutputMarkupId(true);
		
		//setEditor(editor);
		setClassifier(classifiermodel);
		setMembers(getClassifier());
		
		add(new Label("classifier", getClassifier().getName()));
		
		ListView<IModel<DataSetMember>> membersview = new ListView<IModel<DataSetMember>>("membersview", members) {
			public void populateItem(final ListItem<IModel<DataSetMember>> item) {
				DataSetMember member = item.getModelObject().getObject();
				Label strmember = new Label("member", member.getDisplayName()!=null?member.getDisplayName():"--"); 
				item.add(strmember);
				
				if (getEditor2().isEditionEnabled())
					item.add(new AttributeModifier("class", "item editmode"));
				
				WebMarkupContainer removeLink = new WebMarkupContainer("remove-link2") {
					@Override
					public boolean isVisible() {
						return getEditor2().isEditionEnabled();
					}
				};
				
				removeLink.add(new AjaxEventBehavior("onclick") {
					public void onEvent(AjaxRequestTarget target) {
						removeMember(item.getModelObject());
						target.add(MembersPanel.this);
					}
				});
				
				item.add(removeLink);

				item.detach();
			}
		};
		
		add(membersview);
		
		AutoCompleteSettings settings = new AutoCompleteSettings();
		settings.setThrottleDelay(250);  
		settings.setAdjustInputWidth(false);
		settings.setShowListOnEmptyInput(false);
		settings.setShowListOnFocusGain(false);
		settings.setMinInputLength(-1);
		settings.setCssClassName("suggestions");
		
//		final AutoCompleteTextField memberfield = new AutoCompleteTextField("member", new PropertyModel<Suggestion>(this, "suggestion"), new SuggestionRender(), settings) {
//			@Override
//			public void onChange(AjaxRequestTarget target, Suggestion suggestion) {
//				if (suggestion!=null && suggestion.getObject() instanceof DataSetMember) {
//					addMember((DataSetMember)suggestion.getObject());
//					target.add(MembersPanel.this);
//					setSuggestion(null);
//				}
////				else {
////					suggestionvalue = suggestion!=null ? suggestion.getText() : null;
////				}
//			};
//			@Override
//			public boolean isVisible() {
//				//return true;
//				return getEditor().isEditionEnabled();
//			}
//			@Override
//			public List<Suggestion> getSuggestions(String pattern) {
//				//Map<String, Object> parameters = new HashMap<String, Object>();
//				//if (getTemplate().getRoot()!=null)
//				//	parameters.put("root", getTemplate().getRoot());
//				return getClassifier().getService(SuggestionService.class).getSuggestions(pattern); 
//			}
//		};
//		memberfield.setMarkupId("member"+getClassifier().getId());
//		
//		add(memberfield);
//		
//		WebMarkupContainer searcher = new WebMarkupContainer("searcher"){
//			@Override
//			public boolean isVisible() {
//				//return true;
//				return  getEditor().isEditionEnabled();
//			}
//		};
//		
//		searcher.add(new AttributeModifier("onclick", "top."+memberfield.getMarkupId()+".show();"));
		//add(searcher);
	}
	
	@Override
	public boolean isVisible() {
		//return true;
		return getEditor2().isEditionEnabled() || !getMembers().isEmpty();
	}
	
	@Override
	public void updateModel() {
		if (!updated)
			return;
		
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (IModel<DataSetMember> model : this.members) {
			members.add(model.getObject());
		}
		
		((Classificable)getEditor2().getModelObject()).setClassification(getClassifier(), members);
		
		setUpdatedPart("relations");
		
		updated = false;
	}

	public Suggestion getSuggestion() {
		return suggestion;
	}
	
	public void setSuggestion(Suggestion suggestion) {
		this.suggestion = suggestion;
	}
	
	public void setClassifier(IModel<Classifier> model) {
		this.classifiermodel = model;
	}
	
	public Classifier getClassifier() {
		return classifiermodel.getObject();
	}
	
	public int numMembers() {
		return numMenbers;
	}
	
	@Override
	public void onDetach() {
		suggestion = null;
		classifiermodel.detach();
		for (IModel<DataSetMember> model : members) 
			model.detach();
		super.onDetach();
	}

//	private void addMember(DataSetMember member) {
//		for (IModel<DataSetMember> m : members) {
//			if (m.getObject().getId().equals(member.getId()))
//				return;
//		}
//		
//		IModel<DataSetMember> model = new ObjectModel<DataSetMember>(member);
//		
//		if (getClassifier().getMultiplicity().equals(Multiplicity.M0N) || getClassifier().getMultiplicity().equals(Multiplicity.M1N) || members.isEmpty()) {
//			members.add(model);
//			numMenbers++;
//		}
//		else {
//			members.set(0, model);
//		}
//		
//		updated = true;
//	}
	
	private void removeMember(IModel<DataSetMember> model) {
		updated = true;
		numMenbers--;
		members.remove(model);
	}
	
	private List<IModel<DataSetMember>> getMembers() {
		return members;
	}
	
	private void setMembers(Classifier classifier) {
		numMenbers = 0;
		members = new ArrayList<IModel<DataSetMember>>();
		
		for (Classification classification : ((Classificable)getEditor2().getModelObject()).getClassification()) {
			if (classification.getClassifier().equals(classifier)) {
				numMenbers++;
				members.add(new ObjectModel<DataSetMember>(classification.getDataSetMember()));
			}
		}
	}
}
