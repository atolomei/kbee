package kbee.web.command;

import com.novamens.kbee.content.command.ReindexCommand;
import com.novamens.scheduler.SchedulerService;

public class ReindexWorkspaceCommand extends ReindexCommand {
	static private final String QUERY = "from KbeeContent where workspace!=null";
	public ReindexWorkspaceCommand() {
		super(QUERY);
		setPriority(SchedulerService.HIGH_PRIORITY);
	}
}
