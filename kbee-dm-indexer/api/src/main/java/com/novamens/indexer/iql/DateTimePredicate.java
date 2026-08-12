package com.novamens.indexer.iql;

import java.time.OffsetDateTime;

public interface DateTimePredicate extends Predicate {
	public OffsetDateTime calculateDateTime (Object object, Object arguments);
}
