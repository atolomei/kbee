package kbee.web.eform;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormChoice;
import com.novamens.content.form.EFormData;
import com.novamens.wicket.markup.html.form.CheckField;

@SuppressWarnings("serial")
public class EChoiceFieldPanel extends EFieldPanel<EFormChoice> {
	private static final long serialVersionUID = 1L;

	public EChoiceFieldPanel(String id, EFormChoice choice, IModel<EFormData> data) {
		super(id, choice, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		getContainer().add(new CheckField("field", new FieldDataModel<EFormChoice, Boolean>(getFieldModel(), getDataModel())) {

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
				return new Model<String>(getField().getLabel());
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				updateModel();
				fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
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
			
			@Override
			public boolean isRequired() {
				return getField().isRequired();
			}
		});
	}
}