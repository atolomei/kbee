//Created on 11/08/2006
package com.novamens.configuration.impl;


import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections4.map.AbstractReferenceMap;
import org.apache.commons.collections4.map.ReferenceMap;

import org.apache.commons.logging.LogFactory;

import com.novamens.configuration.Configuration;

public class ObserverManagerImpl implements ObserverManager {
	private Timer timer;

	private Map<Configuration, TimerTask> configurations;

	@SuppressWarnings("unchecked")
	public void addObserver(final Configuration configuration,
			final Runnable runnable) {
		if (this.timer == null) {
			this.timer = new Timer(false);
			this.configurations = new ReferenceMap(AbstractReferenceMap.ReferenceStrength.WEAK,			AbstractReferenceMap.ReferenceStrength.HARD, true);
		}
		TimerTask task = this.configurations.get(configuration);
		if (task != null) {
			task.cancel();
		}
		task = new TimerTask() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Throwable e) {
					LogFactory.getLog(ObserverManager.class).error("Error reloading configuracion",e);
				}
			}

			@Override
			protected void finalize() throws Throwable {
				this.cancel();
				super.finalize();
			}
		};
		this.configurations.put(configuration, task);
		this.timer.schedule(task, configuration.getDelay(), configuration
				.getDelay());
	}

	public void removeObserver(final Configuration configuration) {
		final TimerTask task = this.configurations.get(configuration);
		if (task != null) {
			task.cancel();
			this.configurations.remove(configuration);
		}
		if (this.configurations.isEmpty()) {
			this.timer.cancel();
			this.timer = null;
			this.configurations = null;
		}
	}
}
