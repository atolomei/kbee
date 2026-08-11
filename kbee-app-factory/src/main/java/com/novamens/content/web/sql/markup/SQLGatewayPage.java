package com.novamens.content.web.sql.markup;


import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorPanel;

import kbee.web.page.ApplicationPage;


/**
 * select     iot.relname as table_name,
       pg_size_pretty(pg_total_relation_size(iot.relid)) as total_size,
       pg_size_pretty(pg_relation_size(iot.relid)) as data_size,
       pg_size_pretty(pg_total_relation_size(iot.relid) - pg_relation_size(iot.relid))as external_size,
        to_char(ut.n_live_tup, '999,999,999,999') as nrows,
     iot.schemaname as table_schema
from pg_catalog.pg_statio_user_tables iot inner join pg_stat_user_tables ut on iot.relid = ut.relid
order by pg_total_relation_size(iot.relid) desc,
         pg_relation_size(iot.relid) desc
         
         
 *
 */
public class SQLGatewayPage extends ApplicationPage<Person> {
	
	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	
	private static final long serialVersionUID = 1L;

	private String query;
	
	public SQLGatewayPage() {
			this(null);
	}
	
	
	public SQLGatewayPage(String query) {
		setPageTitle(new ResourceModel("mainmenu.sqlgateway"));
		this.query=query;
	}
	
	private boolean isToPanels = true;
	
	public void setTwoPanels(boolean b) {
		isToPanels=b;
	}
	
	public boolean isTwoPanels() {
		return this.isToPanels;
	}
	
	
	final boolean root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	protected boolean hasPermissions() {
		return root && isKbeeDomain();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		Person person = getPerson();
		
		if (person!=null) {
			if (hasPermissions()) {
				setTopNavigation(getMainTopbar());
				setMenu(getMainLaternalMenu());   
				setModel(new ObjectModel<Person>(person));
				SQLPanel pa=new SQLPanel("panel", new ObjectModel<Person>(getPerson()), this.query);
				pa.setTwoPanels(isTwoPanels());
				add(pa); 
			}
			else {
				add(new ErrorPanel("panel", new Model<String>("not authorized")));
			}
		}
		else {
			add(new ErrorPanel("panel", "person not found", ""));
		}		
	}
	
	@Override
	protected Panel getFooter() {
		return null;
	}
	
}
