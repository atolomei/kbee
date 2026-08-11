package com.novamens.kbee.portal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import com.novamens.portal6.model.ViewBKIQL;

@Entity
@Table(name = "PO_VIEWBKIQL")
@PrimaryKeyJoinColumn(name = "view_id")
@DynamicInsert
public class KbeeViewBKIQL extends KbeeViewBK implements ViewBKIQL {
			
	@Column(name = "iql")
	private String iql_or_qery_parameters;
	

	public KbeeViewBKIQL () {
	}

	public KbeeViewBKIQL (String title, String iql) {
		this.iql_or_qery_parameters = iql;
		setName(title);
		setTitle(title);
	}
	
	public KbeeViewBKIQL(String title, String iql, String description) {
		this.iql_or_qery_parameters = iql;
		
		com.novamens.dom.Json z = getCustomValuesJson();
		z.put("query_type",  ViewBKIQL.IQL_TYPE);
		setCustomValuesJson(z);
		setDescription(description);
		setName(title);
		setTitle(title);
	}

	public KbeeViewBKIQL(String title, String iql, String description, String query_type) {
		this.iql_or_qery_parameters = iql;
			if (query_type!=null) {
				com.novamens.dom.Json z = getCustomValuesJson();
				z.put("query_type", query_type);
				setCustomValuesJson(z);
			}
		
		setDescription(description);
		setName(title);
		setTitle(title);
		
	}
	
	public void setisIql( boolean b) {
		com.novamens.dom.Json z = getCustomValuesJson();
		z.put("query_type", b? ViewBKIQL.IQL_TYPE : ViewBKIQL.PARAMETERS_QUERY_TYPE);
		setCustomValuesJson(z);
				
	}
	public boolean isIql() {
		return getCustomValuesJson() == null || 
				getCustomValuesJson().get("query_type") == null ||
				getCustomValuesJson().get("query_type").equals(ViewBKIQL.IQL_TYPE);
	}
	
	@Override
	public KbeeViewBKIQL clone() {
		KbeeViewBKIQL clone = new  KbeeViewBKIQL();
		clone.iql_or_qery_parameters=iql_or_qery_parameters;
		onClone(clone);
		return clone;
	}

	public void onClone( KbeeViewBKIQL clone) {
		super.onClone((KbeeViewBK) clone);
		clone.setStatement(this.getStatement());
	}


	public String getSubtitle() {
		if (super.getSubtitle()==null)
			return this.iql_or_qery_parameters;
		return super.getSubtitle();
		
	}

	@Override
	public String getStatement() {
		return this.iql_or_qery_parameters;
	}

	@Override
	public Object getObject() {
		return this;
	}

	@Override
	public String getTitle() {
		if (super.getTitle() == null)
			return this.getId().toString();
		else
			return super.getTitle();
	}

	@Override
	public void setStatement(String iql) {
		this.iql_or_qery_parameters=iql;
		if (getName() == null)
			setName(iql);
	}

	@Override
	public String getViewType() {
		return KbeeViewBK.IQL_TYPE;
	}

	@Override
	public String getMetadataAsString() {
		StringBuilder str = new StringBuilder();
	
		str.append(getLastModifiedUser() != null ? getLastModifiedUser().getFirstLastName() + ". " : "");
		str.append(getLastModifiedOffsetDateTimeColloquial());
		return str.toString();
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append("\n" + getViewType());
		str.append("\nStatement: " + getStatement() != null ? getStatement() : "");
		return str.toString();
	}

	@Override
	public boolean isSearchable() {
		return false;
	}
	

}
