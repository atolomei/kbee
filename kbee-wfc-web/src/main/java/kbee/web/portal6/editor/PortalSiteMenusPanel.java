package kbee.web.portal6.editor;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.portal.model.KbeePortalMenu;
import com.novamens.kbee.portal.model.KbeePortalMenuItem;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal.service.SiteFactoryService;
import com.novamens.portal6.model.PortalMenu;
import com.novamens.portal6.model.PortalMenuItem;
import com.novamens.portal6.model.PortalPersistentMenu;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.model.ObjectModel;


import kbee.web.event.wicket.ErrorEvent;
import kbee.web.portal6.event.PortalEditEvent;
import kbee.web.portal6.panel.PortalErrorPanel;
import kbee.web.portal6.panel.PortalPanel;
import kbee.web.portal6.panel.Site6HitPanel;

public class PortalSiteMenusPanel extends PortalPanel<Site> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalSiteMenusPanel.class.getName());
	
	private Panel errorPanel;
	private boolean show_deleted = false;
	private  Label sa;
	private  AjaxLink<Void> sd;
	private  AjaxLink<Void> sall;
	private  List<PortalPersistentMenu> list = null;
	private  WebMarkupContainer mpc; 
	private  Panel  menuEditor;
	

	public PortalSiteMenusPanel(String id, IModel<Site> model) {
		this(id, model, null);
	}
	
	
	public PortalSiteMenusPanel(String id, IModel<Site> model, Map<String, String> parameters) {
		super(id, model, parameters);
		setOutputMarkupId(true);
		
	}

	@Override
	public void onDetach() {
		super.onDetach();
		list=null;
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ErrorEvent<?>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ErrorEvent<?> event) {
					Panel err= new PortalErrorPanel<Site>("error-panel", event.getThrowable());
					errorPanel = err;
					PortalSiteMenusPanel.this.addOrReplace(errorPanel);
					event.getRequestTarget().add(PortalSiteMenusPanel.this);
			}
		});
		
		
		add(new WicketEventListener<CloseErrorPanelEvent<?>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(CloseErrorPanelEvent<?> event) {
					errorPanel = new InvisiblePanel("error-panel");
					PortalSiteMenusPanel.this.addOrReplace(errorPanel);
					event.getRequestTarget().add(PortalSiteMenusPanel.this);
			}
		});

		
		
		
		add(new WicketEventListener<PortalCloseEditAjaxEvent<?>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalCloseEditAjaxEvent<?> event) {
							if (event.getModel().getObject() instanceof PortalPersistentMenu) {
								 menuEditor =new InvisiblePanel("menu-editor");
								 PortalSiteMenusPanel.this.addOrReplace(menuEditor);
								 PortalSiteMenusPanel.this.mpc.setVisible(true);
								 event.getRequestTarget().add(PortalSiteMenusPanel.this);
							}
			}
		});
	}
	
	
	
	
	public void onInitialize() {
		super.onInitialize();
		
		
		this.menuEditor =new InvisiblePanel("menu-editor");
		add(menuEditor);
		
		this.errorPanel = new InvisiblePanel("error-panel");
		add(errorPanel);

		 
		this.mpc = new WebMarkupContainer("main-panel-container");
		add(mpc);
				

		this.sd=new AjaxLink<Void>("show-published") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				show_deleted=false;
				sd.add( new AttributeModifier ("class", "btn-mini selected"));
				sall.add( new AttributeModifier ("class", "btn-mini"));
				target.add(PortalSiteMenusPanel.this);
				
			}
		};
		
		this.mpc.add(sd);

		
		this.sall=new AjaxLink<Void>("show-all") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				show_deleted=true;
				sall.add( new AttributeModifier ("class", "btn-mini selected"));
				sd.add( new AttributeModifier ("class", "btn-mini"));
				target.add(PortalSiteMenusPanel.this);
			}
		};
		this.mpc.add(sall);
		
		
		
		sd.add( new AttributeModifier ("class", "btn-mini" + (show_deleted ? "" :" selected")));
		sall.add( new AttributeModifier ("class", "btn-mini" + (!show_deleted ? "" :" selected")));
		
		AjaxLink<Site> ap = new AjaxLink<Site>("add-menu", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					
					PortalPersistentMenu  m = ServiceLocator.getService(SiteFactoryService.class).addNewMenu(getModel().getObject(), "Menu " + String.valueOf(getModel().getObject().getMenus().size()));
					
					addTestMenu(m);
					
					
					list=null;
					if (PortalSiteMenusPanel.this.get("error-panel").isVisible()) {
							errorPanel = new InvisiblePanel("error-panel");
							PortalSiteMenusPanel.this.addOrReplace(errorPanel );
					}
					target.add(PortalSiteMenusPanel.this);
				} catch (Exception e) {
					logger.error(e);
					fire (new ErrorEvent<>(target, getModel(), e));
					
				}
			}
			
		};
		
		this.mpc.add(ap);
		
		
		ListView<PortalPersistentMenu> pages = new ListView<PortalPersistentMenu>("pages", new ListModel<PortalPersistentMenu>( new Model<Panel>(this), "items")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<PortalPersistentMenu> item) {
				IModel<PortalPersistentMenu> page_model = new ObjectModel<PortalPersistentMenu>(item.getModelObject());
				
				Site6HitPanel<PortalPersistentMenu> hp 	= new Site6HitPanel<PortalPersistentMenu>(page_model) {
					private static final long serialVersionUID = 1L;

					@Override
					protected List<MenuItemFactory<PortalPersistentMenu>> getMenuItems(IModel<PortalPersistentMenu> model) {
						return PortalSiteMenusPanel.this.getMenuItems(model);
					}
					@Override
					public void onClick(AjaxRequestTarget target) {
						 menuEditor =new PortalMenuEditor("menu-editor", getModel());
						 PortalSiteMenusPanel.this.addOrReplace(menuEditor);
						 PortalSiteMenusPanel.this.mpc.setVisible(false);
						 target.add(PortalSiteMenusPanel.this);
					}
					
					protected void edit(AjaxRequestTarget target, IModel<PortalPersistentMenu> model) {
						onClick(target);
					}
					
					@Override
					public boolean isAbstract() {
						return false;
					}
					
					@Override
					protected IModel<String> getSubtitle() {
						
						try {
							StringBuilder str = new StringBuilder();
							if (getModel().getObject().getState()!=ObjectState.ENABLED)
								str.append(getModel().getObject().getState().getHTMLLabel(getSessionUser().getLocale()));
						
						return new Model<String>(str.toString());
						} catch (Exception e) {
							logger.error(e);
							return new Model<String>(e.getClass().getName());
						}
					}

				};
				
				hp.setSubtitle(true);
				hp.setAbstract(false);
				item.add(hp);
			}
		};
		this.mpc.add(pages);
		
	}
			

	 
	

	/**
	 * 
	 * @return
	 */
	public List<PortalPersistentMenu> getItems() {
	
		if (list!=null)
			return list;
		
		list =  new ArrayList<PortalPersistentMenu>();
		
		for (PortalPersistentMenu m: getModel().getObject().getMenus()) {
			if (!this.show_deleted) {
					if (m.getState()==ObjectState.ENABLED)
						list.add(m);
			}
			else {
				list.add(m);
			}
		}
		
		
		list.sort(new Comparator<PortalPersistentMenu>() {

			@Override
			public int compare(PortalPersistentMenu o1, PortalPersistentMenu o2) {
				
				try {
					
					//if (o1.getOrder()<o2.getOrder()) return -1;
					//if (o1.getOrder()>o2.getOrder()) return 1;
					
					if (o1.getTitle()==null) return 1;
					if (o2.getTitle()==null) return -1;
					
					return o1.getTitle().compareToIgnoreCase(o2.getTitle());
					
				} catch (Exception e) {
					logger.error(e);
				}
				return 0;
			}
			
		});
		
		return  list;
		
	}
	
	/**
	 * 
	 * 
	 * @param model
	 * @return
	 */
									
	protected List<MenuItemFactory<PortalPersistentMenu>> getMenuItems(IModel<PortalPersistentMenu> model) {
		
		List<MenuItemFactory<PortalPersistentMenu>> items = new ArrayList<MenuItemFactory<PortalPersistentMenu>>();

		items.add(new MenuItemFactory<PortalPersistentMenu>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<PortalPersistentMenu> getItem(String id) {
				return new AjaxMenuItemPanelV5<PortalPersistentMenu>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						fire (new PortalEditEvent<PortalPersistentMenu>(getModel()));
					}
					@Override
					public String getLabel() {
							return "Open Editor";
					}
				};
			}
		});
	

	
		items.add(new MenuItemFactory<PortalPersistentMenu>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<PortalPersistentMenu> getItem(String id) {
				return new AjaxMenuItemPanelV5<PortalPersistentMenu>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						try {
							// towards 0
							getModel().getObject().getSite().moveDown(getModelObject());
							getModel().getObject().getSite().getService(SiteService.class).update("menu up "+ getModelObject().getName());
						} catch (Exception e) {
							logger.error(e);
							fire (new ErrorEvent<>(target, e));
						}
						target.add( PortalSiteMenusPanel.this);
						

					}
					@Override
					public String getLabel() {
							return "Move up";
					}
				};
			}
		});


		items.add(new MenuItemFactory<PortalPersistentMenu>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<PortalPersistentMenu> getItem(String id) {
				return new AjaxMenuItemPanelV5<PortalPersistentMenu>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						try {
							// towards the end
							getModel().getObject().getSite().moveUp(getModelObject());
							getModel().getObject().getSite().getService(SiteService.class).update("menu down "+ getModelObject().getName());
						
						} catch (Exception e) {
							logger.error(e);
							fire (new ErrorEvent<>(target, e));
							//setResponsePage(new ErrorPage<PortalPersistentMenu>(e));
						}
						target.add( PortalSiteMenusPanel.this);
						
					}
					@Override
					public String getLabel() {
							return "Move down";
					}
				};
			}
		});


		items.add(new MenuItemFactory<PortalPersistentMenu>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<PortalPersistentMenu> getItem(String id) {
				return new AjaxMenuItemPanelV5<PortalPersistentMenu>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						try {
												
							PortalPersistentMenu menu =  getRepository(PortalPersistentMenu.class).findById(getModel().getObject().getId());
							
							if (menu==null) 
								throw new  ContentMgmtException("menu is null for id -> " + getModel().getObject().getId().toString());
							
							if ( menu.getState()==ObjectState.ENABLED) {						
								menu.setState(ObjectState.ARCHIVED);
								menu.getSite().getService(SiteService.class).update("menu "+ getModelObject().getTitle()+ " - Set page status ->  "+ getModelObject().getState().toString());
							}
							else {				
								menu.setState(ObjectState.ENABLED);
								menu.getSite().getService(SiteService.class).update("menu "+ getModelObject().getTitle()+" - Set status ->  "+ getModelObject().getState().toString());
							}
							} catch (Exception e) {
								logger.error(e);
								fire (new ErrorEvent<>(target, e));
								
							}
						target.add( PortalSiteMenusPanel.this);
						
					}
					@Override
					public String getLabel() {
						if (getModelObject().getState()==ObjectState.ENABLED) 
								return new StringResourceModel("archive", PortalSiteMenusPanel.this, null).getObject();
						else
							return new StringResourceModel("restore", PortalSiteMenusPanel.this, null).getObject();
					}
				};
			}
		});

		items.add(new MenuItemFactory<PortalPersistentMenu>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<PortalPersistentMenu> getItem(String id) {
				return new AjaxMenuItemPanelV5<PortalPersistentMenu>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
					try {	
						
						String t=getModelObject().getTitle()!=null? getModelObject().getTitle(): ("id: " +  getModelObject().getId().toString());
						getModel().getObject().setState(ObjectState.DELETED);
						getModel().getObject().getSite().getService(SiteService.class).update("page "+ t + " - Set page status ->  "+ getModelObject().getState().toString());
						
						} catch (Exception e) {
							logger.error(e);
							fire (new ErrorEvent<>(target, e));
													}
						target.add( PortalSiteMenusPanel.this);
	
					}
					@Override
					public String getLabel() {
							return "Send to Recycle Bin";
					}
					
					@Override
					public boolean isEnabled() {
						return getModel().getObject().getState()!=ObjectState.DELETED;
					}
					
					@Override
					public boolean isVisible() {
						return getModel().getObject().getState()!=ObjectState.DELETED;
					}
				};
			}
		});

		
		items.add(new MenuItemFactory<PortalPersistentMenu>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<PortalPersistentMenu> getItem(String id) {
				return new AjaxMenuItemPanelV5<PortalPersistentMenu>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
					try {	
						String t=getModelObject().getTitle()!=null? getModelObject().getTitle(): ("id: " +  getModelObject().getId().toString());
						getModel().getObject().getSite().remove(getModelObject());
						getModel().getObject().getSite().getService(SiteService.class).update("menu "+ t + " - Menu deleted  ");
						
						target.add( PortalSiteMenusPanel.this);
						} catch (Exception e) {
							logger.error(e);
							fire (new ErrorEvent<>(target, e));
							target.add( PortalSiteMenusPanel.this);
						}
					}
					@Override
					public String getLabel() {
						return new StringResourceModel("delete", PortalSiteMenusPanel.this, null).getObject();
					}
					
					@Override
					public boolean isVisible() {
						return getModel().getObject().getState()==ObjectState.DELETED;
					}
				};
			}
		});
		return items;
	}
	
	 

	private void addTestMenu(PortalPersistentMenu menu) {
		
		// 1. Servicios
		PortalMenu	   servicios = new KbeePortalMenu("Servicios");
		PortalMenuItem servicios_1 = new KbeePortalMenuItem("Servicios 1");
		PortalMenuItem servicios_2 = new KbeePortalMenuItem("Servicios 2");
		PortalMenuItem servicios_3 = new KbeePortalMenuItem("Servicios 3");
		servicios.add(servicios_1);
		servicios.add(servicios_2);
		servicios.add(servicios_3);

		PortalMenuItem i2 = new KbeePortalMenuItem("Cómo funciona");
		PortalMenuItem i3 = new KbeePortalMenuItem("Quienes Somos");
		PortalMenuItem i4 = new KbeePortalMenuItem("Precios");
		PortalMenuItem i5 = new KbeePortalMenuItem("Contacto");

		
		// 6. Productos
		PortalMenu productos 	= new KbeePortalMenu("Productos");
		PortalMenuItem productos_1 = new KbeePortalMenuItem("Producto 1");
		PortalMenuItem productos_2 = new KbeePortalMenuItem("Producto 2");
		PortalMenuItem productos_3 = new KbeePortalMenuItem("Producto 3");
		productos.add(productos_1);
		productos.add(productos_2);
		productos.add(productos_3);
		
		// 6.1. Subproductos
		PortalMenu productos_sub 	= new KbeePortalMenu("P1 Subproductos");
		PortalMenuItem productos_sub_1 = new KbeePortalMenuItem("P1 SubProducto 1");
		PortalMenuItem productos_sub_2 = new KbeePortalMenuItem("P1 SubProducto 2");
		PortalMenuItem productos_sub_3 = new KbeePortalMenuItem("P1 SubProducto 3");
		productos_sub.add(productos_sub_1);
		productos_sub.add(productos_sub_2);
		productos_sub.add(productos_sub_3);
		productos.add(productos_sub);
		
		
		menu.add(servicios);
		menu.add(i2);
		menu.add(i3);
		menu.add(i4);
		menu.add(i5);
		menu.add(productos);

		// menu.getSite().getService(SiteService.class).update(menu, "menu");
		
		
		
	}
}
