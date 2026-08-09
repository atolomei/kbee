package kbee.web.dashboard;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.entity.Person;
import com.novamens.content.service.ContentSubscriptionService;
import com.novamens.content.subscription.ContentSubscription;
import com.novamens.kbee.content.repository.ContentSubscriptionRepository;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

public class SubscriptionsPanel extends KBPanel {
		
	private static final long serialVersionUID = 1L;
	
	
	static final int MAX = 120;
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SubscriptionsPanel.class.getName());
	
	private ListView<IModel<ContentSubscription>> lview;
	private List<IModel<ContentSubscription>> list_model = null;

	
	/**
	 * by default this panel uses sessionUser()   
	 * when the panel is used by  {@link UserEMailRulesPanel}
	 * then the person can be different from sessionUser() 
	 */
	IModel<Person> model_person = null;
	
	
	public SubscriptionsPanel(String id) {
		this(id, null);
		
	}
	
	public SubscriptionsPanel(String id, IModel<Person> modelPerson) {
		super(id);
		setOutputMarkupId(true);
		if (modelPerson==null)
				model_person = new ObjectModel<Person>(getPerson());
		else
		model_person = modelPerson; 
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		loadList();
	}
	
	
	public IModel<Person> getPersonModel() {
		return model_person;
	}
	/**
	 * 
	 * 
	 * @param model
	 * @return
	 */
	
	@SuppressWarnings("serial")
	protected Panel getMenu(IModel<ContentSubscription> model) {
		try {
				
				ContextMenuPanel<ContentSubscription> menu = new ContextMenuPanel<ContentSubscription>(model);
										
				menu.setOutputMarkupId(true);
				
				menu.addItem(new MenuItemFactory<ContentSubscription>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<ContentSubscription> getItem(String id) {
						return new MenuItemPanelV5<ContentSubscription>(id) {
							
							private static final long serialVersionUID = 1L;
							@Override
							public String getTarget() {
								return "_blank";
							}
							@Override 
							public String getLabel() {
								return SubscriptionsPanel.this.getLabel("open").getObject();
							}

							@Override
							public void onClick() throws Exception {
								try {
									SubscriptionsPanel.this.onClick(getModel(), -1);
								} 
								catch (ContentMgmtException e) {
									logger.error(e);	
								}
							}
						};
					}
				});
				
				
				menu.addItem(new MenuItemFactory<ContentSubscription>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public AbstractMenuItemPanelV5<ContentSubscription> getItem(String id) {
						return new AjaxMenuItemPanelV5<ContentSubscription>(id) {
							public void onClick(AjaxRequestTarget target) {
								 getModelObject().getContent().getService(ContentSubscriptionService.class).subscribe(getPerson());
								
								 SubscriptionsPanel.this.refresh(target);
							}
							@Override 
							public String getLabel() {
								return new StringResourceModel("subscribe", this, null).getObject();
							}
							@Override
							public boolean isVisible() {
								return !getModelObject().getContent().getService(ContentSubscriptionService.class).isSubscribed(getPerson());
	   						}
						};
					}
				});

				
				menu.addItem(new MenuItemFactory<ContentSubscription>() {
					@Override
					public AbstractMenuItemPanelV5<ContentSubscription> getItem(String id) {
						return new AjaxMenuItemPanelV5<ContentSubscription>(id) {
							public void onClick(AjaxRequestTarget target) {
								getModelObject().getContent().getService(ContentSubscriptionService.class).unsubscribe(getPerson());
								
								SubscriptionsPanel.this.refresh(target);
							}
							@Override 
							public String getLabel() {
								return new StringResourceModel("unsubscribe", this, null).getObject();
							}
							@Override
							public boolean isVisible() {
								return getModelObject().getContent().getService(ContentSubscriptionService.class).isSubscribed(getPerson());
	   						}
						};
					}
				});

				
				return menu;
				
			} catch (Exception e) {
				logger.error(e, getSessionUser().getUserName());
				return new InvisiblePanel("menu");
			}
		}

	
	protected void refresh(AjaxRequestTarget target) {
		loadList();		
		 target.add(this);
	}
	


	
	
	
	public List<IModel<ContentSubscription>> getList() {

		if (list_model!=null)
			return list_model;
		
		list_model = new ArrayList<IModel<ContentSubscription>>();
		
		List<ContentSubscription> list = ((ContentSubscriptionRepository) getRepository(ContentSubscription.class)).findAllBy(getPerson(), MAX);
		logger.debug("List<ContentSubscription> getList()");
		
		
		for (ContentSubscription c: list) {
			list_model.add( new ObjectModel<ContentSubscription>(c));
		}
		
		if  (getSortCriteria()!=null &&  getSortCriteria().equals("date")) {
			list_model.sort( new Comparator<IModel<ContentSubscription>>() {
				@Override
				public int compare(IModel<ContentSubscription> o1, IModel<ContentSubscription> o2) {
					try {
						boolean isAfter = o1.getObject().getContent().getLastModifiedOffsetDateTime().isAfter(o2.getObject().getContent().getLastModifiedOffsetDateTime());
						return isAfter ? -1 : 1;
					}catch (Exception e) {
						logger.error(e);
					}
					return 0;
				}
			});
			
		}
		
		return list_model;
	}

	
	protected void close(AjaxRequestTarget target) {
	}


	protected void clearAll(AjaxRequestTarget requestTarget) {
		fireScanAll(new FilterSelectorClearAllEvent(requestTarget));
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private String pad(String title) {
		if (title.length()>MAX) {
			return title.substring(0, MAX)+"...";
		}
		return title;
	}

	public void onDetach() {
		super.onDetach();
		if (list_model!=null)
			list_model.forEach(item-> item.detach());
	}
	
	  
	protected void onClick(IModel<ContentSubscription> modelObject, int index) {
	}
	  
	
	protected void loadList() {
		
		list_model = null;
		
		lview = new ListView<IModel<ContentSubscription>>("subscription", getList() ) {
			
			private static final long serialVersionUID = 1L;
			
			public void populateItem(final ListItem<IModel<ContentSubscription>> item) {
				
			try {	
				if (item.getModel().getObject()!=null) {
						Link<Void> link = new Link<Void>("link") {
							private static final long serialVersionUID = 1L;
							@Override
							public void onClick() {
								SubscriptionsPanel.this.onClick(item.getModel().getObject(), item.getIndex());
							}
						};
						StringBuilder str = new StringBuilder(pad(item.getModel().getObject().getObject().getContent().getDisplayName()));
						link.add((new Label("title", str.toString())).setEscapeModelStrings(false));
						item.add(link);
						item.add(getMenu(item.getModel().getObject()));
					}
					else
					{ 
						item.setVisible(false);
					}
				
				} catch (Exception e) {
					Link<Void> link = new Link<Void>("link") {
						private static final long serialVersionUID = 1L;
						@Override
						public void onClick() {
						}
					};
					
					link.add((new Label("title", e.getClass().getName() + " | " + e.getMessage())).setEscapeModelStrings(false));
					item.add(link);
					item.add(new InvisiblePanel("menu"));
					logger.error(e);
				}
			};
				
		};
		
		lview.setOutputMarkupId(true);
		
		addOrReplace(lview);
		
		
	}

	
	
	public String getSortCriteria() {
		return this.sort_criteria;
	}
	
	String sort_criteria;
	public void setSortCriteria(String sortCriteria) {
		this.sort_criteria = sortCriteria;
		
	}



}
