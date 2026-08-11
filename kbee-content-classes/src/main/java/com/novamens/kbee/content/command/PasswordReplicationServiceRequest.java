package com.novamens.kbee.content.command;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.logging.SecurityUpdateEvent;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.List;

public class PasswordReplicationServiceRequest  extends AbstractServiceRequest {
    private static final long serialVersionUID = 1L;

    // Logger sincronico en la TRX
    static private Logger txlogger = LogManager.getLogger("TxLogger");

    String password;
    Serializable updatedId;

    public PasswordReplicationServiceRequest(KbeeUser user) {
        password = user.getPasswordClear();
        updatedId = user.getId();
    }

    @Override
    public void execute() {


        ContentDao contentDao = getContentDao();
        String email = contentDao.findUserProfileByUserId(updatedId).getPerson().getEmail();
        if(email != null) {
            List<UserProfile> userProfiles = contentDao.findUserProfileByPersonEmail(email);

            for (UserProfile userProfile : userProfiles) {
                KbeeUser user = (KbeeUser) userProfile.getUser();
                if (!user.getId().equals(updatedId) ) {
                    ServiceLocator.getService(SecurityService.class).authenticate(userProfile.getUser().getUserName());
                    txlogger.info(new SecurityUpdateEvent(user, "Password replicated for email '" + email +"'"));

                    user.setPassword(password);
                    contentDao.save(user);
                }
            }
        }
    }


    private ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

}
