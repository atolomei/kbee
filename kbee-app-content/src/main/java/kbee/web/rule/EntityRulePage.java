package kbee.web.rule;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.EntityMember;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.rule.EntityRule;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.HREFBCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.DataSetMembersBC;
import kbee.web.nav.DataSetMembersSectionBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.EntityRulesBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

public class EntityRulePage extends ApplicationPage<ActionRule> {
	private static final long serialVersionUID = -1L;

	private boolean isNew = false;
	
	final boolean is_root = 
		ServiceLocator
		.getService(SecurityService.class)
		.isRoot(); 
	final boolean is_domain_admin  = 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	public EntityRulePage(PageParameters parameters) {
		ActionRule rule = getActionRule(parameters);
		if (rule != null)
			setModel(new ObjectModel<ActionRule>(rule));
	}
	
	public EntityRulePage(IModel<ActionRule> model, boolean isNew) {
		this(model, new GlobalNavigationBar<ActionRule>("navigation"), isNew);
	}
	
	public EntityRulePage(IModel<ActionRule> model, Panel navigation, boolean isnew) {
		super(model, navigation);
		this.isNew=isnew;
		getPageParameters().set("ruleid", model.getObject().getId());
		
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		if (getModel()==null || getModel().getObject()==null) {
			setResponsePage( new ApplicationErrorPage<>(new Model<String>("Rule is null")));
			return;
		}
		
		
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());

		if (hasPermissions()) {
			
			PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<>();
			
			MenuBreadCrumbPanel<?> bc = new MenuBreadCrumbPanel<>();
			
			bc.addElement( new HomeBC());
			bc.addElement(new SettingsDropDownBC());
			
			DropDownMenuBC<?> dd = new DropDownMenuBC<>();
			dd.addElement(new BCElement("bc.dataset.members"), true);
			dd.addElement(new DataSetMembersSectionBC());
			dd.addElement(new SeparatorBC());
			for (IModel<DataSet> ds: getDataSets())
				 dd.addElement( new DataSetMembersBC(ds)); 
			bc.addElement(dd);
			
			bc.addElement(new HREFBCElement("bc-menu-item", "/dataset/"+getEntity().getDataSet().getId().toString(), 
				() -> getEntity().getDataSet().getName() +
				(getEntity().getDataSet().isAggregation()?  ("<span class=\"ago\"> (" + getLabel("built-in").getObject()+")</span>"):"")));

			bc.addElement(new HREFBCElement("bc-menu-item", getEntity().getService(UrlService.class).getRelativeUrl(), () -> getEntity().getDisplayName()));
			bc.addElement(new EntityRulesBC());
			
			panel.setBreadcrumbPanel(bc);
			
			setPageTitle( new Model<String>(getModel().getObject().getDisplayName()));
			panel.setTitle(getModel().getObject().getDisplayName());
			setSearchPanel(false);
			setAdvancedSearch(false);
			setSuggester(false);
			setPageContentHeader(panel);
			addComponents( getModel(), isNew);
		}	
		else {
			add(new ErrorPanel("editor", "not access || not found", ""));
		}
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DATA_MANAGEMENT;
	}
	
	public EntityMember  getEntity() {
		return ((EntityRule)getModelObject()).getEntity();
	}
	
	protected boolean hasPermissions() {
		
		if (getSessionUser()==null)
			return false;
		
		if (!getDomain().getId().equals(getModel().getObject().getDomain().getId())) {
			return false;
		}
		
		if (is_domain_admin ||  is_root) {
			return true;
		}
		
		if (getModelObject() instanceof EntityRule &&
			ServiceLocator
			.getService(UserService.class)
			.isWriteable(((EntityRule)getModelObject()).getEntity())) {
			return true;
		}

		return  false;
	}
	
	private void addComponents(IModel<ActionRule> model, boolean isnew) {
		EntityRuleMainPanel editor = new EntityRuleMainPanel(getModel(), isnew);
		editor.setEditionEnabled(isnew);
		add(editor);
		getPageParameters().set("ruleid", model.getObject().getId());
	}
	
	private ActionRule getActionRule(PageParameters parameters) {
		if (parameters.get("ruleid")!=null && !"".equals(parameters.get("ruleid").toString())) {
			String ruleid = parameters.get("ruleid").toString();
			ActionRule rule = getRepository(ActionRule.class).findById(Long.valueOf(ruleid));
			rule = !(rule instanceof EntityRule) ? null : rule; 
			return rule;
		}	
		return null;
	}
	
	private List<IModel<DataSet>> getDataSets() {
		List<IModel<DataSet>> datasets = new ArrayList<IModel<DataSet>>();
		for (DataSet dataset: getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain())) {
			if (dataset.getDataSetType() == DataSetType.STRING   	||
				dataset.getDataSetType() == DataSetType.EXTERNAL 	||
				dataset.getDataSetType() == DataSetType.ENTITY 		||
				dataset.getDataSetType() == DataSetType.SECURED 	||
				dataset.getDataSetType() == DataSetType.LABEL 		||
				dataset.getDataSetType() == DataSetType.PEOPLE)
			datasets.add(new ObjectModel<DataSet>(dataset));
		}
		return datasets;
	}
}
