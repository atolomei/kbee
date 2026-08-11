package com.novamens.kbee.content.email;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import com.novamens.content.email.EmailTemplate;
import com.novamens.email.EmailService;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;
import com.novamens.text.TemplateModelInfo;
import com.novamens.text.TemplateModelParser;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "kb_email_template")
@DynamicInsert
public class KbeeEmailTemplate extends AbstractObject implements EmailTemplate {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeEmailTemplate.class.getName());
	
	@Id 
	@SequenceGenerator(name = "template_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "lang")
	private String lang;
	
	@Column(name = "strtext")
	private String text;
	
	@Column(name = "plaintext")
	private String plaintext; 
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "subject")
	private String subject; 

	@Column(name = "fromstr")
	private String from;
	
	@Column(name = "xkey")
	private String key;
	
	@Column(name = "isdefault")
	private boolean isdefault; 

	@Column(name = "isdeletable")
	private boolean is_deletable; 
	
	@Column(name = "available_macros")
	private String available_macros; 

	@Column(name = "defaultValue")
	private String defaultValue;
	
	@Column(name = "model")
	private String t_model;
	
	public KbeeEmailTemplate() {
			
	}

	public KbeeEmailTemplate(
			String key, 
            String title,
            String lang, 
            String subject) {
		this (key, null, title, lang, subject, null, null);
	}

		
	public KbeeEmailTemplate(String key, 
			                 String from, 
			                 String title,
			                 String lang, 
			                 String subject, 
			                 String text, 
			                 String available_macros) {
		
		this.title=title;
		this.lang=lang;
		this.text=text;
		this.subject=subject;
		this.key=key;
		this.from=from;
		this.available_macros=available_macros;
		this.is_deletable=true;
	}
	
	@Override							
	public void setId(Serializable id) 					{this.id=(Long) id;}
	
	@Override
	public String getTitle() 							{return title;}
	public void setTitle(String  title) 				{this.title=title;}
	
	
	@Override
	public String getDescription() 							{return description;}
	public void setDescription(String  d)	 				{this.description=d;}
	
	@Override
	public String getLanguage() 						{return lang;}
	public void setLanguage(String  lang) 				{this.lang=lang;}

	@Override
	public String getStringTemplate() {
		if (text==null)
			return getDefaultStringTemplate(); 
		return text;
	}
	
	public void setStringTemplate(String  text)  {
		this.text=text;
	}
	
	@Override
	public String getPlainTextTemplate() {
		return plaintext;
	}
	
	public void setPlainTextTemplate(String  text)  {
		this.plaintext=text;
	}
	
	@Override
	public String getSubject() 							{
		if (subject==null)
			return getDefaultSubject();
			return subject;
	}
	
	public void setSubject(String  title) 				{this.subject=title;}
	
	@Override
	public String getFrom() 							{
		if (from==null)
			return ServiceLocator.getService(EmailService.class).getNoReplyEmailAddress();
		
		return from;
	}
	
	public void setFrom(String  from) 					{this.from=from;}
	
	
	public String getAvailable_macros() {
		return available_macros;
	}

	public void setAvailable_macros(String available_macros) {
		this.available_macros = available_macros;
	}

	@Override
	public boolean isDeletable() {
		return is_deletable;
	}


	@Override
	public void setDeletable(boolean b) {
		this.is_deletable=b;
	}
	  
	  
	public String getDefaultValue() 					{return this.defaultValue;}
	public void setDefaultValue(String  from) 			{this.defaultValue=from;}
	
	
	@Override
	public String getDisplayName() {
		return this.getTitle()!=null?this.getTitle():this.getKey();
	}

	@Override
	public String toString() {
		StringBuilder str  = new StringBuilder(); 
		str.append(getId()!=null? getId().toString(): "");
		
		str.append(getDomain()!=null?  (" | " + getDomain().getId().toString()): "");
		str.append(getKey()!=null?  (" | " + getKey()): "");
		str.append(getLanguage()!=null?  (" | " + getLanguage()): "");
		str.append(getSubject()!=null?  (" | " + getSubject()): "");
		str.append(getStringTemplate()!=null? (" | " + getStringTemplate()): "");
		return str.toString();
	}
	
	public void setDefault( boolean b) {
		this.isdefault=b;
	}
	
	public boolean isDefault() {
		return this.isdefault;
	}
	
	@Override
	public Serializable getId() {
		return this.id;
	}
	
	
	/**
	 * 
	 */
	public KbeeEmailTemplate clone() {
		
		KbeeEmailTemplate nt = new KbeeEmailTemplate();
		super.onClone(nt);

		nt.setFrom(getFrom());
		nt.setKey(getKey());
		nt.setLanguage(getLanguage());
		nt.setState(getState());
		
		nt.setStringTemplate(this.text);
		nt.setSubject(this.subject);
		nt.setTitle(getTitle());
		nt.setDescription(getDescription());
		nt.setDeletable(this.isDeletable());
		nt.setModel(this.t_model);
		
		
		
		return nt;
	}
	
	@Override
	public String getKey() {return this.key;}
	
	public void setKey(String key) {this.key=key;}

	@Override
	public String getName() {
		return id.toString();
	}
	
	@Override
	public String getSubjectHTML() {
		return getTextEscaped(getSubject()); 
	}
	
	@Override
	public String getTextHTML() {
		return getTextEscaped(this.getStringTemplate());
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
	
	private String getTextEscaped(String s) {
		
		if (s==null)
			return null;
		
		String t1 = Jsoup.clean(s, Safelist.basic());
		
		t1 = t1.replace("&", "&amp;");
		t1 = t1.replace("<", "&lt;");
		t1 = t1.replace(">", "&gt;");
		
		String prefix="<span class=\"macro\">";
		String suffix="</span>";
		String b=t1.replace("${", prefix + "${").replace("}","}"+suffix);
		
		return b; 
	}

	public String getSubjectField() {return this.subject;}
	public String getTextField()  {return this.text;}
	
	
	@Override
	public String getDefaultStringTemplate() {
															
		if (this.getLanguage()==null)
			throw new IllegalArgumentException(" lang is null");
		
		EmailTemplate t=ServiceLocator.getService(EmailService.class).getDefaultTemplates( this.getLanguage() ).get(this.getKey());
		
		if (t!=null) {
			return t.getTextField();
		}
		
		logger.error("no default email template for key = '" + this.getKey()+"'");
		return "";
	}

	
	public TemplateModelInfo getDefaultModel() {
															
		if (this.getLanguage()==null)
			throw new IllegalArgumentException(" lang is null");
		
		EmailTemplate t=ServiceLocator.getService(EmailService.class).getDefaultTemplates( this.getLanguage() ).get(this.getKey());
		
		if (t!=null) {
			return t.getModel();
		}
		
		logger.error("no default email template for key = '" + this.getKey()+"'");
		return null;
	}
	
	@Override
	public String getDefaultStringTemplateHTML() {
		String s=getDefaultStringTemplate();
		if (s!=null)
			return getTextEscaped(s);
		return null;
	}

	@Override
	public TemplateModelInfo getModel() {
		if (t_model==null)
			return 	getDefaultModel();
		return TemplateModelParser.Get().getModel(this.t_model);
	}

	@Override
	public String getStrModel() {
		return t_model;
	}
	
	public void setModel(String model) {
		this.t_model = model;
	}
	
	private String getDefaultSubject() {
		if (this.getLanguage()==null)
			throw new IllegalArgumentException(" lang is null");
		EmailTemplate t = ServiceLocator.getService(EmailService.class).getDefaultTemplates( this.getLanguage() ).get(this.getKey());
		if (t!=null)
				return t.getSubjectField();
		logger.error("no default email template for key = '" + this.getKey()+"'");
		return "";
	}
}
