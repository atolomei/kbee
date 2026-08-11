package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.EntitySet;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.Subsection;
import com.novamens.content.model.SubsectionTemplate;
import com.novamens.kbee.content.model.KbeeCodeExecutor;
import com.novamens.kbee.content.model.KbeeModelElementTemplate;

@Deprecated
@SuppressWarnings("serial")
public class ContentTemplateSectionStructureEditor<T> extends AbstractStructureEditor<T> {

	private static final long serialVersionUID = 1L;
	
	private IModel<ContentTemplate> templateModel;
	
	class ScriptValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String script = validatable.getValue();
			if (script==null)
				return;
			KbeeCodeExecutor executor = new KbeeCodeExecutor();
			String message = executor.validate(script, templateModel.getObject());
			if (message!=null) {
				validatable.error(new ValidationError(message));
			}
		}
	}

	public ContentTemplateSectionStructureEditor(IModel<ModelSection> model, IModel<ContentTemplate> templateModel) {
		super(model);
		this.templateModel = templateModel;
	}
	
	public ContentTemplateSectionStructureEditor(String id, IModel<ModelSection> model) {
		super(id, model);
	}

	@Override
	protected List<Property<?>> getProperties() {
		
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
		properties.add(new Property<String>() {
			@Override
			public String getName() {
				return "name";
			}
			public String getDisplayValue(ModelElementTemplate template) {
				
				if (template instanceof ClassifierTemplate) {
					 Classifier c=((ClassifierTemplate) template).getClassifier();
					 if(c!=null) {
						 DataSet d=c.getDataSet();
						 if (d!=null) {
							 if (d.isAggregation()) {
								 return template.getDisplayName()+" <span class=\"ago\">("+ new StringResourceModel("built-in", ContentTemplateSectionStructureEditor.this, null).getObject()+")</span>";
							 }
						 }
					 }
					 
					return template.getDisplayName();
				}
				else
					return template.getDisplayName();
			}
			@Override
			public boolean isEditable(IModel<ModelElementTemplate> model) {
				return model.getObject().getElement()!=null && model.getObject().getElement() instanceof Subsection;
			}
			@Override
			public String getStringWidth() {
				return "30%";
			}
			@Override
			public String getStyle(IModel<ModelElementTemplate> model) {
				return isEditable(model) ? "font-weight: bold;" : null;
			}
		});
		
		
		
		properties.add(new LabelProperty() {
			@Override
			public String getName() {
				return "type";
			}
			@Override
			public String getValue(ModelElementTemplate value) {
				if (value instanceof ClassifierTemplate) {
					String label = "";
					if (((ClassifierTemplate)value).isReverse()) {
						label += ContentTemplateSectionStructureEditor.this.getLabel("reverse").getObject()+" ";
					}
					if (value.getElement()!=null &&
						 ((Classifier)value.getElement()).getDataSet() instanceof EntitySet) {
						return label + ContentTemplateSectionStructureEditor.this.getLabel("classifier").getObject();
					}
					else {
						return label + ContentTemplateSectionStructureEditor.this.getLabel("classifier").getObject();
					}
				}
				else 
				if (value instanceof AttributeTemplate) {
					String label = ContentTemplateSectionStructureEditor.this.getLabel("attribute").getObject();
					if (value.getElement()!=null) {
						label = label + " <span class=\"ago\">( " + ((Attribute)value.getElement()).getType().getLabel()+" )</span>" ;
					}
					return label;
				}
				else 
				if (value instanceof SubsectionTemplate) {
					return "<b>"+ContentTemplateSectionStructureEditor.this.getLabel("subsection").getObject()+"</b>";
				}
				if (value instanceof ModelElementTemplate) {
					if (((ModelElementTemplate)value).getElement() instanceof Classifier) {
						String label = "";
						if (((ModelElementTemplate)value).isReverse()) {
							label += ContentTemplateSectionStructureEditor.this.getLabel("reverse").getObject()+" ";
						}
						if (value.getElement()!=null &&
							 ((Classifier)value.getElement()).getDataSet() instanceof EntitySet) {
							return label + ContentTemplateSectionStructureEditor.this.getLabel("classifier").getObject();
						}
						else {
							return label + ContentTemplateSectionStructureEditor.this.getLabel("classifier").getObject();
						}
					}
					if (((ModelElementTemplate)value).getElement() instanceof Attribute) {
						String label = ContentTemplateSectionStructureEditor.this.getLabel("attribute").getObject();
						if (value.getElement()!=null) {
							label = label + " <span class=\"ago\">( " + ((Attribute)value.getElement()).getType().getLabel()+" )</span>" ;
						}
						return label;
					}
					if (((ModelElementTemplate)value).getElement() instanceof Subsection) {
						return "<b>"+ContentTemplateSectionStructureEditor.this.getLabel("subsection").getObject()+"</b>";
					}
				}
				return "";
			}
			@Override
			public String getStringWidth() {
				return "24%";
			}				
		});

		
//		properties.add(new BooleanProperty() {
//			@Override
//			public String getName() {
//				return "visible";
//			}
//			@Override
//			public boolean isVisible(IModel<ModelElementTemplate> model) {
//				return model.getObject().getElement()!=null && !(model.getObject().getElement() instanceof Subsection);
//			}
//			@Override
//			public String getStringWidth() {
//				return "6%";
//			}
//		});
		
		
		
		properties.add(new Property<Multiplicity>() {
			
			@Override
			public String getDisplayValue(ModelElementTemplate value) {
				return value.getMultiplicity().getLabel( getSessionUser().getLocale() );
			}
			
			
			@Override
			public String getName() {
				return "multiplicity";
			}
			@Override
			public List<Multiplicity> getChoices() {
				return getMultiplicities();
			}
			@Override
			public boolean isSelectable() {
				return true;
			}
			@Override
			public Multiplicity getMultiplicity() {
				return Multiplicity.M01;
			}
			@Override
			public boolean isVisible(IModel<ModelElementTemplate> model) {
				return model.getObject().getElement()!=null && !(model.getObject().getElement() instanceof Subsection);
			}
			@Override
			public String getStringWidth() {
				return "20%";
			}
		});
		
		
//		properties.add(new Property<AccessStrategy>() {
//			@Override
//			public String getName() {
//				return "accessibility";
//			}
//			
//			@Override
//			public AccessStrategy getValue(ModelElementTemplate value) {
//				try {
//					if (value instanceof ClassifierTemplate)
//						return (( ClassifierTemplate) value).getAccessibility();
//					return null;
//				}
//				catch (RuntimeException e) {
//					return null;
//				}
//			}
//
//			@Override
//			public List<AccessStrategy> getChoices() {
//				return getAccessibilities();
//			}
//			@Override
//			public boolean getTitle() {
//				return false;
//			}
//			@Override
//			public boolean isSelectable() {
//				return true;
//			}
//			@Override
//			public boolean getKey() {
//				return false;
//			}
//			@Override
//			public Multiplicity getMultiplicity() {
//				return Multiplicity.M01;
//			}
//			@Override
//			public boolean isVisible(IModel<ModelElementTemplate> model) {
//				return model.getObject().getElement()!=null && 
//					model.getObject().getElement() instanceof Classifier;
//			}
//			@Override
//			public String getStringWidth() {
//				return "10%";
//			}
//		});
//		

//		properties.add(new Property<AccessStrategy>() {
//			@Override
//			public String getName() {
//				return "valuesCriteria";
//			}
//			@Override
//			public boolean isVisible(IModel<ModelElementTemplate> model) {
//				return model.getObject().getElement()!=null && 
//					model.getObject().getElement() instanceof Classifier &&
//					(
//						(model.getObject() instanceof ClassifierTemplate &&	AccessStrategy.Iql.equals(((ClassifierTemplate)model.getObject()).getAccessibility())) ||
//						(model.getObject() instanceof KbeeModelElementTemplate && AccessStrategy.Iql.equals(((KbeeModelElementTemplate)model.getObject()).getAccessibility()))
//					);
//			}
//			@Override
//			public boolean isGrid() {
//				return false;
//			}
//			@Override
//			public String getStringWidth() {
//				return "15%";
//			}
//		});
		

		properties.add(new Property<String>() {
			@Override
			public String getName() {
				return "subsection";
			}
			@Override
			public boolean isVisible(IModel<ModelElementTemplate> model) {
				return false;
			}
			@Override
			public boolean isGrid() {
				return false;
			}
			@Override
			public String getStringWidth() {
				return "30%";
			}
			public boolean isEditable(IModel<ModelElementTemplate> model) {
				return true;
			}
		});
		
//		properties.add(new Property<AttributeSource>() {
//			@Override
//			public String getName() {
//				return "source";
//			}
//			
//			@Override
//			public AttributeSource getValue(ModelElementTemplate value) {
//				try {
//					if (value instanceof AttributeTemplate)
//						return (( AttributeTemplate) value).getSource();
//					return null;
//				}
//				catch (RuntimeException e) {
//					return null;
//				}
//			}
//
//			
//			@Override
//			public List<AttributeSource> getChoices() {
//				return getSources();
//			}
//			@Override
//			public boolean getTitle() {
//				return false;
//			}
//			@Override
//			public boolean isSelectable() {
//				return true;
//			}
//			@Override
//			public boolean getKey() {
//				return false;
//			}
//			@Override
//			public Multiplicity getMultiplicity() {
//				return Multiplicity.M01;
//			}
//			@Override
//			public boolean isVisible(IModel<ModelElementTemplate> model) {
//				return model.getObject().getElement()!=null && 
//					model.getObject().getElement() instanceof Attribute;
//			}
//			@Override
//			public String getStringWidth() {
//				return "10%";
//			}
//		});
		
//		properties.add(new TextAreaProperty() {
//			@Override
//			public String getName() {
//				return "valuesCriteria";
//			}
//			@Override
//			public boolean isVisible(IModel<ModelElementTemplate> model) {
//				return model.getObject().getElement()!=null && 
//					model.getObject().getElement() instanceof Classifier &&
//					(
//						(model.getObject() instanceof ClassifierTemplate &&	AccessStrategy.Iql.equals(((ClassifierTemplate)model.getObject()).getAccessibility())) ||
//						(model.getObject() instanceof KbeeModelElementTemplate && AccessStrategy.Iql.equals(((KbeeModelElementTemplate)model.getObject()).getAccessibility()))
//					);
//			}
//			@Override
//			public boolean isGrid() {
//				return false;
//			}
//			@Override
//			public String getStringWidth() {
//				return "15%";
//			}
//		});
		
//		properties.add(new TextAreaProperty() {
//			
//			@Override
//			public String getValue(ModelElementTemplate value) {
//				try {
//					if (value instanceof ClassifierTemplate)
//						return (( ClassifierTemplate) value).getSelectionScript();
//					return null;
//				}
//				catch (RuntimeException e) {
//					return null;
//				}
//			}
//
//			
//			@Override
//			public String getName() {
//				return "selectionScript";
//			}
//			@Override
//			public boolean helpInfo() {
//				return true;
//			}
//			
//			@Override
//			public String getStringWidth() {
//				return "25%";
//			}
//			@Override
//			public IValidator<String> getValidator(){
//				return new ScriptValidator(); 
//			}
//			@Override
//			public boolean isVisible(IModel<ModelElementTemplate> model) {
//				ModelElementTemplate template = model.getObject();
//				return template.getElement()!=null && template.getElement() instanceof Classifier &&
//					(
//						(template instanceof ClassifierTemplate &&	AccessStrategy.Script.equals(((ClassifierTemplate)template).getAccessibility())) ||
//						(template instanceof KbeeModelElementTemplate && AccessStrategy.Script.equals(((KbeeModelElementTemplate)template).getAccessibility()))
//					); 
//			}
//			@Override
//			public void onHelp(AjaxRequestTarget target) {
//				ContentTemplateSectionStructureEditor.this.onHelp(target); 
//			}
//		});
		
//		properties.add(new TextAreaProperty() {
//			@Override
//			public String getValue(ModelElementTemplate value) {
//				try {
//					if (value instanceof AttributeTemplate)
//						return ((AttributeTemplate) value).getCalculationScript();
//					return null;
//				}
//				catch (RuntimeException e) {
//					return null;
//				}
//			}
//
//			
//			@Override
//			public String getName() {
//				return "calculationScript";
//			}
//			@Override
//			public boolean helpInfo() {
//				return true;
//			}
//			@Override
//			public String getStringWidth() {
//				return "25%";
//			}
//			@Override
//			public boolean isVisible(IModel<ModelElementTemplate> model) {
//				ModelElementTemplate template = model.getObject();
//				return template.getElement()!=null && template.getElement() instanceof Attribute && 
//				(
//					(template instanceof AttributeTemplate && AttributeSource.Script.equals(((AttributeTemplate)template).getSource())) ||
//					(template instanceof KbeeModelElementTemplate && AttributeSource.Script.equals(((KbeeModelElementTemplate)template).getSource()))
//				); 
//			}
//			@Override
//			public IValidator<String> getValidator(){
//				return new ScriptValidator(); 
//			}
//			@Override
//			public void onHelp(AjaxRequestTarget target) {
//				ContentTemplateSectionStructureEditor.this.onHelp(target); 
//			}
//		});
		
		
		properties.add(new Property<ModelElement>() {
			@Override
			public String getName() {
				return "parent";
			}
			@Override
			public boolean isVisible() {
				return false;
			}
		});
		
		properties.add(new BooleanProperty() {
			@Override
			public String getName() {
				return "reverse";
			}
			@Override
			public boolean isVisible() {
				return false;
			}
			@Override
			public boolean isEditable() {
				return false;
			}
		});

		
		return properties;
	}

	protected Property<?> getKey() {
		return new Property<ModelElement>() {
			@Override
			public String getName() {
				return "element";
			}
			@Override
			public List<ModelElement> getChoices() {
				return getElements();
			}
			@Override
			public IModel<ModelElement> getModel(ModelElement value) {
				IModel<ModelElement> model = null;
				if (value instanceof Subsection) {
					model = new SerializableModel(value);
				}
				else {
					model = super.getModel(value);
				}	
				return model;
			}
			
		};
	}

	@Override
	protected ModelElementTemplate getNewValue() {
		KbeeModelElementTemplate template = new KbeeModelElementTemplate();
		template.setVisible(true);
		template.setMultiplicity(Multiplicity.M01);
		template.setMetadataSubtitle(false);
		template.setAccessibility(AccessStrategy.All);
		return template;
	}
	
	protected List<Property<?>> getPropertiesCache() {
		return getProperties();
	}
}
