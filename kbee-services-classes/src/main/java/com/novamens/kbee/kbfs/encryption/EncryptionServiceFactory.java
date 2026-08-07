package com.novamens.kbee.kbfs.encryption;

import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;

public class EncryptionServiceFactory extends AbstractServiceFactory<SystemService> {

    private EncryptionService service;
    boolean started = false;

    public boolean isFactory(Class<? extends Service> serviceClass) {
        return serviceClass.isInstance(getService());
    }

    @SuppressWarnings("unchecked")
    public <S extends SystemService> S getService() {
        return (S)service;
    }

    public void setService(EncryptionService service) {
        this.service = service;
    }

}
