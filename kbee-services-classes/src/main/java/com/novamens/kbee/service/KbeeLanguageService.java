package com.novamens.kbee.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;
import org.hibernate.SessionFactory;

import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.service.LanguageService;
import com.novamens.util.KbeeRuntimeException;

/**
 * 
	create table kb_language_string ( id bigint primary key, key character varying(256) not null, locale character varying(128) not null, value character varying(1024));
	alter table kb_language_string  add constraint localeKey unique (locale, key);
	
	insert into kb_language_string(id, locale, key, value) values (   , 'en', 'xxx', 'xxx');
*/

public class KbeeLanguageService implements LanguageService, EventListener {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeLanguageService.class.getName());
									
	Map<Locale, Map<String, String>> _map = null;

	private List <String> stopwords_en;
	private List <String> stopwords_es;

	private DataSource dataSource;
	private SessionFactory sessionFactory;
	private String schema;
	private String tableName = "KB_LANGUAGE_STRING";
	

	public KbeeLanguageService() {
	}
	

	public void setTableName(String name) {
		this.tableName = name;
	}
	
	public String getTableName() {
		return this.tableName;
	}
	
	public String getSchema() {
		if(this.schema!=null && !"".equals(this.schema.trim()))
			return this.schema.trim()+".";
		return "";
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	
	private synchronized void fillData() {
	
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			connection = this.getDataSource().getConnection();
			String getstatement =  getStatement();
			statement = connection.prepareStatement(getstatement);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
					String localeStr 	= resultSet.getString("locale");
					String key 			= resultSet.getString("key");
					String value	 	= resultSet.getString("value");
					Locale locale = Locale.forLanguageTag(localeStr);
					if (locale!=null) {
						if (_map==null)
							_map=new ConcurrentHashMap<Locale, Map<String, String>>();
						if (!_map.containsKey(locale))
							_map.put(locale, new HashMap<String, String>());
						_map.get(locale).put(key.toLowerCase().trim(), value.trim());

					}
				}	
		
			if (!_map.containsKey(Locale.ENGLISH))
				_map.put(Locale.ENGLISH, new HashMap<String, String>());
			
		}
		catch (SQLException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
		catch (RuntimeException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
		finally {
			try {
				close(connection, statement, resultSet);
				
				
			} catch (IOException e) {
				logger.error(e);
			}
		}
		
		
	}
	

	@Override
	public String getString(String key) {
		return getString(key, Locale.ENGLISH);
	}

	@Override
	public String getString(String key, Locale locale) {
			return getString(key, locale, null);
	}
	
	@Override
	public String getString(String key, Locale locale, String defaultValue) {
		
		if (key==null || locale==null)
			throw new IllegalArgumentException("key or locale is null");
		
		if (!getMap().containsKey(locale)) 
			getMap().put(locale,  new HashMap<String, String>());
		
		Map<String, String> lo= getMap().get(locale);
		
		if (lo.containsKey(key))
			return lo.get(key);
		
		if (!locale.equals(Locale.ENGLISH))
			if (getMap().get(Locale.ENGLISH).containsKey(key))
				return getMap().get(Locale.ENGLISH).get(key);
		
		return defaultValue!=null? defaultValue: key;
		
	}

	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
			if (event instanceof EvictCacheServiceEvent) {
				_map = null;
			}
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
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


	private void close(Connection connection, PreparedStatement statement, ResultSet resultset) throws IOException {
		try {
			
			if (statement 	!= null) statement.close();
			if (resultset 	!= null) resultset.close();
			if (connection 	!= null) connection.close();
		} 
		catch (SQLException e) {
			logger.error(e);
			throw new IOException(e);
		}
	}
	
	private Map<Locale, Map<String, String>> getMap() {
		
		if (_map!=null)
			return _map;
		
		synchronized (this) {
			fillData();
			return _map;
		}
	}
	
	private String getStatement() {
		return "SELECT ID, LOCALE, KEY, VALUE FROM " + getSchema()+getTableName()+ " ORDER BY LOCALE, KEY"; 
	}

	
	private void loadStopWords() {
		try {

			stopwords_es = Files.readAllLines(Paths.get(getClass().getResource("stopwords_es.txt").getPath()));
			stopwords_en = Files.readAllLines(Paths.get(getClass().getResource("stopwords_en.txt").getPath()));
				
		} catch (Exception e) {
			stopwords_es = new ArrayList<String>();
			stopwords_en = new ArrayList<String>();		
			logger.error(e);
		}
	}

	
	/**
	 * 
	 * 
	 */
	public String removeStopWords(String original, Locale locale) {

		ArrayList<String> allWords = Stream.of(original.toLowerCase().split(" "))
	    		                             .collect(Collectors.toCollection(ArrayList<String>::new));

	    if (stopwords_en==null || stopwords_es==null)
	    	loadStopWords();
	    
	    if (locale.equals(Locale.forLanguageTag("es")))
	    	allWords.removeAll(stopwords_es);
	    else
	    	allWords.removeAll(stopwords_en);
	    
	    return allWords.stream().collect(Collectors.joining(" "));
	}
	
	



}
