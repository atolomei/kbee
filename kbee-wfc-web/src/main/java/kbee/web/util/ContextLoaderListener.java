package kbee.web.util;

import javax.servlet.ServletContextEvent;

import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;

public class ContextLoaderListener extends org.springframework.web.context.ContextLoaderListener
{
    //private static final Logger logger = LoggerFactory.getLogger( ContextLoaderListener.class );

    public ContextLoaderListener()
    {
        //logger.info( "Starting application..." );
    }
    
	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServiceLocator.setInstance(new SpringServiceLocator("kbee"));
		super.contextInitialized(event);
	}
}