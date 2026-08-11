package kbee.web.idoc;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.lock.ValueLockerService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.rule.ActionRuleEditor;
import kbee.web.rule.ActionRuleMainPanel;
import kbee.web.rule.ActionRulePage;



/**
 * 
	ServiceLocator.getService(ValueLockerService.class).lock(value);
	
	despues esta el servico de lock persistentes de content  que se usa para los checkout
	new LockTransactionSynchronization(String.valueOf(activity.getContent().getId()))
 * 
 * 
 * @param <T>
 */


public class ContentReminderPanel<T extends Content> extends ModelPanel<T> {
			
	// este lock se debe cambiar por uno basado en cada contentoid
	//
	
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentReminderPanel.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private IModel<ActionRule> ruleModel;

	private WebMarkupContainer editAlert;
	
	
	public ContentReminderPanel(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (ruleModel!=null)
			ruleModel.detach();
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		loadRule();
		
		// -- No Existe Alert -----------------------------------------------------------
		//
		WebMarkupContainer nolaerts = new WebMarkupContainer("no-alerts") {
			public boolean isVisible() {
				return !isActionRule();
			}
		};
		add(nolaerts);
		AjaxLink<T> ab = new  AjaxLink<T>("add", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
						ServiceLocator.getService(ValueLockerService.class).lock(getModel().getObject().getOId());
						loadRule();
						if (!isActionRule()) {
							ruleModel = new ObjectModel<ActionRule>(ServiceLocator.getService(ContentFactoryService.class).createRule( (Content) ContentReminderPanel.this.getModel().getObject()));
							editAlert.addOrReplace(newRuleEditor());
							// Page page = new ActionRulePage(getRuleModel(), null, true);
							// setResponsePage(page);
						}
						target.add(ContentReminderPanel.this);
				} 
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<Void>(e));
				}
				finally {
					ServiceLocator.getService(ValueLockerService.class).unlock(getModel().getObject().getOId());
				}
			}
			public boolean isVisible() {
				return !isActionRule();
			}
		};
		add(ab);

		// -- Existe Alert -----------------------------------------------------------
		//
		editAlert = new WebMarkupContainer("edit-alert" ) {
			public boolean isVisible() {
				return isActionRule();
			}
		};
		
		if (isActionRule()) {
			ActionRuleEditor panel = newRuleEditor();
			panel.setEditionEnabled(false);
			editAlert.add(panel);
		}
		else {
			editAlert.add(new InvisiblePanel("content-alert-info"));
		}
		
		/**
		Link<T> b_editAlert = new  Link<T>("edit", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				try {
					// este lock se debe cambiar por uno basado en cada contentoid
					//
					lock.readLock().lock();
						loadRule();
						if (isActionRule()) {
							Page page = new ActionRulePage(getRuleModel(), null, false);
							setResponsePage(page);
						}
				}
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<Void>(e));
				}
				finally {
					lock.readLock().unlock();
				}
			}
			public boolean isVisible() {
					return isActionRule();
			}
		};
		editAlert.add(b_editAlert);
		**/
		
		add(editAlert);

	}

	private ActionRuleEditor newRuleEditor() {
		
		ActionRuleEditor panel = new ActionRuleEditor("content-alert-info", getRuleModel(), false, new ObjectModel<Content>(getModelObject())) {
			@Override
			protected String getButtonsStyle() {
				return "margin-top:-48px;";
			}
			@Override
			protected boolean isOnlyAlerts() { 
				return true;
			}
			@Override
			public void setEditionEnabled(boolean value) {
				ContentReminderPanel.this.setEditionEnabled(value);
			}
			@Override
			public boolean isEditionEnabled() {
				return ContentReminderPanel.this.isEditionEnabled();
			}
			@Override
			public void onClose(AjaxRequestTarget target) {
				ContentReminderPanel.this.onClose(target);
			}
			@Override
			public void onCancel(AjaxRequestTarget target) {
			 	setEditionEnabled(false);
				target.add(ContentReminderPanel.this);
			}
		};
		
		return panel;
		
		
	}
	protected void onClose(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(ContentReminderPanel.this);
	}

	protected void setEditionEnabled(boolean value) {
		editionEnabled=value;
		
	}

	boolean editionEnabled = true;
	protected boolean isEditionEnabled() {
		return editionEnabled;
	}

	public IModel<ActionRule> getRuleModel() {
		return ruleModel;
	}
	
	private void loadRule() {
		ActionRule rule = getContentDao().findActionRuleByContentOId(getModel().getObject().getOId());
		if (rule!=null)
			this.ruleModel = new ObjectModel<ActionRule>(rule);
	}

	private boolean isActionRule() {
		return (ruleModel!=null);
	}
	
	

}
