package kbee.replica;

public interface MasterReplica extends Replica {
	public void replicate(Object object);
}