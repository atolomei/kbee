package kbee.web.eform;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.form.EComboField;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.ValueUpdated;
import com.novamens.dom.Url;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.ChoiceFieldWithHistory;
import kbee.web.service.ApplicationSiteMapService;

@SuppressWarnings("serial")
public class EComboPanel<T> extends EFieldPanel<EComboField<T>> {
 	private static final long serialVersionUID = 1L;
	
	private boolean isSelectionEnabled = true;
	
	public EComboPanel(String id, EComboField<T> field, IModel<EFormData> data) {
		super(id, field, data);
		setOutputMarkupId(true);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (getContainer().get("field")==null) 
		getContainer().add(new ChoiceFieldWithHistory<T>("field", 
				new FieldDataModel<EComboField<T>, T>(getFieldModel(), getDataModel()),
				getChoicesModel()) {
			
			@Override
			public IModel<String> getLabel() {
				return getField().getLabel()!=null ?
					new Model<String>(getField().getLabel()) :
					new Model<String>("");	
			}
			@Override
			public IModel<String> getSubtitle() {
				return getField().getSublabel()!=null ?
					new Model<String>(getField().getSublabel()) :
					null;	
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				updateModel();
				EComboPanel.this.onUpdate(target);
				fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
			}
			@Override
			public boolean isHelpVisible() {
				return getField().getModel().getMetainfoMessage()!=null || getField().getHelpText()!=null;
			}
			@Override 
			public String getHistoryKey() {
				return "eform-"+getField().getName(); 
			}
			@Override
			public boolean hasFeedback() {
				return !EComboPanel.this.getMessages().isEmpty();
			}
			@Override
			public String getMessage() {
				return hasFeedback() ? EComboPanel.this.getMessages().get(0).toString() : null;
			}
			@Override
			public boolean isInputEnabled() {
				return super.isInputEnabled() && 
					!getField().isReadOnly() && 
					isSelectionEnabled() && 
					isEditionEnabled() &&
					getField().isEnabled(getData()) && 
					getData().getForm().isEnabled() &&
					!getData().isSigned();
			}
			@Override
			public boolean isReadOnly() {
				return getField().isReadOnly();  
			}
			@Override
			protected void onUpdate(T oldvalue, T newvalue) {
				String label = getField().getLabel()!=null  ? getField().getLabel() : getField().getName();
				setUpdatedField(new ValueUpdated(getData().getForm(), label, oldvalue, newvalue));
			}
			@Override
			protected void onKey(AjaxRequestTarget target, String jsKeycode) {
				fireScanAll(new EFocusEvent(target, getField()));
			}
			@Override
			protected IModel<String> getHelpText() {
				String info = getField().getModel().getMetainfoMessage(); 
				String text = getField().getHelpText();
				text = text==null ? info : text;
				return new Model<String>(text);
			}
			@Override
			protected boolean isRequiredMark() {
				return getField().isRequired(); 
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
		});
		
		WebMarkupContainer menu = new WebMarkupContainer("menu-container") {
			public boolean isVisible() {
				return getDataSource().isReadable();
			}
		};
		menu.add(getMenu());
		getContainer().add(menu);
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
					EComboPanel.this.refresh(target);
				}	
				@Override
				public String getLabel() {	
					return EComboPanel.this.getLabelString("refresh");
				}
		});

		
		return menu;
	}


	@SuppressWarnings("unchecked")
	public void setValue(T value) {
		getData().setData(getField(), value!=null ? new ObjectModel<T>(value) : null);
		((ChoiceField<T>)get("container:field")).setValue(value);
	}
	
	public boolean isSelectionEnabled() {
		return isSelectionEnabled;
	}
	
	public void setSelectionEnabled(boolean value) {
		isSelectionEnabled = value;
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
	
	protected IModel<List<T>> getChoicesModel() {
		return new IModel<List<T>>() {
			public List<T> getObject() {
				return getField().getChoicesSource(getFormObject()).getValues();
			}
		};
	}
	
	
	protected EFormDataSource<T> getDataSource() {
		return getField().getModel().getDataSource(getFormObject());
	}
}