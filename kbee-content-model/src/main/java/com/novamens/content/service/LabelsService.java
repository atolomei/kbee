package com.novamens.content.service;

import java.util.List;

import com.novamens.content.user.UserLabel;
import com.novamens.service.ObjectService;

/** --------------------------------------------------------------------------

 *   This is a ObjectService of Content.
 *   Labels Service applies UserLabels to Content object.
 *   
 *  Labels are a special case of DataSetMembers
 *  Labels are created with th Domain by the {@link DomainLifeCyleService} 
 *
 */
public interface LabelsService extends ObjectService {
	
	public void setLabel(String label);
	public void setLabel(UserLabel label);

	public void removeLabel(String label);
	public void removeLabel(UserLabel label);
	public void removeAll();
	
	public boolean labeled(String label);
	public boolean labeled(UserLabel label);
	
	public List<String> getLabels();
	public List<UserLabel> getUserLabels();
	
	public void setLabelForAssign();
	public void removeUserLabelById(String id);
	
}
