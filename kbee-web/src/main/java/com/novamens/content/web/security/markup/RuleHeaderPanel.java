package com.novamens.content.web.security.markup;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.security.IQLRule;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.RulesBC2;
import kbee.web.nav.SecurityDropDownMenuBC;
import kbee.web.object.TitleHeaderPanel;

public class RuleHeaderPanel extends TitleHeaderPanel<IQLRule> {
	private static final long serialVersionUID = 1L;
	

	IModel<String> icon = new Model<String>("far fa-gavel");

	public RuleHeaderPanel(IModel<IQLRule> model) {
		super("rule-panel", model);
		MenuBreadCrumbPanel  bc = new MenuBreadCrumbPanel();
		bc.addElement(new SecurityDropDownMenuBC());
		bc.addElement(new RulesBC2());
		bc.addElement(new BCElement(new Model<String>(model.getObject().getDisplayName())));
		setBreadCrumbPanel(bc);
	}
	
	protected IModel<String> getGlyphicon() {
		return icon; 
	}
} 
