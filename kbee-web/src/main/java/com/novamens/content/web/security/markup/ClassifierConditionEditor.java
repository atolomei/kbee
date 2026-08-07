package com.novamens.content.web.security.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.service.DataAccessService;
import com.novamens.content.web.editor.markup.ObjectEditorPanel;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.SuggestionService;
import com.novamens.kbee.content.security.PredicatesIqlEvaluator;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AutoCompleteFieldV5;

/**
 *  
 * @param <T>
 * 
 */
@SuppressWarnings("serial")
public class ClassifierConditionEditor<T> extends ObjectEditorPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ClassifierConditionEditor.class.getName());
	
	private DataSetMember member;
	private IModel<Classifier> classifiermodel; 
	private List<IModel<DataSetMember>> members = new ArrayList<IModel<DataSetMember>>();
	
	public ClassifierConditionEditor(String id, Editor<T> editor, IModel<Classifier> classifiermodel) {
		super(id);
		
		setOutputMarkupId(true);
		setEditor2(editor);
		setClassifier(classifiermodel);
		setMembers(getClassifier());
		
		add(new Label("classifier", getClassifier().getName()));
		
		ListView<IModel<DataSetMember>> membersview = new ListView<IModel<DataSetMember>>("members", members) {
			public void populateItem(final ListItem<IModel<DataSetMember>> item) {
				DataSetMember member = item.getModelObject().getObject();
				Label strmember = new Label("member", member.getDisplayName()!=null?member.getDisplayName():"--"); 
				WebMarkupContainer removeLink = new WebMarkupContainer("remove-link") {
					@Override
					public boolean isVisible() {
						return getEditor2().isEditionEnabled();
					}
				};
				removeLink.add(new AjaxEventBehavior("click") {
					public void onEvent(AjaxRequestTarget target) {
						removeMember(item.getModelObject());
						ClassifierConditionEditor.this.onUpdate(target);
						target.add(ClassifierConditionEditor.this);
						
					}
				});
				
				item.add(removeLink);
				item.add(strmember);
				if (getEditor2().isEditionEnabled())
					item.add(new AttributeModifier("class", "list-group-item editmode"));
				item.detach();
			}
		};
		
		add(membersview);
		WebMarkupContainer selectorpanel = new WebMarkupContainer("selector-panel") {
			@Override
			public boolean isVisible() {
				return getEditor2().isEditionEnabled(); 
			}
		};
		
		AutoCompleteFieldV5<DataSetMember> selector = new AutoCompleteFieldV5<DataSetMember>("member", new PropertyModel<DataSetMember>(this, "member")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				if(getValue()!=null)
				addMember(getValue());
				setSuggestion(null);
				setStringValue(null);
				ClassifierConditionEditor.this.onUpdate(target);
				target.focusComponent(getInput());
				target.add(ClassifierConditionEditor.this);
			}	
			@Override
			public Disposition getDisposition() {
				return Disposition.VERTICAL;
			}
			@Override 
			public boolean isVisible() {
				return getEditor2().isEditionEnabled(); 
			}
			public List<Suggestion> getSuggestions(String pattern) {
				return getClassifier().getService(DataAccessService.class).getSuggestions(pattern);
				//return getClassifier().getService(SuggestionService.class).getSuggestions(pattern); 
			}
			@Override 
			public String getHistoryKey() {
				if (getClassifier().getUniqueName()!=null)
					return "condition"+getClassifier().getUniqueName().toLowerCase();
				else
					return null;
			}
		};
		
		selectorpanel.add(selector);
		selectorpanel.add(new AttributeModifier("class", "selector "));
		add(selectorpanel);
		
		add(new WebMarkupContainer("empty-panel") {
			public boolean isVisible() {
 				return getEditor2()!=null && !getEditor2().isEditionEnabled() && getMembers().isEmpty();
			}
		});
	}
	
	@Override
	public boolean isVisible() {
		return getEditor2().isEditionEnabled() || !getMembersModels().isEmpty();
	}
	
	@Override
	public void updateModel() {
	}
	
	public void setMember(DataSetMember member) {
		this.member = member;
	}
	
	public DataSetMember getMember() {
		return this.member;
	}

	public void setClassifier(IModel<Classifier> model) {
		this.classifiermodel = model;
	}
	
	public Classifier getClassifier() {
		return classifiermodel.getObject();
	}

	public String getClassifierCondition() {
		
		StringBuffer condition = new StringBuffer();
		
		List<DataSetMember> members = getMembers();
		
		if (members.isEmpty()) 
			return "";
		
		String predicate = "c"+String.valueOf(getClassifier().getId());
		
		condition.append("(");
		int m = 0;
		for (DataSetMember member : members) {
			if (m>0)
				condition.append(" or ");
			condition.append(predicate);
			condition.append("(");
			condition.append(member.getId());
			condition.append(")");
			m++;
		}
		condition.append(")");
		
		return condition.toString();
	}
	
	public String getDescription() {
		StringBuffer condition = new StringBuffer();
		List<DataSetMember> members = getMembers();
		if (members.isEmpty()) 
			return "";
		
		String predicate = getClassifier().getPredicate();

		condition.append("<span class= \"predicate\" >" + predicate+"</span>");
		
		int m = 0;
		for (DataSetMember member : members) {
			if (m>0)
				if (m==members.size()-1)
					condition.append("<span class= \"logical-operator\" > or "+"</span> ");
				else
					condition.append("<span class= \"logical-operator\" > or "+"</span> ");
			
			if (m==0)
				condition.append("<span class= \"iql-group-start\"> ( </span> ");
			
			condition.append("<span class= \"iql-value\" >"+ member.getDisplayName()+"</span> ");
			
			if (m==members.size()-1)
				condition.append("<span class= \"iql-group-end\"> ) </span> ");
			m++;
		}
		
		return condition.toString();
	}

	public int numMembers() {
		return getMembers().size();
	}
	
	public List<DataSetMember> getMembers() {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (IModel<DataSetMember> model : this.members) {
			members.add(model.getObject());
		}
		return members;
	}
	
	public void onUpdate(AjaxRequestTarget target) {
		target.add(ClassifierConditionEditor.this);
	}
	
	/**
	  	private DataSetMember member;
		private IModel<Classifier> classifiermodel;
		private List<IModel<DataSetMember>> members = new ArrayList<IModel<DataSetMember>>();
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		
		member = null;
		
		if (classifiermodel!=null)
			classifiermodel.detach();
		
		for (IModel<DataSetMember> model : members) {
			model.detach();
		}
	}
	
	protected boolean editionEnabled() {
		return getEditor2().isEditionEnabled();
	}
	
	protected String getCondition() {
		return null;
	}
	
	private boolean addMember(DataSetMember member) {
		for (IModel<DataSetMember> m : members) {
			if (m.getObject().getId().equals(member.getId()))
				return false;
		}
		
		members.add(new ObjectModel<DataSetMember>(member));
		
		return true;
	}
	
	private void removeMember(IModel<DataSetMember> model) {
		int index = getIndex(model);
		members.remove(index);
	}
	
	private void setMembers(Classifier classifier) {
		members = new ArrayList<IModel<DataSetMember>>();
		
		try {
			for (DataSetMember member : getMembers(classifier, getCondition())) {
				members.add(new ObjectModel<DataSetMember>(member));
			}
		}
		catch(Exception e) {
			logger.error(e);
		}
		
		Collections.sort(members, new Comparator<IModel<DataSetMember>>() {

			@Override
			public int compare(IModel<DataSetMember> o1, IModel<DataSetMember> o2) {
				try {
					if (o1!=null && o1.getObject()!=null && o1.getObject().getDisplayName()!=null) {
						if (o2!=null && o2.getObject()!=null && o2.getObject().getDisplayName()!=null) {
							try {
								return o1.getObject().getDisplayName().compareToIgnoreCase(o2.getObject().getDisplayName());
							} catch (Exception e) {
								return 0;
							}
						}
						else
							return -1;
					}
					else {
						if (o2!=null && o2.getObject()!=null && o2.getObject().getDisplayName()!=null) {
							return 1;
						}
					}
						return 0;
				} catch (Exception e) {
					return 0;
				}
			}
			
		});
	}
	
	private List<DataSetMember> getMembers(Classifier classifier, String condition) {
		
		Assert.isTrue(classifier.getPredicate()!=null, "predicate not found!");
		
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		
		if (condition==null || "".equals(condition) || condition.contains("null")) 
			return members;
		
		Expression iqlexpression = classifier.getDomain().getService(IqlService.class).getExpression(condition);
		
		PredicatesIqlEvaluator evaluator = new PredicatesIqlEvaluator(iqlexpression);
		Map<String, List<String>> predicates = evaluator.evaluate();
		List<String> membersids = predicates.get(classifier.getPredicate());
		
		if (membersids==null) 
			return members;
		
		for (String memberid : membersids) {
			DataSetMember member = getContentDao().findMemberById(Long.valueOf(memberid));
			if (member!=null) members.add(member);		
		}
		
		return members;
	}
	
	private List<IModel<DataSetMember>> getMembersModels() {
		return members;
	}

	private int getIndex(IModel<DataSetMember> model) {
		int index = 0;
		DataSetMember member = model.getObject();
		for (IModel<DataSetMember> m : members) {
			if (member.equals(m.getObject()))
				break;
			else
				index++;
		}
		return index;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
