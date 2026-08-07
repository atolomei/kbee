package com.novamens.logging.usage;

import java.io.Serializable;
import java.time.OffsetDateTime;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import com.novamens.dom.Json;
  
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="log")
@Table(name = "KB_USAGE_STAT")
public class KbeeUsageStat implements UsageStat, Serializable {
	
 	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "domain_id")
	private Long domain_id;
	
	@Id
	@Column(name = "ts")
	private OffsetDateTime timestamp;
	
	/** includes all Domains. It should only be used by kbee domain */
	@Column(name = "database_usage")
	private long database_usage;
	
	@Column(name = "hard_disk_usage")
	private long hard_disk_usage;

	//@Column(name = "kbfs1_hard_disk_usage")
	//private long kbfs1_hard_disk_usage;
	
	@Column(name = "kbfs2_hard_disk_usage")
	private long kbfs2_hard_disk_usage;

	@Column(name = "kbfs2archive_hard_disk_usage")
	private long kbfs2archive_hard_disk_usage;

	@Column(name = "odilon_hard_disk_usage")
	private long odilon_hard_disk_usage;
	
	@Column(name = "s3_hard_disk_usage")
	private long s3_hard_disk_usage;
	
	@Column(name = "glacier_hard_disk_usage")
	private long glacier_hard_disk_usage;

	/**
	 * External files
	 */
	@Column(name = "hard_disk_usage_gateway")
	private long hard_disk_usage_gateway;
	
	/**
	 * Total contents, including all its versions
	 */
	@Column(name = "contents")
	private long contents;


	@Column(name = "contents_external")
	private long contents_external;
	
	@Column(name = "contents_external_library")
	private long contents_external_library;

	@Column(name = "contents_external_archive")
	private long contents_external_archive;
	
	@Column(name = "contents_external_recycle")
	private long contents_external_recycle;

	@Column(name = "solr_content_items")
	private long solr_content_items;
	
	@Column(name = "solr_audit_items")
	private long solr_audit_items;
	
	@Column(name = "solr_file_items")
	private long solr_file_items;
	
	public long getSolRTotalContent() {return this.solr_content_items; 		}
	public long getSolRTotalAudit()   {return this.solr_audit_items;   		}
	public long getSolRTotalFile()    {return this.solr_file_items;    		}
										
	public void setSolRTotalContent(long l) {this.solr_content_items=l; 	}
	public void setSolRTotalAudit(long l)   {this.solr_audit_items=l;   	}
	public void setSolRTotalFile(long l)  	{this.solr_file_items=l;     	}
	
	
	/**
	 * Resources Stored
	 */
	@Column(name = "resources")
	private long resources;
	

	/**
	 * Resources External
	 */
	@Column(name = "resources_external")
	private long resources_external;
	
	
	@Column(name = "users")
	private long users;

	/**
	 * Active & billable user
	 */
	@Column(name = "billable_users")
	private long billableUsers;

	
	@Deprecated
	@Column(name = "billable_sites")
	private long billableSites;

	@Deprecated
	@Column(name = "units")
	private long units;

	@Column(name = "ATTRIBUTES")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.json.JsonType")
	private Json attributes;


	
	public KbeeUsageStat() {
		this.timestamp= OffsetDateTime.now();
	}
	
	@Override
	public Serializable getDomainId() {
		return domain_id;
	}
	
	@Override
	public long getResources() {
		return resources;
	}
	
	@Override
	public long getHardDisk() {
		return hard_disk_usage;
	}
	
	
	@Override
	public void setGatewayHardDisk(long s) {
		hard_disk_usage_gateway=s;
	}
	
	@Override
	public long getGatewayHardDisk() {
		return hard_disk_usage_gateway;
	}
	
	
	
	//@Override
	//public long getKBF S1HardDisk() {
	//	return kbfs1_hard_disk_usage;
	//}
	
	//@Override
	//public void setKBFS1HardDisk(long hd) {
	//	kbfs1_hard_disk_usage=hd;
	//}
	
	@Override
	public void setKBFS2HardDisk(long d) {
		kbfs2_hard_disk_usage=d;
	}
	
	@Override
	public void setKBFS2ArchiveHardDisk(long d) {
		this.kbfs2archive_hard_disk_usage=d;
	}

	@Override
	public long getKBFS2ArchiveHardDisk() {
		return kbfs2archive_hard_disk_usage;
	}

	@Override
	public void setOdilonHardDisk(long d) {
		odilon_hard_disk_usage=d;
	}

	
	
	@Override
	public void setGatewayResources(long s) {
		resources_external=s;
	}
	
	@Override
	public long getGatewayResources() {
		return resources_external;
	}
	

	@Override
	public long getExternalContents() {
		return contents_external;
	}

	@Override
	public long getExternalLibraryContents() {
		return contents_external_library;
	}
	

	@Override
	public long getContents() {
		return contents;
	}
	
	@Override
	public long getUsers() {
		return users;
	}
	
	@Override
	public OffsetDateTime getTimeStamp() {
		return timestamp;
	}
	
	@Override
	public void setDomainId(Serializable id) {
 		domain_id=Long.valueOf(id.toString());
	}
	
	@Override
	public void setResources(long res) {
		resources=res;
	}

	@Override
	public void setHardDisk(long hd) {
		hard_disk_usage=hd;
	}
	
	@Override
	public void setContents(long cont) {
		contents=cont;
	}
	
	@Override
	public void setExternalContents(long cont) {
		this.contents_external=cont;
	}
	
	@Override
	public void setExternalLibraryContents(long cont) {
		this.contents_external_library=cont;
	}

	/** External Total. External archive  */	
	public void setExternalArchiveContents(long cont) {
		
	}
	public long getExternalArchiveContents() {
		return this.contents_external_archive;
	}

	/** External Total. External recycle */	
	public void setExternalRecycleContents(long cont) {
		this.contents_external_recycle = cont;
	}
	
	@Override
	public long getExternalRecycleContents() {
		return this.contents_external_recycle;
	}
	

	@Override
	public void setDBUsage(long cont) {
		this.database_usage=cont;
	}
	
	
	@Override
	public long getDBUsage() {
		return this.database_usage;
	}

	
	
	@Override
	public void setUsers(long users) {
		this.users=users;
	}
	
	@Override
	public void setTimeStamp(OffsetDateTime ts) {
		timestamp=ts;
	}
	
	@Override
	public long getKBFS2HardDisk() {
		return this.kbfs2_hard_disk_usage;
	}

	@Override
	public long getOdilonHardDisk() {
		return this.odilon_hard_disk_usage;
	}
	
	@Override
	public long getBillableUsers() {
		return billableUsers;
	}
	@Override
	public void setBillableUsers(long billableUsers) {
		this.billableUsers = billableUsers;
	}
	
	//@Override
	//public long getBillableSites() {
	//	return billableSites;
	//}
	//@Override
	//public void setBillableSites(long billableSites) {
