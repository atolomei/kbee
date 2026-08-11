package kbee.web.scheduler;

import java.util.List;

import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.command.ListResultSet;
import com.novamens.kbee.content.command.ListSearchResult;
import com.novamens.scheduler.AbstractCronJobRequest;


/**
 * 
 * 
 *
 */
public class CronJobRequestListResultSet extends ListResultSet<AbstractCronJobRequest> {
			
	public CronJobRequestListResultSet(List<AbstractCronJobRequest> list) {
		super(list);
	}

	@Override
	public SearchResult next() {
		AbstractCronJobRequest obj = (AbstractCronJobRequest) getIterator().next();
		return new ListSearchResult<AbstractCronJobRequest>(obj);
	}
}
