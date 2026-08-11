package com.novamens.content.web.sql.markup;

import java.sql.SQLException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.entity.Person;
import com.novamens.content.properties.Property;
import com.novamens.content.web.admin.markup.SystemInfoPage;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.form.EditButtonsV5;
import kbee.web.user.UserQueryHistoryPanel;

import com.novamens.wicket.markup.html.form.TextAreaField;

/**
 * Panel que se agrega en el DataManagementPanel para lanzar una query SQL
 * "sql-log"
 * 
 * 
 *   Tablas del schema ('public')
 *   --------------------------- 
 *   SELECT * FROM pg_catalog.pg_tables where schemaname='public' order by lower(tablename)
 *   
 *   
 *   Estructura de una tabla ('tablename')
 *   -------------------------------------
 *   select column_name, data_type from information_schema.columns where table_schema='public' and table_name='tablename';
 * 
 */
public class SQLToolsPanel extends ObjectEditor<Person> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SQLToolsPanel.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private String text;

	public SQLToolsPanel(String id, IModel<Person> model) {
		super(id, model);
		setOutputMarkupId(true);
		addComponents();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	@Override
	public void cancel(AjaxRequestTarget target) {
		setResponsePage( new SystemInfoPage());
	}
	
	public void update(AjaxRequestTarget target) {
			logger.info(getText());
			get("error").setVisible(false);
			onNewQuery(target, getText());
	}
	
	protected void onNewQuery(AjaxRequestTarget target, String text) {}
	protected void onQueryChange(AjaxRequestTarget target, String text) {}

	
	protected void onQueryError(AjaxRequestTarget target, SQLException e) {
		String err=e.getClass().getName();
		String msg=e.getMessage();
		logger.error(msg);
		Label error=new Label("error", err+" | " + msg);
		addOrReplace(error);
		target.add(this);
	}
	
	private void addComponents() {
	
		Label error=new Label("error", "");
		error.setVisible(false);
		add(error);
		
		com.novamens.wicket.markup.html.form.Form<?> form = new com.novamens.wicket.markup.html.form.Form<Void>("form", Disposition.VERTICAL);
		
		TextAreaField<String> query = new TextAreaField<String>("text", new PropertyModel<String>(this, "text"));
		query.setRows(6);
		query.setRequired(true);
		form.add(query);
		add(form);
		
		
		form.setOutputMarkupId(true);
		query.setOutputMarkupId(true);
		
		
		add(new EditButtonsV5<Person>(this, true) {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getEditClass() {
				return "btn btn-default btn-sm";
			}

			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sn";
			}
		});
		
		
		add(new WorkingIndicatorAjaxLinkV5<Void>("clean-cache", "Clean Cache") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					ServiceLocator.getService(com.novamens.event.EventService.class).fire(new com.novamens.kbee.event.EvictCacheServiceEvent());
					// Cleah Hibernate Cache
					getContentDao().cleanHibernateCache();
					 try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
					}
					target.add(SQLToolsPanel.this);	
				}
				
				@Override
				public String getWorkingLabel() {
					return "Working";
				}
				
				@Override
				public boolean isVisible() {
					return true;
				}
			
				@Override
				public boolean isEnabled() {
					return true;
				}
		});
		

		add(new WorkingIndicatorAjaxLinkV5<Void>("clear", "Clear") {
			private static final long serialVersionUID = 1L;
			@Override
	
			public void onClick(AjaxRequestTarget target) {
				try {
					setDownloadCommandExecuted(false);
				} catch (Exception e) {
					setDownloadResult(e.getClass().getSimpleName() + " | " + e.getMessage());
					logger.error(e);
				}
				target.add(SQLToolsPanel.this);
			}
			
			@Override
			public boolean isVisible() {
				return isDownloadCommandExecuted();
			}
		});
		
		
		/**
		add(new WorkingIndicatorAjaxLinkV5<Void>("download", "Download CSV") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					String query=getText();
					setDownloadCommandExecuted(true);
					DBQueryDumpCommand command = new DBQueryDumpCommand();
					command.setQuery(query);
					command.execute();
					setDownloadResult(command.getResult() + " | " + command.getResultComment());
				} catch (Exception e) {
					setDownloadResult(e.getClass().getSimpleName() + " | " + e.getMessage());
					logger.error(e);
				}
				target.add(SQLToolsPanel.this);
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		
			@Override
			public boolean isEnabled() {
				return true;
			}
		});

		
		Label dnres = new Label("download-result", new Model<String>() {
			
			public String getObject() {
				return getDownloadResult();
			}
		}) {
			
			private static final long serialVersionUID = 1L;

			public boolean isVisible() {
				return isDownloadCommandExecuted();
			}
		};
		
		add(dnres);
		*/
		
		try {
			add(new UserQueryHistoryPanel("history", "sql-history", getModel()) {
				private static final long serialVersionUID = 1L;
				@SuppressWarnings("unchecked")
				protected void apply(AjaxRequestTarget target, IModel<Property> model) {
					if (model.getObject().getValue().toString()!=null) {
						setText(model.getObject().getValue().toString());
						((TextAreaField<String>) SQLToolsPanel.this.get("form:text")).setValue(model.getObject().getValue().toString());
						onNewQuery(target, getText());
						target.add(SQLToolsPanel.this);
					}
				}
							
				@SuppressWarnings("unchecked")
				protected void setQueryValue(AjaxRequestTarget target, IModel<Property> model) {
					if (model.getObject().getValue().toString()!=null) {
						setText(model.getObject().getValue().toString());
						((TextAreaField<String>) SQLToolsPanel.this.get("form:text")).setValue(model.getObject().getValue().toString());
						onQueryChange(target, model.getObject().getValue().toString());
						target.add(SQLToolsPanel.this);
					}
				}
	
			});
		} catch (Exception e) {
			logger.error(e);
			add(new InvisiblePanel("history"));
		}
	}
	

	String download_description = "Command description";
	
	private void setDownloadResult(String b) {
		download_description=b;
	}
	
//	private String getDownloadResult() {
//		return download_description;
//	}
	
	boolean isdown=false;
	
	private void setDownloadCommandExecuted(boolean b) {
		this.isdown=b;
	}
	
	private boolean isDownloadCommandExecuted() {
		return this.isdown;
	}

	
	
//	
//	private boolean isLinux() {
//		if  (System.getenv("OS")!=null && System.getenv("OS").toLowerCase().contains("windows")) 
//			return false;
//		return true;
//	}

}