//		this.billableSites = billableSites;
	//}
	//@Override
	//public long getUnits() {
		//return units;
	//}
	//@Override
	//public void setUnits(long units) {
//		this.units = units;
//	}

	@Override
	public int hashCode() {
		
    	int hash = 1;
    	
    	long mili;
    	if (timestamp!=null)
    		mili = timestamp.toInstant().getEpochSecond() * 1000 +  timestamp.toInstant().getNano();
    	else
    		mili = 0;
    		
        hash = hash * 17 + (domain_id !=null ? domain_id.hashCode() : 0);
        hash = hash * 31 + Long.valueOf(mili).hashCode();
        hash = hash * 13 + Long.valueOf(hard_disk_usage).hashCode();
        return hash;
	}
	
	
	@Override
	public boolean equals(Object s) {
		
		if (!(s instanceof KbeeUsageStat))
			return false;
		
		KbeeUsageStat stat = (KbeeUsageStat) s;
			
		if (stat.getDomainId()==null || stat.getTimeStamp()==null)
			return false;
		
		if (getDomainId()==null || getTimeStamp()==null)
			return false;
		
		return  domain_id.equals( (Long) stat.getDomainId()) &&
				timestamp.toInstant().toString().equals(stat.getTimeStamp().toInstant().toString());
	}
	@Override
	public long getS3HardDisk() {
		return s3_hard_disk_usage;
	}
	@Override
	public long getGlacierHardDisk() {
		return glacier_hard_disk_usage;

	}
	@Override
	public void setS3HardDisk(long s3) {
		s3_hard_disk_usage = s3;
		
	}
	@Override
	public void setGlacierHardDisk(long glacier) {
		glacier_hard_disk_usage = glacier;
	}

	
}
