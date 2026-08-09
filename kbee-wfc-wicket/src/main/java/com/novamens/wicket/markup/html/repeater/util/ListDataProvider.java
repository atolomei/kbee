package com.novamens.wicket.markup.html.repeater.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class ListDataProvider<T extends Serializable> implements ISortableDataProvider<T, String> {
	private static final long serialVersionUID = 1L;
	private List<T> list = null;
	long size = -1;
	private SingleSortState<String> sortState = new SingleSortState<String>();
	
	public ListDataProvider(List<T> list) {
		this.list = list;
	}
	
	public ListDataProvider() {
	}
	
	public Iterator<? extends T> iterator(final long first, final long count) {
		List<T> list = this.getInternalList();
		long toIndex = first + count;
		if (toIndex > list.size()) {
			toIndex = list.size();
		}
		return list.subList((int)first, (int)toIndex).listIterator();
	}

	public long size() {
		if (size<0) {
			size = this.getInternalList().size();
		}
		return size;
	}

	public IModel<T> model(T object) {
		return new Model<T>(object);
	}
	
	public List<T> getList() {
		return this.list;
	}
	
	public List<T> getData() {
		return this.getInternalList();
	}
	
	public void detach() {
		this.list = null;
		this.size = -1;
	}
	
	public ISortState<String> getSortState() {
		return sortState;
	}

	public void setSortState(ISortState<String> state) {
		this.sortState = (SingleSortState<String>)state;
	}
	
	protected List<T> getInternalList() {
		if (this.list==null) this.list = this.getList();
		if (sortState.getSort()!=null) {
			List<T> orderedlist = new ArrayList<T>();
			for (T object1 : this.list) {
				Object value1 = PropertyResolver.getValue(sortState.getSort().getProperty(), object1);
				int i = 0;
				for (T object2 : orderedlist) {
					Object value2 = PropertyResolver.getValue(sortState.getSort().getProperty(), object2);
					if (sortState.getSort().isAscending()) {
						if (value1.toString().compareTo(value2.toString())<0)
							break;
						else
							i++;
					}
					else {
						if (value1.toString().compareTo(value2.toString())>0)
							break;
						else
							i++;
					}
				}
				orderedlist.add(i, object1);
			}
			return orderedlist;
		}
		return this.list;
	}
}
