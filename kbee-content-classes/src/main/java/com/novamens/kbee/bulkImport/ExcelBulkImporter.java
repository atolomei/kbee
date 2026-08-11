package com.novamens.kbee.bulkImport;

import com.novamens.content.resource.KBFile;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.ProgressEvent;
import com.novamens.event.ProgressListener;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.util.KeyValue;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelBulkImporter {
    RowEntityLoader rowLoader;
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ExcelBulkImporter.class.getName());

    private int templateRowCount = 50000;
    private final int tableStart = 1;
    private final String importDataTableName = "ImportData";
    private long totalItems = 0;

    public ExcelBulkImporter(RowEntityLoader rowLoader) {
        this.rowLoader = rowLoader;
    }

    public void downloadTemplate(OutputStream stream) {

        XSSFWorkbook book = null;
        Map<String, Integer> headerLabelsCount = new HashMap<>();
        try {
            book = new XSSFWorkbook();
            DataFormat fmt = book.createDataFormat();
            CellStyle TextStyle = book.createCellStyle();
            TextStyle.setDataFormat(fmt.getFormat("@"));

            XSSFSheet dataExcelSheet = book.createSheet("Data");
            dataExcelSheet.setDefaultColumnStyle(0, TextStyle);
            dataExcelSheet.addIgnoredErrors(new CellRangeAddress(0, 9999, 0, 9999), IgnoredErrorType.NUMBER_STORED_AS_TEXT);

            XSSFSheet possibleValuesSheet = book.createSheet("PossibleValues");
            //book.setSheetHidden(1, true);
            //this.lockSheet(possibleValuesSheet, true);
            possibleValuesSheet.setDefaultColumnStyle(0, TextStyle);
            possibleValuesSheet.addIgnoredErrors(new CellRangeAddress(0, 9999, 0, 9999), IgnoredErrorType.NUMBER_STORED_AS_TEXT);


            List<EntityRowColumnsDefinition> columnDefinitions = rowLoader.getEntityRowColumnsDefinitions();
            int possibleValuesCol = 0;
            int mainCol = 0;

            Row headerKeysRow = dataExcelSheet.createRow(tableStart-1);
            Row headerRow = dataExcelSheet.createRow(tableStart);

            for (EntityRowColumnsDefinition cd : columnDefinitions) {
                Cell headerRowCell = headerRow.createCell(mainCol);
                headerRowCell.setCellValue(getHeaderLabel(cd.getDisplayName(), headerLabelsCount));

                Cell headerKeysCell = headerKeysRow.createCell(mainCol);
                headerKeysCell.setCellValue(cd.getColumnKey());
                if (cd.getPossibleValues() != null) {
                    createPossibleValuesMap(possibleValuesSheet, cd, possibleValuesCol);
                    String formula = getConstraintFormula(possibleValuesSheet.getSheetName(), possibleValuesCol, cd.getPossibleValues().size());
                    addColumnValidationFormula(dataExcelSheet, mainCol, tableStart+1, tableStart+templateRowCount+1, formula);

                    dataExcelSheet.setColumnWidth(mainCol, possibleValuesSheet.getColumnWidth(possibleValuesCol));
                    possibleValuesCol += 3;
                } else {
                    int colLength = cd.getDisplayName().length();
                    if (colLength < 30)
                        colLength = 30;
                    dataExcelSheet.setColumnWidth(mainCol, calculateColumnWidth(colLength));
                }
                mainCol++;
            }



            AreaReference tableReference = book.getCreationHelper().createAreaReference(
                    new CellReference(tableStart, 0), new CellReference(templateRowCount + tableStart, columnDefinitions.size() - 1));


            XSSFTable table = dataExcelSheet.createTable(tableReference);

            table.setName(importDataTableName);
            table.setDisplayName(importDataTableName);

            table.getCTTable().addNewTableStyleInfo();
            table.getCTTable().getTableStyleInfo().setName("TableStyleMedium2");
            XSSFTableStyleInfo style2 = (XSSFTableStyleInfo) table.getStyle();
            style2.setName("TableStyleMedium2");
            style2.setFirstColumn(false);
            //style2.setShowRowStripes(true);


            XSSFCellStyle [] styles = new XSSFCellStyle[2];
            styles[0] = book.createCellStyle();
            styles[0].setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styles[0].setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_50_PERCENT.getIndex());

            styles[1] = book.createCellStyle();
            styles[1].setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styles[1].setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());

            EntityRowColumnsDefinition.ColumnType prevColumnType = null;
            int idx=0;
            for (int i = 0; i < columnDefinitions.size(); i++) {
                if(columnDefinitions.get(i).getColumnType() != prevColumnType){
                    prevColumnType=columnDefinitions.get(i).getColumnType();
                    idx=(idx+1)%styles.length;
                }
                Cell cell = headerRow.getCell(i);
                cell.setCellStyle(styles[idx]);
            }

