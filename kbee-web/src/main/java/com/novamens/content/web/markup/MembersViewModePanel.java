package com.novamens.content.web.markup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classification;
import com.novamens.wicket.model.ObjectModel;

public class MembersViewModePanel<T extends Content> extends Panel {

	private static final long serialVersionUID = -3007991409108637970L;

	
	@SuppressWarnings("unchecked")
	public void onDetach() {
		// ListView<IModel<Classification>> view = (ListView<IModel<Classification>>) get("classification");
		// Iterator<IModel<Classification>> items = (Iterator<IModel<Classification>>) view.getList().iterator();
		ListView<Pair> view = (ListView<Pair>) get("classification");
		view=null;
		super.onDetach();
	}
	
	private class Pair implements IModel<Classification>  {
		public String label;
		public StringBuilder value;
		ObjectModel<Classification> model;
		public Pair(Classification c) {
			model=new ObjectModel<Classification>(c);
			label=c.getClassifier().getName();
			value=new StringBuilder();
			value.append(c.getStrValue());
		}
		@Override
		public void detach() {
			label=null;
			value=null;
			model.detach();
		}
		@Override
		public Classification getObject() {
			return model.getObject();
		}
		@Override
		public void setObject(Classification object) {
			model=new ObjectModel<Classification>(object);
			label=object.getClassifier().getName();
			value=new StringBuilder();
			value.append(object.getStrValue());
		}
	}
	
	
	IModel<T> model;
	public void setModel(IModel<T> model) {
		this.model=model;
	}
	
	public MembersViewModePanel(String id, IModel<T> model) {
		super(id);
		
		setOutputMarkupId(true);
		
		setModel(model);
		
		List<Classification> list = model.getObject().getClassification();
		
		/*List<IModel<String>> pairs = new ArrayList<IModel<String>>();
		for (Classification cl: list) {
			if ( (pairs.size()>0) && (cl.getClassifier().getName().equals(pairs.get(pairs.size()-1).getObject().label))) {
				pairs.get(pairs.size()-1).getObject().value.append(", "+cl.getStrValue());
			}
			else 
				pairs.add( new Model<Pair>(cl));
		}
		
		ListView<Pair> view = new ListView<Pair>("classification", pairs) {
		 	private static final long serialVersionUID = 8355818030125716563L;
			@Override
			protected void populateItem(ListItem<Pair> item) {
				item.add(new Label("label", item.getModelObject().label+"."));
				item.add( new Label("value", item.getModelObject().value.toString()));
				if (item.getIndex()==0)	
					item.add(new AttributeModifier("class", "first"));
			}
		};
		add(view);
        */
		
		 List<IModel<Classification>> listModel = new ArrayList<IModel<Classification>>();
		 for (Classification cl: list) 
			listModel.add(new ObjectModel<Classification>(cl));
		ListView<IModel<Classification>> view = new ListView<IModel<Classification>>("classification", listModel) {
			@Override
			protected void populateItem(ListItem<IModel<Classification>> item) {
				item.add(new Label("label", item.getModelObject().getObject().getClassifier().getName()+"."));
				item.add( new Label("value", item.getModelObject().getObject().getStrValue()));
				if (item.getIndex()==0)	
					item.add(new AttributeModifier("class", "first"));
			}
		};
		add(view);
		 
	}
	

}
