package kbee.web.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.acl.Acl;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

@SuppressWarnings("serial")
public class AclPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
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

	public AclPanel(String id, IModel<T> model) {
		super(id, model);
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTab(new StringResourceModel("InheritedAcl",AclPanel.this,null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new InheritedAclPanel<T>(panelId, AclPanel.this.getModel());
			}
		});
		
		tabs.add(new AbstractTab(new StringResourceModel("OwnAcl",AclPanel.this,null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new OwnAclPanel<T>(panelId, AclPanel.this.getModel());
			}
		});
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs)  {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
			}
		};
		
		editor.setContentTopPanel(new ContentTitlePanel<T>("content-top-panel", model));
		add(editor);
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
					} 
					catch (Exception e) {
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
