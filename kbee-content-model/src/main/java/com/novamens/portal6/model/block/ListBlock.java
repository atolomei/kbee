package com.novamens.portal6.model.block;

import java.util.List;

import com.novamens.portal6.model.Block;


public interface ListBlock<T> extends Block {
	
	public List<T> getItems();

}
