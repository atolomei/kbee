package kbee.web.portal6.panel;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.PortalDao;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PortalModel;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.portal6.PortalObjectDataProviderService;
import kbee.web.portal6.editor.PortalCloseDataProviderAjaxEvent;
import kbee.web.portal6.editor.PortalCloseEditAjaxEvent;
import kbee.web.portal6.editor.PortalEditAjaxEnabled;

import kbee.web.portal6.event.PortalAddAjaxEvent;
import kbee.web.portal6.event.PortalAjaxEvent;
import kbee.web.portal6.event.PortalAjaxStructureShowHierarchyEvent;
import kbee.web.portal6.event.PortalArchiveAjaxEvent;
import kbee.web.portal6.event.PortalDeleteAjaxEvent;
import kbee.web.portal6.event.PortalEditAjaxEvent;
import kbee.web.portal6.event.PortalMoveDownAjaxEvent;
import kbee.web.portal6.event.PortalMoveParentAjaxEvent;
import kbee.web.portal6.event.PortalMoveUpAjaxEvent;
import kbee.web.portal6.event.PortalRestoreAjaxEvent;

public abstract class PortalIWebPanel<T extends PortalObject> extends PortalPanel<T> {
		
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalIWebPanel.class.getName());
	
	private static final long serialVersionUID = 1L;

	private Boolean flat_view = true;
	private Boolean selected = false;
	private Boolean controller_visible = true;
	
	
	private PortalViewMode view_mode;
	

	private WebMarkupContainer kpanel;
	private WebMarkupContainer class_panel;
	private  WebMarkupContainer all_webmarkupcontainer;
	
	private  WebMarkupContainer ic;
		
	private boolean payload_expanded = true;
	private boolean meta_info_expanded = false;
	

	private WebMarkupContainer data_provider;
	
	private  ControllerFragment controller = null;

	
	boolean data_provider_visible = false;
	
	
	/** ---------------------------------------------
	 * 
	 *
	 */
	public class ControllerFragment extends Fragment {
		
		private static final long serialVersionUID = 1L;
	
		WebMarkupContainer meta_webmarkupcontainer;
		
		public ControllerFragment(String id) {
			super(id, "controller-fragment", PortalIWebPanel.this);
			setOutputMarkupId(true);
		}
		
		public void onBeforeRender() {
			super.onBeforeRender();
			
			meta_webmarkupcontainer = new WebMarkupContainer("meta");
			addOrReplace(meta_webmarkupcontainer);
			

			AjaxLink<Void> ec=new AjaxLink<Void>("edit-content") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					onEditDataProvider(target);
				}
			};
			ec.setVisible(isEditDataProvider());
			addOrReplace(ec);
			
			
			AjaxLink<Void> pv=new AjaxLink<Void>("preview-content") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					String key="payload-visible";
					boolean b=getPreference(key, "no").equals("yes");
					setPreference(key,  (b ? "no" : "yes" ));
					target.add(PortalIWebPanel.this.getPage());
				}
			};
			//pv.setVisible(isEditDataProvider());
			addOrReplace(pv);

			
							
			AjaxLink<T> up=new AjaxLink<T>("up", getModel()) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					try {
						PortalIWebPanel.this.moveUp(target, getModel());
					} 
					catch (Exception e) {
						logger.error(e);
						fire(new ErrorEvent<T>(target, getModel(), e));
					}
				}
			};
			up.setVisible(isMoveUpEnabled());
			addOrReplace(up);
			
							
			AjaxLink<T> down=new AjaxLink<T>("down", getModel()) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					try {
						PortalIWebPanel.this.moveDown(target, getModel());
					} 
					catch (Exception e) {
						logger.error(e);
						fire(new ErrorEvent<T>(target, getModel(), e));
					}
				}
			};
			down.setVisible(isMoveDownEnabled());
			addOrReplace(down);
			
			
			
			
			AjaxLink<Void> ex=new AjaxLink<Void>("expand") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					onExpand(target);
				}
			};
				
			ic = new WebMarkupContainer("icon-expanded");
			ic.add(new AttributeModifier("class", new Model<String>() {
				private static final long serialVersionUID = 1L;
					public String getObject() {
						return payload_expanded ? "far fa-angle-down" : "far fa-angle-up";
					}
			}));
			ex.addOrReplace(ic);
			addOrReplace(ex);
			
			
			if (!meta_info_expanded) {
				meta_webmarkupcontainer.setVisible(false);
			}
			else {
				//if (!meta_generated) {
					meta_webmarkupcontainer = new PortalObjectMetadataPanel<T>("meta", getModel());
					addOrReplace(meta_webmarkupcontainer);
					//meta_generated=true;
				//}
				meta_webmarkupcontainer.setVisible(meta_info_expanded);
			}
			
			all_webmarkupcontainer.setVisible(payload_expanded);
			ic.add(new AttributeModifier("class", new Model<String>() {
				private static final long serialVersionUID = 1L;
					public String getObject() {
						return payload_expanded ? "far fa-angle-down" : "far fa-angle-up";
					}
			}));
		}
		
		

		public void onInitialize() {
			super.onInitialize();

			AjaxLink<Void> t_more=new AjaxLink<Void>("title-more") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					// onClickMore(target);
					PortalIWebPanel.this.edit(target, PortalIWebPanel.this.getModel());
				}
			};

			add(t_more);
			t_more.add(new Label("title", getModel().getObject().getTitle()));
			
			Label sb=new Label("subtitle", " |  " + getModel().getObject().getClassKey() +" " + 
			(getModel().getObject().getDataProviderInfo()!=null?getModel().getObject().getDataProviderInfo():"")
			);
			add(sb);
					
			if (getModel().getObject().getState()==ObjectState.ENABLED)
				add(new Label("state", "").setVisible(false));
			else
				add(new Label("state", " | " + getModel().getObject().getState().getHTMLLabel(getSessionUser().getLocale())).setEscapeModelStrings(false));
			
				
			
				
			
			AjaxLink<Void> r_more=new AjaxLink<Void>("more") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					onClickMore(target);
				}
			};
			
			Label cla=new Label("clazz", getClassInfo());
			cla.setEscapeModelStrings(false);
			r_more.add(cla);
			add(r_more);
			
			add(getMenu());
		}
		
		public void onClickMore(AjaxRequestTarget target) {
			meta_info_expanded = !meta_info_expanded;
			target.add(PortalIWebPanel.this);
		}
		

		protected void onExpand(AjaxRequestTarget target) {
			payload_expanded  = !payload_expanded;
			setPreference("payload-expanded", payload_expanded ?"yes":"no");
			target.add(PortalIWebPanel.this);
		}
		
		
		protected void onEditDataProvider(AjaxRequestTarget target) {
			try {
				PortalIWebPanel.this.editInline(target, getModel());
			} 
			catch (Exception e) {
				logger.error(e);
				fire(new ErrorEvent<T>(target, getModel(), e));
			}
		}
		
	}

	/** --------------------------------------------------- 
	 * 
	 * 
	 * */

	
	public PortalIWebPanel(String id, IModel<T> model, PortalViewMode view_mode,  Map<String, String>  parameters) {
		super(id, model, parameters);
		
		this.view_mode=view_mode; 
		controller_visible = Boolean.valueOf( view_mode != PortalViewMode.PRODUCTION);
		
		setOutputMarkupId(true);
		
		kpanel = new WebMarkupContainer("kbpanel");
		kpanel.setOutputMarkupId(true);
		
		class_panel = new WebMarkupContainer("kbpanel-class");
		class_panel.setOutputMarkupId(true);
		
		kpanel.add(class_panel);
		
		all_webmarkupcontainer = new WebMarkupContainer("all");
		all_webmarkupcontainer.setOutputMarkupId(true);
		
		add(kpanel);
		class_panel.add(all_webmarkupcontainer);
		
		payload_expanded = getPreference("payload-expanded", "yes").equals("yes");
		
		class_panel.add(new InvisiblePanel("controller"));
		

		
		
	}
	
	public void editInline(AjaxRequestTarget target, IModel<T> model) {
		try {
			if ( (this.data_provider ==null) || (this.data_provider instanceof InvisiblePanel)) {
				WebMarkupContainer dp = model.getObject().getService(PortalObjectDataProviderService.class).getDataProviderEditor("data-provider");
				dp.setVisible(false);
				setDataProvider(dp);
			}
		} catch (Exception e) {
			logger.error(e);
			setDataProvider(new PortalErrorPanel<T>("data-provider", e));
		}
		toogleDataProvider(target);
	}


	public boolean isEditDataProvider() {
		if (getModel().getObject() instanceof PortalModel) { 
			// logger.debug (   ((PortalModel) getModel().getObject()).isPayloadEditor());
			return ((PortalModel) getModel().getObject()).isPayloadEditor();
		}
		return false;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	
	@Override
	public void addListeners() {
		super.addListeners();

		 add(new WicketEventListener<PortalCloseEditAjaxEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalCloseEditAjaxEvent<PortalObject> event) {
				if (isSelected()) {
					setSelected(false);
					event.getRequestTarget().add(PortalIWebPanel.this);
				}
			}
		});
		 
		 

		 add(new WicketEventListener<PortalCloseDataProviderAjaxEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalCloseDataProviderAjaxEvent<PortalObject> event) {
				if (data_provider.isVisible()) {
					if (event.getModel()!=null &&  event.getModel().getObject()!=null && event.getModel().getObject().getId().equals( PortalIWebPanel.this.getModel().getObject().getId())) {
						data_provider.setVisible(false);
						event.getRequestTarget().add(PortalIWebPanel.this);
		 			}
				}
			}
		});

		 
		 
		 
			//fireScanAll(new PortalCloseDataProviderAjaxEvent<T>(target, getModel()));
	}
	

	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (getParameters()!=null) {
			
			if (getParameters().containsKey(PortalAjaxEvent.STRUCTURE_VIEW)) {
				try {
					Integer v=Integer.valueOf(getParameters().get(PortalAjaxEvent.STRUCTURE_VIEW));
					if (v==PortalAjaxStructureShowHierarchyEvent.VIEW_HIERARCHY_NO)
						this.setFlatView(true);
					else
						this.setFlatView(false);
				}
				catch (Exception e) {
					logger.error(e);
				}
			}
		
		
			if (getParameters().containsKey(PortalAjaxEvent.CONTROLLER_VISIBLE)) {
				try {
					Integer v=Integer.valueOf(getParameters().get(PortalAjaxEvent.CONTROLLER_VISIBLE));
					if (v==PortalAjaxStructureShowHierarchyEvent.SHOW_CONTROLLER_YES)
						this.controller_visible=Boolean.valueOf(true);
					else
						this.controller_visible=Boolean.valueOf(false);
				}
				catch (Exception e) {
					logger.error(e);
				}
			}
		}

		
		try {
			if (this.isControllerVisible()) {
				controller = new ControllerFragment("controller");
				class_panel.addOrReplace(controller);
			} else 
				class_panel.addOrReplace(new InvisiblePanel("controller"));
		} catch (Exception e) {
			logger.error(e);
			class_panel.addOrReplace(new InvisiblePanel("controller"));
		}
		
		
		class_panel.add(new AttributeModifier("class","kb-"+this.getModel().getObject().getClassKey() + (this.isSelected()?" kbpanel-selected":"")));

		StringBuilder str = new StringBuilder();
		str.append("kbpanel ");
		if (this.isFlatView())
			str.append("kbpanel-flat ");
		else
			str.append("kbpanel-hierarchy ");
			
		
		if (this.getModel().getObject().getState()==ObjectState.ARCHIVED || this.getModel().getObject().getState()==ObjectState.DELETED)
			str.append(" " + this.getModel().getObject().getState().getCss());

		kpanel.add(new AttributeModifier("class", str.toString()));
		

		
	}
	
	protected Panel getVoidPanel(String id) {
			return getVoidPanel(id, null, null);
	}
	
	protected Panel getVoidPanel(String id, IModel<String> lefts, IModel<String> rights) {
		
//		if (view_mode==PortalViewMode.PRODUCTION)
//			return new InvisiblePanel(id);
		
//		if (view_mode==PortalViewMode.EDIT)
//			return new InvisiblePanel(id);
		
		return new InvisiblePanel(id);
	}
	
	protected void xAdd(Component component) {
		 all_webmarkupcontainer.add(component);
	}
	
	protected void xAddOrReplace(Component component) {
		 all_webmarkupcontainer.addOrReplace(component);
	}
	
	
