package kbee.web.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.acl.Acl;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.workflow.Procedure;


@SuppressWarnings("serial")
public class InheritedAclPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	boolean flag=false;
	List<SecurityRule> rules = null;
	public class AclModel implements IModel<Acl> {
		private IModel<T> model;
		private Acl acl;
		public AclModel(IModel<T> model) {
			this.model = model;			
		}
		public void setObject(Acl acl) {
		}
		public Acl getObject() {
			if (acl==null) {
				acl = (Acl) ServiceLocator.getService(ContentSystemSecurityService.class).getAcl(model.getObject());
			}
			return acl;
		}
		public void detach() {
			acl = null;
		}
	}
	
	public class AclEditor extends ObjectEditor<Acl> {
		public AclEditor(IModel<Acl> model) {
			super("editor", model);
		}
		@Override
		public boolean isEditionEnabled() {
			return false;
		}
	}
	
	public class AclEditor2 extends ObjectEditor<Acl> {
		public AclEditor2(IModel<Acl> model) {
			super("editor", model);
		}
		@Override
		public boolean isEditionEnabled() {
			return true;
		}
	}

	public InheritedAclPanel(String id, IModel<T> model) {
		super(id, model);
		
		if(model!=null && model.getObject()!=null && model.getObject().getExternalId()!=null) {
			flag=true;	
		}
		
			add(new Label("externalIdMessage", new StringResourceModel("externalIdText")) {
				@Override
				public boolean isVisible() {
					return false;
				}
			});		
		add(new AclEditorPanel("acl", new AclEditor(new AclModel(model))) {
			@Override
			protected List<Procedure> getProcedures() {
				return InheritedAclPanel.this.getModelObject().getContentTemplate().getProcedures();
			}
		});
		
		add(new ListView<SecurityRule>("rules", new PropertyModel<List<SecurityRule>>(this, "rules")) {
			public void populateItem(final ListItem<SecurityRule> item) {
				Link<Void> rulelink = new Link<Void>("rule-link") {
					public void onClick() {
						//final boolean role_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
						//final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
//						if (role_security)
//							//setResponsePage(new RuleStandAlonePage(new ObjectModel<IQLRule>((IQLRule)item.getModelObject())));
//						else {
//						}
					}
				};
				rulelink.add(new Label("rule-label", item.getModelObject().getName()));
				rulelink.add(new AttributeModifier("target", "_blank"));
				item.add(rulelink);
			}
		});
	}

	public List<SecurityRule> getRules() {
		if (rules==null) {
			rules = new ArrayList<SecurityRule>();
			for (SecurityRule rule : ServiceLocator.getService(ContentSystemSecurityService.class).getRules(getModelObject())) {
				if (rule.getCondition()!=null) {
					rules.add(rule);
				}
			}
			Collections.sort(rules, new Comparator<SecurityRule>() {
				@Override
				public int compare(SecurityRule a, SecurityRule b) {
					try {
						if (a.getName()==null)
							return (b.getName()!=null?1:0);
						else if (b.getName()==null)
							return -1;
						return a.getName().compareToIgnoreCase(b.getName());
					} catch (Exception e) {
						return 0;
					}
				}
				
			});	
		}
		return rules;
	}
	
	public void onDetach() {
		super.onDetach();
		rules = null;
	}
}
