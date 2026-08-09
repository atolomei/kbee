package kbee.web.report;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.QueryBuilder;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.command.ListResultSet;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlPlatformFactory;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;


public abstract class ReportQuery implements Query {
			
	private static final long serialVersionUID = 1L;

	public static final int MAX_REPORT_ROWS = 30000;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportQuery.class.getName());
	
	private Map<String, Object> parameters = new HashMap<String, Object>();
	
	public QueryBuilder getBuilder() {
		return null;
	};
	
	public String getTitle() {
		return "Query";
	};
	
	public ResultSet execute() {
		
		long start = System.currentTimeMillis();
		
		try {
				return new ListResultSet<Row>(getRows());
				
		} finally {
			if (logger.isDebugEnabled())
				logger.debug("Query:  " + String.valueOf(System.currentTimeMillis()-start)+" ms");
		}
	};
	
	public Map<String, Object> getParameters() {
		return this.parameters;
	};
	
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	};
	
	public void setParameter(String name, Object value) {
		this.parameters.put(name, value);
	};
	
	public void setOptions(Map<String, FacetOptions> options) {
		
	};
	
	public List<Facet> getFacets() {
		return null;
	}
	
	public DataSource getDataSource() {
		return (DataSource)ServiceLocator.getService(BeansService.class).getBean("dataSource");
	}
	
	/**
	 * @return
	 */
	protected abstract List<Row> getRows();

	
	public Row getErrorRow(List<Row> rows, String errorMsg) {
		
		if (rows==null || rows.size()==0) {
			return null;
		}
		
		try {
		Row row = rows.get(0);
		Row ret =new Row();
		
		int n=0;
		
		for (kbee.web.report.Row.Pair p: row.getValues()) {
			ret.put(p.getKey(), (n++==0?errorMsg:""));
		}
		return ret;
		} catch (Exception e) {
			return null;
		}
		
	}
	


	
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	
	protected SqlPlatform getSqlPlatform(Connection connection) {
		try {			
			return SqlPlatformFactory.getPlatformFor(connection.getMetaData());
		}
		catch (SQLException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
