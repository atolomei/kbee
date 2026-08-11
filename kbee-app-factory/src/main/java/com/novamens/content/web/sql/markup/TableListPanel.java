package com.novamens.content.web.sql.markup;


import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField;

public class TableListPanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public TableListPanel(String id) {
		super(id);
	
	}

	
	
	private String tableName;
	
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName( String b) {
		this.tableName=b;
	}
	
	
	@Override
	public void onInitialize() {
			super.onInitialize();
		
		
		add(new ExtendedChoiceField<String>("tables", new PropertyModel<String>(this, "tableName"), new PropertyModel<List<String>>(this, "tables")) {
			private static final long serialVersionUID = 1L;
			@Override
			public String getIdValue(String value) {
				return value;
			}
			@Override
			public String getDisplayValue(String value) {
				return ( value);
			}
			
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				String value = getValue();
				onChange(target, value);	
			}
		});
		
		IModel<String> des = new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				return getTableDescription( getTableName());
			}
		};
		
		Label table_desc = new Label("desc", des);
		table_desc.setEscapeModelStrings(true);
		add(table_desc);
		
	}
	
	
	public IModel<String> getTableModel() {
		return new Model<String>(getTableName());
	}
	
	
	protected String getTableDescription(String name) {
		return name + "desc.";
	}

	protected void onChange(AjaxRequestTarget target, String value) {
	}
	
	
	public List<String> getTables() {
		return getContentDao().getTables();
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
}
