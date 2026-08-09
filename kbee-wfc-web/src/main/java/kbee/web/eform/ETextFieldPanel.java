package kbee.web.eform;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.ValueUpdated;
import com.novamens.kbee.content.form.KbeeETextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;

@SuppressWarnings("serial")
public class ETextFieldPanel extends EFieldPanel<KbeeETextField> {
	private static final long serialVersionUID = 1L;

	public ETextFieldPanel(String id, KbeeETextField field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		TextAreaField<String> taf= new TextAreaField<String>("field", 
				new FieldDataModel<KbeeETextField, 
				String>(getFieldModel(), 
				getDataModel())) {
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
				fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
			}
			@Override
			protected void onKey(AjaxRequestTarget target, String jsKeycode) {
				fireScanAll(new EFocusEvent(target, getField()));
			}
			@Override
			protected IModel<String> getHelpText() {
				return new Model<String>(getField().getModel().getMetainfoMessage());
			}
			public boolean isInputEnabled() {
				return super.isInputEnabled() && 
					!getField().isReadOnly() && 
					getField().isEnabled(getData()) && 
					getData().getForm().isEnabled();
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
				return ETextFieldPanel.this.getDisposition();
			}
			@Override
			public boolean isHelpVisible() {
				return getField().getModel().getMetainfoMessage()!=null;
			}
		};
		
		
		taf.setRows(4);
		
				
		getContainer().add(taf);
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	
	
	
	@Override
	@SuppressWarnings("unchecked")
	public void setValues(List<?> values) {
		String value = !values.isEmpty() ? values.get(0).toString() : null;
		getData().setData(getField(), value);
		((TextField<String>)get("container:field")).setValue(value);
	}
}
