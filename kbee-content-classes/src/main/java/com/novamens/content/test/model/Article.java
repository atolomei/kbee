package com.novamens.content.test.model;

import java.util.Date;
import java.util.Map;
import java.io.File;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;



import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.kbee.content.base.KbeeContent;

@Entity
@PrimaryKeyJoinColumn(name="CONTENT_ID")
@Table(name = "ARTICLE")
public class Article extends KbeeContent  {

	@Column(name = "ARTICLE_ID")
	private String articleid;
	
	@Column(name = "ARTICLE_TEXT")
	private String text;
	
	@Column(name = "ARTICLE_DATE")
	private Date date;
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "ARTICLE_AUTHOR_ID")
	private Author author;
	
	//FIXME: para probar el Extractor de Contenido de Archivo al indexar con Solr
	@Transient
	private File file;
	
	public Article() {
	}
	
	public Article(String text) {
		this.text = text;
	}
	
	public String getArticleId() {
		return articleid;
	}
	
	public void setArticleId(String id) {
		this.articleid = id;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public Author getAuthor() {
		return author;
	}
	
	public void setAuthor(Author author) {
		this.author = author;
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	
}