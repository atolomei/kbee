package com.novamens.transaction;

public interface Transaction {
	public void commit();
	public void rollback();
	public boolean isCompleted();
	public boolean isActive();
}
