package com.novamens.kbee.content.indexer;

import com.novamens.content.model.ObjectId;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.java.TextExtractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.util.JXPath;

import kbee.util.logging.Logger;

import org.apache.logging.log4j.LogManager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileObjectIdExtractor implements Extractor {
    private JXPath jpath;

    static Logger logger = new Logger(LogManager.getLogger(TextExtractor.class.getName()));

    public FileObjectIdExtractor() {
    }

    public FileObjectIdExtractor(String path) {
        setPath(path);
    }

    public void setPath(String path) {
        this.jpath = new JXPath(path);
    }

    public Object extract(Object object) throws IndexerException {
        try {
            Object jpathEval = jpath.evaluateAll(object);
            List<String> objIds = new ArrayList<String>();
            for (Object so : (Collection<?>) jpathEval) {
                objIds.add(new ObjectId(so).toString());
            }
            return objIds;
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error(e);
            return null;
        }
    }

}
