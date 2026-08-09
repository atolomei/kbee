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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
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

import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.model.Multiplicity;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.JXPath;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.CollapsibleBehavior;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.FloatingBehavior7;


/**
 * @param <T>  Example: User
 * @param <P>  Example: Group
 */
@SuppressWarnings("serial")
public class RelationEditor<T, P> extends ObjectEditorPanel<T>  {
	private static final long serialVersionUID = 1L;
							
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RelationEditor.class.getName());

	private List<IModel<P>> xvalues = new ArrayList<IModel<P>>();
	
	private IModel<Collection<P>> propertymodel;
	private List<Property<?>> properties;
	private Property<?> key;
	private boolean updated	= false;
	private Disposition disposition;
	private String property_id;
	
	private IModel<String> help;
	private boolean isItemLink = true;	
	
	/** --------------------------------------------------
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

	
	/** --------------------------------------------------
	 * 
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
		public boolean isHelpInfo() {
			return false;
		}
		public V getValue(P value) {
			try {
				return (new PropertyModel<V>(value, getName())).getObject();
			}
			catch (RuntimeException e) {
				return null;
			}
		}
		
		public String getLabel() {
			return RelationEditor.this.getPropertyLabel(getName());
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
		public boolean isAutocomplete() {
			return false;
		}	
		public Field<V> getField(String id) {
			return getField(id, null);
		}
		@SuppressWarnings("unchecked")
		public Field<V> getField(String id, final IModel<P> model) {
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
						return RelationEditor.this.getStringValue(value);
					}
					
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						RelationEditor.this.onUpdate(Property.this, getValue(), target);
						if (getModel()!=null) {
							updated = true;
							getModel().setObject(getValue());
							if (getMultiplicity().equals(Multiplicity.M0N)) {
								//setValue(null);
							}
						}
						else {
							setValue(null);
						}
 						target.add(getParent().getParent().getParent().getParent());
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
						RelationEditor.this.onUpdate(Property.this, getValue(), target);
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
					public boolean isEnabledAdvancedOptions(){
						return true;
					}
				    protected boolean isSelectionBehavior() {
				    	return true;
				    }
					protected String getValue(Suggestion suggestion) {
						T object = (T)((IModel<?>)suggestion.getObject()).getObject();
						String value = DisplayNameExtractor.get(object);
						return value;
					}
					@Override
					protected String getInfo(Suggestion suggestion) {
						T object = (T)((IModel<?>)suggestion.getObject()).getObject();
						String info = suggestion.getObject() != null 
							? RelationEditor.this.getInfo(object)
							: null;
						return info;
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
					protected String getTemplate() {
						return "function(data) {  "+
							"var value = '<div class=\"list-group-item\" style=\"border:none;\"><span class=\"list-group-item-heading\">' + data.value; " +
							"if (data.info) { value = value + '</span> - <span class=\"list-group-item-text\" >' + data.info + '</span></div>'; } else { value = value + '</span></div>' };" +
							"return value;}";
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
 						RelationEditor.this.onUpdate(Property.this, getValue(), target);
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
					public Disposition getDisposition() {
						return Disposition.VERTICAL;
					}
				};
			}
			else {
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
				return (Field<V>)new TextField<V>(id, fieldmodel, false, getValidator()) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
 						RelationEditor.this.onUpdate(Property.this, getValue(), target);
						if (getModel()!=null) {
							updated = true;
 							getModel().setObject(getValue());
						}
						else {
							setValue(null);
						}	
						target.add(getParent().getParent().getParent().getParent());
					}	 
					@Override
					public boolean isHelpInfo() {
						return Property.this.isHelpInfo();
					}
					public void onHelp(AjaxRequestTarget target) {
						Property.this.onHelp(target);
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
		protected void onHelp(AjaxRequestTarget target) {
		}	
	};
	
	/** --------------------------------------------------
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
					} catch (Exception e) {
						logger.error(e);
					}
				}
				else {
					IModel<?> model = RelationEditor.this.getModel(value);
					values.put(getKeyCache(), model);
					model.detach();
				}
				value = null;
			}
		}
	}

	
	/** --------------------------------------------------
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

	
	/** --------------------------------------------------
	 * 
	 */
	public class ValueEditor extends Fragment {
		private IModel<P> model;
		public ValueEditor(IModel<P> model) {
			super("editor", "value-editor-fragment", RelationEditor.this);
			setModel(model);
			add(new ListView<Panel>("form", getFields()) {
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
				if (property.isVisible() && property.isVisible(getModel()))
				fields.add(property.getField("field", getModel()));
			}	
			return fields;
		}
		@Override
		@SuppressWarnings("unchecked")
		public void onDetach() {
			for (Panel panel : ((ListView<Panel>)get("form")).getList()) {
				panel.detach();
			}
			super.onDetach();
		}
	}

	/** --------------------------------------------------
	 * 
	 */

	public class CreationPanel extends Fragment {
		public CreationPanel(String id) {
			super(id, "creation-panel-fragment", RelationEditor.this);
			
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
						RelationEditor.this.add(new NewValueModel(value));
						target.add(RelationEditor.this);
					}
				});
				WebMarkupContainer field = new WebMarkupContainer("selector-field");
				field.setVisible(false);
				add(field);
			}
		}
	}
	
	/** --------------------------------------------------
	 * 
	 */

	public class ValueView extends Fragment {
		private IModel<P> model;
		public ValueView(String id, String markupid, IModel<P> model) {
			super(id, markupid, RelationEditor.this);
			
			setModel(model);
			
			setOutputMarkupId(true);
			
			if (!getPropertiesCache().isEmpty() || ordered()) {
				Panel menuPanel = getMenu(model);
				WebMarkupContainer menulink = new WebMarkupContainer("menulink") {
					@Override
					public boolean isVisible() {
						return getEditor().isEditionEnabled() && !isReadOnly();
					}
				};
				menuPanel.add(new FloatingBehavior7(menulink));
				add(menulink);
				add(menuPanel);
			}
			else {
				AjaxLink<?> removelink = new AjaxLink<Void>("menulink") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						RelationEditor.this.delete(ValueView.this.getModel());
						target.add(RelationEditor.this);
					}
					@Override
					public boolean isVisible() {
						return getEditor().isEditionEnabled() && !isReadOnly();
					}
				};
				
				removelink.add(new AttributeModifier("class", "fa-fw fal fa-times"));
				add(removelink);
				WebMarkupContainer menu = new WebMarkupContainer("menu");
				menu.setVisible(false);
				add(menu);
			}
			
			add(new ValueEditor(model));
		}
		public void setModel(IModel<P> model) {
			this.model = model;
		}
		public IModel<P> getModel() {
			return model;
		}
		public int getIndex() {
			return RelationEditor.this.getIndex(getModel());
		}
		@SuppressWarnings("unchecked")
		private Panel getMenu(IModel<P> model) {

			
			ContextMenuPanel<P> menu = new ContextMenuPanel<P>(model);
			
			
			menu.setPopper(false);
			
			menu.addItem(new MenuItemFactory<P>() {
				@Override
				public AbstractMenuItemPanelV5<P> getItem(String id) {
					return new AjaxMenuItemPanelV5<P>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							ValueView.this.get("editor").setVisible(true);
							target.add(ValueView.this);
						}
						@Override
						public String getLabel() {	
							return RelationEditor.this.getStringLabel("menu.edit");
						}
						@Override
						public boolean isVisible() {
							return !getPropertiesCache().isEmpty();
						}
						
						@Override
						public boolean isEnabled() {
								return getEditor().isEditionEnabled();
						}
						
					};
				}
			});

			menu.addItem(new MenuItemFactory<P>() {
				@Override
				public AbstractMenuItemPanelV5<P> getItem(String id) {
					return new AjaxMenuItemPanelV5<P>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							RelationEditor.this.up((((ContextMenuPanel<P>)ValueView.this.get("menu"))).getModel());
							target.add(RelationEditor.this);
						}
						@Override 
						public String getLabel() {	
							return RelationEditor.this.getStringLabel("menu.up");
						}
						@Override
						public boolean isVisible() {
							return ValueView.this.getIndex()>0;
						}
						@Override
						public boolean isEnabled() {
							return getEditor().isEditionEnabled();
						}
					};
				}
			});
			
			menu.addItem(new MenuItemFactory<P>() {
				@Override
				public AbstractMenuItemPanelV5<P> getItem(String id) {
					return new AjaxMenuItemPanelV5<P>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							RelationEditor.this.down((((ContextMenuPanel<P>)ValueView.this.get("menu"))).getModel());
							target.add(RelationEditor.this);
						}
						@Override
						public String getLabel() {	
							return RelationEditor.this.getStringLabel("menu.down");
						}
						@Override
						public boolean isVisible() {
							return ValueView.this.getIndex()<getValues().size()-1;
						}
						@Override
						public boolean isEnabled() {
								return getEditor().isEditionEnabled();
						}

					};
				}
			});
			
			menu.addItem(new MenuItemFactory<P>() {
				@Override
				public AbstractMenuItemPanelV5<P> getItem(String id) {
					return new SeparatorMenuItemPanelV5<P>(id) {
						@Override
						public String getCssClass() {
							return "divider";
						}
						@Override
						public boolean isVisible() {
							return getValues().size()>1 && creationEnabled();
						}
					};
				}
			});
			
			menu.addItem(new MenuItemFactory<P>() {
				@Override
				public AbstractMenuItemPanelV5<P> getItem(String id) {
					return new AjaxMenuItemPanelV5<P>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							IModel<P> model = (((ContextMenuPanel<P>)ValueView.this.get("menu"))).getModel(); 
							if (RelationEditor.this.deleteable(target, model)) {
								RelationEditor.this.delete(model);
								target.add(RelationEditor.this);
								RelationEditor.this.onUpdate(target);
							}
						} 
						@Override
						public String getLabel() {
							return RelationEditor.this.getStringLabel("menu.delete");
						}
						@Override
						public boolean isVisible() {
							return  creationEnabled();
						}
						@Override
						public boolean isEnabled() {
							return getEditor().isEditionEnabled() && creationEnabled() && deleteEnabled(getModelObject());
						}

					};
				}
			});
			custom(menu);
			return menu;
		}
	}
	
	
	/** --------------------------------------------------
	 * 
	 */


	public class ValueLinkView extends ValueView {
		public ValueLinkView(String id, IModel<P> model) {
			super(id, "value-linkview-fragment", model);
			
			
			
			
			Link<Void> lnk = new Link<Void>("value-link") {
				@Override
				public void onClick() {
					RelationEditor.this.onValueClick(ValueLinkView.this.getModel());
				}
			};
			lnk.add(new AttributeModifier("target",   getTarget()));
			add(lnk);
			lnk.setVisible(RelationEditor.this.isItemLink());
			
			
			
			WebMarkupContainer nol= new WebMarkupContainer("value-nolink");
			nol.setVisible(!RelationEditor.this.isItemLink());
			nol.add((new Label("title", new Model<String>() {
				public String  getObject() {
					return getTitle(ValueLinkView.this.getModel().getObject());
				};
			})).setEscapeModelStrings(false));
			add(nol);
			
			
			lnk.add((new Label("title", new Model<String>() {
				public String  getObject() {
					return getTitle(ValueLinkView.this.getModel().getObject());
				};
			})).setEscapeModelStrings(false));

			Label tx=new Label("text", new Model<String>() {
					public String  getObject() {
						return getText(ValueLinkView.this.getModel().getObject());
					};
				});
			tx.setVisible(isItemTextVisible());
			tx.setEscapeModelStrings(false);

			
			add(tx);
			
			Link<Void> extrainfo = new Link<Void>("extra-link") {
				@Override
				public void onClick() {
					RelationEditor.this.onExtraInfoClick(ValueLinkView.this.getModel());
				}
				@Override
				public boolean isVisible() {
					return isExtraInfo() && !getEditor().isEditionEnabled();
				}
			};
			extrainfo.add(new AttributeModifier("target", "_blank"));
			add(extrainfo);
			
			if (!getPropertiesCache().isEmpty()) {
				get("value-link:title").add(new AttributeModifier("style", "cursor:pointer;"));
				get("value-link:title").add(new AttributeModifier("class", "editable list-group-item-heading"));
				add(new CollapsibleBehavior(get("value-link:title"), get("editor")));
			}
		}
	}

	
	/** --------------------------------------------------
	 * 
	 */

	public class ValueTitleView extends ValueView {
		
		public ValueTitleView(String id, IModel<P> model) {
			super(id, "value-titleview-fragment", model);
			
			Label t = new Label("title", new Model<String>() {
				public String  getObject() {
					return getTitle(ValueTitleView.this.getModel().getObject());
				};
			});
			
			t.setEscapeModelStrings(false);
			
			add(t);
					
				Label tex=new Label("text", new Model<String>() {
										public String  getObject() {
											return getText(ValueTitleView.this.getModel().getObject());
										};
					});
		
				tex.setVisible(isItemTextVisible());
				tex.setEscapeModelStrings(false);
				add(tex);


			if (!getPropertiesCache().isEmpty()) {
				get("title").add(new AttributeModifier("style", "cursor:pointer;"));
				get("title").add(new AttributeModifier("class", "editable list-group-item-heading"));
				add(new CollapsibleBehavior(get("title"), get("editor")));
			}
		}
	}
	
	/** --------------------------------------------------
	 * 
	 */

	public class ValuesFragment extends Fragment {
		
		public ValuesFragment(String id) {
			super(id, "values-fragment", RelationEditor.this);
			WebMarkupContainer values = new WebMarkupContainer("values");
			values.add(new ListView<IModel<P>>("value", new ListModel<IModel<P>>(new Model<Panel>(RelationEditor.this), "values")) {
 				protected void populateItem(ListItem<IModel<P>> item){
 					if (linkView()) { 
 						item.add(new ValueLinkView("value-view", item.getModelObject()));
 					}
 					else {
 						item.add(new ValueTitleView("value-view", item.getModelObject()));
 					}
				}
			});

			values.setOutputMarkupId(true);
			values.add(getCreationPanel());

			values.add(new WebMarkupContainer("empty-panel") {
				public boolean isVisible() {
	 				return getEditor()!=null && !getEditor().isEditionEnabled() && getValues().isEmpty();
				}
			});
			
			add(values);
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------ 
	 * @param property
	 ---------------------------------------------------------------------------------------------------------------------------------*/
	
	public RelationEditor(String id) {
		this(id, id);
	}
	
	
	public String getTarget() {
		return 	"_blank";
	}


	public boolean isItemTextVisible() {
		return true;
	}
	
	
	
	
	public boolean isItemLink() {
		return isItemLink;
	}

	public RelationEditor(String id, String property) {
		super(id);
		
		this.property_id = property;
		
		setOutputMarkupId(true);
		
		
		WebMarkupContainer wc= new WebMarkupContainer("label-container") {
			
			public boolean isVisible() {
				return (getLabel()!=null && getLabel().getObject().length()>0) ||   helpInfo();
			}
		}; 
		add(wc);
				
		
		Label label = new Label("label", getLabel());
		label.setEscapeModelStrings(false);
		label.add(new AttributeModifier("for", new Model<String>() {
			public String getObject() {
				return RelationEditor.this.get("values").getMarkupId();
			}
		}));
		
		wc.add(label);
		
		label.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (getDisposition()==null||getDisposition()==Disposition.HORIZONTAL)
					return "col-lg-2 control-label";
				else
					return "control-label";
			}
		}));
		
		
		AjaxLink<Void> helpLink = new AjaxLink<Void>("help-info") {
			public boolean isVisible() {
				return helpInfo();
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onHelp(target);
			}
		};

		wc.add(helpLink);

		
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
		return new StringResourceModel("property."+ this.property_id, RelationEditor.this, null);
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
		
		setUpdatedPart(getPart());
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
	
	public void onInitialize() {
		super.onInitialize();
	}
	
	
	@Override
	public void onBeforeRender() {
		
		if (getPropertyModel()==null) {
			setPropertyModel(new PropertyModel<Collection<P>>(getEditor().getModel(), getProperty()));
		}
		
		if (getHelp()!=null) {
			Label la=new Label("help", getHelp());
			la.setVisible(true);
			la.setEscapeModelStrings(false);
			addOrReplace(la);
		}
		else 
			addOrReplace(new Label("help", "").setVisible(false));

		
		if (getValues().isEmpty() && !updated) {
			setValues(getPropertyModel());
		}
		super.onBeforeRender();
		if (get("horizontal-layout")==null) {
			WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
			layout.setOutputMarkupId(true); //OJO
			layout.add(new ValuesFragment("values"));
			add(layout);
			add(new ValuesFragment("values"));
			if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
				get("values").setVisible(false);
			}
			else {
				layout.setVisible(false);
			}
		}
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

	public IModel<String> getHelp() {
		return this.help;
	}
	
	public void setHelp( IModel<String> h) {
		this.help=h;
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
	
	protected String getInfo(T object) {
			if (object instanceof DataSetMember) {
				ExtractionRule rule = ((DataSetMember)object).getDataSet().getSublineRule();
				if (rule!=null) {
					String label = (String)rule.extract((DataSetMember)object);
					return label;
				}
			}
		return "";
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
	
	protected String getTitle(P value) {
		StringBuilder title = new StringBuilder();
		
		if (!getPropertiesCache().isEmpty()) {
			if (getKeyCache()!=null) {
				title.append(getStringValue(getKeyCache().getValue(value)));
			}
			else {
				for (Property<?> property : getPropertiesCache()) {
					if (property.getTitle()) {
						title.append(getStringValue(property.getValue(value)));
					}
				}
			}
		}
		else
			title.append(getStringValue(value));
		return title.toString();
	}
	
	protected String getText(P value) {
		StringBuilder text = new StringBuilder();
		for (Property<?> property : getPropertiesCache()) {
			if (property.getTitle()) {
				if (text.length()!=0) 
					text.append(".  ");
				if (property.isBoolean())
					text.append("<span class=\"label\">" + property.getLabel()+"</span> : <span class=\"highlight-"+property.getValue(value)+"\" >"+getStringValue(property.getValue(value)) +"</span>");
				else
					text.append("<span class=\"label\">" + property.getLabel() + "</span> :  <span class=\"highlight\">"+ getStringValue(property.getValue(value)) +"</span>");
			}
		}
		return text.toString();
	}
	
	protected boolean isValid(P value) {
		return value!=null;
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
					return RelationEditor.this.compare(model1, model2);
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
					return RelationEditor.this.compare(model1, model2);
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
		if (propertyvalue!=null && property.getKey()) {
			if (!getPropertiesCache().isEmpty()) {
				P value = getNewValue();
				property.setValue(value, propertyvalue);
				add(new NewValueModel(value));
			}
			else {
				add(new NewValueModel((P)propertyvalue));
			}
			target.add(RelationEditor.this);
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
		
		if (value instanceof Boolean) {
			return new StringResourceModel(value.toString(), RelationEditor.this, null).getObject();
		}
		
			
		String displayValue = "";
		JXPath path = new JXPath("label");
		try {
			List<Object> values = null;
			try {
				values = path.evaluateAll(value);
			}
			catch (IllegalAccessException e) {
			}
			if (values!=null && values.size()>0)
				displayValue = values.get(0).toString();
			else {
				path = new JXPath("displayName");
				try {
					values = path.evaluateAll(value);
				}
				catch (IllegalAccessException e) {
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
	
	protected String getPropertyLabel(String property_name) {
		return (new StringResourceModel("property."+property_name, RelationEditor.this, null)).getObject();
	}
	
	
	protected void setUpdated(boolean b) {
		updated=b;
	}
	
	protected String getPart() {
		return ((Label)get("label-container:label")).getDefaultModelObjectAsString().toLowerCase();
	}
	
	protected boolean isUpdated() {
		return updated;
	}
}
