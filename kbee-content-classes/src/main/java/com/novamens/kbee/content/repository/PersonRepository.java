package com.novamens.kbee.content.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.model.KbeePersonMember;
import com.novamens.kbee.content.service.KbeeDomService;
import com.novamens.kbee.repository.AbstractDomRepository;

import kbee.util.logging.Logger;

@Component
public class PersonRepository extends AbstractDomRepository<KbeePerson, Person> {

    static Logger logger = new Logger(LogManager.getLogger(KbeeDomService.class.getName()));

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<Person> findNotIn(DataSet dataset) {
        String hql = "FROM KbeePersonMember E WHERE " +
        	"E.dataset.id != "+ dataset.getId() + " AND " + 
        	"E.domain.id = "+ dataset.getDomain().getId() + " AND " + 
        	"NOT EXISTS ("+
        	"FROM KbeeEntityMember I WHERE "+
        	"E.entity.id = I.entity.id AND "+
        	"I.dataset.id = "+ dataset.getId() + ")"; 

        logger.debug(hql);

        Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);

        List results = query.list();

        List<Person> values = new ArrayList<Person>();

        for (KbeePersonMember member : (List<KbeePersonMember>) results) {
            values.add(member.getPerson());
        }

        return values;
    }
}