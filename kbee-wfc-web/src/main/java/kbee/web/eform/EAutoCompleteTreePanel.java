package kbee.web.eform;

import java.util.HashMap;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.form.EAutoCompleteField;
import com.novamens.content.form.EClassifierModel;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.ValueUpdated;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.dom.Url;
import com.novamens.indexer.query.QuerySortOrder;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.form.KbeeEClassifierSource;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.service.ServiceLocator;
import com.novamens.text.TextTemplate;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.service.ApplicationSiteMapService;

@SuppressWarnings("serial")
public class EAutoCompleteTreePanel<T> extends EFieldPanel<EAutoCompleteField<T>> {
	private static final long serialVersionUID = 1L;
	
	public EAutoCompleteTreePanel(String id, EAutoCompleteField<T> field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		getContainer().add(
			new AutoCompleteFieldV5<T>("field", new FieldDataModel<EAutoCompleteField<T>, T>(getFieldModel(), getDataModel())) {
				@Override
				public IModel<String> getLabel() {
					return getField().getLabel()!=null ?
						new Model<String>(getField().getLabel()) :
						new Model<String>("");
				}
				@Override
				public IModel<String> getSubtitle() {
					return new Model<String>(getField().getSublabel() ); 
				}
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					EAutoCompleteTreePanel.this.onUpdate(target);
				}
				@Override
				protected void onKey(AjaxRequestTarget target, String jsKeycode) {
					fireScanAll(new EFocusEvent(target, getField()));
				}
				@Override
				public boolean hasFeedback() {
					return !EAutoCompleteTreePanel.this.getMessages().isEmpty();
				}
				@Override
				public String getMessage() {
					return hasFeedback() ? EAutoCompleteTreePanel.this.getMessages().get(0).toString() : null;
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
				public Disposition getDisposition() {
					return EAutoCompleteTreePanel.this.getDisposition();
				}
				@Override
				public boolean isInputEnabled() {
					return super.isInputEnabled() && 
						!getField().isReadOnly() &&
						isEditionEnabled() &&
						getField().isEnabled(getData()) && 
						getData().getForm().isEnabled() &&
						!getData().isSigned();
				}
				@Override
				public boolean isReadOnly() {
					return getField().isReadOnly(); // || !isInputEnabled();
				}
				@Override
				protected boolean isRequiredMark() {
					return getField().isRequired(); 
				}
				@Override 
				public String getHistoryKey() {
					return "eform-"+getField().getName(); 
				}
				@Override 
				public boolean isEnabledAdvancedOptions(){
					return true;
				}
				@Override
				public boolean isHelpVisible() {
					return getField().getModel().getMetainfoMessage()!=null || getField().getHelpText()!=null;
				}
				@Override
				protected void onUpdate(T oldvalue, T newvalue) {
					String label = getField().getLabel()!=null  ? getField().getLabel() : getField().getName();
					getEditor().setUpdatedField(new ValueUpdated(getData().getForm(), label, oldvalue, newvalue));
				}
				@Override 
				protected String serialize(IModel<T> model) {
					return getField().serialize(getFormObject(), model.getObject());
				}
				@Override 
				protected IModel<T> deserialize(String token) {
					return new IModel<T>() {
						public T getObject() {
							return getField().deserialize(getFormObject(), token);
						}
					};
				}
				@Override
				@SuppressWarnings("unchecked")
				protected String getValue(Suggestion suggestion) {
					T object = (T)((IModel<?>)suggestion.getObject()).getObject();
					return object != null 
						? EAutoCompleteTreePanel.this.getValue(object)
						: suggestion.getText();
				}
				@Override
				@SuppressWarnings("unchecked")
				protected String getInfo(Suggestion suggestion) {
					T object = (T)((IModel<?>)suggestion.getObject()).getObject();
					return suggestion.getObject() != null 
						? EAutoCompleteTreePanel.this.getInfo(object)
						: suggestion.getText();
				}
				protected String getTemplate() {
					return "function(data) {  "+
						"var value = '<div class=\"list-group-item\" style=\"border:none;\"><span class=\"list-group-item-heading\">' + data.value; " +
						"if (data.info) { value = value + '</span> - <span class=\"list-group-item-text\" >' + data.info + '</span></div>'; } else { value = value + '</span></div>' };" +
						"return value;}";
				}
				@Override
				protected IModel<String> getHelpText() {
					String info = getField().getModel().getMetainfoMessage(); 
					String text = getField().getHelpText();
					text = text==null ? info : text;
					return new Model<String>(text);
				}
				@Override
				protected boolean isSelectionBehavior() {
					return true;
				}
		});
		
		
		DataSet dataSet =  ((KbeeEClassifierSource)getDataSource()).getRelation().getClassifier().getDataSet();
		getContainer().add(new ETreeNodeSelector("selector", 
				new ObjectModel<DataSet>(dataSet),
				((EClassifierModel<?>)getFieldModel()).getAccessStrategy()) {
			@Override
			@SuppressWarnings("unchecked")
			protected void onSelect(AjaxRequestTarget target, DataSetMember member) {
				setValue((T)member);
				setTree(false);
			}
			@Override
			protected void onClose(AjaxRequestTarget target) {
				setTree(false);
				EAutoCompleteTreePanel.this.refresh(target);
			}
		});
		
		getAutoComplete().add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				String css = "efield";
				if (!getMessages().isEmpty()) {
					css = "eform-error";
				}
				return css;
			}
		})); 
		
		
		WebMarkupContainer menu = new WebMarkupContainer("menu-container") {
			public boolean isVisible() {
				return getDataSource().isReadable();
			}
		};
		menu.add(getMenu());
		getContainer().add(menu);
	}
	
	public void setValue(T value) {
		getData().setData(getField(), value!=null ? new ObjectModel<T>(value) : null);
		getAutoComplete().setValue(value);
		getAutoComplete().setStringValue(value!=null ? DisplayNameExtractor.get(value) : null); 
	}
	
	@SuppressWarnings("unchecked")
	public AutoCompleteFieldV5<T> getAutoComplete() {
		return (AutoCompleteFieldV5<T>)getInput();
	}
	
	protected EFormDataSource<T> getDataSource() {
		return getField().getModel().getDataSource(getFormObject());
	}
	
	@SuppressWarnings("unchecked")
	protected void setValues(List<?> values) {
		for (Object object : values) {
			setValue((T)object);
		}
		if (values.isEmpty()) {
			setValue(null);
		}
	}
	
	protected void setTree(boolean open) {
		getContainer().get("selector").setVisible(open);
	}
	
	protected Panel getMenu() {
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setTree(true);
					EAutoCompleteTreePanel.this.refresh(target);
				}	
				@Override
				public String getLabel() {	
					return "Tree";
				}
				@Override
				public boolean isEnabled() {
					return isEditionEnabled();
				}
			});
		
		for (EFormDataSource.Url url : getDataSource().getUrls()) {
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
						setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage(domurl.getPath(), parameters));
					}	
					@Override
					public boolean isEnabled() {
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
		
		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<Void>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
				@Override
				public boolean isVisible() {
					return  true;
				}
		});
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					EAutoCompleteTreePanel.this.refresh(target);
				}	
				@Override
				public String getLabel() {	
					return EAutoCompleteTreePanel.this.getLabelString("refresh");
				}
		});
		
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
	
	protected void refresh(AjaxRequestTarget target) {
		super.refresh(target);
		getAutoComplete().clearCache(target);
	}
}