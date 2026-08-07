package com.novamens.lock;

public enum LockScope {

	SHARED("shared"), EXCLUSIVE("exclusive"); 

	private final String value;

	LockScope(final String v) {
		this.value = v;
	}

	public String value() {
		return this.value;
	}

	public static LockScope fromValue(final String v) {
		for (final LockScope c : LockScope.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v.toString());
	}
}