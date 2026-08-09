package kbee.web.console.tools;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.util.KbeeRuntimeException;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import kbee.web.report.ReportColumn;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


public class GridExportQueryExcelJXL extends AbstractGridExportQuery {
    static final private String INTERNAL_SEPARATOR = " | ";
    // static final private String SEPARATOR = ",";
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GridExportQueryExcel.class.getName());
    private WritableWorkbook book;

    public GridExportQueryExcelJXL() {
    }

    @Override
    protected void internalExport(OutputStream stream, Query query, List<GridColumn<SearchResult, String>> columns, String gridTitle) throws IOException {

        try {

            this.book = Workbook.createWorkbook(stream);

            WritableSheet dataExcelSheet = book.createSheet("Data", 0);

            ResultSet resulSet = query.execute();
            int current_row = 0;

            if (gridTitle!=null)
                dataExcelSheet.addCell(new Label(0, current_row++, gridTitle));

            int col = 0;

            for (GridColumn<SearchResult, String> column : columns) {
                if (column.isExportable()) {
                    String displayName = column.getDisplayModel().getObject();
                    if (displayName.isEmpty()) {
                        displayName = column.getId();
                    }
                    dataExcelSheet.addCell(new Label(col++, current_row, escape(displayName)));
                }
            }

            while (resulSet.hasNext()) {
                SearchResult result = resulSet.next();
                col = 0;
                current_row++;
                for (GridColumn<SearchResult, String> column : columns) {
                    if (column.isExportable()) {
                        String cellText = "";
                        try {
                            if (column instanceof ReportColumn) {
                                cellText = ((ReportColumn) column).getValueModel(result).getObject();
                            } else {
                                cellText = column.getCellAsString(result).getObject();
                            }
                        } catch (Exception e) {
                            logger.error(e);
                            cellText = e.getClass().getName();
                        }
                        dataExcelSheet.addCell(new Label(col++, current_row, cellText));
                    }
                }
            }

            current_row = 0;
            col = 0;
            WritableSheet filterExcelSheet = book.createSheet("Filters", 1);

            for (String key : query.getParameters().keySet()) {
                filterExcelSheet.addCell(new Label(0, current_row, key));
                filterExcelSheet.addCell(new Label(1, current_row, query.getParameters().get(key).toString()));
                current_row++;
            }


        } catch (Exception e) {
            logger.error(e);
            throw new KbeeRuntimeException(e);
        } finally {
            try {
                this.book.write();
                this.book.close();
            } catch (WriteException e) {
                logger.error(e);
            }
        }
    }


    protected String escape(String str) {
        if (str == null)
            return str;
        return str.replace(INTERNAL_SEPARATOR, " - ");
    }

    @Override
    public String getFileExtension() {
        return ".xls";
    }
}
