package kbee.web.application;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.IDoc;
import com.novamens.content.service.DomainLifeCycleService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.service.UserImagesService;
import com.novamens.content.web.admin.api.APIRequestsReportPage;
import com.novamens.content.web.admin.api.APISOAPReportPage;
import com.novamens.content.web.admin.api.APIStatsReportPage;
import com.novamens.content.web.admin.markup.*;
import com.novamens.content.web.admin.markup.datamanagement.DatabaseExportPage;
import com.novamens.content.web.admin.markup.datamanagement.SystemDataManagementGeneralPage;
import com.novamens.content.web.admin.markup.datamanagement.SystemSchedulerMonitorPage;
import com.novamens.content.web.console.audit.markup.AuditContentPage;
import com.novamens.content.web.console.markup.*;
import com.novamens.content.web.content.classify.markup.BatchClassifyPage;
import com.novamens.content.web.integration.FileSystemIntegrationPage;
import com.novamens.content.web.integration.FileUploadPage;
import com.novamens.content.web.report.markup.ReportSubscriptionPage;
import com.novamens.content.web.security.login.LoginSimplePage;
import com.novamens.content.web.security.markup.*;

import com.novamens.content.web.workflow.markup.batch.TaskBatchCreatePage;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.ReindexCommand;
import com.novamens.kbee.service.KbeeWebSessionService;
import com.novamens.kbee.vault.VaultService;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.FileServerS3;
import com.novamens.portal6.model.PortalLiteralsService;
import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.*;
import com.novamens.spring.service.SpringServiceLocator;
import com.novamens.wicket.converter.CronConverter;
import com.novamens.wicket.protocol.http.SpringWebApplication;
import com.novamens.wicket.util.DummyBlockPanel;

import jp.try0.wicket.toastr.core.ToastOptions;
import jp.try0.wicket.toastr.core.config.ToastrSettings;

import kbee.payment.MercadoPagoPaymentService;

import kbee.util.PropertiesFactory;
import kbee.util.Tuple;
import kbee.util.logging.Logger;
import kbee.web.alert.BillboardsPage;
import kbee.web.command.panel.CommandPage;
import kbee.web.command.panel.CommandsPage;
import kbee.web.content.console.ArchivePage;
import kbee.web.content.console.AuditResourcesPage;
import kbee.web.content.console.ContentBaseTemplatesPage;
import kbee.web.content.console.PendingTasksPage;
import kbee.web.content.console.RecycleBinPage;
import kbee.web.dashboard.DashboardPortalLibraryContentsPanel;
import kbee.web.dashboard.DashboardPortalSavedQueryWidgetPanel;
import kbee.web.dashboard.DashboardEntityPage;
import kbee.web.dashboard.DashboardFactoryHomePage;
import kbee.web.dashboard.DashboardHomePage;
import kbee.web.dashboard.DashboardLibraryWidgetPanel;
import kbee.web.dashboard.DashboardMonitorTasksWidgetPanel;
import kbee.web.dashboard.DashboardMyTasksWidgetPanel;
import kbee.web.dashboard.DashboardPendingTasksWidgetPanel;
import kbee.web.datamanagement.ReindexPage;
import kbee.web.datamanagement.SchedulerRequestPage;
import kbee.web.datamanagement.TagManagementPage;
import kbee.web.datamanagement.ThumbnailServicePage;
import kbee.web.dataset.*;
import kbee.web.domain.DomainCreationPage2;
import kbee.web.eform.EFormPrintPage;
import kbee.web.emailtemplate.EmailTemplatePage;
import kbee.web.emailtemplate.EmailTemplatesPage;
import kbee.web.enoti.ENotiRulePage;
import kbee.web.enoti.ENotiRulesPage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.SessionExpiredErrorPage;
import kbee.web.form.ContentUploadHandler;
import kbee.web.form.FormUploadHandler;
import kbee.web.form.UploadHandler;
import kbee.web.form.VersionUploadHandler;
import kbee.web.idoc.IDocPageV6;
import kbee.web.idoc.IDocTaskPageV5;
import kbee.web.idoc.IDocTaskPageV6;
import kbee.web.idoc.IDocTextViewerPage;
import kbee.web.idoc.SharedFormPage;
import kbee.web.idoc.SharedPage;
import kbee.web.idoc.SharedSignaturePage;
import kbee.web.idoc.SharedTaskPage;
import kbee.web.library.LibrariesPage;
import kbee.web.library.LibraryPage;
import kbee.web.media.StandAlonePlayerPage;
import kbee.web.model.AttributeModelPage;
import kbee.web.model.ClassifierModelPage;
import kbee.web.model.DashboardInformationModelPage;
import kbee.web.model.DataSetPage;
import kbee.web.model.ResourceTagPage;
import kbee.web.model.contentclass.ContentTemplatePage;
import kbee.web.model.eform.EFormPage;
import kbee.web.model.procedure.LauncherGroupPage;
import kbee.web.model.procedure.ProcedurePage;
import kbee.web.model.procedure.TaskConfigurationPage;
import kbee.web.multidimensional.FacetPage;
import kbee.web.multidimensional.FacetsPage;
import kbee.web.nav.DonePage;
import kbee.web.notes.UserNotesPage;
import kbee.web.notification.UserNotificationsPage;
import kbee.web.notes.BillboardPage;
import kbee.web.objectstorage.ObjectStoragePage;
import kbee.web.payment.PaymentDetailsPage;
import kbee.web.payment.PaymentsConsolePage;
import kbee.web.portal.dataprovider.PortalBlockListViewDataProvider;
import kbee.web.portal.dataprovider.PortalBlockTextDataProvider;
import kbee.web.portal.dataprovider.PortalContentListDataProvider;
import kbee.web.portal.library.PortalBlockListContentPanel;
import kbee.web.portal.library.PortalBlockListViewPanel;
import kbee.web.portal6.ExternalSiteEditorPage;
import kbee.web.portal6.PortalMVCService;
import kbee.web.portal6.SitesPage;
import kbee.web.portal6.editor.PortalPageEditorPage;
import kbee.web.portal6.editor.PortalPageStructureEditorPage;
import kbee.web.portal6.editor.PortalSiteEditorPage;
import kbee.web.portal6.library.PortalSimpleTextPanel;
import kbee.web.registration.DeviceRegistrationPage;
import kbee.web.registration.RegistrationPage;
import kbee.web.registration.RegistrationSetupPage;
import kbee.web.report.ReportPageV5;
import kbee.web.report.ReportsHomePage;
import kbee.web.resource.WebResourceReferenceMapper;
import kbee.web.rule.ActionRulePage;
import kbee.web.rule.ActionRulesPage;
import kbee.web.rule.EntityRulePage;
import kbee.web.scheduler.SchedulerCronJobsPage;
import kbee.web.searcher.PortalSearchForm;
import kbee.web.searcher.editor.SearcherSiteEditorPage;
import kbee.web.searcher.page.SearcherUserListPage;
import kbee.web.security.AclPage;
import kbee.web.security.SecuredMemberAclPage;
import kbee.web.security.role.RolePage;
import kbee.web.security.role.RolesPage;
import kbee.web.security.user.MyAccountPage;
import kbee.web.security.user.NewUserPage;
import kbee.web.security.user.UserBulkCreationPage;
import kbee.web.security.user.UserPage;

