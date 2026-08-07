package com.novamens.kbee.command;

import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;

public class KbeeCommandServiceFactory extends AbstractServiceFactory<SystemService> {

    private KbeeCommandService service;
    boolean started = false;

    public boolean isFactory(Class<? extends Service> serviceClass) {
        return serviceClass.isInstance(getService());
    }

    @SuppressWarnings("unchecked")
    public <S extends SystemService> S getService() {
        return (S) service;
    }

    public void setService(KbeeCommandService service) {
        this.service = service;
    }

}
