package com.novamens.whatsapp;

import java.util.List;

import com.novamens.service.SystemService;

public interface WhatsAppService extends SystemService {
	public void startConversation(String template, String phone, List<HsmComponent> components);
}