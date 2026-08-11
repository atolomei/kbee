package com.novamens.kbee.content.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.security.Identifiable;

//@Entity
//@Inheritance(strategy=InheritanceType.JOINED)
//@Table(name = "Kb_Model_Section")
@Deprecated
public class KbeeModelSection implements ModelSection, Identifiable {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeModelSection.class.getName());
	
	@Id
	@SequenceGenerator(name = "section_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "section_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@OneToOne(fetch = FetchType.LAZY, targetEntity=KbeeContentTemplate.class)
	@JoinColumn(name="contenttemplate_id", updatable=false, insertable=false, nullable=false)
	private ContentTemplate template;
	
	@Column(name = "position", insertable=false, updatable=false)
	private int position;
	
	
	@Column(name = "isportal")
	private boolean isportal;
	
	

	@Override
	public boolean isPortal() {
		return isportal;
	}


	public void setportal(boolean isportal) {
		this.isportal = isportal;
	}


	private transient boolean isDefault = false;

	
	public KbeeModelSection(KbeeModelSection src) {
		this.name=src.getName();
		this.description=src.getDescription();
		this.template= src.getContentTemplate();
		this.position=src.position;
		this.isportal=src.isportal;
	}
	
	
	public KbeeModelSection() {
		super();
	}
	
	public KbeeModelSection(ContentTemplate template) {
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
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getDisplayName()	{
		return name;
	}
	
	public void setPosition(int position) {
		this.position = position;
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
	
	public boolean isDefault() {
		return isDefault;
	}
	
	public void setDefault(boolean value) {
		isDefault = value;
	}
	
	public List<ModelElementTemplate> getStructure() {

		
		List<ModelElementTemplate> structure = new ArrayList<ModelElementTemplate>();
		try {
			
		
				for (ModelElementTemplate template : getContentTemplate().getClassifiers()) {
					//if (template!=null && ((template.getSection()==null && isDefault()) || (template.getSection()!=null && template.getSection().equals(this)))) {
					if (template!=null && isDefault()) {
						structure.add(template);
					}
				}
				for (ModelElementTemplate template : getContentTemplate().getAttributes()) {
					//if (template!=null && ((template.getSection()==null && isDefault()) || (template.getSection()!=null && template.getSection().equals(this)))) {
					if (template!=null && isDefault()) {
						structure.add(template);
					}
				}
//				for (ModelElementTemplate template : getContentTemplate().getSubsections()) {
//					if (template!=null && ((template.getSection()==null && isDefault()) || (template.getSection()!=null && template.getSection().equals(this)))) {
//						structure.add(template);
//					}
//				}
				
		} catch (Exception e) {
			logger.error(e);
		}
		
		
		Collections.sort(structure, new Comparator<ModelElementTemplate>() {
			@Override
			public int compare(ModelElementTemplate a, ModelElementTemplate b) {
				try {
					return a.getOrder() < b.getOrder() ? -1 : 1;
				} 
				catch (Exception e) {
					return 0;
				}
			}
		});
		return structure;
	}
	
	
	public void setStructure(List<ModelElementTemplate> structure) {
		
		List <ModelElementTemplate> templatestructure = getContentTemplate().getStructure();
		
		if (!templatestructure.isEmpty())
			templatestructure.clear();
		
//		List <ModelElementTemplate> sectionstructure = templatestructure.stream().
//			filter((element) -> element!=null && element.getSection()!=null && element.getSection().equals(this)).
//			collect(Collectors.toList());
//				
//		templatestructure.removeIf((element) -> element==null || (element.getSection()!=null && element.getSection().equals(this)));
		
		//sectionstructure.forEach((element) -> element.setSection(null));
				
		//structure.forEach((element) -> element.setSection(this));
			
		templatestructure.addAll(structure);
		
		getContentTemplate().setStructure(templatestructure);
		
//		if (!getContentTemplate().getSections().contains(this)) {
//			getContentTemplate().addSection(this);
//		}
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeModelSection)) return false;
		return getId()!=null && getId().equals(((KbeeModelSection)object).getId());
	}
	

	
}