package com.novamens.metrics.domain;

import java.io.Serializable;

/**
 * 
 *
 */
public class DomainData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// current values
	//
	public long users     = 0;
	public long resources = 0;
	public long harddisk_total  = 0;
	
	public long harddisk_kbfs1  		= 0;
	public long harddisk_kbfs2  		= 0;
	public long harddisk_kbfs2archive  	= 0;
	public long harddisk_kbfs2external  = 0;
	
	public long harddisk_odilon         = 0;
	public long harddisk_gateway  		= 0;
	
	public long harddisk_s3 			= 0;
	public long harddisk_glacier		= 0;
	
	public long contents  				= 0;
	public long contents_external		= 0;
								
	public long contents_external_library		= 0;
	public long contents_external_archive		= 0;
	public long contents_external_recycle		= 0;
	
	public long time_measured; 	 				// when current values were measured
	
	// statistics (estimation)
	//
	public long time_estimated_hd_grow = 0; 	// when growth estimate was calculated
	public long limit_bytes = 0;  				// threshold to compare to, to determine if the system is going to hit 

	public double avg_daily_increase = 0.0;     // estimation based on last 100 measures (mean)  
	public double avg_month_increase = 0.0;     // avg_daily_increase * 30 
	public double avg_month_increase_ma = 0.0;  // estimation using moving average   
	
	public long time_reach_limit_milisecs;  	// estimate when it will pass the threshold limit_bytes
	public boolean limit_no_reachable = true;   // true if it will never reach limit_bytes
	public long billableUsers = 0;
	public long billableSites = 0;
	public long units = 0;


	public DomainData() {
	}
	
	
	public DomainData(DomainData d) {
	
		this.users=d.users;
		this.resources=d.resources;
		this.harddisk_total=d.harddisk_total;

		this.harddisk_s3=d.harddisk_s3;
		this.harddisk_kbfs1=d.harddisk_kbfs1;
		this.harddisk_kbfs2=d.harddisk_kbfs2;
		
		this.harddisk_odilon=d.harddisk_odilon;
		
		this.harddisk_kbfs2archive=d.harddisk_kbfs2archive;
		this.harddisk_gateway=d.harddisk_gateway;
		
		this.contents=d.contents;
		this.contents=d.contents_external;
		
		this.contents=d.contents_external_library;
		this.contents=d.contents_external_archive;
		this.contents=d.contents_external_recycle;
		
		this.time_measured=d.time_measured;
		
		this.time_estimated_hd_grow=d.time_estimated_hd_grow;
		
		this.avg_daily_increase=d.avg_daily_increase;  
		this.avg_month_increase=d.avg_month_increase;  
		
		this.limit_no_reachable = d.limit_no_reachable;
		
		this.time_reach_limit_milisecs =d.time_reach_limit_milisecs;
		this.limit_bytes = d.limit_bytes;
		this.billableSites = d.billableSites;
		this.billableUsers = d.billableUsers;
		this.units = d.units;
		
	}
	

	
}
