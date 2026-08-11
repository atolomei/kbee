package kbee.web.dataset;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.DomService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.repository.ActionRuleRepository;
import com.novamens.kbee.content.rule.KbeeActionRule;
import com.novamens.kbee.content.rule.KbeeEntityRule;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.rule.EntityRulePage;

@SuppressWarnings("serial")
public class EntityRulesPanel extends ModelPanel<DataSetMember> {
	private static final long serialVersionUID = 1L;
	
	final boolean role_admin =
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_dataset_members	= role_model || role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	
	public EntityRulesPanel(String id, IModel<DataSetMember> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		add(new AjaxLink<Void>("new-button") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				String name = getLabelString("newrule.label") +" " + getEntity().getDisplayName() + " " + String.valueOf( getRules().size() + 1);
				ServiceLocator.getService(ObjectFactoryService.class).createRule(getEntity(), name);
				target.add(EntityRulesPanel.this);
			}
			@Override
			public boolean isVisible() {
				return role_dataset_members || ServiceLocator
				.getService(UserService.class)
				.isWriteable(getEntity());
			}
		});
		
		add(new ListView<ActionRule>("rule", () -> getRules()) {
			
			public void populateItem(final ListItem<ActionRule> item) {
				
				KbeeActionRule rule = (KbeeActionRule)item.getModelObject();
				
				Link<Void> rulelink = new Link<Void>("rule-link") {
					public void onClick() {
						setResponsePage(new EntityRulePage(new ObjectModel<ActionRule>((ActionRule)item.getModelObject()), false));
					}
				};
				rulelink.add(new Label("label", rule.getName()));
				item.add(rulelink);
				
				OffsetDateTime nextExecution = ((KbeeEntityRule)rule).getNextExecution(); 
				item.add(new Label("nextexecution", nextExecution!=null ? format(nextExecution) : "-"));
				
				OffsetDateTime lastExecution = ((KbeeEntityRule)rule).getLastExecution(); 
				item.add(new Label("lastexecution", lastExecution!=null ? format(lastExecution) : "-"));
				
				item.add(new Label("status", getLabel(((KbeeEntityRule)rule).getState().name())));
				
				Label modifiedlabel = new Label("modified", getModifiedLabel(rule));
				modifiedlabel.setEscapeModelStrings(false);
				item.add(modifiedlabel);
				
				item.add(getMenu(rule));
			}
		});
		
		add(new ConfirmationDialog("confirmation-dialog"));
	}
	
	public EntityMember getEntity() {
		return (EntityMember)getModelObject();
	}
	
	public List<ActionRule> getRules() {
		return ((ActionRuleRepository)getRepository(ActionRule.class)).findByEntity(getEntity());
	}
	
	private void deleteRule(ActionRule rule) {
		((KbeeActionRule) rule).getService(DOMObjectService.class).delete();
	}
	
	private String getModifiedLabel(KbeeActionRule rule) {
		String label = rule.getLastModifiedUser().getDisplayName();
		label += " - " + rule.getLastModifiedOffsetDateTimeColloquial();
		return label; 
	}
	
	private Panel getMenu(ActionRule rule) {
		
		IModel<ActionRule> model = new ObjectModel<ActionRule>(rule, false);
		
		ContextMenuPanel<ActionRule> menu = new ContextMenuPanel<ActionRule>(model);
		
		menu.addItem(id ->
		new AjaxMenuItemPanelV5<ActionRule>(id) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				if (ObjectState.ENABLED.equals(getModelObject().getState()))
					getModelObject().setState(ObjectState.ARCHIVED);
				else
					getModelObject().setState(ObjectState.ENABLED);
				getModelObject().getService(DomService.class).update("status");
				target.add(EntityRulesPanel.this);

			}	
			@Override
			public String getLabel() {	
				if (ObjectState.ENABLED.equals(getModelObject().getState()))
					return getLabelString("menu.pause");
				else
					return getLabelString("menu.active");
			}
		});
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<ActionRule>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					getConfirmationDialog().open(target, 
							null,
							EntityRulesPanel.this.getLabel("delete.confirmation.message"), 
							EntityRulesPanel.this.getLabel("delete.confirmation.text"), 
							Dialog.Delete, 
							new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										deleteRule(getModelObject());
										target.add(EntityRulesPanel.this);
									}	
								}
							});
				}	
				@Override
				public String getLabel() {	
					return getLabelString("menu.delete");
				}
		});
		
		return menu;
	}	
	
	private String format(OffsetDateTime time) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String formatted = formatter.format(time);
		return formatted;
	}
	
	private ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("confirmation-dialog");
	}
}
