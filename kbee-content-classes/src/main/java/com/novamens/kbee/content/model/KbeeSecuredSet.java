package com.novamens.kbee.content.model;

import java.time.OffsetDateTime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.SecuredSet;

@Entity
@DiscriminatorValue(value="7")
public class KbeeSecuredSet extends KbeeDataSet implements SecuredSet {
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeClassifier.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "CLASSIFIER_ID", updatable=false)
	private Classifier classifier;

	public KbeeSecuredSet() {
		super(null, DataSetType.SECURED);
	}
	
	public KbeeSecuredSet(String name) {
		super(name, DataSetType.SECURED);
	}
	
	public DataSetMember createMember() {
		DataSetMember member;
		member = new KbeeSecuredMember(this);
		member.setCreationOffsetDateTime(OffsetDateTime.now());
		member.setLastModifiedOffsetDateTime(OffsetDateTime.now());

		return member;
	}
	
	@Override
	public Classifier getClassifier() {
		return classifier;
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	
	@Override
	public DataSet clone() {
		KbeeSecuredSet clone = new KbeeSecuredSet();
		super.clone(clone);
		return clone;
	}
}