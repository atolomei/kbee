package com.novamens.indexer.query;

public abstract class CubeHelper {
	private static CubeHelper Instance;

	public static CubeHelper getInstance() {
		return Instance;
	}

	public static void setInstance(CubeHelper instance) {
		Instance = instance;
	}

	abstract public Member getMember(String path);
}
