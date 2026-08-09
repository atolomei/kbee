package kbee.web.portal.dataprovider;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.userlist.UserListService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.PortalDataProvider;
import com.novamens.portal6.model.ViewBK;
import com.novamens.portal6.model.block.ListViewBlock;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.EditButtonsV5;
import kbee.web.portal6.factory.PanelPortalModel;
import kbee.web.portal6.panel.PortalPanel;


/***
 * 
 * . IQL View
 * . ContentView
 * . BlockView
 * . LinkView
 * 
 * 
 */
public class PortalBlockListViewDataProvider<T extends ListViewBlock> extends PortalPanel<T> implements PanelPortalModel<T>, PortalDataProvider  {
			
	
	private static final long serialVersionUID = 1L;
	private IModel<T> model;

	static final int MAX = 60;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalBlockListViewDataProvider.class.getName());


	public PortalBlockListViewDataProvider(String id) {
		super(id);
	}
	
	
	public PortalBlockListViewDataProvider(String id, IModel<T> model) {
		super(id, model);
	}


	public List<ViewBK> getItems() {
		List<ViewBK> list = getPortalModel().getObject().getItems();
		return list;
		
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (model!=null)
			model.detach();
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		// List<ViewBK> list = getPortalModel().getObject().getItems();
		
		
		ListView<ViewBK> lview = new ListView<ViewBK>("view", new PropertyModel<List<ViewBK>>(this, "items")) {
			
			private static final long serialVersionUID = 1L;
			
			public void populateItem(final ListItem<ViewBK> item) {
				
				WorkingIndicatorAjaxLinkV5<Void> link = new WorkingIndicatorAjaxLinkV5<Void>("link") {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick(AjaxRequestTarget target) {
						//ViewBK query = item.getModelObject();
						//selected = query.getId();
						//fireScanAll(new ApplyViewBKEvent(target, query));
					}
					
					@Override
					protected String getWorkingLabel() {
						return new StringResourceModel("working", PortalBlockListViewDataProvider.this, null).getObject();
					}
				};
				
				item.add(link);
				
				
				if (item.getModelObject()!=null)
						link.add(new Label("title",                  pad(item.getModelObject().getTitle())));
				else
						link.add(new Label("title",               "error"));
				
				
				//link.add(new Label("total", "(" + String.valueOf(totals.get(item.getModelObject().getId())) + ")" ));
				//if (selected!=null && selected.toString().equals(item.getModelObject().getId().toString())) {
				//		item.add(new AttributeModifier("class", "list-group-item selected"));
				//}
				
				item.add(getMenu(item.getModel()));
				
			};
		};
		
		lview.setOutputMarkupId(true);
		
		add(lview);
		
		
		
		
		
		
		WorkingIndicatorAjaxLinkV5<Void> ral = new WorkingIndicatorAjaxLinkV5<Void>("remove-all") {
			private static final long serialVersionUID = 1L;
			public void onClick(AjaxRequestTarget target) {
				try {
					//((KbeeUser) getSessionUser()).getService(UserListService.class).emptySavedQueriesList(console);
				} 
				catch (ContentMgmtException e) {
					logger.error(e);	
				}
				
		
				target.add( PortalBlockListViewDataProvider.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working",  PortalBlockListViewDataProvider.this, null).getObject();
			}
			
			@Override
			public boolean isVisible() {
				return true;
				//return !getQueries().isEmpty();
			}
		};
		
		
		
				

		WorkingIndicatorAjaxLinkV5<Void> co = new WorkingIndicatorAjaxLinkV5<Void>("close") {
			private static final long serialVersionUID = 1L;
			public void onClick(AjaxRequestTarget target) {
				try {
					// close(target);
				} 
				catch (ContentMgmtException e) {
					logger.error(e);	
				}
				target.add( PortalBlockListViewDataProvider.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working",  PortalBlockListViewDataProvider.this, null).getObject();
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		
		WorkingIndicatorAjaxLinkV5<Void> cal = new WorkingIndicatorAjaxLinkV5<Void>("clear-all") {
			private static final long serialVersionUID = 1L;
			public void onClick(AjaxRequestTarget target) {
				try {
					//clearAll(target);
					 //PortalBlockListViewDataProvider.this.queries=null;
					//selected=null;
				} 
				catch (ContentMgmtException e) {
					logger.error(e);	
				}
				target.add( PortalBlockListViewDataProvider.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working",  PortalBlockListViewDataProvider.this, null).getObject();
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		WebMarkupContainer actions = new WebMarkupContainer("actions") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return true;
			}
		};
		
		add(actions);
		actions.add(ral);
		actions.add(co);
		actions.add(cal);
		
	}
	
	
	@Override
	public void setPortalModel(IModel<T> model) {
		
		this.model=model;
	}

	@Override
	public IModel<T> getPortalModel() {
		return model;
	}
	
	
	protected Panel getMenu(IModel<ViewBK> model) {
		try {
				
				ContextMenuPanel<ViewBK> menu = new ContextMenuPanel<ViewBK>(model);
										
				menu.setOutputMarkupId(true);
				
				menu.addItem(new MenuItemFactory<ViewBK>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public AbstractMenuItemPanelV5<ViewBK> getItem(String id) {
						return new AjaxMenuItemPanelV5<ViewBK>(id) {
							
							/**
							 * 
							 */
							private static final long serialVersionUID = 1L;

							@Override 
							public String getLabel() {
								return "Edit";
								//return PortalBlockListViewDataProvider.this.getLabel("edit").getObject();
							}

							@Override
							public void onClick(AjaxRequestTarget target) throws Exception {
								try {
									//PortalBlockListViewDataProvider.this.addOrReplace(new ViewBKEditorFragment("ViewBK-editor", new ObjectModel<ViewBK>(getModel().getObject()), false));
									target.add(PortalBlockListViewDataProvider.this);
									
								} 
								catch (ContentMgmtException e) {
									logger.error(e);	
								}
							}
						};
					}
				});
				

				menu.addItem(new MenuItemFactory<ViewBK>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<ViewBK> getItem(String id) {
						return new AjaxMenuItemPanelV5<ViewBK>(id) {
							
							/**
							 * 
							 */
							private static final long serialVersionUID = 1L;

							@Override 
							public String getLabel() {
								return  PortalBlockListViewDataProvider.this.getLabel("delete").getObject();
							}

							@Override
							public void onClick(AjaxRequestTarget target) throws Exception {
								try {
									//((KbeeUser) getSessionUser()).getService(UserListService.class).delete(getModel().getObject());
									//target.add( PortalBlockListViewDataProvider.this);
									//fireScanAll(new MyListsDeleteListEvent(target));
									
								} catch (Exception e) {
									logger.error(e);						
								}
							}
						};
					}
				});

			 
				 
				return menu;
				
			} catch (Exception e) {
				logger.error(e);
				return new InvisiblePanel("menu");
			}
		}


	private String pad(String title) {
		if (title!=null && title.length()>MAX) {
			return title.substring(0, MAX)+"...";
		}
		return title;
	}

}
