package com.novamens.content.model;

import com.novamens.content.base.ContentClass;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Indexable;
import com.novamens.security.acl.Acl;


import java.util.List;

/**
 * 
 * <p>
Classifier is conceptually the "tags" that can be applied to RPPD FIles.
Technically it is a relation template between a Content and a DataSet Value. Classifiers are used by RPPD :
As part of the Structure of Content Template, Users and DataSets
By the Search Engine to dynamically generate Filters and also to gauge the relevance algorithm.
by the Security Model for Roles
By IQL Language. A classifier defines a IQL Predicate that can be used to define conditions across the system (Libraries and Sites, Roles, ….)
Sample Classifiers: Owner, Property, Document Type, Status
Note that most Classifiers will refer to a DataSet with the same name.
RPDD Files can be tagged with DataSet Values by adding a Classifier to the ContentTemplate. 
The field on the File form has a selector of DataSet Values.
</p>
 * 
 * see also
 * 
 * {@link Dataset}
 * {@link Classifier}
 * {@link ContentTemplate}

 *
 */
public interface Classifier extends ModelElement, Indexable {
	
	
	public final String STATUS_CLASSIFIER_ALIAS				= "status";
	public final String STATUS_CLASSIFIER_PREDICATE 		= "status";
	public final String STATUS_CLASSIFIER_SOLR 				= "status";

	public final String PORTAL_HOME_CLASSIFIER_ALIAS 		= "portalhome";
	public final String PORTAL_HOME_CLASSIFIER_SOLR 		= "portalhome-NOT SUPPOTED";
	public final String PORTAL_HOME_PREDICATE				= "portalhome";
	
	public final String PORTAL_PROJECTS_CLASSIFIER_ALIAS 	= "site-projects";
	public final String PORTAL_PROJECTS_SOLR 				= "site-projects"; // VER
	public final String PORTAL_PROJECTS_PREDICATE 			= "siteProjects";
	
	public final String PORTAL_CLASSIFIER_ALIAS 			= "portal";
	public final String PORTAL_CLASSIFIER_SOLR	 			= "clsitename";
	public final String PORTAL_CLASSIFIER_PREDICATE			= "portal";
	
	public final String PORTAL_REPOSITORY_ALIAS		 		= "siteRepository";
	public final String PORTAL_REPOSITORY_PREDICATE 		= "siteRepository";
	public final String PORTAL_REPOSITORY_SOLR				=  "clsitereponame";
	
	public void setName(String name);
	
	public Multiplicity getMultiplicity();
	//public void setMultiplicity(Multiplicity value);
	public boolean isMandatory();
	
	// Unique key for API and other
	public void setAlias(String aalias);
	public String getAlias();

	// Unique name for SolR indexer
	//public void setUniqueName(String uniquename);
	public String getUniqueName();
	
	public DataSet getDataSet();
	public DataSet getDataSet2();
	public boolean includes(DataSet dataset);
	public DataSetType getDataSetType();
	
	// Predicate for IQL Rules
	//public void setPredicate(String predicate);
	public String getPredicate();

	public boolean isOrdered();
	public int getOrder();
	
	public boolean isDisplayable();
	//public void setDisplayable(boolean value);

	// Usado para Semantic Distance
	
	public boolean isSemantic();
	
	// Semantica:
	
	public boolean isContentType();
	public boolean isDistribution();
	public boolean isOrganization();
	public boolean isIdentityDocumentType();
	public boolean isWorkflowStatus();
	public boolean isMyDocument();
	public boolean isHierarchical();
	public boolean hasHome();
	
	// Es Default Grid Column en las Grids
	public boolean isDefaultGridColumn();
	//public void setDefaultGridColumn(boolean val);
	
	// Usado en subtitulos que requiren la metadata (Tasks, Portal)
	public boolean isMetadataSubtitle();
	//public void setMetadataSubtitle(boolean val);
	
	public boolean isVisible(String context);
	public void setVisibility(String context, boolean value);
	public Acl getACL();
	public void cleanDatasets();
	public void addDataSet(DataSet dataset) throws ContentMgmtException;
	public List<ContentClass> getContentClassesEnabled();
	public void removeContentClass(ContentClass contentclass); 
	public void addContentClass(ContentClass contentclass);
	public void setOrder(int order);
	public boolean isRuleCondition();
	public boolean isAPIClassifier();
	public boolean isPortalSubtitle();
	public boolean isPortal();
	public boolean isSearchable();

	
	public Classifier clone();
	
	default public String getModelObjectClassName() { return "Classifier";}
}