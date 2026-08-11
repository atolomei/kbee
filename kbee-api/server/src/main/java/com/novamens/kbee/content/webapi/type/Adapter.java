package com.novamens.kbee.content.webapi.type;

public interface Adapter<T1, T2> {
	public T2 adapt(T1 t1);
}
