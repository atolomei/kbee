package com.novamens.logging.usage;

import java.io.Serializable;
import java.time.OffsetDateTime;

public interface UsageStat {

	public Serializable getDomainId();
	public void setDomainId		(Serializable id);

	/**Stored:  , KBFS2, KBFS2Archive */
	public long getHardDisk();
	public void setHardDisk		(long hd);
	
	/**  External files */	
	long getGatewayHardDisk();
	void setGatewayHardDisk(long s);

	
	/**Stored:  KBFS2, Odilon, KBFS2Archive */
	public void  setResources	(long res);
	public long getResources();
	
	/**  External resources */
	long getGatewayResources();
	void setGatewayResources(long s);
	

	/** Total. all versions */
	public void setContents		(long cont);
	public long getContents();
	
	/** Total. all state */
	public long getUsers();
	public void setUsers		(long users);
	
	public OffsetDateTime getTimeStamp();
	public void setTimeStamp	(OffsetDateTime ts);
	
	
	//long getKB FS1HardDisk();
	
	long getKBFS2HardDisk();
	long getKBFS2ArchiveHardDisk();
	
	
	// void setKB FS1HardDisk(long hd);
	void setKBFS2HardDisk(long d);
	void setKBFS2ArchiveHardDisk(long d);


	long getOdilonHardDisk();
	void setOdilonHardDisk(long d);
	
	long getBillableUsers();

	void setBillableUsers(long billableUsers);

 	// Amazon
	long getS3HardDisk();
	long getGlacierHardDisk();
	void setS3HardDisk(long s3);
	void setGlacierHardDisk( long glacier);

	
	/** External Total. all versions */
	public void setExternalContents		(long cont);
	public long getExternalContents();

												
	/** External Total. External library */	
	public void setExternalLibraryContents(long cont);
	public long getExternalLibraryContents();

	/** External Total. External archive  */	
	public void setExternalArchiveContents(long cont);
	public long getExternalArchiveContents();

	/** External Total. External recycle */	
	public void setExternalRecycleContents(long cont);
	public long getExternalRecycleContents();
	
	public long getSolRTotalContent();
	public long getSolRTotalAudit();
	public long getSolRTotalFile();
	
	public void setSolRTotalContent(long l);
	public void setSolRTotalAudit(long l);
	public void setSolRTotalFile(long l);
	
	public long getDBUsage();
	public void setDBUsage(long cont);



}


