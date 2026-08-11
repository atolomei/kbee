package kbee.query;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;


import kbee.util.logging.Logger;

public class Proxy {
	
    static private Logger logger = Logger.getLogger(Proxy.class.getName());

	public static Object Unproxy(Object object) {
        try {
    		if (object instanceof HibernateProxy) {
    			HibernateProxy proxy = (HibernateProxy)object;
    			LazyInitializer initializer = proxy.getHibernateLazyInitializer();
    			object = initializer.getImplementation();
    		}
            return object;
        } 
        catch (Exception e) {
            logger.error(e);
            return object;
        }
	}
}