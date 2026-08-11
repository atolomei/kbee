package kbee.web.rule;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.EntityMember;
import com.novamens.content.rule.Action;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.rule.EntityRule;
import com.novamens.content.rule.LaunchAction;
import com.novamens.content.rule.SendNotificationAction;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.DomService;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.rule.KbeeActionRule;
import com.novamens.kbee.content.rule.KbeeEntityRule;
import com.novamens.kbee.content.rule.KbeeMultipleAction;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.iql.KbeeIqlHelpService;
import kbee.web.panel.AlertPanel;

@SuppressWarnings("serial")
public class EntityRuleEditor extends ObjectEditor<ActionRule> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EntityRuleEditor.class.getName());

	final boolean role_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	
	private List<Action> actions = null;
	private Panel a_p;
	
	
	/**
	 * 

	 *
	 */
	class ActionModel implements IModel<Action> {
		private int index;
		private Action action;
		public ActionModel(Action action) {
			this.action = action;
		}
		public Action getObject() {
			if (action==null) {
				action = getActions().get(index); 
			}
			return action;
		}
		public void setObject(Action action) {
			
		}
		public void detach() {
			if (action!=null) {
				index = getActions().indexOf(action);
				action = null;
			}
		}
	}
	

	/**
	 * 
	 *
	 */
	class DatesSequenceValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String sequence = validatable.getValue();
			StringTokenizer tokenizer = new StringTokenizer(sequence, "\r\n");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (!validDate(token)) {
					validatable.error(new ValidationError(getLabelString("invalid-date.message", token)));
				}
			}
		}
		// dd/MM
		private boolean validDate(String value) {
			try {
				String datevalue = value;
				if (value.length()<6) datevalue += "/"+OffsetDateTime.now().getYear();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/M/yyyy");
				LocalDate.parse(datevalue, formatter);
				return true;
			}
			catch (DateTimeParseException e) {
				return false;
			}
		}
	}


	/** ------------------------------------------------------------------
	 *
	 * 
	 * 
	 * 
	 */
	
	public EntityRuleEditor(IModel<ActionRule> model) {
		this("editor", model, false);
	}
	
	public EntityRuleEditor(String id, IModel<ActionRule> model, boolean isnew) {
		super(id, model);
		setOutputMarkupId(true);
		setIsNew(isnew);
		setEditionEnabled(isnew);
		setAction(getModelObject().getAction());
	}
	
	
	/**
	 * 
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		final OffsetDateTime nextExecution = ((KbeeEntityRule) getModel().getObject()).getNextExecution();
		final String s_nextexecution= nextExecution!=null ? ServiceLocator.getService(DateTimeService.class).format(nextExecution) : "";
		final OffsetDateTime lastExecution = ((KbeeEntityRule) getModel().getObject()).getLastExecution(); 
		final String s_lastexecution= lastExecution!=null ? ServiceLocator.getService(DateTimeService.class).format(lastExecution) : "";

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		AlertPanel<Void> info  =new AlertPanel<Void>("alert-info", AlertPanel.INFO, null,new Model<String>((getEntity().getDisplayName()!=null?getEntity().getDisplayName().toUpperCase():"")), 
			new StringResourceModel("alert-info", this, null).setParameters(new Object[]	{		s_lastexecution,s_nextexecution,} ));
		info.setIcon("fa-duotone fa-alarm-clock");
		
		form.add(info);
		
		form.add(new TextField<String>("name",  true));
		form.add(new BooleanField("calendar"));
		
		/**
		form.add(new TextField<String>("entity", () -> getEntity().getDisplayName()) {
			public boolean isEnabled() {
				return false;
			}
		});**/
		
		
		form.add(new TextAreaField<String>("condition", new DatesSequenceValidator(), 12, 4));
		
		form.add(new ActionFactoryPanel() {
			@Override
			public void onCreate(AjaxRequestTarget target, Action action) {
				EntityRuleEditor.this.addAction(action);
				getUpdatedParts().add("Add Action -> " + EntityRuleEditor.this.getModelObject().getDisplayName());
				target.add(EntityRuleEditor.this.get("form:actions"));
			}
			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
			@Override
			protected boolean forEntity() {
				return true;
			} 
		});
		
		WebMarkupContainer actions = new WebMarkupContainer("actions");
		
		actions.setOutputMarkupId(true);
		
		if (getModel().getObject().getAction()==null) {
			
			a_p = new AlertPanel<ActionRule>("alert", AlertPanel.DANGER, getModel(), null, 
					new StringResourceModel("no-action", EntityRuleEditor.this, null)
					) {
				public boolean isVisible() {
					if (isEditionEnabled())
						return false;
					return EntityRuleEditor.this.getModel().getObject().getAction()==null;  
				}
			};
			actions.add(a_p);
			((AlertPanel<ActionRule>)a_p).setIcon(AlertPanel.ATTENTION);
			
		}
		else {
			a_p = new InvisiblePanel("alert");
			actions.add(a_p);
		}
		
		
		
		actions.add(new ListView<Action>("action", getActions()) {
			public void populateItem(ListItem<Action> item) {
				item.addOrReplace( new WebMarkupContainer("menulink") {
					public boolean isVisible() {
						return isEditionEnabled();
					}
				});
				item.addOrReplace(getMenu(item.getModelObject()));
				item.addOrReplace(getEditor(item.getModelObject()));
			}
		});
		
		form.add(actions);
				
		add(form);
		
		add(new EditButtonsV5<ActionRule>(this) {
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled()  {
				return true;
			}
		});		
		
	}

	
	
	

	
	public void onClose(AjaxRequestTarget target) {
	}
	
	@Override
	public void cancel(AjaxRequestTarget target) {
		if (isNew()) {
			try {
				((KbeeActionRule) getModelObject()).getService(DOMObjectService.class).delete();
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		setEditionEnabled(false);
		onClose(target);
	}

	public void edit(AjaxRequestTarget target) {
		super.edit(target);
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeActionRule rule = (KbeeActionRule)getModelObject();
				rule.setAction(getAction());
				rule.getService(DomService.class).update(getUpdatedParts());
				super.reset();
				target.add(EntityRuleEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public EntityMember getEntity() {
		return ((EntityRule)getModelObject()).getEntity();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	protected Panel getMenu(Action action) {
		
		ContextMenuPanel<Action> menu = new ContextMenuPanel<Action>(getModel(action));
		
		menu.addItem(new MenuItemFactory<Action>() {
			@Override
			public AbstractMenuItemPanelV5<Action> getItem(String id) {
				return new AjaxMenuItemPanelV5<Action>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						getActions().remove(getModelObject());
						setUpdatedPart("Remove Action -> " + EntityRuleEditor.this.getModelObject().getDisplayName());
						target.add(EntityRuleEditor.this.get("form:actions"));
					}
					@Override
					public boolean isVisible() {	
						return true;
					}
					@Override
					public String getLabel() {	
						return new StringResourceModel("delete", EntityRuleEditor.this, null).getObject();
					}
					@Override
					public boolean isEnabled()  {
						return true;
					}
				};
			}
		});
		
		return menu;
	}	
	
	protected IModel<Action> getModel(Action action) {
		return new ActionModel(action);
	}

	protected void onCancel(AjaxRequestTarget target) {
		setEditionEnabled(false);
	}

	protected void onAfterSubmit(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}

	protected void onUpdate(AjaxRequestTarget target) {
		
	}
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}

	public Boolean getCalendar() {
		return getModel().getObject().getCalendar(); 
	}
	
	public void setCalendar( Boolean c) {
		((KbeeActionRule) getModel().getObject()).setCalendar(c);
	}

	protected void setAction(Action action) {
		actions = new ArrayList<Action>();
		if (action!=null && action instanceof KbeeMultipleAction) {
			actions.addAll(((KbeeMultipleAction)action).getActions());
		}
		else {
			if (action!=null) {
				actions.add(action);
			}	
		}
	}
	
	protected void addAction(Action action) {
		actions.add(action);
	}
	
	protected Action getAction() {
		Action action = null;
		if (actions.size()>1) {
			action = new KbeeMultipleAction(actions);
		}
		else {
			if (actions.size()==1) {
				action = actions.get(0);
			}
		}
		return action;
	}
	
	protected Panel getEditor(Action action) {
		if (action instanceof SendNotificationAction) {
			return new SendNotificationActionEditor((SendNotificationAction)action);
		}
		else 
//		if (action instanceof ClassificationAction) {
//			return new ClassificationActionEditor((ClassificationAction)action);
//		}
//		else
//		if (action instanceof ArchiveAction) {
//			return new ArchiveActionEditor((ArchiveAction)action);
//		}
//		else
//		if (action instanceof DeleteAction) {
//			return new DeleteActionEditor((DeleteAction)action);
//		}
//		else
		if (action instanceof LaunchAction) {
			return new LaunchActionEditor((LaunchAction)action) {
				@Override
				public Classificable getOwner() {
					return getEntity();
				}
			};
		}
		else {
			return null;
		}
	}
	
	protected List<Action> getActions() {
		return actions;
	}
	
	protected IModel<String> getPredicatesHelp() {
		return new Model<String>(getDomain().getService(KbeeIqlHelpService.class).getPredicatesHelp());
	}
//	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}
