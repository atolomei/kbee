package kbee.web.panel;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

import kbee.web.workflow.FeedbackPanel;

public class ToastPanel extends FeedbackPanel {

    public ToastPanel(String id) {
        super(id);

        setOutputMarkupId(true);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(
            CssHeaderItem.forReference(
                new CssResourceReference(
                    ToastPanel.class,
                    "toast-panel.css"
                )
            )
        );
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();

        getSession().getFeedbackMessages().clear();
    }
}