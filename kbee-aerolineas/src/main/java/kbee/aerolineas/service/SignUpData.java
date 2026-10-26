package kbee.aerolineas.service;

import com.novamens.content.model.EntityMember;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpData {
	private String name;
	private String lastName;
	private	String email;
	private	String employeeId;
	private	EntityMember area; 
	private	String commnent;
}
