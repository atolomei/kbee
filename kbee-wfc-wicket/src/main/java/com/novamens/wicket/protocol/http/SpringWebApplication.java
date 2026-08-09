package com.novamens.wicket.protocol.http;

import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.context.ApplicationContext;

public abstract class SpringWebApplication extends WebApplication {
	public abstract ApplicationContext getApplicationContext();
}
