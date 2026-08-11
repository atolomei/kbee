package kbee.web.enoti;

import org.apache.wicket.model.IModel;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleDao;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.nav.AlertManagementDropDownBC;
import kbee.web.nav.EmailNotificationsBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SecurityDropDownMenuBC;
import kbee.web.nav.UserBC;
import kbee.web.nav.UsersBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.query.ENotiRulesQuery;

public class ENotiRulePage extends ApplicationPage<ENotiRule> {
			
	private static final long serialVersionUID = 1L;


	boolean isNew =false;
	
	
	public class ENotiRuleBC extends BCElement {
		private static final long serialVersionUID = 1L;
		public IModel<String> getLabel() {
			return new Model<String>() {
				private static final long serialVersionUID = 1L;
				public String getObject() {
					return  new StringResourceModel("bc.enoti", ENotiRulePage.this, null).getString();
				};
			};
		}
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	public ENotiRulePage(PageParameters parameters) {
		ENotiRule rule = getRule(parameters);
		if (rule != null) {
			setTopNavigation(getMainTopbar());  
			setMenu(getMainLaternalMenu());  
			setModel(new ObjectModel<ENotiRule>(rule));
			addComponents(getModel(), false, false);
		}
		else {
		
			add(new ErrorPanel("editor", "rule not found", ""));
		}
	}
	
	public ENotiRulePage(IModel<ENotiRule> model) {
		this(model, false, false, false);
	}
	
	
	public ENotiRulePage(IModel<ENotiRule> model, boolean edition, boolean isMyAccount, boolean isNew) {
		super(model);
		
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
		addComponents(model, edition, isMyAccount);
	}
	
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=new ENotiRulesQuery();
				q.getParameters().put("text", event.getText());
				q.getParameters().put("sort", "relevance");
				setResponsePage(new ENotiRulesPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		panel.setTitle(getModel().getObject().getDisplayName());
		
		
		if (!getModel().getObject().isSystem()) {
			// My Workflow Alerts
			//
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
			bc.addElement(new SecurityDropDownMenuBC());
			bc.addElement(new UsersBC());
			bc.addElement(new UserBC(getModel().getObject().getOwner()));
			// 
			
			// bc.addElement(new ENotiRuleBC());
			bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName()+" <span class=\"ago\">("+ new StringResourceModel("file-alerts", ENotiRulePage.this, null).getObject()  +")</span>")));
			panel.setBreadcrumbPanel(bc);
		} 
		else {
			// Workflow Alerts System
			//
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
			bc.addElement(new HomeBC());
			
			bc.addElement(new AlertManagementDropDownBC());
			bc.addElement( new EmailNotificationsBC());
			bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
			panel.setBreadcrumbPanel(bc);
		}
		
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.enoti", this, null).getObject()));
		setSearchPanel(false);
		setAdvancedSearch(false);
		setSuggester(false);
		panel.setSearchPanel(getSearchPanel());
		
		setPageContentHeader(panel);
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	private void addComponents(IModel<ENotiRule> model, boolean edition, boolean isMyAccount) {
		
		setPageTitle(new Model<String>(model.getObject().getName()));
		ENotiRuleMainPanel editor = new ENotiRuleMainPanel(getModel());
		editor.setEditionEnabled(edition);
		editor.setIsNew(this.isNew);
		add(editor);
		getPageParameters().set("id", model.getObject().getId());
		
	}
	
	private ENotiRule getRule(PageParameters parameters) {
		if (parameters.get("id")!=null && !"".equals(parameters.get("id").toString())) {
			String ruleid = parameters.get("id").toString();
			ENotiRule rule = (ENotiRule) getENotiRuleDao().findENotiRuleById(Long.valueOf(ruleid));
			return rule;
		}	
		return null;
	}
	
	private ENotiRuleDao getENotiRuleDao() {
		return (ENotiRuleDao)ServiceLocator.getService(BeansService.class).getBean("enotiRuleDao");
	}

	

	
	public void setIsNew(boolean b) {
			this.isNew=b;
		
	}
}
