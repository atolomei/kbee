package com.novamens.kbee.wicket.markup.html.console.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class KeyboardEvent extends AbstractWicketAjaxEvent {
	private Key key;
	
	public enum Key {
		CURSORDOWN(40),
		ENTER(65),
		CURSORUP(38);
		private int code;
		private Key(int code) {
			this.code = code;
		}
		public int code() {
			return code;
		}
		public boolean equals(Integer code) {
			return this.code==code;
		}
	}
	
	public KeyboardEvent(AjaxRequestTarget target, Key key) {
		super(target);
		this.key = key;
	}
	
	public Key getKey() {
		return this.key;
	}
}
