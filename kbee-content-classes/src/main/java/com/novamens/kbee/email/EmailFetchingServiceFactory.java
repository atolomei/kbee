package com.novamens.kbee.email;

import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;

public class EmailFetchingServiceFactory  extends AbstractServiceFactory<SystemService> {
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailFetchingServiceFactory.class.getName());


    private KbeeEmailFetchingService service;

    public boolean isFactory(Class<? extends Service> serviceClass) {
        return serviceClass.isAssignableFrom(KbeeEmailFetchingService.class);
    }


    public <S extends SystemService> S getService() {
        if ( !service.isStarted())
            synchronized (this) {
                if (!service.isStarted()) {
                    logger.error("Starting EmailFetchingService...");
                    service.start();
                }else{
                    logger.error("EmailFetchingService is already running.");
                }
            }else{
            logger.error("EmailFetchingService is already running.");
        }
        return (S)service;
    }

    public void setService(KbeeEmailFetchingService service) {
        this.service = service;
    }





}
