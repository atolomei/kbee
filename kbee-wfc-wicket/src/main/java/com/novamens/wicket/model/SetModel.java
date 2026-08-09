package com.novamens.wicket.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

public class SetModel<T> implements IModel<List<T>> {
	private static final long serialVersionUID = 1L;
	private String expression;
	private IModel<?> model;
	private boolean sort = false;
	private List<T> list =null;

	public SetModel(IModel<?> model, String expression, boolean sort) {
		this.expression = expression;
		this.model = model;
		this.sort = sort;
	}
		
	public SetModel(IModel<?> model, String expression) {
		this.expression = expression;
		this.model = model;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes"})
	public List<T> getObject() {
		if (list!=null)
			return list;
		list = new ArrayList<T>();
		Set<T> set = (new PropertyModel<Set<T>>(model.getObject(), expression)).getObject();
		list.addAll(set);
		if (sort) {
			List<? extends Comparable> lc = (List<? extends Comparable>) list;
			Collections.sort(lc, new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1==null)
						return (o2==null?0:1);
					if (o2==null)
						return -1;
					return ((Comparable<Comparable>) o1).compareTo((Comparable) o2);
				}
			});
		}
		return list;
	}
	
	public void setObject(List<T> set) {
	}
	
	public void detach(){
		model.detach();
		list=null;
	}
}