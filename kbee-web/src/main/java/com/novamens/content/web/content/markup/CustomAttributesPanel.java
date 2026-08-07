package com.novamens.content.web.content.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.CustomAttribute;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.util.KeyValue;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;

@SuppressWarnings("serial")
public class CustomAttributesPanel<T extends Content> extends ObjectEditorPanel<T> {
	private static final long serialVersionUID = 1L;
	
	IModel<T> model;
	
	public CustomAttributesPanel(IModel<T> model) {
		this("custom-attributes-panel",model);
	}
	
	public CustomAttributesPanel(String id, IModel<T> model) {
		super(id);
		setOutputMarkupId(true);
		setModel(model);
	}
	
	
	public T getModelObject() {
		return getModel().getObject();
	}
	
	public IModel<T> getModel() {
		return model;
	}
	private void setModel(IModel<T> model2) {
		model=model2;
		
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
			addComponents();
	}
	
	protected void addComponents() {
			
		/**
		WebMarkupContainer addcontainer = new WebMarkupContainer("add-container") {
			@Override
			public boolean isVisible() {
				return false;
				//return getEditor().isEditionEnabled(); 
			}
		};
			
		
		
		AjaxLink<Void> addb = new AjaxLink<Void>("add") {
			@Override
			public void onClick(AjaxRequestTarget target) {
			}
			@Override
			public boolean isEnabled() {
				return false;
			}
			@Override
			public boolean isVisible() {
				return false;
			}
		};
			
		add(addcontainer);
		addcontainer.add(addb);
		*/
			
		addList();
	}
		
	
	public List<KeyValue<String>> getPairs() {
		List<KeyValue<String>> list = new ArrayList<KeyValue<String>>();
		list.add(new KeyValue<String>("key1", "value1"));
		list.add(new KeyValue<String>("key2", "value2"));
		list.add(new KeyValue<String>("key3", "value3"));
		return list;
	}
	private void addList() {
		
		WebMarkupContainer wlist = new WebMarkupContainer("att-list") {
			@Override
			public boolean isVisible() {
				return getModelObject().getUserDefinedAttributes()!=null;
			}
		};
			
		add(wlist);
		
		/**
		wlist.add(new ListView<Pair>("att-element",  getPairs()) {
			@Override
			protected void populateItem(ListItem<Pair> item) {
				item.add(new Label("key",  item.getModelObject().getLabel()));
				item.add(new Label("value",  item.getModelObject().getValue()));
			}
		});
		*/
		
		if (getModelObject().getUserDefinedAttributes()!=null) {
			wlist.add(new ListView<CustomAttribute>("att-element",  getModelObject().getUserDefinedAttributes()) {
				@Override
				protected void populateItem(ListItem<CustomAttribute> item) {
					item.add(new Label("key",  item.getModelObject().getName()));
					item.add(new Label("value",  item.getModelObject().getValue()));
				}
			});
		} else {
			wlist.add(new InvisiblePanel("att-element"));
		}
			
			
	}
		

}
