package kbee.web.console.tools;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;

import java.io.*;
import java.util.List;

public abstract class AbstractGridExportQuery implements GridExport {
					
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger( AbstractGridExportQuery.class.getName());
	
	private String working_dir;

	public AbstractGridExportQuery() {
		this.working_dir = ServiceLocator.getService(ApplicationServerService.class).getDataExportDir() + File.separator + "grid";
	}

	@Override
	public final File export(Query query, List<GridColumn<SearchResult, String>> columns, String fileName, String gridTitle) throws IOException {

		if (query == null) {
			logger.debug("query is null. File Name: " + fileName);
			throw new IllegalArgumentException("query is null. File Name: " + fileName);
		}
		
		File file = null;
		File dir = new File(this.working_dir);

		if (!dir.exists() || !dir.isDirectory())
			KbeeFileUtils.forceMkdir(new File(this.working_dir));
		
		file = new File(this.working_dir + File.separator + fileName);

		try (	FileOutputStream out = new FileOutputStream(file);
				BufferedOutputStream buffOut = new BufferedOutputStream(out)) {
				this.internalExport(buffOut, query, columns, gridTitle);
				}

		return file;
		
	}
	protected abstract void internalExport(OutputStream stream, Query query, List<GridColumn<SearchResult, String>> columns, String gridTitle) throws IOException;

}
