package com.novamens.content.service.workflow;

import com.novamens.security.User;

public class UserWorkLoadData {

	public User user;
	
	public int total;
	
	public int today_due_date;	
	public int past_due_date;
	public int due_plus_one;
	public int due_plus_two;
	public int due_plus_three;
	public int due_plus_four;
	public int due_plus_five;
	public int due_plus_six;
	public int due_plus_n;
	public int due_none;
	
	
	// -3(-), -2, -1, 0, 1, 2, 3(+)
	public Integer  effective[] = new Integer[7];

	public long timestamp;
	
}

