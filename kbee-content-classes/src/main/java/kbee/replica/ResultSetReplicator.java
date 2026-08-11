package kbee.replica;

import java.util.List;
import java.util.function.Function;

import kbee.api.model.ApiProxy;
import kbee.api.model.IResultSet;
import kbee.api.service.ApiService;

public class ResultSetReplicator<T> extends Replicator<T> {

	List<T> values;
	Function<ApiService, IResultSet<?>> dataSource;
	Function<T, ReplicaHandler<T>> handlerFactory;
	IResultSet<?> resultSet;
	Function<ApiProxy, T> proxyResolver;
	
	public class Iterator implements java.util.Iterator<T> {
		public boolean hasNext() {
			return getResultSet().hasNext();
		}
		@SuppressWarnings("unchecked")
		public T next() {
			Object next = getResultSet().next();
			return next instanceof ApiProxy ? resolve((ApiProxy)next) :(T)next;
		}
	}
	
	public ResultSetReplicator(Replica replica, 
			Function<ApiService, IResultSet<?>> dataSource,
			Function<T, ReplicaHandler<T>> handlerFactory,
			Function<ApiProxy, T> resolver) {
		super(replica);
		this.dataSource = dataSource;
		this.handlerFactory = handlerFactory;
		this.proxyResolver = resolver;
	}
	
	public ResultSetReplicator(Replica replica, 
			Function<ApiService, IResultSet<?>> dataSource,
			Function<T, ReplicaHandler<T>> handlerFactory) {
		super(replica);
		this.dataSource = dataSource;
		this.handlerFactory = handlerFactory;
	}
	
	public IResultSet<?> getResultSet() {
		if (resultSet == null) {
			resultSet = dataSource.apply(getReplicaApi());
		}
		return resultSet;
	}
	
	@Override
	public long getTotal() {
		return getResultSet().getSize();
	}
	
	protected T resolve(ApiProxy proxy) {
		return proxyResolver.apply(proxy);
	}
	
	@Override
	protected java.util.Iterator<T> getIterator() {
		return new Iterator();
	}
	
	protected ReplicaHandler<T> getHandler(T value) {
		return handlerFactory.apply(value);
	}
}