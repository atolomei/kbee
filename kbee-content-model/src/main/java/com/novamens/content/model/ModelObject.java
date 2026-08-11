package com.novamens.content.model;

import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.security.audit.AuditSet;

/**
 * 
 * <p>The Information Model is made of:</p>
 * 
 * <ul>
 * 
 * <li>{@link Attribute}
 * 	Free entry field for text / Date / Number
 * </li>

 * <li>{@link DataSet}. 
 * Master files of entities, like Property, Property Management Company, Document Type. 
 * </li>
 * 
 * <li>{@link Classifier}
 * It is a Relationship between Content Template and DataSet. Classifiers are used by RPPD :
	As part of the Structure of Content Template, Users and DataSets
	By the Search Engine to dynamically generate Filters and also to gauge the relevance algorithm.
	by the Security Model for Roles
	By IQL Language. A classifier defines a IQL Predicate that can be used to define conditions across the system (Libraries and Sites, Roles, ….)
	Sample Classifiers: Owner, Property, Document Type, Status
	Note that most Classifiers will refer to a DataSet with the same name.
 </li>
 * 
 * <li>{@link ContentTemplate}
 *   A "File" is  a container of binary files (like XLSX, pdf, MS Word, ...)  with tags and other information elements. Technically these “tags” are Classifiers and Attributes. 
     A Content Template is where RPDD defines the characteristics of the RPPD File that is created using that Template.
     The Content Template is where RPDD defines, among other things,  what tags that RPPD File supports. This is called "Template Structure".
     Sample Content Templates: Compliance File, DocuSign Certificate, Work Order Request.
 * </li>
 * 
 * <ul>
 */
public interface ModelObject extends com.novamens.dom.Object, DomainObject  {

	public String getDescription();
	public String getAlias();
	boolean isOnlyRootEdit();
	
	
	public default String getModelObjectClassName() { return this.getClass().getCanonicalName();}
	
	public default AuditSet getAuditSet() {
		return AuditSet.MODEL;
	}

	
	
	void setName(String name);
	
}