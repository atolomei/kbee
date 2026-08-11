package com.novamens.kbee.content.communication;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.novamens.content.base.Content;
import com.novamens.content.base.RelationshipByCriteria;
import com.novamens.content.base.Resource;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classification;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.text.Text;
import com.novamens.kbee.content.base.KbeeResourceContainer;
import com.novamens.kbee.content.text.KbeeText;

@Entity
@PrimaryKeyJoinColumn(name="content_id")
@Table(name = "ORGANIZATIONALTEXT")
public class KbeeOrganizationalText extends KbeeResourceContainer implements OrganizationalText {
	
	@Column(name = "TEXT")
	private String 	text;
	
	@Column(name = "MEDIA")
	private String 	media;

	@Column(name = "summary")
	private String 	summary;

	public KbeeOrganizationalText() {
		super();
	}
	
	public KbeeOrganizationalText(ContentTemplate ct) {
		super(ct);
	}
	
	public Person getAuthor() {
		return null;
	}
	
	public Text getText() {
		return new KbeeText(text);
	}
	
	public void setText(String text) {
		this.text = (KbeeText.textOf(text)).asString();
	}
	
	public void setText(Text text) {
		this.text = text.asString();
	}
	
	public void setStringText(String text) {
		this.text = (KbeeText.textOf(text)).asString();
	}
	
	public String getStringText() {
		return this.text;
	}

	public void setAuthor(Person author) {
	}
	
	public String getMedia() {
		return media;
	}
	
	public void setMedia(String media) 	{
		this.media=media;
	}

	public void setSummary(String summary) {
		this.summary=summary;
	}
	
	public String getSummary() {
		return summary;
	}
	
	@Override
	public void setTitle(String title) {
		if (getName()==null)
			setName(title);
		super.setTitle(title);
	}
	
	/**
	 * TODO: Attributes ?
	 */
	
	@Override
	public Content clone() {
		KbeeOrganizationalText clone = new KbeeOrganizationalText();
		clone.setOId(getOId());
		clone.setDomain(getDomain());
		clone.setAbstract(getAbstract());
		clone.setState(getState());
		clone.setName(getName());
		clone.setTitle(getTitle());
		clone.setText(new KbeeText(this.text));
		clone.setContentTemplate(getContentTemplate());
		clone.setUserDefinedAttributes(this.getUserDefinedAttributes());
		
		List<Classification> clonedclassification = new ArrayList<Classification>();
		for (Classification classification : getClassification()) {
			Classification cc = classification.clone();
			clonedclassification.add(cc);
		}	 
		
		clone.setClassification(clonedclassification);
		
		for (Resource resource: getResources()) 
			clone.addResource(resource);
		
		for (RelationshipByCriteria relation : getRelationshipsByCriteria()) {
			clone.addRelation(relation.clone());
		}

	
		return clone;
	}
	
	@Override
	public String getClassCode() {
		return OrganizationalText.CLASS_CODE;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
 		str.append(super.toString());
 		if (getAuthor()!=null)
			str.append("\nauthor: " +getAuthor().getDisplayName());
 		if (getMedia()!=null)
			str.append("\nmedia: " + getMedia());
 		
 		if (getSummary()!=null)
			str.append("\nsummary: " + getSummary());
		return str.toString();
	}
}
