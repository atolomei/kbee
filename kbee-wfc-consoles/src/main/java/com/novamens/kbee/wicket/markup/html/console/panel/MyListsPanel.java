package com.novamens.kbee.wicket.markup.html.console.panel;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListService;
import com.novamens.kbee.content.userlist.KbeeUserList;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
 
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.Console;
import kbee.web.error.ErrorPanel;
import kbee.web.service.ApplicationSiteMapService;

public class MyListsPanel extends KBPanel {
	
	private static final long serialVersionUID = 1L;

	static final int MAX = 60;
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MyListsPanel.class.getName());
	
	private String console = null;
	
	private List<IModel<UserList>> m_lists;
	private IModel<Site> site_model;
	private boolean isActions;
	private boolean isClose  = true;
	private boolean isClearAll = true;
	private boolean isnewlist= false;
	private Serializable selected = null;

	/**
	 * Browser<?> browser
	 * @param id
	 * @param browser
	 */
	public MyListsPanel(String id, String consoleKey, IModel<Site> site_model, boolean isActions) {
		super(id);
		setOutputMarkupId(true);
		this.selected=null;
		this.console=consoleKey;
		this.isActions=isActions;
		this.site_model=site_model;
		add(new InvisiblePanel("list-editor"));
	}

	
	public IModel<Site> getSiteModel() {
		return this.site_model;
	}
	
	public void setIsClose(boolean b) {
		this.isClose=b;
		isClearAll = isClose;
	}
	
	public boolean isClose() {
		return this.isClose;
	}
	
	
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<FilterSelectorClearAllEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(FilterSelectorClearAllEvent event) {
				selected = null;
				addLists();
				event.getRequestTarget().add(MyListsPanel.this);
			}
		});
	}
	
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("userlists")==null) 
			addLists();
	}
	
