package com.novamens.content.service;

import com.novamens.dom.Json;
import com.novamens.service.SystemService;

public interface TokenService extends SystemService {
	public String getToken(Json json);
	public Json decode(String token);
}