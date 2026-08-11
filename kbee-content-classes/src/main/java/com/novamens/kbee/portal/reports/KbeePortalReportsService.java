package com.novamens.kbee.portal.reports;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionImplementor;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.kbee.sql.OraclePlatform;
import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlPlatformFactory;
import com.novamens.portal.reports.DVisit;
import com.novamens.portal.reports.UVisit;
import com.novamens.portal.service.PortalReportsService;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

public class KbeePortalReportsService implements PortalReportsService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalReportsService.class.getName());

	static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
	// static DateConverter converter = new PatternDateConverter("dd MMM yyyy hh:mm:ss z",false);

	@SuppressWarnings("unused")
	static private final String TABLE = "po_sitelogin";

	private SessionFactory sessionFactory;

	private DataSource dataSource;
	private String schema;

	public KbeePortalReportsService() {
	}

	

	@Override
	public List<DVisit> getSiteVisits(Site site, Date from, Date to) {
		return null;
	}

	@Override
	public List<DVisit> getSiteUniqueVisitors(Site site, Date from, Date to) {

		List<DVisit> list = new ArrayList<DVisit>();

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {

			connection = this.getDataSource().getConnection();
			String getstatement = this.getSiteUniqueVisitorsStatement(site, from, to, getSqlPlatform(connection));
			logger.debug(getstatement);
			statement = connection.prepareStatement(getstatement);
			resultSet = statement.executeQuery();
			int i = 0;

			// Max 12 years
			//
			final int max=366*12;
			while (resultSet.next() && i < max) {
				DVisit dvisit = new DVisit(resultSet.getInt("day"), resultSet.getInt("month"), resultSet.getInt("year"), resultSet.getInt("users"));
				list.add(dvisit);
				i++;
			}

			if (logger.isDebugEnabled()) {
				for (DVisit visit : list) {
					logger.debug(df.format(visit.getDate()) + " :  " + String.valueOf(visit.getUniqueVisitors()));
				}
			}

			return list;

		} catch (SQLException e) {
			logger.error(e);
			return list;
		} finally {
			try {
				if (statement != null)
					statement.close();
				if (resultSet != null)
					resultSet.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				logger.error(e);
				return list;
			}
		}
	}

	@Override
	public List<DVisit> getPageUniqueVisitors(Page page, Date from, Date to) {
		return null;
	}

	@Override
	public List<DVisit> getContentUniqueVisitors(Content content, Date from, Date to) {
		return null;
	}

	@Override
	public List<DVisit> getTotalUniqueVisitors(Date from, Date to) {
		return null;
	}

	@Override
	public List<UVisit> getContentDetailedVisitors(Content content, Date from, Date to) {
		return null;
	}

	@Override
	public List<UVisit> getPageDetailedVisitors(Page page, Date from, Date to) {
		return null;
	}

	@Override
	public List<UVisit> getSiteVisitors(Content content, Date from, Date to) {
		return null;
	}


	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	
	public DataSource getDataSource() {
		return this.dataSource;
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getSchema() {
		if (schema != null && schema.length() > 0)
			return schema + ".";
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
			if (statement != null)
				statement.close();
			if (resultset != null)
				resultset.close();
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			logger.error(e);
			throw new IOException(e);
		}
	}

	private Connection getConnection(Session session) {
		return ((SessionImplementor) session).connection();
	}

	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	private SqlPlatform getSqlPlatform(Connection connection) {
		try {
			return SqlPlatformFactory.getPlatformFor(connection.getMetaData());
		} catch (SQLException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}

	/**
	 * @param site
	 * @param from
	 * @param to
	 * @param platform
	 * @return
	 */
	private String getSiteUniqueVisitorsStatement(Site site, Date from, Date to, SqlPlatform platform) {

		StringBuffer stm = new StringBuffer();

		Calendar cal_from = Calendar.getInstance();
		cal_from.setTime(from);

		String from_year = String.valueOf(cal_from.get(Calendar.YEAR));
		String from_month = String.valueOf(cal_from.get(Calendar.MONTH));
		String from_day = String.valueOf(cal_from.get(Calendar.DAY_OF_MONTH));

		Calendar cal_to = Calendar.getInstance();
		cal_to.setTime(to);
		String to_year = String.valueOf(cal_to.get(Calendar.YEAR));
		String to_month = String.valueOf(cal_to.get(Calendar.MONTH));
		String to_day = String.valueOf(cal_to.get(Calendar.DAY_OF_MONTH));

		if (platform instanceof OraclePlatform) {

			stm.append("select Day as Day, Month as Month, Year as Year, count(*) as Users, "
					+ "from (select distinct Day, Month, Year, user_id from "
					+ "(select extract(Day from visit_time) as Day, extract(Month from visit_time) as Month, extract(Year from visit_time) as Year, "
					+ "user_id from po_sitelogin where site_id=" + site.getId().toString()
					+ " and extract(YEAR from visit_time)=" + from_year + " and extract(MONTH from visit_time)>="
					+ from_month + " and extract(DAY from visit_time)>=" + from_day
					+ " and extract(YEAR from visit_time)<=" + to_year + " and extract(MONTH from visit_time)<="
					+ to_month + " and extract(DAY from visit_time)<=" + to_day + ") "
					+ "order by Month, Day) group by Year, Month, Day order by Year, Month, Day");
		} else {

		}
		return stm.toString();
	}

	@SuppressWarnings("unused")
	private String getSiteVisitsStatement(Site site, Date from, Date to, SqlPlatform platform) {

		StringBuffer stm = new StringBuffer();
		DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
		String fromstr = df.format(from);
		String tostr = df.format(to);

		if (platform instanceof OraclePlatform) {
			stm.append(
					"select to_char(cast(visit_time as date),'YYYY-MM-DD') as vdate, count(*) as visits from po_sitelogin where ");
			stm.append(" to_char(cast(visit_time as date),'YYYY-MM-DD')>= '");
			stm.append(fromstr);
			stm.append("' and to_char(cast(visit_time as date),'YYYY-MM-DD')<='");
			stm.append(tostr);
			stm.append("'");
			stm.append(" and site_id=");
			stm.append(site.getId().toString());
			stm.append(
					" group by to_char(cast(visit_time as date),'YYYY-MM-DD') order by to_char(cast(visit_time as date),'YYYY-MM-DD')");
		} else {
			stm.append("select date(visit_time) as vdate, count(*) as visits from po_sitelogin where ");
			stm.append(" visit_time> '");
			stm.append(fromstr);
			stm.append("' and date(visit_time)<='");
			stm.append(tostr);
			stm.append("'");
			stm.append(" and site_id=");
			stm.append(site.getId().toString());
			stm.append(" group by date(visit_time) order by date(visit_time)");
		}
		return stm.toString();
	}

}
