package kbee.web.rule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.rule.KbeeClassificationAction;
import com.novamens.kbee.content.rule.KbeeRemoveClassificationAction;
import com.novamens.kbee.content.rule.KbeeSendNotificationAction;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.kbee.content.rule.KbeeArchiveAction;
import com.novamens.kbee.content.rule.KbeeDeleteAction;
import com.novamens.kbee.content.rule.KbeeLaunchAction;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.content.rule.Action;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

@SuppressWarnings("serial")
public class ActionFactoryPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ActionFactoryPanel.class.getName());
	

	protected final boolean root		   = ServiceLocator.getService(SecurityService.class).isRoot();
	protected final boolean role_admin     = root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	
	private boolean onlyAlerts = false;
	
	
	public enum ActionTemplate {
		
		CLASSIFICATION 			(200, "ClassificationAction", KbeeClassificationAction.class, true, false), 
		REMOVE_CLASSIFICATION 	(300, "RemoveClassificationAction", KbeeRemoveClassificationAction.class, true, false), 
		SEND_ALERT 				(100, "NotificationAction", KbeeSendNotificationAction.class, true, true),
		ARCHIVE 				(400, "ArchiveAction", KbeeArchiveAction.class, true, false), 
		DELETE 					(500, "DeleteAction", KbeeDeleteAction.class, true, false); 
		//LAUNCH 					(600, "LaunchAction", KbeeLaunchAction.class, true, true); 
		
		private int code;
		private String label;
		private boolean forContent;
		private boolean forEntity;
		private Class<?> javaclass;
		
		private  ActionTemplate(int code, String message, Class<?> javaclass, boolean forContent, boolean forEntity) {
			this.label = message;
			this.code = code;
			this.javaclass = javaclass;
			this.forEntity = forEntity;
			this.forContent = forContent;
		}

		public int getCode() {
			return code;
		}
		
		public String getLabel() {
			return label;
		}
		
		public Class<?> getJavaClass() {
			return javaclass;
		}
		
		public boolean isForContent() {
			return forContent;
		}
		
		public boolean isForEntity() {
			return forEntity;
		}
	}
	
	/**
	 * 
	 */
	public ActionFactoryPanel() {
		this("new-action");
	}
		
	public ActionFactoryPanel(String id) {
			this(id, false);
	}
	
	public ActionFactoryPanel(String id, boolean onlyAlerts) {
		super(id);
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		WebMarkupContainer newbutton = new WebMarkupContainer ("new-action-button");
		
		newbutton.add(new AttributeModifier("class", "btn-sm btn btn-primary dropdown-toggle"));
		newbutton.add(new AttributeModifier("data-toggle", "dropdown"));
		
		add(newbutton);
		
		List<ActionTemplate> vals = new ArrayList<ActionTemplate>();
			
		 if (!onlyAlerts && isAdmin()) {
				 vals.add(ActionTemplate.CLASSIFICATION);
				 vals.add(ActionTemplate.REMOVE_CLASSIFICATION);  
				 vals.add(ActionTemplate.ARCHIVE); 
				 vals.add(ActionTemplate.DELETE); 				
		 }
		 vals.add(ActionTemplate.SEND_ALERT);
		
		vals.sort(new Comparator<ActionTemplate>() {
			@Override
			public int compare(ActionTemplate o1, ActionTemplate o2) {
				try {
					String l1 = (new StringResourceModel(o1.getLabel(), ActionFactoryPanel.this)).getObject();
					String l2 = (new StringResourceModel(o2.getLabel(), ActionFactoryPanel.this)).getObject();
				return l1.compareToIgnoreCase(l2);
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		
		for(final ActionTemplate template : vals) {
			if ((template.isForContent() && forContent()) || (template.isForEntity() && forEntity())) {
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new AjaxMenuItemPanelV5<Void>(id) {
							@Override
							public void onClick(AjaxRequestTarget target) {
								try {
									onCreate(target, (Action)template.getJavaClass().getDeclaredConstructor().newInstance());
								}
								catch(Exception e) {
									logger.error(e);
								}
							}
							@Override
							public String getLabel() {
								return (new StringResourceModel(template.getLabel(), ActionFactoryPanel.this)).getObject();
							}
						};
					}
				});
			}
		}
		add(menu);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	protected boolean forContent() {
		return false;
	}
	
	protected boolean forEntity() {
		return false;
	}
	
	protected void onCreate(AjaxRequestTarget target, Action action) {
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected boolean isRoot() {
		return  this.root;
	}

	protected boolean isAdmin() {
		return this.role_admin;
	}
	
}
