package com.novamens.kbee.content.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Column;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelDefaultSection;
import com.novamens.content.model.ModelElementTemplate;

@Deprecated
public class KbeeDefaultSection implements ModelDefaultSection {

	private Long id;
	
	private String name;
	
	@Column(name = "description")
	private String description;
	
	private ContentTemplate template;
	
	public KbeeDefaultSection() {
		super();
	}
	
	public KbeeDefaultSection(ContentTemplate template) {
		super();
		this.template = template;
	}
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	@Override
	public String getName()	{
		return name;
	}
	
	public void setName(Serializable id) {
		this.id = (Long)id;
	}
	
	@Override
	public String getDescription()	{
		return description;
	}
	
	public void setDescription(Serializable id) {
		this.id = (Long)id;
	}
	
	public ContentTemplate getContentTemplate() {
		return template;
	}
	
	public List<ModelElementTemplate> getStructure() {
		List<ModelElementTemplate> structure = new ArrayList<ModelElementTemplate>();
		structure.addAll(getContentTemplate().getClassifiers());
		structure.addAll(getContentTemplate().getAttributes());
		Collections.sort(structure, new Comparator<ModelElementTemplate>() {
			@Override
			public int compare(ModelElementTemplate a, ModelElementTemplate b) {
				return a.getOrder() < b.getOrder() ? -1 : 1;
			}
		});
		return structure;
	}
	
	public void setStructure(List<ModelElementTemplate> structure) {
		KbeeModelSection section = new KbeeModelSection(getContentTemplate());
		section.setName(getName());
		section.setStructure(structure);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeDefaultSection)) return false;
		return getId()!=null && getId().equals(((KbeeDefaultSection)object).getId());
	}

	@Override
	public boolean isPortal() {
		// TODO Auto-generated method stub
		return true;
	}
}