//Created on 10/08/2006
package com.novamens.configuration;


public interface Configuration<T> {

	ChangeChecker getChecker();

	void setObserver(ChangeObserver observer);
	
	T getObserverProxy();

	T getProxyChecker();

	long getDelay();

	Factory<T> getFactory();

	public interface Factory<T> {
		T getObject();
	}

	public interface ChangeObserver {
		void changed();
	}

	public interface ChangeChecker {
		boolean checkChange();
	}

	void release();
}