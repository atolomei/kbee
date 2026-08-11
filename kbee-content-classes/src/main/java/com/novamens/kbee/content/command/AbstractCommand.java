package com.novamens.kbee.content.command;

import java.io.*;
import java.time.Duration;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import com.novamens.content.command.CommandLifecycleCallback;
import com.novamens.content.command.CommandParameter;
import com.novamens.content.resource.KBFile;
import com.novamens.util.KbeeRuntimeException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.FormattedMessageFactory;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;


public abstract class AbstractCommand implements Command {


    static private long id_sequence = System.currentTimeMillis();

    static private kbee.util.logging.Logger xlogger = kbee.util.logging.Logger.getLogger(AbstractCommand.class.getName());

    List<CommandLifecycleCallback> callbackList = new ArrayList<>();


    private Domain domain;

    private CommandState state = CommandState.NOT_STARTED;

    private Serializable domain_id;
    private int scope = Command.SCOPE_KBEE_ROOT;

    private OffsetDateTime start;
    private OffsetDateTime end;

    private double progress;

    private String description;
    private String status_info;
    private String result;
    private String resultcomment;
    private String resultsdetails;
    
    

    private OffsetDateTime created;
    private Long id;
    private String name;
    private String key;
    private boolean stop = false;
    private int priority = SchedulerService.LOW_PRIORITY;

    private boolean exactlyOneSemantics = false;

    private PrintStream loggerstream;
    private String logpath;

    private Serializable user_id;

    private boolean requires_explicit_trx = false;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    private KBFile resultFile;
    private File logFile;

    private kbee.util.logging.Logger logger = xlogger;
    private boolean privateLogger = false;

    static synchronized public long getNewId() {
        id_sequence++;
        return id_sequence;
    }


    public AbstractCommand(Map<String, Object> param) {
        this.id = Long.valueOf(AbstractCommand.getNewId());
        this.created = OffsetDateTime.now();
        setParameters(param);

    }


    public AbstractCommand() {
        this.id = Long.valueOf(AbstractCommand.getNewId());
        this.created = OffsetDateTime.now();
    }

