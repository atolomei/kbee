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
import com.novamens.content.userlist.UserListItem;
import com.novamens.kbee.content.userlist.KbeeUserListItem;
import com.novamens.util.KbeeRuntimeException;


@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "kb_userlistclassification")
public class KbeeUserListClassification implements Classification {
			
	@Id
	@SequenceGenerator(name = "memberclassification_sequencer", sequenceName = "classificationid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "memberclassification_sequencer")
	@Column(name = "ID")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeClassifier.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="classifier_id", nullable=false)
	private Classifier classifier;
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeDataSetMember.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="datasetmember_id", nullable=false)
	private DataSetMember datasetmember;
	
	

	
	
	
	
	
	/** this is set by the UserListItem that holds this object in a list */
	@Column(name = "position", insertable=false, updatable=false)
	private int position;
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeUserListItem.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="user_list_item_id", nullable=true, insertable=false, updatable=false)
	private UserListItem userlistitem;

	
	public KbeeUserListClassification(Classifier classifier, DataSetMember datasetmember, UserListItem item) {
		super();
		setDataSetMember(datasetmember);
		setClassifier(classifier);
		setUserListItem(item);
	};
	
	public void  setUserListItem(UserListItem u) {
		this.userlistitem=u;
	}
	
	
	public UserListItem getUserListItem() {
		return this.userlistitem;
	}

	
	
	public KbeeUserListClassification() {
		super();
	};
	
	
	
	
	
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
		KbeeUserListClassification clone = new KbeeUserListClassification();
		clone.setUserListItem(this.getUserListItem());
		clone.setClassifier(getClassifier());
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
