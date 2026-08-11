package com.novamens.kbee.content.form;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import com.novamens.content.base.Content;
import com.novamens.content.base.SignedData;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EListField;
import com.novamens.dom.Json;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.base.KbeeSignedData;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.json.KbeeJson;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_Form_Data")
@Proxy(lazy=false)
public class KbeeEFormData implements EFormContentData {

	@Id
	@SequenceGenerator(name = "data_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_sequencer")
	@Column(name = "ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeContent.class)
	@JoinColumn(name="content_id")
	private Content content;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeEForm.class)
	@JoinColumn(name="form_id")
	private EForm form;	
	
	@Column(name = "lastModifiedDate")
	private OffsetDateTime lastModifiedDate;
	
	@Column(name = "data")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.json.JsonType")
	private Json jsondata;
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeSignedData.class)
	@JoinTable(name = "KB_SIGNEDFORM_DATA", 
	joinColumns = {	@JoinColumn(name = "DATA_ID", nullable = false, updatable = false) }, 
		inverseJoinColumns = { @JoinColumn(name = "SIGNED_ID", nullable = false, updatable = false) })
	@Fetch(FetchMode.SELECT)
	private List<SignedData> signatures = new ArrayList<SignedData>();

	
	public KbeeEFormData() {
		
	}

	public KbeeEFormData(Content content, EForm form) {
		this.content = content;
		this.form = form;
	}
		
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	@Override
	public Content getContent() {
		return content;
	}
	
	public void setContent(Content content) {
		this.content = content;
	}

	@Override
	public Object getData(String name) {
		return null;
	}
	
	@Override
	public Object getObject(String name) {
		return null;
	}
	
	public Json getData() {
		return jsondata;
	}
	
	public void setData(Json data) {
		this.jsondata = data;
	}
	
	@Override
	public <T> List<T> getValues(EListField<T> field) {
		return new ArrayList<T>();
	}
	
	@Override
	public Object getData(EFormField<?> field) {
		if (jsondata==null) {
			return null;
		}
		Object value = jsondata.get(field.getName());
		if (field.getModel() instanceof KbeeEFormAttributeModel && value!=null) {
			value = ((KbeeEFormAttributeModel<?>)field.getModel()).getValueOf(value.toString());
		}
		return value;
	}
	
	public void setData(String name, Object value) {
		if (jsondata==null) {
			jsondata = new KbeeJson();
		}
		if (value!=null) {
			if (value instanceof List<?>) {
				jsondata.put(name, (List<?>)value);
			}
			else {
				jsondata.put(name, value.toString());
			}
		}	
		else {
			jsondata.remove(name);
		}
		setLastModifiedOffsetDateTime(OffsetDateTime.now());
	}
	
	public void setData(EFormField<?> field, Object value) {
		setData(field.getName(), value);
	}
	
	@Override
	public boolean isEmpty() {
		Object value = null;
		for (EFormField<?> field : form.getFields()) {
			value = field.getModel().get(getContent());
			if (value!=null) return false;
		}
		return true;
	}
	
	public boolean isSigned() {
		return !getSignatures().isEmpty();
	}
	
	public List<SignedData> getSignatures() {
		return signatures;
	}
	
	@Override
	public void setSignature(SignedData signature) {
		this.signatures.add(signature);
	}
	
	@Override
	public void setSignatures(List<SignedData> signatures) {
		this.signatures.clear();
		this.signatures.addAll(signatures);
	}
	
	public void clearSignatures() {
		this.signatures.clear();
	}

	@Override
	public EForm getForm() {
		return new KbeeTaskForm(form);
	}
	
	public void setForm(EForm form) {
		this.form = form;
	}
	
	
	@Override
	public String getObjectTitle() {
		return getContent().getTitle();
	}

	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		this.lastModifiedDate = date;
	}
	
	@Override
	public EFormData clone() {
		KbeeEFormData clone = new KbeeEFormData();
		clone.setForm(form);
		clone.setContent(getContent());
		clone.setData(getData());
		return clone;
	}
}