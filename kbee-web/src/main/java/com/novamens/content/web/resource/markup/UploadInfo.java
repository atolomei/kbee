package com.novamens.content.web.resource.markup;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Resource;
import com.novamens.content.resource.KBFile;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.NumberFormatter;

@Deprecated
public class UploadInfo extends WebMarkupContainer {
	private static final long serialVersionUID = 1L;
	
	private StringResourceModel model;

	public UploadInfo(Resource resource) {
		super("info");
		
		User user;
		String dateformatted;
		
		String wxh;
		
		if (resource instanceof KBFile) {
			
			user = ((KBFile) resource).getUploadUser();
			dateformatted = ((KBFile) resource).getUploadOffsetDateTimeColloquial();
			
			int w = ((KBFile) resource).getWidth(); 
			
			if (w>0) {
				int h = ((KBFile) resource).getHeight();
				wxh = " · " + String.valueOf(w)+" x "+String.valueOf(h) + " pixels";
			}
			else
				wxh = "";
		}
		else { 
		
			user = resource.getLastModifiedUser();
			dateformatted = resource .getLastModifiedOffsetDateTimeColloquial();
			
			wxh ="";
		}
		
		if (user==null)	
			user = getUser();
		
		model = new StringResourceModel("fileupload.uploadedby", this);
		model.setParameters(user.getFirstLastName(), dateformatted, NumberFormatter.formatFileSize(resource.getSize()), wxh);
		
	}
	
	@Override
	public String toString() {
		return model.getObject();
	}

	private User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
