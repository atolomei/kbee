package com.novamens.kbee.content.questionanswer;

import com.novamens.security.PersistentEnum;

public enum QuestionAnswerPermission implements PersistentEnum {
			
	CREATE_QUESTION 	(1,  "Create Question"), 
	CREATE_ANSWER 		(2,  "Create Answer"),
	CREATE_COMMENT 		(3,  "Create Comment"),
	VOTE 				(4,  "Vote"),
	
	REPORT 				(5,  "Report Content"),
	
	EDIT_QUESTION 		(6,  "Edit Question"),
	EDIT_ANSWER 		(7,  "Edit Answer"),
	EDIT_COMMENT 		(8,  "Edit Comment"),
	
	DELETE_QUESTION 	(9,  "Delete Question"),
	DELETE_ANSWER 		(10, "Delete Answer"),
	DELETE_COMMENT 		(11, "Delete Comment"),
	
	
	LOCK_QUESTION 		(12,  "Lock Question"),
	UNLOCK_QUESTION 	(13,  "Unlock Question"),
	
	CREATE_USER 		(14, "Create User"),
	DELETE_USER 		(15, "Delete User"),
	EDIT_USER 			(16, "Edit User");
	
	private String label;
	private int id;

	private  QuestionAnswerPermission(int code, String label) {this.label = label;this.id = code;}
	public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
	public String getLabel() {return label;}

	public int getId() {return id;}

	public boolean equals(QuestionAnswerPermission o) {return id==o.id;}
	
}

