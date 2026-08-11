package kbee.web.model;


import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;

public abstract class DataSetFactoryPanel extends Panel {
		
	private static final long serialVersionUID = 1L;

	public DataSetFactoryPanel() {
		this("new-dataset");
	}
		
	public DataSetFactoryPanel(String id) {
		super(id);
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		
		
		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onClick() {
							DataSetFactoryPanel.this.create("entity");							
						}
						@Override
						public String getLabel() {
							return new StringResourceModel("entity", DataSetFactoryPanel.this, null).getObject();
						}
						@Override
						public String getTarget() {
							return "_blank";
						}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onClick() {
							DataSetFactoryPanel.this.create("label");
						}
						@Override
						public String getLabel() {
							return new StringResourceModel("label", DataSetFactoryPanel.this, null).getObject();
						}
						@Override
						public String getTarget() {
							return "_blank";
						}
				};
			}
		});
		


		
		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onClick() {
							DataSetFactoryPanel.this.create("string");						
						}
						@Override
						public String getLabel() {
							return new StringResourceModel("string", DataSetFactoryPanel.this, null).getObject();
						}
						@Override
						public String getTarget() {
							return "_blank";
						}
				};
			}
});

		
		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onClick() {
							DataSetFactoryPanel.this.create("person");						
						}
						@Override
						public String getLabel() {
							return new StringResourceModel("person", DataSetFactoryPanel.this, null).getObject();
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

	abstract protected void create(String string);
}
