package com.novamens.kbee.timer;


import org.springframework.beans.factory.BeanNameAware;

import com.novamens.timer.CallBack;

public class KbeeCallBack implements CallBack, BeanNameAware {
	private String beanName;
	
	public void execute() {
		
	}
	
	public void setBeanName(String bean) {
		this.beanName = bean;
	}
	
	public String getBeanName() {
		return beanName;
	}
}
