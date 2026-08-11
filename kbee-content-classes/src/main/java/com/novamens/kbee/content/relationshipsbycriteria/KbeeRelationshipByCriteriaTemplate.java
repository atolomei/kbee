package com.novamens.kbee.content.relationshipsbycriteria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.security.JavaIqlEvaluator;
import com.novamens.security.Identifiable;

@Entity
@Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "Kb_RsByCriteria_Template")
public class KbeeRelationshipByCriteriaTemplate implements RelationshipByCriteriaTemplate, Identifiable {
	
	@Id
	@SequenceGenerator(name = "template_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "source_label")
	private String sourceLabel;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeContentTemplate.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "sourcetemplate_Id", insertable=false, updatable=false, nullable=false)
	private ContentTemplate sourceTemplate;
	
	@Column(name = "target_label")
	private String targetLabel;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeClassifier.class)
	@JoinColumn(name = "classifier_id")
	private Classifier classifier;
	
	public KbeeRelationshipByCriteriaTemplate() {
	}
	
	public KbeeRelationshipByCriteriaTemplate(KbeeRelationshipByCriteriaTemplate src) {
		this.name=src.name;
		this.sourceTemplate=src.getSourceTemplate();
		this.sourceLabel=src.sourceLabel;
		this.targetLabel=src.targetLabel;
	}
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public String getDisplayName() {
		return name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTargetLabel() {
		return targetLabel;
	}
	
	public void setTargetLabel(String label) {
		this.targetLabel = label;
	}
	
	public String getReverseLabel() {
		return sourceLabel;
	}
	
	public void setReverseLabel(String label) {
		this.sourceLabel = label;
	}

	public ContentTemplate getSourceTemplate() {
		return sourceTemplate;
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
	
	public void setClassifier(Classifier set) {
		this.classifier = set;
	}
	
	public boolean includes(Content content) {
		return true;
	}
	
	public boolean related(Content source, Content target) {
		try {
			if (!source.isHeadVersion())
				return false;
			
			if (source.isArchived())
				return false;
			
			if (getClassifier()==null)
				return false;
			
			List<DataSetMember> members = getClassification(source, getClassifier());
			
			if (members.isEmpty())
				return false;
			
			if (getClassifier().getPredicate()==null)
				return false;
			
			String predicate = getClassifier().getPredicate();
			
			String condition = "";
			for (DataSetMember member : members) {
				if (!"".equals(condition))
					condition += " OR ";
				condition = predicate += "(" + member.getId() + ")";
			}
			
			Expression expression = source.getDomain().getService(IqlService.class).getExpression(condition);
			JavaIqlEvaluator evaluator = new JavaIqlEvaluator(expression);
			boolean evaluation = evaluator.evaluate(target);
			
			return evaluation;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	private List<DataSetMember> getClassification(Content content, Classifier classifier) {
		List<DataSetMember> members = new ArrayList<>();
		List<Classification> classification = content.getClassification(classifier);
		for (Classification c : classification) {
			members.add(c.getDataSetMember());
		}
		return members;
	}
	
}
