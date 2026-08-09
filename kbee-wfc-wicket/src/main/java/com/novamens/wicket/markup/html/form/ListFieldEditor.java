package com.novamens.wicket.markup.html.form;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.Dialog;

 

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import java.util.List;
import java.util.stream.Collectors;

import static com.cronutils.model.CronType.QUARTZ;

public class ListFieldEditor<T> extends Field<List<T>> {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ListFieldEditor.class.getName());
	
    public ListFieldEditor(String id, IModel<String> fieldName, IModel<List<T>> items) {
        super(id, items);
    	
        WebMarkupContainer sub_container;
		Label subtitle;
			
			 sub_container = new  WebMarkupContainer("subtitle-container");
			 add(sub_container);
			 sub_container.setVisible(  getSubtitle()!=null );
			 subtitle = new Label("subtitle", getSubtitle());
			 subtitle.setEscapeModelStrings(false);
			 sub_container.add(subtitle);
			 
			 
        
        
        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);

        final ListView<T> exportsList = new ListView<T>("items", items) {
            @Override
            protected void populateItem(ListItem<T> listItem) {
                listItem.add(new Label("itemLabel", listItem.getModel()));
                listItem.add(new AjaxLink<Void>("removeItem") {
                    public void onClick(AjaxRequestTarget target) {
                        ListFieldEditor.this.getModel().getObject().remove(listItem.getModel().getObject());
                        target.add(container);
                    }
                });
            }
        };

        container.add(exportsList);

        container.add(new Label("label", fieldName).setEscapeModelStrings(false));

        container.add(new WebMarkupContainer("mandatory") {
			public boolean isVisible() {
				return ListFieldEditor.this.isRequired();
			}
		});	

		
        
        final Field<T> valueAddField = this.getAddValueEditor("valueAdd");
        valueAddField.setModel(()->(T)"");
        valueAddField.setOutputMarkupId(true);
        container.add(valueAddField);
        container.add(new AjaxLink<Void>("btnAdd") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                final T value = valueAddField.getValue();
                if(value!=null) {
                    ListFieldEditor.this.getModel().getObject().add(value);
                    //valueAddField.setModel(null);
                    ajaxRequestTarget.add(container);
                }

            }
        });
        add(container);
        
		if (getTabIndex()>0)
			container.add(new AttributeModifier("tabindex", getTabIndex()));

		
    }

    public Field<T> getAddValueEditor(String id){
        TextField<T> component = new TextField<>(id);
        component.setPlaceholderLabel(true);
        return component;
    }



}
