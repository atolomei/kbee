package kbee.api.service;

public enum ApiError {
	
	API_NOT_ENABLED (7000, "Api not enabled"), 
	
	NOT_MODIFIED (1000, "Not modified"), 
	ACCESS_DENIED (1010, "Access denied"), 
	TOO_MANY_REQUESTS(1020, "TOO_MANY_REQUESTS"),
	
	INVALID_VERSION (1030, "Version in Request is older than the current Version"),  
	LOCKED(1040, "Locked"), 
	NO_DATA(1050, "No data"), 
	INVALID_APPLICATION (1060, "Invalid application"),
	
	DOMAIN_NOT_FOUND (1070, "Domain not found"), 
	COMMAND_NOT_FOUND(1080, "Command not found"),
		
	DATASET_NOT_FOUND(1200, "Dataset not found"), 
	CLASSIFIER_NOT_FOUND(1210, "Classifier not found"),
	
	CLASS_NOT_FOUND(1220, "Class not found %1"), 
	
	INVALID_ATTRIBUTE(1230, "Attribute %1 is invalid"), 
	ATTRIBUTE_IS_REQUIRED(1240, " Attribute %1 is required"),
	ATTRIBUTE_NOT_FOUND(1250, "Attribute not found"),
	
	INVALID_MULTIPLICITY(1260, "Invalid Multiplicity for %1"),
	
	VALUE_ALREADY_EXIST(1270, "Value already exist"),
	VALUE_NOT_FOUND(1280, "Value not found"), 
	ENTITY_NOT_FOUND(1290, "Entity not found or value is not a entity"),
	
	AGGREGATOR_NOT_FOUND(1300, "%1 aggregator not found"),
	
	INVALID_RELATION(1400, "Relationship %1 is invalid"), 
	INVALID_DOMAIN(1410, "Invalid domain. %1 was expected but %2 was received"), 
	INVALID_CLASS(1445, "ExternalId references a content of a classname that is not %1"), 
	INVALID_DATE(1460, "Invalid Date"), 
	INVALID_SOURCE(1470, "Invalid Source"),
	
	FORM_NOT_FOUND(1245, "Form not found"), 
	INVALID_FORM(1246, "Form not found"), 

	PRINCIPAL_NOT_FOUND(1270, "User not found"),
	GROUP_NOT_FOUND(1280, "Group not found"), 

	USER_NOT_FOUND(1275, "User not found"),
	USER_ALREADY_EXIST(1480, "User name already exist"), 
	USER_CONSTRAINT(1485, "The user is author of documents"), 
	USER_INVALID_NAME(1490, "User name invalid"), 
	USER_INVALID_TIMEZONE(1495, "Invalid Timezone for User"),
	
	ROLE_NOT_FOUND(1300, "Role not found"),
	RULE_NOT_FOUND(1290, "Rule not found"),
	
	INVALID_SIGNATURE(1475, "Invalid Signature"), 
	SIGNATURE_NOT_FOUND(1350, "Signature not found or not enabled"),
	
	RESOURCE_NOT_FOUND(1260, "Resource not found"), 
	RESOURCES_ERROR(1500, "Resources Error"),
	FILE_NOT_FOUND (1210, "File not found"), 
	
	REFERENCED_DATA(2000, "REFERENCED_DATA"),
	
	PROCEDURE_NOT_FOUND (1210, "Procedure not found"), 
	ACTIVITY_NOT_FOUND(2500, "Activity not found"),
	ACTIVITY_ILLEGAL_STATE(2520, "illegal state"),
	
	DEVICE_NOT_REGISTERED(2600, "device not registered"),
	
	REPLICA_NOT_FOUND(5000, "Replica not found"), 
	
	IQL_SYNTAX_ERROR (8200, "Syntax Error"), 
	IQL_PREDICATE_ERROR (8500, "Predicate not found"),
	
	MALFORMED_URL (8700, "Malformed URL"),
	
	INTERNAL_ERROR (9000, "%1"),
	CLIENT_ERROR (9001, "%1");
	
	private int code;
	private String message;
	
	private ApiError(int code, String message) {
		this.message = message;
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
}