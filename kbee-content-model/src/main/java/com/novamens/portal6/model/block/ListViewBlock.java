package com.novamens.portal6.model.block;

import java.util.List;

import com.novamens.portal6.model.ViewBK;


/**
 * v6
 */
public interface ListViewBlock extends ListBlock<ViewBK> {

	
	public void add(ViewBK view);
	public void remove( ViewBK v);
	public List<ViewBK> getItems();
	public void setViews(List<ViewBK> li);
	
}
