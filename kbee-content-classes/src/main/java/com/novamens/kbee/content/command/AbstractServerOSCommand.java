package com.novamens.kbee.content.command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import kbee.util.PropertiesFactory;

public abstract class AbstractServerOSCommand extends AbstractCommand {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractServerOSCommand.class.getName());

	private String username = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.username", "kbee").trim();;
	private String password = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.password", "novamens").trim();;

	public String getUserName() {
		return username;
	}

	public void setUserName(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDBDriver() {
		return dbdriver;
	}

	public void setDBDriver(String dbdriver) {
		this.dbdriver = dbdriver;
	}

	public String getHostName() {
		return hostname;
	}

	public void setHostName(String hostname) {
		this.hostname = hostname;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getPWD() {
		return PWD;
	}

	public void setPWD(String pWD) {
		PWD = pWD;
	}

	public String getUSERDIR() {
		return USER_DIR;
	}

	public void setUSERDIR(String uSER_DIR) {
		USER_DIR = uSER_DIR;
	}

	public String getUSERHOME() {
		return USER_HOME;
	}

	public void setUSERHOME(String uSER_HOME) {
		USER_HOME = uSER_HOME;
	}


	private String dbdriver;
	private String hostname;
	private String port;
	private String database;

	
	private String PWD;
	private String USER_DIR;
	private String USER_HOME;

	public AbstractServerOSCommand() {

		this.PWD = System.getenv("PWD");
		this.USER_DIR = System.getProperty("user.dir");
		this.USER_HOME = System.getProperty("user.home");
		
		logger.debug("PWD: " + this.PWD);
		logger.debug("user.dir: " + System.getProperty("user.dir"));
		
		if (USER_DIR==null)
			USER_DIR="./";

		if (USER_HOME==null)
			USER_HOME="./";
		
		initOSC();
	}

	protected boolean existsFile(String path) {
		File f = new File(path);
		if (f!=null && f.exists() && !f.isDirectory())
			return true;
		return false;
	}
	
	/**
	 * 
	 * @param path full path
	 * @param list 
	 */
	protected void makeFile(String path, List<String> list) throws IOException {
		
		File file = new File(path);
		BufferedWriter out = null;
		
		try {
			logger.debug("making " + path);
			out = new BufferedWriter(new FileWriter(file));
			for (String s:list) {
				out.write(s+" \n");
				logger.debug(s);
			}
			out.write(" \n");
			out.write(" \n");
			out.write("# --- \n");
			out.write("# Created: " + OffsetDateTime.now().toString() + " \n");
			
		} catch (IOException e) {
			logger.error(e.getClass().getName() + " | " + e.getMessage());
			throw(e);
		} finally {
			if (out!=null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e.getClass().getName() + " | " + e.getMessage());
					throw(e);
				}
			}
		}

		try {
			if (isLinux()) {
				if (isExecutable(file)) {
					logger.debug("chmod +x "+path);
					Process proc = Runtime.getRuntime().exec("chmod +x "+path);
					proc.waitFor();
					logger.debug("After chmod");
				}
			}
			
		} catch (Exception e) {
			logger.error(e);
			throw(new IOException(e));
		}
	}
	
	/**
	 * @throws IOException
	 * 
	 *  psql  -U postgres -h rcpidcdbdoc001 idoc-windsor5
	 *  
	 *  pg_dump -U postgres -w -h  rcpidcdbdoc001  idoc-basic | gzip  > idoc-basic.gz
	 *  
	 *  
	 */
	protected void checkPgPass() throws IOException {
								
		String pgpth=USER_HOME + File.separator +".pgpass";
		
		if (existsFile(pgpth)) 
			return;
		
		List<String> kj = new ArrayList<String>();
		
		kj.add(hostname+":"+port+":"+database+":"+username+":"+password);
		
		logger.debug("Creating " + pgpth);
		
		makeFile(USER_HOME+ File.separator +".pgpass", kj);
		try {
			if (isLinux()) {
				Process proc = Runtime.getRuntime().exec("chmod 600 "+ USER_HOME+ File.separator +".pgpass");
				proc.waitFor();
			}

		} catch (InterruptedException e) {
			logger.error(e);
		}
	}
	
	protected boolean isLinux() {
		if  (System.getenv("OS")!=null && System.getenv("OS").toLowerCase().contains("windows")) 
			return false;
		return true;
	}
	
	protected boolean isExecutable(File f) {
		if (!f.exists())
			return false;
		if (f.isDirectory())
			return false;
		String name = f.getName();
		return name.toLowerCase().matches("^.*\\.(cmd|bat|sh|deb|exe)$");
	}

	
	/**
	 * sets variables
	 */
	private void initOSC() {
		
		logger.debug("init " + this.getClass().getName());
		
		String jdbc = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", "jdbc:postgresql://localhost/idoc").trim();
		String arr [] = jdbc.split("://");
		
		if (arr.length>0) {
			dbdriver = arr[0];
		}
		
		if (arr.length>1) {
			String xrr []=arr[1].split("/");
			if (xrr.length>1) {
				String hp [] = xrr[0].split(":");
				hostname = hp[0]; 
				if (hp.length>1)
					port 	 = hp[1];
				else
					port 	 = "5432";
				database = xrr[1];
			}
			else {
				logger.error("incomplete data");
				return;
			}
		}
		logger.debug(hostname + ":" + port + "/" + database);
	}

	
}
