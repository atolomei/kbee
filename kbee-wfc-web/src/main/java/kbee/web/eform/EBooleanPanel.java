package kbee.web.eform;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.ValueUpdated;
import com.novamens.kbee.content.form.KbeeEBooleanField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class EBooleanPanel extends  EFieldPanel<KbeeEBooleanField> {
	private static final long serialVersionUID = 1L;
	
	public EBooleanPanel(String id, KbeeEBooleanField field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		boolean initialized = isKbInitialized();
		super.onInitialize();
		if (initialized) return;
		getContainer().add(new BooleanField("field", new FieldDataModel<KbeeEBooleanField, Boolean>(getFieldModel(), getDataModel())) {
			@Override
			public IModel<String> getLabel() {
				return getField().getLabel()!=null ?
					new Model<String>(getField().getLabel()) :
					new Model<String>("");	
			}
			@Override
			public IModel<String> getSubtitle() {
				String s=getField().getSublabel();
				if (s!=null)
					return new Model<String>(getField().getSublabel() );
				else
					return null; 
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				updateModel();
				fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
			}
			@Override
			public boolean isRequired() {
				return getField().isRequired();
			}
			@Override
			public boolean isInputEnabled() {
				return isEditionEnabled() && !getField().isReadOnly();
			}
			@Override
			public Disposition getDisposition() {
				return EBooleanPanel.this.getDisposition();
			}
			@Override
			public boolean hasFeedback() {
				return !EBooleanPanel.this.getMessages().isEmpty();
			}
			@Override
			public String getMessage() {
				return hasFeedback() ? EBooleanPanel.this.getMessages().get(0).toString() : null;
			}
			@Override
			protected boolean isRequiredMark() {
				return getField().isRequired(); 
			}
			@Override
			protected void onUpdate(Boolean oldvalue, Boolean newvalue) {
				String label = getField().getLabel()!=null  ? getField().getLabel() : getField().getName();
				setUpdatedField(new ValueUpdated(getData().getForm(), label, oldvalue, newvalue));
			}
			@Override
			protected IModel<String> getHelpText() {
				return new Model<String>(getField().getModel().getMetainfoMessage());
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