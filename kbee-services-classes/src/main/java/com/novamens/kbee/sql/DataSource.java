package com.novamens.kbee.sql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class DataSource extends BasicDataSource {
	public GenericObjectPool<PoolableConnection> getPool() {
		return getConnectionPool();
	}	
}
