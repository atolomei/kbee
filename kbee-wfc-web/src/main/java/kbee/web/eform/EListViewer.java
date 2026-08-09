package kbee.web.eform;

import java.util.ArrayList;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EListField;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.text.TextTemplate;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;


@SuppressWarnings("serial")
public class EListViewer<T> extends EFieldPanel<EListField<T>> {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<T>> values = new ArrayList<IModel<T>>();
	private T value;
	
	public class ControlFragment extends Fragment {
		public ControlFragment(String id) {
			super(id, "control-fragment", EListViewer.this);
			String s=getField().getSublabel();
			Label ls=new Label("subtitle", s!=null?new Model<String>(getField().getSublabel()):"");
			ls.setEscapeModelStrings(false);
			ls.setVisible(getField().getSublabel()!=null);
			add(ls);	
			// TODO VER SUBTITLE
			
			
			add(new ListView<IModel<T>>("value", () -> getValues()) {
				public void populateItem(ListItem<IModel<T>> item) {
					item.add(new Label("label", getValue(item.getModelObject().getObject())));
					Label infolabel = new Label("info", getInfo(item.getModelObject().getObject())) {
						public boolean isVisible() {
							return getInfo(item.getModelObject().getObject())!=null;
						}
					};
					infolabel.setEscapeModelStrings(false);
					item.add(infolabel);
				}
			});
		}
	}	
	
	public EListViewer(String id, EListField<T> field, IModel<EFormData> data) {
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
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
	public List<IModel<T>> getValues() {
		return values;
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
		for (IModel<T> model : values) 
			model.detach();
	}
	
	protected EFormDataSource<T> getDataSource() {
		return getField().getModel().getDataSource(getFormObject());
	}
	
	protected String getValue(T object) {
		if (getField().getValueTemplate()!=null) {
			TextTemplate template = new KbeeTextTemplate(getField().getValueTemplate());
			String label = template.process(object);
			return label;
		}
		else {
			return DisplayNameExtractor.get(object);
		}
	}
	
	protected String getInfo(T object) {
		if (getField().getInfoTemplate()!=null) {
			TextTemplate template = new KbeeTextTemplate(getField().getInfoTemplate());
			String label = template.process(object);
			return label;
		}
		else {
			if (object instanceof DataSetMember) {
				ExtractionRule rule = ((DataSetMember)object).getDataSet().getSublineRule();
				if (rule!=null) {
					String label = (String)rule.extract((DataSetMember)object);
					return label;
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected void setValues() {
		List<?> values = (List<?>)getData().getData(getField());
		this.values.clear();
		if (values!=null) {
			for (Object value : values) {
				this.values.add(new ObjectModel<T>((T)value));
			}
		}
	}
}