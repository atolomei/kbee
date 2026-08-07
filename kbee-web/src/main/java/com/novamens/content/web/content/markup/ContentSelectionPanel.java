package com.novamens.content.web.content.markup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;

import kbee.web.util.Property;


/**
 * 
 *  My Tasks -> Workflow Batch Delete Tasks
 *  
 *  ----
 *  My Tasks -> Workflow Batch Actions
 *  My Tasks -> Batch Classify
 *  
 */
@SuppressWarnings("serial")
public class ContentSelectionPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<Content>> selection;
	private Map<Serializable, String> status = new HashMap<Serializable, String>();

	public ContentSelectionPanel(List<IModel<Content>> selection) {
		super("selection");
		
		setOutputMarkupId(true);
		
		setSelection(selection);
		
		add(new ListView<Property<Content>>("property", getProperties()) {
			public void populateItem(ListItem<Property<Content>> propertyitem) {
				Property<Content> property = propertyitem.getModelObject();
				propertyitem.add(new Label("label", property.getLabel()));
				if (property.getCss()!=null) {
					propertyitem.add(new AttributeModifier("class", property.getCss()));
				}
			}
		});
		
		
		add(new ListView<IModel<Content>>("selection", new PropertyModel<List<IModel<Content>>>(this, "selection")) {
			
			public void populateItem(final ListItem<IModel<Content>> item) {
				
				item.add(new ListView<Property<Content>>("property", getProperties()) {
					public void populateItem(ListItem<Property<Content>> propertyitem) {
						Property<Content> property = propertyitem.getModelObject();
						
						
						Link<Void> link = new Link<Void>("link") {
							public void onClick() {
								Page page = ContentSelectionPanel.this.getPage(item.getModelObject());
								if (page!=null) {
									setResponsePage(page);
								}
							}
						};
						
						if (property.isLink()) {
							propertyitem.add((new Label("value")).setVisible(false));
							link.add(new Label("value", property.getValue(item.getModelObject())));
							propertyitem.add(link);
						}
						else {
							propertyitem.add(new Label("value", property.getValue(item.getModelObject())));
							link.setVisible(false);
							propertyitem.add(link);
						}
						if (property.getCss()!=null) {
							propertyitem.add(new AttributeModifier("class", property.getCss()));
						}
					}
				});
				
				item.add(new Label("status", new Model<String>() {
					public String getObject() {
						String status = ContentSelectionPanel.this.status.get(item.getModelObject().getObject().getId());
						status = status==null ? "" : (status.equals("") ? "<span class=\"success\">OK</span>" : "<span class=\"danger\">"+status+"</span>");
						return status;
					}
				}));
				
				((Label)item.get("status")).setEscapeModelStrings(false);
				
				item.add(new AjaxLink<Void>("remove-link") {
					public void onClick(AjaxRequestTarget target) {
						removeSelection(item.getIndex());
						target.add(ContentSelectionPanel.this);
						onUpdate(target);
					}
					public boolean isVisible() {
						return getSelection().size()>1;
					}
				});
			}
		});
	}
	
	public void onUpdate(AjaxRequestTarget target) {
		
	}
	
	
	public List<IModel<Content>> getSelection() {
		return selection;
	}
	
	
	
	public void setStatus(Content content, String message) {
		status.put(content.getId(), message);
	}
	
	
	
	public boolean hasErrors() {
		for (String xstatus : status.values()) {
			if (!"".equals(xstatus)) {
				return true;
			}
		}
		return false;
	}
	
	
	
	@Override
	public void onDetach() {
		for (IModel<Content> model : getSelection()) {
			model.detach();
		}
		super.onDetach();
	}

	
	
	/** 
	 */
	protected Page getPage(IModel<Content> model) {
		return null;
	}
	
	/**
	 *  
	 * 
	 **/
	protected List<Property<Content>> getProperties() {
		List<Property<Content>> properties = new ArrayList<Property<Content>>();
		properties.add(new Property<Content>() {
			public IModel<String> getLabel() {
				return new StringResourceModel("grid.title", ContentSelectionPanel.this, null);
			}
			public IModel<String> getValue(IModel<Content> content) {
				return new PropertyModel<String>(content, "title");
			}
			public String getCss() {
				return "col-lg-4";
			}
			public boolean isLink() {
				return true;
			}
		});
		
		return properties;
	}
	
	
	
	/**
	 *  
	 */
	protected void setSelection(List<IModel<Content>> selection) {
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
				} 
				catch (Exception e) {
					return 0;
				}
			}
		}));
	}
	
	
	/** 
	 */
	protected void removeSelection(int index) {
		selection.remove(index);
	}
	
	
	
	/** 
	 */
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
}
