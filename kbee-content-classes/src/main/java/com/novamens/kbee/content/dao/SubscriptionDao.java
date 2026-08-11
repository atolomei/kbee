package com.novamens.kbee.content.dao;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionImplementor;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.subscription.SubscriptionEvent;
import com.novamens.content.user.UserProfile;
import com.novamens.dao.Dao;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;


/**
 *  <p>Servicio de Suscripciones a Contenidos y otros objetos de información (Sitio).
	Este servicio se utiliza para los casos donde un usuario se suscribe explicitamente a un Objeto.
	Para las notificaciones a los usuarios mediante reglas se debe EnotiRule y NotificationService
	</p>
 *  <p>
 *  El servicio solamente administra la subscripción / de-subscripción.
 *  Las notificaciones ante eventos deben ser realizadas por el event handler correspondiente.
 *  </p>
 */
public class SubscriptionDao implements Dao {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SubscriptionDao.class.getName());

	static private final String SUBSCRIPTION_TABLE 	= "kb_subscription";
	
	static public int TYPE_EMAIL = 1;   // por el momento las notificaciones 
										// que se envian son sólo por email	
	
	private SessionFactory sessionFactory;

	@SuppressWarnings("unused")
	private SchedulerService service;
	private DataSource dataSource;
	private String schema;
	
	
	public SubscriptionDao() {
	}
	
	public void setService(SchedulerService service) {
		this.service = service;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public DataSource getDataSource() {
		return this.dataSource;
	}
	
	/**
	 * <p> returns the list of subscribers to the SuscriptionEvent for that Content</p> 
	 */
	public List<UserProfile> getSubscribers(Serializable content_oid, SubscriptionEvent event) throws IOException {
		String sql = "Select user_id from " + getSchema() + SubscriptionDao.SUBSCRIPTION_TABLE + " S where S.content_oid="+ content_oid.toString() +" AND S.event_id="+ String.valueOf(event.getId());
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			connection = getConnection(getSessionFactory().getCurrentSession());
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			List<UserProfile> list = new ArrayList<UserProfile>();
			ContentDao dao = getContentDao();
			while (resultSet.next()) { 
				long uid = resultSet.getLong(1);
				UserProfile userProfile =  dao.findUserProfileByUserId(uid);
				list.add(userProfile);
			}
			return list;
		}
		catch (SQLException e) {
			logger.error(e);
			throw new IOException(e);
		}
		finally {
			close(null, statement, resultSet);
		}
	}
	
	
	
	/**
	 * 
	 * @param userProfile
	 * @param content
	 * @throws IOException
	 */
											
	public void unSubscribeContent(UserProfile userProfile, Content content) throws IOException {
		String statement = "Delete from " + getSchema() + SUBSCRIPTION_TABLE + " S where S.user_id=" + userProfile.getUser().getId().toString()+" AND S.content_oid="+ content.getOId().toString();
		
	 
		
		executeSQL(statement);
	}
	/**
	 * 
	 * @param user
	 * @param content
	 * @param event
	 * @return
	 * @throws IOException
	 */									
	public boolean isSubscribedUser(UserProfile userProfile, Content content, SubscriptionEvent event) throws IOException {
		String sql = "Select * from " + getSchema() + SUBSCRIPTION_TABLE + " S where S.user_id=" + userProfile.getUser().getId().toString()+" AND S.content_oid="+ content.getOId().toString()+" AND S.event_id="+ String.valueOf(event.getId());

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			connection = getConnection(getSessionFactory().getCurrentSession());
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			if (resultSet.next()) 
				return true;
			return false;
		}
		catch (SQLException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new IOException(e);
		}
		finally {
			close(null, statement, resultSet);
		}
	}
		
	/**
	 * @param userProfile
	 * @param content
	 * @param event
	 * @throws IOException
	 */
	public void unSubscribe(UserProfile userProfile, Content content, SubscriptionEvent event) throws IOException {
		String statement = "Delete from " + getSchema() + SUBSCRIPTION_TABLE + " S where S.user_id=" + userProfile.getUser().getId().toString()+" AND S.content_oid="+ content.getOId().toString()+" AND S.event_id="+ String.valueOf(event.getId());
		executeSQL(statement);
	}
	
	/**
	 * 
	 * @param userProfile
	 * @param content
	 * @param event
	 * @throws IOException
	 */
	public void subscribe(UserProfile userProfile, Content content, SubscriptionEvent event) throws IOException {
		String st = "Insert into " + getSchema() + SUBSCRIPTION_TABLE + " (user_id, content_oid, event_id, type_id) values ";
		String sql = st + "("+ userProfile.getUser().getId().toString() + ", " + content.getOId().toString() + ", " + String.valueOf(event.getId()) + ", " + String.valueOf(TYPE_EMAIL) + ")";
		executeSQL(sql);
	}
	
	public void unSubscribeAll(UserProfile userProfile) throws IOException {
		String statement = "Delete from " + getSchema() + SUBSCRIPTION_TABLE + " S where S.user_id=" + userProfile.getUser().getId().toString();
		executeSQL(statement);
	}

	public void close() {
	}


	public void setSchema(String schema) {
		this.schema=schema;
	}

	public String getSchema() {
		if (schema!=null && schema.length()>0)
			return schema+".";
		return "";
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
	private void close(Connection connection, PreparedStatement statement, ResultSet resultset) throws IOException {
		try {
			if (statement != null) statement.close();
			if (resultset != null) resultset.close();
			if (connection != null) connection.close();
		} 
		catch (SQLException e) {
			logger.error(e);
			throw new IOException(e);
		}
	}
	
	private Connection getConnection(Session session) {
		return ((SessionImplementor)session).connection();
	}

	private void executeSQL(String sql) throws IOException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = getConnection(getSessionFactory().getCurrentSession());
			statement = connection.prepareStatement(sql);
			statement.executeUpdate();
		}
		catch (SQLException e) {
			logger.error(e);
			throw new IOException(e);
		}
		finally {
			close(null, statement, null);
		}
	}
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
