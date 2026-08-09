package kbee.web.console.tools;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.web.report.ReportColumn;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.TableStyleInfo;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GridExportQueryExcel extends AbstractGridExportQuery {
    static final private String INTERNAL_SEPARATOR = " | ";
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GridExportQueryExcel.class.getName());
    private XSSFWorkbook book;
    private URL customExcelTemplate = null;
    boolean createInTable = true;

    public GridExportQueryExcel() {
    }

    public GridExportQueryExcel(URL customExcelTemplate) {
        this.customExcelTemplate = customExcelTemplate;
    }

    @Override
    protected void internalExport(OutputStream stream, Query query, List<GridColumn<SearchResult, String>> columns, String gridTitle) throws IOException {
        InputStream customExcelTemplateIS = null;
        try {
            XSSFSheet dataExcelSheet = null;
            TableStyleInfo style = null;

            XSSFTable table = null;
            int tableStart = 2;
            if (customExcelTemplate == null) {
                final PackageResourceReference excelReportTemplate = ServiceLocator.getService(BrandingWebService.class).getExcelReportTemplate();
                if (excelReportTemplate != null && excelReportTemplate.getResource().getResourceStream() != null) {
                    customExcelTemplateIS = excelReportTemplate.getResource().getResourceStream().getInputStream();
                    this.book = new XSSFWorkbook(customExcelTemplateIS);
                } else {
                    this.book = new XSSFWorkbook();
                }
            } else {
                customExcelTemplateIS = new BufferedInputStream(customExcelTemplate.openStream());
                this.book = new XSSFWorkbook(customExcelTemplateIS);
            }

            dataExcelSheet = this.book.getSheet("Data");
            if (dataExcelSheet != null) {
                if (createInTable) {
                    table = dataExcelSheet.getTables().stream().filter(t -> t.getName().equals("Report")).findFirst().get();
                    tableStart = table.getArea().getFirstCell().getRow();
                }
            } else {
                dataExcelSheet = this.book.createSheet("Data");
                this.book.setSheetOrder("Data", 0);
            }
            ResultSet resulSet = query.execute();

            int exportableCols = (int) columns.stream().filter(col -> col.isExportable()).count();

            CTAutoFilter autoFilter = null;
            if (createInTable) {
                int tableSize = resulSet.size();
                if (tableSize == 0) tableSize = 1;
                AreaReference tableReference = book.getCreationHelper().createAreaReference(
                        new CellReference(tableStart, 0), new CellReference(tableSize + tableStart, exportableCols - 1));

                if (table == null) {
                    table = dataExcelSheet.createTable(tableReference);
                    table.setName("Report");
                    table.setDisplayName("Report");

                    /**
                     *   Style the table  
                     * */
                    if (style == null) {
                        // For now, create the initial style in a low-level way
                        table.getCTTable().addNewTableStyleInfo();
                        table.getCTTable().getTableStyleInfo().setName("TableStyleMedium2");
                        XSSFTableStyleInfo style2 = (XSSFTableStyleInfo) table.getStyle();
                        style2.setName("TableStyleMedium2");
                        style2.setFirstColumn(false);
                    } else {
                        table.setStyleName(style.getName());
                    }

                    autoFilter = table.getCTTable().addNewAutoFilter();
                } 
                else {
                    table.setArea(tableReference);
                    autoFilter = table.getCTTable().getAutoFilter();
                    //int filterColsCount = autoFilter.getFilterColumnList().size();
                    //for (int i = 0; i < filterColsCount; i++) {
                    //    autoFilter.removeFilterColumn(0);
                    //}
                }
            }

            if (gridTitle != null) {
                Row titleRow = dataExcelSheet.createRow(tableStart - 1);
                Cell cell = titleRow.createCell(0);
                cell.setCellValue(gridTitle);
            }
            int col = 0;
            int[] largestValue = new int[columns.size()];

            int current_row = tableStart;
            Row headerRow = dataExcelSheet.createRow(current_row++);
            Set<String> addedHeaders = new HashSet<>();
            for (GridColumn<SearchResult, String> column : columns) {
                if (column.isExportable()) {
                    String displayName = column.getDisplayModel().getObject();

                    if (displayName.isEmpty()) {
                        displayName = column.getId();
                    }
                    if (displayName.length() > largestValue[col]) largestValue[col] = displayName.length();
                    displayName = controlRepeatedNames(displayName, addedHeaders);
                    Cell cell = headerRow.createCell(col++);
                    cell.setCellValue(escape(displayName));
                    addedHeaders.add(displayName);
                    if (autoFilter != null) {
                        //org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilterColumn filter = autoFilter.addNewFilterColumn();
                        //filter.setColId(col);
                        //filter.setShowButton(true);
                    }
                }
            }

            while (resulSet.hasNext()) {
                SearchResult result = resulSet.next();
                col = 0;
                Row currentRow = dataExcelSheet.createRow(current_row++);
                for (GridColumn<SearchResult, String> column : columns) {
                    if (column.isExportable()) {
                        String cellText = "";
                        try {
                            if (column instanceof ReportColumn) {
                                cellText = ((ReportColumn) column).getValueModel(result).getObject();
                            } else {
                                cellText = column.getCellAsString(result).getObject();
                            }
                            if(cellText==null) cellText ="";
                        } catch (Exception e) {
                            logger.error(e);
                            cellText = e.getClass().getName();
                        }
                        if (cellText.length() > largestValue[col]) largestValue[col] = cellText.length();
                        Cell cell = currentRow.createCell(col++);
                        cell.setCellValue(escape(cellText));
                    }
                }
            }

            for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
                //dataExcelSheet.autoSizeColumn(colIdx);
                int MAGIC_SIZE_MULTIPLIER = 200;
                final int maxLengthCurrentCol = largestValue[colIdx];
                int width = MAGIC_SIZE_MULTIPLIER * (maxLengthCurrentCol > 10 ? maxLengthCurrentCol : 10) + 1500;
                if(width > 65000)
                    width = 65000;
                dataExcelSheet.setColumnWidth(colIdx, width);
            }
            current_row = 0;

            removeSheetIfExists("Filters", book);
            Sheet filterExcelSheet = this.book.createSheet("Filters");

            for (String key : query.getParameters().keySet()) {
                Row row = filterExcelSheet.createRow(current_row++);
                Cell cellKey = row.createCell(0);
                cellKey.setCellValue(key);

                Cell cellValue = row.createCell(1);
                cellValue.setCellValue(query.getParameters().get(key).toString());

            }

            this.book.write(stream);
        } catch (Exception e) {
            logger.error(e);
            throw new KbeeRuntimeException(e);
        } finally {
            try {
                if (customExcelTemplateIS != null)
                    customExcelTemplateIS.close();
            } catch (Exception e) {
                logger.error(e);
            }
            try {
                if (this.book != null)
                    this.book.close();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    private String controlRepeatedNames(String displayName, Set<String> addedHeaders) {
        String finalDisplayName = null;
        if (!addedHeaders.contains(displayName)) {
            finalDisplayName = displayName;
        } else {
            int idx = 2;
            do {
                finalDisplayName = displayName + " (" + idx++ + ")";
            } while (addedHeaders.contains(finalDisplayName));

        }
        addedHeaders.add(finalDisplayName);
        return finalDisplayName;
    }


    private void removeSheetIfExists(String sheetName, XSSFWorkbook book) {
        for (int i = book.getNumberOfSheets() - 1; i >= 0; i--) {
            XSSFSheet tmpSheet = book.getSheetAt(i);
            if (tmpSheet.getSheetName().equals(sheetName)) {
                book.removeSheetAt(i);
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
        return ".xlsx";
    }
}
