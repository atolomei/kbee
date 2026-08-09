package com.novamens.kbee.wicket.services;

import java.io.Serializable;

import org.apache.wicket.markup.html.panel.Panel;

public interface PanelFactory extends Serializable {
	public Panel create(String id);
}