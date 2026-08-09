package kbee.web.eform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EListField;
import com.novamens.content.form.ValueAdded;
import com.novamens.content.form.ValueRemoved;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.service.UrlService;
import com.novamens.dom.Url;
import com.novamens.indexer.query.QuerySortOrder;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.service.ServiceLocator;
import com.novamens.text.TextTemplate;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

 import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.service.ApplicationSiteMapService;

@SuppressWarnings("serial")
public class EListPanel<T> extends EFieldPanel<EListField<T>> {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<T>> values = new ArrayList<IModel<T>>();
	private T value;
	boolean helpvisible = false;

	
	/**
	 * 
	 * 
	 */
	public class ControlFragment extends Fragment {
	
		WebMarkupContainer sub_container, list_container;
		Label subtitle;
		
		public ControlFragment(String id) {
			super(id, "control-fragment", EListPanel.this);
			

			sub_container = new  WebMarkupContainer("subtitle-container");
			add(sub_container);
			sub_container.setVisible(  getField().getSublabel()!=null );
			subtitle = new Label("subtitle", getField().getSublabel());
			subtitle.setEscapeModelStrings(false);
			sub_container.add(subtitle);
			
			list_container = new  WebMarkupContainer("list-container") {
				
				public boolean isVisible() {
					return getValues().size()>0;
				}
			};
			add(list_container);
			
			list_container.add(new ListView<IModel<T>>("value", () -> getValues()) {
				public void populateItem(ListItem<IModel<T>> item) {
					WebMarkupContainer menu = new WebMarkupContainer("menu-container") {
						public boolean isVisible() {
							return !getField().isReadOnly() && 
							getField().isEnabled(getData()) &&
							isEditionEnabled() &&
							getData().getForm().isEnabled();
						}
					};
					menu.setOutputMarkupId(true);
					menu.add(getMenu(item.getModelObject(), item.getIndex()));
					item.add(menu);
					
					item.add(new Label("label", getValue(item.getModelObject().getObject())));
					Label infolabel = new Label("info", getInfo(item.getModelObject().getObject())) {
						public boolean isVisible() {
							return getInfo(item.getModelObject().getObject())!=null;
						}
					};
					infolabel.setEscapeModelStrings(false);
					item.add(infolabel);
				}
			});
			
			
			add(new AutoCompleteFieldV5<T>("field", new PropertyModel<T>(EListPanel.this, "value")) {
				@Override
				public IModel<String> getLabel() {
					return new Model<String>("");
				} 
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					addValue(getValue());
					setValue(null);
					setSuggestion(null);
					setStringValue(null);
					target.add(getContainer());
					setFocus(target);
					fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
				}
				@Override
				public List<Suggestion> getSuggestions(String pattern) {
					return getField().getChoicesSource(getFormObject()).getValues(pattern);
				}
				@Override
				public List<Suggestion> getSuggestions(String pattern, int maxResults, QuerySortOrder querySortOrder) {
					final HashMap<String, Object> parameters = new HashMap<>();
					parameters.put("maxResults",maxResults);
					parameters.put("querySortOrder",querySortOrder);
					return getField().getChoicesSource(getFormObject()).getValues(pattern, parameters);
				}
				@Override
				protected void onKey(AjaxRequestTarget target, String jsKeycode) {
					fireScanAll(new EFocusEvent(target, getField()));
				}
				@Override
				public boolean isInputEnabled() {
					return super.isInputEnabled() && 
						!getField().isReadOnly() && 
						isEditionEnabled() &&
						getField().isEnabled(getData()) && 
						getData().getForm().isEnabled();
				}
				@Override 
				public boolean isVisible() {
					return isInputEnabled();
				}
				@Override 
				public Disposition getDisposition() {
					return Disposition.VERTICAL;
				}
				@Override 
				public boolean isEnabledAdvancedOptions(){
					return true;
				}
				@Override 
				public String getHistoryKey() {
					return "eform-"+getField().getName(); 
				}
				@Override
				protected boolean isRequiredMark() {
					return getField().isRequired();
				}
				@Override
				public boolean hasFeedback() {
					return !EListPanel.this.getMessages().isEmpty();
				}
				@Override
				public String getMessage() {
					return hasFeedback() ? EListPanel.this.getMessages().get(0).toString() : null;
				}
				@Override
				@SuppressWarnings("unchecked")
				protected String getValue(Suggestion suggestion) {
					T object = (T)((IModel<?>)suggestion.getObject()).getObject();
					return object != null 
						? EListPanel.this.getValue(object)
						: suggestion.getText();
				}
				@Override
				@SuppressWarnings("unchecked")
				protected String getInfo(Suggestion suggestion) {
					T object = (T)((IModel<?>)suggestion.getObject()).getObject();
					String info = suggestion.getObject() != null 
						? EListPanel.this.getInfo(object)
						: null;
					return info;
				}
				protected String getTemplate() {
					return "function(data) {  "+
						"var value = '<div class=\"list-group-item\" style=\"border:none;\"><span class=\"list-group-item-heading\">' + data.value; " +
						"if (data.info) { value = value + '</span> - <span class=\"list-group-item-text\" >' + data.info + '</span></div>'; } else { value = value + '</span></div>' };" +
						"return value;}";
				}
			});
		}
		
