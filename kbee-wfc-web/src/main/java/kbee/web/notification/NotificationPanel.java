package kbee.web.notification;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.notification.ContentNotification;
import com.novamens.content.notification.Notification;
import com.novamens.content.notification.NotificationState;
import com.novamens.content.notification.WorkNoteNotification;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Proxy;
import com.novamens.content.notification.NotificationService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.model.service.ObjectModelService;


/***
 * 
 *  Note ()
 *  ----
 *  Expand                             Read
 *  Collapse 
 *  
 *  
 *  File Published (Email Notifications Rule)
 *  
 *  --------------
 *  Mark as Read and View File         Read
 *   	
 *    
 *  File Published (Document Ack Required)
 *  --------------
 *  Mark as Read and View File         Read
 *  
 *  
 *
 */
@SuppressWarnings("serial")
public class NotificationPanel extends KBPanel {

	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(NotificationPanel.class.getName());


	private boolean isVisibleText = false;
	private IModel<Notification> model;

	/**  
	 * 
	 */

	public class NoteFragment extends Fragment {
		
		public NoteFragment(String id) {
			super(id, "noteFragment", NotificationPanel.this);
		}
		
		/**
		 */
		@Override
		public void onInitialize() {
			super.onInitialize();


			WebMarkupContainer nic = new WebMarkupContainer("notification-icon-container") {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return true;
				}
			};
			
			add(nic);
			/**
			WebMarkupContainer ni = new WebMarkupContainer("notification-icon") {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return true;
				}
			};

			
			 ni.add(new AttributeModifier("title", getModel().getObject().getNotificationType().getDisplayName() + " ( " + getModel().getObject().getClass().getSimpleName() +" )"));
			 			ni.add(new AttributeModifier("class", clase +  ((getModel().getObject().getNotificationState().getId() ==	NotificationState.READ.getId())? " read " : " unread ")));
			nic.add(ni);


			*/
			 
			String clase = getModel().getObject().getIcon();
			
