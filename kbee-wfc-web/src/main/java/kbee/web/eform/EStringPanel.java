package kbee.web.eform;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.ValueUpdated;
import com.novamens.kbee.content.form.KbeeEStringField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class EStringPanel extends  EFieldPanel<KbeeEStringField> {
	private static final long serialVersionUID = 1L;
	
	public EStringPanel(String id, KbeeEStringField field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		getContainer().add(new TextField<String>("field", new FieldDataModel<KbeeEStringField, String>(getFieldModel(), getDataModel())) {
			@Override
			public IModel<String> getLabel() {
				return getField().getLabel()!=null ?
					new Model<String>(getField().getLabel()) :
					new Model<String>("");	
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				EStringPanel.this.onUpdate(target);
				updateModel();
				fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
			}
			@Override
			public boolean isRequired() {
				return getField().isRequired();
			}
			@Override
			public boolean isReadOnly() {
				 return getField().isReadOnly();
			}
			@Override
			public void onError(final Serializable message) {
				EStringPanel.this.addMessage(message);
			}
			@Override
			public boolean isInputEnabled() {
				return isEditionEnabled() && !getField().isReadOnly() && !getData().isSigned();
			}
			@Override
			public Disposition getDisposition() {
				return EStringPanel.this.getDisposition();
			}
			@Override
			public boolean isHelpVisible() {
				return getField().getModel().getMetainfoMessage()!=null || getField().getHelpText()!=null;
			}
			@Override
			public boolean hasFeedback() {
				return !EStringPanel.this.getMessages().isEmpty();
			}
			@Override
			public String getMessage() {
				return hasFeedback() ? EStringPanel.this.getMessages().get(0).toString() : null;
			}
			@Override
			protected boolean isRequiredMark() {
				return getField().isRequired(); 
			}
			@Override
			protected void onKey(AjaxRequestTarget target, String jsKeycode) {
				fireScanAll(new EFocusEvent(target, getField()));
			}
			@Override
			protected void onUpdate(String oldvalue, String newvalue) {
				String label = getField().getLabel()!=null  ? getField().getLabel() : getField().getName();
				setUpdatedField(new ValueUpdated(getData().getForm(), label, oldvalue, newvalue));
			}
			@Override
			protected IModel<String> getHelpText() {
				String info = getField().getModel().getMetainfoMessage(); 
				String text = getField().getHelpText();
				text = text==null ? info : text;
				return new Model<String>(text);
			}
		});
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setValues(List<?> values) {
		String value = !values.isEmpty() ? values.get(0).toString() : null;
		getData().setData(getField(), value);
		((TextField<String>)get("container:field")).setValue(value);
	}
} 