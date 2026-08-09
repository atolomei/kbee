package kbee.web.console.tools;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;

import kbee.web.report.ReportColumn;

import java.io.*;
import java.util.List;

public class GridExportQueryCSV extends AbstractGridExportQuery {
	static final private String INTERNAL_SEPARATOR = " | ";
	static final private String SEPARATOR = ",";

	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GridExportQueryCSV.class.getName());

	public GridExportQueryCSV() {
		super();
	}

	@Override
	protected void internalExport(OutputStream outputStream,Query query, List<GridColumn<SearchResult, String>> columns, String gridTitle) throws IOException {

		try (InputStream in = getDownloadStream(query, columns, gridTitle)) {
			int ch;
			while ((ch = in.read()) != -1) {
				outputStream.write(ch);
			}
		}
	}

	protected String escape(String str) {
		if (str == null)
			return "";
		return str.replace(SEPARATOR, "").replace(INTERNAL_SEPARATOR, " - ");
	}

	private InputStream getDownloadStream(Query query, List<GridColumn<SearchResult, String>> columns, String gridTitle) {
		
		StringBuffer filebuffer = new StringBuffer();
		ResultSet resulSet = query.execute();
		
		if (gridTitle!=null)
			filebuffer.append(escape(gridTitle));
		
		int c = 0;
		for (GridColumn<SearchResult, String> column : columns) {
			if (column.isExportable()) {
				if (c++ > 0) filebuffer.append(",");
				String displayName = column.getDisplayModel().getObject();
				if(displayName.isEmpty()){
					displayName = column.getId();
				}
				filebuffer.append(escape(displayName));
			}
		}
		filebuffer.append("\r\n");
		while (resulSet.hasNext()) {
			SearchResult result = resulSet.next();
			c = 0;
			for (GridColumn<SearchResult, String> column : columns) {
				if (column.isExportable()) {
					if (c++ > 0) 
						filebuffer.append(",");
					try {
						if (column instanceof ReportColumn) {
							filebuffer.append(escape(((ReportColumn) column).getValueModel(result).getObject()));
						} else {
							filebuffer.append(escape(column.getCellAsString(result).getObject()));
						}
					}catch(Exception e){
						logger.error(e);
						filebuffer.append(e.getClass().getName());
					}
				}
			}
			filebuffer.append("\r\n");
		}
		InputStream stream = new ByteArrayInputStream(filebuffer.toString().getBytes());
		return stream;
	}


	@Override
	public String getFileExtension() {
		return ".csv";
	}
}
