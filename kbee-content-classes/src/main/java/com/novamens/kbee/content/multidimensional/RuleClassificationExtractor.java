package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.kbee.content.security.PredicatesIqlEvaluator;
import com.novamens.service.ServiceLocator;

public class RuleClassificationExtractor implements Extractor {
	private Classifier classifier;

	
	public RuleClassificationExtractor(Classifier classifier) {
		setClassifier(classifier);
	}
	
	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(SecurityRule.class, object);
		List<String> members = new ArrayList<String>();
		for (DataSetMember member : getMembers(getClassifier(), ((SecurityRule)object))) {
			members.add(getPath(member));
		}
		return members;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	public Classifier getClassifier() {
		return this.classifier;
	}
	
	private List<DataSetMember> getMembers(Classifier classifier, SecurityRule rule) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		if (rule.getCondition()==null || "".equals(rule.getCondition())) return members;
		Expression iqlexpression = classifier.getDomain().getService(IqlService.class).getExpression(rule.getCondition());
		PredicatesIqlEvaluator evaluator = new PredicatesIqlEvaluator(iqlexpression);
		Map<String, List<String>> predicates = evaluator.evaluate();
		for (String predicate : predicates.keySet()) {
			for (String memberid : predicates.get(predicate)) {
				if (isDigits(memberid)) {
					DataSetMember member = getContentDao().findMemberById(Long.valueOf(memberid));
					if (member!=null && member.getDataSet().equals(classifier.getDataSet())) {
						members.add(member);
					}
					else {
						if (member!=null && member instanceof Classificable) {
							for (Classification classification : ((Classificable)member).getClassification(classifier)) {
								if (!members.contains(classification.getDataSetMember()))
								members.add(classification.getDataSetMember());
							}
						}
					}
				}
			}
		}
		
		List<String> membersids = predicates.get(classifier.getPredicate());
		if (membersids!=null)
		for (String memberid : membersids) { 
			if (isDigits(memberid)) {
				DataSetMember member = getContentDao().findMemberById(Long.valueOf(memberid));
				if (member!=null) members.add(member);
			}
		}
		return members;
	}
	
	private String getPath(DataSetMember member) {
		String path = String.valueOf(member.getId());
//		DataSetMember parent = member.getParent();
//		while (parent!=null) {
//			String parentid = String.valueOf(parent.getId());
//			if (!path.contains(parentid+"/")) {
//				path = parentid + "/" + path;
//				parent = parent.getParent();
//			}
//			else
//				parent = null;
//		}
		return path;
	}
	
	private boolean isDigits(String argument) {
		for (int c=0; c<argument.length(); c++) {
			if (!Character.isDigit(argument.charAt(c))) {
				return false;
			}
		}
		return true;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
