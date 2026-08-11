package com.novamens.kbee.content.reportsubscription;


import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.reportsubscription.ReportSubscription;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserSelfService;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

import kbee.email.EmailBuilderSendSubscriptionReport;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @see {@link SubscriptionReportExportRequest}
 */
public class SubscriptionReportExportCommand extends AsyncCommand {

    static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SubscriptionReportExportCommand.class.getName());

    private String reportExportScheduleId = null;

    private ReportExportSchedule reportExportScheduleBean = null;

    public SubscriptionReportExportCommand() {
        setName("Report Subscription Export Command");

    }

    public SubscriptionReportExportCommand(String reportExportScheduleId) {
        this.reportExportScheduleId = reportExportScheduleId;
    }

    @Override
    public String getStringParameter(String name, String defaultValue) {
        return super.getStringParameter(name, defaultValue);
    }


    private boolean force_send = false;

    public boolean isForceSend() {
        return force_send;
    }

    public void setForceSend(boolean b) {
        this.force_send = b;
    }

    public void testEmail(Long impersonatedUserId, String Email) {

        try {
            Runnable task = () -> {
                Transaction transaction = null;
                try {
                    transaction=beginTransaction();
                    UserProfile impersonatedUser = getContentDao().findUserProfileByUserId(impersonatedUserId);


                    ServiceLocator.getService(SecurityService.class).authenticate(impersonatedUser.getUser().getUserName());

                    validateMembers();
                    reportExportScheduleBean = getExportScheduleBean();

                    Class<?> clazz = Class.forName(reportExportScheduleBean.getExporterClass());
                    ReportExporter exporter = (ReportExporter) clazz.newInstance();



                    File reportFile = exportReport(reportExportScheduleBean, exporter, (KbeeUser) impersonatedUser.getUser());
                    String[] attachment = new String[]{reportFile.getAbsolutePath()};
                    // Send email

                    
                    EmailBuilderSendSubscriptionReport builder = new 
                    		EmailBuilderSendSubscriptionReport
                    		(impersonatedUser.getPerson(),
                    				Email,
                                    reportExportScheduleBean.getReport(),
                                    reportExportScheduleBean.getDescription(),
                                    attachment, impersonatedUser.getPerson().getFirstLastName(), null);
                    		
                    ServiceLocator.getService(EmailService.class).send(builder);		
                    
                    /**
                    ServiceLocator.getService(EmailService.class).sendSubscriptionReport(impersonatedUser.getPerson(), Email,
                            reportExportScheduleBean.getReport(),
                            reportExportScheduleBean.getDescription(),
                            attachment, impersonatedUser.getPerson().getFirstLastName(), 0);
                    **/

                    transaction.commit();
                    transaction=null;
                } catch (Exception e) {
                    logger.error(e);
                }finally {
                    if (transaction != null) {
                        transaction.rollback();
                    }
                }
            };
            Thread thread = new Thread(task);
            thread.start();

        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    protected void executeAsync() {
        try {
            validateMembers();
            setState(CommandState.RUNNING);

            logger.debug("Starting report subscriptions export for schedule id: '" + this.reportExportScheduleId + "'");
            com.novamens.hibernate.session.Session.open();
            ContentDao contentDao = getContentDao();

            reportExportScheduleBean = getExportScheduleBean();

            Class<?> clazz = Class.forName(reportExportScheduleBean.getExporterClass());
            ReportExporter exporter = (ReportExporter) clazz.newInstance();

            int errorCounter = 0;

            logger.debug(isForceSend() ? " force send report " : "");

            Transaction transaction = null;

            List<ReportSubscription> reportSubscriptionsForReportSchedule = contentDao.findReportSubscriptionsForReportSchedule(reportExportScheduleBean.getId());

            for (ReportSubscription reportSubscription : reportSubscriptionsForReportSchedule) {
                try {

                    if (isForceSend() || (reportSubscription.getLastExportSent() == null) ||
                            OffsetDateTime.now()
                                    .minus(reportExportScheduleBean.getHoursRangeSamePeriod(), ChronoUnit.HOURS)
                                    .isAfter(reportSubscription.getLastExportSent())) {

                        KbeeUser user = (KbeeUser) reportSubscription.getUsr();
                        ServiceLocator.getService(SecurityService.class).authenticate(user.getUserName());

                        // -----
                        //
                        transaction = beginTransaction();
                        File reportFile = exportReport(reportExportScheduleBean, exporter, user);


                        //sendReportExportEmail(scheduleBean, user, reportFile);

                        //String[] attachment = new String[]{reportFile.getAbsolutePath()};
                        //UserProfile profile = getContentDao().findUserProfileByUser(user);
                        // Save date time last sent for this user and ReportSuscriptionEvent
                        //
                        user.getService(UserSelfService.class).saveUserReportSubscription(reportSubscription, reportExportScheduleBean.getReport(), reportExportScheduleBean.getDescription(), reportFile);

                        logger.debug("Export schedule '" + this.reportExportScheduleId + "'  sucessfully sent to user: '" + reportSubscription.getUsr().toString());
                        transaction.commit();
                        // ----------

                    } else {
                        logger.debug("Export schedule '" + this.reportExportScheduleId + "' already sent to user : '" + reportSubscription.getUsr().toString() + "' at " + reportSubscription.getLastExportSent());
                    }

                } catch (Exception e) {
                    logger.error(e);
                    errorCounter++;
                    if (transaction != null) {
                        transaction.rollback();
                        transaction = null;
                    }
                }
            }

            setResult("ok");
            setState(CommandState.COMPLETED);

        } catch (Exception e) {
            logger.error(e);
            stop();
            setResult(e.getClass().getSimpleName() + " | " + e.getMessage());
            setState(CommandState.ERROR);

        } finally {
            logger.debug("Report subscriptions export process for schedule id: '" + this.reportExportScheduleId + "' finished.");
            com.novamens.hibernate.session.Session.close();
        }
    }

    public ReportExportSchedule getExportScheduleBean() {
        if (this.reportExportScheduleBean == null)
            this.reportExportScheduleBean = (ReportExportSchedule) ServiceLocator.getService(BeansService.class).getBean(this.reportExportScheduleId);
        return this.reportExportScheduleBean;
    }


    /**
     * private void sendReportExportEmail(ReportExportSchedule reportScheduleBean, KbeeUser user, File reportFile) {
     * String[] attachment = new String[]{reportFile.getAbsolutePath()};
     * UserProfile profile = getContentDao().findUserProfileByUser(user);
     * ServiceLocator.getService(EmailService.class).sendSubscriptionReport(profile, profile.getPerson().getEmail(), reportScheduleBean.getReport(), reportScheduleBean.getDescription(), attachment, profile.getPerson().getFirstLastName(), reportFile);
     * }
     **/

    private File exportReport(ReportExportSchedule bean, ReportExporter exporter, KbeeUser user) {

        DateFormat dayDate = new SimpleDateFormat("yyyy-MM-dd HH mm ss");
        String reportFileName = bean.getReport() + " - " + user.getFirstLastName() + " - " + dayDate.format(new Date());

        Map<String, Object> reportParameters = new HashMap<String, Object>(bean.getReportParameters());
        reportParameters.put("userId", user.getId());

        logger.debug(reportFileName + " report will have a null title ");
        return exporter.export(bean.getReportClass(), reportParameters, reportFileName, null);
    }

    private ContentDao getContentDao() {
        BeansService beans = ServiceLocator.getService(BeansService.class);
        return (ContentDao) beans.getBean("contentDao");
    }

    protected Transaction beginTransaction() {
        return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
    }

    public String getReportExportScheduleId() {
        return reportExportScheduleId;
    }

    public void setReportExportScheduleId(String reportExportScheduleId) {
        this.reportExportScheduleId = reportExportScheduleId;
    }

    private void validateMembers() {
        if (this.reportExportScheduleId == null)
            throw new RuntimeException("reportExportScheduleId not set");
    }

    @Override
    public void setParameters(Map<String, Object> map) {
        super.setParameters(map);
        updateParameters();
    }

    @Override
    public void setParameter(String name, Object value) {
        super.setParameter(name, value);
        updateParameters();
    }

    private void updateParameters() {
        String reportExportScheduleIdParam = "reportExportScheduleId".toLowerCase();
        if (this.getParameters().containsKey(reportExportScheduleIdParam))
            this.reportExportScheduleId = (String) this.getParameters().get(reportExportScheduleIdParam);
    }


}
