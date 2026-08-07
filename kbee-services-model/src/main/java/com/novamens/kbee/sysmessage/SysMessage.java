package com.novamens.kbee.sysmessage;

import java.io.Serializable;

import java.util.List;
import java.util.Map;

import com.novamens.security.User;

public interface SysMessage extends Serializable, com.novamens.security.Identifiable {

	public String getTitle();
	public String getText();
	public String getLink();
	
	public void setTitle(String title);
	public void setText(String text);
	public void setLink(String link);
	
	public Map<String, String> getParameter();  // ej. id del TxModel, otros parametros del TxModel
	
	public boolean isGlobal();
	public void setIsGlobal(boolean is_global);
	
	public void setReceiverUserId(Serializable user_id);
	public Serializable getReceiverUserId();
	
	public long getDurationSecs();
	public void setDurationSecs(long secs);
	
	public boolean isLaunchNow();
	public  void setLaunchNow(boolean launch_now);
	
	public void setParameters(Map<String, String> param);
	
	public User getReceiverUser();
	public List<String> getDescriptionAsList();
	boolean getGlobal();
	boolean getLaunchNow();
	
	
}
