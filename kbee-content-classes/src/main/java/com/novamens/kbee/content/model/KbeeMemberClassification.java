package com.novamens.kbee.content.model;


import java.time.OffsetDateTime;
import java.util.Locale;

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

import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.util.KbeeRuntimeException;

/**
 * Enabled, Archived, Deleted
 *
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "MemberClassification")
public class KbeeMemberClassification implements Classification  {
	
	@Id
	@SequenceGenerator(name = "memberclassification_sequencer", sequenceName = "classificationid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "memberclassification_sequencer")
	@Column(name = "ID")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeClassifier.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="classifier_id", nullable=false)
	private Classifier classifier;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeDataSetMember.class)
	@JoinColumn(name="sourcemember_id", insertable=false, updatable=false, nullable=false)
	private DataSetMember sourcemember;
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeDataSetMember.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="targetmember_id", nullable=true)
	private DataSetMember datasetmember;
	
	@Column(name = "position", insertable=false, updatable=false)
	private int position;

	public KbeeMemberClassification() {
		super();
	};
	
	public KbeeMemberClassification(Classifier classifier, DataSetMember targetmember, DataSetMember sourcemember) {
		super();
		setDataSetMember(targetmember);
		setClassifier(classifier);
		setSourceMember(sourcemember);
	};
	
	public DataSetMember getSource() {
		return sourcemember;
	}
	
	public void setSourceMember(DataSetMember source) {
		this.sourcemember = source;
	}
	
	public OffsetDateTime	getDateValue() {
		return null;
	}
	
	public void setDateValue(OffsetDateTime datevalue) {
		throw new KbeeRuntimeException("setDateValue(OffsetDateTime datevalue) not implemented");
	}

	public String getStrValue() {
		
		if (getDataSetType()==DataSetType.DATE)
			return null;
		else
			return getDataSetMember().getStrValue();
	}

	public DataSetType getDataSetType() {
		if (classifier!=null && classifier.getDataSet()!=null)
			return classifier.getDataSet().getDataSetType();
		return null;
	}
	
	public Object getValue() {
		if (getDataSetType()==DataSetType.DATE)
			return (Object) getDateValue();
		return (Object) getStrValue();
	}
	
	public DataSetMember getDataSetMember()	{
			return datasetmember;
	}
	
	public void setDataSetMember(DataSetMember value) {
		this.datasetmember=value;
	}

	public int getPosition() {
		return position;
	}
	
	public void setPosition(int pos) {
		this.position=pos;
	}

	public Classifier getClassifier() {
		return classifier;
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifier=classifier;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		if (classifier!=null && classifier.getDataSet().getDataSetType()==DataSetType.DATE)
				str.append("\ndate: " + getDateValue());
			else
				str.append("\nvalue: " + getStrValue());
		
		if (classifier!=null) 
			str.append("\nclassifier: " + getClassifier().getName());
		
		if (getDataSetMember()!=null && getDataSetMember().getValue()!=null)
			str.append("\ndataset member: " + getDataSetMember().getValue().toString());
		else
			str.append("\ndataset member: null");
		return str.toString();
	}
	
	public Classification clone() {
		KbeeMemberClassification clone = new KbeeMemberClassification();
		
		
		clone.setClassifier(getClassifier());
		clone.setSourceMember(getSource());
		clone.setDateValue(getDateValue());
		clone.setDataSetMember(getDataSetMember());
		return clone;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getAlternativeDisplayValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayName() {
		return getClassifier()!=null?getClassifier().getName():"-";
	}

	
	@Override
	public String getStrValue(Locale locale) {
		return getStrValue(Locale.getDefault());
	}


}
