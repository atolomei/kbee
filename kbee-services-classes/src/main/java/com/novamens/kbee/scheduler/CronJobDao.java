package com.novamens.kbee.scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlPlatformFactory;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import org.hibernate.SessionFactory;

import com.novamens.content.user.UserService;
import com.novamens.dao.Dao;
import com.novamens.dom.Domain;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.scheduler.SchedulerException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class CronJobDao implements Dao {

    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger("Scheduler");

    private DataSource dataSource;
    private SessionFactory sessionFactory;
    private String tableName;
    private String schema;

    private JdbcTemplate jdbcTemplate;
    private SqlPlatform sqlplatform;

    private static String ENABLED_COLUMN = "ISENABLED";
    private static String ID_COLUMN = "ID";
    private static String NAME_COLUMN = "NAME";
    private static String CRONEXPRESSION_COLUMN = "CRONEXPRESSION";
    private static String DESCRIPTION_COLUMN = "DESCRIPTION";
    private static String CLASS_COLUMN = "CLAZZ";
    private static String PARAMETER_COLUMN = "PARAMETER";

    private static String EXECOLDTRIGGERS_COLUMN = "EXECOLDTRIGGERS";
    private static String LASTEXECUTION_COLUMN = "LASTEXECUTION";
    private static String DOMAIN_COLUMN = "DOMAIN";

    
    private static String TIMEZONE_COLUMN = "TIMEZONE";
    
    private static String ISENABLED_COLUMN = "ISENABLED";
    
    public List<AbstractCronJobRequest> getCronJobRequests() throws SchedulerException {
    			return getCronJobRequests(null);
    }
    
    
    public List<AbstractCronJobRequest> getCronJobRequests(Domain domain) throws SchedulerException {
    	
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {

            List<AbstractCronJobRequest> list = new ArrayList<AbstractCronJobRequest>();
            connection = this.getDataSource().getConnection();
            
            String getstatement = this.getGetStatement(domain);

            statement = connection.prepareStatement(getstatement);
            resultSet = statement.executeQuery();

            boolean done = false;
            int errno = 0;
            while (resultSet.next() && !done) {

                try {
                    AbstractCronJobRequest job = getJob(resultSet);
                    //logger.debug("getCronJobRequests() - Adding -> " + job.toString());
                    list.add(job);

                } catch (Exception e) {
                    if (errno++ > 3)
                        done = true;
                    logger.error(e);
                }
            }

            return list;

        } catch (Exception e) {
            logger.error(e);
            throw (new SchedulerException(e));
        } finally {
            try {
                close(connection, statement, resultSet);
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    public void setTableName(String name) {
        this.tableName = name;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getSchema() {
        if (this.schema != null && !"".equals(this.schema.trim()))
            return this.schema.trim() + ".";
        return "";
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }


    private AbstractCronJobRequest getJob(ResultSet resultSet) throws Exception {
        
    	//boolean is_enabled = resultSet.getBoolean(ISENABLED_COLUMN);

        String cron_expression = resultSet.getString(CRONEXPRESSION_COLUMN);
        if (cron_expression != null)
            cron_expression = cron_expression.replace("[", "").replace("]", "");

        CronExpressionJ8 ce = new CronExpressionJ8(cron_expression);

        String clazz = resultSet.getString(CLASS_COLUMN);
        if (clazz != null)
            clazz = clazz.trim();

        AbstractCronJobRequest job = (AbstractCronJobRequest) Class.forName(clazz).newInstance();

        job.setDomainId(resultSet.getLong(DOMAIN_COLUMN));
        job.setCronExpression(ce);
        job.setName(resultSet.getString(NAME_COLUMN));
        job.setDescription(resultSet.getString(DESCRIPTION_COLUMN));
        job.setId(resultSet.getLong(ID_COLUMN));
        job.setEnabled(resultSet.getBoolean(ENABLED_COLUMN));
        job.setTimeZone(resultSet.getString(TIMEZONE_COLUMN));
        job.setUserRequest(true);

        job.setExecuteOldTriggers(resultSet.getBoolean(EXECOLDTRIGGERS_COLUMN));
        
        if (job.getExecuteOldTriggers()) {
        	
            OffsetDateTime lastExecution = resultSet.getObject(LASTEXECUTION_COLUMN, OffsetDateTime.class);
            if (lastExecution != null) {
                job.setTime(job.getCronExpression().nextTimeAfter(lastExecution.atZoneSameInstant(ZoneId.of("UTC"))));
            }
        }

        Map<String, String> parameters = parseParameters(resultSet.getString(PARAMETER_COLUMN));

        if (parameters != null) {
            job.setParameters(parameters);
        }

        return job;
    }

    private String getParameterString(Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> param : parameters.entrySet()) {
            if (!first) {
                sb.append("\r|\n");
            }
            first = false;
            sb.append(param.getKey().replace("=", "\\="));
            sb.append("=");
            sb.append(param.getValue().replace("=", "\\="));
        }
        return sb.toString();
    }

    Map<String, String> parseParameters(String parametersvalue) {
        if (parametersvalue == null || parametersvalue.length() == 0)
            return null;

        Map<String, String> parameters = new HashMap<String, String>();

        if (!parametersvalue.matches("(?<!\\\\)(?:=)")) {//not escaped '='
            parameters.put("param", parametersvalue);
        }

        String values[] = parametersvalue.split("\\r|\\n");
        for (String line : values) {
            String kv[] = line.split("(?<!\\\\)(?:=)", 2);
            if (kv.length == 2) {
                String name = kv[0].trim().toLowerCase();
                String value = kv[1];
                value = value.replace("\\r", "");
                value = value.replace("\\n", "");
                parameters.put(name, value);
            } else {
                String kv2[] = line.split(":", 2);
                if (kv2.length == 2) {
                    String name = kv2[0].trim().toLowerCase();
                    String value = kv2[1];
                    parameters.put(name, value);
                }
            }
        }

        return parameters;
    }

    private String getGetStatement(Domain domain) {
   	if (domain==null)
    		return "SELECT ID, NAME, DESCRIPTION, CRONEXPRESSION, CLAZZ, PARAMETER, ISENABLED,LASTEXECUTION,EXECOLDTRIGGERS,DOMAIN, TIMEZONE FROM " + getSchema() + getTableName() + " ORDER BY NAME";
    	return "SELECT ID, NAME, DESCRIPTION, CRONEXPRESSION, CLAZZ, PARAMETER, ISENABLED,LASTEXECUTION,EXECOLDTRIGGERS,DOMAIN, TIMEZONE FROM " + getSchema() + getTableName() + " WHERE DOMAIN=" + domain.getId().toString()+ " ORDER BY NAME";
    }

    
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteRequest(AbstractCronJobRequest cronJobRequest) throws SchedulerException {
       String update = "DELETE FROM " + getSchema() + getTableName() + " WHERE ID=?";
       jdbcTemplate.update(update, (Long) cronJobRequest.getId() );
    }
    

    
    protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
    
    
    public void saveRequest(AbstractCronJobRequest cronJobRequest) throws SchedulerException {
        try {
            
        	if (cronJobRequest.getDomainId()==null)
        		cronJobRequest.setDomainId(getDomain().getId());
        	
        	if (cronJobRequest.getId() == null) {
                String insert = "INSERT INTO " + getSchema() + getTableName() +
                        "(ID, NAME, DESCRIPTION, CRONEXPRESSION, CLAZZ, PARAMETER, ISENABLED,LASTMODIFIEDUSER, DOMAIN,TIMEZONE) VALUES " +
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
                cronJobRequest.setId(getNewJobId());
                long userId = (long) ServiceLocator.getService(SecurityService.class).getSessionUser().getId();
                jdbcTemplate.update(insert,
                        (Long) cronJobRequest.getId(),
                        cronJobRequest.getName(),
                        cronJobRequest.getDescription(),
                        cronJobRequest.getCronExpression().getExpression(),
                        cronJobRequest.getClass().getName(),
                        getParameterString(cronJobRequest.getParameters()),
                        cronJobRequest.isEnabled(),
                        userId,
                        (Long) cronJobRequest.getDomainId(),
                        cronJobRequest.getTimeZone()
                );
            } else {
                String update = "UPDATE " + getSchema() + getTableName() +
                        " SET NAME=?, DESCRIPTION=?, CRONEXPRESSION=?, CLAZZ=?, PARAMETER=?, ISENABLED=?,LASTMODIFIEDUSER=?,DOMAIN=?, TIMEZONE=?  " +
                        " WHERE ID=?";

                int count = jdbcTemplate.update(update,
                        cronJobRequest.getName(),
                        cronJobRequest.getDescription(),
                        cronJobRequest.getCronExpression().getExpression(),
                        cronJobRequest.getClass().getName(),
                        getParameterString(cronJobRequest.getParameters()),
                        cronJobRequest.isEnabled(),
                        (long) ServiceLocator.getService(SecurityService.class).getSessionUser().getId(),
                        (Long) cronJobRequest.getId(),
                        (Long) cronJobRequest.getDomainId(),
                        cronJobRequest.getTimeZone()
                        
                );
                if (count == 0) {
                    throw new SchedulerException("No scheduler request was modified.");
                }
            }

        } catch (Exception e) {
            logger.error(e);
            throw (new SchedulerException(e));
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateCronLastExecution(Long id, OffsetDateTime date) throws SchedulerException {
        try {
            String update = "UPDATE " + getSchema() + getTableName() +
                    " SET LASTEXECUTION=?  " +
                    " WHERE ID=?";
            int count = jdbcTemplate.update(update,
                    date,
                    id
                );
            if (count == 0) {
                    throw new SchedulerException("No scheduler request was modified.");
            }
        } catch (Exception e) {
            logger.error(e);
            throw (new SchedulerException(e));
        }
    }


    private long getNewJobId() {
        SqlPlatform sqlplatform = getSqlPlatform();
        String sequencename = "contentid_sequence";
        if (getSchema() != null && !getSchema().equals("")) sequencename = getSchema() + "." + sequencename;
        Long value = (Long) this.jdbcTemplate.query(sqlplatform.nextSequenceQuery(sequencename), new ResultSetExtractor<Long>() {
            public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next())
                    return rs.getLong(1);
                return null;
            }
        });
        return value;
    }

    private void close(Connection connection, PreparedStatement statement, ResultSet resultset) throws SQLException {
        try {
            if (statement != null) statement.close();
            if (resultset != null) resultset.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            logger.error(e);
            throw e;
        }
    }

    private SqlPlatform getSqlPlatform() {

        if (sqlplatform != null)
            return sqlplatform;

        Connection connection = null;
        try {
            connection = this.dataSource.getConnection();
            sqlplatform = SqlPlatformFactory.getPlatformFor(connection.getMetaData());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                	logger.error(e);
                    throw new RuntimeException(e);
                }
            }
        }

        return sqlplatform;
    }


}
