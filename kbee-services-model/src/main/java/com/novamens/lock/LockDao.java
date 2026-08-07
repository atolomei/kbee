package com.novamens.lock;

import java.util.List;

import com.novamens.dao.Dao;

public interface LockDao  extends Dao {
	public List<Lock> findByObject(String objectId);
	public void save(Lock lock);
	public void delete(Lock lock);
}
