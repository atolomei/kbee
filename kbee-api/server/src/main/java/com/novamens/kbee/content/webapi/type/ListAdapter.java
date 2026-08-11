package com.novamens.kbee.content.webapi.type;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter<T1, T2> implements Adapter<List<T1>, List<T2>>{
	
	private Adapter<T1, T2> adapter;
	
	public ListAdapter(Adapter<T1, T2> adapter) {
		this.adapter = adapter;
	}
	
	public Adapter<T1, T2> getAdapter() {
		return adapter;
	}
	
	public List<T2> adapt(List<T1> list) {
		
		List<T2> adapted = new ArrayList<T2>();
		for (T1 t1 : list) {
			T2 object = getAdapter().adapt(t1);
			adapted.add(object);
		}
		return adapted;
	}
}
