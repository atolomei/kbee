package kbee.web.util;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

public class DataSourceFactory implements FactoryBean<DataSource> {
	
	public DataSource getObject() throws Exception {
		return (DataSource) ServiceLocator.getService(BeansService.class).getBean("dataSource");
	}

	public Class<DataSource> getObjectType() {
		return DataSource.class;
	}
	
	public boolean isSingleton() {
		return true;
	}
}
