package com.novamens.content.web.security.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.IQLRule;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;


@SuppressWarnings("serial")
public class GroupRulesPanel extends Panel {
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(GroupRulesPanel.class.getName());
	
	private IModel<Group> model;
	List<SecurityRule> rules = null;
	
	public GroupRulesPanel(String id, IModel<Group> model) {
		super(id);
		setModel(model);
		add(new ListView<SecurityRule>("rules", new PropertyModel<List<SecurityRule>>(this, "rules")) {
			public void populateItem(ListItem<SecurityRule> item) {
				
				Link<Void> lnk = new Link<Void>("rule-link") {

					@Override
					public void onClick() {
						// TODO Auto-generated method stub
						GroupRulesPanel.this.onRuleClick(item.getModel());
					}
						
				};
				lnk.add(new AttributeModifier("target", "_blank"));
				lnk.add(new Label("rule", item.getModelObject().getName()));
				item.add(lnk);
				
			}
		});
	}
	
	protected void onRuleClick(IModel<SecurityRule> model2) {
		final boolean role_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
		if (role_security) {
			if (model2.getObject() instanceof IQLRule)
				setResponsePage(new RuleStandAlonePage(new ObjectModel<IQLRule>((IQLRule) model2.getObject())));
			else
				logger.info("Not IQL");
		}
		else
			logger.info("Not Authorized");
	}
	

	public void setModel(IModel<Group> model) {
		this.model = model;
	}
	
	public IModel<Group> getModel() {
		return this.model;
	}
	
	public void onDetach() {
		super.onDetach();
		model.detach();
		rules = null;
	}
	
	public List<SecurityRule> getRules() {
		if (rules==null) {
			rules = new ArrayList<SecurityRule>();
			for (SecurityRule rule : getSecurityDao().getRules(getDomain())) {
				if (rule.getCondition()!=null && includePrincipal(rule)) {
					rules.add(rule);
				}
			}
		}
		return rules;
	}
	
	private boolean includePrincipal(SecurityRule rule) {
		for (AclEntry entry : ((KbeeAcl)rule.getAcl()).getEntries()) {
			if (entry.getPrincipal().equals(getModel().getObject())) {
				return true;
			}
			else {
				if (getModel().getObject().getGroups().contains(entry.getPrincipal()))
					return true;
			}
		}
		return false;
	}
	
	private ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
