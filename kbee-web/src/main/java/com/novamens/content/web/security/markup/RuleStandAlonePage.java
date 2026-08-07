package com.novamens.content.web.security.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.security.IQLRule;

import kbee.web.nav.TabNavigationBar;
import kbee.web.page.AbstractApplicationPage;


@SuppressWarnings("serial")
public class RuleStandAlonePage extends AbstractApplicationPage<IQLRule> {
	private static final long serialVersionUID = -1L;

	/**
	 * @param model
	 * @param navigation
	 * @param editon
	 */
	public RuleStandAlonePage(IModel<IQLRule> model) {
		super(model, new TabNavigationBar<IQLRule>("navigation"));
		addComponents(model);
	}
    
	private void addComponents(IModel<IQLRule> model) {
		
		setPageTitle(new Model<String>(model.getObject().getName()));
		
		RuleMainPanel editor = new RuleMainPanel(getModel(), false) {
			@Override
			protected void onClose(AjaxRequestTarget target) {
				((TabNavigationBar<?>)RuleStandAlonePage.this.get("navigation")).onReturn(target);
			}
		};
		
		editor.setEditionEnabled(false);
		add(editor);
		
		getPageParameters().set("id", model.getObject().getId());
	}
}
