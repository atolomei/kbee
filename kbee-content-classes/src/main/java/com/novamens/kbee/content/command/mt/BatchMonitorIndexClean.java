package com.novamens.kbee.content.command.mt;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import com.novamens.content.command.CommandState;

public class BatchMonitorIndexClean extends BatchCleanCommand {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BatchMonitorIndexClean.class.getName());
	
	public BatchMonitorIndexClean() {
		setName("Batch Monitor Clean");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("statement", "inworkspace:true");
		setParameters(map);
	}
	
	@Override
	protected void executeAsync() {
		
		try {
			Map<String, Object> map = getParameters();
			
			if (map==null)
				map = new HashMap<String, Object>();
			
			map.put("statement", "inworkspace:true");
			setParameters(map);
			
			super.executeAsync();

			setState(CommandState.COMPLETED);
		} catch (Exception e) {
			logger.error(e);
			setState(CommandState.ERROR);
		} finally  {
			
			
			super.setDateTerminated(OffsetDateTime.now());
			
		}
		
	}
	
}
