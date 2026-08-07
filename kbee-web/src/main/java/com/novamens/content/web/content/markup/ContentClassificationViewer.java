package com.novamens.content.web.content.markup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;

@SuppressWarnings("serial")
public class ContentClassificationViewer<T extends Content> extends Panel {
	private static final long serialVersionUID = 1L;
	
	IModel<T> model;

	public class CSList implements Serializable {
		private static final long serialVersionUID = 1L;
		protected String label;
		protected List<String> values;
	
		public CSList(String label, List<String> list) {
			this.label=label;
			this.values=list;
		}
		
		public String getLabel() {
			return label;
		}
		
		public List<String> getValues() {
			return values;
		}
	}
	
	private Map<String, List<String>> map = null;
	List<CSList> data = null;
	
	
	/** ----------------------------------------------------------------------------
	 */
	public ContentClassificationViewer(String id, IModel<T> model) {
		super(id);
	
		setModel(model);
		
		ListView<CSList> membersview = new ListView<CSList>("classifier", getList()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<ContentClassificationViewer<T>.CSList> item) {
				item.add(new Label("classifier-name", item.getModelObject().getLabel()));
				StringBuilder val = new StringBuilder();
				for (String str: item.getModelObject().getValues()) {
					if (val.length()>0)
						val.append(", ");
					val.append(str);
				}
				item.add(new Label("values", val.toString()));	
			}
		};
		
		
		WebMarkupContainer panel = new WebMarkupContainer("panel");

		panel.add(new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				return getCss();
			}
		}));
		add(panel);
				
		panel.add(membersview);
	}
	
	protected String getCss() {
		return "col-lg-12";
	}
	
	public void setModel(IModel<T> model) {
		this.model=model;
	}
	
	public T getModelObject() {
		return model.getObject();
	}
	
	public IModel<T> getModel() {
		return model;
	}

	private List<CSList> getList() {
		if (data==null) {
			data=new ArrayList<CSList>();
			for (Entry<String, List<String>> entry: getMap().entrySet()) 
					data.add( new CSList(entry.getKey(), entry.getValue()));
			
			Collections.sort(data, new Comparator<CSList>() {
				@Override
				public int compare(CSList a, CSList b) {
					try {
						return a.getLabel().compareToIgnoreCase(b.getLabel());
					} catch (Exception e) {
						return 0;
					}
				}
			}); 
		}
		return data;		
	}
	
	private Map<String, List<String>> getMap() {
		if (map == null) {
				map = new HashMap<String, List<String>>();
			for (Classification clasi:getModelObject().getClassification()) {
				Classifier cs = clasi.getClassifier();
				if (!map.containsKey(cs.getName())) {
					List<String> ls = new ArrayList<String>();
					ls.add(clasi.getStrValue());
					map.put(cs.getName(), ls);
				}
				else {
					(map.get(cs.getName())).add(clasi.getStrValue());
				}
			}
		}
		return map;
	}

}
