package kbee.web.searcher.panel;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classification;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.datetime.DateTimeService;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 *
 * <section>
 * <h2>Section_1<h2>
 * <no subsection>
 * 1
 * 2
 * 3
 * Subsection_11
 * 1
 * 2
 * 3
 * Subsection_12
 * 1
 * 2
 * 3
 * Subsection_13
 * </section>
 * 
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
public class SearcherDetailAttributesPanel<T extends Content> extends SearcherDetailPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private class AttributeValue {
		
		private String attribute;
		private List<String> values;
		
		public AttributeValue(String attribute, List<String> values) {
			this.attribute = attribute;
			this.values = values;
		}
		public String getAttribute() {
			return attribute;
		}
		public List<String> getValues() {
			return values;
		}
	}

	
	private boolean isConsole = false;
	
	
	
	public SearcherDetailAttributesPanel(String id, IModel<T> model, IModel<Site> site_model, boolean  isConsole) {
		super(id, model, site_model);
		this. isConsole= isConsole;
	}
	
	public boolean isConsole() {
		return this.isConsole;
	}

	
	public  List<ModelSection>  getSections() {
		return new ArrayList<>();
//		if (!isConsole)
//			return getModelObject().getContentTemplate().getPortalSections();
//		else
//			return getModelObject().getContentTemplate().getSections();
//			
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
			
		com.novamens.wicket.model.ListModel<ModelSection> s = 
				new com.novamens.wicket.model.ListModel<ModelSection>(	new org.apache.wicket.model.Model<org.apache.wicket.markup.html.panel.Panel>(this), "sections");
		
		add(new ListView<ModelSection>("section", s) {
					protected void populateItem(ListItem<ModelSection> item) {
						SectionFragment section_panel = new SectionFragment("section-panel", item.getModel());
						item.add(section_panel);
					}
				}
		);
	}
	
	 
	
	
	private boolean hasExpander = true;
	
	
	public void setHasTitle(boolean b) {
		this.hasExpander = b;
	}
	
	public boolean hasTitle() {
		return this.hasExpander;
	}


	public List<AttributeValue> getAttributes() {
		List<AttributeValue> attributes = new ArrayList<AttributeValue>();
		for (ModelElementTemplate template : getModel().getObject().getContentTemplate().getStructure()) {
			if (isVisible(template)) {
				AttributeValue value = getAttribute(template);
				if (value!=null && value.getValues()!=null && !value.getValues().isEmpty()) {
					attributes.add(value);
				}
			}
		}
		
		Collections.sort( attributes,  new Comparator<AttributeValue>() {
			@Override
			public int compare(SearcherDetailAttributesPanel<T>.AttributeValue arg0, SearcherDetailAttributesPanel<T>.AttributeValue arg1) {
				try {
					return arg0.attribute.compareToIgnoreCase(arg1.attribute);
				} catch (Exception e) {
					return 0;
				}
			}
			
		});
		
		return attributes;
	}

	
	/**
	 * @param template
	 * @return
	 */
	public boolean isVisible(ModelElementTemplate template) {
		ModelElement element = template.getElement();
		boolean visible = isConsole() || element.isVisible("portals");
		return visible;
	}
	
	private AttributeValue getAttribute(ModelElementTemplate template) {
		List<String> values = null;
		if (template instanceof AttributeTemplate) {
			if (((AttributeTemplate) template).getAttribute().isDate()) {
				values = new ArrayList<String>();
				for (String s: getModelObject().getAttributeValues(((AttributeTemplate) template).getAttribute())) {
					OffsetDateTime da=ServiceLocator.getService(DateTimeService.class).parseStrDate(s);
					values.add(ServiceLocator.getService(DateTimeService.class).getDateDisplayString(da, getSessionUser()!=null?getSessionUser().getLocale():Locale.getDefault(),DateTimeService.Month_Day_Year));
				}
			}
			else {
				if (((AttributeTemplate) template).getAttribute().getType().equals(AttributeType.BOOLEAN)) {
					values = new ArrayList<String>();
					for (String value : getModelObject().getAttributeValues(((AttributeTemplate) template).getAttribute())) {
						values.add((new StringResourceModel(value+".value", SearcherDetailAttributesPanel.this)).getObject());
					}
				}
				else {
					values = getModelObject().getAttributeValues(((AttributeTemplate) template).getAttribute());
				}
			}	
		}
		else if (template instanceof ClassifierTemplate) {
			values = new ArrayList<String>();
			for (Classification classification : getModelObject().getClassification(((ClassifierTemplate) template).getClassifier())) {
				values.add(classification.getStrValue());
			}
		}
		return new AttributeValue(template.getElement().getDisplayName(), values);
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	
	
		
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 *
	 */
	public class SectionFragment extends Fragment {
		
		IModel<ModelSection> model;
		
		public SectionFragment(String id, IModel<ModelSection> model) {
			super(id, "section-fragment", SearcherDetailAttributesPanel.this);
			this.model=model;
			this.setOutputMarkupId(true);
		}
		
		public IModel<ModelSection> getModel() {
			return this.model ;
		}
		
		
		
		public List<AttributeValue> getAttributes() {
			
			List<AttributeValue> attributes = new ArrayList<AttributeValue>();
			for (ModelElementTemplate template : getModel().getObject().getStructure()) {
				ModelElement element = template.getElement();
				boolean visible = isConsole() || element.isVisible("portals");
				if (visible) {
					AttributeValue value = getAttribute(template);
					if (value!=null && value.getValues()!=null && !value.getValues().isEmpty()) {
						attributes.add(value);
					}
				}
			}
			Collections.sort( attributes,  new Comparator<AttributeValue>() {
				@Override
				public int compare(SearcherDetailAttributesPanel<T>.AttributeValue arg0, SearcherDetailAttributesPanel<T>.AttributeValue arg1) {
					try {
						return arg0.attribute.compareToIgnoreCase(arg1.attribute);
					} catch (Exception e) {
						return 0;
					}
				}
				
			});
			
			return attributes;
		}
 
		
		public void onInitialize() {
			super.onInitialize();
			
			String name = getModel().getObject().getName();
			if (name==null || name.length()==0) {
				name= new StringResourceModel("attributes.title", this, null).getObject();
			}
			
			Label title=new Label("section-title", name);
			
			//title.setVisible( 
			//		(SectionFragment.this.getModel().getObject().getName()!=null) && 
			//		(SectionFragment.this.getModel().getObject().getName().length()>0));
		//	
			add(title);
			
			com.novamens.wicket.model.ListModel<AttributeValue> lm = 
					new com.novamens.wicket.model.ListModel<AttributeValue> (
							new org.apache.wicket.model.Model<Fragment>(SectionFragment.this),"attributes");

			add(new ListView<AttributeValue>("attributes", lm) {
				@Override
				protected void populateItem(ListItem<SearcherDetailAttributesPanel<T>.AttributeValue> item) {
					
					item.add(new Label("attribute", item.getModelObject().getAttribute()));
					item.add(new ListView<String>("values", item.getModelObject().getValues()) {
						@Override
						protected void populateItem(ListItem<String> itemx) {
								itemx.add(new Label("value", itemx.getModelObject()));
						}
					});
				}
			});
		

			
			/**
			for (ModelElementTemplate m:sub) {
				if (SearcherDetailAttributesPanel.this.isVisible(m)) {
						AttributeValue value = getAttribute(m);
						if (value!=null && value.getValues()!=null && !value.getValues().isEmpty()) {
							add(new Label("attribute", value.getAttribute()));
							add(new ListView<String>("values", value.getValues()) {
								@Override
								protected void populateItem(ListItem<String> item) {
									item.add(new Label("value", item.getModelObject()));
								}
							});
					}
				}
			}
			**/
		}
		
		public void onDetach() {
			super.onDetach();
			this.model.detach();
		}
	}
	
	
	
	
	public class SubSectionFragment extends Fragment {
		
		public SubSectionFragment(String id) {
			super(id, "subsection-fragment", SearcherDetailAttributesPanel.this);
			this.setOutputMarkupId(true);
		}
		
		
		public void onInitialize() {
			super.onInitialize();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
