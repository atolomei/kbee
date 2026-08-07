package com.novamens.content.web.user.markup2;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.service.LabelsService;
import com.novamens.content.user.UserLabel;

import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;

public class UserLabelMenuItem2<T extends Content> extends AjaxMenuItemPanelV5<T> {

    private static final long serialVersionUID = 1L;
    private IModel<UserLabel> model;
    private long time = 0;
    private AjaxLink<?> link;

    public UserLabelMenuItem2(String id, IModel<UserLabel> model) {
        super(id);
        setOutputMarkupId(true);
        this.model = model;
    }

    @Override
    public String getCssClass() {
        if (isIconVisible())
            return "label-selected";
        else
            return "label-no-selected";
    }

    public void onClick(AjaxRequestTarget target) {
        long now = System.currentTimeMillis();
        if (now - time < 1000)
            return;
        time = now;
        LabelsService labelsService = getModel().getObject().getService(LabelsService.class);
        if (!labelsService.labeled(model.getObject()))
            labelsService.setLabel(model.getObject());
        else
            labelsService.removeLabel(model.getObject());
        target.add(link);

        onUpdate(target);
    }

    @Override
    public String getLabel() {
        return model.getObject().getLabel();
    }

    public void onDetach() {
        model.detach();
        super.onDetach();
    }

    public boolean isIconVisible() {
        if (getModel() == null || getModel().getObject() == null)
            return false;
        return (getModel().getObject().getService(LabelsService.class).labeled(model.getObject()));
    }

    @Override
    protected AbstractLink getNewLink(String id) {
        link = new AjaxLink<Void>(id) {
            private static final long serialVersionUID = 1L;

            public void onClick(AjaxRequestTarget target) {
                try {
                    UserLabelMenuItem2.this.onClick(target);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        return link;
    }

    public void onUpdate(AjaxRequestTarget target) {
    }
}
