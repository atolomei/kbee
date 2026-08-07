package com.novamens.repository;

import com.novamens.service.SystemService;

public interface DomRepositoryService extends SystemService {
	public <T> DomRepository<T> getRepository(Class<T> domclass); 
}