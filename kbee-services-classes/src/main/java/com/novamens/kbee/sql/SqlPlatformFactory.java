package com.novamens.kbee.sql;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kbee.util.NovamensRuntimeException;

/**
 * this factory class is responsible to create Platform objects that define
 * RDBMS platform specific behaviour.
 * 
 * @version 1.0
 * @author Thomas Mahler
 */
public class SqlPlatformFactory {

	private static final HashMap<String, SqlPlatform> platforms = new HashMap<String, SqlPlatform>();
	
	private static Logger logger = LogManager.getLogger(SqlPlatform.class);

	/**
	 * returns the Platform matching to the JdbcConnectionDescriptor jcd. The
	 * method jcd.getDbms(...) is used to determine the Name of the platform.
	 * BRJ : cache platforms
	 * 
	 * @param metaData
	 *	the DatabaseMetaData defining the platform
	 */
	public static SqlPlatform getPlatformFor(DatabaseMetaData metaData) throws SQLException {
		SqlPlatform platform = null;
		String platformName = null;
		
		String dbms = metaData.getDatabaseProductName();
		dbms = dbms.replaceAll(" ", "_");
		
		int majorVersion;
		try {
			majorVersion = metaData.getDatabaseMajorVersion();
		} 
		catch (Throwable ignored) {
			majorVersion = 9; // FIXME: hardCodeado para que cargue el Platform de Oracle 9
		}
		int minorVersion;
		try {
			minorVersion = metaData.getDatabaseMinorVersion();
		} 
		catch (Throwable ignored) {
			minorVersion = 0;
		}
		
		platform = getPlatforms().get(dbms);
		
		if (platform == null) {
			try {
				Class<?> platformClass = getClassnameFor(dbms, majorVersion, minorVersion);
				platform = (SqlPlatform) platformClass.newInstance();
			} 
			catch (Throwable t) {
				logger.warn("[PlatformFactory] problems with platform " + platformName, t);
				logger.warn("[PlatformFactory] OJB will use PlatformDefaultImpl instead");

				platform = new PostgresPlatform();
			}
			getPlatforms().put(dbms, platform);
		}
		
		return platform;
	}

	/**
	 * compute the name and load the concrete Class representing the Platform
	 * specified by <code>platform</code>
	 * 
	 * @param platform
	 *            the name of the platform as specified in the repository
	 */
	private static Class<?> getClassnameFor(String product, int majorVersion, int minorVersion) {
		String platform = "Default"; //$NON-NLS-1$
		
		if (product != null) {
			platform = product;
		}
		
		platform = "com.novamens.kbee.sql." + platform.substring(0, 1).toUpperCase() + platform.substring(1) + "Platform";
		Class<?> ret;

		try {
			ret = Class.forName(platform + majorVersion + '_' + minorVersion);
		} 
		catch (final ClassNotFoundException ignored) {
			try {
				ret = Class.forName(platform + majorVersion);
			} 
			catch (final ClassNotFoundException ignored2) {
				try {
					ret = Class.forName(platform); 
				} 
				catch (final ClassNotFoundException ignored3) {
					try {
						ret = Class.forName(PostgresPlatform.class.getName());
					} 
					catch (final ClassNotFoundException e) {
						throw new NovamensRuntimeException(e);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Gets the platforms.
	 * 
	 * @return Returns a HashMap
	 */
	private static HashMap<String, SqlPlatform> getPlatforms() {
		return platforms;
	}
}
