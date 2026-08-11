package kbee.api.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiWorkItem extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private ITask task;
	private String time;
	private ApiWorkflowContext context;
	private List<IFormData> forms; 
} 