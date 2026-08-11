package com.novamens.kbee.content.webapi.type;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;

import kbee.api.model.ApiActivity;
import kbee.api.model.ApiViewMode;

public class ApiContentActivityAdapter implements Adapter<Content, ApiActivity> {
	
	public ApiContentActivityAdapter() {
	}
	
	public ApiActivity adapt(Content content) {
		KbeeWorkflowActivity activity = (KbeeWorkflowActivity)content.getService(WorkflowService.class).getActivity();
		
		if (activity==null) 
			return null;
		
		ApiActivity iactivity = (new ApiActivityAdapter(ApiViewMode.Grid))
			.adapt(activity);
		
		return iactivity;
	}
	
}