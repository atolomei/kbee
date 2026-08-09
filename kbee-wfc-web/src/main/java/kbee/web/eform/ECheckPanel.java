package kbee.web.eform;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormData;
import com.novamens.kbee.content.form.KbeeECheckField;
import com.novamens.wicket.markup.html.form.CheckField;

@SuppressWarnings("serial")
public class ECheckPanel extends  EFieldPanel<KbeeECheckField> {
	private static final long serialVersionUID = 1L;
	
	private CheckField check;
	
	public ECheckPanel(String id, KbeeECheckField field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (getData().getData(getField())==null) {
			getData().setData(getField(), Boolean.TRUE);
		}
		check = new CheckField("field", new FieldDataModel<KbeeECheckField, Boolean>(getFieldModel(), getDataModel())) {
			
			// TODO VER SUBTITLE
			@Override
			public IModel<String> getSubtitle() {
				String s=getField().getSublabel();
				if (s!=null)
					return new Model<String>(getField().getSublabel() );
				else
					return null; 
			}
			
			@Override
			public boolean isReadOnly() {
				 return getField().isReadOnly();
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
				fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
			}
			
			@Override
			public boolean isRequired() {
				return getField().isRequired();
			}
			
			@Override
			public IModel<String> getText() {
				return getField().getText()!=null ?
					new Model<String>(getField().getText()) :
					new Model<String>("");	
			}
			@Override
			protected String getPart() {
				return getField().getName();
			}
		};
		getContainer().add(check);
	}
	
	@Override
	public void setValues(List<?> values) {
		Boolean value = !values.isEmpty() ? Boolean.valueOf(values.get(0).toString()) : null;
		getData().setData(getField(), value);
		check.setValue(value);
		
	}
} 