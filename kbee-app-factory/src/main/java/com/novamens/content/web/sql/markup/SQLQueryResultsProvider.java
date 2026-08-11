package com.novamens.content.web.sql.markup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;
import java.util.Properties;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;

import kbee.util.PropertiesFactory;


public class SQLQueryResultsProvider extends SortableDataProvider<SQLQuerySearchResult, String> {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SQLQueryResultsProvider.class.getName());

	
	static final long serialVersionUID = 1;
	static private final Properties props = PropertiesFactory.getInstance("kbee").getProperties();

	private String statement;
	private ResultSet resultSet;
	private Integer regMod;
	private boolean executed = false;
	private Connection con	= null;
	private	Statement st 	= null;
	
	public SQLQueryResultsProvider() {
		this.statement = "";
	}
	
	public SQLQueryResultsProvider(String statement) {
		this.statement = statement;
	}
	
	public Iterator<SQLQuerySearchResult> iterator(long first, long count) {
		try{


			List<SQLQuerySearchResult> resultList = new ArrayList<SQLQuerySearchResult>();
			
			if (count>0&&first>0) 
				getResultSet().absolute((int)first);
			
			if (first>500) 
				first = 500;
			
			int index = 0;

			ResultSetMetaData metadatos = resultSet.getMetaData();
 			
			List<String> columNames = new ArrayList<String>();
 			
			for(int i=1;i<=metadatos.getColumnCount();i++) {
 				columNames.add(metadatos.getColumnName(i));
 			}
			
			while (getResultSet().next() && index<count) {
				List<Object> valores = new ArrayList<Object>();
				for(String col: columNames){
					String object = resultSet.getString(col);	
					valores.add(object);
				}
				 
				SQLQuerySearchResult result = new SQLQuerySearchResult(valores, index);
				resultList.add(result);
				index++;
			}
			
			return resultList.iterator();
			
		} catch (SQLException e) {
			logger.error(e);
		}
		return null;
	}
	
	
	public long size() {
		long rowcount = 0;
		try {
			if (getResultSet()!=null&&getResultSet().last()) {
				rowcount = getResultSet().getRow();
				getResultSet().beforeFirst(); 
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return rowcount;
	}
	
	
	
	public IModel<SQLQuerySearchResult> model(SQLQuerySearchResult object) {
		return new SQLQuerySearchResultModel(object);
	}

	
	
	
	public void detach() {
		try {
			if (resultSet != null) {
				resultSet.close();
 			}
 			if (st != null) {
 				st.close();
 			}
 			if (con != null) {
 				con.close();
 			}
		} catch (SQLException ex) {}

		resultSet = null;
		st = null;
		con = null;
		executed = false;
	}

	protected void getObjects() throws SQLException {
		resultSet = getResultSet();
	}
	
	public ResultSet getResultSet() throws SQLException {
		
		if (getStatement()==null||getStatement().trim().equals("")) 
			return null;
		
		if((getStatement().toLowerCase().contains("update")||getStatement().toLowerCase().contains("delete"))&&!executed){
			con = DriverManager.getConnection(props.getProperty("jdbc.url").trim(), props.getProperty("jdbc.username").trim(), props.getProperty("jdbc.password").trim());		         
 			st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			regMod = st.executeUpdate(getStatement());
 			executed=true;
 			
		}else if ((!getStatement().toLowerCase().contains("update")&&!getStatement().toLowerCase().contains("delete"))&&this.resultSet == null) {
			con = DriverManager.getConnection(props.getProperty("jdbc.url").trim(), props.getProperty("jdbc.username").trim(), props.getProperty("jdbc.password").trim());		         
 			st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			resultSet = st.executeQuery(getStatement());
		}
		return resultSet;
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	public Integer getRegMod() {
		return regMod;
	}

	public void setRegMod(Integer regMod) {
		this.regMod = regMod;
	}



	

}
