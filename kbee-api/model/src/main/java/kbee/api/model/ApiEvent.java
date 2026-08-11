package kbee.api.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private UUID eventId;
	private ApiObject object;
	private ApiEventType type;
	private OffsetDateTime time;
}