/**
 * The browser is not detached here, but in the Console.
 * 
 */
	public void onDetach() {
		super.onDetach();
		
		if (m_lists!=null)
			m_lists.forEach(item->item.detach());
		
		if (site_model!=null)
			site_model.detach();
	}
	
	/**
	 * @return
	 */
	public List<IModel<UserList>> getLists() {
		
		if (m_lists!=null)
			return m_lists;
		
		m_lists = new ArrayList<IModel<UserList>>();
		if (site_model!=null) {
			for (UserList list: ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(site_model.getObject())) {
				m_lists .add( new ObjectModel<UserList>(list));		
			}
		}
		else {
			
			for (UserList list : ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(getConsole())) {
				m_lists .add( new ObjectModel<UserList>(list));		
			}
		}
		return m_lists;
	}

	
	/**
	 * @return
	 */
	protected void addLists() {
		WebMarkupContainer actions = new WebMarkupContainer("actions") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return isActions;
			}
		};
		
		addOrReplace(actions);
		
		WorkingIndicatorAjaxLinkV5<Void> ali = new WorkingIndicatorAjaxLinkV5<Void>("addlist") {
			private static final long serialVersionUID = 1L;

			@Override
			protected String getLabel() {
				return new StringResourceModel("addlist", MyListsPanel.this, null).getObject();
				
			}
			public void onClick(AjaxRequestTarget target) {
				try {
					KbeeUser user=(KbeeUser) getSessionUser();
					if (site_model!=null) {
						KbeeUserList list = (KbeeUserList) ServiceLocator.getService(ObjectFactoryService.class).createUserList(user, site_model.getObject());
						MyListsPanel.this.addOrReplace(new UserListEditorFragment("list-editor", new ObjectModel<UserList>(list), true));
					}
					else {
						KbeeUserList list = (KbeeUserList) ServiceLocator.getService(ObjectFactoryService.class).createUserList(user, getConsole());
						MyListsPanel.this.addOrReplace(new UserListEditorFragment("list-editor", new ObjectModel<UserList>(list), true));
					}
					 
					m_lists=null;
					addLists();
					fireScanAll(new MyListsAddListEvent(target));
				} 
				catch (Exception e) {
					logger.error(e);	
				}
				target.add(MyListsPanel.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working", MyListsPanel.this, null).getObject();
			}
		};
		actions.add(ali);
		
		WorkingIndicatorAjaxLinkV5<Void> ral = new WorkingIndicatorAjaxLinkV5<Void>("remove-all") {
			private static final long serialVersionUID = 1L;
			
			
			@Override
			protected String getLabel() {
				return new StringResourceModel("removeall", MyListsPanel.this, null).getObject();
				
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					KbeeUser user=(KbeeUser) getSessionUser();
					if (site_model!=null)
						user.getService(UserListService.class).deleteAllLists(site_model.getObject());
					else
						user.getService(UserListService.class).deleteAllLists(getConsole());
					
					m_lists=null;
					addLists();
						
					fireScanAll(new MyListsRemoveAllEvent(target));
				} 
				catch (ContentMgmtException e) {
					logger.error(e);	
				}
				target.add(MyListsPanel.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working", MyListsPanel.this, null).getObject();
			}
			
			@Override
			public boolean isVisible() {
				return !getLists().isEmpty();
			}
		};
		actions.addOrReplace(ral);
		
		WorkingIndicatorAjaxLinkV5<Void> co = new WorkingIndicatorAjaxLinkV5<Void>("close") {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getLabel() {
				return new StringResourceModel("close", MyListsPanel.this, null).getObject();
			}
			
			public void onClick(AjaxRequestTarget target) {
				try {
					if (isnewlist) {
					}
					close(target);
				} 
				catch (ContentMgmtException e) {
					logger.error(e);	
				}
				
				target.add(MyListsPanel.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working", MyListsPanel.this, null).getObject();
			}
			
			@Override
			public boolean isVisible() {
				return isClose;
			}
		};
		actions.addOrReplace(co);
		
		WorkingIndicatorAjaxLinkV5<Void> cal = new WorkingIndicatorAjaxLinkV5<Void>("clear-all") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected String getLabel() {
				return new StringResourceModel("clearall", MyListsPanel.this, null).getObject();
			}

			
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					clearAll(target);
					addLists();
					selected=null;
				} 
				catch (ContentMgmtException e) {
					logger.error(e);	
				}
				target.add(MyListsPanel.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working", MyListsPanel.this, null).getObject();
			}
			
			@Override
			public boolean isVisible() {
				return isClearAll;
			}
		};
		actions.addOrReplace(cal);


		ListView<IModel<UserList>> lview = new ListView<IModel<UserList>>("userlists", new PropertyModel<List<IModel<UserList>>>(this, "lists")) {
			
			private static final long serialVersionUID = 1L;

			public void populateItem(final ListItem<IModel<UserList>> item) {
				
				WorkingIndicatorAjaxLinkV5<Void> link = new WorkingIndicatorAjaxLinkV5<Void>("link") {
					private static final long serialVersionUID = 1L;
					
					// Apply UserList (apply a filter to show its items)
					@Override
					public void onClick(AjaxRequestTarget target) {
						selected=item.getModelObject().getObject().getId();
						MyListsPanel.this.onListSelected(item.getModelObject(), target);
						target.add(MyListsPanel.this);
					}
					@Override
					protected String getWorkingLabel() {
						return new StringResourceModel("working", MyListsPanel.this, null).getObject();
					}
				};
				
				link.add(new Label("title", pad(item.getModelObject().getObject().getTitle())));
				link.add(new Label("total", "(" + String.valueOf(item.getModelObject().getObject().getTotalItems()) + ")" ));
				
				item.add(link);

				if (selected!=null && selected.toString().equals(item.getModelObject().getObject().getId().toString())) {
						item.add(new AttributeModifier("class", "list-group-item selected"));
				}
				
				item.add(getMenu(new ObjectModel<UserList>(item.getModelObject().getObject())));
			};
		};
		
		lview.setOutputMarkupId(true);
		
		addOrReplace(lview);
	}

	
	


	protected void onListSelected(IModel<UserList> iModel, AjaxRequestTarget target) {
		// TODO Auto-generated method stub
		
	}


	protected void close(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
	}

	protected String getConsole() {
		
		if (this.console!=null)
			return this.console;
		
		MarkupContainer parent = getParent();
		while (parent!=null) {
			if (parent instanceof Console<?>) {
				this.console = ((Console<?>)parent).getName();
				break;
			}
			else {
				parent = parent.getParent();
			}
		}
		return this.console;
	}
	

	protected void clearAll(AjaxRequestTarget requestTarget) {
		fireScanAll(new FilterSelectorClearAllEvent(requestTarget));
	}
	
	private String pad(String title) {
		if (title.length()>MAX) {
			return title.substring(0, MAX)+"...";
		}
		return title;
	}
	
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	
	
	
	
	
 
