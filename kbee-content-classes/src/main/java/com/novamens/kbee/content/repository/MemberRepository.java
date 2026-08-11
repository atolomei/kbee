package com.novamens.kbee.content.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.model.KbeeMemberClassification;
import com.novamens.kbee.content.service.KbeeDomService;
import com.novamens.kbee.repository.AbstractDomRepository;

import kbee.util.logging.Logger;

@Component
public class MemberRepository extends AbstractDomRepository<KbeeDataSetMember, DataSetMember> {

    static Logger logger = new Logger(LogManager.getLogger(KbeeDomService.class.getName()));

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<DataSetMember> findAggregationValues(DataSetMember aggregator, DataSet aggregation) {
        String hql = "FROM KbeeMemberClassification K WHERE " +
                "K.sourcemember.dataset.id=" + aggregation.getId().toString() + " and " +
                "K.datasetmember.id=" + aggregator.getId().toString();

         
        logger.debug(hql);

        Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);

        List results = query.list();
        if (results.isEmpty()) {
            new ArrayList<>();
        }

        List<DataSetMember> values = new ArrayList<DataSetMember>();

        for (KbeeMemberClassification classification : (List<KbeeMemberClassification>) results) {
            logger.debug(classification.getSource().getName() + " -> " + classification.getDataSetMember().getName());
            values.add(classification.getSource());
        }

        return values;
    }

    public DataSetMember findAggregationByValue(DataSetMember aggregator, DataSet aggregation, String value) {
        String hql = "SELECT K.sourcemember FROM KbeeMemberClassification K WHERE " +
                "K.sourcemember.dataset.id=" + aggregation.getId().toString() + " and " +
                "K.datasetmember.id=" + aggregator.getId().toString() + " and " +
                "K.sourcemember.strvalue='" + value + "'";

        logger.debug(hql);
        Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
        @SuppressWarnings("unchecked")
		List<DataSetMember> list = (List<DataSetMember>) query.list();
        if(list.isEmpty())
            return null;
        return list.get(0);
    }
    
    public DataSetMember findByExternalId(String id) {
    	String hql = "FROM KbeeDataSetMember WHERE externalId = '" + id + "'";   
    	Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
    	DataSetMember member = (DataSetMember)query.uniqueResult();
    	return member;
    } 			        
    
    public List<DataSetMember> findAll(DataSet dataSet) {
        String hql = "FROM KbeeDataSetMember M WHERE " +
                "M.dataset.id=" + dataSet.getId().toString();
        Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
        @SuppressWarnings("unchecked")
		List<DataSetMember> roots = (List<DataSetMember>) query.list();
        return roots;
    }
    
    public List<DataSetMember> findRoot(DataSet dataSet) {
        String hql = "FROM KbeeDataSetMember M WHERE " +
                "M.dataset.id=" + dataSet.getId().toString() + " and " +
                "M.parent is null";
        Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
        @SuppressWarnings("unchecked")
		List<DataSetMember> roots = (List<DataSetMember>) query.list();
        return roots;
    }
    
    public List<DataSetMember> findChilds(DataSetMember member) {
        String hql = "select M FROM KbeeDataSetMember M join M.parents P WHERE " +
                "P.id =" + member.getId().toString();

        Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
        @SuppressWarnings("unchecked")
		List<DataSetMember> roots = (List<DataSetMember>) query.list();
        return roots;
    }

}