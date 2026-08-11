package kbee.replica;

public interface ReplicaHandler <T> {
	public boolean replicate() throws ReplicaException;
	public Object getLocal();
}
