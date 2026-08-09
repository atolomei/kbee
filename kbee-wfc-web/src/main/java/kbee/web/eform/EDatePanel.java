package kbee.web.eform;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.ValueUpdated;
import com.novamens.kbee.content.form.KbeeEDateField;
import com.novamens.wicket.markup.html.form.DateTimeField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class EDatePanel extends EFieldPanel<KbeeEDateField> {
	private static final long serialVersionUID = 1L;
	
	public EDatePanel(String id, KbeeEDateField field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		getContainer().add(new DateTimeField("field", 
				getZoneId(), 
				new FieldDataModel<KbeeEDateField, OffsetDateTime>(getFieldModel(), getDataModel())) {
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
				EDatePanel.this.onUpdate(target);
			}
			@Override
			public boolean isEnabled() {
				return !getField().isReadOnly() && getData().getForm().isEnabled() && getField().isEnabled(getData());
			}
			@Override
			public boolean isVisible() {
				return getField().isVisible(getData());
			}
			@Override
			public boolean isInputEnabled() {
				return isEditionEnabled() && !getField().isReadOnly() && !getData().isSigned();
			}
			@Override
			public boolean hasFeedback() {
				return !EDatePanel.this.getMessages().isEmpty();
			}
			@Override
			public boolean isRequired() {
				return getField().isRequired();
			}
			@Override
			public String getMessage() {
				return hasFeedback() ? EDatePanel.this.getMessages().get(0).toString() : null;
			}
			@Override
			public Disposition getDisposition() {
				return EDatePanel.this.getDisposition();
			}
			@Override
			protected void onKey(AjaxRequestTarget target, String jsKeycode) {
				fireScanAll(new EFocusEvent(target, getField()));
			}
			@Override
			protected boolean isRequiredMark() {
				return getField().isRequired();
			}
			@Override
			protected void onUpdate(OffsetDateTime oldvalue, OffsetDateTime newvalue) {
				String label = getField().getLabel()!=null  ? getField().getLabel() : getField().getName();
				setUpdatedField(new ValueUpdated(getData().getForm(), label, oldvalue, newvalue));
			}
		});
		
		getInput().addInfoPanel((id) -> new EMetainfoPanel("panel") {
			@Override
			public List<Serializable> getMessages() {
				return EDatePanel.this.getMessages();
			}
		});
		
		getInput().add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				String css = "efield";
				if (!getMessages().isEmpty()) {
					css = "eform-error";
				}
				return css;
			}
		}));
	}

	
	protected ZoneId getZoneId() {
		return getDomain()!=null ? ZoneId.of(getDomain().getTimeZone()) : ZoneId.systemDefault();
	}
}