    @Override
    public String getTitle() {
        return this.getClass().getSimpleName();
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        if (this.name == null)
            return this.getClass().getSimpleName();
        return name;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public void setDescription(String des) {
        this.description = des;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public CommandState getState() {
        return state;
    }

    @Override
    public String getHelp() {
        try {
            ResourceBundle resources = ResourceBundle.getBundle(getClass().getName());
            if (resources != null) {
                return resources.getString("help");
            }
        } catch (MissingResourceException e) {
        }
        return null;
    }

    /**
     * CommandBean -> RequestCommand -> Scheduler TRX
     * RequestCronJob -> Scheduler TRX
     * <p>
     * From UI -> must include trx here
     *
     * @param b
     */

    @Override
    public void setRequiresExplicitTrx(boolean b) {
        this.requires_explicit_trx = b;
    }

    @Override
    public boolean isRequiresExplicitTrx() {
        return this.requires_explicit_trx;
    }

    /**
     * in milliseconds
     */
    public long getDuration() {

        if (start == null)
            return 0;


        if (!isTerminated()) {
            return Duration.between(start, OffsetDateTime.now()).getSeconds() * 1000;
        }

        if (end == null)
            return 0;

        return Duration.between(start, end).getSeconds() * 1000;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        if (this.key == null)
            return this.key;
        return this.name;
    }

    public double getProgress() {
        return progress;
    }

    public OffsetDateTime getDateCreated() {
        return this.created;
    }

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public Serializable getDomainId() {
        return this.domain_id;
    }

    public void setDomain(Domain domain) {
        if (domain == null)
            throw new IllegalArgumentException("domain is null");
        this.domain = domain;
        this.domain_id = domain.getId();
    }

    @Override
    public void setDomainId(Serializable did) {
        this.domain_id = did;
    }

    @Override
    public Serializable getUserId() {
        return this.user_id;
    }

    @Override
    public void setUserId(Serializable did) {
        this.user_id = did;
    }

    @Override
    public OffsetDateTime getDateStarted() {
        return start;
    }

    @Override
    public OffsetDateTime getDateTerminated() {
        return end;
    }

    @Override
    public void setState(CommandState state) {
        this.state = state;
    }

    @Override
    public boolean isTerminated() {
        return (getState() == CommandState.CANCELED ||
                getState() == CommandState.COMPLETED ||
                getState() == CommandState.ERROR ||
                getState() == CommandState.UNKNOWN);
    }

    @Override
    public synchronized void stop() {
        setDateTerminated(OffsetDateTime.now());
        setResult("User Stop");
        close();
    }

    public synchronized void end() {
        setResult("COMPLETED");
        setState(CommandState.COMPLETED);
        close();
    }

    public synchronized void error() {
        setResult("ERROR");
        setState(CommandState.ERROR);
        close();
    }

    protected void close() {
        setDateTerminated(OffsetDateTime.now());
        this.stop = true;

        //uploadPrivateLogger();
        closeLogger();
        callbackEvent(CommandLifecycleCallback::end);
    }

	/*	@Override
	public synchronized void stop() {
		logger.info("User Stop.");
		setResult("User Stop");
		setState(CommandState.CANCELED);
	}

	public synchronized void end() {
		setResult("COMPLETED");
		setState(CommandState.COMPLETED);

	}
	public synchronized void error() {
		setResult("ERROR");
		setState(CommandState.ERROR);
	}

*/


    public double estimatedSecsToEnd() {

        if (this.start == null)
            return -1.0;

        if (this.end != null)
            return 0.0;

        if (getProgress() == 0)
            return -1;

        long elapsed = Duration.between(start, OffsetDateTime.now()).getSeconds() * 1000;

        double estimate_millisecs = ((double) (100.0 / (double) getProgress()) * (double) elapsed) - (double) elapsed;

        return (estimate_millisecs / 1000.0);
    }

    @Override
    public String getResult() {
        return this.result;
    }

    @Override
    public String getResultComment() {
        return this.resultcomment;
    }

    @Override
    public void setParameter(String name, Object value) {
        this.parameters.put(name, value);
    }

    @Override
    public Object getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public String getStringParameter(String name, String defaultValue) {
        String s = (String) parameters.get(name);
        return s != null ? s : defaultValue;
    }


    @Override
    public String getResultDetails() {
        return resultsdetails;
    }

    @Override
    public void setResultDetails(String rsd) {
        resultsdetails = rsd;
    }

    @Override
    public boolean isStopped() {
        return stop;
    }

    @Override
    public void setPriority(int p) {
        this.priority = p;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public String getStatusInfo() {
        if (status_info == null)
            return getState().getLabel();
        return status_info;
    }

    public KBFile getResultFile() {
        return resultFile;
    }

    public void setResultFile(KBFile resultFile) {
        this.resultFile = resultFile;
    }

    public File getLogFile() {
        if(loggerstream!=null)
            loggerstream.flush();

        return logFile;
    }

    private void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    @Override
    public List<String> getMetadataAsList() {

        List<String> list = new ArrayList<String>();

        list.add("Status: " + getState().getLabel());

        Locale locale = null;
        String zid = null;

        DateTimeService service = ServiceLocator.getService(DateTimeService.class);

        if (getUser() != null) {
            locale = getUser().getLocale();
            zid = service.getMapZoneIds().get(getUser().getTimeZone());
        }

        if (locale == null)
            locale = Locale.getDefault();

        if (zid == null)
            zid = ZoneId.systemDefault().getId();

        if (getUserId() != null) {
            if (getDateStarted() != null)
                list.add("Launched: " + getUserId().toString() + ". " + service.timeElapsed(getDateStarted(), zid, locale) + ". ");
            else
                list.add("Launched: " + getUserId().toString() + ". ");
        }

        if (this.isTerminated() && getDateTerminated() != null)
            list.add("Terminated: " + getUserId().toString() + ". " + service.timeElapsed(getDateTerminated(), zid, locale) + ". ");


        return list;
    }

    @Override
    public void setParameters(Map<String, Object> map) {
        this.parameters = map;
    }

    @Override
    public Map<String, Object> getParameters() {
        if (this.parameters == null)
            this.parameters = new HashMap<String, Object>();
        return this.parameters;
    }

    @Override
    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public kbee.util.logging.Logger getLogger() {
        return logger;
    }

    public String getLogPath() {
        return logpath;
    }

    public long getTotalItems() {
        return 0;
    }

    public long getTotalItemsProcessed() {
        return 0;
    }

    public String toString() {

        StringBuilder str = new StringBuilder();

        if (this.getName() != null)
            str.append(this.getName());

        if (this.getDescription() != null) {
            if (str.length() > 0)
                str.append(" | ");
            str.append(this.getDescription());
        }

        return str.toString();
    }

    @Override
    public boolean isExactlyOneSemantics() {
        return exactlyOneSemantics;
    }

    @Override
    public void setExactlyOneSemantics(boolean exactlyOneSemantics) {
        this.exactlyOneSemantics = exactlyOneSemantics;
    }

    @Override
    public int getThreads() {
        return 1;
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isPaused() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
    }

    protected void initCommand() {
        state = CommandState.NOT_STARTED;
        start = null;
        end = null;
        status_info = null;
        result = null;
        resultcomment = null;
        resultsdetails = null;
        this.stop = false;
    }


    protected void setResultComments(String resultcomment) {
        this.resultcomment = resultcomment;
    }

    /**
     * progres is a value between 0.00 and 100.00
     *
     * @param progress
     */
    protected void setProgress(double progress) {
        this.progress = progress;
    }

    protected void setDateStarted(OffsetDateTime start) {
        this.start = start;
        callbackEvent(CommandLifecycleCallback::start);

    }

    protected void setDateTerminated(OffsetDateTime end) {
        this.end = end;
    }

    protected void setResult(String result) {
        this.result = result;
    }

    protected void setStatusInfo(String si) {
        this.status_info = si;
    }

    protected void error(Object message) {
        getLogger().error((String) message);
    }

    protected void debug(Object message) {
        getLogger().debug(message);
    }

    protected void debug(String message) {
        getLogger().debug(message);
    }

    protected void info(String message) {
        getLogger().info(message);
    }


    //By default there is no simultaneous execution control
    public String getConcurrentUniqueKey() {
        return null;
    }

    @Override
    public void addCallback(CommandLifecycleCallback commandLifecycleCallback) {
        callbackList.add(commandLifecycleCallback);
    }

    public void setUpPrivateLogger() {
        if (!isPrivateLogger()) {
            setLogger(getPrivateLoggerPath());
            privateLogger = true;
        }
    }

    private String getPrivateLoggerPath() {
        return "logs/" + this.getClass().getName() + "_" + getId() + ".log";
    }

    public boolean isPrivateLogger() {
        return privateLogger;
    }

    public void setLogger(String path) {
        try {
            logpath = path;
            final File file = new File(path);
            setLogFile(file);
            file.createNewFile();
            loggerstream = new PrintStream(new BufferedOutputStream(new FileOutputStream(file) ));
            String loggername = "Command" + getId() + "Logger";
            SimpleLogger logger = new SimpleLogger(loggername, Level.ALL, false, false, true, false, "dd MMM yyyy hh:mm:ss z",
                    new FormattedMessageFactory(), new PropertiesUtil(new Properties()), loggerstream);
            this.logger = new kbee.util.logging.Logger(logger);
        } catch (IOException e) {
            throw new KbeeRuntimeException(e);
        }
    }

    private void closeLogger() {
        if (loggerstream != null) {
            loggerstream.flush();
            loggerstream.close();
        }
    }

    //method for simplifying calls to events
    private void callbackEvent(Consumer<CommandLifecycleCallback> f) {

        for (CommandLifecycleCallback cb : callbackList) {
            try {
                f.accept(cb);
            } catch (Exception e) {
                logger.error(e, "Exception received while processing event.");
            }
        }
    }


    @Override
    public Domain getDomain() {
 
    	if (domain == null)
            domain = ServiceLocator.getService(UserService.class).getDomain();
        return domain;
    }

    private User getUser() {
        return ServiceLocator.getService(SecurityService.class).getSessionUser();
    }

    protected String getDataExportDir() {
        return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() + File.separator + "dataexport";
    }

    protected String getWorkDir() {
        return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath();
    }

    protected Transaction beginTransaction() {
        return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
    }
    
    protected Transaction beginTransaction(boolean session) {
        return ServiceLocator.getService(TransactionService.class).beginTransaction(session);
    }
	
	@Override
	public List<CommandParameter> getParametersDefinition() {
		List<CommandParameter> list=new ArrayList<CommandParameter>();
		return list;
	}
}
