package com.novamens.content.web.security.markup;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.SecurityRule;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;


@SuppressWarnings("serial")
public abstract class RuleFactoryPanel extends Panel {
				
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public RuleFactoryPanel() {
		this("new-rule");
	}
	

		
	public RuleFactoryPanel(String id) {
		super(id);
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		WebMarkupContainer  newm = new WebMarkupContainer ("new-multiple-button");
		newm.add(new AttributeModifier("class", "btn-md btn btn-primary dropdown-toggle"));
		newm.add(new AttributeModifier("data-toggle", "dropdown"));
		add(newm);
		
		menu.addItem(new MenuItemFactory<Void>() {
						/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

						@Override
						public AbstractMenuItemPanelV5<Void> getItem(String id) {
							return new MenuItemPanelV5<Void>(id) {
								@Override
								public void onClick() {
									onCreate(SecurityRule.RULE_WIZARD_IQL);
								}
								@Override
								public String getLabel() {
									return new StringResourceModel("wizard", RuleFactoryPanel.this, null).getObject();
								}
								@Override
								public String getTarget() {
									return "_blank";
								}
							};
						}
		});
		
		
		menu.addItem(new MenuItemFactory<Void>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new MenuItemPanelV5<Void>(id) {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick() {
							onCreate(SecurityRule.RULE_COLLOQUIAL_IQL);
					}
					@Override
					public String getLabel() {
						return new StringResourceModel("colloquial", RuleFactoryPanel.this, null).getObject();
					}
					@Override
					public String getTarget() {
						return "_blank";
					}
				};
			}
		});
		
		add(menu);
		
	}

	protected abstract void onCreate(int type);

}
