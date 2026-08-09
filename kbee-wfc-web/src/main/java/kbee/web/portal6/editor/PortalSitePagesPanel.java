package kbee.web.portal6.editor;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal.service.SiteFactoryService;

import com.novamens.portal6.model.Page;

import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.portal6.event.PortalEditEvent;
import kbee.web.portal6.panel.PortalErrorPanel;
import kbee.web.portal6.panel.PortalPanel;
import kbee.web.portal6.panel.Site6HitPanel;


public class PortalSitePagesPanel extends PortalPanel<Site> {
	
private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalSitePagesPanel.class.getName());
							
	private static final long serialVersionUID = 1L;

	private List<Page> list = null;
	
	private Panel errorPanel;
	private boolean show_deleted = false;
	private  Label sa;
	private  AjaxLink<Void> sd;

	
	
	public PortalSitePagesPanel(String id, IModel<Site> model) {
		super(id, model);
		setOutputMarkupId(true);
		
	}

	
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
					PortalSitePagesPanel.this.addOrReplace(err);
					event.getRequestTarget().add(PortalSitePagesPanel.this);
			}
		});
	}
	
	/**
	 * Page
	 * Area
	 * Block
	 * [Menu][title]
	 * [Subtitle]
	 * [description]
	 * 
	 *  add (new SiteBCPanel("bc.site-pages", getModel()));
	 */
	
	
		
	public void onInitialize() {
		super.onInitialize();
		
		
		errorPanel = new InvisiblePanel("error-panel");
		add(errorPanel);

		Link<Void> ss= new Link<Void>("site-sections") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				
				Page page=PortalSitePagesPanel.this.getModel().getObject().getTopBottomSectionPage();
				if (page!=null)
					fire (new PortalEditEvent<Page>(new ObjectModel<Page>(page)));
				else
					setResponsePage(new ApplicationErrorPage<Site>( 
							new Model<String>(PortalSitePagesPanel.this.getModel().getObject().toString()), 
							new Model<String>("Site Sections Page does not exist")
					));
			}
		};
		add(ss);
		
		

		 sd=new AjaxLink<Void>("show-deleted") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				show_deleted=!show_deleted;	
				sa=new Label("show-all", show_deleted? "view published":"view all");
				sd.addOrReplace(sa);
				target.add(PortalSitePagesPanel .this);
			}
		};
		
		sa=new Label("show-all", show_deleted? "view published":"view all");
		sd.add(sa);
		
		add(sd);
		

		
		AjaxLink<Site> ap = new AjaxLink<Site>("add-page", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					ServiceLocator.getService(SiteFactoryService.class).addNewPage(getModel().getObject(), "Page " + String.valueOf(getModel().getObject().getPages().size()));
					list=null;
					target.add(PortalSitePagesPanel.this);
				} catch (Exception e) {
					logger.error(e);
					fire (new ErrorEvent(target, e));
					//setResponsePage( new ErrorPage<Void>(e));
					
				}
			}
			
		};
		
		add(ap);
		
		
		ListView<Page> pages = new ListView<Page>("pages", new ListModel<Page>( new Model<Panel>(this), "items")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<Page> item) {
				IModel<Page> page_model = new ObjectModel<Page>(item.getModelObject());
				
				Site6HitPanel<Page> hp 	= new Site6HitPanel<Page>(page_model) {
					private static final long serialVersionUID = 1L;

					@Override
					protected List<MenuItemFactory<Page>> getMenuItems(IModel<Page> model) {
						return PortalSitePagesPanel.this.getMenuItems(model);
					}
					@Override
					public void onClick(AjaxRequestTarget target) {
						fire (new PortalEditEvent<Page>(getModel()));
					}
					
					@Override
					public boolean isAbstract() {
						return false;
					}
					
					@Override
					protected IModel<String> getSubtitle() {
						
						try {
							StringBuilder str = new StringBuilder();

							if (getModel().getObject().isHome())
								str.append(new StringResourceModel("home", this, null).getObject()+" ");

							if (!getModel().getObject().isRegularPage())
								str.append(new StringResourceModel("sections", this, null).getObject() + " ");

							if (!getModel().getObject().isBuildable())
								str.append( new StringResourceModel("not-buildable", PortalSitePagesPanel.this, null).getObject() + " ");
							                                        
							
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
		add(pages);
	}
			

	 
	

	/**
	 * 
	 * @return
	 */
	public List<Page> getItems() {
	
		if (list!=null)
			return list;
		
		list =  new ArrayList<Page>();
		
		
		for (Page page: getModel().getObject().getPages()) {
			
			
			if (!page.isSiteSection()) {
			
				if (!this.show_deleted) {
						if (page.getState()==ObjectState.ENABLED)
							list.add(page);
				}
				else {
					list.add(page);
				}
			
			}
		}
		
		
		list.sort(new Comparator<Page>() {

			@Override
			public int compare(Page o1, Page o2) {
				
				try {
					
					if (o1.getOrder()<o2.getOrder()) return -1;
					if (o1.getOrder()>o2.getOrder()) return 1;
					
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
	
	
	protected List<MenuItemFactory<Page>> getMenuItems(IModel<Page> model) {
		
		List<MenuItemFactory<Page>> items = new ArrayList<MenuItemFactory<Page>>();

		items.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						fire (new PortalEditEvent<Page>(getModel()));
						// Site6HitPanel.this.edit(target, getModel());
					}
					@Override
					public String getLabel() {
							return "Open Editor";
					}
				};
			}
		});
	

		items.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						fire (new PortalEditEvent<Page>(getModel()));
						//fire (new PortalOpenAjaxEvent<Page>(target, getModel())); 
					}
					@Override
					public String getLabel() {
							return "Open in Portal";
					}
				};
			}
		});
		

		items.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						try {
							
							// towards 0
							getModel().getObject().getSite().moveDown(getModelObject());
							getModel().getObject().getSite().getService(SiteService.class).update("page up "+ getModelObject().getName());
							
							
						} catch (Exception e) {
							logger.error(e);
							fire (new ErrorEvent(target, e));
							//setResponsePage(new ErrorPage(e));
						}
						target.add(PortalSitePagesPanel.this);
						

					}
					@Override
					public String getLabel() {
							return "Move up";
					}
				};
			}
		});


		items.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						try {
							// towards the end
							getModel().getObject().getSite().moveUp(getModelObject());
							getModel().getObject().getSite().getService(SiteService.class).update("page down "+ getModelObject().getName());
						
						} catch (Exception e) {
							logger.error(e);
							fire (new ErrorEvent(target, e));
							//setResponsePage(new ErrorPage<Page>(e));
						}
						target.add(PortalSitePagesPanel.this);
						
					}
					@Override
					public String getLabel() {
							return "Move down";
					}
				};
			}
		});


		items.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						try {
							Page page = getPortalDao().findPageById( getModel().getObject().getId());
							
							if ( page.getState()==ObjectState.ENABLED) {						
								 page.setState(ObjectState.ARCHIVED);
								 page.getSite().getService(SiteService.class).update("page "+ getModelObject().getTitle()+ " - Set page status ->  "+ getModelObject().getState().toString());
							}
							else {				
								 page.setState(ObjectState.ENABLED);
								 page.getSite().getService(SiteService.class).update("page "+ getModelObject().getTitle()+" - Set status ->  "+ getModelObject().getState().toString());
							}
							} catch (Exception e) {
								logger.error(e);
								fire (new ErrorEvent(target, e));
								
							}
						target.add(PortalSitePagesPanel.this);
						
					}
					@Override
					public String getLabel() {
						if (getModelObject().getState()==ObjectState.ENABLED) 
								return "Archive";
						else
							return "Restore";
					}
				};
			}
		});

		items.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
					try {	
						
						Page page = getPortalDao().findPageById(getModel().getObject().getId());
						page.setState(ObjectState.DELETED);											
						page.getSite().getService(SiteService.class).update("page "+ page.getName() + " - Set page status ->  "+ ObjectState.DELETED.getLabel());
						
						} catch (Exception e) {
							logger.error(e);
							fire (new ErrorEvent<>(target, e));
													}
						target.add(PortalSitePagesPanel.this);
	
					}
					@Override
					public String getLabel() {
							return "Send to Recycle Bin";
					}
					
					@Override
					public boolean isEnabled() {
						return getModel().getObject().isRegularPage();
					}
					
					@Override
					public boolean isVisible() {
						return getModel().getObject().getState()!=ObjectState.DELETED;
					}
				};
			}
		});

		
		items.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, model) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
					try {	
						String t=getModelObject().getTitle()!=null? getModelObject().getTitle(): ("id: " +  getModelObject().getId().toString());
						getModel().getObject().getSite().remove(getModelObject());
						getModel().getObject().getSite().getService(SiteService.class).update("page "+ t + " - Page deleted  ");
						
						target.add(PortalSitePagesPanel.this);
						} catch (Exception e) {
							logger.error(e);
							target.add(PortalSitePagesPanel.this);
							// setResponsePage(new ErrorPage<Site>(e));
						}
					}
					@Override
					public String getLabel() {
							return "Delete";
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


	
	



}
