package kbee.api.model;

import java.time.OffsetDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApiActivity extends ApiWorkItem {
	private static final long serialVersionUID = 1L;
	
	private String status;
	private INote note;
	private ApiProxy user;
	private OffsetDateTime startTime;
	private ApiProcess process;
}
