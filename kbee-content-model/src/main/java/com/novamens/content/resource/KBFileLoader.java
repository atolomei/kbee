package com.novamens.content.resource;

import java.io.IOException;
import java.io.InputStream;

public interface KBFileLoader {
	public InputStream getInputStream(String url) throws IOException;
	public InputStream getInputStream(KBFileProxy file) throws IOException;
	public long getSize(String url) throws IOException;
}
