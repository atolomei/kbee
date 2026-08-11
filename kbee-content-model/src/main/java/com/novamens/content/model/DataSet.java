package com.novamens.content.model;

import java.util.List;

import com.novamens.dom.Indexable;
import com.novamens.indexer.query.ResultSet;
import com.novamens.security.acl.Acl;
	
/**
 * <p>A DataSet is an entity in the client's Information Model 
 * DataSet instances are {@link DataSetMember}. Contents can be tagged with DataSetMembers 
 * via a {@link Classifier} on a {@link ContentTemplate}</p>
 */
public interface DataSet extends ModelObject, Indexable {

	public final String DOCUMENT_TYPE = "document-type";
	public final String STATUS = "status";
	
	public final String PORTAL = "portal";
	public final String PORTAL_REPOSITORY = "portal-repository";
	
	public final String PORTAL_PROJECTS = "portal-projects";
	public final String PORTAL_HOME_DATASET_ALIAS = "portalhome";
	
	public String getName();
	public void setName(String name);
	public String getDisplayName();
	
	//public void setAlias(String abb);
	public String getAlias();
	
	public DataSetMember createMember();
	
	public DataSetType getDataSetType();
	
	public void setEnabled(boolean value);
	public boolean isEnabled();
	
	public boolean isHierachical();
	//public void setHierachical(boolean value);
	
	public List<Classifier> getClassifiers();
	public void setClassifiers(List<Classifier> classifiers);
	
	public boolean isAFunctionOf(DataSet dataset);
	
	public List<AttributeTemplate> getAttributes();
	public void setAttributes(List<AttributeTemplate> attributes);
	
	public String getAlternativeDisplayName();
	
	public ExtractionRule getDisplayNameRule();
	public ExtractionRule getSublineRule();
	
	public boolean isDisplayNameEditable();
	
	public List<ModelElementTemplate> getStructure();
	
	// Maps a External Entity (like Site or User from LDAP)
	public boolean isExternal();
	public boolean isSuggester();
	
	public DataSet clone();
	
	public boolean isAggregation();
	
	public boolean isCanonical();
	
	public String getExternalId();
	public void setExternalId(String externalId);
	
	public boolean isReadonly();
	
	public boolean isUniqueValues();
	
	public String getDescription();
	
	// Strategy for Search forms and Selectors
	public AccessStrategy getAccessStrategy();
	
	public ResultSet getData();
	public Acl getACL();
	
	default public String getModelObjectClassName() { return "DataSet";}
	public  String getDisplayNameTemplate();
	public  String getConsoleDisplayNameTemplate();
}	