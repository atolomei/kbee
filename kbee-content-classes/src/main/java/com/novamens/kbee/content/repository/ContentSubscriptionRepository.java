   package com.novamens.kbee.content.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.subscription.ContentSubscription;
import com.novamens.kbee.content.service.KbeeDomService;
import com.novamens.kbee.content.subscription.KbeeContentSubscription;
import com.novamens.kbee.repository.AbstractDomRepository;

import kbee.util.logging.Logger;

@Component
public class ContentSubscriptionRepository extends AbstractDomRepository<KbeeContentSubscription, ContentSubscription> {

    static Logger logger = new Logger(LogManager.getLogger(KbeeDomService.class.getName()));

    @SuppressWarnings({"rawtypes"})
    public ContentSubscription findBy(Content content, Person person) {
        String hql = "FROM KbeeContentSubscription S WHERE " +
                "S.person.id=" + person.getId().toString() + " and " +
                "S.content.id=" + content.getId().toString();

        logger.debug(hql);

        Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
        query.setCacheable(true);

        List results = query.list();

        return results.isEmpty() ? null : (ContentSubscription)results.get(0);
    }
    
    public List<ContentSubscription> findAllBy(Content content) {
    	
    	List<ContentSubscription> subscriptions = new ArrayList<ContentSubscription>();
    	
    	String hql = "FROM KbeeContentSubscription S WHERE " + "S.content.id=" + content.getId().toString();

        logger.debug(hql);

        Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
        query.setCacheable(true);

        for (Object result : query.list()) {
        	subscriptions.add((ContentSubscription)result);
        }
        
        return subscriptions;
    }
    
    

    public List<ContentSubscription> findAllBy(Person person, int limit) {
    	
    	List<ContentSubscription> subscriptions = new ArrayList<ContentSubscription>();
    	
    	String hql = "FROM KbeeContentSubscription S WHERE " + "S.person.id=" + person.getId().toString() +" order by S.lastModifiedDate desc";

        logger.debug(hql);
        // S.content.title
        
        Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
        //query.setCacheable(true);

        if (limit>0)
        	query.setMaxResults(limit);	
        
        for (Object result : query.list())
        	subscriptions.add((ContentSubscription)result);
        
        
        subscriptions.sort( new Comparator<ContentSubscription>() {
			@Override
			public int compare(ContentSubscription o1, ContentSubscription o2) {
				try {
					return o1.getContent().getDisplayName().compareToIgnoreCase(o2.getContent().getDisplayName());
					
				} catch (Exception e) {
					return 0;	
				}
				
			}
        	
        });
        
        return subscriptions;
    }
    
}