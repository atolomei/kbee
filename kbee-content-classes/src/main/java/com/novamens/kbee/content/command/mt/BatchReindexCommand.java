package com.novamens.kbee.content.command.mt;


import java.util.ArrayList;
import java.util.List;

import com.novamens.content.command.CommandParameter;
import com.novamens.content.command.CommandParameterType;
import com.novamens.dom.Indexable;
import com.novamens.dom.ObjectID; 
/**
 *  Parameters
 * 
	<ul>
	<li><b>statement</b> 
	Hibernate Query
	</li>
	
	<li><b>limit</b> 
	max amount of content to process 
	default: Long.MAX_VALUE
	</li>
	
	<li><b>max-threads</b>
	Default: 
	QueueProcessor.MAX_THREADS (number of available processors)
	</li>
	
	<li><b>batch-size</b> 
	Default: QueueProcessor.BATCH_SIZE  
	</li>
	
	<li><b>include-attachments</b> 
	Default: true 
	</li>
	</ul>
  
  @see BatchReindexExecutor
  @see QueuedBatchProcessor
  @see BatchReindexCommand
  @see QueueProcessorCommand
  
**/

public class BatchReindexCommand extends QueueProcessorCommand<ObjectID> {
			
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BatchReindexCommand.class.getName());
	
	
	public BatchReindexCommand() {
		setName("Batch Index");
		setDescription(
				"<ul>\r\n" + 
				"	<li><b>statement</b> \r\n" + 
				"	Hibernate Query\r\n" + 
				"	</li>\r\n" + 
				"	\r\n" + 
				"	<li><b>limit</b> \r\n" + 
				"	max amount of content to process \r\n" + 
				"	default: Long.MAX_VALUE\r\n" + 
				"	</li>\r\n" + 
				"	\r\n" + 
				"	<li><b>max-threads</b>\r\n" + 
				"	Default: \r\n" + 
				"	" + String.valueOf(QueueProcessorCommand.DEFAULT_MAX_THREADS) + "(number of available processors)\r\n" + 
				"	</li>\r\n" + 
				"	\r\n" + 
				"	<li><b>batch-size</b> \r\n" + 
				"	Default: QueueProcessor.BATCH_SIZE ("+ String.valueOf(QueueProcessorCommand.BATCH_SIZE)  + ") \r\n" + 
				"	</li>\r\n" + 
				"	\r\n" + 
				"	<li><b>include-attachments</b> \r\n" + 
				"	Default: true \r\n" + 
				"	</li>\r\n" + 
				"	</ul>"
				);
	}
	
	
	/**
	 * 
	 * Normally this class:
	 * {@link BatchReindexExecutor}
	 * 
	 */
	@Override
	public String getProcessorBean() {
		return "batch-indexer";
	}
	
	@Override
	public List<CommandParameter> getParametersDefinition() {
		List<CommandParameter> commandParameterList=new ArrayList<CommandParameter>();
		commandParameterList.add(new CommandParameter("statement", "statement", false, CommandParameterType.STRING));
		commandParameterList.add(new CommandParameter("limit", "limit", false, CommandParameterType.LONG));
		
		commandParameterList.add(new CommandParameter("max-threads", "max-threads", false, CommandParameterType.LONG));
		commandParameterList.add(new CommandParameter("batch-size", "batch-size", false, CommandParameterType.LONG));
		commandParameterList.add(new CommandParameter("include-attachments", "include-attachments", false, CommandParameterType.BOOLEAN));
		
		return commandParameterList;
	}
}