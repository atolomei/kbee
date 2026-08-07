package com.novamens.content.web.report.markup;

import com.novamens.wicket.markup.html.form.BooleanSwitchField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class ReportSubscriptionSwitchPanel extends Panel {

    public ReportSubscriptionSwitchPanel(String id, ReportSubscriptionEditor.UserSubscription userSubscription) {
        super(id);


        BooleanSwitchField components = new BooleanSwitchField("selector",
                new PropertyModel<Boolean>(userSubscription, "subscribed"),
                new PropertyModel<String>(userSubscription, "reportName"), null);

        add(components);
        add(new Label("description", ()-> userSubscription.getDescription()));
    }
}
