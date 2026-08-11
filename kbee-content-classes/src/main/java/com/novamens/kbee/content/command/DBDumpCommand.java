package com.novamens.kbee.content.command;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.command.CommandParameter;
import com.novamens.content.command.CommandParameterType;
import com.novamens.content.command.CommandState;

import kbee.util.PropertiesFactory;

/**
 * check if .pgpass exists, if not create it
 * 
 * database dump is saved inside WORKDIR 
 */
public class DBDumpCommand extends AbstractCommand implements DBToolCommand {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DBDumpCommand.class.getName());

	private String dbdriver;
	private String hostname;
	private String port;
	private String database;
								
	private String username = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.username", "kbee").trim();;
	private String password = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.password", "novamens").trim();;
										
	private String PWD;
	private String USER_DIR;
	private String USER_HOME;
	
	private DateTimeFormatter workdf = DateTimeFormatter.ofPattern("YYYYMMddhhmm");
	
	
	public DBDumpCommand() {
		setName("DB dump Command");
		setExactlyOneSemantics(true);
	}
	
	@Override
	public void execute() {
		
		logger.debug("Starting Command execution " + getName());

		
		setDateStarted(OffsetDateTime.now());
		setProgress(0);

		// sets hostname, database, port
		//
		initDBD();

		if  (System.getenv("OS")!=null && System.getenv("OS").toLowerCase().contains("windows")) {
			logger.error("This command doesnt work on Windows");
			setState(CommandState.ERROR);
			setResult("This command doesnt work on Windows");
			setDateTerminated(OffsetDateTime.now());
			 return;
		}
	
		
		if (this.dbdriver==null || (!this.dbdriver.trim().toLowerCase().equals("jdbc:postgresql"))) {
			logger.error("This command requires PostgreSQL");
			setState(CommandState.ERROR);
			setResult("This command requires PostgreSQL");
			setDateTerminated(OffsetDateTime.now());
			 return;
		}
		
		this.PWD = System.getenv("PWD");
		this.USER_DIR = System.getProperty("user.dir");
		this.USER_HOME = System.getProperty("user.home");
		
	
		logger.debug("PWD: " + this.PWD);
		logger.debug("user.dir: " + System.getProperty("user.dir"));
		
		if (USER_DIR==null)
			USER_DIR="./";

		if (USER_HOME==null)
			USER_HOME="./";

		try {
			
			checkPgPass();
			
			String export2= getWorkDir() + File.separator + database.toLowerCase()+ "-" + workdf.format(OffsetDateTime.now()) + ".pgsql";
			String cmd2 ="pg_dump -U postgres -w -h "+ hostname + " -f " + export2 + " " + database;
			logger.debug(cmd2);
			Process proc2 = Runtime.getRuntime().exec(cmd2);
			int code = proc2.waitFor();
			logger.debug("end executing the script pg_dump. code: " + String.valueOf(code));
			
			String export_tar= getWorkDir() + File.separator + database.toLowerCase()+ "-" + workdf.format(OffsetDateTime.now()) + ".tar.gz";
			String cmd3 ="tar cvzf " + export_tar + "  " + export2;
			Process proc3 = Runtime.getRuntime().exec(cmd3);
			code = proc3.waitFor();
			logger.debug("end executing tar. code: " + String.valueOf(code));
			
			setProgress(100.0);
			setState(CommandState.COMPLETED);
			setResult("ok");
			setResultComments(cmd2+ " | " + cmd3);
			
		} catch (Exception e) {
			setState(CommandState.ERROR);
			setResult(e.getClass().getName());
			setResultComments(e.getMessage());
			logger.error(e);
		}
		finally {
			setDateTerminated(OffsetDateTime.now());
		}
	}
	
	/**
	 * sets variables
	 */
	private void initDBD() {
		
		logger.debug("init");
		
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

	
	private boolean existsFile(String path) {
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
	private void makeFile(String path, List<String> list) throws IOException {
		
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
				logger.debug("chmod +x "+path);
				Process proc = Runtime.getRuntime().exec("chmod +x "+path);
				proc.waitFor();
				logger.debug("After chmod");
			}
			
		} catch (Exception e) {
			logger.error(e.getClass().getName() + " | " + e.getMessage() + Thread.currentThread().getStackTrace()[1].getMethodName());
			throw(new IOException(e));
		}
	}
	
	/**
	 * @throws IOException
	 * 
	 *  psql  -U postgres -h rcpidcdbdoc001 idoc-windsor5
	 *  
	 *  pg_dump -U postgres -w -h  rcpidcdbdoc001  idoc-basic | gzip  > idoc-basic.g
	 *  
	 *  
	 */
	private void checkPgPass() throws IOException {
								
		String pgpth=USER_HOME + File.separator +".pgpass";
		
		if (existsFile(pgpth)) 
			return;
		
		List<String> kj = new ArrayList<String>();
		
		kj.add(hostname+":"+port+":"+database+":"+username+":"+password);
		
		logger.debug("Creating " + pgpth);
		
		makeFile(USER_HOME+ File.separator +".pgpass", kj);
		try {
			Process proc = Runtime.getRuntime().exec("chmod 600 "+ USER_HOME+ File.separator +".pgpass");
			proc.waitFor();

		} catch (InterruptedException e) {
			logger.error(e);
		}
	}
	
	private boolean isLinux() {
		if  (System.getenv("OS")!=null && System.getenv("OS").toLowerCase().contains("windows")) 
			return false;
		return true;
	}
	@Override
	public List<CommandParameter> getParametersDefinition() {
		List<CommandParameter> commandParameterList=new ArrayList<CommandParameter>();
		//commandParameterList.add(new CommandParameter("dump1", "dump1 name", false, CommandParameterType.STRING));
		//commandParameterList.add(new CommandParameter("dump2", "dump2 name", false, CommandParameterType.STRING));
		return commandParameterList;
	}				
}
