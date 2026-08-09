package kbee.web.form;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidator;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.util.Assert;

import com.novamens.content.model.Multiplicity;
import com.novamens.indexer.query.Suggestion;

import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.JXPath;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.SortableBehavior;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.model.ObjectModel;


/**
 *
 * @param <T>  Example: User
 * @param <P>  Example: Group
 * 
 */
@SuppressWarnings("serial")
public class RelationGridEditor<T, P> extends ObjectEditorPanel<T>  {

	private static final long serialVersionUID = 1L;
							
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RelationGridEditor.class.getName());
	
	private List<IModel<P>> xvalues = new ArrayList<IModel<P>>();
	
	private IModel<Collection<P>> propertymodel;
	private List<Property<?>> properties;
	private Property<?> key;
	private boolean updated	= false;
	private Disposition disposition;
	private String property_id;
	
	private ValueRowView expandedrow = null;

	
	/**
	 * 
	 */
	public class SModel implements IModel<P> {
		private P value;
		public SModel(P value) {
			this.value = value;
		}
		public P getObject() {
			return value;
		}
		public void setObject(P value) {
			this.value = value;
		}
		public void detach() {
		}
	}


	protected String getPropertyLabel(String property_name) {
		return (new StringResourceModel("property."+property_name, RelationGridEditor.this, null)).getObject();
	}
	
	
	protected void setUpdated(boolean b) {
		updated=b;
	}
	
	protected boolean isUpdated() {
		return updated;
	}

	public interface UpdateListener extends Serializable {
		public void onUpdate(AjaxRequestTarget target);
	}
	
	/**
	 *
	 *
	 */
	public class Property<V> implements Serializable {
		private String name;
		private boolean title = false;
		private Boolean selectable = null;
		private boolean key = false;
		
		public class SerializableModel implements IModel<V> {
			V value;
			public SerializableModel(V value) {
				this.value = value;
			}
			public V getObject() {
				return value;
			}
			public void setObject(V value) {
				this.value = value;
			}
			public void detach() {
			}
		}
		
		
		
		
		
		
		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public V getValue(P value) {
			try {
				return (new PropertyModel<V>(value, getName())).getObject();
			}
			catch (RuntimeException e) {
				logger.error(e);
				return null;
			}
		}
		
		public String getLabel() {
			return RelationGridEditor.this.getPropertyLabel(getName());
		}
		
		public IModel<V> getValueModel(P value) {
			IModel<V> model = null;
			V propertyvalue = getValue(value);
			model = getModel(propertyvalue);
			return model;
		}
		public IModel<V> getModel(V value) {
			IModel<V> model = null;
			if (value instanceof Identifiable) {
				model = new ObjectModel<V>(value);
				model.detach();
			}
			else
			if (value instanceof Serializable) {
				model = new SerializableModel(value);
			}
			return model;
		}
		@SuppressWarnings("unchecked")
		public void setValue(P value, Object propertyvalue) {
			(new PropertyModel<V>(value, getName())).setObject((V)propertyvalue);
		}
		public String getDisplayValue(IModel<P> model) {
			return getDisplayValue(model.getObject());
		}
		public String getDisplayValue(P value) {
			return getStringValue(getValue(value));
		}
		
		
		
		
		public void setTitle(boolean value) {
			this.title = value;
		}
		public boolean getTitle() {
			return title;
		}
		public String getHistoryKey() {
			return getName();
		}
		public void setSelectable(boolean value) {
			this.selectable = value;
		}
		public boolean isEditable() {
			return true;
		}
		public boolean isSelectable() {
			if (selectable==null) {
				selectable = getChoices()!=null; 
			}
			return selectable;
		}	
		public void setKey(boolean value) {
			this.key = value;
		}
		public boolean getKey() {
			return key;
		}
		public boolean isBoolean() {
			return false;
		}
		public boolean isTextArea() {
			return false;
		}
		public boolean isAutocomplete() {
			return false;
		}	
		public Field<V> getField(String id) {
			return getField(id, null, null);
		}
		@SuppressWarnings("unchecked")
		public Field<V> getField(String id, final IModel<P> model, UpdateListener listener) {
			if (isSelectable()) {
				IModel<List<V>> choices = new IModel<List<V>>() {
					public List<V> getObject() {
						List<V> choices = getChoices();
						if (model!=null) {
							V value = Property.this.getValue(model.getObject());
							if (value!=null && !choices.contains(value)) {
								choices.add(0, value);
							}
						}
						return choices;
					}
					public void setObject(List<V> values) {
					}
					public void detach() {
					}
				};
				IModel<V> fieldmodel = null;
				if (model==null) {
					fieldmodel= new IModel<V>() {
						public V getObject() {
							return null;
						}
						public void setObject(V object) {
						}
						public void detach() {
						}
					};
				}
				else {
					fieldmodel = new PropertyModel<V>(model, getName());
				}
				return new ChoiceField<V>(id, fieldmodel, choices) {
					@Override
					protected String getDisplayValue(V value) {
						return RelationGridEditor.this.getStringValue(value);
					}
					
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						RelationGridEditor.this.onUpdate(Property.this, getValue(), target);
						if (getModel()!=null) {
							updated = true;
							getModel().setObject(getValue());
							if (getMultiplicity().equals(Multiplicity.M0N)) {
								setValue(null);
							}
						}
						else {
							setValue(null);
						}
						if (listener!=null) {
							listener.onUpdate(target);
						}
					}	
					@Override
					public String getProperty() {
						return Property.this.getName();
					}
					@Override
					public Disposition getDisposition() {
						return Disposition.VERTICAL;
					}
					@Override
					public IModel<V> getModel(V value) {
						return Property.this.getModel(value);
					}
				};
			}
			else
			if (isAutocomplete()) {
				IModel<V> fieldmodel = null;
				if (model==null) {
					fieldmodel= new IModel<V>() {
						public V getObject() {
							return null;
						}
						public void setObject(V object) {
						}
						public void detach() {
						}
					};
				}
				else {
					fieldmodel = new PropertyModel<V>(model, getName());
				}
				return new AutoCompleteFieldV5<V>(id, fieldmodel) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						RelationGridEditor.this.onUpdate(Property.this, getValue(), target);
						if (getModel()!=null) {
							updated = true;
							getModel().setObject(getValue());
						}
						setSuggestion(null);
						setStringValue(null);
						target.focusComponent(getInput());
 						target.add(getParent().getParent().getParent().getParent());
					}	
					@Override
					public String getProperty() {
						return Property.this.getName();
					}
					@Override
					public IModel<String> getLabel() {
						return new Model<String>(Property.this.getLabel());
					}
					@Override
					public Disposition getDisposition() {
						return Disposition.VERTICAL;
					}
					@Override
					public List<Suggestion> getSuggestions(String pattern) {
						return Property.this.getSuggestions(pattern);
					}
					@Override
					public String getHistoryKey() {
						return Property.this.getHistoryKey();
					}
					@Override
					protected boolean isValid(IModel<V> model) {
						return Property.this.isValid(model);
					}
					@Override
					protected IModel<V> getModel(V value) {
						return Property.this.getModel(value);
					}
					@Override
					protected String serialize(IModel<V> model) {
						String value= Property.this.serialize(model);
						if (value==null) value = super.serialize(model);
						return value;
					}
					@Override
					protected IModel<V> deserialize(String value) {
						IModel<V> model = Property.this.deserialize(value);
						if (model==null) model = super.deserialize(value);
						return model;
					}
				};
			}
			else if (isBoolean()) {
				IModel<Boolean> fieldmodel = null;
				if (model!=null) {
					fieldmodel = new PropertyModel<Boolean>(model, getName());
				}
				return (Field<V>)new BooleanField(id, fieldmodel) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
 						RelationGridEditor.this.onUpdate(Property.this, getValue(), target);
						if (getModel()!=null) {
							updated = true;
							getModel().setObject(getValue());
						}
					}	
					@Override
					public String getProperty() {
						return Property.this.getName();
					}
					@Override
					public Disposition getDisposition() {
						return Disposition.VERTICAL;
					}
				};
			}
			else if (isTextArea()) {
				IModel<V> fieldmodel = null;
				if (model!=null) {
					fieldmodel = new PropertyModel<V>(model, getName());
				}
				Field<V> field = (Field<V>)new TextAreaField<V>(id, fieldmodel) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
 						RelationGridEditor.this.onUpdate(Property.this, getValue(), target);
						if (getModel()!=null) {
							updated = true;
							getModel().setObject(getValue());
						}
					}	
					@Override
					public String getProperty() {
						return Property.this.getName();
					}
					@Override
					public boolean isHelpInfo() {
						return ((TextAreaProperty)Property.this).helpInfo();
					}
					@Override
					public void onHelp(AjaxRequestTarget target) {
						((TextAreaProperty)Property.this).onHelp(target);
					}
					@Override
					public Disposition getDisposition() {
						return Disposition.VERTICAL;
					}
				};
				if (getValidator()!=null) {
					field.add(getValidator());
				}
				return field;
			}
			else {
				IModel<V> fieldmodel = null;
				if (model!=null) {
					fieldmodel = new PropertyModel<V>(model, getName());
				}
				return (Field<V>)new TextField<V>(id, fieldmodel, false, getValidator()) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
 						RelationGridEditor.this.onUpdate(Property.this, getValue(), target);
						if (getModel()!=null) {
							updated = true;
 							getModel().setObject(getValue());
						}
						target.add(getParent().getParent().getParent().getParent());
					}	 
					@Override
					public String getProperty() {
						return Property.this.getName();
					}
					@Override
					public boolean isVisible() {
						return Property.this.isVisible(model);
					}
					@Override
					public Disposition getDisposition() {
						return Disposition.VERTICAL;
					}
				};
			}
		}
		public List<V> getChoices() {
			return null;
		}
		public List<Suggestion> getSuggestions(String pattern) {
			return null;
		}
		public Multiplicity getMultiplicity() {
			return Multiplicity.M0N;
		}
		public boolean isValid(IModel<V> model) {
			return true;
		}
		public IValidator<V> getValidator() {
			return null;
		}
		public String serialize(IModel<V> model) {
			return null;
		}
		public IModel<V> deserialize(String value) {
			return null;
		}
		public boolean isVisible() {
			return true;
		}
		public boolean isVisible(IModel<P> model) {
			return true;
		}
		public boolean isEditable(IModel<P> model) {
			return isVisible(model);
		}
		public boolean isGrid() {
			return true;
		}
		public String getStringWidth() {
			return "10%";
		}
		public String getStyle(IModel<P> model) {
			return null;
		}
	};
	
	
	/**
	 * 
	 * 
	 * 
	 * 
	 *
	 */
	public class BooleanProperty extends Property<Boolean> {
		@Override
		public boolean isBoolean() {
			return true;
		}
		public String getDisplayValue(P object) {
			Boolean value = getValue(object);
			return "<span class=\""+(value.booleanValue()?"yes":"no")  + "\">" +  RelationGridEditor.this.getStringLabel(value.toString()+".value") + " </span>";
		
		}
		@Override
		public String getStringWidth() {
			return "50px";
		}
	}
	
	public class TextAreaProperty extends Property<String> {
		@Override
		public boolean isTextArea() {
			return true;
		}
		public boolean helpInfo() {
			return false;
		}
		@Override
		public boolean isGrid() {
			return false;
		}
		protected void onHelp(AjaxRequestTarget target) {
		}
	}

	
	public class LabelProperty extends Property<String> {
		@Override
		public void setValue(P value, Object propertyvalue) {
		}
		@Override
		public boolean isEditable() {
			return false;
		}
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	public class NewValueModel implements IModel<P> {
		private Map<Property<?>, IModel<?>> values;
		private P value;
		public NewValueModel(P value) {
			setObject(value);
		}
		@SuppressWarnings("unchecked")
		public P getObject() {
			try {
				if (value==null) {
					if (!getPropertiesCache().isEmpty()) {
						value = getNewValue();
						for (Property<?> property : values.keySet()) {
							property.setValue(value, values.get(property).getObject());
						}
					}
					else {
						for (Property<?> property : values.keySet()) {
							value = (P)values.get(property).getObject();
						}
					}
				}
				return value;
			} 
			catch (Exception e) {
				logger.error(e);
				return null;
			}
		}
		
		public void setObject(P value) {
			this.value = value;
		}
		public void detach() {
			if (value!=null) {
				values = new HashMap<Property<?>, IModel<?>>();
				if (!getPropertiesCache().isEmpty()) {
					try {
						for (Property<?> property : getPropertiesCache()) {
							IModel<?> model = property.getValueModel(value);
							if (model!=null) {
								values.put(property, model);
								model.detach();
							}
						}
						if (getKeyCache()!=null) {
							values.put(getKeyCache(), getKeyCache().getValueModel(value));
						}
					} 
					catch (Exception e) {
						logger.error(e);
					}
				}
				else {
					IModel<?> model = RelationGridEditor.this.getModel(value);
					values.put(getKeyCache(), model);
					model.detach();
				}
				value = null;
			}
		}
	}

	
	/**
	 *
	 * 
	 * 
	 * 
	 * 
	 *
	 */
	public class ValueModel implements IModel<P> {
		private Map<Property<?>,IModel<?>> values;
		private P value;
		private IModel<P> model;
		public ValueModel(IModel<P> model) {
			this.model = model;
			setObject(model.getObject());
		}
		public P getObject() {
			if (value==null) {
				value = model.getObject();
				if (!getPropertiesCache().isEmpty()) {
					for (Property<?> property : values.keySet()) {
						property.setValue(value, values.get(property).getObject());
					}
				}
			}
			return value;
		}
		public void setObject(P value) {
			this.value = value;
		}
		public void detach() {
			if (value!=null) {
				values = new HashMap<Property<?>,IModel<?>>();
				if (!getPropertiesCache().isEmpty()) {
					for (Property<?> property : getPropertiesCache()) {
						IModel<?> model = property.getValueModel(value);
						if (model!=null) {
							values.put(property, model);
							model.detach();
						}	
					}
				}
				value = null;
			}
			model.detach();
		}
	}

	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 *
	 */
	public class ValueEditor extends Fragment {
		private IModel<P> model;
		public ValueEditor(IModel<P> model) {
			super("editor", "value-editor-fragment", RelationGridEditor.this);
			setModel(model);
			add(new ListView<Panel>("form", () -> getFields()) {
				protected void populateItem(ListItem<Panel> item){
					item.add(item.getModelObject());
				}	
			});
		}
		public void setModel(IModel<P> model) {
			this.model = model;
		}
		public IModel<P> getModel() {
			return model;
		}
		public boolean isVisible() {
			return super.isVisible() && !getPropertiesCache().isEmpty();
		}
		public List<Panel> getFields() {
			List<Panel> fields = new ArrayList<Panel>();
			for (Property<?> property : getPropertiesCache()) {
				if (property.isVisible() && property.isEditable(getModel()) && property.isEditable())
				fields.add(property.getField("field", getModel(), new UpdateListener() {
					public void onUpdate(AjaxRequestTarget target) {
						target.add(ValueEditor.this.getParent());
					}
				}));
			}	
			for(Panel field : fields) {
				if (field instanceof Field<?>) {
					if (((Field<?>)field).getValidator()!=null) { 
						((Field<?>)field).onBeforeRender(); 
						((Field<?>)field).validateModel(); 
					}
				}
			}
			return fields;
		}
		@Override
		public void onDetach() {
			super.onDetach();
//			for (Panel panel : ((ListView<Panel>)get("form")).getList()) {
//				panel.detach();
//			}
			getModel().detach();
		}
	}

	
	public class CreationPanel extends Fragment {
		public CreationPanel(String id) {
			super(id, "creation-panel-fragment", RelationGridEditor.this);
			
			Property<?> key = getKeyCache();
			WebMarkupContainer addbuttonpanel = new WebMarkupContainer("add-button-panel");
			add(addbuttonpanel);
			if (key!=null) {
				Field<?> field = key.getField("selector-field");
				field.setDisposition(Disposition.VERTICAL);
				add(field);
				addbuttonpanel.setVisible(false);
			}
			else {
				addbuttonpanel.add(new AjaxLink<Void>("button") {
					public void onClick(AjaxRequestTarget target) {
						P value = getNewValue();
						RelationGridEditor.this.add(new NewValueModel(value));
						target.add(RelationGridEditor.this);
					}
				});
				WebMarkupContainer field = new WebMarkupContainer("selector-field");
				field.setVisible(false);
				add(field);
			}
		}
	}
	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 *
	 */
	public class ValueView extends Fragment {
		private IModel<P> model;
		public ValueView(String id, String markupid, IModel<P> model) {
			super(id, markupid, RelationGridEditor.this);
			setModel(model);
			setOutputMarkupId(true);
		}
		public void setModel(IModel<P> model) {
			this.model = model;
		}
		public IModel<P> getModel() {
			return model;
		}
		public int getIndex() {
			return RelationGridEditor.this.getIndex(getModel());
		}
		protected Panel getMenu(IModel<P> model) {
			
			ContextMenuPanel<P> menu = new ContextMenuPanel<P>(model);
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<P>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						ValueView.this.edit(target);
					}
					@Override
					public String getLabel() {	
						return RelationGridEditor.this.getStringLabel("menu.edit");
					}
					@Override
					public boolean isVisible() {
						return !getPropertiesCache().isEmpty();
					}
					@Override
					public boolean isEnabled() {
						return getEditor().isEditionEnabled();
					}
				}
			);

			menu.addItem(id ->
				new AjaxMenuItemPanelV5<P>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						RelationGridEditor.this.up(ValueView.this.getModel());
						target.add(RelationGridEditor.this);
					}
					@Override 
					public String getLabel() {	
						return RelationGridEditor.this.getStringLabel("menu.up");
					}
					@Override
					public boolean isVisible() {
						return ValueView.this.getIndex()>0;
					}
					@Override
					public boolean isEnabled() {
						return getEditor().isEditionEnabled();
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<P>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						RelationGridEditor.this.down(ValueView.this.getModel());
						target.add(RelationGridEditor.this);
					}
					@Override
					public String getLabel() {	
						return RelationGridEditor.this.getStringLabel("menu.down");
					}
					@Override
					public boolean isVisible() {
						return ValueView.this.getIndex()<getValues().size()-1;
					}
					@Override
					public boolean isEnabled() {
						return getEditor().isEditionEnabled();
					}
				}
			);
			
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<P>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return getValues().size()>1 && creationEnabled();
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<P>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						IModel<P> model = ValueView.this.getModel(); 
						if (RelationGridEditor.this.deleteable(target, model)) {
							RelationGridEditor.this.delete(model);
							target.add(RelationGridEditor.this);
							RelationGridEditor.this.onUpdate(target);
						}
					} 
					@Override
					public String getLabel() {
						return RelationGridEditor.this.getStringLabel("menu.delete");
					}
					@Override
					public boolean isVisible() {
						return  creationEnabled();
					}
					@Override
					public boolean isEnabled() {
						return getEditor().isEditionEnabled() && creationEnabled() && deleteEnabled(getModelObject());
					}
				}	
			);
			custom(menu);
			return menu;
		}
		protected void edit(AjaxRequestTarget target) {
			
		}
	}
	
	public class ValueRowView extends ValueView {
		private boolean isexpanded = false;
		public ValueRowView(String id, IModel<P> model) {
			super(id, "value-rowview-fragment", model);
			
			setOutputMarkupId(true);
			
			add(new AjaxLink<Void>("expander") {
				public void onClick(AjaxRequestTarget target) {
					expand(target);
				}
			});
			((MarkupContainer)get("expander")).add(new WebMarkupContainer("icon"));
			get("expander:icon").add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return isexpanded ? "far fa-angle-down" : "far fa-angle-up";
				}
			}));
			
			WebMarkupContainer menucell = new WebMarkupContainer("menu-cell") {
				public boolean isVisible() {
					return getEditor()==null || getEditor().isEditionEnabled();
				}
			};
			WebMarkupContainer menulink = new WebMarkupContainer("menulink");
			menulink.setOutputMarkupId(true);
			menucell.add(menulink);
			Panel menu = getMenu(model);
			menu.add(new AttributeModifier("aria-labelledby", String.valueOf(menulink.getMarkupId()))); 
			menucell.add(menu);
			add(menucell);
			
			add(new ListView<Property<?>>("td", ()-> getGridProperties()) {
				protected void populateItem(ListItem<Property<?>> item) {
					Property<?> property = item.getModelObject();
					IModel<P> model = ValueRowView.this.getModel();
					String label = property.isVisible(model) ?
						property.getDisplayValue(model) :
						"";	
					item.add((new Label("cell", label)).setEscapeModelStrings(false));
					String style = null;
					
					if (property.getStringWidth()!=null) {
						style = "width:"+property.getStringWidth();
					}
					if (property.getStyle(model)!=null) {
						style = style==null ? property.getStyle(model) : style + (style!=null?";":"") + property.getStyle(model);
					}
					if (style!=null) {
						item.add(new AttributeModifier("style", style));
					}
				}
			});
			
			WebMarkupContainer expanded = new WebMarkupContainer("expanded-row") {
				public boolean isVisible() {
					return isexpanded;
				}
			};
			expanded.setOutputMarkupId(true);
			expanded.add(new ValueEditor(getModel()));
			
			add(expanded);
			
		}
		public void edit(AjaxRequestTarget target) {
			if (!isexpanded) {
				expand(target);
			}
		}
		public void expand(AjaxRequestTarget target) {
			isexpanded = !isexpanded;
			if (isexpanded && expandedrow!=null && expandedrow!=ValueRowView.this) {
				((ValueRowView)expandedrow).collapse();
				target.add(expandedrow);
			}
			expandedrow = isexpanded ? ValueRowView.this : null;
			target.add(ValueRowView.this);
		}
		public void collapse() {
			isexpanded = false;
		}
	}
	
	public class ValuesGridFragment extends Fragment {
		
		public ValuesGridFragment(String id) {
			super(id, "values-grid-fragment", RelationGridEditor.this);
			WebMarkupContainer values = new WebMarkupContainer("values");
			values.setOutputMarkupId(true);
			
			WebMarkupContainer header = new WebMarkupContainer("header");
			header.add(new ListView<Property<?>>("column", ()-> getGridProperties()) {
				protected void populateItem(ListItem<Property<?>> item) {
					Property<?> property = item.getModelObject();
					String label = property.getLabel();
					
					item.add( (new Label("label", label)).setEscapeModelStrings(false));
					
					if (property.getStringWidth()!=null) {
						item.add(new AttributeModifier("style", "width:"+property.getStringWidth()));
					}
				}
			});
			values.add(header);
			
			WebMarkupContainer body = new WebMarkupContainer("body");
			body.add(new ListView<IModel<P>>("value", new ListModel<IModel<P>>(new Model<Panel>(RelationGridEditor.this), "values")) {
				protected void populateItem(ListItem<IModel<P>> item){
					item.add(new ValueRowView("value-view", item.getModelObject()));
					item.add(new AttributeModifier("data-id", "value_"+item.getIndex()));
				}
			});
			
			body.add(new SortableBehavior() {
				@Override
				public void onSort(AjaxRequestTarget target, List<String> ids) {
					sort(ids);
					target.add(RelationGridEditor.this);
				}
				@Override
				public String getItemSelector() {
					return "div.value";
				}
			});
			
			values.add(body);
			
			add(values);
			
			add(getCreationPanel());
		}
	}

	
	/** ------------------------------------------------------------------------------------------------------------------------ 
	 * 
	 * 
	 * @param property
	 * 
	 ---------------------------------------------------------------------------------------------------------------------------------*/
	
	public RelationGridEditor(String id) {
		this(id, id);
	}
	
	public RelationGridEditor(String id, String property) {
		super(id);
		
		this.property_id = property;
		
		setOutputMarkupId(true);
		
		Label label = new Label("label", getLabel());
		
		add(label);
		
		label.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (getDisposition()==null||getDisposition()==Disposition.HORIZONTAL)
					return "col-lg-2 control-label";
				else
					return "control-label";
			}
		}));
		
		
