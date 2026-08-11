package com.novamens.kbee.content.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.util.Pair;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class MemberUpdateServiceRequest extends AbstractServiceRequest {
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = LogManager.getLogger(MemberUpdateServiceRequest.class.getName());
	
	private Serializable memberId;
	private transient DataSetMember member;
	//private transient Classifier classifier;

	public MemberUpdateServiceRequest(Classifier classifier, DataSetMember member) {
		setName(member.getDataSet().getDisplayName()+" " + member.getDisplayName()+ " update propagation");
		setMember(member);
		//setClassifier(classifier);
	}
	
	/**
	 * Member updated
	 */
	public DataSetMember getMember() {
		if (member==null) {
			member = getContentDao().findMemberById(memberId);
		}
		return member;
	}
	
	public void setMember(DataSetMember member) {
		this.memberId = member.getId();
	}
	
	@Override
	public int getPriority() {
		return SchedulerService.LOW_PRIORITY;
	}
	
	/**
	 * All the datasets that have in any element of their structure the dataset of the modified member as 
	 * parent, are evaluated
	 */
	@Override
	public void execute() {
		ServiceLocator.getService(SecurityService.class).authenticate("root@"+getDomain().getName());
		for (Pair<DataSet, Classifier> datasetbyclassifier : getRelatedDataSets()) {
			updateDataSet(datasetbyclassifier.getFirst(), datasetbyclassifier.getSecond());
		}
	}
	
	/**
	 * All members related to the modified member are evaluated
	 */
	protected void updateDataSet(DataSet dataset, Classifier classifier) {
		for (DataSetMember member : getRelatedMembers(dataset, classifier)) {
			updateMember(member, classifier);
		}
	}
	
	protected void updateMember(DataSetMember member, Classifier parentclassifier) {
		for (ModelElementTemplate template :  member.getDataSet().getStructure()) {
 			if (template!=null && template.getParent()!=null &&
				template.getParent() instanceof Classifier &&
				((Classifier)template.getParent()).equals(parentclassifier) &&
				template.getElement() instanceof Classifier &&
				!((ClassifierTemplate)template).getMultiplicity().isMultiple()) {
				
				Classifier classifier = (Classifier)template.getElement();
				
				Set<DataSetMember> parentmembers = new HashSet<DataSetMember>();
 				
				for (Classification classification : member.getClassification(parentclassifier)) {
					DataSetMember parentmember = classification.getDataSetMember();
					for (Classification parentclassification : parentmember.getClassification(classifier)) {
						parentmembers.add(parentclassification.getDataSetMember());
					}
				}
				
				Set<DataSetMember> members = new HashSet<DataSetMember>();
				
				for (Classification classification : member.getClassification(classifier)) {
					members.add(classification.getDataSetMember());
				}
				
				if (!parentmembers.equals(members)) {
					if (parentmembers.size()<=1) {
						List<DataSetMember> memberlist = new ArrayList<DataSetMember>();
						memberlist.addAll(parentmembers);
						member.setClassification(classifier, memberlist);
					}
					else {
						member.setClassification(classifier, new ArrayList<DataSetMember>());
					}
					member.getService(DOMObjectService.class).update(classifier.getDisplayName());
				}
 			}	
		}
	}
	
	protected List<Pair<DataSet, Classifier>> getRelatedDataSets() {
		List<Pair<DataSet,Classifier>> datasets = new ArrayList<Pair<DataSet,Classifier>>();
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			for (ModelElementTemplate template : dataset.getStructure()) {
				if (template!=null && template.getParent()!=null &&
					template.getParent() instanceof Classifier &&
					((Classifier)template.getParent()).getDataSet().equals(getMember().getDataSet()) &&
					template.getElement() instanceof Classifier &&
					!((ClassifierTemplate)template).getMultiplicity().isMultiple()) {
					datasets.add(Pair.of(dataset, (Classifier)template.getParent()));
				}	
			}
		}	
		return datasets;
	}
	
	protected List<DataSetMember> getRelatedMembers(DataSet dataset, Classifier classifier) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		ResultSet resultSet = null;
		try {
			SolrParametersQuery query = new SolrParametersQuery(getIndex());
			query.getParameters().put("type", "datasetmember");
			query.getParameters().put(classifier.getUniqueName()+"member", String.valueOf(getMember().getId()));
			query.getParameters().put("dataset", String.valueOf(dataset.getId()));
			resultSet = query.execute();
			while (resultSet.hasNext()) {
				Object object = resultSet.next().getObject();
				if (object instanceof DataSetMember) {
					members.add((DataSetMember)object);
				}
			}
		}
		catch (IndexerException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		finally {
 			if (resultSet!=null)
				resultSet.close();
		}
		return members;
	}
	
	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected Domain getDomain() {
		return getMember().getDomain();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
}