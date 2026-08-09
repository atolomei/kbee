package kbee.web.portal6.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.security.Identifiable;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.repeater.util.AbstractHitPanel;

public abstract class PO6HitPanel<T> extends AbstractHitPanel<T> {
									
	private static final long serialVersionUID = 1L;
	private IModel<T> model;
	
	private boolean menu = false;
	
	private boolean abs = false;
	private boolean subtitle = false;
	

	public PO6HitPanel(String id, IModel<T> model) {
		super(id);
		this.model = model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		AjaxLink<T> li = new AjaxLink<T>("link", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				PO6HitPanel.this.onClick(target);
			}
		};
			
		add(li);
		li.add(new Label("title", getTitle()));

		WebMarkupContainer wmk = new WebMarkupContainer("abstract-container");
		wmk.setVisible(isSubtitle() || isAbstract());
		
		Label s=new Label("subtitle", getSubtitle());
		s.setEscapeModelStrings(false);
		s.setVisible(isSubtitle());
		wmk.add(s);
		
		Label a=new Label("abstract", getAbstract());
		a.setEscapeModelStrings(false);
		a.setVisible(isAbstract());
		wmk.add(a);
		
		
		add(wmk);
		
		add(getMenuPanel());
		
		
	}
	
	

	
	public boolean isMenu() {
		return menu;
	}

	public void setMenu(boolean menu) {
		this.menu = menu;
	}

	public boolean isSubtitle() {
		return subtitle;
	}

	public void setSubtitle(boolean subtitle) {
		this.subtitle = subtitle;
	}


	public boolean isAbstract() {
		return abs;
	}

	public void setAbstract(boolean ab) {
		this.abs = ab;
	}
	
	protected void edit(AjaxRequestTarget target, IModel<T> model) {}

	public void onClick(AjaxRequestTarget target) {}

	
	protected IModel<String> getAbstract() {
		if (getModel()==null)
			return null;
		return new Model<String>(getModel().getObject().getClass().getName());
	}

	protected IModel<String> getSubtitle() {
		if (getModel()==null)
			return null;
		return new Model<String>(getModel().getObject().getClass().getName());
	}

	protected IModel<String> getTitle() {
		if (getModel()==null)
			return null;
		if (getModel().getObject() instanceof Identifiable)
			return  new Model<String>(((Identifiable) getModel().getObject()).getDisplayName()); 
		return new Model<String>(getModel().getObject().getClass().getName());
	}

	
	protected Panel getMenuPanel() {
		ContextMenuPanel<T> menu = new ContextMenuPanel<T>(getModel());
		menu.setOutputMarkupId(true);
		for (MenuItemFactory<T> fa:  getMenuItems(getModel()))
			menu.addItem(fa);
		return menu;

	}

	
	
		
	
	protected void moveUp(AjaxRequestTarget target, IModel<T> model) {}
	protected void moveDown(AjaxRequestTarget target, IModel<T> model) {}
	protected void archiveRestore(AjaxRequestTarget target, IModel<T> model) {}
	protected void openPortal(AjaxRequestTarget target, IModel<T> model) {}
	protected void delete(AjaxRequestTarget target, IModel<T> model) {}

	
	/**
	 * @param model
	 * @param snippets
	 * @param showLabels
	 */
	public PO6HitPanel(IModel<T> model) {
		super("hit-panel");
		this.model = model;
	}

	/**
	 * 
	 * @return
	 */
	public IModel<T> getModel() {
		return model;
	}

	/**
	 * 
	 */
	@Override
	public void onDetach() {

		if (model != null)
			model.detach();

		super.onDetach();
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
						PO6HitPanel.this.edit(target, getModel());
					}
					@Override
					public String getLabel() {
						return PO6HitPanel.this.getLabel("open-editor").getObject();
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
						PO6HitPanel.this.openPortal(target, getModel());
					}
					@Override
					public String getLabel() {
						return PO6HitPanel.this.getLabel("open-portal").getObject();
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
						PO6HitPanel.this.moveUp(target, getModel());
					}
					@Override
					public String getLabel() {
							return PO6HitPanel.this.getLabel("move-up").getObject();
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
						PO6HitPanel.this.moveDown(target, getModel());
					}
					@Override
					public String getLabel() {
						return PO6HitPanel.this.getLabel("move-down").getObject();
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
						PO6HitPanel.this.archiveRestore(target, getModel());
					}
					@Override
					public String getLabel() {
							return PO6HitPanel.this.getLabel("archive-restore").getObject();
							
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
						PO6HitPanel.this.delete(target, getModel());
					}
					@Override
					public String getLabel() {
						return PO6HitPanel.this.getLabel("delete").getObject();
					}
				};
			}
		});

		return items;
	}
 
	
}
