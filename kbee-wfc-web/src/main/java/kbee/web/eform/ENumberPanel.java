package kbee.web.eform;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.ValueUpdated;
import com.novamens.kbee.content.form.KbeeENumberField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class ENumberPanel extends  EFieldPanel<KbeeENumberField> {
	private static final long serialVersionUID = 1L;
	
	public ENumberPanel(String id, KbeeENumberField field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		getContainer().add(new TextField<String>("field", new FieldDataModel<KbeeENumberField, String>(getFieldModel(), getDataModel())) {
			@Override
			public IModel<String> getSubtitle() {
				String s=getField().getSublabel();
				if (s!=null)
					return new Model<String>(getField().getSublabel() );
				else
					return null;
			}
			
			@Override
			public IModel<String> getLabel() {
				return getField().getLabel()!=null ?
					new Model<String>(getField().getLabel()) :
					new Model<String>("");	
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				updateModel();
				ENumberPanel.this.validate(target);
				fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
			}
			@Override
			protected void onKey(AjaxRequestTarget target, String jsKeycode) {
				fireScanAll(new EFocusEvent(target, getField()));
			}
			@Override
			public boolean hasFeedback() {
				return !ENumberPanel.this.getMessages().isEmpty();
			}
			
			
			@Override
			public boolean isReadOnly() {
				 return getField().isReadOnly();
			}
			
			@Override
			public boolean isEnabled() {
				return !getField().isReadOnly() && getData().getForm().isEnabled() && getField().isEnabled(getData());
			}
			@Override
			public String getMessage() {
				return hasFeedback() ? ENumberPanel.this.getMessages().get(0).toString() : null;
			}
			
			@Override
			public boolean isRequired() {
				return getField().isRequired();
			}
			@Override
			protected void onUpdate(String oldvalue, String newvalue) {
				String label = getField().getLabel()!=null  ? getField().getLabel() : getField().getName();
				setUpdatedField(new ValueUpdated(getData().getForm(), label, oldvalue, newvalue));
			}
			@Override
			public Disposition getDisposition() {
				return ENumberPanel.this.getDisposition();
			}
		});
		
		getInput().add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (!getMessages().isEmpty())
					return "eform-error";
				return "efield";
			}
		}));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setValues(List<?> values) {
		String value = !values.isEmpty() ? values.get(0).toString() : null;
		getData().setData(getField(), value);
		((TextField<String>)get("container:field")).setValue(value);
	}
} 