package com.novamens.kbee.scheduler;

import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.ServiceLocator;
import com.novamens.service.SystemService;
import com.novamens.spring.service.SpringServiceLocator;

public class CronKbeeSchedulerServiceFactory extends AbstractServiceFactory<SystemService> {
    private String bean;

    public boolean isFactory(Class<? extends Service> serviceClass) {
        return serviceClass.isInstance(getService());
    }

    @SuppressWarnings("unchecked")
    public <S extends SystemService> S getService() {
        SpringServiceLocator serviceLocator = (SpringServiceLocator) ServiceLocator.getInstance();
        return (S)serviceLocator.getContext().getBean(getBean());
    }

    public void setBean(String beanname) {
        this.bean = beanname;
    }
    public String getBean() {
        return bean;
    }

}
