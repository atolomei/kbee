package kbee.web.eform;

import java.util.ArrayList;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.EListField;
import com.novamens.content.form.ValueAdded;
import com.novamens.content.form.ValueRemoved;
import com.novamens.content.model.Classificable;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextField;

@SuppressWarnings("serial")
public class EStringListPanel extends EFieldPanel<EListField<String>> {
	private static final long serialVersionUID = 1L;
	
	private List<String> values = new ArrayList<String>();
	private String value;
	
	public class ControlFragment extends Fragment {
		public ControlFragment(String id) {
			super(id, "control-fragment", EStringListPanel.this);
			add(new ListView<String>("value", () -> getValues()) {
				public void populateItem(ListItem<String> item) {
					item.add(new AjaxLink<Void>("remove-link") {
						public void onClick(AjaxRequestTarget target) {
							removeValue(item.getModelObject());
							fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
							target.add(getContainer());
							setFocus(target);
						}	
						@Override
						public boolean isVisible() {
							return !getField().isReadOnly() && 
								getField().isEnabled(getData()) &&
								isEditionEnabled() &&
								getData().getForm().isEnabled();
						}
					});
					item.add(new Label("label", item.getModelObject()));
				}
			});
			
			add(new TextField<String>("field", new PropertyModel<String>(EStringListPanel.this, "value")) {
				@Override
				public IModel<String> getLabel() {
					return new Model<String>("");
				} 
				
				
				@Override
				public boolean isRequired() {
					return getField().isRequired();
				}
				
				
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					addValue(getValue());
					setValue(null);
					target.add(getContainer());
					setFocus(target);
					fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
				}
				@Override
				protected void onKey(AjaxRequestTarget target, String jsKeycode) {
					fireScanAll(new EFocusEvent(target, getField()));
				}
				@Override
				public boolean isInputEnabled() {
					return super.isInputEnabled() && 
						!getField().isReadOnly() && 
						isEditionEnabled() &&
						getField().isEnabled(getData()) && 
						getData().getForm().isEnabled();
				}
				@Override 
				public boolean isVisible() {
					return isInputEnabled();
				}
				@Override 
				public Disposition getDisposition() {
					return Disposition.VERTICAL;
				}
				@Override
				protected boolean isRequiredMark() {
					return getField().isRequired();
				}
			});
		}
	}	
	
	public EStringListPanel(String id, EListField<String> field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setValues();
		
		WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
		layout.add(new ControlFragment("control"));
		getContainer().add(new ControlFragment("control"));
		getContainer().add(layout);
		
		getContainer().add(new Label("label", new Model<String>() {
			public String getObject() {
				return getField().getLabel()!=null ?
					getField().getLabel() :
					"";	
			}
		}));
		
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			getContainer().get("control").setVisible(false);
		}
		else {
			layout.setVisible(false);
		}
	}
	
	public void setFocus(AjaxRequestTarget target) {
		super.setFocus(target);
	}
	
	@Override
	public void update(Classificable classificable) {
		getField().set(classificable, getData());
	}
	
	public Disposition getDisposition() {
		return Disposition.HORIZONTAL;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public List<String> getValues() {
		return values;
	}
	
	public void addValue(String value) {
		if (value!=null && !this.values.contains(value)) {
			this.values.add(value);
			setUpdatedField(new ValueAdded(getData().getForm(), getLabel(), value));
		}
		getData().setData(getField(), getValues());
	}
	
	public void removeValue(String value) {
		if (values.remove(value)) {
			setUpdatedField(new ValueRemoved(getData().getForm(), getLabel(), value));
		}
		getData().setData(getField(), getValues());
	}
	
	@Override
	public Field<?> getInput() {
		return (Field<?>)getContainer().get("horizontal-layout:control:field");
	}
	
	public String getLabel() {
		return getField().getLabel()!=null ?
			getField().getLabel() :
			"";	
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		addFeedbackPanel();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.value = null;
	}
	
	protected void setValues() {
		List<?> values = (List<?>)getData().getData(getField());
		this.values.clear();
		if (values!=null) {
			for (Object value : values) {
				this.values.add((String)value);
			}
		}
	}
}