package com.novamens.kbee.idoc.webapi.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpFileEntity implements HttpEntity {

    private File file;

    public HttpFileEntity(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

   // @Override
    public InputStream getStream() throws IOException {
        FileInputStream stream = new FileInputStream(file);
        return stream;
    }

    //@Override
    public long getSize() {
        return getFile().length();
    }
}
