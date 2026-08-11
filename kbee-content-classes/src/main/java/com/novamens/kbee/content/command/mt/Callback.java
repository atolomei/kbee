package com.novamens.kbee.content.command.mt;

public interface Callback<T> {
	public void execute(T object) throws Exception;
}
