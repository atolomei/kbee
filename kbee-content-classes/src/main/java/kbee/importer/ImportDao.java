package kbee.importer;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.novamens.dom.Domain;
import com.novamens.security.Identifiable;

import kbee.api.model.ApiObject;

@Deprecated
public class ImportDao implements LocalMatcher {
	private JdbcTemplate jdbcTemplate;
	private String serverUrl;
	private Domain localdomain;
	
	public ImportDao() {
	}
	
	public ImportDao(DataSource dataSource, String server, Domain localdomain) {
		setDataSource(dataSource);
		setServer(server);
		setLocalDomain(localdomain);
	}
	
	public void setServer(String url) {
		this.serverUrl = url;
	}
	
	public String getServer() {
		//return "localhost";
		return this.serverUrl;
	}
	
	public void setLocalDomain(Domain domain) {
		this.localdomain = domain;
	}
	
	public Domain getLocalDomain() {
		return this.localdomain;
	}
	
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public Long getLocal(ApiObject remote) {
		Object[] parameters = new Object[] { getServer(), remote.getDomain(), getLocalDomain().getName(), remote.getClass().getSimpleName(), Long.valueOf(remote.getId()) };
		//parameters[0] = "localhost";
		List<Long> locals = getJdbcTemplate().query(getLocalStatement(), parameters, new RowMapper<Long>() {
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong(1);
			}
		});
		if (locals==null || locals.isEmpty() || locals.size()>1)
			return null;
		return locals.get(0);
	}
	
	public void setLocal(ApiObject remote, Identifiable local) throws IOException {
		getJdbcTemplate().update(getInsertStatement(), new Object[] {
				getServer(),
				remote.getDomain(),
				remote.getClass().getSimpleName(),
				Long.valueOf(remote.getId()),
				getLocalDomain().getName(),
				local.getId(), 
				new Date()
			});
	}
	
	protected String getInsertStatement() {
		return "insert into kb_import_data(server_url, remote_domain, object_class, remote_id, local_domain, local_id, import_time) values (?, ?, ?, ?, ?, ?, ?)"; 
	}
	
	protected String getLocalStatement() {
		return "select local_id from kb_import_data where server_url=? and remote_domain=? and local_domain = ? and object_class=? and remote_id=?"; 
	}
}
