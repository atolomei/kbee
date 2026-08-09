package kbee.web.datamanagement;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelElement;
import com.novamens.kbee.content.command.TagOperation;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.model.ObjectModel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TagManagementContentTemplateAction  extends TagManagementAction{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	IModel<ContentTemplate> contentTemplate;
    List<IModel<ContentTemplate>> contentTemplates;

    public TagManagementContentTemplateAction(String id) {
        super(id);
    }

    @Override
    public String getActionName() {
        return new StringResourceModel("contentTemplate",this, null).getObject();
    }


    @Override
    protected void onInitialize() {
        super.onInitialize();


        ChoiceField<ContentTemplate> contentTemplateField = new ChoiceField<ContentTemplate>("contentTemplate", new PropertyModel<ContentTemplate>(this, "contentTemplate"), new PropertyModel<List<ContentTemplate>>(this, "contentTemplates"), true) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getHelpText() {
                return TagManagementContentTemplateAction.this.getTagHelpText();
            }

            public void onUpdate(AjaxRequestTarget target) {
                setContentTemplate(getValue());
            }
            @Override
            protected String getDisplayValue(ContentTemplate value) {
                return value.getDisplayName();
            }
        };
        contentTemplateField.setRequired(true);
        this.add(contentTemplateField);
    }

    protected IModel<String> getTagHelpText() {
        return new StringResourceModel("contentTemplateField.help",this, null);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (contentTemplate != null)
            contentTemplate.detach();

        if (contentTemplates != null)
            contentTemplates.stream().forEach(c -> c.detach());
    }


    public void setContentTemplate(ContentTemplate contentTemplate){
        this.contentTemplate=new ObjectModel<>(contentTemplate);
    }

    public ContentTemplate getContentTemplate(){
        return contentTemplate != null ? contentTemplate.getObject() : null;
    }

    public List<ContentTemplate> getContentTemplates() {
        return this.contentTemplates.stream().map(o -> o.getObject()).collect(Collectors.toList());
    }

    public void setContentTemplates(List<ContentTemplate> contentTemplates) {
        this.contentTemplates = contentTemplates.stream().map(o -> new ObjectModel<>(o)).collect(Collectors.toList());
        this.contentTemplates.sort(new Comparator<IModel<ContentTemplate>>() {
            @Override
            public int compare(IModel<ContentTemplate> o1, IModel<ContentTemplate> o2) {
                try {
                    return o1.getObject().getName().compareToIgnoreCase(o2.getObject().getName());
                } catch (Exception e) {
                    return 0;
                }
            }
        });
    }



}