import kbee.web.service.PortalUrlMapperService;
import kbee.web.source.SourcePage;
import kbee.web.source.SourcesPage;
import kbee.web.support.ReportIssuePage;
import kbee.web.uploader.TusUploadHandlerPage;
import kbee.web.workflow.EFormViewerPage;
import kbee.web.workflow.ResolutionLetterViewPage;
import net.ftlines.wicketsource.WicketSource;
import org.apache.wicket.*;
import org.apache.wicket.coop.CrossOriginOpenerPolicyConfiguration;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.markup.html.pages.AccessDeniedPage;
import org.apache.wicket.markup.html.pages.InternalErrorPage;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.version.StaticResourceVersion;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public abstract class BaseWebApplication extends SpringWebApplication {

    static private final String TREE_RESOURCES = PropertiesFactory.getInstance("kbee").getProperties()
            .getProperty("tree-resources", "no").trim();
    static private final String SERVERID = PropertiesFactory.getInstance("kbee").getProperties().getProperty("server.id", "")
            .trim();

    static private kbee.util.logging.Logger logger = Logger.getLogger("StartupLogger");
    private static kbee.util.logging.Logger stdlogger = Logger.getLogger(BaseWebApplication.class.getName());

    static private final Properties props = PropertiesFactory.getInstance("kbee").getProperties();
    static private final String SOLR_DATA_DIR = PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.data",
            "solr");

    static final String kbee[] = { " _   __  _ _ _   _ _ _   _ _ _  ", "| | / / |  _  \\ |  _ _| |  _ _| ",
            "| |/ /  | |_| | | |_    | |_    ", "|   \\   |  _  | |  _|   |  _|   ", "| |\\ \\  | |_| | | |_ _  | |_ _  ",
            "|_| \\_\\ |_ _ _/ |_ _ _| |_ _ _| " };

    private boolean maintenance = false;

    private String PORT;

    private long starttime = System.currentTimeMillis();
    
    
    
    

    public boolean maintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean value) {
        maintenance = value;
    }

    public ApplicationContext getApplicationContext() {
        return WebApplicationContextUtils.getRequiredWebApplicationContext((ServletContext) this.getServletContext());
    }

    public Supplier<IExceptionMapper> getExceptionMapperProvider() {
        return new Supplier<IExceptionMapper>() {
            @Override
            public IExceptionMapper get() {
                return new ExceptionMapper();
            }
        };
    }

    public long getStartTime() {
        return starttime;
    }

    /**
     * ---------------------------------------------------------------
     * 
     * 
     * <p>
     * Startup kbee Domain Import User Images Init User Root for Domain kbee Check
     * SolR indexes and rebuild if required
     * </p>
     * 
     */
    public void onStartup() {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {

                /* Roman legionaries used this when falling in battle */
                System.out.println("\n\nDulce et decorum est pro patria mori.\nShuting down... goodbye\n\n\n");
                stdlogger.info("Dulce et decorum est pro patria mori...Shuting down... goodbye\n\n\n");
                logger.debug("Dulce et decorum est pro patria mori...Shuting down... goodbye\n\n\n");
            }
        });

        boolean kbfs2_enabled = props.getProperty("kbfs2.enabled", "yes").toLowerCase().trim().equals("yes");
        boolean odilon_enabled = props.getProperty("odilon.enabled", "no").toLowerCase().trim().equals("yes");
        boolean vault_enabled = !props.getProperty("vault.url", "no").toLowerCase().trim().equals("no");

        logger.debug("Log4j2: " + System.getProperty("log4j.configurationFile"));
        logger.info("jdbc.url: " + props.getProperty("jdbc.url", "").trim());
        logger.debug("jdbc.username: " + props.getProperty("jdbc.username", "").trim() + " | "
                + props.getProperty("jdbc.password", "").trim());

        ServiceLocator.getService(ApplicationServerService.class).setWicketConfigurationType(
                getConfigurationType() == RuntimeConfigurationType.DEPLOYMENT ? "DEPLOYMENT" : "DEVELOPMENT");
        logger.debug(
                "Wicket configuration: " + ServiceLocator.getService(ApplicationServerService.class).getWicketConfigurationType());

        this.getDebugSettings().setDevelopmentUtilitiesEnabled(getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT);

        if (this.getDebugSettings().isDevelopmentUtilitiesEnabled())
            WicketSource.configure(this);

        KbeeWebSessionService kbeeWebSessionService = (KbeeWebSessionService) ServiceLocator.getService(WebSessionService.class);
        if (getApplicationContext().containsBean("sessionRegistry"))
            kbeeWebSessionService.setSessionRegistry((SessionRegistry) getApplicationContext().getBean("sessionRegistry"));

        getServletContext().addListener((EventListener) getApplicationContext().getBean("httpSessionEventPublisher"));

        String ping_db = pingDataBase();
        if (!(ping_db == null || ping_db.toLowerCase().trim().equals("ok"))) {
            logger.error(ping_db);
            logger.error("--------------------------------------------------");
            logger.error("KBEE can not run without a Database.");
            logger.error("Please check file -> kbee.properties");
            logger.error("--------------------------------------------------");
            System.exit(1);
        }

        if (SERVERID.length() < 1) {
            logger.info("Server id: ERROR - Please provide the parameter -> server.id");
        } else {
            logger.info("Server id: " + SERVERID);
        }

        FileServerS3 fss3 = ServiceLocator.getService(FileServerS3.class);
        boolean s3_enabled = (fss3 != null && fss3.isEnabled());

        logger.info("Minio Enabled: " + (kbfs2_enabled ? "yes" : "no"));
        logger.info("Odilon Enabled: " + (odilon_enabled ? "yes" : "no"));
        logger.info("Amazon S3 Enabled: " + (s3_enabled ? "yes" : "no"));

        
        // Vault
        // --------------------------------------------------------------------------------------------------------
        //

        if (vault_enabled) {
            try {

                String ping = ServiceLocator.getService(VaultService.class).ping();
                if (ping == null || ping.toLowerCase().equals("ok")) {
                    logger.info("Vault Status: " + ServiceLocator.getService(VaultService.class).ping());
                } else {
                    logger.error("-------------------------------------------------");
                    logger.error("Vault Status: " + ServiceLocator.getService(VaultService.class).ping());
                    logger.error("-------------------------------------------------");
                }

            } catch (Exception e) {
                logger.info("Vault Status: " + e.getClass().getName() + " | " + e.getMessage());
            }
        }

        // Odilon
        // --------------------------------------------------------------------------------------------------------
        //

        if (odilon_enabled) {

            FileServerOdilon fsodilon = ServiceLocator.getService(FileServerOdilon.class);

            if (fsodilon == null) {
                logger.error("--------------------------------------------------");
                logger.error("Odilon is enabled but there is no Odilon File Server.");
                logger.error("Please check file -> kbee.properties");
                logger.error("--------------------------------------------------\n\n");
                System.exit(1);
            }

            logger.info("Odilon Status: " + fsodilon.ping());

            if (fsodilon.ping() == null || !fsodilon.ping().equals("ok")) {
                logger.error("Odilon is enabled but the Odilon File Server is not working.");
                logger.error("Please check file -> kbee.properties");

            }
        }

        ServiceLocator.getService(MercadoPagoPaymentService.class).initialize();

        // --------------------------------------------------------------------------------------------------------
        //
        try {
            ServiceLocator.getService(ApplicationServerService.class).checkDirs();
            startIndexes();
            startUpUserImages();
            try {
                com.novamens.hibernate.session.Session.open();
                ServiceLocator.getService(DomainLifeCycleService.class).kbeeDomainStartUp();
                logger.debug("Kbee Domain Startup: done.");
            } catch (Exception e) {
                logger.error(e);
            } finally {
                com.novamens.hibernate.session.Session.close();
            }

            initKbeeRoot();

            for (String s : getAppCharacterName())
                logger.info(s);

            try {
                ServiceLocator.getService(SecurityContentMgmtService.class)
                        .startApplicationServer(System.currentTimeMillis() - this.getStartTime());
            } catch (Exception e) {
                logger.error(e);
            }
        } catch (Exception e) {
            logger.error(e);
        }

        if (getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT) {
            logger.info("----------------------------------");
            logger.info("");
            logger.info("WICKET IS IN DEVELOPEMENT MODE");
            logger.info("------------------------------");
            logger.info("");
            logger.info("This mode should not be used in production.");
            logger.info("For production use please change the value of the parameter");
            logger.info("wicketDebugEnabled in the kbee.properties file to:");
            logger.info("wicketDebugEnabled = false");
            logger.info("----------------------------------");
        }

        logger.debug("KBEE Factory Domain Startup.");

    }

    protected void addStartRequests() {

        try {
            com.novamens.hibernate.session.Session.open();
        } catch (Exception e) {
            logger.error(e);
        } finally {
            com.novamens.hibernate.session.Session.close();
        }

    }

    public RuntimeConfigurationType getConfigurationType() {
        String wicketDebugEnabled = PropertiesFactory.getInstance("kbee").getProperties().getProperty("wicketDebugEnabled");

        if (wicketDebugEnabled != null) {
            if (wicketDebugEnabled.equals("true") || wicketDebugEnabled.equals("yes"))
                return RuntimeConfigurationType.DEVELOPMENT;

            else
                return RuntimeConfigurationType.DEPLOYMENT;

            // if (wicketDebugEnabled.equals("false") || wicketDebugEnabled.equals("no"))
            // return RuntimeConfigurationType.DEPLOYMENT;
            // else
            // return RuntimeConfigurationType.DEVELOPMENT;
        }
        // return super.getConfigurationType();
        return RuntimeConfigurationType.DEPLOYMENT;
        // return RuntimeConfigurationType.DEVELOPMENT;
    }

    private String[] brand_char = null;

    public String[] getProductCursesName() {
        return kbee;
    }

    protected String[] getAppCharacterName() {

        if (brand_char != null)
            return brand_char;

        brand_char = new String[6];

        PORT = getJettyPort();

        String xkbee[] = getProductCursesName();
        brand_char[0] = xkbee[0] + "";
        brand_char[1] = xkbee[1] + "";
        brand_char[2] = xkbee[2] + "";
        brand_char[3] = xkbee[3] + "";
        brand_char[4] = xkbee[4] + "";
        brand_char[5] = xkbee[5] + " Version: " + BrandingService.VERSION;
        return brand_char;
    }

    private String pingDataBase() {
        try {
            com.novamens.hibernate.session.Session.open();
            return getContentDao().pingDataBase();

        } catch (Exception e) {
            logger.error(e);
            return e.getClass().getName() + " | " + e.getMessage();
        } finally {
            com.novamens.hibernate.session.Session.close();
        }

    }

    /**
     * @return
     */
    protected boolean isKbee() {
        return true;
    }

    /**
     * if SolR is standalone, we dont check for local indexes.
     */
    protected void startIndexes() {

        if (!isSolrCompiled())
            return;

        long start = System.currentTimeMillis();

        File solr_data = new File(SOLR_DATA_DIR); // el directorio solr esta cableado actualmente !

        boolean exists_data = false;
        boolean exists_audit = false;
        if (solr_data.exists() && solr_data.isDirectory()) {
            File files[] = solr_data.listFiles();
            if (files != null) {
                for (File fi : files) {
                    if (fi.getName().toLowerCase().equals("auditdata"))
                        exists_audit = true;
                    if (fi.getName().toLowerCase().equals("data"))
                        exists_data = true;
                }
            }
        }

        if (exists_audit && exists_data) {
            logger.info("SolR Directories: OK");
            return;
        } else {
            logger.info("SolR Data: " + (exists_data ? " ok" : "no"));
            logger.info("SolR Audit: " + (exists_audit ? " ok" : "no"));
        }

        OffsetDateTime date = OffsetDateTime.now().minusMonths(3);
        String year = String.valueOf(date.getYear());
        String month = String.valueOf(date.getMonth().getValue());

        String this_year = String.valueOf(OffsetDateTime.now().getYear());
        String range = "(year(lastModifiedDate)=" + this_year + " or  (year(lastModifiedDate)>= " + year
                + " and (month(lastModifiedDate)> " + month + " )))";

        try {
            logger.info("Indexing...");
            com.novamens.hibernate.session.Session.open();

            List<Domain> dos = getContentDao().getDomains();
            int counter = 0;
            for (Domain domain : dos) {

                logger.info("Indexing Domain: " + domain.getName());

                List<String> statements = new ArrayList<String>();

                if (!exists_data) {

                    /** Security **/
                    statements.add("from KbeeGroup where domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeeUser where domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeeRole where domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeeSecurityRule where domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeeENotiRule where domain.id=" + String.valueOf(domain.getId()));

                    // Model
                    statements.add("from KbeeDataSet where domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeeAttribute where domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeeClassifier where domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeeContentTemplate where domain.id=" + String.valueOf(domain.getId()));

                    // Dataset Values
                    statements.add("from KbeeDataSetMember where domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeePerson where domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeeUserLabel");

                    // Content
                    statements.add("from KbeeBillboard where domain.id=" + String.valueOf(domain.getId()));

                    statements.add("from KbeeIDoc where workspace>=0 and domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeeIDoc where (workspace is null or workspace<1) and " + range + " and domain.id="
                            + String.valueOf(domain.getId()));

                    statements.add("from KbeeTreeIDoc where workspace>=0 and domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeeTreeFile where workspace>=0 and domain.id=" + String.valueOf(domain.getId()));

                    statements
                            .add("from KbeeOrganizationalText where workspace>=0 and domain.id=" + String.valueOf(domain.getId()));
                    statements.add("from KbeeOrganizationalText where workspace is null and  domain.id="
                            + String.valueOf(domain.getId()) + " and " + range);

                    // Templates are not mapped ?
                    // statements.add("from KbeeEmailTemplate");

                    logger.info("Please note that we will index only Workspaces and the Contents modified for the last 3 months ");

                }

                // ----------
                //
                // if (!exists_audit) {
                // statements.add("from ObjectEvent where "+event_range + " and
                // domainId="+String.valueOf(domain.getId()));
                // statements.add("from SendEmailEvent where " + event_range + " and
                // domainId="+String.valueOf(domain.getId()));
                // logger.info("Please note that we will index Events modified from the start of
                // this year ");
                // }
                // ----------

                for (String statement : statements) {
                    ReindexCommand indexer = new ReindexCommand(statement, domain);
                    indexer.setIncludeAttachments(false);
                    logger.info("Indexing (without attachments): " + statement + " in " + domain.getName());
                    try {
                        indexer.execute();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }

                logger.info("End Domain: " + domain.getName() + "  [" + String.valueOf(++counter) + "/" + String.valueOf(dos.size())
                        + "]");
                logger.info("--------------------------------------------------------------");

                List<String> xst = new ArrayList<String>();
                xst.add("from KbeeDomain");
                xst.add("from  KbeeSite");
                // xst.add("from KbeePage");
                // xst.add("from KbeeArea");
                // xst.add("from KbeeBlock");
                // xst.add("from KbeeViewBK");
                // xst.add("from KbeeViewBKLink");
                // xst.add("from KbeeViewDetailContent");

                for (String statement : xst) {
                    ReindexCommand indexer = new ReindexCommand(statement);
                    indexer.setIncludeAttachments(false);
                    logger.info("Indexing (without attachments): " + statement);
                    try {
                        indexer.execute();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
        } finally {
            com.novamens.hibernate.session.Session.close();
            long end = System.currentTimeMillis();
            logger.info("Start Indexes: " + String.valueOf((end - start) / 1000.0) + " seg.");
        }
    }

    protected void init() {

        if (ServiceLocator.getInstance() == null)
            ServiceLocator.setInstance(new SpringServiceLocator("kbee"));

        getCspSettings().blocking().disabled();
        
        

        this.mountPage("/done", DonePage.class);

        this.mountPage("/reports", ReportsHomePage.class);
        this.mountPage("/reports/subscriptions", ReportSubscriptionPage.class);
        this.mountPage("/reports/${reportgroup}/${key}", ReportPageV5.class);

        // ErrorPage and Support
        // ------------------------------------------------------------------------------
        //
        this.mountPage("/error", ApplicationErrorPage.class);
        this.mountPage("/support", ReportIssuePage.class);

        // Rules
        // ---.-------------------------------------------------------------------------------
        //
        this.mountPage("/actionrules", ActionRulesPage.class);
        this.mountPage("/actionrule/${id}", ActionRulePage.class);

        // Admin Pages
        // -----------------------------------------------------------------------------
        //
        this.mountPage("/systeminfo/keymetrics", SystemInfoPage.class);
        this.mountPage("/systeminfo/parameters", SystemParametersPage.class);
        this.mountPage("/systeminfo/${id}", SystemInfoGeneralPage.class);

        this.mountPage("/ping", PingPage.class);
        this.mountPage("/maintenance", MaintenancePage.class);

        // DataManagement
        // -------------------------------------------------------------------------
        //
        this.mountPage("/datamanagement/tagtool", TagManagementPage.class);

        this.mountPage("/datamanagement/${id}", SystemDataManagementGeneralPage.class);
        this.mountPage("/datamanagement/reindex", ReindexPage.class);

        this.mountPage("/datamanagement/scheduler", SystemSchedulerMonitorPage.class);
        this.mountPage("/datamanagement/scheduler/request", SchedulerRequestPage.class);
        this.mountPage("/datamanagement/scheduler/cronjobs", SchedulerCronJobsPage.class);
        this.mountPage("/datamanagement/objectstorage", ObjectStoragePage.class);
        this.mountPage("/datamanagement/cache", ThumbnailServicePage.class);

        this.mountPage("/fileserver", FileSystemIntegrationPage.class);
        this.mountPage("/fileserver/upload", FileUploadPage.class);

        // LOGIN PAGE CAN NO BE INSTANTIATED THIS WAY ??? this.mountPage("/login",
        // getLoginPageClass());

        
        //Object o = ServiceLocator.getService(BeansService.class).getBean("loginpage");

        //if (o!=null)
        //    this.mountPage("/login", (java.lang.Class<Page>)  o.getClass());
        //else
        //	this.mountPage("/login", LoginSimplePage.class);


        
        
        
        
        // TODO AT ------------------------------------------------
        mount(new MountedBeanMapper(
        	"/login", 
        	new PageBeanResolver("login-page",LoginSimplePage.class)));
        //this.mountPage("/forgotpassword", AAForgotPasswordPage.class);
        this.mountPage("/forgotusername", ForgotUsernamePage.class);
        this.mountPage("/passwordrecovery", PasswordRecoveryPage2.class);
        
        // Content -----
        // ---------------------------------------------------------------------------
        this.mountPage("/content", kbee.web.content.console.ContentBasePage.class);
        this.mountPage("/content/${library}", kbee.web.content.console.ContentBasePage.class);

        this.mountPage("/mydocuments", kbee.web.content.console.MyDocumentsPage.class);

        this.mountPage("/dbexport", DatabaseExportPage.class);

        this.mountPage("/" + IDoc.CLASS_CODE + "/${oid}/#{ver}/#{id}", IDocPageV6.class);
        this.mountPage("/" + "text" + "/${oid}/#{ver}/#{id}", IDocTextViewerPage.class);
     
        this.mountPage("/shared/${token}", SharedPage.class);
        this.mountPage("/sharedform/${token}", SharedFormPage.class);
        this.mountPage("/sharedsignature/${user}/${device}", SharedSignaturePage.class);

        this.mountPage("/registrationinit/${token}", RegistrationSetupPage.class);
        this.mountPage("/registration/${token}", RegistrationPage.class);
        this.mountPage("/registrationdevice/${token}", DeviceRegistrationPage.class);

        this.mountPage("/archive", ArchivePage.class);

        this.mountPage("/explorer", kbee.web.content.console.TreeExplorerPage.class);

        // Resources-------
        // ------------------------------------------------------------------------
        //
        this.mountPage("/upload", UploadHandler.class);
        this.mountPage("/versionupload", VersionUploadHandler.class);
        this.mountPage("/contentupload", ContentUploadHandler.class);
        this.mountPage("/formupload", FormUploadHandler.class);
        this.mountPage("/api/upload/complete", TusUploadHandlerPage.class);
        this.mountPage("/viewer", StandAlonePlayerPage.class);

        // Dashboard
        // -----------------------------------------------------------------------------------
        this.mountPage("/myhome", DashboardHomePage.class);
        this.mountPage("/factoryhome", DashboardFactoryHomePage.class);
        this.mountPage("/entityhome/${entity_id}/${classifier_id}", DashboardEntityPage.class);

        // Tasks
        // -----------------------------------------------------------------------------------
        this.mountPage("/mytasks", kbee.web.content.console.WorkspacePage.class);
        this.mountPage("/mydrafts", kbee.web.content.console.MyResourcesPage.class);

        this.mountPage("/publicdrafts", kbee.web.content.console.PublicResourcesPage.class);

        this.mountPage("/task/" + IDoc.CLASS_CODE + "/${task}/${content}", IDocTaskPageV5.class);
        this.mountPage("/task/" + IDoc.CLASS_CODE + "/v6/${task}/${content}", IDocTaskPageV6.class);
        this.mountPage("/sharedtask/${token}", SharedTaskPage.class);

        this.mountPage("/mytasks/uploadandcreate", TaskBatchCreatePage.class);
        this.mountPage("/mytasks/bulkclassify", BatchClassifyPage.class);
        this.mountPage("/pendingtasks", PendingTasksPage.class);

        this.mountPage("/mynotepad", UserNotesPage.class);
        this.mountPage("/notepad/${id}", UserNotesPage.class);
        this.mountPage("/mynotifications", UserNotificationsPage.class);

        // Monitor
        // --------------------------------------------------------------------------------
        this.mountPage("/monitor", kbee.web.content.console.MonitorPage.class);
        this.mountPage("/monitor/dashboard", com.novamens.content.web.console.markup.DashboardPage.class);
   
        // Alerts
        // ---------------------------------------------------------------------------------
        this.mountPage("/billboards", BillboardsPage.class);
        this.mountPage("/billboards/all", BillboardsPage.class);
        this.mountPage("/billboards/${id}", BillboardPage.class);

        this.mountPage("/recyclebin", RecycleBinPage.class);
        this.mountPage("/templates", ContentBaseTemplatesPage.class); // ver si sirve ??

        // DataSet Values
        // -------------------------------------------------------------------------
        this.mountPage("/dataset/${id}/${memberid}", MemberPage.class);
        this.mountPage("/dataset/members/recurrent/${ruleid}", EntityRulePage.class);

        this.mountPage("/dataset/bulkcreationt/${id}", MemberBulkCreationPage.class);

        this.mountPage("/datasetmembers", DashboardDataSetMembersHomePage.class);

        this.mountPage("/dataset/${id}", DataSetMembersPage.class);
        this.mountPage("/dataset/batchcreate/${id}", MemberBatchCreationPageV5.class);

        // EForms
        // -----------------------------------------------------------------------------------
        this.mountPage("/eform/${template}/${eform}", EFormPage.class);
        this.mountPage("/eform/print/${eform}", EFormPrintPage.class);

        // General Domain Info
        // --------------------------------------------------------------------
        //
        this.mountPage("/domain/${id}/settings", kbee.web.domain.DomainPage.class);
        this.mountPage("/domain/settings", kbee.web.domain.DomainPage.class);

        // Information Model
        // ----------------------------------------------------------------------
        //

        this.mountPage("/model", DashboardInformationModelPage.class);
        // this.mountPage("/model/home", InformationModelHomePage.class);

        this.mountPage("/model/datasets", kbee.web.model.DataSetsPage.class);
        this.mountPage("/model/datasets/${id}", DataSetPage.class);

        this.mountPage("/model/classifiers", kbee.web.model.ClassifiersPage.class);
        this.mountPage("/model/classifiers/${id}", ClassifierModelPage.class);

        this.mountPage("/model/attributes", kbee.web.model.AttributesPage.class);
        this.mountPage("/model/attributes/${id}", AttributeModelPage.class);

        this.mountPage("/model/contentclasses", kbee.web.model.contentclass.ContentTemplatesPage.class);
        this.mountPage("/model/contentclass/${id}", ContentTemplatePage.class);

        this.mountPage("/model/resourcetags", kbee.web.model.ResourceTagsPage.class);
        this.mountPage("/model/resourcetag/${id}", ResourceTagPage.class);

        this.mountPage("/model/launchergroups", kbee.web.model.procedure.LauncherGroupsPage.class);
        this.mountPage("/model/launchergroup/${id}", LauncherGroupPage.class);

        this.mountPage("/model/procedure/${id}", ProcedurePage.class);
        this.mountPage("/model/task/${procedure}/${task}", TaskConfigurationPage.class);

        // Libraries
        // ------------------------------------------------------------------------------
        //
        this.mountPage("/libraries", LibrariesPage.class);
        this.mountPage("/libraries/${id}", LibraryPage.class);

        // Sources
        // --------------------------------------------------------------------------------
        //
        this.mountPage("/sources", SourcesPage.class);
        this.mountPage("/sources/${id}", SourcePage.class);

        // email templates
        // -----------------------------------------------------------------------
        //
        this.mountPage("/emailtemplates", EmailTemplatesPage.class);
        this.mountPage("/emailtemplates/${lg}/${key}", EmailTemplatePage.class);

       
        this.mountPage("/audit/${activityid}/${ename}", EFormViewerPage.class);
        this.mountPage("/audit/${activityid}/resolution", ResolutionLetterViewPage.class);

        // Security
        // -------------------------------------------------------------------------------
        //
        this.mountPage("/security/users", kbee.web.security.user.UsersPage.class);
        this.mountPage("/security/newuser", NewUserPage.class);
        this.mountPage("/security/users/bulkcreation", UserBulkCreationPage.class);
        this.mountPage("/security/users/${id}", UserPage.class);
        this.mountPage("/security/groups", com.novamens.content.web.security.markup.GroupsPage.class);
        this.mountPage("/security/groups/${id}", GroupPage.class);
        this.mountPage("/security/roles", RolesPage.class);

        this.mountPage("/security/roles/${id}", RolePage.class);
        this.mountPage("/security/rules", RulesPage.class);
        this.mountPage("/security/rules/${id}", RulePage.class);
        this.mountPage("/aclpage/${oid}/${id}", AclPage.class);
        this.mountPage("/aclpage/${id}", SecuredMemberAclPage.class);

        // Search filters
        // -------------------------------------------------------------------------
        //
        this.mountPage("/facets", FacetsPage.class);
        this.mountPage("/facets/${name}", FacetPage.class);

        // Email notifications
        // --------------------------------------------------------------------
        //
        this.mountPage("/emailnotifications", ENotiRulesPage.class);
        this.mountPage("/emailnotifications/${id}", ENotiRulePage.class);

        // Account
        // --------------------------------------------------------------------------------
        //
        this.mountPage("/myaccount", MyAccountPage.class);

        // Logs
        // -----------------------------------------------------------------------------------
        //
        this.mountPage("/logs/activity", com.novamens.content.web.console.markup.AuditActivityPage.class);
        this.mountPage("/logs/email", AuditEmailPage.class);
        this.mountPage("/logs/content", AuditContentPage.class);
        this.mountPage("/logs/resources", AuditResourcesPage.class);

        if (TREE_RESOURCES.equals("yes"))
            this.mountPage("/logs/treeresources", AuditTreeFileResourcesPage.class);

        // Error
        // ----------------------------------------------------------------------------------
        //
        this.mountPage("/pageexpired", SessionExpiredErrorPage.class);
        this.mountPage("/internalerror", InternalErrorPage.class);

        // Domain Management
        // ----------------------------------------------------------------------
        //
        this.mountPage("/factory/domains", kbee.web.domain.DomainsPage.class);
        this.mountPage("/factory/domainrecyclebin", kbee.web.domain.DomainsRecycleBinPage.class);
        this.mountPage("/factory/newdomain", DomainCreationPage2.class);

        // Commands
        // --------------------------------------------------------------------------------
        //
        this.mountPage("/commands", CommandsPage.class);
        this.mountPage("/commands/${id}/", CommandPage.class);

        // API
        // -------------------------------------------------------------------------------------
        //
        this.mountPage("/api/reports/requests", APIRequestsReportPage.class);
        this.mountPage("/api/reports/soap", APISOAPReportPage.class);
        this.mountPage("/api/reports/stats", APIStatsReportPage.class);

        //his.mountPage("/browser", BrowserPage.class);
        this.mountPage("/browser2", kbee.web.resource.BrowserPage.class);
        this.mountPage("/browser3", kbee.web.content.template.BrowserPage.class);

        this.mountPage("/paymentdetails/${id}", PaymentDetailsPage.class);
        this.mountPage("/payments", PaymentsConsolePage.class);

        // Portales----------------------------------------------------------------------------------
        //
        // this.mountPage("/portal-explorer/{siteurl}", SearcherExplorerPage.class);

        registerPortalComponents();

        ServiceLocator.getService(PortalUrlMapperService.class).map(this);

        setApplicationSettings(new ApplicationSettings());

        ApplicationSettings settings = (ApplicationSettings) getApplicationSettings();

        settings.setInternalErrorPage(InternalErrorPage.class);

        settings.setAccessDeniedPage(AccessDeniedPage.class); // access denied page
        settings.setPageExpiredErrorPage(AccessDeniedPage.class); // page expired page

        settings.setResourceErrorPage(ApplicationErrorPage.class);
        settings.setWorkflowErrorPage(kbee.web.workflow.ErrorPage.class);

        this.getSecuritySettings().setCrossOriginOpenerPolicyConfiguration(CrossOriginOpenerPolicyConfiguration.CoopMode.DISABLED);

        this.getMarkupSettings().setStripWicketTags(true);

        
//        {
//
//            // 📦 Cache largo
//            getResourceSettings()
//                .setDefaultCacheDuration(java.time.Duration.ofDays(365));
//
//            // 🔖 Versionado correcto
//            getResourceSettings()
//                .setCachingStrategy(
//                    new FilenameWithVersionResourceCachingStrategy(
//                        "-v-",
//                        new StaticResourceVersion("1.0.0") // 👈 aquí está el cambio
//                    )
//                );
//
//            // ⚡ Minificados
//            getResourceSettings().setUseMinifiedResources(true);
//
//            // 🧹 Limpieza
//            getMarkupSettings().setStripWicketTags(true);
//        }
        
        
        
        
        

        getRequestCycleListeners().add(new IRequestCycleListener() {
            public void onBeginRequest(RequestCycle cycle) {
                Session session = Session.get();
                if (session.getAttribute("locale") == null) {
                    User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
                    if (user != null && user.getLocale() != null) {
                        Locale locale = user.getLocale();
                        session.setLocale(locale);
                        session.setAttribute("locale", locale.getLanguage());
                    } else {
                        session.setLocale(Locale.getDefault());
                    }
                }
            }
        });

        ((SystemMapper) this.getRootRequestMapper()).add(new WebResourceReferenceMapper());
        ServiceLocator.getService(SchedulerService.class);
        IPackageResourceGuard packageResourceGuard = this.getResourceSettings().getPackageResourceGuard();
        if (packageResourceGuard instanceof SecurePackageResourceGuard) {
            SecurePackageResourceGuard guard = (SecurePackageResourceGuard) packageResourceGuard;
            guard.addPattern("+**/*.xlsx");
        }

        ToastrSettings.createInitializer(this).setAutoAppendBehavior(true).setMessageFilter(IFeedbackMessageFilter.NONE)
                .setGlobalOptions(ToastOptions.create().setPositionClass(ToastOptions.PositionClass.BOTTOM_RIGHT)
                        .setShowDuration(200).setIsEnableProgressBar(true).setIsEnableCloseButton(true))
                .initialize();
    }

    /***
     * 
     * 
     *
     */
    protected void startUpUserImages() {
        UserImagesService service = ServiceLocator.getService(UserImagesService.class);
        if (!service.isInitialized()) {
            try {
                com.novamens.hibernate.session.Session.open();
                service.startUp();
            } catch (Exception e) {
                logger.error(e);
            } finally {
                com.novamens.hibernate.session.Session.close();
                logger.debug("User Images Startup: done.");
            }
        }
    }

    protected void initKbeeRoot() {
        DomainLifeCycleService service = ServiceLocator.getService(DomainLifeCycleService.class);
        try {
            com.novamens.hibernate.session.Session.open();
            service.kbeeDomainInitRoot();

        } catch (Exception e) {
            logger.error(e);
        } finally {
            com.novamens.hibernate.session.Session.close();
            logger.debug("Kbee root user init: done.");
        }
    }

    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

    protected boolean isSolrCompiled() {
        return PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null) == null;
    }

    @PostConstruct
    public void started() {
        // stdlogger.debug("Set TimeZone to UTC");
        // TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Override
    protected IConverterLocator newConverterLocator() {
        ConverterLocator locator = (ConverterLocator) super.newConverterLocator();
        locator.set(CronExpressionJ8.class, new CronConverter());
        return locator;
    }

    public void registerPortalComponents() {

    	
    	


    	
    	
    	
    	
    	
    	
    	
    	
    	
        // Sites
        // ----------------------------------------------------------------------------------
        //
        this.mountPage("/portal", SitesPage.class);
        this.mountPage("/portal/site/${id}", SearcherSiteEditorPage.class);
        this.mountPage("/portals/external/${id}", ExternalSiteEditorPage.class);
        this.mountPage("/portal/test/${id}", PortalSiteEditorPage.class);
        this.mountPage("/portal/sedit/${id}", PortalSiteEditorPage.class);
        this.mountPage("/portal/pedit/${id}", PortalPageEditorPage.class);
        this.mountPage("/portal/pedit/structure/${id}", PortalPageStructureEditorPage.class);
        this.mountPage("/portal/ulist/${id}", SearcherUserListPage.class);

        ServiceLocator.getService(PortalMVCService.class).register(PortalLiteralsService.BLOCK_SEARCH,
                PortalSearchForm.class.getName(), null);
        ServiceLocator.getService(PortalMVCService.class).register(PortalLiteralsService.BLOCK_MY_TASKS,
                DashboardMyTasksWidgetPanel.class.getName(), null);
        ServiceLocator.getService(PortalMVCService.class).register(PortalLiteralsService.BLOCK_PENDING_TASKS,
                DashboardPendingTasksWidgetPanel.class.getName(), null);
        ServiceLocator.getService(PortalMVCService.class).register(PortalLiteralsService.BLOCK_MONITOR,
                DashboardMonitorTasksWidgetPanel.class.getName(), null);
        ServiceLocator.getService(PortalMVCService.class).register(PortalLiteralsService.BLOCK_LIBRARY,
                DashboardLibraryWidgetPanel.class.getName(), null);
        ServiceLocator.getService(PortalMVCService.class).register(PortalLiteralsService.BLOCK_PORTAL_LIBRARY,
                DashboardPortalLibraryContentsPanel.class.getName(), null);

        ServiceLocator.getService(PortalMVCService.class).register(PortalLiteralsService.BLOCK_PORTAL_TEXT,
                PortalSimpleTextPanel.class.getName(), PortalBlockTextDataProvider.class.getName());
        ServiceLocator.getService(PortalMVCService.class).register(PortalLiteralsService.BLOCK_PORTAL_LISTVIEW,
                PortalBlockListViewPanel.class.getName(), PortalBlockListViewDataProvider.class.getName());
        ServiceLocator.getService(PortalMVCService.class).register(PortalLiteralsService.BLOCK_PORTAL_CONTENT_LIST,
                PortalBlockListContentPanel.class.getName(), PortalContentListDataProvider.class.getName());

        ServiceLocator.getService(PortalMVCService.class).register(PortalLiteralsService.BLOCK_DASHBOARD_QUERIES,
                DashboardPortalSavedQueryWidgetPanel.class.getName(), null);
        ServiceLocator.getService(PortalMVCService.class).register(PortalLiteralsService.BLOCK_DASHBOARD_DATASETMEMBERS,
                DashboardPortalSavedQueryWidgetPanel.class.getName(), null);

        // ------------------
        ServiceLocator.getService(PortalMVCService.class).registerViewer("block-billboard",
                kbee.web.alert.BillboardPanel.class.getName());
        ServiceLocator.getService(PortalMVCService.class).registerViewer("area-billboard",
                kbee.web.alert.BillboardPanel.class.getName());
        ServiceLocator.getService(PortalMVCService.class).registerViewer("area-dummy",
                com.novamens.wicket.util.DummyBlockPanel.class.getName());
        ServiceLocator.getService(PortalMVCService.class).registerViewer("block-dummy", DummyBlockPanel.class.getName());

        // DataProvider ---
    }

    protected String getJettyPort() {
        if (PORT != null)
            return PORT;
        for (Tuple t : systemEnv()) {
            if (t.getLabel().equals("jetty.port")) {
                PORT = t.getValue();
                return PORT;
            }
        }
        PORT = PropertiesFactory.getInstance("kbee").getProperties().getProperty("port", "").trim();
        return PORT;
    }

    private List<Tuple> systemEnv() {
        return dumpVars(System.getenv());
    }

    /***
     * 
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

}
