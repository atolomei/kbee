package kbee.web.portal6.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.portal6.model.PortalObject;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;

import kbee.web.portal6.event.PortalOpenAjaxEvent;

public class Site6HitPanel<T extends PortalObject> extends PO6HitPanel<T> {

	private static final long serialVersionUID = 1L;
	
	public Site6HitPanel(String id, IModel<T> model) {
		super(id, model);
	}

	public Site6HitPanel(IModel<T> model) {
		super(model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	protected List<MenuItemFactory<T>> getMenuItems(IModel<T> model) {
		
		List<MenuItemFactory<T>> items = new ArrayList<MenuItemFactory<T>>();

		items.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						Site6HitPanel.this.edit(target, getModel());
					}
					@Override
					public String getLabel() {
						return Site6HitPanel.this.getLabel("open-editor").getObject();
					}
				};
			}
		});
		
		
		items.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						Site6HitPanel.this.openPortal(target, getModel());
					}
					@Override
					public String getLabel() {
						return Site6HitPanel.this.getLabel("open-portal").getObject();
					}
				};
			}
		});
		
		items.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						Site6HitPanel.this.moveUp(target, getModel());
					}
					@Override
					public String getLabel() {
							return Site6HitPanel.this.getLabel("move-up").getObject();
					}
				};
			}
		});

		
		items.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						Site6HitPanel.this.moveDown(target, getModel());
					}
					@Override
					public String getLabel() {
						return Site6HitPanel.this.getLabel("move-down").getObject();
					}
				};
			}
		});


		items.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						Site6HitPanel.this.archiveRestore(target, getModel());
					}
					@Override
					public String getLabel() {
							return Site6HitPanel.this.getLabel("archive-restore").getObject();
							
					}
				};
			}
		});


		items.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						Site6HitPanel.this.delete(target, getModel());
					}
					@Override
					public String getLabel() {
						return Site6HitPanel.this.getLabel("delete").getObject();
					}
				};
			}
		});

		return items;
	}

	@Override
	protected void openPortal(AjaxRequestTarget target, IModel<T> model) {
		fire (new PortalOpenAjaxEvent<T>(target, getModel()));
	}
	

	@Override
	protected IModel<String> getAbstract() {
		return new Model<String>(getModel().getObject().getMetadataAsString());
	}

	@Override
	protected IModel<String> getSubtitle() {
		return new Model<String>(getModel().getObject().getMetadataAsString());
	}

	@Override
	protected IModel<String> getTitle() {
		return new Model<String>(getModel().getObject().getDisplayName());
	}

	


}
