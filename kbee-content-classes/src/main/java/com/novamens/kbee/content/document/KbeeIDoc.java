package com.novamens.kbee.content.document;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentLink;
import com.novamens.content.base.ContentResource;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Resource;

import com.novamens.content.document.IDoc;
import com.novamens.content.form.EFormData;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.content.base.KbeeContentLink;
import com.novamens.kbee.content.base.KbeeTreeFileResourceContainer;
import com.novamens.kbee.content.model.KbeeRelation;

@Entity
@PrimaryKeyJoinColumn(name="content_id")
@Table(name = "idoc")
public class KbeeIDoc extends KbeeTreeFileResourceContainer implements IDoc  {
															
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeIDoc.class.getName());
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "editorialstate")
	private int editorialstate;
			
	@Column(name = "template_id")
	private Long template_id;

	public KbeeIDoc(ContentTemplate ct) {
		super(ct);
	}
	
	public KbeeIDoc() {
		super();
	}
								
	public String getTitle() {
		return title;
	}
				
	public void setTitle(String title) {
		super.setTitle(title);
		this.title=title;
	}
	
	@Override
	public String getClassCode() {
		return IDoc.CLASS_CODE;
	}

	@Override
	public Content clone() {

		KbeeIDoc clone = new KbeeIDoc();
		super.onClone(clone);
		
		clone.setOId(getOId());
		clone.setExternalId(getExternalId());
		clone.setExternalTime(getExternalTime());
		clone.setDomain(getDomain());
		clone.setState(getState());
		clone.setAbstract(getAbstract());
		clone.setName(getName());
		clone.setTitle(getTitle());
		clone.setContentTemplate(getContentTemplate());
		clone.setUserDefinedAttributes(this.getUserDefinedAttributes());
		clone.setSource(getSource());
		
		List<Classification> clonedclassification = new ArrayList<Classification>();
		for (Classification classification : getClassification()) {
			if (classification!=null) {
				Classification cc = classification.clone();
				clonedclassification.add(cc);
			}
		}	 
		
		clone.setClassification(clonedclassification);
		
		clone.setTreeFile(this.getTreeFile());
		
		
		for (ContentResource resource: getContentResources()) {
			if (resource!=null)
			clone.addResource(resource);
		}	
		
		for (AttributeTemplate template : getContentTemplate().getAttributes()) {
			List<String> values = getAttributeValues(template.getAttribute());
			if (!values.isEmpty()) {
				clone.setAttributeValues(template.getAttribute(), values);
			}
		}
		
		for (Relation relation : getReverseRelations()) {
			if (relation!=null) {
				KbeeRelation newrelation = (KbeeRelation)relation.clone();
				newrelation.setTarget(clone);
				newrelation.getSource().addRelation(newrelation);
			}
		}
		
		List<Relation> relations = new ArrayList<>();
		for (Relation relation : getRelations()) {
			relations.add(relation.clone());
		}
		clone.setRelations(relations);
		
		
		for (ContentLink link : getReverseLinks()) {
			if (link!=null) {
				KbeeContentLink newlink = (KbeeContentLink)link.clone();
				newlink.setTarget(clone);
				newlink.getSource().addLink(newlink);
			}
		}
		
		List<ContentLink> links = new ArrayList<>();
		for (ContentLink link : getLinks()) {
			links.add(link.clone());
		}
		clone.setLinks(links);
		
		for (EFormData data : getFormsData()) {
			clone.getFormsData().add(data.clone());
		}

		return clone;
	}
	
	
	/**
	 * <p>toString is used to display info of the Object for the developers</p>
	 */

	@Override
	public String toString() {
		
		try {
		
			StringBuilder str = new StringBuilder();
			
			str.append("Id: " + (getId()!=null ? String.valueOf(getId()):" null"));
			str.append(" | OId: " + (getOId()!=null ? String.valueOf(getOId()):" null"));
			str.append(" | ContentTemplate: " +  (getContentTemplate()!=null ? getContentTemplate().getName() : "null"));
			
			str.append(" | Domain: " + (getDomain()!=null ?  (String.valueOf(getDomain().getId())+ " " + getDomain().getName()):" null"));
			str.append(" | State: " + (getState()!=null ? getState().getLabel():" null"));
			str.append(" | Version: " + String.valueOf(getVersion()));

			if (getAbstract()!=null)
				str.append("| Abstract: " + getAbstract());
						
			str.append(" | Title: " + (getTitle()!=null?getTitle():""));
			str.append(" | Name: " + (getName()!=null?getName():""));

			List<Resource> list= getResources();
			
			if (list!=null && list.size()>0) {
				str.append(" | Resources " + "("+ String.valueOf(list.size())+ ")"   +":");
				int n=0;
				for (Resource res: list) {
					if (n++>0)	str.append(" | ");
					str.append((res.getTitle()!=null? res.getTitle():"") + " (" + (res.getId()!=null?res.getId().toString():"id null") +") ");
				}
			}

			return str.toString();
		
		} 
		catch (Exception e) {
			logger.error(e);
			return e.getClass().getName();
		}
	}
	


}










