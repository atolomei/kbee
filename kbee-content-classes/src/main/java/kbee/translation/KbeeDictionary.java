package kbee.translation;

import java.io.Serializable;
 import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.security.audit.AuditSet;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "kb_dictionary")
public class KbeeDictionary extends AbstractObject implements Dictionary {
	
	@Id 
	@SequenceGenerator(name = "dictionary_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dictionary_sequencer")
	
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "locale")
	private String localevalue;
	
	@OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, targetEntity = KbeeDictionaryEntry.class)
	@JoinColumn(name = "dictionary_id", nullable=false) 
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
	List<KbeeAclEntry> entries = new ArrayList<KbeeAclEntry>();
	
	@Override
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	@Override
	public String getName() {
		return getLocale().toString();
	}

	public Locale getLocale() {
		return null;
	}
	
	public List<KbeeAclEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<KbeeAclEntry> entries) {
		this.entries = entries;
	}

	public String get(String key) {
		return null;
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.MODEL;
	}
}
