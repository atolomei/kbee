package com.novamens.kbee.content.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import com.novamens.content.model.EntityMember;
import com.novamens.content.rule.ActionRule;
import com.novamens.kbee.content.rule.KbeeActionRule;
import com.novamens.kbee.repository.AbstractDomRepository;

@Component 
public class ActionRuleRepository extends AbstractDomRepository<KbeeActionRule, ActionRule> {

    public List<ActionRule> findByEntity(EntityMember entity) {
    	List<ActionRule> rules = new ArrayList<ActionRule>();
    	
        String hql = "FROM KbeeEntityRule Rule WHERE " +
                "Rule.entity.id=" + String.valueOf(entity.getId());
        
        Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
        query.setCacheable(true);
        
        for (Object result : query.list())
        	rules.add((ActionRule)result);
        
        
		Collections.sort(rules, new Comparator<ActionRule>() {
			@Override
			public int compare(ActionRule a, ActionRule b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});	

        return rules;
    }
}