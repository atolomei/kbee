package com.novamens.kbee.content.command;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.command.CommandState;

/**
 * 1. Create .sql file
 * 2. Exect psql
 * 3. return Path to file ?
 * 
 *
 *  - DBDump
 *  - QueryDump
 *
 */
//  \copy "(Select * From domain) To '/tmp/test.csv' With CSV"


public class DBQueryDumpCommand extends  AbstractServerOSCommand {

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DBQueryDumpCommand.class.getName());

	private String _query 			= "Select * from Domain";
	private String _exportFileName 	= "domain";
	
	
	private DateTimeFormatter workdf = DateTimeFormatter.ofPattern("YYYYMMddhhmm");

	private static final String description = "<p>Saves a csv file from a SQL query.</p><h4>Parameters</h4><p>query, file (optional)</p><h4>Example</h4><p>query = select * from domain<br/>file = domain-list</p>";
	
	public DBQueryDumpCommand() {
		setName("DB Query dumnp Command");
		
		setExactlyOneSemantics(true);
	}
	
	@Override
	public String getDescription () {
		return "<p>Saves a csv file from a SQL query.</p><h4>Parameters</h4><p>query, file (optional)</p><h4>Example</h4><p>query = select * from domain<br/>file = domain-list</p>";
	}
	public String getQuery() {
		return _query;
	}

	public void setQuery(String query) {
		this._query = query;
	}

	public String getExportFileName() {
		return _exportFileName;
	}

	public void setExportFileName(String exportFileName) {
		this._exportFileName = exportFileName;
	}

	
	@Override
	public void execute() {
		
		try {
		
			logger.debug("Starting Command execution " + getName());
	
			String sq = (String) getStringParameter("query", null);
			
			if (sq==null)
				throw (new IllegalArgumentException("query is null"));
			
			setQuery(sq);
			setExportFileName( (String) getStringParameter("file", this.getClass().getSimpleName().toLowerCase()));
			
			setDateStarted(OffsetDateTime.now());
			setProgress(0);
	
	
			/**
			if  (System.getenv("OS")!=null && System.getenv("OS").toLowerCase().contains("windows")) {
				// .psql of copy to csv
				String csv = getWorkDir() + File.separator + getDatabase().toLowerCase()+ "-" + getExportFileName() + "-" + workdf.format(OffsetDateTime.now()) + ".csv";
				String sqlScriptName = getWorkDir() + File.separator + getDatabase().toLowerCase()+ "-" + getExportFileName() + "-" + workdf.format(OffsetDateTime.now()) + ".pgsql";
				StringBuilder str = new StringBuilder();
				str.append("\\copy (" + getQuery() + ") To '"+ csv + "' With CSV");
				List<String> list = new ArrayList<String>();
				list.add(str.toString());
				try {
					makeFile(sqlScriptName, list);
				} catch (Exception e) {
					logger.error(e);
				}
				
				// command psql
				String psql_command ="psql -U postgres -w -h "+ getHostName() + " -f " + sqlScriptName + " " + getDatabase();
				logger.debug(psql_command);
	
				// command tar
				String export_tar= getWorkDir() + File.separator + getDatabase().toLowerCase()+ "-" + getExportFileName() + "-" + workdf.format(OffsetDateTime.now()) + ".tar.gz";
				String tar_command ="tar cvzf " + export_tar + "  " + csv;
				
				if (isLinux())
					logger.debug(tar_command);
				
			}*/
		
			
			if (getDBDriver()==null || (!getDBDriver().trim().toLowerCase().equals("jdbc:postgresql"))) {
				logger.error("This command requires PostgreSQL");
				setState(CommandState.ERROR);
				setResult("This command requires PostgreSQL");
				setDateTerminated(OffsetDateTime.now());
				 return;
			}
			
	
			try {
				
				checkPgPass();
				
				// .sql with Copy to csv inside
				//
				//
				String csv = getWorkDir() + File.separator + getDatabase().toLowerCase()+ "-" + getExportFileName() + "-" + workdf.format(OffsetDateTime.now()) + ".csv";
				String sqlScriptName = getWorkDir() + File.separator + getDatabase().toLowerCase()+ "-" + getExportFileName() + "-" + workdf.format(OffsetDateTime.now()) + ".pgsql";
				StringBuilder str = new StringBuilder();
				str.append("\\copy (" + getQuery().trim()  + ") To '"+ csv + "'  CSV HEADER");
				List<String> list = new ArrayList<String>();
				list.add(str.toString());
				makeFile(sqlScriptName, list);
				
				
				// now .psql command to execute
				//
				//
				String psql_command ="psql -U postgres -w -h "+ getHostName() + " -f " + sqlScriptName + " " + getDatabase();
				logger.debug(psql_command);
				Process proc2 = Runtime.getRuntime().exec(psql_command);
				int code = proc2.waitFor();
				logger.debug("end executing the script pg_dump. code: " + String.valueOf(code));
				
				//
				//
				//
				String export_tar= getWorkDir() + File.separator + getDatabase().toLowerCase()+ "-" + getExportFileName() + "-" + workdf.format(OffsetDateTime.now()) + ".tar.gz";
				String tar_command ="tar cvzf " + export_tar + "  " + csv;
				int code3 = 0;
				if (isLinux()) {
					logger.debug(tar_command);
					Process proc3 = Runtime.getRuntime().exec(tar_command);
					code3 = proc3.waitFor();
					logger.debug("end executing tar. code: " + String.valueOf(code3));
				}
				
				setProgress(100.0);
				setState(CommandState.COMPLETED);
				setResult("ok");
				
				logger.debug(psql_command + "(" + String.valueOf(code)+ (isLinux() ?( ") | " + tar_command + "("+String.valueOf(code3)+") "):""));
				setResultComments(psql_command + "(" + String.valueOf(code)+ (isLinux() ?( ") | " + tar_command + "("+String.valueOf(code3)+") "):""));
				
			} catch (Exception e) {
				setState(CommandState.ERROR);
				setResult(e.getClass().getName());
				setResultComments(e.getMessage());
				logger.error(e);
			}
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
	

	

}
