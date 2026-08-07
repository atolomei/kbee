package com.novamens.kbee.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;

public class KbeeDomRepositoryService implements DomRepositoryService{
	
	
	private Map<Class<?>, DomRepository<?>> repositories = Collections.synchronizedMap(new HashMap<>()); 
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> DomRepository<T> getRepository(Class<T> domclass) {
		 
		DomRepository<T> repository = (DomRepository<T>)repositories.get(domclass);
		
		if (repository==null) {
			SpringServiceLocator serviceLocator = (SpringServiceLocator)ServiceLocator.getInstance();
			Map<String, DomRepository> beans = serviceLocator.getContext().getBeansOfType(DomRepository.class);
			for (String bean : beans.keySet()) {
				DomRepository<?> r = (DomRepository<?>)serviceLocator.getContext().getBean(bean);
				if (((AbstractDomRepository<?,?>)r).accept(domclass)) {
					repositories.put(domclass, r);
					repository = (DomRepository<T>)r;
					break;
				}
			}
		}
		
		return repository;
	}
}