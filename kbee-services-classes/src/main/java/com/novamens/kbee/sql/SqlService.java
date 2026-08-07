package com.novamens.kbee.sql;

import java.sql.Connection;
import java.sql.SQLException;

import com.novamens.service.SystemService;

public class SqlService implements SystemService {
	
	private SqlPlatform sqlplatform;
	private DataSource dataSource;
	
	public SqlPlatform getSqlPlatform() {
		
		if (sqlplatform!=null) 
			return sqlplatform;
		
		Connection connection = null;
		
		try {
			connection = getDataSource().getConnection();
			sqlplatform = SqlPlatformFactory.getPlatformFor(connection.getMetaData());
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (connection!=null) {
				try {
					connection.close();
				}				
				catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		return sqlplatform;
	}
	
	public void setDataSource(DataSource datasource) {
		this.dataSource = datasource;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
}
