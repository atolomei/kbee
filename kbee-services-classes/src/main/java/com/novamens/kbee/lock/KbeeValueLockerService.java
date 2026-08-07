package com.novamens.kbee.lock;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.novamens.lock.ValueLockerService;

public class KbeeValueLockerService implements ValueLockerService {
	
	private static int LOCK_TIMEOUT = 60;

	public class ValueLock {
		private ReadWriteLock lock = new ReentrantReadWriteLock();
		private int references = 0;
		private long time; 
		
		public void lock() {
			references++;
			lock.writeLock().lock();
			time = Instant.now().toEpochMilli();
		}
		
		public boolean trylock() {
			boolean success = false;
			try {
				success = lock.writeLock().tryLock(LOCK_TIMEOUT, TimeUnit.SECONDS);
				if (success) {
					time = Instant.now().toEpochMilli();
					references++;
				}
			}
			catch (InterruptedException e) {
			}
			return success;
		}
		
		public void unlock() {
			references--;
			lock.writeLock().unlock();
		}
		
		public int getReferences() {
			return references;
		}
		
		public long getTimeStamp() {
			return time;
		}
		
		public boolean isHeldByCurrentThread() {
			return ((ReentrantReadWriteLock)lock).writeLock().isHeldByCurrentThread();
		}
	}
	
	private ConcurrentHashMap<Serializable, ValueLock> locks = new ConcurrentHashMap<Serializable, ValueLock>();
	private Set<Serializable> lockedvalues =  Collections.synchronizedSet(new HashSet<Serializable>());

		
	public void lock(Serializable value) {
		ValueLock lock = locks.get(value);
		if (lock == null) {
			lock = new ValueLock();
			ValueLock existinglock = locks.putIfAbsent(value, lock);
			if (existinglock!=null) lock = existinglock;
		}
		lock.lock();
	}
	
	public void tryLock(Serializable value) {
		
		boolean getvalue = false;
		int v = 1;
		while (!getvalue) {
			if (lockedvalues.contains(value)) {
				value = String.valueOf(value) + "_"+ String.valueOf(v++);
				if (v>5) throw new RuntimeException("critical locks");
			}	
			else
				getvalue = true;
		}
		
		ValueLock lock = getLock(value);
		
		boolean success = false;
		while (!success) {
			success = lock.trylock();
			if (!success) {
				lockedvalues.add(value);
				value = String.valueOf(value) + "_"+ String.valueOf(v++);
				if (v>5) throw new RuntimeException("critical locks");
				lock = getLock(value);
			}
		}	
	}
	
	public void unlock(Serializable value) {
		ValueLock lock = locks.get(value);
		if (lock!=null) {
			synchronized (lock) {
				lock.unlock();
				if (lock.getReferences()<=0) {
					locks.remove(value);
				}
			}
		}
	}
	
	public void tryUnlock(Serializable value) {
		Serializable originalvalue = value;
		boolean unlock = false;
		int v=1;
		while (!unlock) {
			ValueLock lock = locks.get(value);
			if (lock!=null && !lock.isHeldByCurrentThread()) {
				value = String.valueOf(value) + "_"+ String.valueOf(v++);
			}
			else {
				if (lock!=null) {
					synchronized (lock) {
						lock.unlock();
						if (lock.getReferences()<=0) {
							locks.remove(value);
							lockedvalues.remove(value);
						}
						unlock = true;
					}
				}
				else {
					for (Serializable key : locks.keySet()) {
						if (String.valueOf(key).startsWith(String.valueOf(originalvalue) + "_")) {
							lock = locks.get(key);
							if (lock!=null && lock.isHeldByCurrentThread()) {
								synchronized (lock) {
									lock.unlock();
									if (lock.getReferences()<=0) {
										locks.remove(value);
										lockedvalues.remove(value);
									}
								}
								break;
							}
						}
					}
					unlock = true;
				}
			}
		}
	}
	
	protected ValueLock getLock(Serializable value) {
		ValueLock lock = locks.get(value);
		if (lock == null) {
			lock = new ValueLock();
			ValueLock existinglock = locks.putIfAbsent(value, lock);
			if (existinglock!=null) lock = existinglock;
		}
		return lock;
	}


}
