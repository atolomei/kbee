package com.novamens.portal.reports;


import java.util.Calendar;
import java.util.Date;


public class DVisit {
	
	public DVisit(int day, int month, int year, long visits) {
		this.day=day;
		this.month=month;
		this.year=year;
		this.visits=visits;
	}
	
	private int day;
	private int month;
	private int year;
	
	private long visits;
	private long unique_visitors;
	
	public int getDay() 	{return day;}
	public int getMonth() 	{return month;}
	public int getYear() 	{return year;}
	
	public Date getDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		return cal.getTime();
	}
	
	public long getVisits() {return visits;}
	public long getUniqueVisitors() {return unique_visitors;}
	
}
