package com.novamens.kbee.content.base;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.SignedData;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserSignature;
import com.novamens.kbee.content.user.KbeeUserDevice;
import com.novamens.kbee.content.user.KbeeUserSignature;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.Identifiable;

import kbee.util.logging.Logger;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "kb_Signed_Data")
public class KbeeSignedData implements SignedData, Identifiable {
	
	private static Logger logger = Logger.getLogger(KbeeSignedData.class.getName());
	
	@Id
	@SequenceGenerator(name = "signed_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "signed_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "date")
	private OffsetDateTime date;
	
	@Column(name = "signed_data")
	private String signedData;
	
	@Column(name = "snapshot")
	private String snapshot;
	
	@Column(name = "resources")
	private String resources;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUserSignature.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "signature_id")
	private UserSignature signature;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUserDevice.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "device_id")
	private UserDevice device;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public UserSignature getSignature() {
		return signature;
	}

	public void setSignature(UserSignature signature) {
		this.signature = signature;
	}

	public OffsetDateTime getDate() {
		return date;
	}

	public void setDate(OffsetDateTime date) {
		this.date = date;
	}

	public String getSignedData() {
		return signedData;
	}

	public void setSignedData(String signedData) {
		this.signedData = signedData;
	}

	public String getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}
	
	
	public UserDevice getDevice() {
		return device;
	}

	public void setDevice(UserDevice device) {
		this.device = device;
	}

	public void addResource(com.novamens.content.base.Resource resource) {
		List<SignedData.Resource> resources = getResources();
		SignedData.Resource signed = new SignedData.Resource();
		signed.setId(String.valueOf(resource.getId()));
		signed.setDigest(getDigest(resource));
		resources.add(signed);
		this.resources = write(resources);
	}

	public List<SignedData.Resource> getResources() {
		return parse(this.resources);
	}
	
	public String getDigest() {
		String digest = getSnapshot();
		if (resources!=null) {
			digest += resources;
		}
		return digest;
	}

	@Override
	public String getDisplayName() {
		return String.valueOf(id);
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeSignedData)) return false;
		return ((KbeeSignedData)object).getId().equals(getId());
	}
	
	private String getDigest(com.novamens.content.base.Resource resource) {
		String digest;
		if (resource instanceof KBFile) {
			digest = ((KBFile)resource).getSHA256();
		}
		else {
			digest = resource.toString();		
		}
		return digest;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String write(List<SignedData.Resource> resources) {
		KbeeJson json = new KbeeJson();
		List<Map<String, String>> jsonresources = new ArrayList<Map<String, String>>();
		for (SignedData.Resource resource : resources) {
			Map resourcemap = new HashMap();
			resourcemap.put("id", resource.getId());
			resourcemap.put("digest", resource.getDigest());
			jsonresources.add(resourcemap);
		}
		if (!jsonresources.isEmpty())
			json.put("resources", jsonresources);
		else
			return null;
		return json.toString();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<SignedData.Resource> parse(String string) {
		List<SignedData.Resource> resources = new ArrayList<SignedData.Resource>();
		if (string==null) return resources;
		try {
			KbeeJson json = new KbeeJson(string);
			List<Map> resourcesmaps = (List<Map>)json.get("resources");
			if (resourcesmaps!=null) {
				for (Map map : resourcesmaps) {
					SignedData.Resource resource = new SignedData.Resource();
					resource.setId((String)map.get("id"));
					resource.setDigest((String)map.get("digest"));
					resources.add(resource);
				}
			}
		}	
		catch (Exception e) {
			logger.error(e);
		}
		return resources;
	}
}