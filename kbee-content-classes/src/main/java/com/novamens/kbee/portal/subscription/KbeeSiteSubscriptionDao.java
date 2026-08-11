package com.novamens.kbee.portal.subscription;


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
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.content.dao.SubscriptionDao;
import com.novamens.portal.subscription.SiteSubscriptionEvent;
import com.novamens.portal6.model.Site;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

/**
 * 
 * 
 */
public class KbeeSiteSubscriptionDao implements  com.novamens.portal.subscription.SubscriptionDao {

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SubscriptionDao.class.getName());
	
	static public int TYPE_EMAIL = 1; // por el momento las notificaciones que se envian son solo por email

	private SessionFactory sessionFactory;

	private String schema;

	private final String SUBSCRIPTION_TABLE = "po_site_subscription";

	@SuppressWarnings("unused")
	private SchedulerService service;
	private DataSource dataSource;

	public KbeeSiteSubscriptionDao() {
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

	
	public int getTotalSubscribers(Long oId, SiteSubscriptionEvent event) {
		String sql = "Select count(*) from " + getSchema() + SUBSCRIPTION_TABLE + " S where S.site_oid=" + oId.toString() + " AND S.event_id=" + String.valueOf(event.getId());
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			connection = getConnection(getSessionFactory().getCurrentSession());
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			while (resultSet.next())
				return resultSet.getInt(1);
			return 0;
		} catch (SQLException e) {
			logger.error(e);
			return 0;
		} finally {
			try {
				close(null, statement, resultSet);
			} catch (IOException e) {
				logger.error(e);

			}
		}
	}

	/**
	 * <p>
	 * returns the list of subscribers to the SuscriptionEvent to that Site
	 * </p>
	 */
	public List<UserProfile> getSubscribers(Serializable site_oid, SiteSubscriptionEvent event) throws IOException {
		String sql = "Select user_id from " + getSchema() + SUBSCRIPTION_TABLE + " S where S.site_oid="+ site_oid.toString() + " AND S.event_id='" + String.valueOf(event.getId());
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			connection = getConnection(getSessionFactory().getCurrentSession());
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			List<UserProfile> list = new ArrayList<UserProfile>();
			ContentDao contentdao = getContentDao();
			while (resultSet.next()) {
				long uid = resultSet.getLong(1);
				UserProfile userProfile = contentdao.findUserProfileByUserId(uid);
				list.add(userProfile);
			}
			return list;
		} catch (SQLException e) {
			throw new IOException(e);
		} finally {
			close(null, statement, resultSet);
		}
	}

	/**
	 * 
	 * @param userProfile
	 * @param content
	 * @throws IOException
	 */

	public void unSubscribeContent(UserProfile userProfile, Site site) throws IOException {
		String statement = "Delete from " + getSchema() + SUBSCRIPTION_TABLE + " S where S.user_id=" + userProfile.getUser().getId().toString() + " AND S.site_oid=" + site.getOId().toString();
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
	public boolean isSubscribedUser(UserProfile userProfile, Site site, SiteSubscriptionEvent event) throws IOException {
		String sql = "Select * from " + getSchema() + SUBSCRIPTION_TABLE + " S where S.user_id=" + userProfile.getUser().getId().toString() + " AND S.site_oid=" + site.getOId().toString() + " AND S.event_id=" + String.valueOf(event.getId());
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
		} catch (SQLException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		} finally {
			close(null, statement, resultSet);
		}
	}

	/**
	 * @param userProfile
	 * @param content
	 * @param event
	 * @throws IOException
	 */
	public void unSubscribe(UserProfile userProfile, Site site, SiteSubscriptionEvent event) throws IOException {
		String statement = "Delete from " + getSchema() + SUBSCRIPTION_TABLE + " S where S.user_id='"
				+ userProfile.getUser().getId().toString() + "' AND S.site_oid='" + site.getOId().toString()
				+ "' AND S.event_id='" + String.valueOf(event.getId()) + "'";
		executeSQL(statement);

	}

	/**
	 * 
	 * @param userProfile
	 * @param content
	 * @param event
	 * @throws IOException
	 */
	public void subscribe(UserProfile userProfile, Site site, SiteSubscriptionEvent event) throws IOException {
		
		String st = "Insert into " + getSchema() + SUBSCRIPTION_TABLE
				+ " (user_id, site_oid, event_id, type_id) values ";
		String sql = st + "(" + userProfile.getUser().getId().toString() + ", " + String.valueOf(site.getOId()) + ", "
				+ String.valueOf(event.getId()) + ", " + String.valueOf(TYPE_EMAIL) + ")";
		executeSQL(sql);
	}

	public void unSubscribeAll(UserProfile userProfile) throws IOException {
		String statement = "Delete from " + getSchema() + SUBSCRIPTION_TABLE + " S where S.user_id=" + userProfile.getUser().getId().toString();
		executeSQL(statement);
	}

	public void close() {
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	public void setSchema(String schema) {
		this.schema = schema != null ? schema.trim() : null;
	}

	public String getSchema() {
		if (schema != null && schema.length() > 0)
			return schema + ".";
		return "";
	}

	private void close(Connection connection, PreparedStatement statement, ResultSet resultset) throws IOException {
		try {
			if (statement != null)
				statement.close();
			if (resultset != null)
				resultset.close();
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	private Connection getConnection(Session session) {
		return ((SessionImplementor) session).connection();
	}

	private void executeSQL(String sql) throws IOException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = getConnection(getSessionFactory().getCurrentSession());
			statement = connection.prepareStatement(sql);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.error(e);
			throw new IOException(e);
		} finally {
			close(null, statement, null);
		}
	}

	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	/**
	private User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}
	}**/

}