protected Panel getMenu(IModel<UserList> model) {
	try {
			
			ContextMenuPanel<UserList> menu = new ContextMenuPanel<UserList>(model);
									
			menu.setOutputMarkupId(true);
	
			/**
			if (getSiteModel()!=null) {
				menu.addItem(new MenuItemFactory<UserList>() {
					@Override
					public AbstractMenuItemPanelV5<UserList> getItem(String id) {
						return new MenuItemPanelV5<UserList>(id) {
							
							@Override 
							public String getLabel() {
								return  MyListsPanel.this.getLabel("share").getObject();
							}

							@Override
							public String getTarget() {
								return "_blank";
							}
							@Override
							public void onClick() throws Exception {
								try {
											setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.UserListPage,new Object[] {getModel() }));
								} 
								catch (ContentMgmtException e) {
									logger.error(e);
									//setResponsePage(new ErrorPage(e));
								}
							}
						};
					}
				});
			}
**/
			 

			
			menu.addItem(new MenuItemFactory<UserList>() {
				@Override
				public AbstractMenuItemPanelV5<UserList> getItem(String id) {
					return new AjaxMenuItemPanelV5<UserList>(id) {
						
						@Override 
						public String getLabel() {
							return  MyListsPanel.this.getLabel("apply").getObject();
						}

						@Override
						public void onClick(AjaxRequestTarget target) throws Exception {
							try {
								selected=getModel().getObject().getId();
								boolean apply=true;
								
								fire(new MyListsApplyUserListEvent(target, getModel(), apply));
								target.add(MyListsPanel.this);
							} 
							catch (ContentMgmtException e) {
								logger.error(e);	
							}
						}
					};
				}
			});
			


			
				
		menu.addItem(new MenuItemFactory<UserList>() {
				@Override
				public AbstractMenuItemPanelV5<UserList> getItem(String id) {
					return new AjaxMenuItemPanelV5<UserList>(id) {
						
						@Override 
						public String getLabel() {
							return  MyListsPanel.this.getLabel("edit-list-name").getObject();
						}

						@Override
						public void onClick(AjaxRequestTarget target) throws Exception {
							try {
								MyListsPanel.this.addOrReplace(new UserListEditorFragment("list-editor", new ObjectModel<UserList>(getModel().getObject()), false));
								target.add(MyListsPanel.this);
								
							} 
							catch (ContentMgmtException e) {
								logger.error(e);	
							}
						}
					};
				}
			});
			

			menu.addItem(new MenuItemFactory<UserList>() {
				@Override
				public AbstractMenuItemPanelV5<UserList> getItem(String id) {
					return new AjaxMenuItemPanelV5<UserList>(id) {
						
						@Override 
						public String getLabel() {
							return  MyListsPanel.this.getLabel("empty-list").getObject();
							
						}

						@Override
						public void onClick(AjaxRequestTarget target) throws Exception {
							try {
								((KbeeUser) getSessionUser()).getService(UserListService.class).deleteAllItems(getModel().getObject());
								m_lists=null;
								addLists();
								target.add(MyListsPanel.this);
								fireScanAll(new MyListsEmptyListEvent(target));
							} catch (Exception e) {
								logger.error(e);						
							}
						}
					};
				}
			});


			menu.addItem(new MenuItemFactory<UserList>() {
				@Override
				public AbstractMenuItemPanelV5<UserList> getItem(String id) {
					return new AjaxMenuItemPanelV5<UserList>(id) {
						
						@Override 
						public String getLabel() {
							return  MyListsPanel.this.getLabel("delete-list").getObject();
						}

						@Override
						public void onClick(AjaxRequestTarget target) throws Exception {
							try {
								((KbeeUser) getSessionUser()).getService(UserListService.class).delete(getModel().getObject());
								m_lists=null;
								addLists();
								target.add(MyListsPanel.this);
								fireScanAll(new MyListsDeleteListEvent(target));
								
							} catch (Exception e) {
								logger.error(e);						
							}
						}
					};
				}
			});

		 
			 
			return menu;
			
		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new ErrorPanel("menu", e);
		}
	}


	/**				
	 *
	 */
	public class UserListEditorFragment extends Fragment {
	
		private static final long serialVersionUID = 1L;
		
		IModel<String> name;
		IModel<UserList> model;
		boolean is_new = false;
		

		public UserListEditorFragment  (String id, IModel<UserList> model, boolean is_new) {
			super(id, "user-list-editor-fragment",MyListsPanel.this);
			this.model=model;
			this.is_new=is_new;
		}

		public IModel<String> getName() {
			return this.name;
		}
		
		public void setName(IModel<String> s) {
			this.name=s;
		}

		public void setModel(IModel<UserList> m) {
			this.model=m;
		}

		public IModel<UserList> getModel() {
			return this.model;
		}
		
		public void onDetach() {
			super.onDetach();
			this.model.detach();
		}

		
		/**
		 *
		 * 
		 */
		@Override
		public void onInitialize() {
			super.onInitialize();

			final Form<Void> form = new Form<Void>("form");
			add(form);
			
			String s=UserListEditorFragment.this.getModel().getObject().getTitle();
			
			UserListEditorFragment.this.setName(new Model<String>(s));
			
			TextField<String> code = new TextField<String>("name", new Model<String>() {
			
				private static final long serialVersionUID = 1L;

				public String getObject() {
					return UserListEditorFragment.this.getModel().getObject().getTitle();
				}
				
				public void setObject(String s) {
					UserListEditorFragment.this.getModel().getObject().setTitle(s);
				}
			}, true);
			

			
			TextField<String> dees = new TextField<String>("description", new Model<String>() {
				
				private static final long serialVersionUID = 1L;

				public String getObject() {
					return UserListEditorFragment.this.getModel().getObject().getDescription();
				}
				
				public void setObject(String s) {
					UserListEditorFragment.this.getModel().getObject().setDescription(s);
				}
			}, false);

			
			
			form.add(code);
			form.add(dees);
			
			
			add(new AjaxSubmitLink("save", form) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onSubmit(AjaxRequestTarget target) {
					try {
						((KbeeUser) getSessionUser()).getService(UserListService.class).save(UserListEditorFragment.this.getModel().getObject());
						fireScanAll(new MyListsUpdateListEvent(target));
						UserListEditorFragment.this.setVisible(false);
					} catch (Exception e) {
						logger.error(e);
					}
					target.add(MyListsPanel.this);
					
				}
			});
			
			add(new AjaxLink<Void>("cancel") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					if (is_new) {
						((KbeeUser) getSessionUser()).getService(UserListService.class).delete(UserListEditorFragment.this.getModel().getObject());
					}
					UserListEditorFragment.this.setVisible(false);
					target.add(MyListsPanel.this);
					
					
				}
			});
			
		}
	}

}
