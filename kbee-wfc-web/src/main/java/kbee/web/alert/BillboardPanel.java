package kbee.web.alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.notification.Notification;
import com.novamens.content.notification.WorkNoteNotification;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.notes.KbeeBillboard;
import com.novamens.content.notification.NotificationService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceLink2;

public class BillboardPanel extends KBPanel {
				
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BillboardPanel.class.getName());
	private int _index = 0;

	public class BillboardAlertFragment extends Fragment {
		private static final long serialVersionUID = 1L;

		Map<String, Object> parameters;
		IModel<Notification> model;
		
		public IModel<Notification> getModel() {
			return this.model;
		}
		
		public void onDetach() {
			super.onDetach();
			
			notifications_list = null;
			
			if (this.model!=null)
				this.model.detach();
		}
		
	
		public BillboardAlertFragment (String id, IModel<Notification> model) {
			super(id, "billboard-alert-fragment", BillboardPanel.this);
			this.model=model;
			getParameters().put("title", model.getObject().getTitle());
			if (model.getObject() instanceof WorkNoteNotification) {
				String s=((WorkNoteNotification) model.getObject()).getWorkNote().getGlyphicon();
				
				if (s!=null) {
					// getParameters().put("icon", KbeeBillboard.getFontAwesomeIcon(s));
					getParameters().put("icon", s);
					getParameters().put("icon-color", "success");
				}
				
				getParameters().put("text", ((WorkNoteNotification) model.getObject()).getWorkNote().getText());
			}
			else
				getParameters().put("text", model.getObject().getText());
		}
		
		@SuppressWarnings("serial")
		public void onInitialize() {
			super.onInitialize();
			
			WorkingIndicatorAjaxLinkV5<Notification> close= new WorkingIndicatorAjaxLinkV5<Notification>("close", "close") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					ServiceLocator.getService(NotificationService.class).markAsRead(BillboardAlertFragment.this.getModel().getObject());
					getNotifications().remove(getIndex());
					if (getNotifications().isEmpty())
						target.add(BillboardPanel.this.getPage()); 
					else {
						try {
						  Thread.sleep(100);
							WebMarkupContainer c = new BillboardAlertFragment("billboard-notification", new ObjectModel<Notification>(getNotifications().get(getIndex())));
							((WebMarkupContainer) BillboardPanel.this.get("bill-container")).addOrReplace(c);					
							target.add(BillboardPanel.this);
						} catch (Exception e) {
							logger.error(e);
						}
					}
				}
			};
			add(close);
			
			
			
			
			WebMarkupContainer wc = new WebMarkupContainer("attachment-container");
			add(wc);
			if (getModel().getObject().getFile()!=null) {
				Link<Resource> ofile = new ResourceLink2("openfile", new ObjectModel<Resource>(getModel().getObject().getFile()));
				wc.add(ofile);
				ofile.add(new Label("file", getModel().getObject().getFile().getName()));
			}
			else
				wc.setVisible(false);
			
			
			
			Label dt=new Label("datetime", ServiceLocator.getService(DateTimeService.class).format( model.getObject().getLastModifiedOffsetDateTime(), null, getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_year));
			add(dt);
			
			Label title= new Label ("title", new Model<String>() {
				public String getObject() {
					return (String) getParameters().get("title");
				}			
			}) {
				public boolean isVisible() {
					return ((String) getParameters().get("title")!=null);
				}
			};
			add(title);
			Label text= new Label ("text", new Model<String>() {
				public String getObject() {
					return (String) getParameters().get("text");
				}
				
			}) {
				public boolean isVisible() {
					return ((String) getParameters().get("text")!=null);
				}
			};
			text.setEscapeModelStrings(false);
			
			add(text);
			WebMarkupContainer icon= new WebMarkupContainer ("icon") {
				public boolean isVisible() {
					return ((String) getParameters().get("icon")!=null);
				}
			};
			
			
			// icon --------------------------------------------------------------------
			//
			WebMarkupContainer icon_container = new WebMarkupContainer("icon-container");
			icon_container.setVisible((String) getParameters().get("icon")!=null);
			add(icon_container);
			icon.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					
					if ((String) getParameters().get("icon") != null) {
						return  (String) getParameters().get("icon") + (
								(String) getParameters().get("icon-color")!=null ?
							    (" "+ (String) getParameters().get("icon-color")) : "" ) ;
					}
					return "";
				}
			}));
			icon_container.add(icon);
			

			// image --------------------------------------------------------------------
			//
			WebMarkupContainer image_container = new WebMarkupContainer("image-container");
			image_container.setVisible(false);
			add(image_container);
			//image_container.add(icon);

			
		}

		/**
		 * 
		 * until ok
		 * 1 
		 * 2
		 * 3 days (until ok)
		 * 
		 */

		public Map<String, Object> getParameters() {
			if (parameters==null)
				parameters =  new HashMap<String, Object>();
			return parameters;
		}
		
		public void setParameters(Map<String, Object> parameters) {
			this.parameters=parameters;
		}
	}
	
			

	
	public BillboardPanel() {
			this("billboard");
	}
	
	public BillboardPanel(String id) {
		super(id);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);

		
		
		WebMarkupContainer billc =new WebMarkupContainer("bill-container") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return !getNotifications().isEmpty();
			}
		};
		
		billc.setOutputMarkupId(true);
		add(billc);
		
		if (getNotifications().isEmpty()) {
			billc.add(new InvisiblePanel("billboard-notification"));
			setVisible(false);
			return;
		}
		billc.add( new BillboardAlertFragment("billboard-notification", new ObjectModel<Notification>(getNotifications().get(getIndex()))));
		
	}

	

	protected int getIndex() {
		return _index;
	}
	
	protected void setIndex(int n) {
		this._index=n;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	
	private List<Notification> notifications_list = null;
	
	
	public List<Notification> getNotifications() {
			return getNotifications(false);
	}
	
	
	public void setNotifications(List<Notification> list ) {
		notifications_list = list;
	}
	
	/**
	 * @return
	 */
	public List<Notification> getNotifications(boolean force) {
		try {

			if (notifications_list!=null && !force)
				return notifications_list;
			
			this.notifications_list = getContentDao().getBillboardNotifications(getSessionUser());
			
		} catch (ContentMgmtException e) {
			logger.error(e);
			return new ArrayList<Notification>();
		}
		
		catch (javax.persistence.PersistenceException  e2 ) {
			logger.error(e2);
			
			// logger.info("removing all Notifications for user " + getSessionUser().getUserName());
			// ServiceLocator.getService(NotificationService.class).deleteAll(getSessionUser());

			// TODO PASAR A EVENTO
			ServiceLocator.getService(NotificationService.class).evict();
			notifications_list = new ArrayList<Notification>();
		}
		catch (Exception e1) {
			logger.error(e1);
			this.notifications_list = new ArrayList<Notification>();
		}
		
		return this.notifications_list;
	}
	


}
