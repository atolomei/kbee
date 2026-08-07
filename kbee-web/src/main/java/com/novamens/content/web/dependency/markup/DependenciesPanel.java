package com.novamens.content.web.dependency.markup;

import com.novamens.content.web.report.markup.ReportSubscriptionEditor;
import com.novamens.kbee.dependencies.Dependency;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DependenciesPanel extends Panel {


    public DependenciesPanel(String id, Map<String, Dependency> dependency) {
        super(id);

        this.add(new ListView<Dependency>("dependencies", new ArrayList(dependency.values())) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Dependency> item) {
                Dependency dep = (Dependency) item.getDefaultModelObject();

                Label lbl = new Label("dependencyRow", ()-> dep.getTargetLocator().getDescription());
                item.add(lbl);
            }
        });

    }




}
