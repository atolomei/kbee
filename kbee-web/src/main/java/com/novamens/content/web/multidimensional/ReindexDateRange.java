package com.novamens.content.web.multidimensional;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.command.Command;
import com.novamens.kbee.command.CommandService;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.service.ServiceLocator;

@Deprecated
public class ReindexDateRange extends AbstractCronJobRequest {
	private static final long serialVersionUID = 8493968328948344749L;
	
	static private Logger logger = LogManager.getLogger(ReindexDateRange.class.getName());
						
	private String facetname;
	
	private Serializable command_id;
	double progress=0;
	
	public ReindexDateRange() {
		setName("Reindex Date Range");
	}
	
	@Override
	public void stop() {
		logger.debug("Stopping Request ");
		if (this.command_id!=null) {
			Command com = ServiceLocator.getService(CommandService.class).getCommand(Long.valueOf(this.command_id.toString()));;
			 if (com!=null) { 
				 com.stop();
				 ServiceLocator.getService(CommandService.class).executed(com);
			 }
		 }
	}
	
	@Override
	public void execute() {
		logger.debug("Executing " + this.toString());
		ReindexDateRangeCommand com = new ReindexDateRangeCommand(getFacetName());
		this.command_id = com.getId();
		ServiceLocator.getService(CommandService.class).register(com);
		com.execute();
		ServiceLocator.getService(CommandService.class).remove(Long.valueOf(com.getId().toString()));;
		logger.info("done.");
		
	}
	
	public double getProgress() {
		return 0;
	}
	
	public void setFacetName(String facet) {
		this.facetname = facet;
	}
	
	public String getFacetName() {
		return this.facetname; 
	}
	
	public void setCronExpression(String expression) {
		super.setCronExpression(new CronExpressionJ8(expression));
	}
	
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append(" | Facet: " + getFacetName());
		return str.toString();	
	}
}


