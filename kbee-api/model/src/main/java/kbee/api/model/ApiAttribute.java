package kbee.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiAttribute extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String type;
	private String alias;
	private String multiplicity;
	private String predicate;
	private String uniqueName;
	private boolean filterable;
	
}