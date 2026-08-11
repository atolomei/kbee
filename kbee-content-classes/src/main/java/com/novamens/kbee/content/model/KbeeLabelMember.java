package com.novamens.kbee.content.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Type;

import com.novamens.content.model.LabelColor;
import com.novamens.content.model.LabelMember;
import com.novamens.content.model.LabelSet;

/**
 * DataSetType.LABEL
 */
@Entity
@DiscriminatorValue(value="10")
public class KbeeLabelMember extends KbeeValueMember implements LabelMember {

	@Column(name = "labelcolor", nullable=false, insertable = true, updatable = true)
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.LabelColorUserType")
	private LabelColor color;

	public KbeeLabelMember() {
		super();
	}
	
	public KbeeLabelMember(LabelSet ds) {
		super(ds);
		this.color = LabelColor.get(super.hashCode());
	}
	
	public void setLabelColor(LabelColor color) {
		this.color=color;
	}
	
	@Override
	public LabelColor getLabelColor() {
		return color;
	}
	
}
