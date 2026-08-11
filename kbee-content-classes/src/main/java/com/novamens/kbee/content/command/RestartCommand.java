package com.novamens.kbee.content.command;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.command.CommandState;

public class RestartCommand extends AbstractCommand {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RestartCommand.class.getName());
	static private Logger stlogger = LogManager.getLogger("StartupLogger");
	
	private String PWD;
	private String USER_DIR;
	
	public RestartCommand() {
		setName("Application Restart Command");
		setExactlyOneSemantics(true);
	}


	@Override
	public void execute() {

		logger.debug("Starting Command execution " + getName());

		setDateStarted(OffsetDateTime.now());
		setProgress(0);

		if  (System.getenv("OS")!=null && System.getenv("OS").toLowerCase().contains("windows")) {
			logger.error("This command doesnt work on Windows");
			setState(CommandState.ERROR);
			setResult("This command doesnt work on Windows");
			setDateTerminated(OffsetDateTime.now());
			return;
		}

		this.PWD = System.getenv("PWD");
		USER_DIR = System.getProperty("user.dir");

		logger.debug("PWD " + this.PWD);
		logger.debug("user.dir" + System.getProperty("user.dir"));

		if (USER_DIR==null)
			USER_DIR="./";

		try {


			String restartFileName = USER_DIR+ File.separator + "bin" + File.separator +"restart_idoc.sh";
			if(!existsFile(restartFileName))
				makeRestart(restartFileName);

			logger.debug("-----------------------------------------------------");
			logger.debug("WARNING. The System will shutdown now....");
			logger.debug("About to execute -> " + restartFileName);
			logger.debug("-----------------------------------------------------");

			stlogger.debug("Restarting application ");

			Process proc = Runtime.getRuntime().exec(restartFileName);
			proc.waitFor();

			/** uy  */
			stlogger.debug("Restarting application FAILED ");

			setState(CommandState.COMPLETED);

		} catch (Exception e) {
			logger.error(e);
			setState(CommandState.ERROR);
			setResult(e.getClass().getName());
		}
		finally {
			logger.debug("Terminated");
			setDateTerminated(OffsetDateTime.now());
		}
	}
	
	@Override
	public int getPriority() {
		return 0;
	}

	private void makeRestart(String filename) throws IOException {
		List<String> kj = new ArrayList<String>();

		kj.add("cd " + PWD + File.separator + "bin");
		kj.add("./shutdown.sh kill");
		kj.add("./start_bg.sh");
		makeFile(filename, kj);
	}

	/**
	 * 	    kill_java.sh
	 */
	private void makeKillJava() throws IOException {

		List<String> kj = new ArrayList<String>();
		kj.add("PIDS=$(ps -ef | grep java | grep webapps | grep -v grep | awk {'print $2'})");
		kj.add("echo ${PIDS}");
		kj.add("for i in ${PIDS}");
		kj.add("do");
		kj.add("	/bin/kill -9 $i");
		kj.add("	echo $i");
		kj.add("done");
		kj.add("exit 0");
		makeFile(USER_DIR+File.separator +"kill_java.sh", kj);
	}


	/**
	 * 
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
			logger.error(e);
			throw(e);
		} finally {
			if (out!=null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e);
					throw(e);
				}
			}
		}
		
		try {
			
			logger.debug("chmod +x "+path);
			Process proc = Runtime.getRuntime().exec("chmod +x "+path);
			proc.waitFor();
			logger.debug("After chmod");
			
		} catch (Exception e) {
			logger.error(e);
			throw(new IOException(e));
		}
	}
	
	
	private boolean existsFile(String path) {
		File f = new File(path);
		if (f!=null && f.exists() && !f.isDirectory())
			return true;
		return false;
	}

	
	
	/**
	 * run.bat
	 * java -jar /opt/jetty/start.jar --module=logging-log4j2 --lib=webapps/root/WEB-INF/lib/novamens-logging-*.jar --lib=webapps/root/WEB-INF  STOP.PORT=28290 STOP.KEY=secret  -Xmx6G -Xms4G  -Djava.net.preferIPv4Stack=true -Duser.timezone=US/Central  -Dlog4j.configurationFile=log4j-dev.xml &
	 */
	
	/**
	  * shutdown.bat
	  * java -jar /opt/jetty/start.jar STOP.PORT=28290 STOP.KEY=secret --stop
	  */
	/**
	 * start.ini

#===========================================================
# Jetty Startup 
#
# Starting Jetty from this {jetty.home} is not recommended.
#
# A proper {jetty.base} directory should be configured, instead
# of making changes to this {jetty.home} directory.
#
# See documentation about {jetty.base} at
# http://www.eclipse.org/jetty/documentation/current/startup.html
#
# A demo-base directory has been provided as an example of
# this sort of setup.
#
# $ cd demo-base
# $ java -jar ../start.jar
#
#===========================================================

# To disable the warning message, comment the following line
--module=home-base-warning

# --------------------------------------- 
# Module: ext
# Adds all jar files discovered in $JETTY_HOME/lib/ext
# and $JETTY_BASE/lib/ext to the servers classpath.
# --------------------------------------- 
--module=ext


# --------------------------------------- 
# Module: server
# Enables the core Jetty server on the classpath.
# --------------------------------------- 
--module=server

### ThreadPool configuration
## Minimum number of threads
# jetty.threadPool.minThreads=10

## Maximum number of threads
# jetty.threadPool.maxThreads=200

## Thread idle timeout (in milliseconds)
# jetty.threadPool.idleTimeout=60000

### Common HTTP configuration
## Scheme to use to build URIs for secure redirects
# jetty.httpConfig.secureScheme=https

## Port to use to build URIs for secure redirects
# jetty.httpConfig.securePort=8443

## Response content buffer size (in bytes)
# jetty.httpConfig.outputBufferSize=32768

## Max response content write length that is buffered (in bytes)
# jetty.httpConfig.outputAggregationSize=8192

## Max request headers size (in bytes)
# jetty.httpConfig.requestHeaderSize=8192

## Max response headers size (in bytes)
# jetty.httpConfig.responseHeaderSize=8192

## Whether to send the Server: header
# jetty.httpConfig.sendServerVersion=true

## Whether to send the Date: header
# jetty.httpConfig.sendDateHeader=false

## Max per-connection header cache size (in nodes)
# jetty.httpConfig.headerCacheSize=512

## Whether, for requests with content, delay dispatch until some content has arrived
# jetty.httpConfig.delayDispatchUntilContent=true

## Maximum number of error dispatches to prevent looping
# jetty.httpConfig.maxErrorDispatches=10

## Maximum time to block in total for a blocking IO operation (default -1 is to use idleTimeout on progress)
# jetty.httpConfig.blockingTimeout=-1

### Server configuration
## Whether ctrl+c on the console gracefully stops the Jetty server
# jetty.server.stopAtShutdown=true

## Timeout in ms to apply when stopping the server gracefully
# jetty.server.stopTimeout=5000

## Dump the state of the Jetty server, components, and webapps after startup
# jetty.server.dumpAfterStart=false

## Dump the state of the Jetty server, components, and webapps before shutdown
# jetty.server.dumpBeforeStop=false

## The name to uniquely identify this server instance
#jetty.defaultSessionIdManager.workerName=node1

## How frequently sessions are inspected
#jetty.sessionInspectionInterval.seconds=60

# --------------------------------------- 
# Module: jsp
# Enables JSP for all webapplications deployed on the server.
# --------------------------------------- 
--module=jsp


# --------------------------------------- 
# Module: resources
# Adds the $JETTY_HOME/resources and/or $JETTY_BASE/resources
# directory to the server classpath. Useful for configuration
# property files (eg jetty-logging.properties)
# --------------------------------------- 
--module=resources


# --------------------------------------- 
# Module: deploy
# Enables webapplication deployment from the webapps directory.
# --------------------------------------- 
--module=deploy

# Monitored directory name (relative to $jetty.base)
# jetty.deploy.monitoredDir=webapps
# - OR -
# Monitored directory path (fully qualified)
# jetty.deploy.monitoredPath=/var/www/webapps

# Defaults Descriptor for all deployed webapps
# jetty.deploy.defaultsDescriptorPath=${jetty.base}/etc/webdefault.xml

# Monitored directory scan period (seconds)
# jetty.deploy.scanInterval=1

# Whether to extract *.war files
# jetty.deploy.extractWars=true

# --------------------------------------- 
# Module: jstl
# Enables JSTL for all webapplications deployed on the server
# --------------------------------------- 
--module=jstl


# --------------------------------------- 
# Module: websocket
# Enable websockets for deployed web applications
# --------------------------------------- 
--module=websocket


# --------------------------------------- 
# Module: http
# Enables a HTTP connector on the server.
# By default HTTP/1 is support, but HTTP2C can
# be added to the connector with the http2c module.
# --------------------------------------- 
--module=http

### HTTP Connector Configuration

## Connector host/address to bind to
# jetty.http.host=0.0.0.0

## Connector port to listen on
 jetty.http.port=8089

## Connector idle timeout in milliseconds
jetty.http.idleTimeout=720000

## Connector socket linger time in seconds (-1 to disable)
# jetty.http.soLingerTime=-1

## Number of acceptors (-1 picks default based on number of cores)
# jetty.http.acceptors=-1

## Number of selectors (-1 picks default based on number of cores)
# jetty.http.selectors=-1

## ServerSocketChannel backlog (0 picks platform default)
# jetty.http.acceptorQueueSize=0

## Thread priority delta to give to acceptor threads
# jetty.http.acceptorPriorityDelta=0

## HTTP Compliance: RFC7230, RFC2616, LEGACY
# jetty.http.compliance=RFC7230
*/
	
	
	
}



