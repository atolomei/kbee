package com.novamens.content.web.migration.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ENTITYMATCHING")
public class EntityMatching implements Serializable {
	
	private static final long serialVersionUID = -939290086875008130L;

	@Column(name = "kbee_class_name")
	private String kbeeClassName;
	
	@Column(name = "lastModifiedDate")
	private OffsetDateTime lastModifiedDate;
	
	@Column(name = "class_name")
	private String className;
	
	@Column(name = "id")
	private String id;

	@EmbeddedId
	private EntityMatchingId compositeid;
	
	public EntityMatching() {
		super();
	}

	public EntityMatching(String koId, String kbeeClass, OffsetDateTime fechaModificacion, String nameClass, String id, String url) {
		super();
		this.kbeeClassName = kbeeClass;
		this.lastModifiedDate = fechaModificacion;
		this.className = nameClass;
		this.id= id;
		compositeid = new EntityMatchingId(koId, url);
	}

	public String getKbeeClassName() {
		return kbeeClassName;
	}

	public void setKbeeClassName(String kbeeClassName) {
		this.kbeeClassName = kbeeClassName;
	}

	public OffsetDateTime getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Serializable getCompositeId() {
		return id;
	}

	
	@Embeddable
	public static class EntityMatchingId implements Serializable {
		private static final long serialVersionUID = 1L;

		@Column(name = "kbee_id")
		private String koId;

		@Column(name = "url")
		private String url;

		public EntityMatchingId() {
		}

		public EntityMatchingId(String koId, String url) {
			this.koId = koId;
			this.url = url;
		}
		
		@Override
		public int hashCode() {
			return super.hashCode();
		}

	}
	
}
