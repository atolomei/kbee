package kbee.web.notification;


import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.notification.Notification;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 * 
 * Notifications Panel. 
 * It uses NotificationServices.
 * When the user clicks on the Title, it is left to the Console to open the content.
 * The only check performed before passing the execution to the Console is whether the Content is still 
 * in the User#s Workspace. It the Content is no longer in the users Workspace, the panel shows an error message.
 *  
 *
 */
@Deprecated
public class NotificationsPanel extends Panel {
				
	
	private static final long serialVersionUID = -2134839021149076423L;

	static final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(NotificationsPanel.class.getName());

	IModel<User> model;
	List<IModel<Notification>> nlist;	

	private FeedbackPanel feedback;
	
	private IModel<String> da = new StringResourceModel("notifications.acceptall", this, null);
	private IModel<String> dc = new StringResourceModel("notifications.close", this, null);

	//private NotificationService noti;
	private int total = 0;

	public NotificationsPanel(String id, IModel<User> umodel) {
		this(id, umodel, 234);
	}
 		
	/**
	 * 
	 * @param id
	 * @param umodel
	 * 
	 */
	public NotificationsPanel(String id, IModel<User> umodel, int left) {
		super(id);
		setModel(umodel);
		
		setOutputMarkupId(true);
		
		WebMarkupContainer main= new WebMarkupContainer("main-panel");
		add(main);
		main.add(new AttributeModifier("style", "left:"+String.valueOf(left)+"px;"));
		
		
		main.add(new AjaxLink<Void>("close-link") {
			private static final long serialVersionUID = -7494366195670591121L;
			public void onClick(AjaxRequestTarget target){
				NotificationsPanel.this.close(target);
			}
		});
										
		main.add(new AjaxLink<Void>("acceptall-link") {
			private static final long serialVersionUID = -1984585123431671168L;
			public void onClick(AjaxRequestTarget target){
			//
			}
		});
		
		main.add(new Label("total", new Model<String>("total") {
			private static final long serialVersionUID = 1393850016807763365L;
 			@Override
			public String getObject() {
				return "("+String.valueOf(getTotal())+")";
			}
		}));
		
		ListView<IModel<Notification>> notilist = new ListView<IModel<Notification>>("list", getNotifications()) {
			private static final long serialVersionUID = 218696482756845294L;
			@Override
			protected void populateItem(ListItem<IModel<Notification>> item) {
				
				try { 
						Notification no = item.getModelObject().getObject();
						//final int nindex = item.getIndex();
						
						String txt;
						if (no.getText()!=null) {
						    if (no.getText().length()>230)
						    	txt=no.getText().substring(0, 230)+"...";
						    else
						    	txt=no.getText();
						}
						else {
							txt=null;
						}
						
						AjaxLink<Void> title_link = new AjaxLink<Void>("title-link"){
							private static final long serialVersionUID = 4134052973623884532L;
		
							@Override
							public void onClick(AjaxRequestTarget target) {
								//Notification noti=getNotifications().get(nindex).getObject();
								//Content content = getContentDao().findContentById(noti.getContentId());
								//if (content!=null && content.getWorkspace()!=null && content.getWorkspace().equals(Long.valueOf(NotificationsPanel.this.getModel().getObject().getId().toString()))) {
								//	NotificationsPanel.this.onTitleClick(target, new ObjectModel<Content>(content));
								//}
								//else {
								//	feedback.info( (content!=null?content.getTitle() +" ":"") + new StringResourceModel("notifications.notfound", NotificationsPanel.this, null).getString());
								//	onInfo(target);
								//}
		 					}
						};
		 				
						title_link.add(new Label("title", no.getTitle()));
						item.add(title_link);
						
						item.add((new Label("text", txt)).setVisible(txt!=null));
						item.add(new Label("sender", no.getSender()!=null?no.getSender().getFirstLastName():"sender"));

						
						DateTimeService service = ServiceLocator.getService(DateTimeService.class);
						User user = getUser();
						String zid = service.getMapZoneIds().get(user.getTimeZone());
						if (zid==null) 
								zid=ZoneId.systemDefault().getId();
						ZonedDateTime zd = ZonedDateTime.ofInstant(no.getOffsetDateTimeSent().toInstant(), ZoneId.of(zid));
						String tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
						item.add( (new Label("time", tst)).setEscapeModelStrings(false));
						//DateFormatter.getInstance().timeElapsedColloquialWithAgo(date, getLocale().getLanguage(), "ago")
						
						item.setVisible(true);
						add(item);
		
						
						AjaxLink<Void> mark = new AjaxLink<Void>("markasread") {
							private static final long serialVersionUID = 3760956825755014543L;
							@Override
							public void onClick(AjaxRequestTarget target) {
								
		 						//
								
							}
						};
						item.add(mark);
				
				} catch (org.hibernate.ObjectNotFoundException e) {
					item.setVisible(false);
					logger.warn(e);
				}
			}
		};
		
		notilist.setOutputMarkupId(true);
		main.add(notilist);
		
		feedback = new FeedbackPanel("feedback") {
			private static final long serialVersionUID = -2166471612263570720L;
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (this.anyErrorMessage()) {
					tag.append("class", "error", " ");
				} 
				else if (anyMessage(FeedbackMessage.SUCCESS)) {
					tag.append("class", "ok", " ");
				}
			}
		};
		
	 	feedback.setEscapeModelStrings(false);
		feedback.setOutputMarkupId(true);
		main.add(feedback);
		
		main.add(new Behavior() {
			 
			private static final long serialVersionUID = -1932043492052563579L;

			@Override
			public void renderHead(Component component, org.apache.wicket.markup.head.IHeaderResponse response) {
				StringBuffer script = new StringBuffer();
				script.append("function hidefeedback(feedbackid) {");
				script.append("var styleObj = document.getElementById(feedbackid).style;");
				script.append("styleObj.display = 'none'");
				script.append("}");
				response.render(new JavaScriptContentHeaderItem(script.toString(), "feedback"));
			}
		});
		
	}
	
	public void onInfo(AjaxRequestTarget target) {
		target.add(feedback);
		target.appendJavaScript("setTimeout(\"hidefeedback('"+feedback.getMarkupId()+"')\",2600);");
	}
	
 	public void setModel(IModel<User> model) {
		this.model=model;
	}

	public IModel<User> getModel() {
		return model;
	}

	public void onTitleClick(AjaxRequestTarget target, IModel<Content> content) {}
 	
	public void onListChange(AjaxRequestTarget target, int index) {}
 	
//	private NotificationService getNotifierService() {
//		if (noti==null)
//			noti = ServiceLocator.getService(NotificationService.class);
//		return noti;
//	}

 	private List<IModel<Notification>> getNotifications() {
		if  (nlist==null)
			nlist = new ArrayList<IModel<Notification>>();
		return nlist;
	}
 
	public void close(AjaxRequestTarget target){}

	private int getTotal() {
		return total;
	}
	
//	private void setTotal(int total) {
//		this.total=total;
//	}
	
	@Override
	public void onDetach() {
		
		da.detach();
		dc.detach();
		feedback.detach();
		//noti=null;
		getModel().detach();
		
		@SuppressWarnings("unchecked")
		ListView<IModel<Notification>> notilist = (ListView<IModel<Notification>>) get("main-panel:list");
		for (IModel<Notification> model: notilist.getList()) { 
		try {
			model.detach();
			} catch (org.hibernate.ObjectNotFoundException e) {}
		}
		nlist=null;
		super.onDetach();
	}
	
// 	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
	
 	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
 }
