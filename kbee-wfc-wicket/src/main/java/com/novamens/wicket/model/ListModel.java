package com.novamens.wicket.model;


import java.util.Collections;
import java.util.Comparator;

import java.util.List;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;



public class ListModel<T> implements IModel<List<T>> {
	
	private static final long serialVersionUID = 1L;
	private String expression;
	private IModel<?> model;
	private boolean sort = false;
	private List<T> list =null;

	public ListModel(IModel<?> model, String expression, boolean sort) {
		this.expression = expression;
		this.model = model;
		this.sort = sort;
	}
	
	public ListModel(IModel<?> model, String expression) {
		this.expression = expression;
		this.model = model;
	}
	
	
	

	@SuppressWarnings({ "unchecked", "rawtypes"})
	public List<T> getObject() {
		if (sort) {
			if (list==null) {
				list = (new PropertyModel<List<T>>(model.getObject(), expression)).getObject();
				List<? extends Comparable> lc = (List<? extends Comparable>) list;
				Collections.sort(lc, new Comparator() {
					public int compare(Object o1, Object o2) { 
						try {
							return ((Comparable<Comparable>) o1).compareTo((Comparable) o2);
						} catch (Exception e) {
							return 0;
						}
					}
				});
			}
			return list;
		}
		else {
			return (new PropertyModel<List<T>>(model.getObject(), expression)).getObject();
		}
	}
	
	public void setObject(List<T> list) {
	}
	
	public void detach() {
		model.detach();
		list=null;
	}
	
	
}