package kbee.web.report;

import java.io.Serializable;

import java.time.OffsetDateTime;


public class RangeItem implements Serializable {

	private static final long serialVersionUID = 1L;
	public String id;
	public String label;
	public OffsetDateTime from;
	public OffsetDateTime to;
	public RangeItem(String id, String label, OffsetDateTime from, OffsetDateTime to) {
		this.id=id;
		this.label=label;
			this.from=from;
			this.to=to;
	}
}
