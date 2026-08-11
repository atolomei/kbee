package kbee.web.dashboard;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.library.Library;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketAjaxEvent;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ApplicationErrorPage;

@SuppressWarnings("serial")
public abstract class LibraryWidgetHeaderPanel extends KBPanel {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LibraryWidgetHeaderPanel.class.getName());
	
	private static final long serialVersionUID = 1L;

	private IModel<Library> librarymodel;
	private List<IModel<Library>> libraries = null;
	
	private IModel<String> title;
	private IModel<User> model_session_user = null;
	
	private boolean isEdit  = false;
	private boolean isHelp  = false;
	private boolean isViewMode  = false;
	
	private boolean isCollapsed  = false;
	private WebMarkupContainer icon;	
	
	public LibraryWidgetHeaderPanel(String id, IModel<String> title, IModel<Library> librarymodel, List<IModel<Library>> libraries) {
		super(id);
		this.title=title;
		setOutputMarkupId(true);
		this.librarymodel = librarymodel;
		this.libraries = libraries;
 	}
	
	public void setLibrary(IModel<Library> lib) {
		this.librarymodel = lib;
	}
	
	public IModel<Library> getLibrary() {
		return librarymodel;
		
	}
	
	public List<IModel<Library>> getLibraries() {
		return libraries;
	}
	
	
	protected int getIntUserPreference(String key) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			return user.getService(PreferencesService.class).getIntValue("dashboard-"+ getPreferencesKey(), key, 0);
		return 0;
	}
	
	protected String getUserPreference(String key, String default_value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) {
			return user.getService(PreferencesService.class).getValue("dashboard-"+ getPreferencesKey(), key, default_value);
		}
		return default_value;
	}
	
	protected String getUserPreference(String key) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			return user.getService(PreferencesService.class).getValue("dashboard-"+	getPreferencesKey(), key);
		return null;
	}
		
	protected void setUserPreference(String key, String value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) {
			user.getService(PreferencesService.class).setValue("dashboard-"+getPreferencesKey(), key, value);
		}	
	}
	
	protected void setIntUserPreference(String key, int value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) {
			user.getService(PreferencesService.class).setIntValue("dashboard-"+getPreferencesKey(), key, value);
		}	
	}
	
	abstract protected String getPreferencesKey();
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		isCollapsed  = getUserPreference("expanded", "yes").equals("no");
		
		if (getLibraries().size()==0) {
			add(new InvisiblePanel("library"));
		}
		else {
			
			 DropDownChoice<IModel<Library>> ch = new DropDownChoice<IModel<Library>> ("library", new PropertyModel<IModel<Library>>(this, "library"), () -> getLibraries());
			 
			 ch.add(new AjaxFormComponentUpdatingBehavior("change") {
				protected void onUpdate(AjaxRequestTarget target) {
					@SuppressWarnings("unchecked")
					DropDownChoice<IModel<Library>> d = (DropDownChoice<IModel<Library>>) LibraryWidgetHeaderPanel.this.get("library");
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("library", d.getModel().getObject().getObject().getKey());
					fire (new GeneralWicketAjaxEvent (target, LibraryWidgetHeaderPanel.this.getClass().getName(), map));
				}
			});
			 
			ch.setChoiceRenderer(new ChoiceRenderer<IModel<Library>>() {
				public String getIdValue(IModel<Library> value, int index) {
					return value.getObject().getId().toString();
				};
				public String getDisplayValue(IModel<Library> value) {
					return  new StringResourceModel("library-name", LibraryWidgetHeaderPanel.this, null).setParameters(new Object[] {value.getObject().getDisplayName()}).getObject();
				};
			});
			
			add(ch);
		}
		
				
		
		Link<Void> tl = new Link<Void>("title-link") {
			@Override
			public void onClick() {
				onTitleClick();
			}
			
		};
		add(tl);		 
		tl.add(new Label("title", "").setVisible(false));
		
		
		AjaxLink<Void> re = new AjaxLink<Void>("refresh") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				LibraryWidgetHeaderPanel.this.refresh(target);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"far fa-sync \"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin spinning\"></i>'";
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};
		
		add(re);
		
		
		
		
		
		
		AjaxLink<Void> help = new AjaxLink<Void>("help") {
			private static final long serialVersionUID = 1L;
			
			public boolean isVisible() {
				return isHelp();
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				 LibraryWidgetHeaderPanel .this.onHelp(target);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"fal fa-info-circle \"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin   spinning\"></i>'";
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};
		
		

		
		
		
		AjaxLink<Void> edit = new AjaxLink<Void>("edit") {
			private static final long serialVersionUID = 1L;
			
			
			public boolean isVisible() {
				return isEdit();
			}
			@Override
			public void onClick(AjaxRequestTarget target) {
				 LibraryWidgetHeaderPanel.this.onEdit(target);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"far fa-sync \"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-edit spinning\"></i>'";
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};
		AjaxLink<Void> collapse = new AjaxLink<Void>("collapse") {
			public boolean isVisible() {
				return true;
			}
			@Override
			public void onClick(AjaxRequestTarget target) {
				isCollapsed = !isCollapsed;
				icon.add(new AttributeModifier("class", new Model<String>() {
					public String getObject() {
						return  isCollapsed ? "far fa-angle-up" : "far fa-angle-down"; 
					}
				}));
				
				LibraryWidgetHeaderPanel.this.setUserPreference("expanded", isCollapsed ?  "no" : "yes");
				target.add(LibraryWidgetHeaderPanel.this);
				LibraryWidgetHeaderPanel.this.onClickCollapse(target);
			}
		};

		
		/**
		AjaxLink<Void> viewmode = new AjaxLink<Void>("view") {
			public boolean isVisible() {
				return isViewMode();
			}
			@Override
			public void onClick(AjaxRequestTarget target) {
				onViewMode(target);
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"fa-duotone " +  (!isViewMode()  ?"fa-grip-lines" : "fa-diagram-cells")+ "\"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\"fa spinning\"></i>'";
						return s;
					}
					
					
					
					
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};
		*/
		
		icon = new WebMarkupContainer("collapse-icon");
		
		icon.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return  isCollapsed ? "far fa-angle-up" : "far fa-angle-down"; 
			}
		}));
		

		
		Label cl= new Label("expand-label", new StringResourceModel("expand", this, null)) {
			public boolean isVisible() {
				return isCollapsed;
			}
		};
		collapse.add(cl);
		
		
		
		collapse.add(icon);
		add(collapse);
		
		//add(viewmode);
		add(help);
		add(edit);
		
		
		WebMarkupContainer menuCon = new WebMarkupContainer("menu-container");
		add(menuCon);
		menuCon.setVisible(isMenu());
		menuCon.add(getMenu());
	}
	
	
	protected abstract void onTitleClick();

	protected Panel getMenu() {
		try {
			
			ContextMenuPanel<Panel> menu = new ContextMenuPanel<Panel>("menu", new Model<Panel>(this));
			menu.setOutputMarkupId(true);
			
			
			
			menu.addItem(new MenuItemFactory<Panel>() {
				private static final long serialVersionUID = 1L;
				@Override
				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<Panel>(id) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							try {
								LibraryWidgetHeaderPanel.this.onViewMode(target, "compact");				
							} 
							catch (Exception e) {
								setResponsePage(new ApplicationErrorPage<>(e));
								logger.error(e);	
							}
							//FeedbackHelper.showInfoToast(getLabel());
						}
						@Override
						public String getLabel() {
							return new StringResourceModel("compact-view", this, null).getObject();
						}
						@Override
						public boolean isEnabled() {
								return !getViewModeCriteria().equals("compact");
						}

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
						
						@Override
						public boolean isVisible() {
								return true;
						}
						
						@Override
						public boolean isIconVisible() {
							return getViewModeCriteria().equals("compact");
						}
						@Override 
						public String getWorkingLabel() {
							return new StringResourceModel("working", LibraryWidgetHeaderPanel.this, null).getString();
						}
					};
				}
			});
			

			
			menu.addItem(new MenuItemFactory<Panel>() {
				private static final long serialVersionUID = 1L;
				@Override
				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<Panel>(id) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							try {
								LibraryWidgetHeaderPanel.this.onViewMode(target, "comfortable");				
							} 
							catch (Exception e) {
								setResponsePage(new ApplicationErrorPage<>(e));
								logger.error(e);	
							}
							// FeedbackHelper.showInfoToast(getLabel());
						}
						@Override
						public String getLabel() {
							return new StringResourceModel("comfortable-view", this, null).getObject();
						}
						@Override
						public boolean isEnabled() {
								return !getViewModeCriteria().equals("comfortable");
						}

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
						
						@Override
						public boolean isVisible() {
								return true;
						}
						
						@Override
						public boolean isIconVisible() {
							return getViewModeCriteria().equals("comfortable");
						}
						@Override 
						public String getWorkingLabel() {
							return new StringResourceModel("working",LibraryWidgetHeaderPanel.this, null).getString();
						}
					};
				}
			});


			
			
			
			if (isSort()) {
				menu.addItem(new MenuItemFactory<Panel>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<Panel> getItem(String id) {
						return new SeparatorMenuItemPanelV5<Panel>(id) {
							private static final long serialVersionUID = 1L;
							@Override
							public String getCssClass() {
								return "divider";
							}
							@Override
							public boolean isVisible() {
								return  true;
							}
						};
					}
				});
				
				
				menu.addItem(new MenuItemFactory<Panel>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<Panel> getItem(String id) {
						return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<Panel>(id) {
							private static final long serialVersionUID = 1L;
							@Override
							public void onCheckClick(AjaxRequestTarget target) {
								try {
									LibraryWidgetHeaderPanel.this.onSort(target, "title");				
								} 
								catch (Exception e) {
									setResponsePage(new ApplicationErrorPage<>(e));
									logger.error(e);	
								}
								// FeedbackHelper.showInfoToast(getLabel());
							}
							@Override
							public String getLabel() {
								return new StringResourceModel("sort-title", this, null).getObject();
							}
							@Override
							public boolean isEnabled() {
									return !getSortCriteria().equals("title");
							}

							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								attributes.setEventPropagation(EventPropagation.STOP); 
							}
							
							@Override
							public boolean isVisible() {
									return true;
							}
							
							@Override
							public boolean isIconVisible() {
								return getSortCriteria().equals("title");
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", LibraryWidgetHeaderPanel.this, null).getString();
							}
						};
					}
				});


				menu.addItem(new MenuItemFactory<Panel>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<Panel> getItem(String id) {
						return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<Panel>(id) {
							private static final long serialVersionUID = 1L;
							@Override
							public void onCheckClick(AjaxRequestTarget target) {
								try {
									LibraryWidgetHeaderPanel.this.onSort(target, "date");				
								} 
								catch (Exception e) {
									setResponsePage(new ApplicationErrorPage<>(e));
									logger.error(e);	
								}
								// FeedbackHelper.showInfoToast(getLabel());
							}
							@Override
							public String getLabel() {
								return new StringResourceModel("sort-date", this, null).getObject();
							}
							@Override
							public boolean isEnabled() {
								return !getSortCriteria().equals("date");
							}

							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								attributes.setEventPropagation(EventPropagation.STOP); 
							}
							
							@Override
							public boolean isVisible() {
									return true;
							}
							
							@Override
							public boolean isIconVisible() {
								return getSortCriteria().equals("date");
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", LibraryWidgetHeaderPanel.this, null).getString();
							}
						};
					}
				});
			}

			return menu;
			
		} catch (Exception e) {
			logger.error(e);
			return new InvisiblePanel("menu");
		}
	}



	protected void onSort(AjaxRequestTarget target, String string) {}

	public boolean isMenu() {
		return true;
	}
	


	protected void onViewMode(AjaxRequestTarget target, String criteria) {
	}

	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
	
	public void onDetach() {
		super.onDetach();
		
		if (model_session_user != null)
			model_session_user.detach();

		if (librarymodel!=null)
			librarymodel.detach();
		
		if (libraries!=null) 
			libraries.forEach(item -> item.detach());
	}

	
	abstract protected void refresh(AjaxRequestTarget target);
	abstract protected void onEdit(AjaxRequestTarget target);
	abstract protected void onHelp(AjaxRequestTarget target);
	abstract protected void onClickCollapse(AjaxRequestTarget target);

	abstract protected String getViewModeCriteria();
	abstract protected String getSortCriteria();
	
	
	public void setViewMode(boolean v) {
		this.isViewMode=v;
	}
	
	public boolean isViewMode() {
		return isViewMode;
	}
	
	
	public boolean isSort() {
		return true;
	}
	
	
	public boolean isHelp() {
		return isHelp;
	}


	public void setHelp(boolean isHelp) {
		this.isHelp = isHelp;
	}


	public boolean isEdit() {
		return isEdit;
	}


	public void setEdit(boolean isEdit) {
		this.isEdit = isEdit;
	}	

	protected KbeeUser getSessionUser() {
		try {
			if (model_session_user != null && model_session_user.getObject() != null)
				return (KbeeUser) model_session_user.getObject();

			User session_user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			model_session_user = new ObjectModel<User>(session_user);
			return (KbeeUser) model_session_user.getObject();
		} catch (Exception e) {
				logger.error(e);
			return null;
		}
	}
	
}
