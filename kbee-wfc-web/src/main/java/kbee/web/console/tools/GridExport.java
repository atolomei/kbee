package kbee.web.console.tools;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface GridExport {
	File export(Query query, List<GridColumn<SearchResult, String>> columns, String fileName, String gridTitle) throws IOException;
	String getFileExtension();
}
