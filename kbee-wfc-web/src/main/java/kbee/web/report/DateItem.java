package kbee.web.report;

import java.io.Serializable;
import java.time.OffsetDateTime;


public class DateItem implements Serializable {

	private static final long serialVersionUID = 1L;
	public String id;
	public String label;
	public OffsetDateTime date;
	public DateItem(String id, String label, OffsetDateTime date) {
		this.id=id;
		this.label=label;
		this.date = date;
	}
}
