package com.novamens.kbee.sysmessage;

import java.io.Serializable;
import java.util.List;

import com.novamens.service.SystemService;


/**
 *  SysMessages are System messages 
 *   
 *
 */
public interface SysMessageService extends SystemService {
	
	public void register(SysMessage message);
	
	/**
	 * Returns the next MessageEntry for the user
	 * @param userid
	 * @return
	 */
	public SysMessageEntry getSysMessage(Serializable userid);
	public void done(SysMessageEntry entry, Serializable userid);

	/**
	 * get a particular MessageEntry
	 * userid is required for performance
	 * 
	 * @param userid
	 * @return
	 */
	public SysMessageEntry getSysMessageEntry(String id, Serializable uid);

	public List<SysMessageEntry> getSysMessageEntries();

	/**
	 * Remove de global
	 */
	public void remove(Serializable entryid);
	
	/**
	 * Remove de un usuario particular
	 */
	public void remove(Serializable userid, String entryid);

	public void removeAll();

}

