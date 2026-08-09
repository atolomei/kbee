package kbee.web.eform;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EAutoCompleteField;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.ValueUpdated;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.PersonMember;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.service.ApplicationSiteMapService;


@SuppressWarnings("serial")
public class EAutoCompleteWithPreviewPanel<T extends DataSetMember> extends  EFieldPanel<EAutoCompleteField<T>> {
	private static final long serialVersionUID = 1L;
	
	
	public class PreviewPanel extends Fragment {
		public PreviewPanel(String id) {
			super(id, "preview-fragment", EAutoCompleteWithPreviewPanel.this);
			
			if (getValue()!=null &&  getValue().getDataSet()!=null) {
				Label ti = new Label("label-title", getValue().getDataSet().getDisplayName());
				add(ti);
			}
			else {
				add( (new Label("label-title")).setVisible(false));
			}
			
			add(new Label("label", new Model<String>() {
				public String getObject() {
					if (getValue()==null)
						return "na";
					if (getValue() instanceof PersonMember) {
						return  ((PersonMember) getValue()).getFirstLastName();
					}
					else {
						return  getValue().getDisplayName();
					}
				}
			}));
			
			
			
			add(new ListView<IModel<ModelElementTemplate>>("element", () -> getElements()) {
				public void populateItem(ListItem<IModel<ModelElementTemplate>> item) {
					
					ModelElementTemplate template = item.getModelObject().getObject();
					if ( !getValues(item.getModelObject()).isEmpty() ) {
						item.add(new Label("label", template.getDisplayName()));
						item.add(new ListView<String>("value", () -> getValues(item.getModelObject())) {
							public void populateItem(ListItem<String> item) {
								item.add(new Label("label", item.getModelObject()));
							}
						});
					}
					
					else {
						item.add( (new Label("label", template.getDisplayName())).setVisible(false));
						ListView<String> lv=new ListView<String>("value", () -> getValues(item.getModelObject())) {
							public void populateItem(ListItem<String> item) {
								item.add(new Label("label", item.getModelObject()));
							}
						};
						lv.setVisible(false);
						item.add(lv);
					}
				
					item.getModelObject().detach();
				}
			});
			
			
			add(new AjaxLink<Void>("remove") {
				public void onClick(AjaxRequestTarget target) {
					getData().setData(getField(), null);
					target.add(getContainer());
				}
				public boolean isVisible() {
					
					if (isReadOnly())
						return false;
					
					if (!isEditionEnabled())
						return false;
					
					return true;
				}
			});
			
			add(new Link<Void>("open") {
				public void onClick() {
					if (getValue()!=null) {
						setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage(
								 ApplicationSiteMapService.DataSetMemberPage, new ObjectModel<DataSetMember>(getValue())));	
					}
				}
				
				public boolean isVisible() {
					if (isReadOnly())
						return false;
					if (!isEditionEnabled())
						return false;
					return true;
				}
			});
			
		}
		public boolean isVisible() {
			return getValue()!=null;
		}
		@SuppressWarnings("unchecked")
		public T getValue() {
			return (T)getData().getData(getField());
		}
		private List<String> getValues(IModel<ModelElementTemplate> model) {
			List<String> values = new ArrayList<String>();
			ModelElementTemplate template = model.getObject();
			if (template instanceof AttributeTemplate) {
				com.novamens.content.model.Attribute attribute = ((AttributeTemplate)template).getAttribute();
				if (attribute!=null) {
					values.addAll(getValue().getAttributeValues(attribute));
				}
			}
			if (template instanceof ClassifierTemplate) {
				Classifier classifier = ((ClassifierTemplate)template).getClassifier();
				if (classifier!=null) {
					for (Classification classification : getValue().getClassification(classifier)) {
						if (classification!=null) {
							values.add(classification.getDataSetMember().getDisplayName());
						}
					}
				}
			}
			return values;
		}
		private List<IModel<ModelElementTemplate>> getElements() {
			List<IModel<ModelElementTemplate>> elements = new ArrayList<IModel<ModelElementTemplate>>();
			for (ModelElementTemplate template : getValue().getDataSet().getStructure()) {
				 elements.add(new ObjectModel<ModelElementTemplate>(template));
			}
			return elements;
		}
	}
	
	public class ControlFragment extends Fragment {
		public ControlFragment(String id) {
			super(id, "control-fragment", EAutoCompleteWithPreviewPanel.this);
			
			add(new PreviewPanel("preview") {
				public boolean isVisible() {
					return getValue()!=null;
				}
			});
			
			add(new AutoCompleteFieldV5<T>("field", 
					new FieldDataModel<EAutoCompleteField<T>, T>(getFieldModel(), getDataModel())) {
				
				
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
				public IModel<String> getLabel() {
					return new Model<String>("");
				} 
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					setValue(null);
					setSuggestion(null);
					setStringValue(null);
					target.add(getContainer());
					setFocus(target);
					fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
				}
				@Override
				protected void onKey(AjaxRequestTarget target, String jsKeycode) {
					fireScanAll(new EFocusEvent(target, getField()));
				}
				@Override
				public boolean hasFeedback() {
					return !EAutoCompleteWithPreviewPanel.this.getMessages().isEmpty();
				}
				@Override
				public String getMessage() {
					return hasFeedback() ? EAutoCompleteWithPreviewPanel.this.getMessages().get(0).toString() : null;
				}
				@Override
				public List<Suggestion> getSuggestions(String pattern) {
					return getField().getChoicesSource(getFormObject()).getValues(pattern);
				}
				@Override
				public Disposition getDisposition() {
					return EAutoCompleteWithPreviewPanel.this.getDisposition();
				}
				@Override
				public boolean isEnabled() {
					return !getField().isReadOnly() && getField().isEnabled(getData());
				}
				
				@Override
				public boolean isRequired() {
					return getField().isRequired();
				}
				@Override
				public boolean isVisible() {
					return getData().getData(getField())==null;
				}
				@Override 
				public String getHistoryKey() {
					return "eform-"+getField().getName(); 
				}
				@Override 
				protected String serialize(IModel<T> model) {
					return getField().serialize(getFormObject(), model.getObject());
				}
				@Override
				protected void onUpdate(T oldvalue, T newvalue) {
					String label = getField().getLabel()!=null  ? getField().getLabel() : getField().getName();
					getEditor().setUpdatedField(new ValueUpdated(getData().getForm(), label, oldvalue, newvalue));
				}
				@Override 
				protected IModel<T> deserialize(String token) {
					return new IModel<T>() {
						public T getObject() {
							return getField().deserialize(getFormObject(), token);
						}
					};
				}
			});
			
//			getAutoComplete().add(new AttributeModifier("class", new Model<String>() {
//				public String getObject() {
//					String css = "efield";
//					if (!getMessages().isEmpty()) {
//						css = "eform-error";
//					}
//					return css;
//				}
//			}))
			

			//add(getFeedbackPanel());
		}
	}	
	
	public EAutoCompleteWithPreviewPanel(String id, EAutoCompleteField<T> field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
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
		getData().setData(getField(), value!=null ? new ObjectModel<T>(value) : null);
		getAutoComplete().setValue(value);
		getAutoComplete().setStringValue(value!=null ? DisplayNameExtractor.get(value) : null); 
	}
	
	@SuppressWarnings("unchecked")
	public AutoCompleteFieldV5<T> getAutoComplete() {
		return (AutoCompleteFieldV5<T>)getInput();
	}
	
	public Field<?> getInput() {
		return (Field<?>)get("container:control:field");
	}
	
	@SuppressWarnings("unchecked")
	protected void setValues(List<?> values) {
		for (Object object : values) {
			setValue((T)object);
		}
		if (values.isEmpty()) {
			setValue(null);
		}
	}
	
	protected void refresh(AjaxRequestTarget target) {
		super.refresh(target);
		getAutoComplete().clearCache(target);
	}
}	
