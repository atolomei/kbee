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
import com.novamens.content.base.Relation;
import com.novamens.content.model.RelationTemplate;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.security.Identifiable;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "KB_CONTENT_RELATION")
public class KbeeRelation implements Relation, Identifiable {

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
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeContent.class)
	@JoinColumn(name="target_id", nullable=false)
	private Content target;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeRelationTemplate.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="template_id", nullable=false)
	private RelationTemplate template;
	
	@Override
	public Long getId() {
		return id;
	}
	
	@Override
	public String getDisplayName() {
		return getTarget().getDisplayName();
	}
	
	@Override
	public Content getSource() {
		return source;
	}
	
	public void setSource(Content content) {
		this.source = content;
	}
	
	@Override
	public Content getTarget() {
		return target;
	}
	
	public void setTarget(Content content) {
		this.target = content;
	}
	
	@Override
	public RelationTemplate getTemplate() {
		return template;
	}
	
	public void setTemplate(RelationTemplate template) {
		this.template = template;
	}
	
	@Override
	public Relation clone() {
		KbeeRelation clone = new KbeeRelation();
		clone.setSource(getSource());
		clone.setTemplate(getTemplate());
		clone.setTarget(getTarget());
		return clone;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeRelation)) return false;
		KbeeRelation relation = (KbeeRelation)object;
		if (relation.getId()!=null && getId()!=null)
			return relation.getId().equals(getId());
		if (relation.getTarget()==null || getTarget()==null || 
				relation.getTemplate()==null || getTemplate()==null)
			return false;
		return relation.getTarget().getId().equals(getTarget().getId()) && 
				relation.getTemplate().getName().equals(getTemplate().getName());
	}
}
