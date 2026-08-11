package com.novamens.kbee.content.support;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;



@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "KB_TIP")
public class KbeeTip implements Tip {

	@Id
	@SequenceGenerator(name = "domain_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "domain_sequencer")
	@Column(name = "id")
	private Long id;

	@Column(name = "tip_title")
	private String title;
	
	@Column(name = "tip_text")
	private String text;
	
	@Column(name = "tip_lang")
	private String lang;
	
	@Column(name = "tip_texyid")
	private String texyid;
	
	@Column(name = "tip_area")
	private String area;

	
	
	@Transient
	int index;
	
	//@Column(name = "tip_domainid")
	//@Column(name = "status")

	
	public KbeeTip() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public String getLang() {
		return lang;
	}


	public void setLang(String lang) {
		this.lang = lang;
	}


	public String getTexyid() {
		return texyid;
	}


	public void setTexyid(String texyid) {
		this.texyid = texyid;
	}

	/*
	@Override
	public TexyModel getTexy() {
		if (getTexyid()==null)
			return null;
		
		return TexyModel.get(getTexyid());
	}
*/
	
	@Override
	public String toString() {
		
		StringBuilder str = new StringBuilder();
		
		if (getId()!=null)
			str.append(getId().toString());
		
		if (getTitle()!=null)
			str.append( (str.length()>0?"\n":"") + getTitle());
		
		
		if (getText()!=null)
			str.append( (str.length()>0?"\n":"") + getText());
		

		//if (getTexy()!=null)
		//	str.append( (str.length()>0?"\n":"") + getTexy().toString());


		if (getArea()!=null)
			str.append( (str.length()>0?"\n":"") + getArea());

		
		return str.toString();
		
		
	}

	@Override
	public void setIndex(int index) {
			this.index=index;		
	}

	@Override
	public int getIndex() {
		return index;
	}
	
	@Override
	public String getArea() {
		return area;
	}
	
	@Override
	public void setArea(String area) {
		this.area=area;
	}
	
}
