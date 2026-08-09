package kbee.web.datamanagement;

import com.novamens.content.model.*;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.SuggestionService;
import com.novamens.kbee.content.command.TagOperation;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.*;
import com.novamens.wicket.model.ObjectModel;
import kbee.web.form.AutoCompleteFieldV5;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public abstract class TagManagementTagAction extends TagManagementAction {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TagOperation tagOperation = TagOperation.replace;
    private String attributeValue;

    private TagType tagType;

    private WebMarkupContainer tagSelectors;
    private IModel<ModelElement> tagElement1 = null;
    private  List<IModel<ModelElement>> tagElementsTemplates1 = new ArrayList<>();
    private WebMarkupContainer valueContainer;
    private IModel<DataSetMember> datasetMember = null;

    private Boolean useMacro=false;
    private String macro;

    public TagManagementTagAction(String id) {
        super(id);
    }


    @Override
    protected void onInitialize() {
        super.onInitialize();

        this.setOutputMarkupId(true);
        tagSelectors = new WebMarkupContainer("tag-selector");

        ChoiceField<ModelElement> tagChoiceField=null;

        tagChoiceField = new ChoiceField<ModelElement>("tag", new PropertyModel<ModelElement>(this, "tagElement"), new PropertyModel<List<ModelElement>>(this, "tagElementsTemplates"), true) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getHelpText() {
                return TagManagementTagAction.this.getTagHelpText();
            }

            public void onUpdate(AjaxRequestTarget target) {
                setTagElement(this.getValue());
                setAttributeValue(null);
                setDatasetMember(null);
                Component fieldEditorForAttribute = resetFieldEditor();

                if (getTagOperation() == TagOperation.remove) {
                    fieldEditorForAttribute.setVisible(false);
                }
                target.add(TagManagementTagAction.this);
            }

            @Override
            protected String getDisplayValue(ModelElement value) {
                return value.getDisplayName();
            }
        };

        tagChoiceField.setRequired(true);
        tagSelectors.add(tagChoiceField);

        BooleanSwitchField macroSwitch = new BooleanSwitchField("macroSwitch", new PropertyModel<Boolean>(this, "useMacro")){
			private static final long serialVersionUID = 1L;
			@Override
            public void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                setUseMacro(getValue());
                resetFieldEditor();
                target.add(valueContainer);
            }
        };
        tagSelectors.add(macroSwitch);

        valueContainer = new WebMarkupContainer("valueContainer");
        valueContainer.setOutputMarkupId(true);
        this.add(valueContainer);
        tagSelectors.add(valueContainer);
        tagSelectors.setOutputMarkupId(true);
        tagSelectors.setOutputMarkupPlaceholderTag(true);
        this.add(tagSelectors);
        resetFieldEditor();

        setTagOperation(TagOperation.replace);
        final ChoiceField<TagOperation> tagOperation = new ChoiceField<TagOperation>("tagOperation", new PropertyModel<TagOperation>(this, "tagOperation"), () -> Arrays.asList(TagOperation.replace, TagOperation.add, TagOperation.remove), true) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getHelpText() {
                return TagManagementTagAction.this.getTagHelpText();
            }

            @Override
            protected String getDisplayValue(TagOperation value) {
                return new StringResourceModel(value.toString(), TagManagementTagAction.this).getString();
            }

            @Override
            public void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                setTagOperation(getValue());

                if (getValue() == TagOperation.remove) {
                    valueContainer.setVisible(false);
                }else{
                    valueContainer.setVisible(true);
                }
                target.add(TagManagementTagAction.this);
            }
        };
        tagOperation.setRequired(true);
        this.add(tagOperation);
    }



    private Component resetFieldEditor() {
        Component fieldEditor = null;

        if (this.getTagElement() == null) {
            setTagType(null);
            fieldEditor= new InvisiblePanel("tagValue");
        }else {
            if (this.getTagElement() instanceof Attribute) {
                setTagType(TagType.attribute);
            } else {
                setTagType(TagType.classifier);
            }
            if(getUseMacro()){
                fieldEditor= getFieldEditorForMacro();
            }else if(getTagType() == TagType.attribute){
                final Attribute attribute = ((Attribute) this.getTagElement());
                fieldEditor = getFieldEditorForAttribute(attribute);
            }else{
                fieldEditor = getFieldEditorForClassifier();
            }
        }
        valueContainer.addOrReplace(fieldEditor);
        return fieldEditor;
    }


    private TextField<String> getFieldEditorForMacro() {
        final TextField<String> macro = new TextField<String>("tagValue", new PropertyModel<String>(TagManagementTagAction.this, "macro")) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void onUpdate(AjaxRequestTarget target) {
                setMacro(getValue());
                target.add(TagManagementTagAction.this);
            }
        };
        macro.setRequired(true);
        return macro;
    }


    @SuppressWarnings({ "rawtypes", "serial", "unchecked" })
	private Field getFieldEditorForAttribute(Attribute attribute) {
        Field field;
        if (attribute.isDate()) {

            final IModel<OffsetDateTime> convertDateModel = new IModel<OffsetDateTime>() {
                /**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
                public OffsetDateTime getObject() {
                    if (getAttributeValue() != null)
                        return ServiceLocator.getService(DateTimeService.class).parseStrDate(getAttributeValue());
                    return null;
                }

                @Override
                public void setObject(OffsetDateTime offsetDateTime) {
                    if (offsetDateTime != null)
                        setAttributeValue(ServiceLocator.getService(DateTimeService.class).getStr_ISO_OFFSET_DATE_TIME(offsetDateTime));
                    else
                        setAttributeValue(null);
                }
            };
            field = new OffsetDateTimeField("tagValue", ZoneId.of(getDomain().getTimeZone()), convertDateModel);
            field.setVisible(true);
        } else {
            final AttributeType type = attribute.getType();
            if (type.equals(com.novamens.content.model.AttributeType.BOOLEAN)) {
                field = new BooleanField("tagValue", new PropertyModel<Boolean>(TagManagementTagAction.this, "attributeValue")) {
                    @Override
                    public void onUpdate(AjaxRequestTarget target) {
                        setValue(getValue());
                    }
                };
            } else {

                field = new TextField<String>("tagValue", new PropertyModel<String>(TagManagementTagAction.this, "attributeValue")) {
                    @Override
                    public void onUpdate(AjaxRequestTarget target) {

                        final boolean isNumber = type.equals(AttributeType.NUMBER);
                        if (!isNumber && (getValue() != null && !"".equals(getValue().trim()) && !getValue().matches("[0-9]+"))) {
                            setError((new ValidationError()).addKey("requiredvalidator.message"));
                        } else {
                            getFeedbackMessages().clear();
                            setValue(getValue());
                        }
                        target.add(TagManagementTagAction.this);
                    }
                };
                if (type.equals(AttributeType.NUMBER)) {
                    field.add(new IValidator<String>() {
                        @Override
                        public void validate(IValidatable<String> iValidatable) {
                            if (!iValidatable.getValue().matches("[0-9]+"))
                                iValidatable.error(new ValidationError(this, "not-number"));
                        }
                    });
                }
            }
        }
        field.setRequired(true);
        return field;
    }


    private Field<DataSetMember> getFieldEditorForClassifier() {

        AutoCompleteFieldV5<DataSetMember> datasetMemberChoiceField = new AutoCompleteFieldV5<DataSetMember>("tagValue", new PropertyModel<DataSetMember>(this, "datasetMember"), true) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onUpdate(AjaxRequestTarget target) {
                target.focusComponent(getInput());
                setDatasetMember(this.getValue());
            }

            @Override
            public List<Suggestion> getSuggestions(String pattern) {

                if (tagElement1 == null && isClassifierSelected())
                    return new ArrayList<>();
                final Classifier classifier = (Classifier) TagManagementTagAction.this.getTagElement();

                Map<String, Object> parameters = new HashMap<String, Object>();
                List<String> members = new ArrayList<String>();
                members.add("type/datasetmember");
                members.add("dataset/" + classifier.getDataSet().getId());
                parameters.put("members", members);
                return classifier.getService(SuggestionService.class).getSuggestions(pattern, parameters);
            }

            /**
             * to save history selection
             */
            @Override
            public String getHistoryKey() {
                if (tagElement1 == null || !isClassifierSelected())
                    return null;
                final ClassifierTemplate classifier = (ClassifierTemplate) TagManagementTagAction.this.getTagElement();
                return TagManagementTagAction.this.getClass().getSimpleName() + "-" + (classifier != null ? classifier.getClassifier().getAlias() : "") + "-datasetmmember";
            }

            private boolean isClassifierSelected() {
                return TagManagementTagAction.this.tagElement1 instanceof ClassifierTemplate;
            }
        };
        datasetMemberChoiceField.setOutputMarkupId(true);
        datasetMemberChoiceField.setRequired(true);
        return datasetMemberChoiceField;
    }

    protected IModel<String> getTagHelpText() {
        return new StringResourceModel("tag.help",this, null);
    }
    protected IModel<String> getActionHelpText() {
        return new StringResourceModel("action.help",this, null);
    }


    protected Domain getDomain() {
        return (Domain) ServiceLocator.getService(UserService.class).getDomain();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (datasetMember != null)
            datasetMember.detach();

        if (tagElement1 != null)
            tagElement1.detach();

        if (tagElementsTemplates1 != null)
            tagElementsTemplates1.stream().forEach(c -> c.detach());
    }

    public void setTagElementsTemplates(List<ModelElement> tagElementsTemplates) {
        this.tagElementsTemplates1 = tagElementsTemplates.stream().map(o -> new ObjectModel<>(o)).collect(Collectors.toList());
        this.tagElementsTemplates1.sort(Comparator.comparing(p -> p.getObject().getDisplayName()));
    }
    public List<ModelElement> getTagElementsTemplates() {
        return this.tagElementsTemplates1.stream().map(o -> o.getObject()).collect(Collectors.toList());
    }



    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }


    public DataSetMember getDatasetMember() {
        return datasetMember != null ? datasetMember.getObject() : null;
    }

    public void setDatasetMember(DataSetMember datasetMember) {
        this.datasetMember = datasetMember != null ? new ObjectModel<>(datasetMember) : null;
    }

    public ModelElement getTagElement() {
        return tagElement1 != null ? tagElement1.getObject() : null;
    }

    public void setTagElement(ModelElement modelElement) {
        this.tagElement1 = modelElement != null ? new ObjectModel<>(modelElement) : null;
    }


    public TagOperation getTagOperation() {
        return tagOperation;
    }

    public void setTagOperation(TagOperation tagOperation) {
        this.tagOperation = tagOperation;
    }

    protected TagType getTagType() {
        return tagType;
    }

    protected void setTagType(TagType tagType) {
        this.tagType = tagType;
    }

    public String getMacro() {
        return macro;
    }

    public void setMacro(String macro) {
        this.macro = macro;
    }

    public Boolean getUseMacro() {
        return useMacro;
    }

    public void setUseMacro(Boolean useMacro) {
        this.useMacro = useMacro;
    }

    @Override
    public String getActionName() {
        return new StringResourceModel("actionName",this, null).getObject();
    }

    protected enum TagType {
        classifier {
            @Override
            public String toString() {
                return "classifier";
            }
        },
        attribute {
            @Override
            public String toString() {
                return "attribute";
            }
        }
    }

}
