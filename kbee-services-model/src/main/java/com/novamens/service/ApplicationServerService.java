package com.novamens.service;

import java.util.List;


import kbee.util.Tuple;

public interface ApplicationServerService extends SystemService {

	
	public boolean isOdilonEnabled();
	public boolean isLocalFSEnabled();
	public boolean isMinioEnabled();
	
	
	// public String getWorkDir();
	public String getWorkDirAbsolutePath();
	
	public String getHomeDir();
	
	public String getDriveDir();
	//public String getKBFS1 Dir();
	public String getDataExportDir();
	public void checkDirs();
	public String getImagesDir();
	
	public String getEmailTemplatesDir();
	
	public String getLoginImagesDir();
	
	public String getHomeDirAbsolutePath();
	
	public String getWicketConfigurationType();
	public void  setWicketConfigurationType(String str);
	
	// public String encrypt(String str);
	// public String decrypt(String str);
	
	
	public String getApplicationServerId();
	public String getServerHost();
	
	
	// public boolean isEforms();
	public String getAvatarImagesDir();
	public String getInlineHelpDir();
	
	/* System info */
	public List<Tuple> serversInfo();
	public List<Tuple> schedulerInfo();
	public List<Tuple> infrastructureInfo();
	public List<Tuple> pingInfo();
	
	public   String getJettyPort();
	public String getFormTemplatesDir();	
	
	 
	
		
	
}
