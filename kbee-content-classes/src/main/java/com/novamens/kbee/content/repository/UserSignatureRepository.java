package com.novamens.kbee.content.repository;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserSignature;
import com.novamens.kbee.content.service.KbeeDomService;
import com.novamens.kbee.content.user.KbeeUserSignature;
import com.novamens.kbee.repository.AbstractDomRepository;
import com.novamens.security.Identifiable;

import kbee.util.logging.Logger;

@Component
public class UserSignatureRepository extends AbstractDomRepository<KbeeUserSignature, UserSignature> {

    static Logger logger = new Logger(LogManager.getLogger(KbeeDomService.class.getName()));

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<UserSignature> findByDevice(UserDevice device) {
        String hql = "FROM KbeeUserSignature S WHERE " +
                "K.signature.id=" + ((Identifiable)device).getId().toString();
   
        logger.debug(hql);

        Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);

        List results = query.list();
        
        return results;
    }
}