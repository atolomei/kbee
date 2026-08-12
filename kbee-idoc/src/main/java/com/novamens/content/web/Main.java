package com.novamens.content.web;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;

import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

import com.novamens.solr.sync.SolrSyncMain;
import kbee.util.PropertiesFactory;
import kbee.util.logging.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;

import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Main {

    static private kbee.util.logging.Logger logger = Logger.getLogger("StartupLogger");
    
    private static final String PARAM_GZIP_ENABLED = "gzipEnabled";

    private static final String PARAM_UPDATE_SOLR_SCHEMAS   = "updateSolrSchemas";
    private static final String PARAM_UPDATE_DB_SCHEMA      = "updateDBSchema";

    private static final String PARAM_OVERRIDE_WEB_XML = "com.novamens.kbee.overridedWebXml";
    private static final String PARAM_JETTY_PROFILES_DIR = "com.novamens.kbee.jettyProfilesDir";
    private static final String PARAM_JETTY_PROFILES_NAMES = "com.novamens.kbee.jettyProfiles";
    private static final String PARAM_JETTY_SHUTDOWN_PASSWORD_OLD = "com.novamens.kbee.jettyShutdownPassword";
    private static final String PARAM_JETTY_SHUTDOWN_PASSWORD = "server.jettyShutdownPassword";

    public static void main(String[] args) throws Exception {

        Server server;

        boolean updateSolrSchemas = isFlagEnabled(PARAM_UPDATE_SOLR_SCHEMAS);
        if (updateSolrSchemas)
            checkSolrSchemas(updateSolrSchemas);

        boolean updateDBSchema = isFlagEnabled(PARAM_UPDATE_DB_SCHEMA);
        if (updateDBSchema)
            checkLiquibaseChanges();

        server = loadConfigurationFromFile();

        if (server == null) {
            logger.info("Creating default Jetty server");
            server = new Server();
            ServerConnector connector = new ServerConnector(server);
            String port = System.getProperty("jetty.http.port");
            if (port!=null)
                connector.setPort(Integer.valueOf(port));
            else
            	connector.setPort(8080);
            connector.setHost("0.0.0.0");
            connector.setIdleTimeout(30000);
            server.addConnector(connector);
        }

        WebAppContext webapp = new WebAppContext();
        webapp.setServer(server);
        webapp.setContextPath("/");

        String pathToOverridedWebXml = System.getProperty(PARAM_OVERRIDE_WEB_XML);
        if (pathToOverridedWebXml != null && !pathToOverridedWebXml.isEmpty()) {
            webapp.setOverrideDescriptor(pathToOverridedWebXml);
        }

        ProtectionDomain protectionDomain = Main.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        webapp.setWar(location.toExternalForm());

        String shutdownPassword = (String) PropertiesFactory.getInstance("kbee").getProperties().get(PARAM_JETTY_SHUTDOWN_PASSWORD);
        if (shutdownPassword == null || shutdownPassword.length() == 0)
            shutdownPassword = PARAM_JETTY_SHUTDOWN_PASSWORD_OLD;

        ShutdownHandler shutdownHandler = new ShutdownHandler(shutdownPassword, true, false);

        HandlerList handlers = new HandlerList();

        boolean gzipEnabled = isFlagEnabled(PARAM_GZIP_ENABLED);
        if (gzipEnabled)
            webapp.setGzipHandler(getGzipHandler());

        handlers.addHandler(shutdownHandler);
        handlers.addHandler(webapp);
        server.setHandler(handlers);

        try {

            server.start();
            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(inputStreamReader);
            String read = br.readLine();
            if (read == null) {
                logger.error(">>> readLine returned null, probably no stdin specified.");
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (Exception e) {
                }
            }
            server.stop();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected static boolean isFlagEnabled(String flag) {
        String paramVal = (String) PropertiesFactory.getInstance("kbee").getProperties().get(flag);
        return paramVal != null && !paramVal.equals("false");
    }

    private static GzipHandler getGzipHandler() {
        GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.setIncludedMethods("POST", "GET");
        gzipHandler.setIncludedMimeTypes("text/html", "text/plain", "text/xml", "text/css", "application/javascript",
                "text/javascript", "application/json", "text/xml", "application/xml");
        gzipHandler.setInflateBufferSize(2048);
        gzipHandler.setMinGzipSize(2048);
        return gzipHandler;
    }

    private static Server loadConfigurationFromFile() throws Exception {

        List<String> configurations;
        String jettyConfigDir = (String) System.getProperties().get(PARAM_JETTY_PROFILES_DIR);

        if (jettyConfigDir == null) {
            return null;
        }

        String jettyProfiles = (String) System.getProperties().get(PARAM_JETTY_PROFILES_NAMES);
        if (jettyProfiles == null) {
            jettyProfiles = "jetty.xml;jetty-http.xml";
            // logger.error("Property \"" + jettyProfilePropName + "\" not set. using
            // default value: " + jettyProfiles);
        }

        configurations = Arrays.asList(jettyProfiles.split(";"));

        XmlConfiguration cumulativeConfig = null;
        // List<Object> configuredObjects = new ArrayList<Object>();

        Properties startingProperties = (Properties) System.getProperties().clone();
        // createDummyKeyStore("dummyStore.jks","dummyStorePassword","jks");
        // startingProperties.put("jetty.sslContext.keyStorePath","dummyStore.jks");
        // startingProperties.put("jetty.sslContext.keyStorePassword","dummyStorePassword");
        /*
         * startingProperties.put("jetty.sslContext.keyStorePath","cert/keystore");
         * startingProperties.put("jetty.sslContext.keyStorePassword","novamens");
         * 
         * startingProperties.put("jetty.sslContext.trustStorePath","cert/truststore");
         * startingProperties.put("jetty.sslContext.trustStorePassword","novamens");
         * 
         * 
         * startingProperties.put("jetty.sslContext.keyManagerPassword","novamens");
         */
        for (String configFile : configurations) {

            InputStream configStream = null;

            try {
            String file = jettyConfigDir + configFile;
            File xmlConfiguration = new File(file);

            if (xmlConfiguration.exists()) {
                configStream = new FileInputStream(xmlConfiguration);
            } else {
                throw new RuntimeException(xmlConfiguration + " file does not exist.");
            }
            Resource configResource = Resource.newResource(new File(file));

            XmlConfiguration configuration = new XmlConfiguration(configResource);

            for (String property : startingProperties.stringPropertyNames()) {
                configuration.getProperties().putIfAbsent(property, startingProperties.getProperty(property));
            }

            if (cumulativeConfig != null) {
                configuration.getIdMap().putAll(cumulativeConfig.getIdMap());
                configuration.configure();
                cumulativeConfig = configuration;
            } else {
                configuration.configure();
                cumulativeConfig = configuration;
            }
            } finally {
                if (configStream!=null)
                    configStream.close();
            }
        }

        if (cumulativeConfig == null) {
            throw new RuntimeException("No server created from profiles specified.");
        }
        for (String key : cumulativeConfig.getIdMap().keySet()) {
            Object obj = cumulativeConfig.getIdMap().get(key);
            if (obj instanceof Server) {
                return (Server) obj;
            }
        }
        throw new RuntimeException("No server found on configuration files.");
    }

    public static void createDummyKeyStore(String storeName, String storePassword, String storeType) {

        try (FileOutputStream fileOutputStream = new FileOutputStream(storeName)) {
            KeyStore keystore = KeyStore.getInstance(storeType);
            keystore.load(null, storePassword.toCharArray());
            keystore.store(fileOutputStream, storePassword.toCharArray());
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
            throw new RuntimeException("Cannot create dummy keystore", e);
        }
    }
    /*
     * public static void createTrustedKeyStore(String storeName, String
     * storePassword, String storeType ){
     * 
     * try (FileOutputStream fileOutputStream = new FileOutputStream(storeName)) {
     * TrustStore keystore = KeyStore.getInstance(storeType); keystore.load(null,
     * storePassword.toCharArray()); keystore.store(fileOutputStream,
     * storePassword.toCharArray()); } catch (CertificateException |
     * NoSuchAlgorithmException | IOException | KeyStoreException e) { throw new
     * RuntimeException("Cannot create dummy keystore",e); } }
     */

    private static void checkSolrSchemas(boolean applyChanges) {
        // solr.content-core=windsorcontent
        // solr.file-core=idoc7file

        // List<String> cores = Arrays.asList("audit-core", "content-core",
        // "file-core");
        List<String> cores = Arrays.asList("content-core", "file-core");

        String[] args = new String[5];
        args[0] = "--jsonConfigPath";
        args[2] = "--solrCoreUrl";
        args[4] = applyChanges ? "synchronize" : "printChanges";

        String solrUrl = (String) PropertiesFactory.getInstance("kbee").getProperties().get("solr.url");
        for (String coreId : cores) {
            String coreName = (String) PropertiesFactory.getInstance("kbee").getProperties().get("solr." + coreId);
            args[1] = "classpath:" + getMetaInfPath() + "/solr-schemas/" + coreId + "/schema.json";
            args[3] = solrUrl + "/" + coreName;
            SolrSyncMain.main(args);
        }
    }

    private static void checkLiquibaseChanges() {
        Liquibase liquibase = null;
        try {
            String changeLog = "db.changelog-master.xml";

            String dbUrl = (String) PropertiesFactory.getInstance("kbee").getProperties().get("jdbc.url");
            String dbUser = (String) PropertiesFactory.getInstance("kbee").getProperties().get("liquibase.username");
            String dbPass = (String) PropertiesFactory.getInstance("kbee").getProperties().get("liquibase.password");
            String dbDriverClass = (String) PropertiesFactory.getInstance("kbee").getProperties().get("jdbc.driverClassName");

            Database database = DatabaseFactory.getInstance().openDatabase(dbUrl, dbUser, dbPass, dbDriverClass, null, null, null,
                    new ClassLoaderResourceAccessor());
            URL resource = Main.class.getClassLoader().getResource(getMetaInfPath() + "/db-evolution/");

            URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { resource });
            liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(urlClassLoader), database);
            liquibase.update(new Contexts());
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        } finally {
            if (liquibase != null)
                try {
                    liquibase.close();
                } catch (LiquibaseException e) {
                    logger.error(e);
                }
        }
    }

    private static String getMetaInfPath() {
        String path = "WEB-INF/classes/META-INF";
        if (null == Main.class.getClassLoader().getResource(path)) {
            path = "META-INF"; // we are running inside IDE
        }
        return path;
    }
}