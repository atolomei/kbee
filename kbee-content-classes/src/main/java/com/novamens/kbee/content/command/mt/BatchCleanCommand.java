package com.novamens.kbee.content.command.mt;


import com.novamens.dom.Indexable; 
/**
 *  Parameters
 * 
	<ul>
	<li><b>statement</b> 
	SolR Query
	
	Monitor -> inworkspace:true
	
	
	</li>
	
	<li><b>limit</b> 
	max amount of content to process 
	default: Long.MAX_VALUE
    </li>
	
	<li><b>max-threads</b>
	Default: 
	QueueProcessor.MAX_THREADS (3)
    </li>
	
	<li><b>batch-size</b> 
	Default: QueueProcessor.BATCH_SIZE  
    </li>
	</ul>
  
**/

public class BatchCleanCommand extends QueueProcessorCommand<Indexable> {
			
	public BatchCleanCommand() {
		setName("Batch Clean");
	}
	
	/**
	 * 
	 * Normally this class:
	 * 
	 * {@link BatchReindexExecutor}
	 * 
	 * 
	 * <bean id="batch-cleaner" class="com.novamens.kbee.content.command.mt.BatchCleanExecutor" scope="prototype"/>
	 * 
	 * 
	 * 
	 */
	@Override
	public String getProcessorBean() {
		return "batch-cleaner";
	}
}