package kbee.web.scheduler;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


import com.novamens.content.command.Command;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.command.CommandService;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.service.ServiceLocator;

public class SchedulerCronJobListSearchResult implements SearchResult {
	
	private static final long serialVersionUID = 1L;

	private AbstractCronJobRequest object;	
	
	
	public SchedulerCronJobListSearchResult(AbstractCronJobRequest request) {
		this.object=request;
		//this.command_id=command.getId();
	}
	
	

	@Override
	public Object getObject() {
		//if (detached) {
		//	object= getCommandService().getCommand((Long) this.command_id);
		//	detached=false;
		//}
		return this.object;
	}



	@Override
	public void detach() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Map<String, Object> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public float getScore() {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public List<String> getSnippets() {
		// TODO Auto-generated method stub
		return null;
	}

		

	
}
