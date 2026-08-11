package com.novamens.kbee.portal.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.novamens.portal6.model.Block;

public class KbeeBlockDummy extends KbeeBlock implements Block {
	
	public  KbeeBlockDummy() {
	}
	
	public  KbeeBlockDummy(String name) {
		super(name);
	}

}
