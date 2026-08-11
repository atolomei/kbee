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
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 * Enabled, Archived, Deleted
 *
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "Classification")
public class KbeeClassification implements Classification  {
	
	@Id
	@GenericGenerator(
		name = "classification_sequencer",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "classificationid_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "hilo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "classification_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "datevalue")
	private OffsetDateTime datevalue;
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeClassifier.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="classifier_id", nullable=false)
	private Classifier classifier;
	
	@Column(name = "position", insertable=false, updatable=false)
	private int position;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeContent.class)
	@JoinColumn(name="content_id", insertable=false, updatable=false, nullable=false)
	private Content content;
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeDataSetMember.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="datasetmember_id")
	private DataSetMember datasetmember;
	
	public KbeeClassification() {
		super();
	};
	
	public KbeeClassification(Classifier classifier, OffsetDateTime date, Content content) {
		super();
		setDateValue(date);
		setClassifier(classifier);
		setContent(content);
	};
	
	public KbeeClassification(Classifier classifier, DataSetMember datasetmember, Content content) {
		super();
		setDataSetMember(datasetmember);
		setClassifier(classifier);
		setContent(content);
	};
	
	public Content	getContent() {
		return content;
	}
	
	public void setContent(Content content) {
		this.content=content;
	}
	
	public OffsetDateTime getDateValue() {
		return datevalue;
	}
	
	public void setDateValue(OffsetDateTime datevalue) {
		this.datevalue=datevalue;
	}

	public String getAlternativeDisplayValue() {
		if (getDataSetMember().getAlternativeDisplayName()!=null)
			return getDataSetMember().getAlternativeDisplayName();
		else 
			return getStrValue();
	}

	@Override
	public String getStrValue() {
		if (getDataSetType()==DataSetType.DATE) {// this is legacy, Date are no longer Classifiers (they are Attributes)
			User user = getSessionUser();
			return getStrValue(user!=null ? user.getLocale() : Locale.getDefault());  // eLocale.getDefault()
		}		 
		else
			return getDataSetMember().getDisplayName();
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	@Override
	public String getStrValue(Locale locale) {
		if (getDataSetType()==DataSetType.DATE) {  // legacy , should not be used
			if (datevalue!=null) 
				return ServiceLocator.getService(DateTimeService.class).getDateDisplayString(datevalue, locale, DateTimeService.DATE_FORMAT_GMT);
			else
			 return null;
		}
		else
			return getDataSetMember().getDisplayName();
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
		KbeeClassification clone = new KbeeClassification();
		clone.setClassifier(getClassifier());
		clone.setContent(getContent());
		clone.setDateValue(getDateValue());
		clone.setDataSetMember(getDataSetMember());
		return clone;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getDisplayName() {
		return (getContent()!=null?getContent().getTitle():"-");
	}
}
