package kbee.replica;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import kbee.api.service.ApiService;

public class ListReplicator<T> extends Replicator<T> {

	private List<T> values;
	private Function<ApiService, List<T>> dataSource;
	private Function<T, ReplicaHandler<T>> handlerFactory;
	
	public ListReplicator(Replica replica, 
			Function<ApiService, List<T>> dataSource,
			Function<T, ReplicaHandler<T>> handlerFactory) {
		super(replica);
		this.dataSource = dataSource;
		this.handlerFactory = handlerFactory;
	}
	
	public List<T> getValues() {
		if (values == null) {
			values = dataSource.apply(getReplicaApi());
		}
		return values;
	}
	
	@Override
	public long getTotal() {
		return getValues().size();
	}
	
	@Override
	protected Iterator<T> getIterator() {
		return getValues().iterator();
	}
	
	protected ReplicaHandler<T> getHandler(T value) {
		return handlerFactory.apply(value);
	}
}