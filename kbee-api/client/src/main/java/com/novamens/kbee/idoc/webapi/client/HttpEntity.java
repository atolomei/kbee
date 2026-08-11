package com.novamens.kbee.idoc.webapi.client;

import java.io.IOException;
import java.io.InputStream;

public interface HttpEntity {
    public InputStream getStream() throws IOException;
    public long getSize();
}