//		AjaxLink<Void> helpLink = new AjaxLink<Void>("help-info") {
//			public boolean isVisible() {
//				return helpInfo();
//			}
//
//			@Override
//			public void onClick(AjaxRequestTarget target) {
//				onHelp(target);
//			}
//		};
//
//		add(helpLink);

		
	}

	public boolean helpInfo() {
		return false;
	}
	
	public String getProperty() {
		return this.property_id; 
	}
	
	public List<IModel<P>> getValues() {
		return xvalues;
	}
	
	public IModel<String> getLabel() {
		return new StringResourceModel("property."+ this.property_id, RelationGridEditor.this, null);
	}
	
	@Override
	public void updateModel() {
		
		if (!updated) 
			return;
		
		if (ordered()) {
			List<P> values = new ArrayList<P>();
			for (IModel<P> model : getValues()) {
				values.add(model.getObject());
			}
			getPropertyModel().setObject(values);
		}
		else {
			Set<P> values = new HashSet<P>();
			for (IModel<P> model : getValues()) {
				values.add(model.getObject());
			}
			getPropertyModel().setObject(values);
		}
		
		setUpdatedPart(((Label)get("label")).getDefaultModelObjectAsString().toLowerCase());
		updated = false;
	}
	
	public boolean ordered() {
		return true;
	}
	
	@Override
	public void cancel() {
		setValues(getPropertyModel());
		updated = false;
	}
	
	@Override
	public void onBeforeRender() {
		if (getPropertyModel()==null) {
			setPropertyModel(new PropertyModel<Collection<P>>(getEditor().getModel(), getProperty()));
		}
		
		if (getValues().isEmpty() && !updated) {
			setValues(getPropertyModel());
		}
		super.onBeforeRender();
		if (get("horizontal-layout")==null) {
			WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
			layout.setOutputMarkupId(true); //OJO
			layout.add(new ValuesGridFragment("values"));
			add(layout);
			add(new ValuesGridFragment("values"));
			if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
				get("values").setVisible(false);
			}
			else {
				layout.setVisible(false);
			}
		}
		
		if (get("total")==null)   {
			Label total = new Label("total", "("+ String.valueOf( (getValues()!=null?getValues().size():"0"))+")");
			add(total);
		}
		
		if (get("help-info")==null)   {
			AjaxLink<Void> hi = new AjaxLink<Void>("help-info") {
				@Override
				public void onClick(AjaxRequestTarget target) {
							RelationGridEditor.this.onHelp(target);
				}
			};
			hi.setVisible(isHelpInfoVisible());
			add(hi);
		}

		
	}
	
	protected boolean isHelpInfoVisible() {
		return false;
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


	@Override
	public void onDetach() {
		
		for (IModel<P> model : getValues()) {
			model.detach();
		}
		if (propertymodel!=null) {
 			propertymodel.detach();
		}
		
		 
		super.onDetach();
	}
	
	protected void onHelp(AjaxRequestTarget target) {
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
	}
	
	protected WebMarkupContainer getCreationPanel() {
		return new CreationPanel("creation-panel") {
			public boolean isVisible() {
				return getEditor()!=null && getEditor().isEditionEnabled() && creationEnabled() && !isReadOnly();
			}
		};
	}
	
	protected Property<?> getKeyCache() {
		if (key == null) {
			key = getKey();
			if (key!=null)
				key.setKey(true);
		}
		return key;
	}
	
	protected Property<?> getKey() {
		return null;
	}
	
	protected boolean isValid(P value) {
		return true;
	}
	
	/**
	 * 
	 * @param model
	 */
	protected void setValues(IModel<Collection<P>> model) {
		
		List<IModel<P>> list = getValues();
		
		if (list==null) { 
			this.xvalues = new ArrayList<IModel<P>>();
			list=this.xvalues;
		}
		
		list.clear();
		
		if (model!=null) { 
			for (P value : model.getObject()) {
				try {
					if (isValid(value))
						getValues().add(new ValueModel(getModel(value)));
				} 
				catch (Exception e) {
					logger.error(e);
				}
			}
		}
		
		if (!ordered()) {
			Collections.sort(getValues(), new Comparator<IModel<P>>() {
				public int compare(IModel<P> model1, IModel<P> model2) {
					return RelationGridEditor.this.compare(model1, model2);
				}
			});
		}
	}
	
	protected boolean updated() {
		return updated;
	}
	
	protected int compare(IModel<P> model1, IModel<P> model2) {
		return compare(model1.getObject(), model2.getObject());
	}
	
	protected int compare(P value1, P value2) {
		try {
			return getStringValue(value1).compareTo(getStringValue(value2));
		} catch (Exception e) {
			logger.error(e);
			return 0;
		}
	}
	
	protected void setPropertyModel(IModel<Collection<P>> model) {
		propertymodel = model;
	}
	
	protected IModel<Collection<P>> getPropertyModel() {
		return propertymodel;
	}
	
	protected boolean deleteable(AjaxRequestTarget target, IModel<P> model) {
		return true;
	}
	
	protected void delete(IModel<P> model) {
		int index = getIndex(model);
		List<IModel<P>> list = getValues();
		list.remove(index);
		updated = true;
	}
	
	protected void up(IModel<P> model) {
		int index = getIndex(model);
		List<IModel<P>> list = getValues();
		IModel<P> upvalue = list.get(index-1);
		list.set(index-1, model);
		list.set(index, upvalue);
		updated = true;
	}
	
	protected void down(IModel<P> model) {
		int index = getIndex(model);
		List<IModel<P>> list = getValues();
		IModel<P> downvalue = list.get(index+1);
		list.set(index+1, model);
		list.set(index, downvalue);
		updated = true;
 	}
	
	protected void sort(List<String> ids) {
		int i = 0;
		
		List<IModel<P>> values = getValues();
		List<IModel<P>> values2 = new ArrayList<IModel<P>>();
		values2.addAll(values); 
		
		if (values.size()==ids.size()) {
			i =0;
			for (String id : ids) {
				int index = Integer.valueOf(id);
				values.set(i, values2.get(index));
				i++;
			}
			updated = true;
		}
	}
	
	protected void add(IModel<P> model) {
		String stringvalue = getStringValue(model.getObject());
		
		List<IModel<P>> list = getValues();
		for (IModel<P> valuemodel : list) {
			if (getStringValue(valuemodel.getObject()).equals(stringvalue)) {
				return;
			}
		}
		list.add(model);
		updated = true;	
		if (!ordered()) {
			Collections.sort(list, new Comparator<IModel<P>>() {
				public int compare(IModel<P> model1, IModel<P> model2) {
					return RelationGridEditor.this.compare(model1, model2);
				}
			});
		}
	}
	
	@SuppressWarnings("unchecked")
	protected IModel<P> getModel(P value) {
		if (value instanceof HibernateProxy) {
			value = (P)Hibernate.unproxy(value);
		}
		if (value instanceof Serializable) {
			return new SModel(value);
		}
		if (value instanceof Identifiable)
			return new com.novamens.wicket.model.ObjectModel<P>(value);
		Assert.isTrue(false, "no model");
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected void onUpdate(Property<?> property, Object propertyvalue, AjaxRequestTarget target) {
		if (property.getKey()) {
			if (!getPropertiesCache().isEmpty()) {
				P value = getNewValue();
				property.setValue(value, propertyvalue);
				add(new NewValueModel(value));
			}
			else {
				add(new NewValueModel((P)propertyvalue));
			}
			target.add(RelationGridEditor.this);
		}
		onUpdate(target);
	}
	
	protected P getNewValue() {
		return null;
	}
	
	protected List<Property<?>> getPropertiesCache() {
		if (properties==null || properties.isEmpty()) {
			properties = getProperties();
			if (properties==null)
				properties = new ArrayList<Property<?>>();
		}
		return properties;
	}
	
	protected List<Property<?>> getGridProperties() {
		return getProperties().stream().filter(p -> p.isVisible() && p.isGrid()).collect(Collectors.toList());
	}
	
	protected List<Property<?>> getVisibleProperties() {
		return getProperties().stream().filter(p -> p.isVisible()).collect(Collectors.toList());
	}
	
	protected List<Property<?>> getProperties() {
		return null;
	}
	
	protected int getIndex(IModel<P> model) {
		int index = getValues().indexOf(model);
		return index;
	}
	
	protected void custom(ContextMenuPanel<P> menu) {
		
	}
	
	protected String getStringValue(Object value) {
		String displayValue = "";
		JXPath path = new JXPath("label");
		try {
			List<Object> values = null;
			try {
				values = path.evaluateAll(value);
			}
			catch (IllegalAccessException e) {
				logger.debug(e);

			}
			if (values!=null && values.size()>0)
				displayValue = values.get(0).toString();
			else {
				path = new JXPath("displayName");
				try {
					values = path.evaluateAll(value);
				}
				catch (IllegalAccessException e) {
					logger.debug(e);
				}
				if (values!=null && values.size()>0) {
					displayValue = values.get(0).toString();
				}
				else {
					path = new JXPath("name");
					try {
						values = path.evaluateAll(value);
					}
					catch (IllegalAccessException e) {
						logger.debug(e);
					}
					if (values!=null && values.size()>0) {
						displayValue = values.get(0).toString();
					}
					else
						displayValue = value!=null ? value.toString() : "";
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		return displayValue;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	/**
	 *
	 */
	protected String getStringLabel(String resourceKey) {
		return ((new StringResourceModel(resourceKey, this, null)).getString());
	}
	
	protected void onValueClick(IModel<P> model) {
	}

	protected boolean isExtraInfo() {
		return false;
	}
	
	protected boolean linkView() {
		return true;
	}

	protected void onExtraInfoClick(IModel<P> model) {
	}
	
	protected boolean deleteEnabled(P value) {
		return true;
	}
	
	protected boolean creationEnabled() {
		return true;
	}
}
