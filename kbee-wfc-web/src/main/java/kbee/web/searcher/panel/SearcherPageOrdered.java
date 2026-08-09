package kbee.web.searcher.panel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.ReadOperation;
import com.novamens.transaction.TransactionService;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

public class SearcherPageOrdered extends Searcher {
				
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherPageOrdered.class.getName());

	public SearcherPageOrdered(Query q) {
		super(q);
	}
	
	public Iterator<SearchResult> iterator(final long first, final long count) {
		return ServiceLocator.getService(TransactionService.class).execute(new ReadOperation<Iterator<SearchResult>>() {
			public Iterator<SearchResult> execute() {
				List<SearchResult> resultList = null;
				if (count>0) 
					getResultSet().absolute((int)first+1);
				
				resultList = new ArrayList<SearchResult>((int)count);
				int index = 0;
				while (getResultSet().hasNext() && index<count) {
					try {
						((ArrayList<SearchResult>)resultList).add(getResultSet().next());
						index++;
					}
					catch (RuntimeException e) {
						logger.error(e);
						throw e;
					}
				}
				
				resultList.sort(new Comparator<SearchResult>() {

					@Override
					public int compare(SearchResult a, SearchResult b) {
						if (a!=null && b!=null && a.getObject()!=null && b.getObject()!=null) {
							if (a.getObject() instanceof Identifiable && b.getObject() instanceof Identifiable) {
								String s1 = ((Identifiable) a.getObject()).getDisplayName();
								String s2 = ((Identifiable) b.getObject()).getDisplayName();
								if (s1==null)
									s1="";
								if (s2==null)
									s2=null;
								return (s1.compareToIgnoreCase(s2));
							}
						}
						return 0;
					}
				});
				
				
				return resultList.iterator();
			}
		});
	}
}
