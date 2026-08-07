package com.novamens.content.web.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.content.web.editor.markup.DeprecatedObjectEditor;
import com.novamens.content.web.editor.markup.ObjectEditorPanel;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.Principal;
import com.novamens.security.acl.Group;
import com.novamens.wicket.model.ObjectModel;

@SuppressWarnings("serial")
@Deprecated
public class GroupsPanel extends ObjectEditorPanel<Group> {
	private static final long serialVersionUID = 1L;
	
	private boolean updated = false;
	private Suggestion suggestion;
	//private int multiplicity = 1;
	private List<IModel<Group>> groups = new ArrayList<IModel<Group>>();
	
	public GroupsPanel(DeprecatedObjectEditor<Group> editor) {
		super("groups-editor");
		
		setOutputMarkupId(true);
		setEditor2(editor);
		setGroups(getModelObject2());
		
		ListView<IModel<Group>> groupsview = new ListView<IModel<Group>>("groupsview", groups) {
			public void populateItem(final ListItem<IModel<Group>> item) {
				Group group = item.getModelObject().getObject();
				
				Label name = new Label("name", group.getName()); 
				item.add(name);
				
				if (getEditor2().isEditionEnabled())
					item.add(new AttributeModifier("class", "item editmode"));
				
				item.add(new AjaxLink<Void>("remove-link") {
					public void onClick(AjaxRequestTarget target) {
						removeGroup(item.getModelObject().getObject());
						target.add(GroupsPanel.this);
					}
					@Override
					public boolean isVisible() {
						return getEditor2().isEditionEnabled();
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
				});
				
				item.getModelObject().detach();
			}
		};
		
		add(groupsview);
		
		AutoCompleteSettings settings = new AutoCompleteSettings();
		settings.setThrottleDelay(250);  
		settings.setAdjustInputWidth(false);
		settings.setShowListOnEmptyInput(false);
		settings.setShowListOnFocusGain(false);
		settings.setMinInputLength(-1);
		settings.setCssClassName("suggestions");
		
//		AutoCompleteTextField groupfield = new AutoCompleteTextField("group", new PropertyModel<Suggestion>(this, "suggestion"), new SuggestionRender(), settings) {
//			@SuppressWarnings("unchecked")
//			@Override
//			public void onChange(AjaxRequestTarget target, Suggestion suggestion) {
//				if (suggestion!=null && suggestion.getObject() instanceof ObjectModel) {
//					addGroup(((ObjectModel<Group>)suggestion.getObject()).getObject());
//					target.add(GroupsPanel.this);
//					setSuggestion(null);
//				}
//			};
//			//@Override
//			public boolean isVisible() {
//				return getEditor().isEditionEnabled();
//			}
//			@Override
//			public List<Suggestion> getSuggestions(String pattern) {
//				return ServiceLocator.getService(GroupSuggestionService.class).getSuggestions(pattern); 
//			}
//		};
//		
//		add(groupfield);
		
		WebMarkupContainer searcher = new WebMarkupContainer("searcher"){
			@Override
			public boolean isVisible() {
				return  getEditor2().isEditionEnabled();
			}
		};
		
		//searcher.add(new AttributeModifier("onclick", "top."+groupfield.getMarkupId()+".show();"));
		add(searcher);
	}
	
	@Override
	public void updateModel() {
		if (!updated)	
			return;
		
		for (Group group : getGroups()) {
			if (cycle(group, getGroup())) {
				//getEditor2().error("Cycle");
				return;
			}
		}
		
		getGroup().setGroups(getGroups());
		
		setUpdatedPart("groups");
		
		updated = false;
	}

	public Suggestion getSuggestion() {
		return suggestion;
	}
	
	public void setSuggestion(Suggestion suggestion) {
		this.suggestion = suggestion;
	}

	public void resetValue() {
		setGroups(getModelObject2());
	}
	
	public Group getGroup() {
		return getModelObject2();
	}
	
	public Set<Group> getGroups() {
		Set<Group> groups = new HashSet<Group>();
		for (IModel<Group> model : this.groups) {
			groups.add(model.getObject());
		}
		return groups;
	}
	
	@Override
	public boolean isVisible() {
		return getEditor2().isEditionEnabled() || !groups.isEmpty();
	}

	@Override
	public void onDetach() {
		for (IModel<Group> model : groups) {
			model.detach();
		}
		super.onDetach();
	}

//	private void addGroup(Group group) {
//		for (IModel<Group> model : groups) {
//			if (model.getObject().equals(group))
//				return;
//		}
//		if (multiplicity==1 && !groups.isEmpty())
//			groups.clear();
//		groups.add(new ObjectModel<Group>(group));
//		updated = true;
//	}
	
	private void removeGroup(Group group) {
		updated = true;
		for (IModel<Group> model : groups) {
			if (model.getObject().equals(group)) {
				groups.remove(model);
				break;
			}
		}
	}
	
	
	private void setGroups(Group group) {
		groups = new ArrayList<IModel<Group>>();
		for (Group parent : group.getGroups()) {
			groups.add(new ObjectModel<Group>(parent));
		}
		
		Collections.sort(groups, new Comparator<IModel<Group>>() {
			@Override
			public int compare(IModel<Group> a, IModel<Group> b) {
				try {
					return a.getObject().getName().compareToIgnoreCase(b.getObject().getName());
				} catch (Exception e) {
					return 0;
				}
			}
		});
		
	}
	
	private boolean cycle(Group parent, Group child) {
		if (parent.equals(child))
			return true;
		for (Principal principal : ((KbeeGroup)child).getMembers()) {
			if (principal instanceof Group) {
				if (parent.equals(principal)) {
					return true;
				}
				else {
					if (cycle(parent, (Group)principal))
						return true;
				}
			}
		}
		return false;
	}
}