/**
 * 
 * Block -> move to another section,  move to a different area
 * Area -> move to another page_section
 * 
 * public class MenuItem extends MenuComponent implements Accessible  
 * public class Menu extends MenuItem implements MenuContainer, Accessible  
 * 
 */
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (this.data_provider==null) {
			
			 this.data_provider= new InvisiblePanel("data-provider");
			// this.data_provider=new DummyBlockPanel("data-provider", new Model<String>(getClass().getSimpleName()));
			
			//if (this.isEditDataProvider()) {
			//	WebMarkupContainer dp = getModel().getObject().getService(PortalObjectDataProviderService.class).getDataProviderEditor("data-provider");
			//	this.data_provider= dp!=null ? dp : new InvisiblePanel("data-provider");
			//}
			//else
			//	this.data_provider= new InvisiblePanel("data-provider");
			
		}
		class_panel.addOrReplace(this.data_provider);
	}

	public void toogleDataProvider(AjaxRequestTarget target) {
		
		if (this.data_provider!=null && !(this.data_provider instanceof InvisiblePanel) ) {
			this.data_provider.setVisible(! this.data_provider.isVisible());
			target.add(this.kpanel);
		}
	}

	public IModel<String> getClassInfo() {
		return new Model<String>(getModel().getObject().getClassKey());
	}
	
	
	
	protected List<MenuItemFactory<T>> getMenuItems() {
		
		List<MenuItemFactory<T>> list = new ArrayList<MenuItemFactory<T>>();
		
		
		list.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 100;
			}

			
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							PortalIWebPanel.this.edit(target, getModel());
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<T>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return getResourceModel("edit").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						return PortalIWebPanel.this.isEditEnabled();
					}
					
				};
			}
		});
		

		list.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 200;
			}

			
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							PortalIWebPanel.this.add(target, getModel());
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<T>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return getMenuLabel("add").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						return PortalIWebPanel.this.isAddEnabled();
					}
					
				};
			}
		});

		
		
		
		
		list.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 300;
			}

			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							PortalIWebPanel.this.moveUp(target, getModel());
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<T>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return getResourceModel("moveup").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						return PortalIWebPanel.this.isMoveUpEnabled();
					}

				};
			}
		});
		
		
		list.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public int getOrder() {
				return 400;
			}

			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							PortalIWebPanel.this.moveDown(target, getModel());
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<T>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return getResourceModel("movedown").getObject();
					}
					
					
					@Override
					public boolean isEnabled() {
						return PortalIWebPanel.this.isMoveDownEnabled();
					}

					
				};
			}
		});
		

		
		list.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public int getOrder() {
				return 500;
			}

			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							PortalIWebPanel.this.moveParent(target, getModel());
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<T>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return getMenuLabel("move").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						return PortalIWebPanel.this.isMoveEnabled();
					}
				};
			}
		});

		
		
		
		list.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public int getOrder() {
				return 600;
			}
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new SeparatorMenuItemPanelV5<T>(id) {
					private static final long serialVersionUID = 1L;
					@Override
                     public String getCssClass() {
                         return "divider";
                     }

                     @Override
                     public boolean isVisible() {
                         return true;
                     }
				};
			}
		});
		
		
		list.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public int getOrder() {
				return 700;
			}
			
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							if (getModel().getObject().getState()==ObjectState.ARCHIVED)
								PortalIWebPanel.this.restore(target, getModel());
							else
								PortalIWebPanel.this.archive(target, getModel());
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<T>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						if (getModel().getObject().getState()==ObjectState.ARCHIVED)
							return getResourceModel("restore").getObject();
						else
							return getResourceModel("archive").getObject();
					}

					@Override
					public boolean isEnabled() {
							return true;
					}
				};
			}
		});
		
		
		list.add(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public int getOrder() {
				return 800;
			}

			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							PortalIWebPanel.this.delete(target, getModel());
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<T>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return getResourceModel("delete").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						return PortalIWebPanel.this.isDeleteEnabled();
					}
				};
			}
		});
		
		return list;

	}
	
	
	/**
	 * Edit
	 * Move Up
	 * Move Down
	 * Archive/Restore
	 * Delete 
	 * 
	 * @return
	 */
	protected Panel getMenu() {
		
		ContextMenuPanel<T> menu = new ContextMenuPanel<T>(getModel());
		
		menu.setOutputMarkupId(true);
		
		List<MenuItemFactory<T>> list = getMenuItems();
		
		list.sort(new Comparator<MenuItemFactory<T>>() {

			@Override
			public int compare(MenuItemFactory<T> o1, MenuItemFactory<T> o2) {
				if (o1.getOrder()<o2.getOrder())
					return -1;
				
				if (o1.getOrder()>o2.getOrder())
					return 1;
				
				return 0;
			}
		});
		
		for (MenuItemFactory<T> f: list) {
			menu.addItem(f);
		}
		
		return menu;
		
		
	}



	protected void moveExternal(AjaxRequestTarget target, IModel<T> model) {
	}

	protected void moveInternal(AjaxRequestTarget target, IModel<T> model) {
	}
	

	public PortalViewMode getViewMode() {
		return view_mode;
	}

	public void setViewMode(PortalViewMode view_mode) {
		this.view_mode = view_mode;
	}


	public boolean isControllerVisible() {
		return this.controller_visible;
		
	}

	public void setControllerVisible(boolean b) {
		this.controller_visible =b;
	}


	public boolean isDataProviderVisible() {
		return this.data_provider_visible;
		
	}

	public void setDataProviderVisible(boolean b) {
		this.data_provider_visible =b;
	}

	

	public void setDataProvider(WebMarkupContainer dp) {
		
		if (dp==null)
			return;
			
		 if (!dp.getId().equals("data-provider"))
			 throw new IllegalArgumentException("data provider panel must have id -> data-provider");		 
		
		 if (this.data_provider!=null) {
			this.data_provider = dp;
			this.class_panel.addOrReplace(this.data_provider);
		}
		else
			this.data_provider = dp;
	}
	
	public PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
	
	public boolean isSelected() {return selected.booleanValue();}
	
	public void setSelected(boolean b) {
		selected=Boolean.valueOf(b);
	}


	public boolean isFlatView() {return flat_view.booleanValue();}
	public void setFlatView(boolean b) {flat_view=Boolean.valueOf(b);}
					
	

	protected boolean isMoveUpEnabled() {return true;}
	protected boolean isMoveDownEnabled() {return true;}
	protected boolean isEditEnabled() {return true;}
	protected boolean isAddEnabled() {return true;}
	
	protected boolean isArchiveEnabled() {return true;}
	protected boolean isDeleteEnabled() {return true;}
	
	protected boolean isMoveEnabled() {return true;}
	
	protected boolean isMoveInternalEnabled() {return true;}
	protected boolean isMoveExternalEnabled() {return true;}
	
	
	
	protected IModel<String>  getMenuLabel(String string) {
		return getLabel(string);
	}
	
	
	protected void moveParent(AjaxRequestTarget target, IModel<T> model) {
		fire( new PortalMoveParentAjaxEvent<T>(target, model));
	}

	
	protected void edit(AjaxRequestTarget target, IModel<T> model)		{
		fire( new PortalEditAjaxEvent<T>(target, model));
	}
	
	protected void add(AjaxRequestTarget target, IModel<T> model) {
		fire( new PortalAddAjaxEvent<T>(target, model));
	}

	
	protected void moveUp(AjaxRequestTarget target, IModel<T> model) 	{	
		fire( new PortalMoveUpAjaxEvent<T>(target, model));
	}
	protected void moveDown(AjaxRequestTarget target, IModel<T> model) 	{	
		fire( new PortalMoveDownAjaxEvent<T>(target, model));
	}
	protected void archive(AjaxRequestTarget target, IModel<T> model) 	{	
		fire( new PortalArchiveAjaxEvent<T>(target, model));
	}
	protected void restore(AjaxRequestTarget target, IModel<T> model) 	{	
		fire( new PortalRestoreAjaxEvent<T>(target, model));
	}
	protected void delete(AjaxRequestTarget target, IModel<T> model)	{	
		fire( new PortalDeleteAjaxEvent<T>(target, model));
	}

	public void setPreference(String key, String value) {
		((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue("portal-"+getModel().getObject().getClassKey()+"-"+getModel().getObject().getId().toString(), key, value);
	}
	
	public void setIntPreference(String key, int value) {
		((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue("portal-"+getModel().getObject().getClassKey()+"-"+getModel().getObject().getId().toString(), key, value);
	}
	
	
	public String getPreference(String key, String defaultValue) {
		return ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue("portal-"+getModel().getObject().getClassKey()+"-"+getModel().getObject().getId().toString(), key, defaultValue);
	}

	public int getIntPreference(String key, int defaultValue) {
		return ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getIntValue("portal-"+getModel().getObject().getClassKey()+"-"+getModel().getObject().getId().toString(), key, defaultValue);
	}

}
