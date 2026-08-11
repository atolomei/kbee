package com.novamens.kbee.content.command.mt;

public interface Dispatcher<T extends Runnable> {
	public void dispatch(T runnable);
}
