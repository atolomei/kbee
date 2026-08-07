
package com.novamens.kbee.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Modifier;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.AppMonitoringService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.KBFSStorageType;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.kbfs.KbeeShardedMinioFileServer;
import com.novamens.kbee.kbfs.KbeeShardedOdilonFileServer;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.FileServerS3;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.scheduler.ServiceRequest;
import com.novamens.security.User;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.system.properties.SystemPropertiesService;
import com.novamens.util.KbeeFileUtils;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.NumberFormatter;
import kbee.util.PropertiesFactory;
import kbee.util.Tuple;

public class KbeeApplicationServerService implements ApplicationServerService, EventListener {

    static private final String SERVER_ID = PropertiesFactory.getInstance("kbee").getProperties().getProperty("server.id", "dev")
            .trim();
    static private final String USER_DIR = PropertiesFactory.getInstance("kbee").getProperties()
            .getProperty("user.dir", "." + File.separator).trim();
    static private final String HOME = PropertiesFactory.getInstance("kbee").getProperties().getProperty("home", USER_DIR).trim();

    
    static private final String DRIVE = PropertiesFactory.getInstance("kbee").getProperties()
            .getProperty("drive", (isLinux() ? "." : ".") + File.separator + "drive").trim();

    
    //static private final String KB FS1 = PropertiesFactory.getInstance("kbee").getProperties()
    //        .getProperty("local-repository", (isLinux() ? "." : ".") + File.separator + "local-repository").trim();

