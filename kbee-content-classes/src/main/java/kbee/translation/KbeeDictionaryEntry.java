package kbee.translation;

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

@Entity
@Table(name = "kb_dictionary_entry")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
public class KbeeDictionaryEntry {
	
	@Id 
	@SequenceGenerator(name = "entry_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entry_sequencer")
	@Column(name = "ID")
	private Long id;
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}

}
