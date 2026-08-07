//Created on 11/08/2006
package com.novamens.configuration.impl;

import com.novamens.configuration.Configuration;

public interface ObserverManager {

	void addObserver(Configuration configuration, Runnable runnable);

	void removeObserver(Configuration configuration);

}