/*
            for (int i = 0; i < table.getColumns().size(); i++) {
                XSSFTableColumn column = table.getColumns().get(i);
                column.setName(columnDefinitions.get(i).getDisplayName());
            }
            table.updateHeaders();*/


            book.write(stream);
            stream.flush();
        } catch (
                Exception e) {
            logger.error(e);
        } finally {
            try {
                if (book != null)
                    book.close();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public KBFile processFile(InputStream stream, ProgressListener listener){
        XSSFWorkbook book = null;
        try {
            book = new XSSFWorkbook(stream);

            XSSFSheet dataExcelSheet = book.getSheet("Data");

            Row headerKeysRow = dataExcelSheet.getRow(tableStart-1);

            XSSFTable table = dataExcelSheet.getTables().stream().filter(tbl -> tbl.getName().equals(importDataTableName)).findFirst().orElseThrow( ()->new KbeeRuntimeException("Main table not found"));

            int columnCount = table.getColumnCount();
            int rowCount = table.getRowCount();
            //totalItems = rowCount;
            int rowStart = table.getStartRowIndex()+1;
            int colStart = table.getStartColIndex();

            List<List<RowEntityValues>> entries = new ArrayList<>();
            for (int rowIdx = rowStart; rowIdx < rowStart+rowCount; rowIdx++) {
                List<RowEntityValues> entry = new ArrayList<>();
                Row currentRow = dataExcelSheet.getRow(rowIdx);
                if(currentRow != null) {
                    for (int colIdx = 0; colIdx < colStart + columnCount; colIdx++) {
                        String key = headerKeysRow.getCell(colIdx).getStringCellValue();
                        final Cell cellValue = currentRow.getCell(colIdx);
                        if (cellValue != null) {
                            String value = getValueAsString(cellValue);
                            entry.add(new RowEntityValues(colIdx, key, value));
                        }
                    }
                    if (!entry.isEmpty())
                        entries.add(entry);
                }
            }
            final XSSFCellStyle errorStyle = book.createCellStyle();
            errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            errorStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.RED.getIndex());

            final XSSFCellStyle validStyle = book.createCellStyle();
            validStyle.setFillPattern(FillPatternType.NO_FILL);

            List<Integer> validRows = new ArrayList<>();
            totalItems = entries.size();
            for (int i = 0; i < entries.size(); i++) {
                List<RowEntityValues> entity = entries.get(i);
                Transaction trx = null;
                try {
                    trx = beginTransaction();
                    rowLoader.create(entity);
                    listener.progressUpdate(new ProgressEvent(stream, i, entries.size()));
                    validRows.add(i+rowStart);
                    trx.commit();
                    trx=null;
                }catch(BulkImportException e){

                    final XSSFRow currentRow = dataExcelSheet.getRow(i + rowStart);
                    for (RowEntityValues rowEntityValues : entity) {
                        if(rowEntityValues.getColumnIdx().equals(e.getField())){
                            currentRow.getCell(rowEntityValues.getColumnIdx()).setCellStyle(errorStyle);
                        }else{
                            currentRow.getCell(rowEntityValues.getColumnIdx()).setCellStyle(validStyle);
                        }
                    }
                }catch (Exception e) {
                    int a=1;
                }finally {
                    if (trx != null) {
                        trx.rollback();
                    }
                }
            }

            for (int i = validRows.size()-1; i >= 0; i--) {
                int rowNum = validRows.get(i);
                dataExcelSheet.removeRow(dataExcelSheet.getRow(rowNum));
            }

            File tmpFile = File.createTempFile("import_result_", ".xlsx");
            tmpFile.deleteOnExit();

            try(OutputStream os = new FileOutputStream(tmpFile.getPath())) {
                book.write(os);
                os.flush();
            }
            return getDomain().getService(DomainService.class).importFileFromLocalDisk(tmpFile);
        } catch (Exception e) {
            throw new KbeeRuntimeException(e);
        }
    }
    
    
    public long getTotalItems() {
        return totalItems;
    }

    public long getTotalItemsProcessed() {
        return 0;
    }

    private String getValueAsString(Cell cellValue) {
        switch (cellValue.getCellType()){
            case NUMERIC:
                return String.valueOf(cellValue.getNumericCellValue());
            case BOOLEAN:
                return cellValue.getBooleanCellValue() ? "TRUE" : "FALSE";
            case STRING:
            default:
                return cellValue.getStringCellValue();
        }


    }
    protected Domain getDomain() {
        return ServiceLocator.getService(UserService.class).getDomain();
    }

    protected Transaction beginTransaction() {
        return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
    }

    private short getColumnColor(EntityRowColumnsDefinition df){
        switch (df.getColumnType()){
            case NATIVE: return HSSFColor.HSSFColorPredefined.GREY_50_PERCENT.getIndex();
            case CLASSIFIER: return HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex();
            case ATTRIBUTE: return HSSFColor.HSSFColorPredefined.LIGHT_ORANGE.getIndex();
            case ROLE: return HSSFColor.HSSFColorPredefined.LIGHT_TURQUOISE.getIndex();
            default: return  HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getIndex();
        }
    }

    private String getHeaderLabel(String displayName, Map<String, Integer> headerLabelsCount) {
        if(!headerLabelsCount.containsKey(displayName)){
            headerLabelsCount.put(displayName, 1);
            return displayName;
        }else{
            Integer count = headerLabelsCount.get(displayName);
            count++;
            headerLabelsCount.put(displayName, count);
            return displayName+"_" +count;
        }
    }

    private void lockSheet(XSSFSheet sheet, boolean enable) {
        sheet.lockDeleteColumns(enable);
        sheet.lockDeleteRows(enable);
        sheet.lockFormatCells(enable);
        sheet.lockFormatColumns(enable);
        sheet.lockFormatRows(enable);
        sheet.lockInsertColumns(enable);
        sheet.lockInsertRows(enable);
        if (enable)
            sheet.enableLocking();
        else
            sheet.disableLocking();
    }

    private void addColumnValidationFormula(XSSFSheet dataExcelSheet, int column, int startRow, int endRow, String formula) {
        DataValidationHelper validationHelper = new XSSFDataValidationHelper(dataExcelSheet);
        CellRangeAddressList addressList = new CellRangeAddressList(startRow, endRow, column, column);
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(formula);
        DataValidation dataValidation = validationHelper.createValidation(constraint, addressList);
        dataValidation.setSuppressDropDownArrow(true);
        dataExcelSheet.addValidationData(dataValidation);
    }

    private String getConstraintFormula(String possibleValuesSheetName, int possibleValuesColumn, int possibleValuesCount) {
        String excelColName = excelColumnFromNumber(possibleValuesColumn + 1);
        return String.format("%S!$%S$2:$%S$%d", possibleValuesSheetName, excelColName, excelColName, possibleValuesCount+1);
    }

    private void createPossibleValuesMap(XSSFSheet sheet, EntityRowColumnsDefinition cd, int column) {

        XSSFRow possibleValuesHeaderRow = sheet.getRow(0);
        if (possibleValuesHeaderRow == null)
            possibleValuesHeaderRow = sheet.createRow(0);

        List<KeyValue<String>> possibleValues = cd.getPossibleValues();
        Cell headerCellLabel = possibleValuesHeaderRow.createCell(column);
        headerCellLabel.setCellValue(cd.getDisplayName());

        Cell headerCellKey = possibleValuesHeaderRow.createCell(column + 1);
        headerCellKey.setCellValue("key");

        for (int i = 0; i < possibleValues.size(); i++) {
            KeyValue<String> pv = possibleValues.get(i);
            XSSFRow pvRow = sheet.getRow(i + 1);
            if (pvRow == null)
                pvRow = sheet.createRow(i + 1);
            XSSFCell pvCellValue = pvRow.createCell(column);
            pvCellValue.setCellValue(pv.getDisplayName());

            XSSFCell pvCellKey = pvRow.createCell(column + 1);
            pvCellKey.setCellValue(pv.getValue());
        }
        sheet.setColumnWidth(column, calculateLabelColumnSize(cd));

    }

    private int calculateLabelColumnSize(EntityRowColumnsDefinition cd) {
        int largestValue = cd.getDisplayName().length();
        List<KeyValue<String>> possibleValues = cd.getPossibleValues();
        for (int i = 0; i < possibleValues.size(); i++) {
            String label = possibleValues.get(i).getDisplayName();
            if (label != null) {
                int curLen = label.length();
                if (curLen > largestValue)
                    largestValue = curLen;
            }
        }
        final int maxColumnLen = 60;
        if (largestValue > maxColumnLen)
            largestValue = maxColumnLen;


        final int maxLengthCurrentCol = largestValue;
        int width = calculateColumnWidth(maxLengthCurrentCol);
        return width;
    }

    private int calculateColumnWidth(int maxLengthCurrentCol) {
        int MAGIC_SIZE_MULTIPLIER = 200;
        return MAGIC_SIZE_MULTIPLIER * (maxLengthCurrentCol > 10 ? maxLengthCurrentCol : 10) + 1500;
    }


    private String excelColumnFromNumber(int column) {
        String columnString = "";
        int columnNumber = column;
        while (columnNumber > 0) {
            int currentLetterNumber = (columnNumber - 1) % 26;
            char currentLetter = (char) (currentLetterNumber + 65);
            columnString = currentLetter + columnString;
            columnNumber = (columnNumber - (currentLetterNumber + 1)) / 26;
        }
        return columnString;
    }

    private int numberFromExcelColumn(String column) {
        int retVal = 0;
        String col = column.toUpperCase();
        for (int iChar = col.length() - 1; iChar >= 0; iChar--) {
            char colPiece = col.charAt(iChar);
            int colNum = colPiece - 64;
            retVal = retVal + colNum * (int) Math.pow(26, col.length() - (iChar + 1));
        }
        return retVal;
    }


}
