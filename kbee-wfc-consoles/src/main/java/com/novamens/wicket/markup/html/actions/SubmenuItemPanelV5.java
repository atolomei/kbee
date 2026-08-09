package com.novamens.wicket.markup.html.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class SubmenuItemPanelV5<T> extends AbstractMenuItemPanelV5<T> {
    private static final long serialVersionUID = 1L;
    ;

    //private boolean renderhead = false;

    private List<MenuItemFactory<?>> items = new ArrayList<MenuItemFactory<?>>();
    WebMarkupContainer icon;
    WebMarkupContainer itemlink;
    WebMarkupContainer menu;
    Label itemLabel;

    WebMarkupContainer popupMenu;
    Label popupItemTitle;

    public SubmenuItemPanelV5(String id) {
        this(id, null, null, null);
    }

    public SubmenuItemPanelV5(String id, IModel<T> model) {
        this(id, model, null, null);
    }

    public SubmenuItemPanelV5(String id, String iconcss) {
        this(id, null, iconcss);
    }

    public SubmenuItemPanelV5(String id, IModel<T> model, String iconcss) {
        this(id, model, iconcss, null);
    }

    public SubmenuItemPanelV5(String id, IModel<T> model, String iconcss, String dropdownid) {
        super(id, iconcss);

        setModel(model);


        popupMenu = new WebMarkupContainer("popup-menu") {
            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
                response.render(OnDomReadyHeaderItem.forScript(getPopperScript(itemlink.getMarkupId(), this.getMarkupId())));
            }
        };
        popupItemTitle = new Label("popup-item-label", new Model<String>() {
            public String getObject() {
                return getLabel();
            }
        });

        WebMarkupContainer popupMenuList = new WebMarkupContainer("popup-menu-list");

        ListView<MenuItemFactory<?>> popupItemsView = new ListView<MenuItemFactory<?>>("item", this.items) {
            public void populateItem(ListItem<MenuItemFactory<?>> factoryitem) {
                AbstractMenuItemPanelV5<?> item = factoryitem.getModelObject().getItem("panel");
                item.setMarkupId("panel" + factoryitem.getIndex());
                item.setVisible(factoryitem.isVisible());
                item.setIndex(getIndex());
                if (item.getCssClass() != null)
                    factoryitem.add(new AttributeModifier("class", item.getCssClass()));
                factoryitem.add(item);
                factoryitem.setVisible(item.isVisible());
            }
        };
        popupMenuList.add(popupItemsView);
        popupMenu.add(popupMenuList);

        popupMenu.add(popupItemTitle);
        popupMenu.setOutputMarkupId(true);
        add(popupMenu);


        itemlink = new WebMarkupContainer("item-link");
        itemlink.setOutputMarkupId(true);

        itemLabel = new Label("item-label", new Model<String>() {
            public String getObject() {
                return getLabel();
            }
        });
        itemLabel.setOutputMarkupId(true);


        itemlink.add(itemLabel);


        icon = new WebMarkupContainer("icon") {
            @Override
            public boolean isVisible() {
                return getIconCssClass() != null;
            }
        };

        if (getIconCssClass() != null)
            icon.add(new AttributeModifier("class", getIconCssClass()));

        icon.setOutputMarkupId(true);
        itemlink.add(icon);

        add(itemlink);

        menu = new WebMarkupContainer("menu");
        ListView<MenuItemFactory<?>> itemsview = new ListView<MenuItemFactory<?>>("item", this.items) {
            public void populateItem(ListItem<MenuItemFactory<?>> factoryitem) {
                AbstractMenuItemPanelV5<?> item = factoryitem.getModelObject().getItem("panel");
                item.setMarkupId("panel" + factoryitem.getIndex());
                item.setVisible(factoryitem.isVisible());
                item.setIndex(getIndex());
                if (item.getCssClass() != null)
                    factoryitem.add(new AttributeModifier("class", item.getCssClass()));
                factoryitem.add(item);
                factoryitem.setVisible(item.isVisible());
            }
        };
        menu.setOutputMarkupId(true);
        menu.add(itemsview);
        if (dropdownid != null) {
            menu.add(new AttributeModifier("id", dropdownid));
        }
        //menuDiv.add(menu);
        add(menu);
    }

    public void addItem(MenuItemFactory<T> item) {
        this.items.add(item);
    }

    public void addModelItem(MenuItemFactory<?> item) {
        this.items.add(item);
    }


    public List<MenuItemFactory<?>> getItems() {
        return this.items;
    }


    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(OnDomReadyHeaderItem.forScript(
                "$(document).ready(function () {" +
                        "   var itemLink=$('#" + itemlink.getMarkupId() + "');" +
                        "   var popupMenu=$('#" + popupMenu.getMarkupId() + "');" +
                        "   var popperInstance=window['popper" + itemlink.getMarkupId() + "'];" +
                        "   $('#sidebar').append($('#" + popupMenu.getMarkupId() + "'));" +
                        "   itemLink.mouseenter(function () {" +
                        "           selectedSideMenuOption=itemLink.attr('id');" +
                        "           popupMenu.show();" +
                        "           popperInstance.update();" +
                        "	});" +
                        "   popupMenu.mouseenter(function () {" +
                        "      setTimeout(function(){" +
                        "           selectedSideMenuOption=itemLink.attr('id');" +
                        "      },1);" +
                        "	});" +
                        "   $.each([itemLink,popupMenu], function(idx, item) {" +
                        "           item.mouseleave(function() {" +
                        "               selectedSideMenuOption=null;" +
                        "               setTimeout(function(){" +
                        "                   if( !(selectedSideMenuOption == itemLink.attr('id')) ) { popupMenu.hide();  }" +
                        "               },10); " +
                        "       })" +
                        "   });" +
                        "});"));
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();
    }

    private String getPopperScript(String reference, String popper) {
        return "$( document ).ready(function() {\n" +
                "	window['popper" + reference + "'] = Popper.createPopper(document.querySelector('#" + reference + "'), document.querySelector('#" + popper + "')," +
                "	{" +
                "	        placement: 'right-start',\n" +
                "	        modifiers: {\n" +
                "	            preventOverflow: {\n" +
                "	                enabled: true,\n" +
                "	                boundariesElement: 'window',\n" +
                "	                escapeWithReference: true\n" +
                "	            }\n" +
                "	        }," +
                "	});" +

                "})";
    }

    @Override
    public void onClick() throws Exception {
    }

    @Override
    public String getBeforeClick() {
        return null;
    }

    @Override
    public String getCssClass() {
        return null;
    }

}
