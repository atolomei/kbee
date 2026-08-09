package com.novamens.kbee.wicket.markup.html.console.panel;


import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.model.ProxyModel;

public class MyListItemsPanel extends KBPanel {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MyListItemsPanel.class.getName());

	private static final long serialVersionUID = 1L;
	
	private static final int MAX = 100;
	
	private boolean isActions = true;
	private boolean isClose = true;
	
	private IModel<UserList> model;
	private boolean targetBlank = true;
	private String consoleKey;
	ListView<UserListItem> lview;
	
	
	public void setTargetBlank( boolean b) {
		this.targetBlank=b;
	}
	
	public MyListItemsPanel(String id, IModel<UserList> model, 	boolean isActions, String consoleKey) {
		super(id, model);
		this.model=model;
		this.isActions=isActions;
		this.consoleKey=consoleKey;
	}

	public String getConsoleKey() {
		return consoleKey;
	}
	
	public void setIsClose(boolean b) {
		this.isClose=b;
	}
	
	public boolean isClose() {
		return this.isClose;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		WebMarkupContainer actions = new WebMarkupContainer("actions") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return isActions;
			}
		};
		add(actions);
		
		WorkingIndicatorAjaxLinkV5<Void> co = new WorkingIndicatorAjaxLinkV5<Void>("close") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected String getLabel() {
				return new StringResourceModel("close", MyListItemsPanel.this, null).getObject();
			}
			
			public void onClick(AjaxRequestTarget target) {
				try {
					close(target);
				} 
				catch (ContentMgmtException e) {
					logger.error(e);	
				}
				target.add(MyListItemsPanel.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working", MyListItemsPanel.this, null).getObject();
			}
			
			@Override
			public boolean isVisible() {
				return  isClose();
			}
		};
		actions.add(co);
		load();
		
	}
	
	private void load() {
		
		this.lview = new ListView<UserListItem>("list-items", new PropertyModel<List<UserListItem>>(this, "items")) {
			
			private static final long serialVersionUID = 1L;

			public void populateItem(final ListItem<UserListItem> item) {
				
				Link<?> link = new Link<Void>("link") {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick() {
						try {
							UserListItem ulist = item.getModel().getObject();
							open(ulist, null);
						} catch (Exception e) {
							logger.error(e);
						}
					}
				};
				
				if (isTargetBlank())
					link.add(new AttributeModifier("target", "_blank"));
				
				try {
					com.novamens.dom.Object ob= (com.novamens.dom.Object) item.getModelObject().getObject();
					link.add(new Label("title", pad(ob.getDisplayName()!=null?ob.getDisplayName(): ob.getId().toString())));
				} catch (Exception e) {
					link.add(new Label("title",  e.getClass().getSimpleName()));
					logger.error(e);
				}
				
				try {
					UserListItem ob = item.getModelObject();
					 Classificable c = ((Classificable) ob.getObject());
					 if (c instanceof Content) {			
						 link.add(new Label("subtitle", pad(((Content) c).getContentTypeClassificationAsString())));	 
					 }
					 else
						 link.add( (new Label("subtitle", "")).setVisible(false));
					
					
				} catch (Exception e) {
					link.add(new Label("subtitle",  e.getClass().getSimpleName()));
					logger.error(e);
				}
				item.add(link);				
				item.add(getMenu(item.getModel()));
			};
		};
		lview.setOutputMarkupId(true);
		addOrReplace(lview);
	}
	
	
	protected boolean isTargetBlank() {
		return targetBlank;
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected void open(UserListItem ulist, AjaxRequestTarget target) {
		com.novamens.dom.Object o = (com.novamens.dom.Object) ulist.getObject();
		if (o instanceof DataSetMember) {
			DataSetMember dm = (DataSetMember) o;
			DataSetMember dm1 = (DataSetMember) getContentDao().reload(dm);
			fire (new ClickEvent<DataSetMember>( target, new ObjectModel<DataSetMember>(dm1), 0, getConsoleKey()));
		}
		else if (o instanceof Content) {
			Content c= (Content) getContentDao().reload(o);
			fireScanAll (new ClickEvent<Content>( target, new ProxyModel<Content>(c), 0, getConsoleKey()));
		}
		else if (o instanceof User) {
			User c= (User) o;
			fire (new ClickEvent<User>( target, new ObjectModel<User>( (User) getContentDao().reload(c)), 0, getConsoleKey()));
		}
		else  {
			fire (new ClickEvent<com.novamens.dom.Object>( target, new ObjectModel<com.novamens.dom.Object>(o), 0, getConsoleKey()));
		}
	}

	/**
	 * 
	 * 
	 * 
	 * @param objectModel
	 * @return
	 */
	protected Panel getMenu(IModel<UserListItem> objectModel) {

			try {

				ContextMenuPanel<UserListItem> menu = new ContextMenuPanel<UserListItem>(objectModel);
					menu.setOutputMarkupId(true);
					menu.addItem(new MenuItemFactory<UserListItem>() {
						private static final long serialVersionUID = 1L;
						@Override
						public AbstractMenuItemPanelV5<UserListItem> getItem(String id) {
							return new AjaxMenuItemPanelV5<UserListItem>(id) {
								private static final long serialVersionUID = 1L;
								@Override 
								public String getLabel() {
									return  MyListItemsPanel.this.getLabel("open").getObject();
								}
								@Override
								public void onClick(AjaxRequestTarget target) throws Exception {
									try {
										UserListItem ulist= getModel().getObject();
										open(ulist, target);
									} 
									catch (Exception e) {
										logger.error(e);	
									}
								}
							};
						}
					});
					
					menu.addItem(new MenuItemFactory<UserListItem>() {
						private static final long serialVersionUID = 1L;
						@Override
						public AbstractMenuItemPanelV5<UserListItem> getItem(String id) {
							return new AjaxMenuItemPanelV5<UserListItem>(id) {
								
								 
								private static final long serialVersionUID = 1L;

								@Override 
								public String getLabel() {
									return  MyListItemsPanel.this.getLabel("remove").getObject();
								}

								@Override
								public void onClick(AjaxRequestTarget target) throws Exception {
									try {
										
										UserList list = getModel().getObject().getUserlist();
										list.removeItem(getModel().getObject());
										((KbeeUser) list.getOwner()).getService(UserListService.class).save(list);
										
										target.add(MyListItemsPanel.this);
										
										fireScanAll(
														new MyListsUserListItemUpdateObjectEvent<com.novamens.dom.Object>( 
														target, 
														new ObjectModel<com.novamens.dom.Object>( (com.novamens.dom.Object) getModel().getObject().getObject()),   
														new ObjectModel<UserList>(list)
													)
												);
									
										load();
										
									} 
									catch (Exception e) {
										logger.error(e);	
									}

								}
							};
						}
					});
					
					return menu;
					
				} catch (Exception e) {
					return new InvisiblePanel("menu");
				}
	}
	
	public List<UserListItem> getItems() {
		List<UserListItem> list = getModel().getObject().getItems();
		list.sort(new Comparator<UserListItem>() {
			@Override
			public int compare(UserListItem a, UserListItem b) {
				try  { 
					return (((com.novamens.dom.Object) a.getObject()).getDisplayName().compareToIgnoreCase(((com.novamens.dom.Object) b.getObject()).getDisplayName()));
					
				} catch (Exception e) {
					return 0;
				}
			}
		});
		return list;
	}
	
	public IModel<UserList> getModel() {
		return this.model;
	}
	
	public void onDetach() {
		super.onDetach();
		this.model.detach();
	}


	private String pad(String title) {
		if (title!=null && title.length()>MAX) {
			return title.substring(0, MAX)+"...";
		}
		return title;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	protected void close(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
	}
	
	
}
