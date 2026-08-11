package com.novamens.content.web.sql.markup;



import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;
import com.novamens.content.properties.Property;
import com.novamens.kbee.content.user.UserPropertyService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.error.ErrorPanel;

/**
 * 
 * 
 * COPY (select * from datasetmember ) TO 'c:\tmp\file.csv'  delimiter ',';

copy affunits FROM 'c:\tmp\props1.csv' csv DELIMITER ';' HEADER


 *
 */
@SuppressWarnings("serial")
public class SQLPanel extends ModelPanel<Person> {
	
	private static final long serialVersionUID = 1L;

	private boolean istwopanels = false;
																
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SQLPanel.class.getName());
	private static kbee.util.logging.Logger sqllogger = kbee.util.logging.Logger.getLogger("sql");
	
	private String query;
	
	
	
	public SQLPanel(String id, IModel<Person> model) {
		this(id, model, null);
	}
	
	public SQLPanel(String id, IModel<Person> model, String query) {
		super(id, model);
		setOutputMarkupId(true);
		this.query=query;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		addComponents(this.query);
		
		add(new WicketEventListener<InfoClickEvent>() {
			public void onEvent(InfoClickEvent event) {
				setTwoPanels(!isTwoPanels());
				if (isTwoPanels()) {
					SQLPanel.this.get("tools").add(new AttributeModifier("class", "col-lg-3 sqltools-twopanels"));
					SQLPanel.this.get("data").add(new AttributeModifier("class", "col-lg-9"));
					SQLPanel.this.get("tools").setVisible(true);
				} 
				else {
					SQLPanel.this.get("tools").setVisible(false);
					SQLPanel.this.get("data").add(new AttributeModifier("class", "col-lg-12"));
				}
				event.getRequestTarget().add(SQLPanel.this);
			}
		});
	}
	
	@Override
	public void onDetach() {
		get("tools").detach();
		if (get("data")!=null)
			get("data").detach();
		super.onDetach();
	}

	protected void saveUserQuery(String text) {
		
		if (text==null)
			return;
		
		try {
			List<Property> list = getSessionUser().getService(UserPropertyService.class).getPropertiesSet("sql-history", 30);
			for (Property p:list) {
					if (p.getValue().toString().toLowerCase().trim().equals(text.toLowerCase().trim()))
							return;
			}
			DateTimeFormatter dt= DateTimeFormatter.ISO_DATE_TIME;
			getSessionUser().getService(UserPropertyService.class).setProperty("query-"+dt.format(OffsetDateTime.now()), "sql-history", text);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	
	/**
	 * @param query
	 */
	private void addComponents(String query) {
		
		SQLToolsPanel sqt = new  SQLToolsPanel("tools", getModel()) {
			
			private static final long serialVersionUID = 1L;
			protected void onQueryChange(AjaxRequestTarget target, String text) {
				target.add(this);
			}
			
			
			@Override
			protected void onNewQuery(AjaxRequestTarget target, String text) {
				try {
					SQLDataPanel panel = new SQLDataPanel("data", text);
					SQLPanel.this.addOrReplace(panel);
					setTwoPanels(true);
					SQLPanel.this.get("tools").add(new AttributeModifier("class", "col-lg-3 sqltools-twopanels"));
					SQLPanel.this.get("data").add(new AttributeModifier("class", "col-lg-9"));
					saveUserQuery(text);
					sqllogger.debug(getSessionUser().getUserName() + " -> " + text);
					target.add(SQLPanel.this);
				
				} 
				catch (SQLException e) {
					// this if should never evaluate true
					//
					if (SQLPanel.this.get("data")==null) {
						String msg=e.getMessage();
						SQLPanel.this.addOrReplace(new ErrorPanel("data", e.getClass().getName(), msg));
					}
					logger.error(e);
					onQueryError(target, e);
				}
			}
		};
		
		sqt.setVisible(isTwoPanels());
		add(sqt);
		
		if (query==null)
			add(new InvisiblePanel("data"));
		else {
			try {
				SQLDataPanel panel = new SQLDataPanel("data", query);
				SQLPanel.this.addOrReplace(panel);
				SQLPanel.this.get("data").add( isTwoPanels() ? new AttributeModifier("class", "col-lg-9") : new AttributeModifier("class", "col-lg-12"));
				saveUserQuery(query);
				
			} 
			catch (SQLException e) {
				logger.error(e);
				
				
				String msg=e.getMessage();
				SQLPanel.this.addOrReplace(new ErrorPanel("data", e.getClass().getName(), msg));
			} 
			finally {
				((SQLToolsPanel) SQLPanel.this.get("tools")).setText(query);				
				SQLPanel.this.get("tools").add(new AttributeModifier("class", "col-lg-3 sqltools-twopanels"));
				setTwoPanels(true);
			}
		}
	}

	
    public void setTwoPanels(boolean b) {
		istwopanels =b;
	}
    
	private boolean isTwoPanels() {
		return istwopanels;
	}

}
