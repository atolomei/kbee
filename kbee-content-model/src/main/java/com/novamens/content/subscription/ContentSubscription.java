package com.novamens.content.subscription;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;

public interface ContentSubscription {
	public Content getContent();
	public Person getPerson();
}
