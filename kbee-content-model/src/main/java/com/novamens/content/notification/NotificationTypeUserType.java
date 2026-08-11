package com.novamens.content.notification;

import java.io.Serializable;


import com.novamens.dom.DomPersistentEnumUserType;

public class NotificationTypeUserType extends DomPersistentEnumUserType<NotificationType>  implements Serializable  {
																	
	private static final long serialVersionUID = 1L;

	@Override
	public Class<NotificationType> returnedClass() {
		return NotificationType.class;
	}
	
	
	

	
}
