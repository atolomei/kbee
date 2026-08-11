package com.novamens.kbee.content.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.novamens.content.base.Content;
import com.novamens.content.base.RelationshipByCriteria;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.relationshipsbycriteria.KbeeRelationshipByCriteriaTemplate;
import com.novamens.kbee.content.security.JavaIqlEvaluator;
import com.novamens.security.Identifiable;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "Kb_Content_RsByCriteria")
public class KbeeRelationshipByCriteria implements RelationshipByCriteria, Identifiable {

	@Id
	@GenericGenerator(
		name = "relation_sequencer",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "classificationid_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "hilo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "relation_sequencer")
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeContent.class)
	@JoinColumn(name="source_id", insertable=false, updatable=false, nullable=false)
	private Content source;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeRelationshipByCriteriaTemplate.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="template_id", nullable=false)
	private RelationshipByCriteriaTemplate template;
	
	@Column(name = "condition")
	private String condition;
	
	@Override
	public Long getId() {
		return id;
	}
	
	@Override
	public String getDisplayName() {
		return getTemplate().getName();
	}
	
	@Override
	public Content getSource() {
		return source;
	}
	
	public void setSource(Content content) {
		this.source = content;
	}
	
	@Override
	public String getCriteria() {
		return condition;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	@Override
	public boolean includes(Content content) {
		Expression iqlexpression = getCriteriaExpression();
		if (iqlexpression!=null) {
			JavaIqlEvaluator evaluator = new JavaIqlEvaluator(iqlexpression);
			boolean evaluation = evaluator.evaluate(content);
			return evaluation;
		}
		return false;
	}
	
	public Expression getCriteriaExpression() {
		try {
			Expression expression = getSource().getDomain().getService(IqlService.class).getExpression(getCriteria());
			return expression;
		} 
		catch (Exception e) {
//			logger.error(e);
		}
		return null;
	}
	
	@Override
	public RelationshipByCriteriaTemplate getTemplate() {
		return template;
	}
	
	public void setTemplate(RelationshipByCriteriaTemplate template) {
		this.template = template;
	}
	
	@Override
	public RelationshipByCriteria clone() {
		KbeeRelationshipByCriteria clone = new KbeeRelationshipByCriteria();
		clone.setSource(getSource());
		clone.setCondition(getCriteria());
		clone.setTemplate(getTemplate());
		return clone;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeRelationshipByCriteria)) return false;
		KbeeRelationshipByCriteria relation = (KbeeRelationshipByCriteria)object;
		if (relation.getId()!=null && getId()!=null)
			return relation.getId().equals(getId());
		if (relation.getCriteria()!=null && getCriteria()!=null)
			return relation.getCriteria().equals(getCriteria());
		return false;
	}
}
