package com.novamens.kbee.content.command;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;

public class LogingHelper {
    private static RollingFileAppender firstLogger=null;

    public static String getLoggerPath(String logfileKey){

        return new File(extractDirectoryPath(getFirstRollingFile().getFileName()), logfileKey + ".log").toString();
    }

    public static kbee.util.logging.Logger getPrivateLogger(String logfileKey, Level logLevel) {

        kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(logfileKey);


        String appenderName = logfileKey + "_appender";
        LoggerContext lc = (LoggerContext) LogManager.getContext(false);


        Configuration contextConfig = lc.getConfiguration();
        LoggerConfig prevLoggerConfig = contextConfig.getLoggerConfig(logfileKey);

        boolean appenderExists = prevLoggerConfig.getAppenderRefs().stream().anyMatch(ap -> ap.getRef().equals(appenderName));
        if (!appenderExists) {
            RollingFileAppender firstRollingFile = getFirstRollingFile();
            if (firstRollingFile == null)
                throw new RuntimeException("Unable to logging path, no RollingFileAppender found.");


            FileAppender fa = FileAppender.newBuilder().setName(appenderName).withAppend(false).withFileName(new File(extractDirectoryPath(firstRollingFile.getFileName()), logfileKey + ".log").toString())
                    .setLayout(PatternLayout.newBuilder().withPattern("%-5p %d  [%t] %C{2} (%F:%L) - %m%n").build())
                    .setConfiguration(contextConfig).build();
            fa.start();
            contextConfig.addAppender(fa);

            AppenderRef ref = AppenderRef.createAppenderRef(appenderName, Level.DEBUG, null);
            AppenderRef[] refs = new AppenderRef[]{ref};
            LoggerConfig loggerConfig = LoggerConfig.createLogger(false, logLevel, logfileKey + "_logger",
                    "true", refs, null, contextConfig, null);

            loggerConfig.addAppender(fa, Level.DEBUG, null);
            contextConfig.addLogger(logfileKey, loggerConfig);
            lc.updateLoggers(contextConfig);

            logger = kbee.util.logging.Logger.getLogger(logfileKey);
        }
        return logger;
    }

    private static RollingFileAppender getFirstRollingFile() {
        if(firstLogger == null) {
            LoggerContext lc = (LoggerContext) LogManager.getContext(false);
            final Configuration contextConfig = lc.getConfiguration();
            firstLogger= (RollingFileAppender) contextConfig.getAppenders().values().stream().filter(app -> app instanceof RollingFileAppender).findFirst().orElse(null);
        }
        return firstLogger;
    }


    private static String extractDirectoryPath(String path) {
        if ((path == null) || path.equals("") || path.equals("/")) {
            return "";
        }

        int lastSlashPos = path.lastIndexOf('/');

        if (lastSlashPos >= 0) {
            return path.substring(0, lastSlashPos); //strip off the slash
        } else {
            return "";
        }
    }
}