		private Panel getMenu(IModel<T> model, int index) {
			
			ContextMenuPanel<T> menu = new ContextMenuPanel<T>(model);
			
			menu.setPopper(false);
			
			menu.addItem(id ->
				new LinkMenuItemPanel<T>(id) {
					@Override
					public void onClick() {
						if (getModelObject() instanceof DataSetMember) {
							setResponsePage(new RedirectPage(((DataSetMember)getModelObject()).getService(UrlService.class).getUrl()));
						}
					}	
					@Override
					public boolean isVisible() {
						return getModelObject() instanceof DataSetMember;
					}
					@Override
					public String getLabel() {	
						return getLabelString("menu.open");
					}
					@Override
					public String getTarget() {
						return "_blank";
					}
			});
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<T>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						upValue(getModelObject());
						fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
						target.add(getContainer());
						setFocus(target);
					}	
					@Override
					public boolean isVisible() {
						return index>0;
					}
					@Override
					public String getLabel() {	
						return getLabelString("menu.up");
					}
			});
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<T>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						downValue(getModelObject());
						fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
						target.add(getContainer());
						setFocus(target);
					}	
					@Override
					public boolean isVisible() {
						return index<getValues().size()-1;
					}
					@Override
					public String getLabel() {	
						return getLabelString("menu.down");
					}
			});
			
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<T>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
			});
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<T>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						removeValue(getModelObject());
						fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
						target.add(getContainer());
						setFocus(target);
					}	
					@Override
					public boolean isEnabled() {
						return true;
					}
					@Override
					public String getLabel() {	
						return getLabelString("menu.delete");
					}
			});
			

			
			return menu;
		}	
	}	

	
	/**
	 * 
	 * 
	 * 	
	 * @param id
	 * @param field
	 * @param data
	 */
	public EListPanel(String id, EListField<T> field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setValues();
		
		WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
		layout.add(new ControlFragment("control"));
		getContainer().add(new ControlFragment("control"));
		getContainer().add(layout);
		
		WebMarkupContainer menu = new WebMarkupContainer("menu-container");
		menu.add(getMenu());
		getContainer().add(menu);
		
		getContainer().add(new Label("label", new Model<String>() {
			public String getObject() {
				return getField().getLabel()!=null ?
					getField().getLabel() :
					"";	
			}
		}));
		
		addHelpLink();
		
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			getContainer().get("control").setVisible(false);
		}
		else {
			layout.setVisible(false);
		}
	}
	
	public void setFocus(AjaxRequestTarget target) {
		super.setFocus(target);
	}
	
	@Override
	public void update(Classificable classificable) {
		getField().set(classificable, getData());
	}
	
	public Disposition getDisposition() {
		return Disposition.HORIZONTAL;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
	public List<IModel<T>> getValues() {
		return values;
	}
	
	public void addValue(T member) {
		//getField().validate(new KbeeEValidatable(getData(), getField())); 
		if (member!=null && !contains(member)) {
			this.values.add(new ObjectModel<T>(member));
			getData().setData(getField(), getValues());
			getField().validate(new KbeeEValidatable(getData(), getField()));
			if (!hasErrorMessage()) {
				setUpdatedField(new ValueAdded(getData().getForm(), getLabel(), member));
			}
		}
		//getData().setData(getField(), getValues());
	}
	
	public void removeValue(T member) {
		for (IModel<T> model : values) {
			if (model.getObject().equals(member)) {
				values.remove(model);
				setUpdatedField(new ValueRemoved(getData().getForm(), getLabel(), model.getObject()));
				break;
			}
		}
		getData().setData(getField(), getValues());
	}
	
	public void upValue(T member) {
		int index = 0;
		for (IModel<T> model : values) {
			if (model.getObject().equals(member) && index>0) {
				IModel<T> previous = values.get(index-1);
				values.set(index-1, model);
				values.set(index, previous);
				setUpdatedField(new ValueRemoved(getData().getForm(), getLabel(), model.getObject()));
				break;
			}
			index++;
		}
		getData().setData(getField(), getValues());
	}
	
	public void downValue(T member) {
		int index = 0;
		for (IModel<T> model : values) {
			if (model.getObject().equals(member) && index<values.size()-1) {
				IModel<T> next = values.get(index+1);
				values.set(index+1, model);
				values.set(index, next);
				setUpdatedField(new ValueRemoved(getData().getForm(), getLabel(), model.getObject()));
				break;
			}
			index++;
		}
		getData().setData(getField(), getValues());
	}
	
	public boolean contains(T member) {
		for (IModel<T> model : values) {
			if (model.getObject().equals(member)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Field<?> getInput() {
		return (Field<?>)getContainer().get("horizontal-layout:control:field");
	}
	
	public String getLabel() {
		return getField().getLabel()!=null ?
			getField().getLabel() :
			"";	
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		addFeedbackPanel();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		for (IModel<T> model : values) 
			model.detach();
		
		this.value = null;
	}
	
	protected EFormDataSource<T> getDataSource() {
		return getField().getModel().getDataSource(getFormObject());
	}
	
	protected Panel getMenu() {
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		for (com.novamens.content.form.EFormDataSource.Url url : getDataSource().getUrls()) {
			final Url domurl =  url.getUrl();
			final String label =  url.getLabel();
			menu.addItem(id ->
				new LinkMenuItemPanel<Void>(id) {
					@Override
					public void onClick() {
						PageParameters parameters = null;
						if (domurl.getParameters()!=null) {
							parameters = new PageParameters();
							for (String key : domurl.getParameters().keySet()) {
								parameters.add(key, domurl.getParameters().get(key));
							}
						}
						setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage(domurl.getPath(), parameters));
					}	
					@Override
					public boolean isEnabled() {
						// permisos??
						return true;
					}
					@Override
					public String getLabel() {	
						return label;
					}
					@Override
					public String getTarget() {
						return "_blank";
					}
			});
		}
		
		return menu;
	}
	
	protected String getValue(T object) {
		if (getField().getValueTemplate()!=null) {
			TextTemplate template = new KbeeTextTemplate(getField().getValueTemplate());
			String label = template.process(object);
			return label;
		}
		else {
			return DisplayNameExtractor.get(object);
		}
	}
	
	protected String getInfo(T object) {
		if (getField().getInfoTemplate()!=null) {
			TextTemplate template = new KbeeTextTemplate(getField().getInfoTemplate());
			String label = template.process(object);
			return label;
		}
		else {
			if (object instanceof DataSetMember) {
				ExtractionRule rule = ((DataSetMember)object).getDataSet().getSublineRule();
				if (rule!=null) {
					String label = (String)rule.extract((DataSetMember)object);
					return label;
				}
			}
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setValues(List<?> values) {
		List<IModel<T>> model = new ArrayList<IModel<T>>();
		for (Object value : values) {
			setUpdatedField(new ValueAdded(getData().getForm(), getLabel(), value));
			model.add(new ObjectModel<T>((T)value));
		}
		getData().setData(getField(), model);
		setValues();
	}
	
	@SuppressWarnings("unchecked")
	protected void setValues() {
		List<?> values = (List<?>)getData().getData(getField());
		this.values.clear();
		if (values!=null) {
			for (Object value : values) {
				this.values.add(new ObjectModel<T>((T)value));
			}
		}
	}
	
	protected void addHelpLink() {
		IModel<String> help = new Model<String>() {
			public String getObject() {
				return getHelpText();
			}
		};
		
		
		AjaxLink<T>  hl = new AjaxLink<T>("help-link-choice") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				helpvisible = !helpvisible;
				target.add(getContainer());
			}
			public boolean isVisible() {
				return getHelpText()!=null; 
			}
		};
		
		Label la=new Label("helpstr", getLabel("help"));
		la.setEscapeModelStrings(false);
		hl.add(la);
		getContainer().add(hl);
		hl.setVisible(false);
		if (help!=null && help.getObject()!=null) {
			Label label=new Label ("help", help) {
				public boolean isVisible() {
					return helpvisible;
				}
			};
			label.setEscapeModelStrings(false);
			getContainer().add(label);
		}
		else
			getContainer().add((new Label ("help", "")).setVisible(true));
	}
}