package kbee.web.rule;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.base.Content;
import com.novamens.content.rule.Action;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.rule.ArchiveAction;
import com.novamens.content.rule.ClassificationAction;
import com.novamens.content.rule.DeleteAction;
import com.novamens.content.rule.SendNotificationAction;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.DomService;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.rule.ExecutionDateCalculator;
import com.novamens.kbee.content.rule.KbeeActionRule;
import com.novamens.kbee.content.rule.KbeeMultipleAction;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.DateTimeField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.iql.KbeeIqlHelpService;

@SuppressWarnings("serial")
public class ActionRuleEditor extends ObjectEditor<ActionRule> {

	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ActionRuleEditor.class.getName());

	final boolean role_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	
	private List<Action> actions = null;
	private IModel<Content> contentmodel = null;
	
	private Form<?> form;
	
	/** ------
	 * 
	 */
	private class ActionModel implements IModel<Action> {
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
	
	/** ------
	 * 
	 */
	private class IqlValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String statement = validatable.getValue();
			try {
				if (statement==null || "".equals(statement))
					return;
				IqlService iqlservice = getDomain().getService(IqlService.class);
				ResultSet set = iqlservice.execute(statement);
				set.hasNext();
			} 
			catch (RuntimeException e) {
				logger.error(e);
				validatable.error(new ValidationError(this));
			}
		}
	}

	/**
	 * 
	 */
	public ActionRuleEditor(IModel<ActionRule> model) {
		this("editor", model, false, null);
	}

	
	public ActionRuleEditor(String id, IModel<ActionRule> model, boolean isnew, IModel<Content> contentmodel) {
		super(id, model);
		setOutputMarkupId(true);
		setIsNew(isnew);
		setEditionEnabled(isnew);
		setContent(contentmodel);
		setAction(getModelObject().getAction());
	}
	
	public void setContent(IModel<Content> model) {
		contentmodel = model;
	}
	
	public Content getContent() {
		return contentmodel !=null ? contentmodel.getObject() : null;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		this.form = new Form<Void>("form", Disposition.VERTICAL);
		
		WebMarkupContainer contentContainer = new WebMarkupContainer("content-container");
	
		this.form.add(contentContainer);
		
		contentContainer.setVisible(this.isContentInstanceRule());
		contentContainer.add(new StaticField<String>("content",new Model<String>(getContentTitle())));
		
		this.form.add(new TextField<String>("name",  true));
		
		this.form.add(new TextAreaField<String>("condition", new IqlValidator(), 4, 4) {
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				IqlService iqlservice = getDomain().getService(IqlService.class);
				Expression expression = iqlservice.getExpression(getValue());
				ExecutionDateCalculator calculator = new ExecutionDateCalculator(expression);
				OffsetDateTime time = calculator.evaluate(getContent());
				OffsetDateTime time2 = ((ActionRule)getEditor().getModelObject()).getExecutionDate(getContent());
				System.out.println(time);
				System.out.println(time2);
				((DateTimeField)form.get("executionDate")).setValue(time);
				target.add(ActionRuleEditor.this);
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "How to express a Criteria"; }, getPredicatesHelp());
			}
		});
		
		this.form.add(new DateTimeField("executionDate", getZoneId(), () -> getExecutionDate()) {
			@Override
			public boolean isEnabled() {
				return false;
			}
		});
		
		this.form.add(new ActionFactoryPanel("new-action", isOnlyAlerts()) {
			@Override
			public void onCreate(AjaxRequestTarget target, Action action) {
				ActionRuleEditor.this.addAction(action);
				getUpdatedParts().add("Add Action -> " + ActionRuleEditor.this.getModelObject().getDisplayName());
				target.add(ActionRuleEditor.this.form.get("actions"));
			}
			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
			@Override
			protected boolean forContent() {
				return true;
			}
		});
		
		WebMarkupContainer actions = new WebMarkupContainer("actions");
		
		actions.setOutputMarkupId(true);
		
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
		
		this.form.add(actions);
		
		add(this.form);
		
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
			
			public String getStrStyle() {
				return ActionRuleEditor.this.getButtonsStyle();
			}
		});		
		
		add(new InfoDialog("help-modal"));
	}

	protected String getButtonsStyle() {
		return null;
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
		onClose(target);
	}

	@Override
	public void edit(AjaxRequestTarget target) {
		super.edit(target);
	}
	
	public boolean isContentInstanceRule() {
		return getModel().getObject().isContentRule();
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeActionRule rule = (KbeeActionRule)getModelObject();
				rule.setAction(getAction());
				rule.getService(DomService.class).update(getUpdatedParts());
				super.reset();
				target.add(ActionRuleEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public OffsetDateTime getExecutionDate() {
		return getModelObject().getExecutionDate(getContent());
	}

	protected boolean isOnlyAlerts() {
		return false;
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
						setUpdatedPart("Remove Action -> " + ActionRuleEditor.this.getModelObject().getDisplayName());
						target.add(ActionRuleEditor.this.get("form:actions"));
					}
					@Override
					public boolean isVisible() {	
						return true;
					}
					@Override
					public String getLabel() {	
						return new StringResourceModel("delete", ActionRuleEditor.this, null).getObject();
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
		if (action instanceof ClassificationAction) {
			return new ClassificationActionEditor((ClassificationAction)action);
		}
		else
		if (action instanceof ArchiveAction) {
			return new ArchiveActionEditor((ArchiveAction)action);
		}
		else
		if (action instanceof DeleteAction) {
			return new DeleteActionEditor((DeleteAction)action);
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
	
	private String getContentTitle() {
		
		if (getModel()==null || getModel().getObject()==null || getModel().getObject().getContentOId()==null)
			return "";
		
		Content content = getContent()!=null ?
			getContent() :
			getContentDao().findContentByOId(getModel().getObject().getContentOId());
		
		return content.getDisplayName();
	}
	
	private ZoneId getZoneId() {
		return getDomain()!=null ? ZoneId.of(getDomain().getTimeZone()) : ZoneId.systemDefault();
	}
}
