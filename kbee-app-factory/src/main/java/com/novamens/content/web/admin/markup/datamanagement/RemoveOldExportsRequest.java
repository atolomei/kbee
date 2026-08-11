package com.novamens.content.web.admin.markup.datamanagement;


import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronExpressionJ8;


public class RemoveOldExportsRequest extends AbstractCronJobRequest {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RemoveOldExportsRequest.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	public RemoveOldExportsRequest() {
		super();
	}
	
	
	/**
	 * Recorre la lista de directorio y borra todos los que tienen mas de 10 dias.
	 */
	@Override
	public void execute() {
	
			try {

				RemoveOldExportsCommand command = new RemoveOldExportsCommand();
				command.execute();
						
			} catch (Exception e) {
				logger.error(e);
			}
	}
	
	public void setCronExpression(String expression) {
		super.setCronExpression(new CronExpressionJ8(expression));
	}

}
