package com.novamens.cluster;

import java.util.Map;

import com.novamens.service.SystemService;

public interface ClusterService extends SystemService {
	public <K, V> Map<K, V> getMap(String name);
	public String getNode();
}
