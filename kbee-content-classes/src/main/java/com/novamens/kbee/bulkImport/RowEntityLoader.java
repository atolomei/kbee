package com.novamens.kbee.bulkImport;

import java.util.List;

public interface RowEntityLoader {
    List<EntityRowColumnsDefinition> getEntityRowColumnsDefinitions();
    void create(List<RowEntityValues> rowEntityValues) throws BulkImportException;

}
