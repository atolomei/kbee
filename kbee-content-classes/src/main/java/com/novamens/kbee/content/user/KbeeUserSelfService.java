package com.novamens.kbee.content.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.novamens.content.reportsubscription.ReportSubscription;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.content.user.externalLogin.UserExternalLoginPlatform;
import com.novamens.content.user.externalLogin.UserExternalPlatformIdType;
import com.novamens.logging.SecurityUpdateEvent;
import com.novamens.portal6.model.Site;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.service.AuditResourceService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserSelfService;

import com.novamens.email.EmailService;
import com.novamens.kbee.content.command.CleanIndexCommand;
import com.novamens.kbee.content.command.ReindexCommand;
import com.novamens.kbee.content.reportsubscription.ReportSubscriptionEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbfs.FileServerException;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.user.PreferencesService;
import com.novamens.whatsapp.HsmComponent;
import com.novamens.whatsapp.HsmParameter;
import com.novamens.whatsapp.WhatsAppService;
import com.novamens.whatsapp.HsmComponent.Section;

import kbee.email.EmailBuilderAdminSendPasswordReset;
import kbee.email.EmailBuilderSendSubscriptionReport;

public class KbeeUserSelfService implements UserSelfService {

	static private Logger trx_logger = LogManager.getLogger("TxLogger");
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeUserSelfService.class.getName());

    private User user;
    
    public KbeeUserSelfService() {
    }

    public KbeeUserSelfService(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    
    public Site getDashboardSite() {
    	return null;
    }
    
    
    public void sendLinkToResetPassword(Person sender) {
        
    	KbeeUser user = (KbeeUser) getUser();

    	if (user == null)
            return;
    	
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		String token = service.nextSecureToken();
		service.addToken(user, token);
        
        UserProfile profile = getContentDao().findUserProfileByUser(user);
        EmailBuilderAdminSendPasswordReset builder = new EmailBuilderAdminSendPasswordReset (profile.getPerson(), sender, token);
		builder.setLanguage(sender.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
        ServiceLocator.getService(EmailService.class).send(builder);
        
        
        if (profile.isWhatsAppEnabled()) {
    		List<HsmComponent> components = new ArrayList<>();
    		HsmComponent component;
    		
    		List<HsmParameter> parameters;
    		HsmParameter parameter;
    		
    		component = new HsmComponent();
    		component.setSection(Section.Header);
    		parameters = new ArrayList<>();
    		
    		parameter = new HsmParameter();
    		parameter.setType("text");
    		parameter.setValue(user.getDomain().getDisplayName());
    		parameters.add(parameter);
    		
    		component.setParameters(parameters);
    		components.add(component);
    		
    		component = new HsmComponent();
    		component.setSection(Section.Body);
    		parameters = new ArrayList<>();
    		
    		parameter = new HsmParameter();
    		parameter.setType("text");
    		parameter.setValue(user.getDisplayName());
    		parameters.add(parameter);
    		
    		component.setParameters(parameters);
    		components.add(component);
    		
    		component = new HsmComponent();
    		component.setSection(Section.Button);
    		parameters = new ArrayList<>();

    		parameter = new HsmParameter();
    		parameter.setType("text");

    		String urlsuffix = "?key=" + token;
    		
    		parameter.setValue(urlsuffix);
    		parameters.add(parameter);

    		component.setParameters(parameters);
    		components.add(component);
    		
    		String templateName = "reset_password";
    		String phone = profile.getPerson().getPhone();
    		
    		ServiceLocator.getService(WhatsAppService.class).startConversation(templateName, phone, components);

        	
        }
        
//		String phoneNumber = profile.getPerson().getPhone();
//		if (phoneNumber!=null && !"".equals(phoneNumber.trim())) {
//	        String message = builder.buildPlain();
//	        if (message!=null) {
//	        	ServiceLocator.getService(SmsService.class).sendMessage(new KbeeSmsMessage( phoneNumber , message));
//	        }
//		}
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void resetPreferences() {
        ((KbeeUser) getUser()).getService(PreferencesService.class).deleteAllPreferences();
    }

    /**
     * <p>Reindex and Clean contents in the Workspace</p>
     */
    public void reindex() {

    	logger.debug("reindex " + getUser().getLastFirstName());
    	
    	/**  Clean indexes Workspace  */
        CleanIndexCommand cleanIndexCommand = new CleanIndexCommand("workspace:" + getUser().getId().toString(), 
        		
        		getContentDao().findUserProfileByUser(user).getDomain().getId().toString()
        		
        		);
        cleanIndexCommand.execute();

        /** Reindex  */
        String statement = "from KbeeContent where workspace=" + getUser().getId().toString();
        ReindexCommand reindexcommand = new ReindexCommand(statement);
        reindexcommand.setDoNotSu(true);
        reindexcommand.execute();

        // Clean indexes Workspace
        //
        cleanIndexCommand = new CleanIndexCommand("workspace:" + getUser().getId().toString(),
        		getContentDao().findUserProfileByUser(user).getDomain().getId().toString()
        );
        cleanIndexCommand.execute();
        
        
        // reindex user
        //
        String statement_u = "from KbeeUser where id=" + getUser().getId().toString();
        ReindexCommand reindexcommand_u = new ReindexCommand(statement_u);
        reindexcommand_u.setDoNotSu(true);
        reindexcommand_u.execute();
        
        
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void setEmailNotifications(boolean b) {
        UserProfile profile = getContentDao().findUserProfileByUser(getUser());
        if (profile != null) {
            profile.setEmailNotifications(b);
            List<String> list = new ArrayList<String>();
            list.add((b ? "Enable " : " Disable ") + "Email Notifications ");
            try {
                ServiceLocator.getService(SecurityContentMgmtService.class).update(profile, list);
            } catch (ContentMgmtException e) {
                logger.error(e);
            }
        }
    }


    @Override
    public List<ReportSubscription> getUserReportSubscriptions() {
        List<ReportSubscription> subscriptions = getContentDao().findReportSubscriptionsByUser(getUser().getId());
        return subscriptions;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveUserReportSubscriptions(List<ReportSubscription> userReportSubscriptions) {

        for (ReportSubscription userReportSubscription : userReportSubscriptions) {
            userReportSubscription.setLastModifiedUser(getUser());
            userReportSubscription.setLastModifiedOffsetDateTime(OffsetDateTime.now());
            getContentDao().save(userReportSubscription);
        }
        List<String> updatedParts = userReportSubscriptions.stream().map(
                sub -> sub.getReportExportScheduleId() + "  " + (sub.isEnabled() ? "enabled" : "disabled")
        ).collect(Collectors.toList());

        // todo: ajustar clase del evento
        trx_logger.info(new SecurityUpdateEvent(getUser(), updatedParts));
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addLinkLoginPlatform(ExternalPlatformId externalPlatformId, UserExternalPlatformIdType userExternalPlatformIdType, String userIdInPlatform) {
        final UserProfile userProfile = getContentDao().findUserProfileByUser(user);

        if(userProfile != null){
            List<UserExternalLoginPlatform> userExternalLoginPlatforms = new ArrayList<UserExternalLoginPlatform>();
            if (userProfile.getUserExternalLoginPlatforms()!=null)
            userExternalLoginPlatforms.addAll(userProfile.getUserExternalLoginPlatforms());
//            if(userExternalLoginPlatforms == null)
//                userExternalLoginPlatforms = new ArrayList<>();

            UserExternalLoginPlatform findExistent = userExternalLoginPlatforms.stream().filter(el -> el.getPlatformId() == externalPlatformId.getId() &&
                    el.getUserPlatformIdType() == userExternalPlatformIdType.getId() &&
                    el.getUserPlatformId().equals(userIdInPlatform)).findFirst().orElse(null);

            if(findExistent == null){
                UserExternalLoginPlatform newEntry = new KbeeUserExternalLoginPlatform();
                newEntry.setEnabled(true);
                newEntry.setPlatformId(externalPlatformId.getId());
                newEntry.setUserPlatformIdType(userExternalPlatformIdType.getId());
                newEntry.setUserPlatformId(userIdInPlatform);
                newEntry.setUserProfile(userProfile);
                userExternalLoginPlatforms.add(newEntry);
                userExternalLoginPlatforms.isEmpty();
                userProfile.setUserExternalLoginPlatforms(userExternalLoginPlatforms);
                getContentDao().save(userProfile);
            }
        }
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveUserReportSubscription(ReportSubscription userReportSubscription, String reportName, String reportDescription, File file) {


        String[] attachment = new String[]{file.getAbsolutePath()};
        UserProfile profile = getContentDao().findUserProfileByUser(user);

        try {
            KBFile kb_file = ServiceLocator.getService(AuditResourceService.class).putFile(file, userReportSubscription.getUsr(), userReportSubscription.getDomain());

            // Send email
            
            EmailBuilderSendSubscriptionReport builder = new 
            		EmailBuilderSendSubscriptionReport
            		(profile.getPerson(), profile.getPerson().getEmail(),
                            reportName,
                            reportDescription,
                            attachment, profile.getPerson().getFirstLastName(), (long) kb_file.getId());
            		
            ServiceLocator.getService(EmailService.class).send(builder);		

            
            //ServiceLocator.getService(EmailService.class).sendSubscriptionReport(profile.getPerson(), profile.getPerson().getEmail(),
             //       reportName,
              //      reportDescription,
               //     attachment, profile.getPerson().getFirstLastName(), (long) kb_file.getId());

            userReportSubscription.setLastExportSent(OffsetDateTime.now());

            getContentDao().save(userReportSubscription);
            // The email was already sent at this point, so if there is a problem
            // we include it in the audit event
            ReportSubscriptionEvent event = new ReportSubscriptionEvent(userReportSubscription, kb_file);
            trx_logger.info(event);


        } catch (FileNotFoundException | FileServerException | ServiceNotFoundException e) {
            logger.error(e);
        }
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveUserReportSubscriptionNoAudit(ReportSubscription userReportSubscription) {
        getContentDao().save(userReportSubscription);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sessionFlush() {
        getContentDao().sessionFlush();
    }

    // Spring
    //
    private ContentDao contentDao;

    public ContentDao getContentDao() {
        return contentDao;
    }

    public void setContentDao(ContentDao dao) {
        contentDao = dao;
    }


}
