package com.novamens.kbee.content.support;


import com.novamens.security.User;
import com.novamens.service.BusinessSystemService;

public interface SupportService extends BusinessSystemService {

	public Tip getTipOfTheDay(User user, String area);

	public Tip getNext(User user, String area, Tip tip);
	public Tip getNext(User user, String area, int src_index);
	
	public void evict();

	public Tip getRandomNext(User user, String area);
	public Tip getRandomNext(User user, String area, String default_area);

}