    static private final String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);
    static private final String _OK = PropertiesFactory.getInstance("kbee").getProperties().getProperty("ping.ok", "OK");

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger
            .getLogger(KbeeApplicationServerService.class.getName());
    static public final double GB = 1000000000.0;

    private String port = null;
    private String linux_home = (new File(System.getProperty("user.dir"))).getPath();
    private String windows_home = System.getProperty("user.dir");

    @SuppressWarnings("unused")
    static private Logger txLogger = LogManager.getLogger("TxLogger");

    static String serverhost = sHost();

    private String dataexport_dir;
    private String work_dir;
    private String home_dir;
    private String drive_dir;
    private String kbfs1_dir;
    private String image_dir;
    private String email_templates_dir;
    private String form_templates_dir;
    private String inline_help_dir;
    private String login_image_dir;
    private String avatar_image_dir;

    private String wicket_configuration_type = "";

    boolean kbfs1_enabled = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs1.enabled", "yes").toLowerCase()
            .trim().equals("yes");
    boolean kbfs2_enabled = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.enabled", "yes").toLowerCase()
            .trim().equals("yes");
    boolean odilon_enabled = PropertiesFactory.getInstance("kbee").getProperties().getProperty("odilon.enabled", "no").toLowerCase()
            .trim().equals("yes");

    public boolean isOdilonEnabled() {
        return odilon_enabled;
    }

    public boolean isLocalFSEnabled() {
        return kbfs1_enabled;
    }

    public boolean isMinioEnabled() {
        return kbfs2_enabled;
    }

    public void checkDirs() {

        File work = new File(getWorkDirAbsolutePath());
        if (!(work.exists() && work.isDirectory())) {
            try {
                KbeeFileUtils.forceMkdir(work);
            } catch (IOException e) {
                logger.error(e);
                throw new KbeeRuntimeException("Can not create Work Directory " + getWorkDirAbsolutePath());
            }
        }

        File drive = new File(getDriveDir());
        if (!(drive.exists() && drive.isDirectory())) {
            try {
                KbeeFileUtils.forceMkdir(drive);
            } catch (IOException e) {
                logger.error(e);
                throw new KbeeRuntimeException("Can not create Work Directory " + getDriveDir());
            }
        }

        File images = new File(getImagesDir());

        if (!(images.exists() && images.isDirectory())) {
            try {
                KbeeFileUtils.forceMkdir(images);
            } catch (IOException e) {
                logger.error(e);
                throw new KbeeRuntimeException("Can not create Images Directory " + getImagesDir());
            }
        }

        File loginimages = new File(getLoginImagesDir());
        if (!(loginimages.exists() && loginimages.isDirectory())) {
            try {
                KbeeFileUtils.forceMkdir(loginimages);
            } catch (IOException e) {
                logger.error(e);
                throw new KbeeRuntimeException("Can not create LoginImages Directory " + getLoginImagesDir());
            }
        }

        File avatar = new File(getAvatarImagesDir());
        if (!(avatar.exists() && avatar.isDirectory())) {
            try {
                KbeeFileUtils.forceMkdir(avatar);
            } catch (IOException e) {
                logger.error(e);
                throw new KbeeRuntimeException("Can not create Avatar Images Directory " + getAvatarImagesDir());
            }
        }

        /**
        File kbfs1 = new File(this.getKB FS1Dir());
        if (!(kbfs1.exists() && kbfs1.isDirectory())) {
            try {
                KbeeFileUtils.forceMkdir(kbfs1);
            } catch (IOException e) {
                logger.error(e);
                throw new KbeeRuntimeException("Can not create Work Directory " + getKBFS1Dir());
            }
        }
*/
        logger.debug("getWorkDir() -> " + getWorkDirAbsolutePath());
        logger.debug("getDriveDir() -> " + getDriveDir());
        logger.debug("getImagesDir() -> " + getImagesDir());
        logger.debug("getLoginImagesDir() -> " + getLoginImagesDir());
        logger.debug("getAvatarImagesDir() -> " + getAvatarImagesDir());
    //    logger.debug("getKBF S1Dir() -> " + getKBFS1Dir());
        logger.debug("getHomeDir() -> " + getHomeDir());

    }

    @Override
    public String getHomeDir() {

        if (home_dir != null)
            return home_dir;

        if (HOME.endsWith("/"))
            home_dir = HOME.length() > 1 ? HOME.substring(0, HOME.length() - 1) : HOME;
        else
            home_dir = HOME;
        return home_dir;

    }

    public String getWorkDirAbsolutePath() {

        if (work_dir != null)
            return work_dir;

        String home_abs_path = getHomeDirAbsolutePath();

        if (home_abs_path.endsWith("/"))
            home_abs_path = home_abs_path.length() > 1 ? home_abs_path.substring(0, HOME.length() - 1) : home_abs_path;

        work_dir = home_abs_path + File.separator + "tmp";

        return work_dir;

    }

    /**
     * 
     * public String getWorkDir() {
     * 
     * if (work_dir!=null) return work_dir;
     * 
     * 
     * 
     * 
     * if (WRK.endsWith("/")) work_dir = WRK.length()>1?WRK.substring(0,
     * WRK.length()-1):WRK; else work_dir = WRK; return work_dir; }
     **/

    @Override
    public String getImagesDir() {
        if (image_dir != null)
            return image_dir;
        image_dir = getHomeDir() + File.separator + "images";
        return image_dir;
    }

    @Override
    public String getAvatarImagesDir() {
        if (avatar_image_dir != null)
            return avatar_image_dir;
        avatar_image_dir = getHomeDir() + File.separator + "images" + File.separator + "avatar";
        return avatar_image_dir;
    }

    /**
     * 
     * 
     * 
     * @param r_dir
     * @param name
     * @return
     */

    public String getEmailTemplatesDir() {

        if (email_templates_dir != null) {
            return email_templates_dir;
        }
        try {
            File folder = new ClassPathResource("email-templates").getFile();
            email_templates_dir = folder.getAbsolutePath();
        } catch (Exception e) {
            email_templates_dir = new File("").getAbsolutePath();
        }
        logger.debug(email_templates_dir);
        return email_templates_dir;
    }

    @Override
    public String getFormTemplatesDir() {

        if (form_templates_dir != null) {
            return form_templates_dir;
        }
        try {
            File folder = new ClassPathResource("form-templates").getFile();
            form_templates_dir = folder.getAbsolutePath();
        } catch (Exception e) {
            form_templates_dir = new File("").getAbsolutePath();
        }
        logger.debug(form_templates_dir);
        return form_templates_dir;
    }

    @Override
    public String getLoginImagesDir() {
        if (login_image_dir != null)
            return login_image_dir;
        login_image_dir = getHomeDir() + File.separator + "images" + File.separator + "login";
        return login_image_dir;
    }

    @Override
    public String getDriveDir() {
        if (drive_dir != null)
            return drive_dir;
        if (DRIVE.endsWith("/"))
            drive_dir = DRIVE.substring(0, DRIVE.length() - 1);
        else
            drive_dir = DRIVE;
        return drive_dir;
    }


    /**@Override
    public String getKBFS1Dir() {
        if (kbfs1_dir != null)
            return kbfs1_dir;
        if (KBFS1.endsWith("/"))
            kbfs1_dir = KBFS1.substring(0, KBFS1.length() - 1);
        else
            kbfs1_dir = KBFS1;
        return kbfs1_dir;
    }
**/
    
    @Override
    public String getDataExportDir() {
        if (dataexport_dir != null)
            return dataexport_dir;
        dataexport_dir = getWorkDirAbsolutePath() + File.separator + "dataexport";
        return dataexport_dir;

    }

    private static boolean isLinux() {
        if (System.getenv("OS") != null && System.getenv("OS").toLowerCase().contains("windows"))
            return false;
        return true;
    }

    @Override
    public String getHomeDirAbsolutePath() {
        if (isLinux())
            return linux_home;
        return windows_home;
    }

    @Override
    public String getWicketConfigurationType() {
        return wicket_configuration_type;
    }

    @Override
    public void setWicketConfigurationType(String str) {
        wicket_configuration_type = str;

    }

    // ---------------------------------------------------------
    //
    // PASAR AL VAULT
    //
    // private static String secretKey = "kbeetexyc0w#";
    // private static String salt = "tiraparaarribatira";
    //
    // ---------------------------------------------------------
    /**
     * 
     * @param strToEncrypt
     * @param secret
     * @return
     * 
     *         public static String encrypt(String strToEncrypt, String secret) {
     *         try { byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
     *         IvParameterSpec ivspec = new IvParameterSpec(iv);
     * 
     *         SecretKeyFactory factory =
     *         SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); KeySpec spec =
     *         new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
     *         SecretKey tmp = factory.generateSecret(spec); SecretKeySpec secretKey
     *         = new SecretKeySpec(tmp.getEncoded(), "AES");
     * 
     *         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
     *         cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec); return
     *         Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
     *         } catch (Exception e) { logger.error("Error while encrypting: " +
     *         e.toString()); } return null; }
     * 
     * 
     *         public static String decrypt(String strToDecrypt, String secret) {
     * 
     *         try { byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
     *         IvParameterSpec ivspec = new IvParameterSpec(iv); SecretKeyFactory
     *         factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
     *         KeySpec spec = new PBEKeySpec(secretKey.toCharArray(),
     *         salt.getBytes(), 65536, 256); SecretKey tmp =
     *         factory.generateSecret(spec); SecretKeySpec secretKey = new
     *         SecretKeySpec(tmp.getEncoded(), "AES"); Cipher cipher =
     *         Cipher.getInstance("AES/CBC/PKCS5PADDING");
     *         cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec); return new
     *         String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt))); }
     *         catch (Exception e) { logger.error("Error while decrypting: " +
     *         e.toString()); } return null; }
     * 
     *         public String encrypt(String str) { return encrypt(str, secretKey); }
     * 
     *         public String decrypt(String str) { return decrypt(str, secretKey); }
     */

    @Override
    public String getApplicationServerId() {
        return SERVER_ID;
    }

    public String getServerHost() {
        return serverhost;
    }

    @Override
    public boolean listen(Event event) {
        if (event instanceof EvictCacheServiceEvent)
            return true;
        return false;
    }

    @Override
    public void onEvent(Event event) {
    }

    private static String sHost() {

        StringBuilder output = new StringBuilder();

        try {

            ProcessBuilder processBuilder = new ProcessBuilder();

            if (isLinux())
                processBuilder.command("bash", "-c", "hostname");
            else
                processBuilder.command("cmd.exe", "/c", "hostname");

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                logger.debug(output.toString());
            } else {
                logger.debug(String.valueOf(exitVal));
            }

        } catch (Exception e) {
            return e.getClass().getName();
        }
        return output.toString();
    }

    public Map<String, String> getServerInfo() {
        Map<String, String> map = new TreeMap<String, String>();

        try {

            map.put(getLabel("Hostname"), getServerHost());
            map.put(getLabel("Available Processors"), String.valueOf(Runtime.getRuntime().availableProcessors()) + " cores");
            map.put(getLabel("Free memory"), String.format("%6.4f", (double) Runtime.getRuntime().freeMemory() / GB) + " GB");

            long maxMemory = Runtime.getRuntime().maxMemory();

            map.put(getLabel("Maximum memory"),
                    (maxMemory == Long.MAX_VALUE ? "no limit" : String.format("%6.4f", (double) maxMemory / GB)) + " GB");
            map.put(getLabel("Total memory"), String.format("%6.4f", (double) Runtime.getRuntime().totalMemory() / GB) + " GB");

        } catch (Exception e) {
            map.put("Error", e.getClass().getName());
            logger.error(e);

        }

        try {

            String strOSName = System.getProperty("os.name");

            if (strOSName != null)
                map.put(getLabel("OS"), strOSName);

            String strOSVersion = System.getProperty("os.version");

            if (strOSVersion != null)
                map.put(getLabel("OS Version"), strOSVersion);

            if (System.getenv() != null) {
                map.put(getLabel("Username"), System.getenv().get("USERNAME"));
                map.put(getLabel("Profile"), System.getenv().get("USERPROFILE"));
            }

            map.put("user.country", System.getProperty("user.country"));
            map.put("user.dir", System.getProperty("user.dir"));
            map.put("user.home", System.getProperty("user.home"));
            map.put("user.language", System.getProperty("user.language"));

            String strJavaVersion = System.getProperty("java.specification.version");
            map.put(getLabel("JVM Spec"), strJavaVersion);

        } catch (Exception e) {
            map.put("Error", e.getClass().getName() + (e.getMessage() != null ? (". " + e.getMessage()) : ""));
            logger.error(e);

        }

        return map;

    }

    @Override
    public String getInlineHelpDir() {

        if (inline_help_dir != null)
            return inline_help_dir;

        try {
            File folder = new ClassPathResource("help").getFile();
            inline_help_dir = folder.getAbsolutePath();
        } catch (Exception e) {
            inline_help_dir = new File("").getAbsolutePath();
        }

        logger.debug(inline_help_dir);
        return inline_help_dir;

    }

    @Override
    public List<Tuple> serversInfo() {

        List<Tuple> data = new ArrayList<Tuple>();
        long start = System.currentTimeMillis();

        try {
            data.add(new Tuple("App Version", ServiceLocator.getService(BrandingService.class).getApplicationVersion()));

            OffsetDateTime date_started = ServiceLocator.getService(AppMonitoringService.class).getDateAppStarted();

            data.add(new Tuple("App Started", ServiceLocator.getService(DateTimeService.class).timeElapsed(date_started)));

            data.add(new Tuple("Web App host", getServerHost()));
            data.add(new Tuple("Web App OS", System.getenv("OS") != null ? System.getenv("OS") : ""));

            if (PropertiesFactory.getInstance("kbee").getProperties().getProperty("vanity-server") != null) {
                data.add(new Tuple("Vanity server mask",
                        PropertiesFactory.getInstance("kbee").getProperties().getProperty("vanity-server", "")));
            }

            data.add(new Tuple("Database", database));
            data.add(new Tuple("SolR",
                    "<a href=\"" + PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.url", "").trim()
                            + "\" target=\"_blank\">"
                            + PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.url", "").trim() + "</a>"));

            data.add(new Tuple("solr.content-core",
                    PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.content-core", "").trim()));
            data.add(new Tuple("solr.file-core",
                    PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.file-core", "") != null
                            ? PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.file-core", "").trim()
                            : ""));

            {
                int n = 1;
                for (String s : getMinioList()) {
                    data.add(new Tuple("Minio_" + String.valueOf(n++), s));
                }
            }
            {
                int n = 1;
                for (String s : getOdilonList()) {
                    data.add(new Tuple("Odilon_" + String.valueOf(n++), s));
                }
            }

            try {
                FileServerS3 s3 = ServiceLocator.getService(FileServerS3.class);
                if (s3 != null) {
                    if (s3.isEnabled())
                        data.add(new Tuple("Amazon S3", s3.getEnvironment()));
                    else
                        data.add(new Tuple("Amazon S3", getLabel("disabled")));
                } else {
                    data.add(new Tuple("Amazon S3", "not installed"));
                }
            } catch (Exception e) {
                data.add(new Tuple("Amazon S3", e.getClass().getName()));
                logger.error(e);
            }
        } catch (Exception e) {
            data.add(new Tuple("Error ", e.getClass().getName() + (e.getMessage() != null ? (". " + e.getMessage()) : "")));
            logger.error(e);

        } finally {
            long end = System.currentTimeMillis();
            if (logger.isDebugEnabled())
                data.add(new Tuple("Render time ", String.valueOf(end - start) + " <span class=\"ago\">ms</span>"));
            logger.debug("Render time " + String.valueOf(end - start) + "ms");
        }
        return data;
    }

    private String getLabel(String string) {
        return string;
    }

    /**
     * 
     * @return
     */
    public List<Tuple> schedulerInfo() {

        long start = System.currentTimeMillis();

        List<Tuple> data = new ArrayList<Tuple>();
        SchedulerService service = ServiceLocator.getService(SchedulerService.class);

        data.add(new Tuple("Info page",
                "<a class=\"btn-link\"  href=\"/datamanagement/scheduler\"target=\"_blank\"> /datamanagement/scheduler</a>"));

        try {

            String pg = service.getStatus();
            if (pg == null)
                pg = "err";
            boolean isok = pg.toLowerCase().equals("ok");
            String s = "<span class= \" " + (isok ? "success" : "danger") + "\" />" + pg + "</span>";
            data.add(new Tuple("Scheduler Engine status ", s));
        } catch (Exception e) {
            data.add(new Tuple("Scheduler Engine status. ", e.getClass().getName()));
            logger.error(e);
        }

        try {
            data.add(new Tuple("Scheduler. Queue in db (Std Err)", NumberFormatter.formatNumber(service.getQueueSize())
                    + "<span class=\"separator\">|</span>" + String.valueOf(service.getErrorQueueSize())));
            data.add(
                    new Tuple("Scheduler. Total items (Batches | Requests)", NumberFormatter.formatNumber(service.getTotalBatches())
                            + "<span class=\"separator\">|</span>" + String.valueOf(service.getTotalInBatches()))); // total batches
                                                                                                                    // in the system
        } catch (Exception e) {
            data.add(new Tuple("Scheduler. Queue Std Size", e.getClass().getName() + " | " + e.getMessage()));
        }

        try {
            double diff1 = service.getOneMinuteInputRateHp() - service.getOneMinuteThroughPutHP();
            double diff5 = service.getFiveMinuteInputRateHp() - service.getFiveMinuteThroughPutHP();
            double diff15 = service.getFifteenMinuteInputRateHp() - service.getFifteenMinuteThroughPutHP();

            double diffthp = (diff1 > 0 ? diff1 : 0) + (diff5 > 0 ? diff5 : 0) + (diff15 > 0 ? diff15 : 0);

            String cs_hp;

            if (diffthp > 1.15)
                cs_hp = "warning";
            else if (diffthp > 1.4)
                cs_hp = "danger";
            else
                cs_hp = "stack";

            double diff1l = service.getOneMinuteInputRateLp() - service.getOneMinuteThroughPutLP();
            double diff5l = service.getFiveMinuteInputRateLp() - service.getFiveMinuteThroughPutLP();
            double diff15l = service.getFifteenMinuteInputRateLp() - service.getFifteenMinuteThroughPutLP();

            double difftlp = (diff1l > 0 ? diff1l : 0) + (diff5l > 0 ? diff5l : 0) + (diff15l > 0 ? diff15l : 0);

            String cs_lp;

            if (difftlp > 1.15)
                cs_lp = "warning";
            else if (difftlp > 1.5)
                cs_lp = "danger";
            else
                cs_lp = "stack";

            String v1a = NumberFormatter.formatNumber(service.getOneMinuteInputRateHp()).trim();
            String v1b = NumberFormatter.formatNumber(service.getOneMinuteThroughPutHP()).trim();

            String v2a = NumberFormatter.formatNumber(service.getFiveMinuteInputRateHp()).trim();
            String v2b = NumberFormatter.formatNumber(service.getFiveMinuteThroughPutHP()).trim();

            String v3a = NumberFormatter.formatNumber(service.getFifteenMinuteInputRateHp()).trim();
            String v3b = NumberFormatter.formatNumber(service.getFifteenMinuteThroughPutHP()).trim();

            String rate_hp = "<div class=\"" + cs_hp + "\"> <b>&nbsp;1m.&nbsp;</b>  " + v1a
                    + "<span class=\"internal-separator\">/</span>" + v1b + "<span class=\"separator\">|</span></div>"
                    + "<div class=\"" + cs_hp + "\"> <b>&nbsp;5m.&nbsp;</b>  " + v2a + "<span class=\"internal-separator\">/</span>"
                    + v2b + "<span class=\"separator\">|</span></div>" + "<div class=\"" + cs_hp + "\"> <b>15m.&nbsp;</b> " + v3a
                    + "<span class=\"internal-separator\">/</span>" + v3b + "</div>";

            String vl1a = NumberFormatter.formatNumber(service.getOneMinuteInputRateLp()).trim();
            String vl1b = NumberFormatter.formatNumber(service.getOneMinuteThroughPutLP()).trim();

            String vl2a = NumberFormatter.formatNumber(service.getFiveMinuteInputRateLp()).trim();
            String vl2b = NumberFormatter.formatNumber(service.getFiveMinuteThroughPutLP()).trim();

            String vl3a = NumberFormatter.formatNumber(service.getFifteenMinuteInputRateLp()).trim();
            String vl3b = NumberFormatter.formatNumber(service.getFifteenMinuteThroughPutLP()).trim();

            String rate_lp = "<div class=\"" + cs_lp + "\"> <b>&nbsp;1m.&nbsp;</b> " + vl1a
                    + "<span class=\"internal-separator\">/</span>" + vl1b + "<span class=\"separator\">|</span></div>"
                    + "<div class=\"" + cs_lp + "\"> <b>&nbsp;5m.&nbsp;</b> " + vl2a + "<span class=\"internal-separator\">/</span>"
                    + vl2b + "<span class=\"separator\">|</span></div>" + "<div class=\"" + cs_lp + "\"> <b>15m.&nbsp;</b>" + vl3a
                    + "<span class=\"internal-separator\">/</span>" + vl3b + "</div>";

            data.add(new Tuple("Scheduler HP I/O req/sec (1m 5m 15m) ", rate_hp));
            data.add(new Tuple("Scheduler LP I/O req/sec (1m 5m 15m) ", rate_lp));

            String mean_rate_i_hp = NumberFormatter.formatNumber(service.getMeanHPIn(), getLocale())
                    + " <span class=\"atright ago\">req/sec</span>";
            String mean_rate_i_lp = NumberFormatter.formatNumber(service.getMeanLPIn(), getLocale())
                    + " <span class=\"atright ago\">req/sec</span>";
            String mean_rate_o_hp = NumberFormatter.formatNumber(service.getMeanHPOut(), getLocale())
                    + " <span class=\"atright ago\">req/sec</span>";
            String mean_rate_o_lp = NumberFormatter.formatNumber(service.getMeanLPOut(), getLocale())
                    + " <span class=\"atright ago\">req/sec</span>";

            data.add(new Tuple("Scheduler HP In mean rate ", mean_rate_i_hp));
            data.add(new Tuple("Scheduler LP In mean rate ", mean_rate_i_lp));

            data.add(new Tuple("Scheduler HP Out mean rate ", mean_rate_o_hp));
            data.add(new Tuple("Scheduler LP Out mean rate ", mean_rate_o_lp));

        } catch (Exception e) {
            data.add(new Tuple("Scheduler Engine status. ",
                    e.getClass().getName() + (e.getMessage() != null ? (". " + e.getMessage()) : "")));
            logger.error(e);

        } finally {
            long end = System.currentTimeMillis();
            if (logger.isDebugEnabled())
                data.add(new Tuple("Render time ", String.valueOf(end - start) + " <span class=\"ago\">ms</span>"));
            logger.debug("Render time " + String.valueOf(end - start) + " ms");
        }

        return data;
    }

    /***
     * 
     * ping.enabled = yes | no (execute the ping commmand) <br />
     * ping.notify = yes | no (notify if errrors by email) <br />
     * ping.email = email to send Ping error <br />
     * ping.cpu.threshold = 5 ping.ok = OK
     * 
     */
    public List<Tuple> pingInfo() {

        List<Tuple> data = new ArrayList<Tuple>();
        try {
            boolean ping_enabled = getContentDao().findSystemParameterValueByKey("ping.enabled", "yes").toLowerCase().trim()
                    .equals("yes");
            data.add(new Tuple(getLabel("Ping Enabled"), ping_enabled ? "yes" : "no"));
            boolean notify_ping = getContentDao().findSystemParameterValueByKey("ping.notify", "yes").toLowerCase().trim()
                    .equals("yes");
            data.add(new Tuple(getLabel("Ping page"), "<a href=\"/ping\" class=\"btn-link\" target=\"_blank\">/ping</a>"));
            data.add(new Tuple(getLabel("Ping notify"), notify_ping ? "yes" : "no"));

            String email = getContentDao().findSystemParameterValueByKey("ping.email", "null").toLowerCase().trim();
            data.add(new Tuple(getLabel("Ping email"), email));
            data.add(new Tuple(getLabel("Ping Page OK message"), _OK));

        } catch (Exception e) {
            logger.error(e);
            data.add(new Tuple("Ping Error", e.getClass().getName() + (e.getMessage() != null ? (". " + e.getMessage()) : "")));
        }
        return data;
    }

    /***
     * 
     * 
     */
    public List<Tuple> infrastructureInfo() {

        List<Tuple> data = new ArrayList<Tuple>();
        try {
            data.add(new Tuple(getLabel("Available Processors"),
                    String.valueOf(Runtime.getRuntime().availableProcessors()) + " cores"));

            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            data.add(new Tuple(getLabel("Load Average"),
                    os.getSystemLoadAverage() > 0 ? String.valueOf(os.getSystemLoadAverage()) : "n/a"));
            data.add(new Tuple(getLabel("Free memory"),
                    String.format("%6.4f", (double) Runtime.getRuntime().freeMemory() / GB) + " GB"));

            long maxMemory = Runtime.getRuntime().maxMemory();

            data.add(new Tuple(getLabel("Maximum memory"),
                    (maxMemory == Long.MAX_VALUE ? "no limit" : String.format("%6.4f", (double) maxMemory / GB)) + " GB"));
            data.add(new Tuple(getLabel("Total memory"),
                    String.format("%6.4f", (double) Runtime.getRuntime().totalMemory() / GB) + " GB"));
            String strJavaVersion = System.getProperty("java.specification.version");

            data.add(new Tuple(getLabel("JVM Spec"), strJavaVersion));

        } catch (Exception e) {
            logger.error(e);
            data.add(new Tuple("Infrastructure Error",
                    e.getClass().getName() + (e.getMessage() != null ? (". " + e.getMessage()) : "")));
        }
        return data;
    }

    private List<String> getOdilonList() {

        List<String> list = new ArrayList<String>();

        boolean odilon_enabled = PropertiesFactory.getInstance("kbee").getProperties().getProperty("odilon.enabled", "yes")
                .toLowerCase().trim().equals("yes");

        if (odilon_enabled) {
            FileServerOdilon fsv2 = ServiceLocator.getService(FileServerOdilon.class);
            if (fsv2 instanceof KbeeShardedOdilonFileServer) {
                try {
                    for (Entry<Integer, FileServerOdilon> entry : ((KbeeShardedOdilonFileServer) fsv2).getShards().entrySet()) {
                        list.add("<a class=\"btn-link\" target=\"_blank\" href="+entry.getValue().getEndPoint()+">"+entry.getValue().getEndPoint()+"</a>");
                    }
                } catch (Exception e) {
                    logger.error(e);
                    list.add(e.getClass().getName());
                }
            } else {
                list.add("<a class=\"btn-link\" target=\"_blank\" href="+fsv2.getEndPoint()+">"+fsv2.getEndPoint()+"</a>");
            }
        } else
            list.add("Odilon " + getLabel("disabled"));
        return list;

    }

    private List<String> getMinioList() {

        List<String> list = new ArrayList<String>();

        boolean kbfs2_enabled = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.enabled", "yes")
                .toLowerCase().trim().equals("yes");

        if (kbfs2_enabled) {
            FileServerMinio fsv2 = ServiceLocator.getService(FileServerMinio.class);
            if (fsv2 instanceof KbeeShardedMinioFileServer) {
                try {
                    for (Entry<Integer, FileServerMinio> entry : ((KbeeShardedMinioFileServer) fsv2).getShards().entrySet()) {
                        list.add("<a class=\"btn-link\" target=\"_blank\" href="+entry.getValue().getEndPoint()+">"+entry.getValue().getEndPoint()+"</a>");
                    }
                } catch (Exception e) {
                    logger.error(e);
                    list.add(e.getClass().getName());
                }
            } else {
                list.add("<a class=\"btn-link\" target=\"_blank\" href="+fsv2.getEndPoint()+">"+fsv2.getEndPoint()+"</a>");
            }
        } else
            list.add("Minio " + getLabel("disabled"));

        return list;
    }

    protected Locale getLocale() {
        User user = getSessionUser();
        return user != null ? user.getLocale() : Locale.getDefault();

    }

    protected User getSessionUser() {
        try {
            return ServiceLocator.getService(SecurityService.class).getSessionUser();
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    /**
     *
     */
    @Override
    public String getJettyPort() {

        if (port != null)
            return port;

        for (Tuple t : systemEnv()) {
            if (t.getLabel().equals("jetty.port")) {
                port = t.getValue();
                return port;
            }
        }
        port = PropertiesFactory.getInstance("kbee").getProperties().getProperty("port", "").trim();
        return port;
    }

    private List<Tuple> systemEnv() {
        return dumpVars(System.getenv());
    }

    /***
     * 
     */
    private List<Tuple> dumpVars(Map<String, ?> m) {
        List<Tuple> list = new ArrayList<Tuple>(m.size());
        List<String> keys = new ArrayList<String>(m.keySet());
        for (String k : keys) {
            list.add(new Tuple(k, m.get(k).toString()));
        }
        return list;
    }

    private ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

}
