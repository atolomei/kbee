package kbee.web.eform;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebSession;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.form.KbeeEDateTimeField;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
  

@SuppressWarnings("serial")
public class EValueViewer extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private IModel<EFormData> datamodel;
	private IModel<EFormField<?>> fieldmodel;
	
	public EValueViewer(String id, EFormField<?> field, IModel<EFormData> data) {
		super(id);
		setField(field);
		setData(data);
	}
	
	public void setField(EFormField<?> field) {
		this.fieldmodel = new ComponentModel<EFormField<?>>(field);
	}
	
	public EFormData getData() {
		return getDataModel().getObject();
	}
	
	public void setData(IModel<EFormData> model) {
		this.datamodel = model;
	}
	
	public IModel<EFormData> getDataModel() {
		return datamodel;
	}
	
	public IModel<EFormField<?>> getFieldModel() {
		return fieldmodel;
	}
	
	public EFormField<?> getField() {
		return getFieldModel().getObject();
	}
	
	public IModel<String> getLabel() {
		return getField().getLabel()!=null ?
			new Model<String>(getField().getLabel()) :
			new Model<String>("");	
	}
	
	protected String getCssClass() {
		String css = "";
		if (getField().getCssClass()!=null) {
			css += getField().getCssClass();
		}
		return "".equals(css.trim()) ? null : css.trim();
	}
	
	@Override
	public boolean isVisible() {
		return getField().isVisible(getData());
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		WebMarkupContainer container = new WebMarkupContainer("container");
		if (getCssClass()!=null) {
			container.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return getCssClass();
				}
			}));
		}
		container.add(new Label("label", getLabel()));
		container.add(new Label("value", getValueModel()));
		if (getValueCss()!=null)
			((Label)container.get("value")).add(new AttributeModifier("class",()->getValueCss()));
		((Label)container.get("value")).setEscapeModelStrings(false);
		add(container);
	}
	
	protected IModel<String> getValueModel() {
		return new Model<String>() {
			public String getObject() {
				return getDisplayValue(getDataModel().getObject().getData(getField()));
			}
		};
	}
	
	protected String getValueCss() {
		return null;
	}
	
	private String getDisplayValue(Object object) {
		String displayValue;
		if (object instanceof OffsetDateTime) {
			// el formato lo tiene que resolver el field
			if (getField() instanceof KbeeEDateTimeField) {
				String pattern = "es".equals(WebSession.get().getLocale().getLanguage()) ? "dd/MM/yyyy h:m a" : "MM/dd/yyyy h:m a";
				displayValue = DateTimeFormatter.ofPattern(pattern, WebSession.get().getLocale()).format((OffsetDateTime)object);
			}	
			else
				displayValue = ServiceLocator.getService(DateTimeService.class).getDateDisplayString((OffsetDateTime)object, getSessionUser()!=null ? getSessionUser().getLocale() : Locale.getDefault());
		}
		else {
			if (object instanceof String) {
				displayValue = (String)object;
			}
			else
			if (object instanceof Boolean) {
				displayValue = getLabelString(((Boolean)object).toString()+".label");
			}
			else {
				displayValue = DisplayNameExtractor.get(getDataModel().getObject().getData(getField()));
			}
		}
		return displayValue;
	}
	
	private KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
} 