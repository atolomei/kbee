package kbee.content.support;

public class SupportTicketsDailySubmitterCommand extends SupportTicketsSubmitterCommand {

	
	public SupportTicketsDailySubmitterCommand() {
		super("daily");
		super.setUpperThreshold(6);
	}
}
