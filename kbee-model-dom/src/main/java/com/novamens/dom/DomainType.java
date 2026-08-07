package com.novamens.dom;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum DomainType implements PersistentEnum, Serializable {
	
	EXPRESS 				(3,  "express",            "express"),  					// free version
	PREMIUM					(1,  "standard",           "enterprise"), 					// standard
	SYSTEM 					(4,  "system",             "system"), 						// kbee 
	
	FILE_SYSTEM_READER 		(2,  "file_system_reader", "file_system_reader"),   		// mapping of a fsrepository. readonly
	TEMPLATE				(6,  "template",           "template"), 					// Windsor and other Compliance
	COMPLIANCE				(5,  "compliance",         "compliance"),
	OBJECT_STORAGE 			(7,  "object-storage",            "object-storage");  					// free version; 					// Windsor and other Compliance
															
	private String label;
	private int id;
	private String css;
	
	private DomainType(int code, String label, String css) {this.label = label;this.id = code; this.css=css;}
	
	public String toString()	{return ("id: " + getId() + ". label: "+ getLabel()) + ". css: "+getCss();} 
	public String getLabel() 	{return getLabel(Locale.getDefault());}
	public String getCss() 		{return css;}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(DomainType.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() 			{return id;}
	
	
	public String getAlias() {
		return this.label;
	}
	
	public String getDisplayName() {
		return getLabel();
	}

	public static DomainType fromId(int id) {
		for (DomainType e : values()) {
			if (e.getId() == id) return e;
		}
		return null;
	}
	
}


/**

s


select U.username, UP.lastlogindate  from  userprofile UP,  profile P , users U  where  P.id=UP.id and UP.user_id=U.id  
and UP.lastlogindate is not null 
and not (username like 'root@%' or
               username like 'atolomei@%' or
               username like 'gsapiurka@%' or
               username like 'onesitedm@%' or
               username like 'smarkham@%' or
               username like 'sso@%' 
)
and P.domain_id in (select id from domain where type=3)
order by UP.lastlogindate


---

select U.lastname, U.firstname, U.username, C "#Sign In" from (
select count(*) C, event_user EU from LogEvent where 
event_time > (now()  - interval '1 year')
and
event_user in (
select U.id  from  userprofile UP,  profile P , users U  where  P.id=UP.id and UP.user_id=U.id  
and UP.lastlogindate is not null 
and not (	   username like 'root@%'      or
               username like 'atolomei@%'  or
               username like 'gsapiurka@%' or
               username like 'onesitedm@%' or
               username like 'smarkham@%'  or
               username like 'sso@%' 
)
and P.domain_id in (select id from domain where type=3)
)
and event_type = 'LoginEvent' group by event_user) TMP,
users U where U.id=EU order by U.lastname, U.firstname

---






*/