			WebMarkupContainer unread = new WebMarkupContainer ("unread") {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return ((getModel().getObject().getNotificationState().getId() ==	NotificationState.READ.getId()) ? false : true);
				}
			};
			nic.add(unread);

			
			
			
			AjaxLink<Notification> wn_link = new AjaxLink<Notification>("title-note-link", getModel()) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					try {
						setVisibleText(!isVisibleText());
						target.add(NotificationPanel.this);
						
					} catch (IndexOutOfBoundsException e) {
						logger.error(e, "Probable cause: the Alert was deleted ");
					} catch (Exception e) {
						logger.error(e);
					}
				}
			};
			
			wn_link.add(new AttributeModifier("title", getLabel("expand-collapse")));
			add(wn_link);
			
			Label wn_title = new Label("title-note", getModel().getObject().getTitle());
			wn_link.add(wn_title);
			
			addMetadataPanel(this);
			
			add(new WebMarkupContainer("menulink"));
			add(getMenu());
			
			
			
			WorkingAjaxLink<Notification> expand = new WorkingAjaxLink<Notification>("expand-collapse-link", getModel()) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					try {
						setVisibleText(!isVisibleText());
						target.add(NotificationPanel.this);
						
					} catch (IndexOutOfBoundsException e) {
						logger.error(e, "Probable cause: the Alert was deleted ");
					} catch (Exception e) {
						logger.error(e);
					}
				}
			};
			add(expand);
				
			Label lab= new Label("expand-collapse", new Model<String>() {
					public String getObject() {
						return isVisibleText() ? getLabel("collapse").getObject() : getLabel("expand").getObject();
					}
			});
			expand.add(lab);
			
			
			Label text = null;
			WebMarkupContainer mk = new WebMarkupContainer("text-container") {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return NotificationPanel.this.isVisibleText();
				}
			};
			add(mk);

			try {
				    String s= ((WorkNoteNotification) getModelObject()).getWorkNote().getText();
					text = new Label("text", s);
					text.add(new AttributeModifier("style", "margin-top:10px; float:left;"));
				
					if (s==null) {
						text.setVisible(false);
						setVisibleText(false);
					}
				
			} catch (Exception e) {
				text = new Label("text", e.getClass().getSimpleName() +  " " + e.getMessage());
		 	}
			
			text.setEscapeModelStrings(false);
			mk.add(text);
			
			 
			
			
		 

		}
		
	}
	
	/** ---------------------------------------------------------------------------------------------------
	 * 
	 * 
	 */
	public class ContentNotificationFragment extends Fragment {
		private static final long serialVersionUID = 1L;
		
		public ContentNotificationFragment(String id) {
			super(id, "alertFragment", NotificationPanel.this);
		}
		
		@Override
		public void onInitialize() {
			super.onInitialize();

			
			WebMarkupContainer nic = new WebMarkupContainer("notification-icon-container") {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return true;
				}
			};
			
			add(nic);
			
			/**
			WebMarkupContainer ni = new WebMarkupContainer("notification-icon") {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return true;
				}
			};
			nic.add(ni);
ni.add(new AttributeModifier("class", clase +  ((getModel().getObject().getNotificationState().getId() ==	NotificationState.READ.getId())? " read " : " unread ")));
			ni.add(new AttributeModifier("title", getModel().getObject().getNotificationType().getDisplayName() + " (" + getModel().getObject().getClass().getSimpleName() +") "));
			*/
			
			String clase = getModel().getObject().getIcon();
			
			

			WebMarkupContainer unread = new WebMarkupContainer ("unread") {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return ((getModel().getObject().getNotificationState().getId() ==	NotificationState.READ.getId()) ? false : true);
				}
			};
			nic.add(unread);

			
			
			
			Link<Notification> titlelink = new Link<Notification>("title-link", getModel()) {
				@Override
				public void onClick() {
					try {
						ServiceLocator.getService(NotificationService.class).markAsRead(getModel().getObject());
						onTitleClick(getModel());
					} 
					catch (IndexOutOfBoundsException e) {
						logger.error(e, "Probable cause: the Alert was deleted ");
					} 
					catch (Exception e) {
						logger.error(e);
					}
				}
			};

			titlelink.add(new AttributeModifier("title", getLabel("open-library")));
			add(titlelink);
			
			titlelink.add(new Label("title", getModel().getObject().getTitle()));
			
			add(new Label("subtitle", getModel().getObject().getText()));
			
			addMetadataPanel(this);
			
			addGenerationRulePanel(this);
			
			add(new WebMarkupContainer("menulink"));
			add(getMenu());
			
		}
	}
	
	/** ----------------------------------------------------------------------------
	 * 
	 * 
	 * @param id
	 * @param model
	 * 
	 */
	public NotificationPanel(String id, IModel<Notification> model) {
		super(id);
		this.model=model;
		setOutputMarkupId(true);
	}
	
	public Notification getModelObject() {
		return model.getObject();
	}
	
	public IModel<Notification> getModel() {
		return model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
			 if (getModel().getObject() instanceof ContentNotification)				add( new ContentNotificationFragment("notification-detail")); 	// ENotiRule / ActionRule
		else if (getModel().getObject() instanceof WorkNoteNotification)			add( new NoteFragment("notification-detail"));  				// Manual Alert
		else																		add( new InvisiblePanel("notification-detail")); 
			 
	}
	
	
	protected void addGenerationRulePanel(Fragment component) {
	
		Serializable ar=getModelObject().getGeneratingActionRule();
		Serializable enr=getModelObject().getGeneratingENotiRule();
		
		if (ar!=null) {
			component.add(new Label("generating-rule", "ActionRule: " + ar.toString()));
		}
		else if (enr!=null){
			component.add(new Label("generating-rule", "ENotiRule:" + enr.toString()));
		}
		else {
			component.add(new Label("generating-rule", "").setVisible(false));
		}
	}
	

	
	protected void addGeneratingAlertPanel(Fragment component) {
		
		if (getModel().getObject() instanceof WorkNoteNotification) {
			WorkNoteNotification noti =  (WorkNoteNotification) getModel().getObject();
			if (noti!=null)						
				component.add(new Label("generating-alert", "Alert: " + noti.getWorkNote().getDisplayName()));
			else 
				component.add(new Label("generating-alert", "null"));	
			
		}
		else {
			component.add(new Label("generating-alert", getModel().getObject().getClass().getSimpleName()));
		}
	}
	
	
	
	
	
	
	
	
	

	protected void addMetadataPanel(Fragment component) {
		Person person = getContentDao().findUserProfileByUser(model.getObject().getSender()).getPerson();
		
		
		
		
		StringBuilder str = new StringBuilder();

		
		//component.add(new Label("user", person.getFirstLastName()));
		
		   
		

		
		// style="float:left;"
		str.append(new StringResourceModel("published", NotificationPanel.this, null).setParameters(
				new Object[] {person.getFirstLastName()}).getObject() );
		
		// SUBJECT ?
		//component.add(new Label("note-type", getModelObject().getSubject(getSessionUser().getLocale())));
		
		OffsetDateTime xd = model.getObject().getCreationOffsetDateTime();
		String date_format = DateTimeService.COLlOQUIAL_AGO_LABEL;
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getSessionUser();
		String zid = service.getMapZoneIds().get(user.getTimeZone());
		
		if (zid==null)
			zid=ZoneId.systemDefault().getId();
		
		if (xd!=null) {
			ZonedDateTime zd = ZonedDateTime.ofInstant(xd.toInstant(), ZoneId.of(zid));
			component.add(new Label("date",service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago")).setEscapeModelStrings(false));

			
					/** if (date_format.equals(DateTimeService.COLlOQUIAL_AGO_LABEL))
				 else if (date_format.equals(DateTimeService.COLlOQUIAL_LABEL))			
					 str.append(service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL, null));
				 
				 else if (date_format.equals(DateTimeService.MONTH_DAY_YEAR_LABEL))		
					 tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year);
				 
				 else if (date_format.equals(DateTimeService.TIMESTAMP_LABEL))			
					 tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year_hh_mm_ss_zzz);
				 else																	
					 tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year_hh_mm);
				 
				//component.add((new Label("date", tst)).setEscapeModelStrings(false));
				 * **
				 */
		}
		else
			component.add(new Label("date","na").setEscapeModelStrings(false));
		
		component.add( (new Label("metadata",str.toString())).setEscapeModelStrings(false));
		
	}
	
	
	
	protected void onTitleClick(IModel<Notification> model) {
		Content content = ((ContentNotification) getModel().getObject()).getContent();
		if (content !=null && content.getState()==ObjectState.ENABLED)
			setResponsePage(getContentPage(content));
		else
			setResponsePage(new ApplicationErrorPage<Content>( new Model<String>(getModelObject().getTitle() + " " + getLabel("not-in-library").getObject()), new Model<String>("")));
	}
	
	protected void onAfterDelete(AjaxRequestTarget target) {
		
	}
	
	private Panel getMenu() {
		
		ContextMenuPanel<Notification> menu = new ContextMenuPanel<Notification>(model);
		
		menu.addItem(new MenuItemFactory<Notification>() {
			@Override
			public AbstractMenuItemPanelV5<Notification> getItem(String id) {
				return new LinkMenuItemPanel<Notification>(id) {
					@Override
					public void onClick() {
						try {															
							ServiceLocator.getService(NotificationService.class).markAsRead(getModel().getObject());
							Content content = ((ContentNotification) getModel().getObject()).getContent();
							if (content !=null && content.getState()==ObjectState.ENABLED)
								setResponsePage(getContentPage(content));
							else		
								setResponsePage(new ApplicationErrorPage<Content>(NotificationPanel.this.getLabel("not-in-library"), new Model<String>("")));
						}
						catch (java.lang.IndexOutOfBoundsException e) {
							logger.error(e);
						}	
						catch (Exception e) {
							logger.error(e);
						}
					}
					@Override
					public String getLabel() {	
						return NotificationPanel.this.getLabel("menu.readandopen").getObject();
					}
					@Override
					public String getTarget() {	
						return "_blank";
					}
					public String getBeforeClick() {
						return "refresh();";
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<Notification>() {
			@Override
			public AbstractMenuItemPanelV5<Notification> getItem(String id) {
				return new AjaxMenuItemPanelV5<Notification>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						ServiceLocator.getService(NotificationService.class).markAsRead(getModel().getObject());
						target.add(NotificationPanel.this);
					}
					@Override
					public String getLabel() {	
						return NotificationPanel.this.getLabel("menu.read").getObject();
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Notification>() {
			@Override
			public AbstractMenuItemPanelV5<Notification> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Notification>(id) {
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
		
		menu.addItem(new MenuItemFactory<Notification>() {
			@Override
			public AbstractMenuItemPanelV5<Notification> getItem(String id) {
				return new AjaxMenuItemPanelV5<Notification>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						ServiceLocator.getService(NotificationService.class).markAsDelete(getModel().getObject());
						target.add(NotificationPanel.this);
					}
					@Override
					public String getLabel() {	
						return NotificationPanel.this.getLabel("menu.delete").getObject();
					}
				};
			}
		});
	
		return menu;
	}	
	
	public IModel<String> getLabel(String key) {
		try {
			return new StringResourceModel(key, NotificationPanel.this, null);
		}
		catch (Exception e) {
			logger.error(e);
		}
		return new Model<String>(key);
	}
	
	private boolean isVisibleText() {
		return this.isVisibleText;
	}
	
	private void  setVisibleText(boolean b) {
		this.isVisibleText = b;
	}

	private KbeeUser getSessionUser() {
		return  (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
	
	private Page getContentPage(Content content) {
		return (Page) ServiceLocator.getService(BeansService.class).getBean(getContentClass(content) + "-page", ServiceLocator.getService(ObjectModelService.class).getObjectModel(content));
	}
	
	private String getContentClass(Content content) {
		return Proxy.getClassName(content).toLowerCase();
	}
}
