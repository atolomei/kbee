package com.novamens.content.web.deployManagement;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;

import kbee.web.query.ConsoleQuery;

public class BackupsQuery extends ConsoleQuery {
    public BackupsQuery(Index index) {
        super(index);
    }
}
