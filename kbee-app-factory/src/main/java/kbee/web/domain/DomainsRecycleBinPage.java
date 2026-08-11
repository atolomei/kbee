package kbee.web.domain;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.command.ReindexByCriteriaCommand;
import com.novamens.kbee.content.support.Tip;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.nav.CommandsBC;
import kbee.web.nav.DomainsBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.page.FactoryPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.systeminfo.FactorySystemInfoDropdownBC;

public class DomainsRecycleBinPage extends DomainsPage implements FactoryPage {
		
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainsRecycleBinPage.class.getName());

	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 

	

	public DomainsRecycleBinPage() {
		this(null);
	}
	

	public DomainsRecycleBinPage(Query query) {
		super(query);
		logger.debug(this.getClass().getSimpleName());
		setPageTitle(new Model<String>("Domains Recycle Bin"));
	}

	
	protected Panel getBreadcrumbPanel() {
		MenuBreadCrumbPanel<?> bc =new MenuBreadCrumbPanel<Void>();
		bc.addElement( new HomeBC());
		
		DropDownMenuBC<?> dd = new DropDownMenuBC<Void>();
		
		
		dd.addElement(new DomainsBC(), true);
		dd.addElement(new DomainsBC());
		dd.addElement(new CommandsBC());
		bc.addElement(dd);
		bc.addElement(new BCElement("bc.recyclebin"));
		return bc;
		
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();

			
		
	    PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>();
		panel.setTitle(new StringResourceModel("bc.recyclebin", this, null));
		panel.setBreadcrumbPanel(getBreadcrumbPanel());
		setSearchPlaceHolder(new StringResourceModel("bc.recyclebin", DomainsRecycleBinPage.this, null).getObject());
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		panel.setSearchPanel(getSearchPanel());
			
		setPageContentHeader(panel);

		

	
	}

	
	
	@Override
	public Console<Domain> newConsole(Query query) {
		logger.debug("newConsole");
		return new DomainsRecyleBinConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return DomainsRecycleBinPage.this.getConsolePage(query, index);
			}
		};
	}


	@Override
	public void onDetach() {
		super.onDetach();
	}


	@Override
	public boolean hasPermissions() {
		return isDomainKbee() && (is_domain_admin || is_root || is_service_admin || is_factory_admin || is_api || is_linux);  
	}

//	private com.novamens.service.SecurityService getSecurityService() {
//		return ServiceLocator.getService(com.novamens.service.SecurityService.class);
//	}

	

	@Override
	public Page getConsolePage(Query query, long index) {
		return new DomainsRecycleBinPage(query);
	}
	
	
	@Override
	protected String getTipCategory() {
		return Tip.MODEL;
	}
	
 
	
	 

}
