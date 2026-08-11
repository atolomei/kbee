package com.novamens.content.web.solr.markup;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.entity.Person;
import com.novamens.content.properties.Property;
import com.novamens.content.web.admin.markup.SystemInfoPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
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
 * 				
 */
public class SolrToolsPanel extends ObjectEditor<Person> {
			
	static Logger logger = LogManager.getLogger(SolrToolsPanel.class.getName());

	private static final long serialVersionUID = 1L;
	
	private String text;

	public SolrToolsPanel(String id, IModel<Person> model) {
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
		query.setRows(5);
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
		
		
		try {
		add(new UserQueryHistoryPanel("history", "solr-iql-condition", getModel()) {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("unchecked")
			protected void apply(AjaxRequestTarget target, IModel<Property> model) {
				if (model.getObject().getValue().toString()!=null) {
					setText(model.getObject().getValue().toString());
					((TextAreaField<String>) SolrToolsPanel.this.get("form:text")).setValue(model.getObject().getValue().toString());
					onNewQuery(target, getText());
					target.add(SolrToolsPanel.this);
				}
			}
						
			@SuppressWarnings("unchecked")
			protected void setQueryValue(AjaxRequestTarget target, IModel<Property> model) {
				if (model.getObject().getValue().toString()!=null) {
					setText(model.getObject().getValue().toString());
					((TextAreaField<String>) SolrToolsPanel.this.get("form:text")).setValue(model.getObject().getValue().toString());
					onQueryChange(target, model.getObject().getValue().toString());
					target.add(SolrToolsPanel.this);
				}
			}

		});
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			add(new InvisiblePanel("history"));
		}
	}

		
}
