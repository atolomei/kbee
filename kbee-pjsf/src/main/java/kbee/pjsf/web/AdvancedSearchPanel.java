package kbee.pjsf.web;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class AdvancedSearchPanel extends Panel {

    private static final long serialVersionUID = 1L;


    public AdvancedSearchPanel(String id) {
        super(id);

        List<ITab> tabs = Arrays.asList(

            new AbstractTab(Model.of("Fallos")) {
                @Override
                public Panel getPanel(String panelId) {
                    return new FalloSearchForm(panelId);
                }
            },

            new AbstractTab(Model.of("Sumarios")) {
                @Override
                public Panel getPanel(String panelId) {
                    return new SumarioSearchForm(panelId);
                }
            }
        );

        AjaxTabbedPanel<ITab> tabbedPanel =
                new AjaxTabbedPanel<>("tabs", tabs);

        tabbedPanel.setSelectedTab(0);
        tabbedPanel.setOutputMarkupId(true);

        add(tabbedPanel);
    }
}

