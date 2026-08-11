package kbee.replica;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.model.DataSet;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.model.KbeePersonMember;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiObject;

public class KbeeReplicaLocalDao implements LocalMatcher {
	
	private JdbcTemplate jdbcTemplate;
	private Replica replica;
	
	public KbeeReplicaLocalDao() {
	}
	
	public KbeeReplicaLocalDao(Replica replica) {
		setReplica(replica);
	}
	
	public Replica getReplica() {
		return replica;
	}

	public void setReplica(Replica replica) {
		this.replica = replica;
	}
	
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate==null) {
			jdbcTemplate = new JdbcTemplate(getDataSource());
		}
		return jdbcTemplate;
	}

	public Long getLocal(ApiObject remote) {
		Long remoteId = remote instanceof ApiFile ? Long.valueOf(((ApiFile)remote).getOId()) : Long.valueOf(remote.getId());
		Object[] args = new Object[] { getReplica().getId(), remote.getClass().getSimpleName(), remoteId };
		int[] types = new int[] { java.sql.Types.BIGINT, java.sql.Types.VARCHAR, java.sql.Types.BIGINT };
		List<Long> locals = getJdbcTemplate().query(getLocalStatement(), args, types, new RowMapper<Long>() {
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong(1);
			}
		});
		if (locals==null || locals.isEmpty() || locals.size()>1)
			return null;
		return locals.get(0);
	}
	
	public void setLocal(ApiObject remote, Identifiable local) throws IOException {
		Long remoteId = remote instanceof ApiFile ? Long.valueOf(((ApiFile)remote).getOId()) : Long.valueOf(remote.getId());
		Long localId = remote instanceof ApiFile ? Long.valueOf(((Content)local).getOId()) : (Long)local.getId();
		getJdbcTemplate().update(getInsertStatement(), new Object[] {
				getReplica().getId(),
				remote.getClass().getSimpleName(),
				remoteId,
				localId, 
				new Date()
			});
	}
	
//	public void setLocal(IObject remote, Serializable localId) throws IOException {
//		getJdbcTemplate().update(getInsertStatement(), new Object[] {
//				getReplica().getId(),
//				remote.getClass().getSimpleName(),
//				Long.valueOf(remote.getId()),
//				localId, 
//				new Date()
//			});
//	}
	
	public void removeLocal(Identifiable local) throws IOException {
		String remoteclass = getRemoteClass(local);
		if (remoteclass!=null) {
			getJdbcTemplate().update(getDeleteStatement(), new Object[] {
					getReplica().getId(),
					remoteclass,
					local.getId() 
				});
		}
	}
	
	private String getInsertStatement() {
		return "insert into kb_replica_object(replica_id, kbclass, remote_id, local_id, replica_time) values (?, ?, ?, ?, ?)"; 
	}
	
	private String getLocalStatement() {
		return "select local_id from kb_replica_object where replica_id=? and kbclass=? and remote_id=?"; 
	}
	
	private String getDeleteStatement() {
		return "delete from kb_replica_object where replica_id=? and kbclass=? and local_id=?"; 
	}
	
	private DataSource getDataSource() {
		return (DataSource)ServiceLocator.getService(BeansService.class).getBean("dataSource");
	}
	
	private String getRemoteClass(Identifiable local) {
		if (local instanceof DataSet)
			return "IDataSet";
		if (local instanceof KbeePersonMember)
			return "IPersonValue";
		if (local instanceof KbeePerson)
			return "IPerson";
		if (local instanceof User)
			return "IUser";
		if (local instanceof Content)
			return "IFIle";
		return null;
	}
}