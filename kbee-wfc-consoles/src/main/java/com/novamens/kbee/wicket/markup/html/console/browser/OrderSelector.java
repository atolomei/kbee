package com.novamens.kbee.wicket.markup.html.console.browser;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.model.PropertyModel;

import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.kbee.wicket.markup.html.console.event.QueryChangeEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public class OrderSelector extends ToolbarItem {
	private static final long serialVersionUID = 1L;
	
	private NavigationOrder order;

	public OrderSelector(BaseBrowser<?> browser, Align align) {
		super(browser, align);
		
		setOutputMarkupId(true);
	
		add(new WicketEventListener<QueryChangeEvent>() {
			@Override
			public void onEvent(QueryChangeEvent event) {
				event.getRequestTarget().add(OrderSelector.this);
			}
		});
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
//		int i = 0;
		boolean selected = false;
		String sort = (String)getBrowser().getQuery().getParameters().get("sort");
		boolean ascending = "true".equals((String)getBrowser().getQuery().getParameters().get("ascending")) ? true : false;
		for (NavigationOrder order : getOrders()) {
			if (order.getProperty().equals(sort) && ascending==order.isAscending()) {
				setOrder(order);
				selected = true;
				break;
			}
		//	i++;
		}
		if (!selected) {
			for (NavigationOrder order : getOrders()) {
				if (order.getProperty().equals(sort)) {
					setOrder(order);
					selected = true;
					break;
				}
			//	i++;
			}
		}
		if (get("order")==null) {
			addChoices();
		}
	}
	
	public List<NavigationOrder> getOrders() {
		return getBrowser().getOrders();
	}
	
	public void setOrder(NavigationOrder order) {
		this.order = order;
	}
	
	public NavigationOrder getOrder() {
		return order;
	}
	
	protected void addChoices() {
		
		add(new ExtendedChoiceField<NavigationOrder>("order", new PropertyModel<NavigationOrder>(this, "order"), new PropertyModel<List<NavigationOrder>>(this, "orders")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				NavigationOrder order = getValue();
				if (order==null) return;
				getBrowser().getQuery().getParameters().put("sort", order.getProperty());
				getBrowser().getQuery().getParameters().put("ascending", order.isAscending() ? "true" : "false");
				target.add(getBrowser().getPanel(DataViewPanel.class));
			}
			@Override
			public String getIdValue(NavigationOrder value) {
				return value.getProperty();
			}
			@Override
			public String getDisplayValue(NavigationOrder value) {
				return value.getLabel();
			}
		});
	}

}
